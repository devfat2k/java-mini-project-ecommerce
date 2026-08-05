# 🎯 Plan Tổng Hợp Cải Tiến Mini-Ecommerce (V2 — Full Merge)

> **Đây là bản cập nhật thay thế hoàn toàn file plan trước đó.** Gộp toàn bộ 3 nguồn: (1) 9 góp ý gốc của mentor, (2) 4 góp ý bổ sung của mentor (rate limiting, SSO, Kafka, token/IP binding), (3) 7 gợi ý bổ sung được phát hiện khi rà lại codebase thực tế theo `PROJECT_OVERVIEW.md` và `auth_security_jwt_plan.md`.
>
> **Cách sắp xếp:** Không chia theo tuần/ngày. Chia theo **Tier kiến thức + độ ưu tiên** — làm hết Tier 0 mới sang Tier 1, cứ vậy đến hết. Trong mỗi Tier, làm theo thứ tự liệt kê vì có phụ thuộc lẫn nhau (đã ghi rõ ở từng mục).
>
> **Phạm vi:** Toàn bộ plan này dừng lại **trước ngưỡng microservices** — đúng như đã thống nhất, microservices sẽ là plan riêng sau khi hoàn thành các Tier dưới đây.
>
> **Nguyên tắc làm việc giữ nguyên xuyên suốt:** Thảo luận thiết kế trước khi code cho các mục Tier 2 trở lên. Không copy code mẫu đầy đủ — chỉ đọc hiểu cơ chế + tự viết. Mỗi mục có phần "Tự kiểm tra hiểu" — trả lời được câu đó trước khi coi là xong, không chỉ code chạy được là đủ.

---

## 🗺️ Bản đồ tổng thể 5 Tier

| Tier | Tên | Bản chất | Số mục |
|---|---|---|---|
| **Tier 0** | Rủi ro nghiệp vụ & bảo mật cấp thiết | Lỗ hổng thật, có thể gây mất tiền/mất dữ liệu nếu không sửa | 6 mục |
| **Tier 1** | Nền tảng vận hành & chất lượng code | Không sửa ngay không chết, nhưng là nền để làm Tier 2+ an toàn | 6 mục |
| **Tier 2** | Kiến trúc — cần thiết kế trước khi code | Đổi cấu trúc hệ thống, rủi ro cao hơn nếu làm ẩu | 4 mục |
| **Tier 3** | Tính năng nâng cao — làm nếu còn dư sức | Giá trị học tốt nhưng không cấp thiết bằng Tier 0-2 | 2 mục |
| **Tier 4** | Chỉ cần hiểu, không code | Trả lời phỏng vấn tốt, code không đáng effort lúc này | 2 mục |

---
---

# 🔴 TIER 0 — RỦI RO NGHIỆP VỤ & BẢO MẬT CẤP THIẾT

> Đây là những lỗ hổng có thể xảy ra **thật** trên chính flow bạn đã xây (Order, Payment, Login, OTP) — không phải rủi ro lý thuyết. Ưu tiên tuyệt đối, làm trước tất cả các mục khác.

## 0.1. Concurrency Control khi trừ tồn kho — rà soát & nâng cấp

**Hiện trạng thật (đã xác nhận trong code):** `ProductEntity` và `OrderEntity` **đã có** `@Version` (Optimistic Locking), và `GlobalExceptionHandler` **đã có** handler cho `ObjectOptimisticLockingFailureException` trả về `409 Conflict`. Vậy phần nền tảng chống race condition đã tồn tại — đây không phải lỗ hổng "chưa làm gì" như đánh giá vội trước đó, mà là điểm cần **rà soát độ hoàn thiện**.

**3 câu hỏi cần tự trả lời để biết còn thiếu gì:**

1. **Khi `OrderServiceImpl` tạo Order và trừ `stock` của nhiều `ProductEntity` cùng lúc (nhiều `OrderItemRequestDto` trong 1 request) — quá trình trừ stock có nằm trong **1 `@Transactional`** không?** Nếu có 3 sản phẩm trong giỏ, trừ được sản phẩm 1 và 2 nhưng sản phẩm 3 hết hàng → phải rollback cả 3, không được để lỡ dở.
2. **Khi `ObjectOptimisticLockingFailureException` xảy ra (2 người cùng mua sản phẩm cuối), hiện tại API trả `409` cho Client — nhưng Client có được hướng dẫn retry không, hay chỉ báo lỗi và User phải tự bấm lại?** Optimistic Lock đúng chuẩn thường đi kèm cơ chế **retry tự động ở tầng Service** (thử lại N lần trước khi trả lỗi hẳn cho user) — kiểm tra xem `OrderServiceImpl` đã có `@Retryable` (Spring Retry) hay retry thủ công chưa.
3. **Optimistic Lock có đủ cho tình huống "flash sale" (100 người cùng bấm mua 1 sản phẩm còn 5 cái trong 1 giây) không?** Về lý thuyết vẫn đúng (mỗi request đều được xử lý tuần tự nhờ conflict detection), nhưng **99 trong 100 request sẽ bị 409 và phải retry**, tạo ra rất nhiều round-trip DB lãng phí. Đây là lúc cần biết tới **Redis Distributed Lock** (`SETNX` hoặc thư viện Redisson) như một lớp chắn trước DB — không bắt buộc code trong giai đoạn này, nhưng phải hiểu và nói được sự khác biệt khi phỏng vấn hỏi "Optimistic Lock có đủ cho flash sale không?".

**Việc cần làm cụ thể:**
1. Đọc lại `OrderServiceImpl.createOrder()` — xác nhận toàn bộ vòng lặp trừ stock nằm trong đúng 1 `@Transactional`, không có save() rải rác ngoài transaction.
2. Nếu chưa có retry khi optimistic lock conflict — thêm `@Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))` (cần thêm dependency `spring-retry`) hoặc tự viết vòng lặp retry thủ công trong Service (khuyến khích tự viết tay trước để hiểu cơ chế, sau đó mới đổi sang annotation).
3. Viết note (không cần code) giải thích khi nào cần nâng cấp lên Redis Distributed Lock — lưu vào "Nợ Kiến Thức" để trả lời phỏng vấn.

**Tự kiểm tra hiểu:** Optimistic Lock hoạt động dựa trên cơ chế gì để phát hiện xung đột? (Gợi ý: cột `version` — mỗi lần `UPDATE`, Hibernate tự thêm điều kiện `WHERE version = ?`; nếu 0 dòng bị ảnh hưởng nghĩa là dữ liệu đã bị người khác sửa trước, Hibernate ném `ObjectOptimisticLockingFailureException`.)

**Tiêu chí DONE:** Viết được 1 test (hoặc test thủ công bằng 2 tab Postman bắn gần như đồng thời) mô phỏng 2 request cùng mua sản phẩm còn 1 stock — xác nhận chỉ 1 request thành công, request kia nhận lỗi rõ ràng (không phải 500 abrupt) hoặc tự retry thành công nếu còn đủ stock cho request đó.

---

## 0.2. Idempotency cho Payment IPN Callback (VNPay)

**Vấn đề thật trong code:** Endpoint `GET /api/v1/payments/vnpay-ipn` là **Public** (không cần token, đúng bản chất webhook), và VNPay có cơ chế **gọi lại IPN nhiều lần** cho cùng 1 giao dịch nếu không nhận được response đúng định dạng kịp thời từ server bạn. Nếu `PaymentServiceImpl` xử lý IPN mà không kiểm tra "giao dịch này đã xử lý `SUCCESS` chưa", có nguy cơ: cập nhật `OrderStatus` lặp lại, gửi email xác nhận đơn hàng 2-3 lần cho khách, hoặc (nếu có logic cộng điểm thưởng sau này) cộng trùng.

**Kiến thức cần nắm:** **Idempotency** = gọi API nhiều lần với cùng input phải cho ra cùng 1 kết quả cuối, không được xử lý lặp lại phần side-effect (gửi email, trừ kho, đổi trạng thái). Webhook (IPN, callback thanh toán) là nơi kinh điển cần idempotency vì bản chất giao thức không đảm bảo "chỉ gọi đúng 1 lần".

**Việc cần làm:**
1. Mở `PaymentServiceImpl`, tìm method xử lý `vnpay-ipn` — kiểm tra xem bước đầu tiên có phải là **query `PaymentEntity` theo mã giao dịch VNPay trả về, kiểm tra status hiện tại** trước khi làm bất kỳ side-effect nào không.
2. Nếu chưa có: thêm guard đầu method — nếu `PaymentEntity` tương ứng đã có status `SUCCESS`/`COMPLETED` rồi thì **return ngay response hợp lệ cho VNPay** (VNPay cần nhận đúng response format để họ dừng retry) mà **không** chạy lại logic cập nhật Order/gửi email.
3. Đảm bảo việc "check status cũ" + "update status mới" nằm trong **1 transaction** (tránh chính idempotency-check này lại bị race condition — 2 IPN gọi gần như đồng thời cùng đọc thấy status cũ trước khi cái đầu tiên kịp update).

**Tự kiểm tra hiểu:** Vì sao không thể chỉ dựa vào Idempotency Key do Client tự gửi (như cách nhiều API khác dùng `Idempotency-Key` header) cho trường hợp VNPay IPN? (Gợi ý: VNPay là bên thứ 3 gọi vào server bạn, bạn không kiểm soát được header họ gửi — phải tự tạo idempotency dựa trên dữ liệu nghiệp vụ đã có sẵn, ở đây là mã giao dịch/`orderId` do chính bạn sinh ra lúc tạo `CreatePaymentResponseDto`.)

**Tiêu chí DONE:** Gọi lại `vnpay-ipn` 2 lần với cùng payload (mô phỏng bằng Postman) — lần 2 không tạo thêm email, không đổi lại trạng thái Order đã `DONE`/`CONFIRMED`, log rõ ràng "giao dịch đã xử lý trước đó, bỏ qua".

---

## 0.3. Rate Limiting cho Login / OTP (`/login`, `/verify-otp`, `/resend-otp`, `/forgot-password`)

**Vấn đề:** 4 endpoint Public này (`AuthController`) hiện không giới hạn số lần gọi — dễ bị brute-force (dò password) hoặc spam OTP (tốn email quota Brevo, tốn tài nguyên).

**Vì sao ưu tiên cao:** Bạn đã có `Redis` connector sẵn (`RedisCacheConfig`), tận dụng ngay không cần thêm hạ tầng mới. `OtpServiceImpl` đã có sẵn khái niệm cooldown 60s cho resend — rate limiting là mở rộng logic tương tự nhưng tổng quát hơn (giới hạn theo cửa sổ thời gian, không chỉ 1 khoảng cooldown cố định).

**Kiến thức cần nắm trước khi code:**
- **Fixed Window Counter** (đơn giản nhất, đủ dùng): dùng `RedisTemplate`, key dạng `rate_limit:login:{email hoặc IP}`, lệnh `INCR` + `EXPIRE` (chỉ set TTL ở lần `INCR` đầu tiên trả về 1). Nếu giá trị vượt ngưỡng (VD 5 lần / 15 phút) trong TTL còn hiệu lực → chặn, trả `429 Too Many Requests`.
- Phân biệt rate limit theo **IP** (chống 1 nguồn tấn công nhiều tài khoản) và theo **email/username** (chống brute-force đúng 1 tài khoản từ nhiều IP khác nhau) — nên áp dụng **cả 2 lớp** cho `/login`.
- HTTP status chuẩn cho rate limit: `429 Too Many Requests`, kèm header `Retry-After` (giây) — cần thêm exception mới `RateLimitExceededException` và handler tương ứng trong `GlobalExceptionHandler` (đã có sẵn 22 handler, thêm 1 cái nữa theo đúng pattern hiện có).

**Việc cần làm:**
1. Tạo `RateLimitService` (interface + impl) dùng `StringRedisTemplate`, method `boolean isAllowed(String key, int maxRequests, Duration window)`.
2. Áp dụng vào `AuthServiceImpl`/`OtpServiceImpl` ở đầu các method `login()`, `verifyOtp()`, `generateAndSendOtp()` (đã có `ResendCooldownException` riêng cho resend — rate limiting chung là lớp bảo vệ bổ sung, không thay thế cooldown đã có).
3. Đề xuất ngưỡng khởi điểm: `login` — 5 lần/15 phút theo email; `verify-otp` — đã có giới hạn `attempts >= 5` per OTP record rồi (khác cơ chế, giữ nguyên), rate limit thêm theo IP để chặn dò nhiều email khác nhau.
4. Thêm `RateLimitExceededException extends RuntimeException`, handler trả `429` trong `GlobalExceptionHandler`.

**Tự kiểm tra hiểu:** Vì sao dùng `INCR` + `EXPIRE` của Redis mà không dùng cách "đọc số đếm hiện tại rồi cộng 1 rồi lưu lại" bằng code Java thông thường? (Gợi ý: `INCR` của Redis là **atomic** ở tầng server Redis — tránh race condition giữa "đọc" và "ghi" khi nhiều request tới gần như đồng thời, còn cách làm thủ công bằng Java sẽ có khoảng hở giữa read và write.)

**Tiêu chí DONE:** Gọi `/login` sai password 6 lần liên tiếp trong 15 phút → lần thứ 6 nhận `429`, không phải `401` như 5 lần trước.

---

## 0.4. Redis — Token theo thiết bị / Check IP bất thường (nâng cấp Refresh Token)

**Hiện trạng schema thật (`refresh_tokens` table theo `auth_security_jwt_plan.md` Giai đoạn E2):** `id`, `user_id`, `token_hash`, `expires_at`, `revoked`, `created_at`. **Chưa có cột lưu thiết bị/IP** — đây chính xác là khoảng trống thầy chỉ ra.

**Kiến thức cần nắm:**
- **Device Fingerprinting cơ bản:** không cần công nghệ phức tạp — chỉ cần lưu lại `User-Agent` header (thô hoặc hash lại) và `IP address` (lấy từ `HttpServletRequest.getRemoteAddr()`, lưu ý nếu có proxy/load balancer phía trước thì cần đọc header `X-Forwarded-For` thay vì `getRemoteAddr()` trực tiếp) tại **thời điểm cấp Refresh Token** (lúc login).
- **So sánh lúc dùng Refresh Token (endpoint `/refresh-token`):** so `User-Agent`/IP hiện tại với lúc cấp token. Có 3 mức phản ứng khi lệch, chọn 1 mức phù hợp độ phức tạp:
  - **Mức 1 (khuyến nghị làm trước — rẻ nhất):** Chỉ **log cảnh báo** (log level WARN, ghi rõ `userId`, IP cũ/mới, User-Agent cũ/mới) — chưa chặn gì cả. Đây đã là điểm cộng lớn khi phỏng vấn vì thể hiện tư duy audit/observability.
  - **Mức 2:** Nếu lệch **User-Agent hoàn toàn** (đổi hẳn loại thiết bị/trình duyệt, không phải do update version nhỏ) → tự động `revoked = true` token đó, bắt buộc login lại.
  - **Mức 3 (nâng cao, không bắt buộc):** Tính khoảng cách địa lý giữa 2 IP (cần dùng thêm service tra cứu GeoIP) để phát hiện "Impossible Travel" (đăng nhập Hà Nội, 5 phút sau refresh token lại từ IP nước ngoài) — chỉ cần biết khái niệm này tồn tại, không cần code.

**Việc cần làm:**
1. Viết migration mới `V4__add_device_info_to_refresh_tokens.sql`, thêm 2 cột `device_info VARCHAR(500)` (lưu User-Agent) và `ip_address VARCHAR(45)` (đủ dài cho IPv6) vào bảng `refresh_tokens` — cột mới cho phép `NULL` vì token cũ đã tồn tại trước đó không có data này.
2. Sửa `RefreshTokenEntity` thêm 2 field tương ứng.
3. Sửa `AuthServiceImpl.login()` — lúc tạo `RefreshTokenEntity` mới, lấy `User-Agent` từ `HttpServletRequest` (`@RequestHeader("User-Agent")` hoặc inject `HttpServletRequest` vào Controller rồi truyền xuống Service) và IP, lưu kèm.
4. Sửa endpoint `/refresh-token` — khi verify token hợp lệ, so sánh `device_info`/`ip_address` hiện tại với lưu trong DB, log cảnh báo nếu lệch (bắt đầu ở Mức 1).

**Tự kiểm tra hiểu:** Tại sao không nên **chặn cứng ngay lập tức** (Mức 2) làm mặc định cho mọi trường hợp lệch IP? (Gợi ý: user dùng mạng 4G di động hoặc đổi WiFi rất thường xuyên đổi IP hợp pháp trong đời sống thật — chặn cứng theo IP dễ gây trải nghiệm tệ, false positive cao; đây là lý do các hệ thống lớn thường chỉ dùng IP để "tăng độ nghi ngờ" kết hợp nhiều tín hiệu khác, không dùng đơn lẻ để chặn.)

**Tiêu chí DONE:** Login từ Postman với `User-Agent` giả lập A → refresh token thành công. Đổi header `User-Agent` sang giá trị khác hẳn, gọi lại `/refresh-token` với cùng refresh token → thấy log cảnh báo xuất hiện, đồng thời API vẫn trả token mới bình thường (Mức 1, chưa chặn).

---

## 0.5. Testing Coverage cho 3 luồng rủi ro cao nhất

**Vấn đề:** Mục "Unit Tests" đã nằm sẵn trong TODO (`PROJECT_OVERVIEW.md` mục 19.2) nhưng chưa làm. Đặt lại ở đây vì đây chính là **lưới an toàn bắt buộc** trước khi động tay vào bất kỳ mục Tier 0 nào ở trên (0.1-0.4 đều sửa logic Auth/Order/Payment — không có test, rất dễ sửa xong mà không biết có vỡ luồng cũ hay không).

**Kiến thức cần nắm:**
- `JUnit 5` + `Mockito` — đã có sẵn `spring-boot-starter-test` trong `pom.xml`, chưa cần thêm dependency.
- Phân biệt **Unit Test** (test 1 Service method, mock hết Repository/dependency khác bằng `@Mock`/`@InjectMocks`) vs **Integration Test** (`@SpringBootTest`, chạy thật với DB — có thể dùng Testcontainers để spin PostgreSQL thật trong Docker lúc chạy test, tránh test dựa vào H2 khác biệt hành vi so với PostgreSQL production).

**Việc cần làm — ưu tiên đúng 3 luồng, không dàn trải:**
1. **`OrderServiceImpl.createOrder()`** — test case: tạo order thành công trừ đúng stock; tạo order khi 1 sản phẩm không đủ stock → ném `InsufficientStockException`, toàn bộ transaction rollback (không sản phẩm nào bị trừ); test race condition ở mức đơn giản (2 thread cùng gọi, dùng `CountDownLatch` để mô phỏng gần như đồng thời).
2. **`PaymentServiceImpl` xử lý IPN** — test case: IPN hợp lệ lần đầu → update đúng status; gọi IPN lần 2 với cùng data (test cho mục 0.2 vừa làm) → không xử lý lại, không lỗi.
3. **`AuthServiceImpl` — luồng JWT refresh token** — test case: refresh với token hợp lệ → access token mới; refresh với token đã `revoked` → `InvalidRefreshTokenException`; refresh với token hết hạn → tương tự.

**Tự kiểm tra hiểu:** Vì sao Unit Test cho `OrderServiceImpl` nên **mock** `ProductRepository` thay vì gọi DB thật, trong khi Integration Test lại **cần** DB thật? (Gợi ý: Unit test mục tiêu là test đúng **logic nghiệp vụ** trong Service — nhanh, không phụ thuộc hạ tầng, chạy được hàng trăm lần/giây; Integration test mục tiêu là test **toàn bộ pipeline thật** bao gồm cả SQL, transaction, constraint DB — chậm hơn nhưng bắt được lỗi mà mock không bắt được, VD lỗi do đúng cú pháp SQL Hibernate sinh ra.)

**Tiêu chí DONE:** Chạy `./mvnw test` — tối thiểu 8-10 test case cho 3 luồng trên đều pass, và **chủ động cho 1 test fail thử** (VD sửa tạm code Service cho sai) để xác nhận test thật sự bắt được lỗi, không phải test giả (test luôn pass bất kể code đúng sai).

---

## 0.6. Chuẩn hóa Global Exception Handler — audit tính nhất quán

**Vấn đề:** Với 22 handler hiện có trong `GlobalExceptionHandler`, khi thêm handler mới (như `RateLimitExceededException` ở mục 0.3), cần đảm bảo **format response giống hệt các handler cũ** — tránh tình trạng phổ biến là code phát triển dần theo thời gian khiến response bị lệch chuẩn giữa các handler khác nhau.

**Việc cần làm:**
1. Rà lại toàn bộ 22 handler — xác nhận **tất cả** đều trả về đúng cấu trúc `ApiResponse<T>` (đã định nghĩa ở mục 16 `PROJECT_OVERVIEW.md`: `code`, `message`, `data`, `errors`, `timestamp`), không có handler nào trả raw string hoặc cấu trúc khác.
2. Kiểm tra riêng `MethodArgumentNotValidException` (lỗi validation) — field `errors` có đang trả về dạng **map field → message rõ ràng** (VD `{"email": "Email không hợp lệ"}`) để FE dễ hiển thị lỗi đúng field hay không, hay chỉ trả 1 message chung chung.
3. Viết thêm handler mới cho các exception sắp bổ sung (`RateLimitExceededException` ở 0.3) theo đúng convention đã audit ở bước 1-2.

**Tự kiểm tra hiểu:** Vì sao dùng `@RestControllerAdvice` tập trung 1 chỗ tốt hơn try-catch rải rác trong từng Controller/Service? (Gợi ý: tách biệt rõ "xử lý nghiệp vụ" (Service) khỏi "định dạng lỗi trả về Client" (tầng Exception Handling) — Single Responsibility; đồng thời đảm bảo format lỗi nhất quán toàn hệ thống chỉ từ 1 nơi duy nhất, dễ maintain khi có 39+ endpoint.)

**Tiêu chí DONE:** Gọi thử 5 endpoint khác nhau cố tình gây lỗi (400, 401, 403, 404, 409) — response body của cả 5 đều đúng 1 khuôn `ApiResponse<T>`, khác nhau đúng ở `code`/`message`, không lệch cấu trúc.

---
---

# 🟠 TIER 1 — NỀN TẢNG VẬN HÀNH & CHẤT LƯỢNG CODE

> Tier này không phải lỗ hổng cấp thiết như Tier 0, nhưng là nền tảng bắt buộc phải có **trước khi** làm các thay đổi kiến trúc lớn hơn ở Tier 2 (RBAC động, package-by-feature...) — vì nếu chưa có logging/audit/test tốt, sửa kiến trúc rất dễ gây lỗi âm thầm không phát hiện được.

## 1.1. Cron ra `application.yaml` *(Mentor #8)*

**Hiện trạng:** `OtpCleanupScheduler` hardcode `@Scheduled(cron = "0 0 * * * *")`. Tương tự, scheduler quét payment hết hạn (`PaymentExpiredInPaymentServiceImpl`, chạy mỗi 2 phút theo `PROJECT_OVERVIEW.md` mục 5) cũng cần kiểm tra tương tự.

**Việc cần làm:**
1. Thêm key `app.scheduler.otp-cleanup.cron` và `app.scheduler.payment-expired.cron` vào `application.yaml`, cho phép override qua env var: `${OTP_CLEANUP_CRON:0 0 * * * *}`.
2. Sửa 2 nơi dùng `@Scheduled(cron = "${...}")` thay vì hardcode string.

**Tự kiểm tra hiểu:** Nếu thiếu khai báo property mà vẫn dùng `${app.scheduler.otp-cleanup.cron}` trong `@Scheduled`, lỗi gì xảy ra lúc khởi động ứng dụng?

**Tiêu chí DONE:** Đổi lịch chạy chỉ bằng sửa biến môi trường, không sửa code Java, không cần build lại image.

---

## 1.2. BaseEntity + JPA Auditing (createdBy/updatedBy) *(Mentor #6)*

**Hiện trạng:** Entity hiện tại dùng `@CreationTimestamp`/`@UpdateTimestamp` riêng lẻ (VD `UserEntity` có `createdAt`/`updatedAt`), nhưng **không có field `createdBy`/`updatedBy`**, và không có class cha chung — mỗi Entity tự khai báo lại.

**Kiến thức cần nắm:**
- `@MappedSuperclass` — class cha không tạo bảng riêng, các Entity con kế thừa field qua annotation này.
- `@EntityListeners(AuditingEntityListener.class)` + `@EnableJpaAuditing` (bật ở class `MiniEcommerceApplication` hoặc 1 `@Configuration` riêng).
- `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`.
- `AuditorAware<T>` — bạn phải tự implement, lấy user hiện tại từ `SecurityContextHolder.getContext().getAuthentication()` → cast sang `UserPrincipal` (đã có sẵn trong package `security/`).

**Việc cần làm:**
1. Đọc doc chính thức Spring Data JPA Auditing trước khi code: `https://docs.spring.io/spring-data/jpa/reference/auditing.html`.
2. Tạo `BaseEntity` (`@MappedSuperclass`) với 4 field: `createdAt`, `updatedAt`, `createdBy`, `updatedBy` — kiểu `createdBy`/`updatedBy` nên là `String` (lưu email hoặc username) để đơn giản, không cần FK sang `UserEntity`.
3. Viết `AuditorAwareImpl implements AuditorAware<String>`.
4. Thêm `@EnableJpaAuditing`.
5. Cho `ProductEntity`, `OrderEntity`, `CategoryEntity`, `PaymentEntity` `extends BaseEntity`, **bỏ field `createdAt`/`updatedAt` cũ trùng lặp nếu có**.
6. **Viết migration mới `V5__add_audit_columns.sql`** (sau V4 ở mục 0.4) — thêm cột `created_by`, `updated_by` (cho phép `NULL` vì data cũ không có) vào các bảng liên quan. **Không sửa lại V1/V2/V3 đã chạy** — bài học từ lỗi Flyway checksum mismatch trước đây.

**Tự kiểm tra hiểu:** Tại sao `AuditorAware` cần tự implement thay vì Spring tự động biết? (Gợi ý: khái niệm "user đang đăng nhập" là logic nghiệp vụ riêng của ứng dụng, Spring chỉ cung cấp "móc" để cắm vào, không tự biết `UserPrincipal` của bạn là gì.)

**Tiêu chí DONE:** Sửa 1 Product qua API → kiểm tra DB thấy `updated_by` tự động điền đúng email user đang login, không set thủ công trong Service.

---

## 1.3. Correlation ID + MDC Logging

**Vấn đề:** Log hiện tại (SLF4J mặc định) không có cách nào biết "dòng log này thuộc request nào" khi nhiều request chạy song song — đặc biệt rối với `@Async("emailTaskExecutor")` (gửi email OTP, gửi email đơn hàng) vì log của thread async tách rời khỏi log request gốc đã tạo ra nó.

**Kiến thức cần nắm:**
- `MDC` (Mapped Diagnostic Context) của SLF4J — cơ chế gắn key-value (VD `correlationId`) vào context của 1 thread, tự động xuất hiện trong mọi dòng log của thread đó nếu cấu hình đúng pattern trong `logback`/`application.yaml`.
- Vấn đề MDC **không tự propagate** sang thread pool khác (`@Async` chạy ở thread riêng của `emailTaskExecutor`) — cần chủ động copy MDC context sang thread mới, thường làm qua 1 `TaskDecorator` custom gắn vào `ThreadPoolTaskExecutor` trong `AsyncConfig`.

**Việc cần làm:**
1. Viết 1 `Filter` (`OncePerRequestFilter`) đặt sớm trong chain (trước `JwtAuthenticationFilter`), sinh `UUID` mới cho mỗi request, gọi `MDC.put("correlationId", uuid)`, nhớ `MDC.clear()` ở `finally` sau khi request xử lý xong (tránh leak context sang request khác dùng lại cùng thread trong thread pool Tomcat).
2. Sửa pattern log trong `application.yaml`/`logback-spring.xml` thêm `%X{correlationId}` vào format.
3. Viết `MdcTaskDecorator implements TaskDecorator`, gắn vào `emailTaskExecutor` trong `AsyncConfig` — copy MDC context từ thread gốc sang thread async trước khi chạy task.

**Tự kiểm tra hiểu:** Vì sao phải `MDC.clear()` trong `finally` chứ không phải để tự nhiên hết? (Gợi ý: thread trong Tomcat thread pool được **tái sử dụng** cho request tiếp theo — nếu không clear, request sau vô tình "thừa hưởng" `correlationId` của request trước, gây log sai lệch nghiêm trọng, rất khó debug vì lỗi chỉ xuất hiện ngẫu nhiên.)

**Tiêu chí DONE:** Gọi 1 API tạo Order (có trigger gửi email async) — tìm trong log thấy **cùng 1 `correlationId`** xuất hiện ở cả dòng log của Controller/Service (thread chính) lẫn dòng log gửi email (thread async).

---

## 1.4. Spring Boot Actuator — Health Check chuẩn

**Vấn đề:** Chưa rõ Render đang health-check app bằng cách nào ngoài HTTP 200 ở root — không đủ để biết DB/Redis có thật sự sống hay không.

**Việc cần làm:**
1. Thêm dependency `spring-boot-starter-actuator`.
2. Bật `management.endpoints.web.exposure.include=health,info` trong `application.yaml` (không expose toàn bộ endpoint actuator ra Public — cân nhắc bảo vệ `/actuator/**` bằng `SecurityConfig`, chỉ cho phép nội bộ/ADMIN).
3. Actuator tự động detect DB (qua `DataSource` bean) và Redis (qua `RedisConnectionFactory` bean) đã có sẵn trong context — endpoint `/actuator/health` sẽ tự báo cáo `status: DOWN` nếu 1 trong 2 mất kết nối, không cần code thêm gì nếu chỉ cần mức cơ bản này.
4. Cập nhật `deploy_render_plan.md`/cấu hình Render trỏ health check path sang `/actuator/health` thay vì root `/`.

**Tự kiểm tra hiểu:** Vì sao không nên expose toàn bộ endpoint Actuator (VD `/actuator/env`, `/actuator/beans`) ra Public? (Gợi ý: các endpoint này lộ thông tin cấu hình nhạy cảm — biến môi trường, secret, cấu trúc nội bộ hệ thống — là mục tiêu trinh sát yêu thích của attacker nếu vô tình public.)

**Tiêu chí DONE:** Tắt tạm Redis container (`docker compose stop redis`) → gọi `/actuator/health` → thấy `status: DOWN` với chi tiết Redis component fail, không phải app crash hoàn toàn.

---

## 1.5. Database Indexing Audit

**Hiện trạng:** `V1__init_mini_shop.sql` đã có 9 index sẵn (theo `PROJECT_OVERVIEW.md` mục 6.1) và `otp_verifications` có `idx_otp_user_purpose` — nền tảng khá tốt, cần audit xem còn thiếu chỗ nào sau khi các tính năng mới ở Tier 0 thêm cột/bảng.

**Việc cần làm:**
1. Liệt kê lại toàn bộ cột dùng trong `WHERE`/`JOIN`/`ORDER BY` ở các Repository — đặc biệt `OrderRepository` (query theo `user_id`, có thể theo `status`), `RefreshTokenRepository` (query theo `token_hash` — xác nhận đã có index từ E2, và giờ thêm `ip_address`/`device_info` từ mục 0.4 có cần index không — thường KHÔNG cần vì chỉ dùng để so sánh/log, không dùng để tìm kiếm).
2. Xác nhận `products.category_id` (FK) đã có index chưa — JPA/Hibernate **không tự tạo index cho FK**, phải khai báo tường minh trong migration bằng `CREATE INDEX`.
3. Nếu phát hiện thiếu, viết migration mới bổ sung (gộp chung đợt với V4/V5 ở trên nếu tiện, hoặc tách riêng `V6__add_missing_indexes.sql`).

**Tự kiểm tra hiểu:** Vì sao JPA không tự tạo index cho cột FK dù đã tạo constraint FK? (Gợi ý: FK constraint đảm bảo **tính toàn vẹn dữ liệu** — không cho insert giá trị không tồn tại ở bảng cha, đây là việc của tầng ràng buộc dữ liệu; index là quyết định **tối ưu hiệu năng truy vấn**, hoàn toàn tách biệt về mục đích, và Hibernate mặc định để DBA/developer tự quyết định index nào cần thiết dựa trên pattern truy vấn thực tế thay vì tự đoán.)

**Tiêu chí DONE:** Chạy `EXPLAIN ANALYZE` (PostgreSQL) cho 2-3 query hay dùng nhất (VD lấy Order theo `user_id`, lấy Product theo `category_id`) — xác nhận thấy `Index Scan` thay vì `Seq Scan` trong execution plan.

---

## 1.6. Tách Controller Admin/User *(Mentor #4)*

**Hiện trạng:** `ProductController` (11 endpoint), `CategoryController` (5), `UserController` (6), `OrderController` (4) đang gộp chung endpoint admin và user, phân biệt chỉ bằng `@PreAuthorize` trên từng method — đối chiếu với bảng phân quyền mục 22.5 `PROJECT_OVERVIEW.md` đã liệt kê rõ nhóm nào thuộc Admin, dễ dùng làm checklist khi tách.

**Việc cần làm:**
1. Dùng đúng bảng mục 22.5 làm nguồn sự thật để phân loại từng endpoint: Public / Authenticated (User) / Admin Only.
2. Convention: tách theo package `controller/admin/AdminProductController.java` + `controller/user/ProductController.java` (rõ ràng nhất, dễ nói trong phỏng vấn).
3. Làm từng controller: `ProductController` trước (phức tạp nhất, có `top-buy`, `revenue-*` là Admin-only) → `CategoryController` → `UserController` → `OrderController`.
4. Đơn giản hóa lại `SecurityConfig` — match theo path prefix `/api/v1/admin/**` thay vì rải rác từng method.
5. Cập nhật Swagger tags rõ nhóm Admin/User để Frontend dễ tích hợp theo đúng mục 22.5 đã document.

**Tự kiểm tra hiểu:** Sau khi tách, việc match theo `/api/v1/admin/**` trong `SecurityConfig` có rủi ro gì nếu 1 dev sau này lỡ tạo route mới nhưng quên đặt đúng prefix? (Gợi ý: route đó sẽ rơi vào nhóm mặc định — cần đảm bảo `SecurityConfig` có 1 rule "catch-all" hợp lý ở cuối, VD mặc định yêu cầu `authenticated()` thay vì vô tình để lọt thành `permitAll()`.)

**Tiêu chí DONE:** Không còn controller nào chứa cả endpoint admin và user; gọi thử 1 endpoint admin bằng token USER thường → nhận đúng `403`, không phải lỗi định tuyến.

---
---

# 🟡 TIER 2 — KIẾN TRÚC: CẦN THIẾT KẾ TRƯỚC KHI CODE

> Từ đây trở đi, mỗi mục **bắt buộc thảo luận thiết kế trước** (đúng cách làm việc đã thống nhất) vì ảnh hưởng cấu trúc rộng, rủi ro phá vỡ code đang chạy cao hơn hẳn Tier 0-1.

## 2.1. Audit mapping One-to-Many / Many-to-One theo best practice *(Mentor #7)*

**Chỉ đọc + tự audit, không refactor nếu code hiện tại đã ổn — trừ khi phát hiện lỗi thật.**

**Việc cần làm:**
1. Đọc các bài viết cốt lõi trên `vladmihalcea.com`: `bidirectional @OneToMany`, `orphanRemoval`, `Set` thay vì `List` cho `@OneToMany`, N+1 query problem, vì sao `FetchType.LAZY` nên là mặc định cho `@OneToMany`/`@ManyToMany`.
2. Tự audit từng quan hệ Entity thật trong project:
   - `OrderEntity` ↔ `OrderItemEntity` (1-N) — cascade khi xóa Order có xóa theo OrderItem không (`orphanRemoval`)? Fetch type đang LAZY hay vô tình EAGER?
   - `CategoryEntity` ↔ `ProductEntity` (1-N) — có bidirectional không hay chỉ 1 chiều? (Nhớ lại: `CategoryHasProductsException` đã tồn tại — nghĩa là logic check "category còn product không trước khi xóa" đã có, nhưng cách check hiện tại là query riêng hay dựa vào mapping bidirectional?)
   - `UserEntity` ↔ `RefreshTokenEntity`, `UserEntity` ↔ `OtpVerificationEntity` — 1 user có thể có nhiều refresh token cùng lúc (đăng nhập nhiều thiết bị, đã ghi rõ trong E2 của `auth_security_jwt_plan.md`) — mapping có phản ánh đúng N-1 không hay bị thiết kế nhầm thành 1-1?
3. Ghi chú lại "Nợ Kiến Thức" cho bất kỳ điểm nào phát hiện chưa tối ưu nhưng quyết định không sửa ngay.

**Tự kiểm tra hiểu:** Vì sao Vlad Mihalcea khuyến nghị dùng `Set` thay vì `List` cho `@OneToMany`? (Gợi ý: Hibernate xử lý remove/reorder trên `List` được đánh index kém hiệu quả hơn — với `List` không có `@OrderColumn`, Hibernate có thể phải xóa và insert lại toàn bộ collection thay vì chỉ xóa đúng 1 phần tử, gây ra nhiều câu lệnh SQL dư thừa hơn cần thiết.)

**Tiêu chí DONE:** Vẽ được sơ đồ quan hệ Entity trên giấy, giải thích rõ FetchType + cascade behavior của từng quan hệ khi được hỏi trực tiếp.

---

## 2.2. Dynamic RBAC dưới DB *(Mentor #3, scope tối giản)*

**Hiện trạng:** Role tĩnh `enum Role { USER, ADMIN }` trong package `enums`, phân quyền hardcode `@PreAuthorize`.

**⚠️ Cần ngồi thảo luận thiết kế trước khi code** vì ảnh hưởng: JWT payload, `UserPrincipal`, `CustomUserDetailsService`, và toàn bộ 39 endpoint đang dùng `@PreAuthorize`.

**Kiến thức cần nắm:**
- Mô hình DB: `roles`, `permissions`, `role_permissions` (many-to-many).
- `CustomUserDetailsService` cần sửa lại: load permission từ DB thay vì hardcode từ enum.
- Cân nhắc cache permission bằng Redis (đã có sẵn) để tránh query DB mỗi request.

**Scope tối giản đề xuất (Minimum Viable, không làm UI quản lý):**
1. Migration mới tạo 3 bảng, seed data tương đương hành vi hiện tại (`ADMIN` = full quyền, `USER` = quyền giới hạn) — **không đổi behavior**, chỉ đổi nơi lưu trữ.
2. `CustomUserDetailsService` sửa lại load permission từ DB.
3. Không cần API cho Admin tự thêm/sửa permission trong giai đoạn này — để lại làm Technical Debt tiếp theo.

**Tự kiểm tra hiểu:** Nếu nhét permission vào JWT claims lúc login, Admin thu hồi quyền ngay sau đó — user dùng token cũ có bị chặn không? (Gợi ý: KHÔNG — đây là lý do nhiều hệ thống không nhét permission vào JWT mà chỉ nhét `userId`/`roleId`, query permission fresh mỗi request hoặc cache TTL ngắn ở Redis.)

**Tiêu chí DONE (scope tối giản):** Xóa 1 permission khỏi `role_permissions` trong DB → user gọi API tương ứng bị `403` ngay, không cần deploy lại.

---

## 2.3. 3-Environment Config: dev / staging / prod *(Mentor #1, phần khái niệm)*

**Hiện trạng:** Chỉ có `application-dev.yaml`/`application-prod.yaml` (2 tầng). Về Jenkins — bạn dùng GitHub Actions, giữ nguyên (xem lý do ở Tier 4).

**Việc cần làm:**
1. Tạo `application-staging.yaml` — copy từ prod, khác biệt: log level cao hơn 1 chút, DB/Redis instance riêng (free tier Render thứ 2, hoặc tái dùng dev instance với schema riêng nếu hạn chế ngân sách).
2. Cập nhật GitHub Actions: thêm job deploy staging trước, chỉ deploy prod sau khi staging pass (hoặc dùng branch riêng `staging`/`main` để đơn giản hóa với quy mô 1 dev).
3. Document lại trong `PROJECT_OVERVIEW.md` mục Config: vai trò từng tầng.

**Tự kiểm tra hiểu:** Vì sao staging cần "giống prod nhất có thể" thay vì chỉ cần "khác dev là đủ"? (Gợi ý: mục đích staging là bắt được bug chỉ xuất hiện dưới điều kiện gần giống thật — nếu staging cấu hình sai khác nhiều so với prod, sẽ bỏ lỡ đúng loại bug quan trọng nhất mà staging sinh ra để bắt.)

**Tiêu chí DONE:** Deploy độc lập lên staging để test trước khi merge vào nhánh deploy prod; giải thích rõ sự khác biệt 3 tầng khi được hỏi.

---

## 2.4. Pilot Package-by-Feature trên 1 domain *(Mentor #2, phạm vi thu nhỏ)*

**Hiện trạng:** Cấu trúc package-by-layer (`controller/`, `service/`, `repository/`, `dto/`, `entity/` ở top level, domain trộn lẫn bên trong) — xác nhận đúng theo cấu trúc thư mục mục 5 `PROJECT_OVERVIEW.md`.

**Vì sao chỉ pilot 1 domain:** Restructure toàn bộ rủi ro cao, tốn thời gian không tương xứng ở giai đoạn này — quyết định lùi lại là hợp lý, chỉ cần 1 case thực tế để trả lời phỏng vấn có chiều sâu hơn lý thuyết suông.

**Việc cần làm:**
1. Chọn domain nhỏ, độc lập nhất — đề xuất `payment` (đã tương đối tách biệt: `PaymentController`, `PaymentService`/`PaymentServiceImpl`, `PaymentEntity`, `VNPayConfig`, `VNPayUtil`).
2. Tạo cấu trúc mới `com.devfat.mini_ecommerce.payment.{controller, service, repository, dto, entity}`.
3. Move từng file một, sửa import, **build lại sau mỗi lần move** (không move hàng loạt).
4. Note lại phần nào của `payment` domain vẫn phải phụ thuộc ngược vào `common`/`config`/`exception` dùng chung toàn hệ thống — chính là điểm khó thực tế của package-by-feature "thuần túy".

**Tự kiểm tra hiểu:** Nếu `payment` và `order` domain đều cần chung `PaymentStatus`/`OrderStatus` enum, nên đặt ở đâu? (Gợi ý: không đặt trong 1 trong 2 feature package — cần package `shared`/`common` cho thứ dùng chéo domain, đây là ngoại lệ luôn tồn tại trong thực tế của package-by-feature.)

**Tiêu chí DONE:** Domain `payment` build/chạy độc lập với cấu trúc mới, domain khác không ảnh hưởng; giải thích rõ trade-off khi phỏng vấn hỏi sâu.

---
---

# 🟢 TIER 3 — TÍNH NĂNG NÂNG CAO (làm nếu còn dư sức, sau khi xong Tier 0-2)

## 3.1. Kafka — áp dụng trong monolith trước (không tách service vội)

**Cách tiếp cận đúng độ khó:** Không dựng Kafka cluster + tách Notification Service ngay. Thay `@Async` hiện tại (dùng cho gửi email OTP/đơn hàng qua `EmailService`/`MailTransport`) bằng Kafka Producer/Consumer **trong cùng 1 service**:

- Hiện tại: `OrderServiceImpl`/`OtpServiceImpl` gọi trực tiếp `@Async EmailService.send(...)`.
- Nâng cấp: Service publish message (VD `OrderCreatedEvent`, `OtpGeneratedEvent`) lên Kafka topic → 1 `@KafkaListener` riêng (vẫn cùng JVM lúc này) consume và gọi `EmailService` gửi.

**Giá trị:** Trải nghiệm thật Producer/Consumer, Topic, Partition, Consumer Group — kiến thức lõi Kafka — mà chưa phải đối mặt bài toán khó của microservices thật (distributed transaction, service discovery). Đúng tinh thần modular monolith.

**Việc cần làm:**
1. Thêm Kafka + Zookeeper/KRaft vào `docker-compose.yml` (bạn đã có kinh nghiệm viết compose, phần hạ tầng không đáng ngại).
2. Đọc concept: Topic, Partition, Offset, Consumer Group, at-least-once vs exactly-once delivery.
3. Viết Producer cho 2-3 event đơn giản trước (VD `OrderCreatedEvent`), Consumer tương ứng thay thế dần `@Async` cũ.
4. Giữ lại `@Async` cho case đơn giản không cần Kafka (VD email test nội bộ) để so sánh trực tiếp 2 cách tiếp cận khi trả lời phỏng vấn.

**Tự kiểm tra hiểu:** Kafka giải quyết vấn đề gì mà `@Async` (Spring's ThreadPoolTaskExecutor) không giải quyết được? (Gợi ý: `@Async` chỉ tách thread trong **cùng JVM** — nếu app crash giữa chừng, task async đang chạy dở **mất luôn**, không ai biết để retry. Kafka lưu message persistent trên disk (log-based), Consumer crash/restart vẫn đọc lại được từ offset cũ chưa xử lý — đảm bảo message không bị mất, đây là khác biệt cốt lõi.)

**Tiêu chí DONE:** Tạo Order → email gửi qua đường Kafka Producer/Consumer thành công; thử tắt Consumer app giữa chừng, khởi động lại → message chưa xử lý vẫn được consume tiếp (không mất).

---

## 3.2. SSO — Login with Google (OAuth2 Client, không phải Identity Provider)

**Làm rõ scope:** Không xây Authorization Server (Keycloak-style). Chỉ thêm "Login with Google" bằng Spring Security OAuth2 Client.

**Việc cần làm:**
1. Đọc khái niệm OAuth2 Authorization Code Flow trước khi code (đây là phần khó nhất, không phải code).
2. Thêm dependency `spring-boot-starter-oauth2-client`, đăng ký OAuth App trên Google Cloud Console lấy `client-id`/`client-secret`.
3. Config `application.yaml` phần `spring.security.oauth2.client.registration.google`.
4. Xử lý callback: nhận profile từ Google → tìm `UserEntity` theo email, nếu chưa tồn tại thì tạo mới (đặt `emailVerified = true` luôn vì Google đã xác thực email hộ, không cần qua OTP flow) → issue JWT của chính hệ thống bạn như luồng login thường (tái dùng `JwtProvider` đã có).

**Tự kiểm tra hiểu:** Vì sao user login qua Google không cần qua lại OTP flow như đăng ký thường? (Gợi ý: Google đã tự xác thực quyền sở hữu email đó rồi mới cho phép OAuth consent — tái xác thực bằng OTP là dư thừa, không tăng thêm bảo mật.)

**Tiêu chí DONE:** Bấm "Login with Google" → redirect Google → consent → quay lại app với JWT hợp lệ, `UserEntity` được tạo/map đúng.

---
---

# ⚪ TIER 4 — CHỈ CẦN HIỂU, KHÔNG CODE (chuẩn bị trả lời phỏng vấn)

## 4.1. Maven Multi-module (api/web/core/scheduler) *(Mentor #9)*

**Không code** — quyết định hợp lý đã có từ trước. Chỉ cần nắm:
- Cấu trúc: parent `pom.xml` với `<modules>`, mỗi module con `pom.xml` riêng, `core` chứa business logic dùng chung, `api`/`web`/`scheduler` phụ thuộc `core`.
- Điểm mạnh: deploy độc lập từng module. Điểm yếu: phức tạp hóa build, overkill cho quy mô hiện tại.
- Câu trả lời chuẩn: *"Đã cân nhắc, nhưng quy mô hiện tại (1 developer, chưa cần scale riêng từng phần) thì single-module + package-by-feature là đủ, multi-module là bước tiếp theo khi có nhu cầu deploy độc lập thật."*

## 4.2. Jenkins *(Mentor #1, phần công cụ)*

**Không cần cài lại CI bằng Jenkins** — quá tốn effort so với giá trị nhận lại trong giai đoạn này. Chỉ cần hiểu đủ để nói chuyện: `Jenkinsfile`, khái niệm `stage`/`agent`/`pipeline`, khác biệt so với GitHub Actions (Jenkins cần tự host server, GitHub Actions tích hợp sẵn). Câu trả lời chuẩn: *"Em chọn GitHub Actions cho project cá nhân vì tích hợp sẵn với GitHub, không cần tự vận hành server CI riêng — nhưng em hiểu rõ Jenkins Pipeline nếu công ty dùng."*

---
---

# 📋 Tổng hợp thứ tự thực hiện (đầu đến cuối, không chia tuần)

```
TIER 0 (bắt buộc, làm trước tất cả):
 0.1 Concurrency Control (audit + retry)
 0.2 Idempotency Payment IPN
 0.3 Rate Limiting Login/OTP
 0.4 Redis Token Device/IP Binding      ← 0.1-0.4 nên làm gần nhau, cùng đụng Order/Payment/Auth/Redis
 0.5 Testing 3 luồng chính              ← làm cuối Tier 0, test lại toàn bộ 0.1-0.4 vừa sửa
 0.6 Chuẩn hóa Exception Handler        ← rà soát nhanh, gộp lúc thêm exception mới ở 0.3

TIER 1 (nền tảng, sau khi Tier 0 ổn định):
 1.1 Cron → properties                 ← nhanh nhất, làm trước để tạo đà
 1.2 BaseEntity + JPA Auditing
 1.3 Correlation ID + MDC Logging
 1.4 Actuator Health Check
 1.5 DB Indexing Audit
 1.6 Tách Admin/User Controller         ← cần xong trước khi làm 2.2 (RBAC động)

TIER 2 (thiết kế trước khi code, sau khi Tier 1 xong):
 2.1 Audit mapping 1-N/N-1              ← chỉ đọc, xen kẽ lúc nghỉ
 2.2 Dynamic RBAC (scope tối giản)      ← phức tạp nhất, cần 1.6 xong trước
 2.3 3-Environment Config
 2.4 Pilot Package-by-Feature (payment)

TIER 3 (nếu còn dư thời gian):
 3.1 Kafka (trong monolith)
 3.2 SSO Login with Google

TIER 4 (chỉ đọc, không chiếm slot code):
 4.1 Maven Multi-module — hiểu khái niệm
 4.2 Jenkins — hiểu khái niệm
```

---

# ⚠️ Nguyên tắc an toàn xuyên suốt toàn bộ plan

1. **Mỗi Tier/mục lớn = 1 nhánh Git riêng**, không gộp chung 1 branch khổng lồ — dễ rollback nếu có sự cố.
2. **Không sửa migration Flyway cũ** — Tier 0 dùng `V4`, Tier 1 dùng `V5`/`V6` tiếp theo, luôn tạo migration mới (bài học từ lỗi checksum mismatch trước đây).
3. **Tier 0 xong và có test bảo vệ (0.5) rồi mới sang Tier 1** — vì Tier 0 sửa đúng vào luồng nghiệp vụ lõi (Order/Payment/Auth), cần chắc chắn không vỡ gì trước khi mở rộng sang việc khác.
4. **Tier 2 là ranh giới rủi ro** — nếu gần deadline phỏng vấn mà chưa xong, có thể dừng lại ở phần "hiểu sâu + thiết kế trên giấy" thay vì ép code, tương tự cách xử lý Tier 4.
5. **Không tự ý bỏ qua phần "Tự kiểm tra hiểu"** — đây chính là nội dung để trả lời phỏng vấn, không phải thủ tục hình thức.
