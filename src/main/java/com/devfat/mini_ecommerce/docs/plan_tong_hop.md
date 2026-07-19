# 🎯 PLAN TỔNG HỢP: Lộ trình hoàn chỉnh Mini Ecommerce (v3 — hợp nhất)

> **Đây là bản hợp nhất** của `plan_uu_tien_hoc_tap_v2.md` (8 Giai đoạn Lớn 0→7) và `payment_upload_plan_v1.md` (Giai đoạn P + U, vốn được chèn giữa Giai đoạn 2 và 3 ở bản v2). Thay vì để 2 file rời nhau — dễ quên mất chỗ chèn, dễ lạc số thứ tự — plan này **đánh số lại liên tục 0→9**, sắp xếp sao cho **Output của giai đoạn trước luôn là Input của giai đoạn sau**, không có chỗ nào "đứng một mình" không liên kết.
>
> Có 1 điểm mâu thuẫn giữa 2 bản gốc đã được xử lý khi hợp nhất: bản Payment/Upload gốc giả định Docker (`docker-compose.yml`) đã có sẵn từ Giai đoạn Docker — nhưng bản gốc đó lại đặt trước Giai đoạn Docker. Plan hợp nhất này giữ nguyên vị trí Payment/Upload ngay sau Concurrency (đúng lý do kỹ thuật: Payment cần tư duy race condition), nhưng xử lý MinIO giống hệt cách Redis đã được xử lý trước đó — **chạy tạm bằng `docker run` đơn lẻ**, và chỉ **gộp chính thức vào `docker-compose.yml`** khi tới Giai đoạn Docker chính thức. Điều này được ghi rõ lại ở từng mục liên quan, không còn mâu thuẫn ngầm.
>
> **Nguyên tắc giữ nguyên xuyên suốt:** KHÔNG code mẫu đầy đủ để copy — chỉ pseudo-code/flow khi cần hình dung, còn lại là khái niệm cốt lõi + keyword tự tra + doc chính thức. Không đốt giai đoạn.
> **Không bao gồm ôn phỏng vấn** — làm plan riêng sau khi có sản phẩm thật để nói về.

---

## 🗺️ Bản đồ tổng thể — 10 Giai đoạn Lớn (0 → 9)

```
GIAI ĐOẠN 0 — Dọn nợ kỹ thuật                                    [~1-2 ngày]
GIAI ĐOẠN 1 — User Module (áp dụng lại kiến thức Auth)           [~2-3 ngày]
GIAI ĐOẠN 2 — Concurrency & Locking                              [~3-4 ngày]
GIAI ĐOẠN 3 — Payment Integration                                [theo mốc/checkpoint]
GIAI ĐOẠN 4 — File & Image Upload                                [theo mốc/checkpoint]
GIAI ĐOẠN 5 — Caching với Redis                                  [~3-4 ngày]
GIAI ĐOẠN 6 — Async Processing với @Async                        [~2-3 ngày]
GIAI ĐOẠN 7 — Docker + CI (gộp chính thức Postgres+Redis+MinIO)  [~3-4 ngày]
GIAI ĐOẠN 8 — Testing (Unit & Integration)                       [~6-8 ngày]
GIAI ĐOẠN 9 — Frontend React (hoàn chỉnh sản phẩm FE-BE)         [~2-3 tuần]
```

**Vì sao thứ tự này liền mạch, không đứt đoạn:**

| Chuyển tiếp | Vì sao Output trước = Input sau |
|---|---|
| 0 → 1 | Code đã sạch nợ kỹ thuật mới bắt đầu thêm module mới, tránh xây trên nền lỗi |
| 1 → 2 | User Module dùng lại `@AuthenticationPrincipal`; Concurrency cần Product/Order đã ổn định |
| 2 → 3 | Payment cần đúng tư duy race condition/`@Version` vừa học ở Concurrency (P7 dùng lại trực tiếp) |
| 3 → 4 | Cả hai đều là "mảnh ghép cho sản phẩm trông giống thật"; Upload không phụ thuộc kỹ thuật của Payment nhưng đi liền để có đủ 2 mảnh trước khi tối ưu (Cache) |
| 4 → 5 | Cache cần có dữ liệu thật để cache (Product giờ đã có `imageUrl` từ Upload, Order giờ đã có trạng thái từ Payment) — cache đúng thứ đã "chốt" chứ không cache thứ còn dở dang |
| 5 → 6 | Async cần hiểu Cache trước để không tạo ra cache stale khi xử lý bất đồng bộ (email xác nhận thanh toán) |
| 6 → 7 | Docker gộp chính thức mọi service (Postgres/Redis/MinIO) đã dùng "tạm" ở các giai đoạn trước — đây là lúc dọn dẹp, đóng gói |
| 7 → 8 | Testing cần Docker (Testcontainers) để test đúng Postgres/Redis/MinIO thật, không mock giả |
| 8 → 9 | Frontend cần Backend đã ổn định (đủ tính năng + đủ test) mới có API "chốt" để gọi vào |

**Điểm dừng an toàn nếu cần demo gấp (phỏng vấn sớm hơn dự kiến):** hoàn thành hết Giai đoạn 0 → 2, cộng **P1→P6** (trong Giai đoạn 3) + **U1→U4** (trong Giai đoạn 4) là đã có 1 luồng "đặt hàng → thanh toán sandbox → webhook cập nhật đúng → sản phẩm có ảnh thật" chạy trọn vẹn, đủ kể 1 câu chuyện hoàn chỉnh dù Cache/Async/Docker/Test/Frontend chưa xong.

---

# GIAI ĐOẠN 0 — Dọn nợ kỹ thuật

*Không phải "học" — đây là việc đã biết cách làm, chỉ cần ngồi sửa. Làm nhanh, không lên kế hoạch chi tiết như các giai đoạn học kiến thức mới. Chi tiết bug cụ thể lấy từ bản phân tích code thực tế (`PROJECT_OVERVIEW_V3.md`).*

| Task | Việc cần làm |
|------|-------------|
| 0.1 | `ProductServiceImpl.update()` thiếu set `price` — thêm `if (updateProductRequest.price() != null) product.setPrice(...)` |
| 0.2 | `OrderRepository.findByUserIdAndUserId()` đặt tên sai (dead code, xoá luôn); `findAllByUserId()` đang trả `Optional` → sửa thành `List`, rà lại toàn bộ nơi gọi |
| 0.3 | `application.yaml`: `ddl-auto: update` → `validate`; kiểm tra Entity vs Migration Flyway có khớp không, lệch thì tạo migration mới (`V3__...sql`) |
| 0.4 | Secret/config nhạy cảm (`JWT secret`, `DB password`, `mail password`) → chuyển sang biến môi trường (`${JWT_SECRET_KEY:...}`) + tạo `.env.example` + thêm `.env` vào `.gitignore` |
| 0.5 | `OrderServiceImpl.changeStatus()`: khi `newStatus == CANCELLED` → lặp `order.getItems()`, cộng lại `stock` cho từng Product (`@Transactional` đã có sẵn nên tự rollback nếu lỗi giữa chừng) |
| 0.6 | Chốt 1 format lỗi duy nhất: giữ `ApiResponse<T>` cho cả success + error, xoá `ErrorResponse.java` (dead code) và `ExpiredJwtException.java` (dead code, shadow thư viện) |
| 0.7 (nhỏ, tiện tay) | Thêm `@Override` cho `decreaseStock()`; xoá import `SecurityContextHolder` không dùng; bổ sung `description`, `isActive` vào `ProductResponseDto`; `getProductsWithSearch()` chỉ trả sản phẩm `isActive=true` |

**Output cần đạt trước khi qua Giai đoạn 1:** build chạy sạch, không còn method dead code, `decreaseStock`/`changeStatus` đã đúng nghiệp vụ, config nhạy cảm không còn hardcode trong file yaml.

---

# GIAI ĐOẠN 1 — User Module

*Áp dụng lại kiến thức Auth vừa học, không có khái niệm mới — chủ yếu luyện tay.*

## 1.1. GET/PATCH `/users/me` — Xem & sửa profile

**Input:** `@AuthenticationPrincipal` (đã dùng ở Order).
**Output cần đạt:** Lấy `userId` từ token, không cho phép sửa `email`/`role` qua API này (chỉ `fullName`, `phoneNumber`).

## 1.2. PATCH `/users/me/password` — Đổi mật khẩu (step-up verification)

**Input:** `PasswordEncoder`/BCrypt đã có sẵn từ Auth.
**Khái niệm cốt lõi:** *Step-up verification* — có access token hợp lệ không đồng nghĩa được phép làm mọi hành động nhạy cảm; đổi mật khẩu bắt buộc verify lại `oldPassword`.
**Output cần đạt:** `passwordEncoder.matches(oldPassword, user.getPassword())` phải `true` mới cho đổi. (Nâng cao, optional: revoke toàn bộ refresh token hiện có sau khi đổi mật khẩu.)

## 1.3. Admin quản lý User

**Output cần đạt:**
- `GET /users` (ADMIN, pagination)
- `PATCH /users/{id}/status` — toggle `isActive` (soft-ban)
- **Lưu ý quan trọng:** đảm bảo `UserDetails.isEnabled()` map đúng field `isActive` — nếu không, user bị khoá vẫn login được bình thường.

**Keyword:** `Spring Security get current authenticated user`, `change password re-authentication best practice`

---

# GIAI ĐOẠN 2 — Concurrency & Locking

*Vấn đề THẬT đang tồn tại trong code: `ProductServiceImpl.decreaseStock()` đọc stock → check → trừ → save, tuần tự không khoá. 2 request mua cùng lúc sản phẩm cuối kho → cả 2 đều pass check → oversell hoặc stock âm. Đây không phải bài tập giả định.*

## 2A. Race Condition — vì sao hệ thống hiện tại có lỗ hổng thật

**Input cần biết trước:** Transaction cơ bản (đã dùng `@Transactional` ở Order).

**Khái niệm cốt lõi:** `@Transactional` đảm bảo *một* transaction chạy trọn vẹn hoặc rollback toàn bộ — nhưng **không** tự động ngăn *hai* transaction chạy song song cùng đọc-ghi trên cùng 1 dòng dữ liệu. Atomicity (trong 1 transaction) khác concurrency control (giữa nhiều transaction).

**Flow lỗi thực tế:**
```
Thread A: đọc Product(stock=1) → check stock>=1 OK → (chưa kịp save)
Thread B: đọc Product(stock=1) → check stock>=1 OK → save(stock=0)
Thread A: save(stock=0)   ← ghi đè, nhưng thực ra đã bán 2 đơn cho 1 sản phẩm
```

**Output cần đạt:** Tự giải thích được bằng lời tại sao `@Transactional` không đủ.

**Keyword:** `race condition explained`, `database transaction isolation level basics`
**Doc chính thức:** `https://www.postgresql.org/docs/current/transaction-iso.html`

## 2B. Optimistic Locking — @Version

**Input cần biết trước:** 2A.

**Khái niệm cốt lõi:** Thêm cột `version`. Mỗi `UPDATE` thực tế là `UPDATE ... WHERE id=? AND version=?`, tăng `version` lên 1. Hai transaction cùng đọc `version=5`, cái đầu save thành công → `version=6`; cái sau save với điều kiện `version=5` → không match → `UPDATE` trả về 0 dòng → JPA ném `OptimisticLockException`.

**Output cần đạt:** Thêm `@Version` vào `ProductEntity`, quyết định xử lý: retry tự động hay trả lỗi rõ ràng cho client.

**Lưu ý:** Phù hợp khi xung đột **hiếm xảy ra** — đúng với bán hàng thông thường.

**Keyword:** `JPA @Version optimistic locking`, `OptimisticLockException handling Spring`
**Doc chính thức:** `https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html`

## 2C. Pessimistic Locking — SELECT FOR UPDATE

**Input cần biết trước:** 2B.

**Khái niệm cốt lõi:** Thay vì phát hiện xung đột *sau*, Pessimistic khoá dòng dữ liệu *ngay khi đọc* — transaction thứ hai gọi `SELECT ... FOR UPDATE` trên cùng dòng phải **chờ**.

| | Optimistic | Pessimistic |
|---|---|---|
| Phát hiện xung đột | Lúc save (sau) | Lúc đọc (trước) |
| Hiệu năng | Tốt nếu ít xung đột | Chậm hơn |
| Rủi ro | Cần code retry | Deadlock nếu khoá nhiều bảng khác thứ tự |

**Lưu ý:** Với quy mô fresher, **chỉ cần chọn 1 để implement thật** (khuyến nghị Optimistic) — Pessimistic chỉ cần hiểu để so sánh khi phỏng vấn.

**Keyword:** `pessimistic locking JPA SELECT FOR UPDATE`, `deadlock database transaction`

## 2D. Áp dụng vào decreaseStock() + xử lý exception

**Input cần biết trước:** 2B, `GlobalExceptionHandler` đã có.

**Output cần đạt:**
1. Thêm `@Version` vào `ProductEntity` + migration Flyway mới
2. Sửa `decreaseStock()`: bắt `OptimisticLockException`, map sang lỗi rõ ràng qua `GlobalExceptionHandler`
3. Quyết định: retry tự động (tối đa 3 lần) hay để client tự gọi lại — ghi rõ lý do

**Keyword:** `@RestControllerAdvice ObjectOptimisticLockingFailureException`, `retry pattern Spring`

## 2E. Kiểm chứng bằng kịch bản đa luồng

**Input cần biết trước:** 2D hoàn chỉnh.

**Output cần đạt:** Script mô phỏng nhiều thread gọi `decreaseStock()` đồng thời trên Product stock=1 — trước fix: cả 2 thành công (bug thật). Sau fix: chỉ 1 thành công.

**Lưu ý:** Đây chỉ là script kiểm chứng thủ công — bộ Unit Test chính thức viết lại kỹ hơn ở Giai đoạn 8 (8B).

**Keyword:** `Java ExecutorService simulate concurrent requests`, `CountDownLatch test concurrency`

---

# GIAI ĐOẠN 3 — Payment Integration

> Đặt ngay sau Concurrency vì P7 dùng lại trực tiếp `@Version` vừa học ở 2B — không phải khái niệm mới, mà là bài áp dụng. Chia theo **mốc (checkpoint)** thay vì "ngày", vì lịch phỏng vấn có thể đến bất ngờ; mỗi mốc là điểm dừng an toàn, dừng ở đó vẫn demo được.
>
> **Phase 1 (P1→P6)** = tối thiểu để show được ngay. **Phase 2 (P7→P9)** = đào sâu, làm khi còn thời gian.

## P1. Payment domain modeling — vì sao tách riêng khỏi Order

**Input cần biết trước:** Entity/Repository layer đã quen (từ Order).

**Khái niệm cốt lõi:** `Order` mô tả "khách muốn mua gì" (PENDING → CONFIRMED → SHIPPED → DONE). `Payment` mô tả "tiền đã chuyển hay chưa" (PENDING → SUCCESS/FAILED/EXPIRED), có thể **nhiều lần thử** cho cùng 1 Order. Nhét 2 khái niệm vào chung 1 bảng là lỗi thiết kế phổ biến — không biết lưu lịch sử các lần thử thất bại ở đâu.

**Thiết kế gợi ý (không phải code mẫu):**
```
PaymentEntity
├── id
├── order        (@ManyToOne → OrderEntity)
├── amount       (snapshot số tiền tại thời điểm tạo, không đọc lại Order.totalAmount sau này)
├── provider     (VNPAY, MOMO...)
├── providerTransactionId   (mã giao dịch phía cổng thanh toán — dùng đối soát)
├── status       (PENDING, SUCCESS, FAILED, EXPIRED)
├── createdAt / paidAt
```

**Output cần đạt:** Vẽ được quan hệ `Order (1) — (N) Payment`; Order chỉ chuyển trạng thái tiếp theo khi có ít nhất 1 Payment SUCCESS.

**Keyword:** `payment transaction domain model design`, `order vs payment entity separation ecommerce`

## P2. Chọn cổng thanh toán sandbox — VNPay vs Momo

**Input cần biết trước:** P1.

| | VNPay Sandbox | Momo Sandbox |
|---|---|---|
| Docs tiếng Việt | Có, khá đầy đủ | Có |
| Cơ chế | Redirect sang trang VNPay → callback | Redirect hoặc QR → callback |
| Chữ ký | HMAC-SHA512 | HMAC-SHA256 |
| Độ phổ biến trong JD BE VN | Rất phổ biến | Cũng phổ biến |

**Output cần đạt:** Đăng ký xong 1 trong 2 (không cần cả hai), lấy `merchantId`/`secretKey` sandbox, đọc xong tài liệu tích hợp chính thức.

**Keyword:** `VNPay sandbox integration document`, `Momo payment sandbox API`
**Doc chính thức:** `https://sandbox.vnpayment.vn/apis/docs/` hoặc `https://developers.momo.vn/`

## P3. Luồng khởi tạo thanh toán — Create Payment URL

**Input cần biết trước:** P2.

**Khái niệm cốt lõi:** BE tạo ra **1 URL đã ký chữ ký** (chứa amount/orderId/returnUrl), trả về cho Client. Client redirect người dùng sang URL đó — thanh toán diễn ra trên trang của cổng thanh toán, không phải trang của bạn.

**Flow:**
```
Client: POST /api/v1/payments/{orderId}/create
BE:
  1. Tìm Order → kiểm tra status = PENDING, chưa có Payment SUCCESS nào
  2. Tạo PaymentEntity mới, status = PENDING
  3. Build tham số theo đúng format cổng thanh toán
  4. Ký chữ ký (P5)
  5. Trả về { paymentUrl }
Client: redirect sang paymentUrl
```

**Lưu ý quan trọng:** Số tiền phải lấy từ **Order đã lưu trong DB**, tuyệt đối không nhận amount từ Client — đây là lỗ hổng IDOR/tampering kinh điển.

**Output cần đạt:** Gọi API, nhận URL, redirect thấy đúng trang sandbox, đúng số tiền.

**Keyword:** `VNPay create payment URL Java example`, `payment gateway redirect flow`

## P4. Return URL vs Webhook/IPN — 2 luồng callback khác nhau

**Input cần biết trước:** P3. Điểm **dễ nhầm nhất** khi mới tích hợp thanh toán.

| | Return URL | Webhook / IPN |
|---|---|---|
| Ai gọi | Trình duyệt của khách | Server cổng thanh toán gọi thẳng vào server bạn |
| Độ tin cậy | **Thấp** — khách có thể tắt trình duyệt giữa chừng | **Cao** — nguồn sự thật (source of truth) |
| Mục đích | Chỉ hiển thị UI cho khách | BE **thực sự** cập nhật status trong DB |

**Sai lầm phổ biến:** chỉ code Return URL rồi cập nhật Order ngay tại đó → khách tắt trình duyệt trước khi redirect về → Order vĩnh viễn không được cập nhật dù tiền đã trừ. Đây là lý do IPN/Webhook bắt buộc.

**Output cần đạt:** Vẽ được sơ đồ 2 luồng riêng biệt; chỉ webhook mới ghi status vào DB, Return URL chỉ query lại để hiển thị.

**Keyword:** `payment gateway IPN vs return URL difference`, `webhook reliability source of truth`

## P5. Xác minh chữ ký (Signature/HMAC) — chống giả mạo callback

**Input cần biết trước:** P4. Liên hệ lại Hashing đã học ở Auth (BCrypt) — cùng họ khái niệm, nhưng HMAC thay vì BCrypt.

**Khái niệm cốt lõi:** Endpoint webhook là public, không qua JWT. Cổng thanh toán ký (HMAC) toàn bộ tham số bằng `secretKey` chỉ 2 bên biết. BE tự tính lại chữ ký, so sánh — khớp mới xử lý.

**Flow:**
```
Webhook nhận: { orderId, amount, transactionId, ..., signature }
BE:
  1. Build lại chuỗi từ tham số (trừ signature) theo đúng thứ tự quy định
  2. HMAC(chuỗi, secretKey) → mySignature
  3. So sánh mySignature == signature nhận được?
     - Khớp → tiếp tục xử lý (P6)
     - Không khớp → 400/reject, log cảnh báo, KHÔNG cập nhật gì
```

**Output cần đạt:** Verify chữ ký sandbox thành công, và tự demo được trường hợp sửa 1 ký tự → bị từ chối đúng.

**Keyword:** `HMAC signature verification Java`, `VNPay IPN checksum verify`

## P6. Idempotency — webhook gọi lại nhiều lần không được xử lý 2 lần

**Input cần biết trước:** P5. Kết nối trực tiếp với Giai đoạn 2 (Concurrency).

**Khái niệm cốt lõi:** Cổng thanh toán không đảm bảo chỉ gọi webhook đúng 1 lần (mạng lag, timeout, retry). Nếu không kiểm tra trạng thái hiện tại trước khi xử lý, request thứ 2 xử lý lại từ đầu.

**Flow xử lý đúng:**
```
Webhook nhận request (đã verify chữ ký ở P5):
  1. Tìm PaymentEntity theo providerTransactionId
  2. payment.status đã SUCCESS chưa?
     - Đã SUCCESS → return 200 OK ngay, KHÔNG làm gì thêm
     - Chưa SUCCESS → cập nhật status = SUCCESS, cập nhật Order, ...
```

**Lưu ý quan trọng:** Nên đặt **UNIQUE constraint** ở DB trên `providerTransactionId` — "lưới an toàn" ở tầng DB, cùng tinh thần Optimistic Locking (2B) — dùng ràng buộc DB để bắt lỗi mà tầng application có thể bỏ sót.

**Output cần đạt:** Giả lập gọi webhook 2 lần cùng payload → Order chỉ được cập nhật đúng 1 lần.

**Keyword:** `webhook idempotency pattern`, `idempotent payment processing`, `database unique constraint idempotency`

> **✅ Checkpoint an toàn:** hoàn thành P1→P6 là đã đủ 1 luồng thanh toán demo được trọn vẹn. P7→P9 dưới đây là phần đào sâu.

## P7. Cập nhật Order + Payment an toàn dưới tải đồng thời

**Input cần biết trước:** P6, và Giai đoạn 2 (Optimistic/Pessimistic Locking) — đây là chỗ **áp dụng lại**, không phải khái niệm mới.

**Khái niệm cốt lõi:** Khi webhook set `Payment.status = SUCCESS`, thường kèm `Order.status = CONFIRMED` trong cùng 1 transaction. Nếu đúng lúc Admin cũng đang đổi status Order (huỷ đơn) thì sao? → Áp dụng lại `@Version` (2B) trên `OrderEntity` là đủ, không cần Pessimistic (xung đột giữa "webhook" và "admin thao tác" rất hiếm).

**Output cần đạt:** `Order.status = CONFIRMED` và `Payment.status = SUCCESS` nằm trong cùng 1 `@Transactional` — lỗi 1 trong 2 thì cả 2 rollback.

**Keyword:** (không cần tra mới — quay lại đọc kỹ 2B/2C)

## P8. Xử lý timeout / huỷ giao dịch

**Input cần biết trước:** P3-P7.

**Khái niệm cốt lõi:** Payment tạo quá X phút (VD: 15 phút) mà chưa có webhook SUCCESS → tự chuyển EXPIRED.

**Cách làm:** `@Scheduled` (Spring có sẵn) chạy định kỳ, quét Payment PENDING quá hạn → set EXPIRED, đồng thời set Order về CANCELLED (nối với task 0.5 "hoàn kho khi CANCELLED" đã làm ở Giai đoạn 0 — tái sử dụng logic, không viết lại).

**Output cần đạt:** Tạo payment, không thanh toán, chờ (hoặc set ngắn để test nhanh) → Payment chuyển EXPIRED, Order chuyển CANCELLED, kho được hoàn.

**Keyword:** `Spring @Scheduled cron job`, `payment expiration handling`

## P9. End-to-end test checklist Payment

- [ ] Tạo Order → gọi API tạo Payment → nhận `paymentUrl`, redirect đúng số tiền
- [ ] Thanh toán thành công trên sandbox → webhook gọi → Order chuyển CONFIRMED
- [ ] Sửa 1 tham số trong request giả lập webhook → bị từ chối (chữ ký sai)
- [ ] Gọi lại đúng webhook payload 2 lần → chỉ xử lý 1 lần (P6)
- [ ] Không thanh toán, để hết hạn → Payment EXPIRED, Order CANCELLED, kho hoàn
- [ ] Gọi API tạo Payment cho Order đã CONFIRMED → bị từ chối (không cho tạo trùng)

---

# GIAI ĐOẠN 4 — File & Image Upload

> Đặt liền sau Payment vì cả hai đều là "mảnh ghép cho sản phẩm trông giống thật", không phụ thuộc kỹ thuật lẫn nhau — làm liền mạch để trước khi qua Cache đã có đủ 2 mảnh dữ liệu thật (Order có trạng thái thanh toán thật, Product có ảnh thật) để cache đúng thứ đã "chốt".

## U1. Multipart upload cơ bản

**Input cần biết trước:** Controller layer đã quen.

**Khái niệm cốt lõi:** Upload file dùng `multipart/form-data` (khác `application/json`) — Spring nhận qua `MultipartFile`. Validate ngay tại tầng nhận: giới hạn kích thước (`spring.servlet.multipart.max-file-size`), giới hạn loại file bằng **content-type thật của file**, không tin đuôi file (đổi tên `virus.exe` thành `anh.jpg` không đổi nội dung bên trong).

**Output cần đạt:** API `POST /products/{id}/image` nhận file, validate size/type đúng, từ chối rõ ràng (400) nếu sai.

**Keyword:** `Spring Boot MultipartFile upload example`, `validate file content type not extension`

## U2. Local storage vs Object storage — vì sao production không lưu local

**Input cần biết trước:** U1.

**Khái niệm cốt lõi:** Lưu file vào ổ đĩa server có 2 vấn đề: (1) **Không scale được** — nhiều instance BE, ổ đĩa riêng, ảnh instance A không thấy được từ instance B; (2) **Mất dữ liệu khi container restart** — container là ephemeral. → Giải pháp: **Object Storage** (S3 hoặc tương đương), BE chỉ lưu **URL**, không lưu file thật.

**Output cần đạt:** Giải thích được câu hỏi phỏng vấn kinh điển "vì sao không lưu ảnh trực tiếp vào DB (BLOB)?" — BLOB làm phình DB, chậm backup/restore, không tận dụng CDN.

**Keyword:** `why not store images in database`, `object storage vs local file storage scalability`

## U3. MinIO (S3-compatible) — chạy tạm bằng `docker run` đơn lẻ

**Input cần biết trước:** U2. **Docker cơ bản** — chỉ cần đủ để chạy `docker run`, chưa cần hiểu sâu (giống cách Redis được xử lý sau này ở Giai đoạn 5B: dùng tạm trước, gộp chính thức vào `docker-compose.yml` khi tới Giai đoạn 7 — Docker chính thức).

**Khái niệm cốt lõi:** Không cần tài khoản AWS thật — **MinIO** là object storage mã nguồn mở, tương thích 100% API với S3, chạy được bằng 1 dòng `docker run`. Học API MinIO gần như học thẳng API S3 thật.

**Output cần đạt:** MinIO chạy trong 1 container tạm (`docker run minio/minio ...`), tạo được 1 bucket (VD: `product-images`) qua MinIO Console, hiểu khái niệm **presigned URL** (URL có chữ ký, tạm thời, cho phép Client upload/download trực tiếp không cần qua BE — chỉ cần biết khái niệm ở vòng này).

> **Ghi chú liên kết:** Container MinIO chạy ở đây là tạm thời, dữ liệu có thể mất khi container bị xoá — **không sao**, vì mục tiêu Giai đoạn 4 là học API và ráp luồng upload. Việc đảm bảo dữ liệu bền vững qua volume + gộp vào `docker-compose.yml` chung với Postgres/Redis sẽ làm chính thức ở **Giai đoạn 7 (Docker)**.

**Keyword:** `MinIO Docker Spring Boot integration`, `S3 presigned URL concept`
**Doc chính thức:** `https://min.io/docs/minio/linux/developers/java/minio-java.html`

## U4. Áp dụng upload ảnh Product

**Input cần biết trước:** U1-U3.

**Flow:**
```
Client: POST /products/{id}/image (multipart, ADMIN only)
BE:
  1. Validate file (U1)
  2. Upload lên MinIO bucket → nhận object key/URL
  3. Lưu URL vào field imageUrl của ProductEntity
  4. Trả về ProductResponseDto có imageUrl
```

**Output cần đạt:** `ProductResponseDto` có thêm `imageUrl`, ảnh upload xong hiển thị được. Đây là mảnh ghép cuối để sản phẩm "trông giống thật" — catalog có ảnh thay vì chỉ text.

**Keyword:** (không cần tra mới — ráp nối U1+U2+U3 vào Product entity đã có)

> **✅ Checkpoint an toàn:** hoàn thành U1→U4 là đủ có ảnh sản phẩm thật để demo cùng luồng Payment ở Giai đoạn 3.

## U5. (Nâng cao, optional) Resize/optimize ảnh trước khi lưu

**Input cần biết trước:** U4.

**Khái niệm cốt lõi:** Ảnh upload từ điện thoại có thể nặng vài MB, 4000x3000px — không cần độ phân giải đó cho danh sách sản phẩm. Resize xuống VD max 800px trước khi lưu.

**Output cần đạt:** Thử làm 1 lần bằng thư viện Java (VD: Thumbnailator) — không bắt buộc nếu gấp thời gian.

**Keyword:** `Java image resize before upload Thumbnailator`

## U6. End-to-end test checklist Upload

- [ ] Upload ảnh hợp lệ (jpg/png, dưới size limit) → thành công, `imageUrl` đúng
- [ ] Upload file quá size → bị từ chối (400)
- [ ] Upload file đổi đuôi giả (`.txt` → `.jpg`) → bị từ chối vì content-type thật không khớp
- [ ] (Sau khi tới Giai đoạn 7, MinIO đã có volume chính thức) Container Docker restart → ảnh vẫn còn

---

# GIAI ĐOẠN 5 — Caching với Redis

*Đến đây Product đã có `imageUrl` thật (Giai đoạn 4) và Order đã có trạng thái thanh toán thật (Giai đoạn 3) — cache đúng dữ liệu đã "chốt", không phải dữ liệu còn dở dang.*

## 5A. Cache là gì — Cache-aside pattern

**Input cần biết trước:** Không cần gì, điểm xuất phát Giai đoạn 5.

```
Đọc:  Client → check Cache → có (hit) → trả về ngay, KHÔNG chạm DB
                            → không có (miss) → query DB → ghi vào Cache → trả về
Ghi:  Client → update DB → XOÁ (evict) cache liên quan (không update cache trực tiếp,
      xoá đơn giản và an toàn hơn — lần đọc sau tự query DB và ghi cache mới)
```

**Output cần đạt:** Giải thích được vì sao `GET /products`, `GET /categories` (public, gọi liên tục, ít đổi, giờ đã có `imageUrl` ổn định) là ứng viên lý tưởng cho cache, còn `GET /orders/me` (riêng tư, hay đổi theo trạng thái Payment) thì không nên cache.

**Keyword:** `cache aside pattern explained`, `when to use caching`

## 5B. Redis cơ bản — key-value, TTL, chạy tạm bằng `docker run`

**Input cần biết trước:** 5A. Docker cơ bản đủ để chạy `docker run` (cùng cách tiếp cận "chạy tạm trước" như MinIO ở U3).

**Khái niệm cốt lõi:** Redis là in-memory key-value store — cực nhanh vì dữ liệu nằm trong RAM. **TTL** = thời gian sống của 1 key trước khi tự động bị xoá — bắt buộc phải có TTL, tránh serve data cũ mãi mãi nếu quên evict.

**Output cần đạt:** Chạy Redis local qua `docker run redis`, dùng `redis-cli` thử `SET`/`GET`/`TTL`/`EXPIRE` thủ công trước khi tích hợp Spring.

> **Ghi chú liên kết:** Cũng như MinIO ở U3, container Redis này chỉ là tạm — sẽ gộp chính thức vào `docker-compose.yml` cùng Postgres và MinIO ở **Giai đoạn 7**.

**Keyword:** `Redis TTL expire basics`, `redis-cli tutorial`
**Doc chính thức:** `https://redis.io/docs/latest/develop/`

## 5C. Spring Cache abstraction — @Cacheable/@CacheEvict/@CachePut

**Input cần biết trước:** 5A, 5B.

**Khái niệm cốt lõi:** Spring tách biệt **abstraction** (annotation) khỏi **provider cụ thể** (Redis, Caffeine...) — đổi provider sau này không cần sửa code nghiệp vụ.
- `@Cacheable("products")` — cache kết quả trả về
- `@CacheEvict("products")` — xoá cache khi method chạy xong
- `@CachePut` — luôn chạy method VÀ cập nhật cache

**Output cần đạt:** Cấu hình `spring-boot-starter-cache` + `spring-boot-starter-data-redis`, hiểu key generation mặc định và khi nào cần tự định nghĩa key qua SpEL.

**Keyword:** `Spring Cache abstraction annotations tutorial`, `Spring Data Redis cache configuration`
**Doc chính thức:** `https://docs.spring.io/spring-boot/reference/io/caching.html`

## 5D. Áp dụng vào Product/Category/Analytics

**Input cần biết trước:** 5C.

**Output cần đạt:**
1. `@Cacheable` cho `GET /products/{id}`, `GET /categories`
2. Cache kết quả Analytics (top-buy, revenue) với TTL ngắn (VD: 5 phút)
3. Đo thử: lần 1 (query DB, chậm) vs lần 2 (từ cache, nhanh hẳn) — log thời gian để tự thấy hiệu quả

**Keyword:** `Spring @Cacheable custom TTL Redis`

## 5E. Cache Invalidation khi có Concurrent Update (nối với Giai đoạn 2 & 3)

**Input cần biết trước:** 5D, Giai đoạn 2 (Concurrency), Giai đoạn 3 (Payment).

**Vấn đề:** Nếu `decreaseStock()` (Optimistic Locking, 2D) sửa `stock` thành công nhưng quên evict cache → client vẫn thấy stock cũ. Tương tự, nếu sau này có cache liên quan tới trạng thái Order/Payment mà quên evict khi webhook (P6) cập nhật, cũng bị stale data.

**Output cần đạt:** Thêm `@CacheEvict("products")` vào đúng những method làm thay đổi Product (`update`, `delete`, `increaseStock`, `decreaseStock`, và `update ảnh` ở U4) — đây là điểm giao thoa thực tế giữa Concurrency, Payment và Caching, không phải các chủ đề tách rời.

**Keyword:** `cache invalidation strategies`, `stale cache problem`

---

# GIAI ĐOẠN 6 — Async Processing với @Async

## 6A. Blocking vs Non-blocking — vì sao gửi mail không nên chặn response

**Input cần biết trước:** Không cần gì, điểm xuất phát Giai đoạn 6.

**Khái niệm cốt lõi:** Mặc định mọi dòng code trong 1 request chạy tuần tự trên cùng 1 thread — nếu gửi email (phụ thuộc mail server ngoài, có thể chậm/timeout) nằm giữa luồng, client phải chờ email gửi xong mới nhận response, dù nghiệp vụ chính đã xong từ lâu.

**Output cần đạt:** Vẽ sơ đồ so sánh luồng đồng bộ vs bất đồng bộ.

**Keyword:** `blocking vs non-blocking IO`, `synchronous vs asynchronous processing`

## 6B. @Async + @EnableAsync

**Input cần biết trước:** 6A. Hiểu proxy pattern cơ bản (giống cách `@Transactional`/`@Cacheable` hoạt động).

**Khái niệm cốt lõi:** Đánh dấu `@Async` lên 1 method → Spring chạy method đó trên thread khác (từ thread pool), method gọi return ngay không chờ.

**Output cần đạt:** Bật `@EnableAsync`, viết thử 1 method `@Async`, verify bằng log `Thread.currentThread().getName()`.

**Keyword:** `Spring @Async @EnableAsync tutorial`
**Doc chính thức:** `https://docs.spring.io/spring-framework/reference/integration/scheduling.html#scheduling-annotation-support-async`

## 6C. ThreadPoolTaskExecutor riêng cho async task

**Input cần biết trước:** 6B.

**Khái niệm cốt lõi:** Không cấu hình riêng → `@Async` dùng `SimpleAsyncTaskExecutor` mặc định — tạo thread mới không giới hạn, nguy hiểm ở production. Cần `ThreadPoolTaskExecutor` với core/max pool size và queue capacity rõ ràng.

**Output cần đạt:** Cấu hình 1 `Executor` Bean riêng (VD: `emailTaskExecutor`), gán vào `@Async("emailTaskExecutor")`, hiểu `corePoolSize`, `maxPoolSize`, `queueCapacity`.

**Keyword:** `ThreadPoolTaskExecutor configuration Spring Boot`

## 6D. Self-invocation problem — giới hạn của AOP Proxy

**Input cần biết trước:** 6B, 6C.

**Khái niệm cốt lõi:** `@Async` (và cả `@Transactional`, `@Cacheable`) hoạt động qua proxy. Nếu method A gọi method B `@Async` trong cùng 1 class (`this.methodB()`), lời gọi bỏ qua proxy → `@Async` không có tác dụng, chạy đồng bộ mà không báo lỗi (khó debug).

**Output cần đạt:** Tự giải thích lại hiện tượng này; biết cách tránh: tách method `@Async` sang 1 Service riêng, gọi qua Spring-managed bean, không gọi `this.xxx()`.

**Keyword:** `Spring AOP proxy self-invocation limitation`, `@Async not working same class`

## 6E. Áp dụng: gửi email xác nhận đơn hàng & xác nhận thanh toán bất đồng bộ

**Input cần biết trước:** 6A-6D, và luồng Payment ở Giai đoạn 3 (đặc biệt P6 — webhook idempotent).

**Output cần đạt:**
1. Tạo `EmailService` riêng (tách class, tránh 6D), method `sendOrderConfirmation()` đánh dấu `@Async`
2. Gọi từ `OrderServiceImpl` **sau khi** transaction tạo Order đã commit — không để lỗi gửi mail làm rollback Order
3. **Mở rộng thêm 1 method** `sendPaymentSuccessEmail()`, gọi từ đúng nhánh "chưa SUCCESS → cập nhật status" trong luồng webhook (P6) — nhờ P6 đã idempotent, email này chắc chắn chỉ gửi đúng 1 lần dù webhook bị gọi lại nhiều lần
4. Log rõ ràng nếu gửi mail thất bại (không throw exception ra ngoài ảnh hưởng luồng chính)

**Keyword:** `send email after transaction commit Spring`, `@TransactionalEventListener AFTER_COMMIT` (nâng cao, tùy chọn)

---

# GIAI ĐOẠN 7 — Docker + CI (gộp chính thức mọi service)

*Đây là lúc "dọn dẹp, đóng gói" — Redis (5B) và MinIO (U3) đã chạy tạm bằng `docker run` đơn lẻ ở các giai đoạn trước, giờ gộp chính thức vào 1 `docker-compose.yml` cùng Postgres.*

## 7A. Docker Image vs Container — khái niệm nền

**Input cần biết trước:** Đã có trải nghiệm thực tế `docker run` từ 5B và U3.

**Khái niệm cốt lõi:** **Image** = bản thiết kế đóng gói sẵn, bất biến. **Container** = 1 instance đang chạy từ Image, giống quan hệ class–object trong OOP.

**Output cần đạt:** Phân biệt `docker build` (tạo Image) vs `docker run` (tạo & chạy Container).

**Keyword:** `Docker image vs container difference`

## 7B. Dockerfile multi-stage cho Spring Boot

**Input cần biết trước:** 7A.

**Khái niệm cốt lõi:** Stage 1 (builder, Maven+JDK) build ra `.jar`; Stage 2 (runtime, chỉ JRE gọn nhẹ) copy `.jar` từ Stage 1. Image cuối không mang theo Maven/source/build cache.

**Output cần đạt:** `docker build` thành công, `docker run` khởi động app.

**Keyword:** `Docker multi-stage build Java Spring Boot`
**Doc chính thức:** `https://docs.docker.com/build/building/multi-stage/`

## 7C. docker-compose — gộp app + PostgreSQL + Redis + MinIO

**Input cần biết trước:** 7B, kinh nghiệm chạy Redis (5B) và MinIO (U3) đơn lẻ trước đó.

**Khái niệm cốt lõi:** Thay vì 3 container Redis/MinIO/Postgres chạy tạm rời rạc bằng `docker run` riêng lẻ như trước, `docker-compose.yml` định nghĩa **4 service** (`app`, `db` postgres:16, `redis`, `minio`) trong 1 network chung, gọi nhau qua tên service (không phải `localhost` nữa — đây là điểm khác biệt quan trọng cần sửa lại config so với lúc chạy tạm).

**Output cần đạt:** `docker-compose.yml` đầy đủ 4 service, volume cho Postgres và MinIO để không mất data khi restart (khắc phục đúng vấn đề đã nêu ở U3 lúc MinIO còn chạy tạm).

**Keyword:** `docker-compose Spring Boot PostgreSQL Redis MinIO example`

## 7D. Config qua biến môi trường trong container

**Input cần biết trước:** 7C, mục 0.4 (đã đổi config sang env var ở Giai đoạn 0).

**Output cần đạt:** `.env` (gitignored) chứa secret thật, `docker-compose.yml` đọc qua `${VAR_NAME}`. Người khác clone repo chỉ cần copy `.env.example` → `.env`, chạy `docker compose up` — không cần cài Java/Maven/Postgres/Redis/MinIO trên máy họ.

**Keyword:** `docker-compose env_file variable substitution`

## 7E. Healthcheck & .dockerignore

**Input cần biết trước:** 7C, 7D.

**Output cần đạt:** `.dockerignore` loại `target/`, `.git/`, `.idea/`. Dùng `PingController` có sẵn làm `healthcheck` trong compose — tránh race condition app start trước khi DB sẵn sàng.

**Keyword:** `Docker HEALTHCHECK instruction`

## 7F. CI cơ bản với GitHub Actions

**Input cần biết trước:** 7A-7E, và ít nhất vài test chạy được (`mvn test` cần có gì đó để chạy) — thực tế bước này nên hoàn thiện sau khi bắt đầu Giai đoạn 8 (Testing), có thể quay lại làm sau nếu 8 chưa xong.

**Khái niệm cốt lõi:** CI — mỗi lần push/PR, GitHub tự động chạy `mvn test`, báo kết quả pass/fail ngay trên PR.

**Output cần đạt:** File `.github/workflows/ci.yml` chạy được, badge "build passing" trên README.

**Keyword:** `GitHub Actions Maven Spring Boot CI`
**Doc chính thức:** `https://docs.github.com/en/actions/use-cases-and-examples/building-and-testing/building-and-testing-java-with-maven`

---

# GIAI ĐOẠN 8 — Testing (Unit & Integration)

*Đến đây bộ test giàu hơn hẳn 1 plan testing thông thường — có sẵn case sinh ra từ Giai đoạn 2-3-4-5-6 (concurrency, payment, upload, cache, async), không chỉ test CRUD đơn giản.*

## 8A. Testing Pyramid

```
        ▲  Chậm, ít test    E2E / Manual (đã làm thủ công ở 2E, P9, U6)
        │                   Integration Test (@SpringBootTest)
        │                   Unit Test (@Mock — nhanh, nhiều nhất)
        ▼  Nhanh, nhiều test
```
**Output cần đạt:** Hiểu vì sao ưu tiên Unit Test cho Service, Integration Test chỉ cho luồng quan trọng.

**Keyword:** `testing pyramid Martin Fowler`

## 8B. Unit Test Service layer — JUnit 5 + Mockito

**Input cần biết trước:** 8A.

**Output cần đạt — ưu tiên theo rủi ro, không test CRUD đơn giản không có logic:**
- `ProductServiceImpl`: `decreaseStock()` — case bình thường, hết hàng, **case `OptimisticLockException`** (khoá bug 2D không tái diễn)
- `OrderServiceImpl`: tạo order thành công/hết hàng/state machine hợp lệ, **hoàn kho khi CANCELLED** (0.5)
- `AuthServiceImpl`: login đúng/sai, refresh token hết hạn/revoked
- `PaymentServiceImpl`: **verify chữ ký sai bị từ chối (P5)**, **webhook gọi lại 2 lần chỉ xử lý 1 lần (P6)**, tạo payment với amount lấy đúng từ Order chứ không từ request (P3)
- `EmailService`: verify method được gọi (`Mockito.verify()`) mà không cần mail server thật — cả `sendOrderConfirmation` lẫn `sendPaymentSuccessEmail` (6E)

**Lưu ý:** Tên test theo convention `<hành_động>_<điều_kiện>_<kết_quả_mong_đợi>`.

**Keyword:** `Mockito @Mock @InjectMocks tutorial`, `AAA pattern unit test`

## 8C. Repository Test — @DataJpaTest + Testcontainers

**Input cần biết trước:** 8B, Giai đoạn 7 (Docker — Testcontainers chạy Postgres thật trong container chỉ cho lúc test).

**Khái niệm cốt lõi:** `@DataJpaTest` chỉ khởi động phần JPA. Vì project dùng `CHECK` constraint đặc thù Postgres, H2 in-memory không mô phỏng đủ chính xác → dùng Testcontainers.

**Output cần đạt:** Test custom query trong `ProductRepository`/`OrderRepository`, **test hành vi `@Version`** (2B) — 2 transaction cùng update, verify đúng 1 cái thành công, **và UNIQUE constraint trên `providerTransactionId`** (P6) — verify constraint thật sự chặn insert trùng ở tầng DB.

**Keyword:** `Testcontainers PostgreSQL Spring Boot integration`

## 8D. Integration Test Controller — @SpringBootTest + MockMvc

**Input cần biết trước:** 8B, 8C.

**Output cần đạt:**
- Register → Login → gọi API cần token → thành công
- Gọi API cần ADMIN bằng token USER → 403
- **Cache trả đúng data sau `@CacheEvict`** (5E) — verify không stale
- **Đo thời gian response tạo Order không bị chặn bởi gửi mail** (6E)
- **Luồng tạo Payment → gọi webhook giả lập → Order chuyển CONFIRMED** (nối P3-P6 với Order thật)
- **Upload ảnh → `imageUrl` xuất hiện đúng trong `GET /products/{id}`** (nối U4 với cache 5D — verify cache evict đúng sau khi ảnh cập nhật)

**Keyword:** `Spring Boot @SpringBootTest MockMvc example`

## 8E. Test riêng cho Security — 401/403/JWT filter

**Output cần đạt:** Token hết hạn → 401, chữ ký sai → 401, thiếu quyền → 403, thiếu header → 401.

**Keyword:** `test Spring Security JWT filter unit test`

## 8F. Coverage có chọn lọc — JaCoCo

**Output cần đạt:** Cấu hình JaCoCo, Service layer đạt ~70-80%, không test Entity/DTO thuần data.

**Keyword:** `JaCoCo Maven plugin setup`

---

# GIAI ĐOẠN 9 — Frontend React (hoàn chỉnh sản phẩm FE-BE)

*Đặt cuối vì cần Backend đã ổn định (đủ tính năng, đủ test) — tránh vừa code FE vừa phải sửa API liên tục. Đã có nền React Native + TypeScript, nên giai đoạn này chủ yếu chuyển kiến thức component/state/hooks quen thuộc sang môi trường Web (khác Native ở routing, DOM, không có navigation container).*

## 9A. Kiến trúc FE gọi BE — điểm khác biệt so với React Native

**Input cần biết trước:** CORS đã cấu hình sẵn ở BE.

**Khái niệm cốt lõi:** Web FE và BE là 2 origin khác nhau → cần CORS đúng (đã có). Khác React Native (không có "trình duyệt", không CORS, không cookie theo domain), Web FE cần quyết định: lưu access token ở đâu (memory/localStorage) và refresh token ở đâu (httpOnly cookie khuyến nghị hơn localStorage vì tránh XSS).

**Output cần đạt:** Quyết định rõ chiến lược lưu token, giải thích được vì sao (liên hệ Refresh Token đã học ở Auth).

**Keyword:** `httpOnly cookie vs localStorage JWT storage security`, `CORS explained`

## 9B. Setup React (Vite) + Routing

**Input cần biết trước:** 9A. Kinh nghiệm React Native/TS đã có sẵn — JSX/hooks/component không cần học lại.

**Khái niệm cốt lõi mới (khác RN):** React Router (điều hướng qua URL thay vì navigation stack), SPA (Single Page Application).

**Output cần đạt:** Project React (Vite + TS) chạy được, có route cơ bản: `/login`, `/register`, `/products`, `/products/:id`, `/cart`, `/checkout` (mới — dẫn tới trang thanh toán, nối Giai đoạn 3), `/orders`, `/admin/*`.

**Keyword:** `React Router v6 tutorial`, `Vite React TypeScript setup`

## 9C. Auth Flow ở FE — lưu token, tự động refresh khi 401

**Input cần biết trước:** 9A, 9B, luồng Refresh Token đã học ở BE.

**Khái niệm cốt lõi:** Axios interceptor — bắt lỗi 401, tự động gọi `/auth/refresh`, thành công thì retry lại request gốc với access token mới, transparent với người dùng.

**Output cần đạt:** Login → lưu token đúng chiến lược ở 9A → gọi API cần auth → tự refresh khi hết hạn mà người dùng không nhận ra gián đoạn.

**Keyword:** `Axios interceptor refresh token pattern`

## 9D. Xây trang chính — Catalog (có ảnh thật), Cart, Order

**Input cần biết trước:** 9C, Giai đoạn 4 (Upload — Product giờ có `imageUrl` thật), Giai đoạn 5 (Cache — `GET /products` đã nhanh hơn nhờ cache).

**Output cần đạt:** Danh sách sản phẩm hiển thị **ảnh thật** (gọi `GET /products`, có cache ở BE — có thể đo thử thấy nhanh hơn), giỏ hàng (state ở FE), tạo Order, xem lịch sử Order của mình.

**Keyword:** (không cần, phần bạn đã quen thuộc từ React Native)

## 9E. Trang Checkout — tích hợp luồng Payment

**Input cần biết trước:** 9D, toàn bộ luồng Payment ở Giai đoạn 3 (P3, P4).

**Khái niệm cốt lõi:** FE gọi `POST /payments/{orderId}/create` (P3), nhận `paymentUrl`, redirect người dùng sang trang sandbox cổng thanh toán. Sau khi thanh toán, sandbox redirect người dùng về **Return URL** của FE (P4) — trang này chỉ hiển thị "đang kiểm tra trạng thái", **không tự set trạng thái thành công**, mà gọi lại `GET /orders/{id}` để lấy status thật (vì status thật chỉ được ghi bởi webhook, không phải Return URL — đúng nguyên tắc đã học ở P4).

**Output cần đạt:** Luồng "bấm Thanh toán → sang trang sandbox → quay lại FE → thấy đúng trạng thái Order thật (CONFIRMED/PENDING tuỳ webhook đã xử lý xong chưa)" chạy trọn vẹn.

**Keyword:** `payment gateway frontend return url handling`

## 9F. Trang Admin — quản lý Product/Category/Order/User

**Output cần đạt:** CRUD Product/Category (kèm upload ảnh, tái sử dụng U4), cập nhật trạng thái Order, quản lý User (khoá/mở) — tái sử dụng toàn bộ API ADMIN đã xây ở BE.

## 9G. Đồng bộ UI theo Role — ẩn/hiện chức năng theo ADMIN/USER

**Input cần biết trước:** 9C (đã có role trong token/state).

**Khái niệm cốt lõi:** Phân quyền ở FE **chỉ là UX** (ẩn nút cho gọn giao diện) — **không phải bảo mật thật**, vì FE chạy trên máy người dùng, có thể bị bypass. Bảo mật thật luôn nằm ở BE (`@PreAuthorize`).

**Output cần đạt:** Tự giải thích được khi bị hỏi phỏng vấn "phân quyền ở FE có đủ an toàn không?" — câu trả lời chuẩn: "không, FE chỉ ẩn UI, BE mới là nơi chặn thật".

**Keyword:** `frontend authorization is not security`

---

## 📌 Ghi chú — Ôn phỏng vấn

Phần ôn phỏng vấn (giải thích luồng Auth/Payment/Concurrency, câu hỏi Spring Security/JPA/Transaction, checklist demo trực tiếp) **không nằm trong plan này** — sẽ làm thành 1 plan riêng, chuyên biệt, sau khi Giai đoạn 0→9 ở trên đã có sản phẩm thật để nói về (không ôn lý thuyết suông khi chưa có gì để dẫn chứng).

---

*Hết plan hợp nhất. Đi tuần tự từng Giai đoạn 0 → 9, trong mỗi Giai đoạn Lớn đi tuần tự từng mục nhỏ. Nếu vướng ở bước nào, quay lại hỏi đúng mã (VD: "tôi đang vướng ở 2B", "tôi đang vướng ở P5", "tôi đang vướng ở U3", "tôi đang vướng ở 9E") để được hỗ trợ đúng trọng tâm.*
