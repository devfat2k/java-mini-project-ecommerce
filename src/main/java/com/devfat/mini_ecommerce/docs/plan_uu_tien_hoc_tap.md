# 🎯 PLAN CHUYÊN SÂU: Lộ trình tiếp theo cho Mini Ecommerce (v2)

> Đây là bản kế thừa `plan_uu_tien_hoc_tap_v1.md`, viết lại theo đúng format của `auth_security_jwt_plan.md` — Giai đoạn Lớn chứa nhiều Giai đoạn Nhỏ, mỗi Giai đoạn Nhỏ có Input → Khái niệm cốt lõi → Output → Keyword tra cứu.
> Nguyên tắc giữ nguyên xuyên suốt các plan trước: KHÔNG code mẫu đầy đủ để copy, chỉ pseudo-code/flow khi cần hình dung. Không đốt giai đoạn — Output của giai đoạn trước là Input của giai đoạn sau.
> **Không bao gồm** ôn phỏng vấn — sẽ là 1 plan riêng, làm sau khi có sản phẩm thật để nói về.
> **Giả định thời gian:** ~4-7 tiếng/ngày dành cho Java. Ước lượng dưới đây tính theo mốc đó.

---

## 🗺️ Bản đồ tổng thể — 8 giai đoạn lớn

```
GIAI ĐOẠN LỚN 0: Dọn nợ kỹ thuật (không tính "học", làm nhanh)     [~2 ngày]
GIAI ĐOẠN LỚN 1: User Module — áp dụng lại kiến thức Auth          [~2-3 ngày]
GIAI ĐOẠN LỚN 2: Concurrency & Locking                              [~3-4 ngày]
  2A. Race Condition — vì sao hệ thống hiện tại có lỗ hổng thật
  2B. Optimistic Locking — @Version
  2C. Pessimistic Locking — SELECT FOR UPDATE
  2D. Áp dụng vào decreaseStock() + xử lý exception
  2E. Kiểm chứng bằng kịch bản đa luồng

GIAI ĐOẠN LỚN 3: Caching với Redis                                  [~3-4 ngày]
  3A. Cache là gì — Cache-aside pattern
  3B. Redis cơ bản — key-value, TTL, chạy bằng Docker
  3C. Spring Cache abstraction — @Cacheable/@CacheEvict/@CachePut
  3D. Áp dụng vào Product/Category/Analytics
  3E. Cache Invalidation khi có Concurrent Update (nối với Giai đoạn 2)

GIAI ĐOẠN LỚN 4: Async Processing với @Async                        [~2-3 ngày]
  4A. Blocking vs Non-blocking — vì sao gửi mail không nên chặn response
  4B. @Async + @EnableAsync
  4C. ThreadPoolTaskExecutor riêng cho async task
  4D. Self-invocation problem — giới hạn của AOP Proxy
  4E. Áp dụng: gửi email xác nhận đơn hàng bất đồng bộ

GIAI ĐOẠN LỚN 5: Docker + CI cơ bản                                  [~3-4 ngày]
  5A. Docker Image vs Container — khái niệm nền
  5B. Dockerfile multi-stage cho Spring Boot
  5C. docker-compose — app + PostgreSQL + Redis
  5D. Config qua biến môi trường trong container
  5E. Healthcheck & .dockerignore
  5F. CI cơ bản với GitHub Actions

GIAI ĐOẠN LỚN 6: Testing — Unit & Integration                       [~6-8 ngày]
  6A. Testing Pyramid
  6B. Unit Test Service layer — JUnit 5 + Mockito
  6C. Repository Test — @DataJpaTest + Testcontainers
  6D. Integration Test Controller — @SpringBootTest + MockMvc
  6E. Test riêng cho Security — 401/403/JWT filter
  6F. Coverage có chọn lọc — JaCoCo

GIAI ĐOẠN LỚN 7: Frontend React — hoàn chỉnh sản phẩm FE-BE          [~2-3 tuần]
  7A. Kiến trúc FE gọi BE — CORS, kiến trúc client-server đã học ở Auth
  7B. Setup React (Vite) + Routing
  7C. Auth Flow ở FE — lưu token, tự động refresh khi 401
  7D. Xây trang chính — Catalog, Cart, Order
  7E. Trang Admin — quản lý Product/Category/Order/User
  7F. Đồng bộ UI theo Role — ẩn/hiện chức năng theo ADMIN/USER
```

**Vì sao thứ tự 2→3→4→5→6→7 như vậy:** mỗi giai đoạn lớn xây trên nền giai đoạn trước — Caching (3) cần hiểu Concurrency (2) để biết khi nào cache bị stale; Testing (6) cần Docker (5) để dùng Testcontainers đúng; Frontend (7) đặt cuối vì cần Backend đã ổn định (đủ test, đủ tính năng) mới có API "chốt" để FE gọi vào, tránh phải sửa API liên tục khi đang code FE.

---

# GIAI ĐOẠN LỚN 0 — Dọn nợ kỹ thuật

*Không phải "học" — đây là việc bạn đã biết cách làm, chỉ cần ngồi sửa. Làm nhanh trong ~2 ngày, không lên kế hoạch chi tiết như các giai đoạn học kiến thức mới.*

| Task | Việc cần làm |
|------|-------------|
| 0.1 | `ProductServiceImpl.update()` thiếu set `price` |
| 0.2 | `OrderRepository.findByUserIdAndUserId()` đặt tên sai, `findAllByUserId()` trả sai kiểu (`Optional` → `List`) |
| 0.3 | `ddl-auto: update` → `validate` |
| 0.4 | Secret/config nhạy cảm → biến môi trường + `.env.example` |
| 0.5 | Hoàn kho khi Order chuyển `CANCELLED` |
| 0.6 | Chốt 1 format lỗi duy nhất (`ApiResponse`), xoá `ErrorResponse` dead code |

Chi tiết kỹ thuật từng task đã có sẵn trong `plan_hoan_thien_project_F_K.md` (mục F1-F6) — cứ theo đó làm, không cần viết lại.

---

# GIAI ĐOẠN LỚN 1 — User Module

*Áp dụng lại kiến thức Auth vừa học, không có khái niệm mới — chủ yếu luyện tay.*

## 1.1. GET/PATCH `/users/me` — Xem & sửa profile

**Input:** `@AuthenticationPrincipal` (đã dùng ở Order, plan Auth mục D4).
**Output cần đạt:** Lấy `userId` từ token, không cho phép sửa `email`/`role` qua API này (chỉ `fullName`, `phoneNumber`).

## 1.2. PATCH `/users/me/password` — Đổi mật khẩu (step-up verification)

**Input:** `PasswordEncoder`/BCrypt (plan Auth mục B2).
**Khái niệm cốt lõi:** *Step-up verification* — có access token hợp lệ không đồng nghĩa được phép làm mọi hành động nhạy cảm; đổi mật khẩu bắt buộc verify lại `oldPassword`.
**Output cần đạt:** `passwordEncoder.matches(oldPassword, user.getPassword())` phải `true` mới cho đổi. (Nâng cao, optional: revoke toàn bộ refresh token hiện có sau khi đổi mật khẩu, buộc đăng nhập lại mọi thiết bị.)

## 1.3. Admin quản lý User

**Output cần đạt:**
- `GET /users` (ADMIN, pagination)
- `PATCH /users/{id}/status` — toggle `isActive` (soft-ban)
- **Lưu ý quan trọng:** đảm bảo `UserDetails.isEnabled()` map đúng field `isActive` — nếu không, user bị khoá vẫn login được bình thường.

**Keyword tra cứu chung Giai đoạn 1:** `Spring Security get current authenticated user`, `change password re-authentication best practice`

---

# GIAI ĐOẠN LỚN 2 — Concurrency & Locking

*Vấn đề THẬT đang tồn tại trong code: `ProductServiceImpl.decreaseStock()` đọc stock → check → trừ → save, tuần tự không khoá. 2 request mua cùng lúc sản phẩm cuối kho → cả 2 đều pass check → oversell hoặc stock âm. Đây không phải bài tập giả định.*

## 2A. Race Condition — vì sao hệ thống hiện tại có lỗ hổng thật

**Input cần biết trước:** Transaction cơ bản (đã dùng `@Transactional` ở Order — plan F-K mục F5).

**Khái niệm cốt lõi:** `@Transactional` đảm bảo *một* transaction chạy trọn vẹn hoặc rollback toàn bộ — nhưng **không** tự động ngăn *hai* transaction chạy song song cùng đọc-ghi trên cùng 1 dòng dữ liệu. Đây là 2 vấn đề khác nhau: atomicity (trong 1 transaction) vs concurrency control (giữa nhiều transaction).

**Flow lỗi thực tế:**
```
Thread A: đọc Product(stock=1) → check stock>=1 OK → (chưa kịp save)
Thread B: đọc Product(stock=1) → check stock>=1 OK → save(stock=0)
Thread A: save(stock=0)   ← ghi đè, nhưng thực ra đã bán 2 đơn cho 1 sản phẩm
```

**Output cần đạt:** Tự giải thích được bằng lời tại sao `@Transactional` không đủ để chống lỗi này — vì mỗi transaction tự nó đúng logic, chỉ sai khi *xen kẽ* với transaction khác.

**Keyword tra cứu:** `race condition explained`, `database transaction isolation level basics`
**Doc chính thức:** `https://www.postgresql.org/docs/current/transaction-iso.html`

---

## 2B. Optimistic Locking — @Version

**Input cần biết trước:** 2A.

**Khái niệm cốt lõi:** Thêm cột `version` (số nguyên) vào bảng. Mỗi lần `UPDATE`, câu lệnh thực tế là `UPDATE ... WHERE id=? AND version=?`, và tăng `version` lên 1. Nếu 2 transaction cùng đọc `version=5`, transaction đầu tiên save thành công → `version=6`; transaction thứ hai save sau đó với điều kiện `version=5` → **không match** → `UPDATE` trả về 0 dòng ảnh hưởng → JPA ném `OptimisticLockException`.

**Output cần đạt:** Thêm `@Version` vào `ProductEntity`, hiểu được khi nào exception này bị ném, và **quyết định xử lý**: retry tự động (đọc lại, thử lại) hay trả lỗi rõ ràng cho client ("sản phẩm vừa được cập nhật, vui lòng thử lại").

**Lưu ý quan trọng:** Optimistic Locking phù hợp khi xung đột **hiếm xảy ra** (đa số request không đụng nhau) — đúng với trường hợp bán hàng thông thường, không phù hợp nếu tranh chấp xảy ra liên tục.

**Keyword tra cứu:** `JPA @Version optimistic locking`, `OptimisticLockException handling Spring`
**Doc chính thức:** `https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html`

---

## 2C. Pessimistic Locking — SELECT FOR UPDATE

**Input cần biết trước:** 2B.

**Khái niệm cốt lõi:** Thay vì phát hiện xung đột *sau khi xảy ra* (Optimistic), Pessimistic Locking khoá dòng dữ liệu *ngay khi đọc* — transaction thứ hai gọi `SELECT ... FOR UPDATE` trên cùng dòng sẽ phải **chờ** (block) tới khi transaction đầu tiên commit/rollback.

**Output cần đạt:** Hiểu `@Lock(LockModeType.PESSIMISTIC_WRITE)` trong Spring Data JPA repository method, và phân biệt rõ với 2B:

| | Optimistic | Pessimistic |
|---|---|---|
| Khi nào phát hiện xung đột | Lúc save (sau) | Lúc đọc (trước) |
| Hiệu năng | Tốt nếu ít xung đột | Chậm hơn (transaction phải chờ nhau) |
| Rủi ro | Cần code xử lý retry | Deadlock nếu khoá nhiều bảng theo thứ tự khác nhau |

**Lưu ý quan trọng:** Với quy mô project fresher, **chỉ cần chọn 1 trong 2** để implement thật (khuyến nghị Optimistic vì đơn giản, ít rủi ro deadlock) — Pessimistic chỉ cần hiểu khái niệm để so sánh khi bị hỏi phỏng vấn "vì sao bạn không chọn cách kia?".

**Keyword tra cứu:** `pessimistic locking JPA SELECT FOR UPDATE`, `deadlock database transaction`

---

## 2D. Áp dụng vào decreaseStock() + xử lý exception

**Input cần biết trước:** 2B (hoặc 2C nếu chọn hướng đó), `GlobalExceptionHandler` đã có sẵn.

**Output cần đạt:**
1. Thêm `@Version` vào `ProductEntity` + migration Flyway mới (`V3__add_version_to_product.sql`)
2. Sửa `decreaseStock()`: bắt `OptimisticLockException`/`ObjectOptimisticLockingFailureException`, map sang response lỗi rõ ràng qua `GlobalExceptionHandler` (không để lộ stacktrace)
3. Quyết định: có retry tự động (VD: thử lại tối đa 3 lần) hay để client tự gọi lại — ghi rõ lý do chọn

**Keyword tra cứu:** `@RestControllerAdvice ObjectOptimisticLockingFailureException`, `retry pattern Spring`

---

## 2E. Kiểm chứng bằng kịch bản đa luồng

**Input cần biết trước:** 2D hoàn chỉnh.

**Output cần đạt:** Viết 1 test/script nhỏ mô phỏng 2 (hoặc nhiều) thread gọi `decreaseStock()` đồng thời trên cùng 1 Product có stock=1 — **trước khi fix**: chứng minh cả 2 đều thành công (bug thật). **Sau khi fix**: chỉ 1 thread thành công, thread còn lại nhận exception đúng như thiết kế.

**Lưu ý:** Đây chỉ là script kiểm chứng thủ công ở giai đoạn này — bộ Unit Test chính thức sẽ viết lại kỹ hơn ở Giai đoạn Lớn 6 (mục 6B).

**Keyword tra cứu:** `Java ExecutorService simulate concurrent requests`, `CountDownLatch test concurrency`

---

# GIAI ĐOẠN LỚN 3 — Caching với Redis

## 3A. Cache là gì — Cache-aside pattern

**Input cần biết trước:** Không cần gì, điểm xuất phát của Giai đoạn 3.

**Khái niệm cốt lõi:**
```
Đọc:  Client → check Cache → có (hit) → trả về ngay, KHÔNG chạm DB
                            → không có (miss) → query DB → ghi vào Cache → trả về
Ghi:  Client → update DB → XOÁ (evict) cache liên quan (không update cache trực tiếp,
      xoá đơn giản và an toàn hơn — lần đọc sau sẽ tự query DB và ghi cache mới)
```

**Output cần đạt:** Giải thích được vì sao `GET /products`, `GET /categories` (public, bị gọi liên tục, ít thay đổi) là ứng viên lý tưởng cho cache, còn `GET /orders/me` (riêng tư từng user, hay thay đổi) thì không nên cache.

**Keyword tra cứu:** `cache aside pattern explained`, `when to use caching`

---

## 3B. Redis cơ bản — key-value, TTL, chạy bằng Docker

**Input cần biết trước:** 3A. Docker cơ bản (nếu chưa biết `docker run`, tra nhanh trước — Giai đoạn Lớn 5 sẽ học sâu hơn, ở đây chỉ cần đủ để chạy Redis local).

**Khái niệm cốt lõi:** Redis là in-memory key-value store — cực nhanh vì dữ liệu nằm trong RAM. **TTL (Time-To-Live)** = thời gian sống của 1 key trước khi tự động bị xoá — bắt buộc phải có TTL cho cache (không cache vĩnh viễn), tránh serve data cũ mãi mãi nếu quên evict.

**Output cần đạt:** Chạy được Redis local qua `docker run redis`, dùng `redis-cli` thử `SET`/`GET`/`TTL`/`EXPIRE` thủ công để hiểu cơ chế trước khi tích hợp Spring.

**Keyword tra cứu:** `Redis TTL expire basics`, `redis-cli tutorial`
**Doc chính thức:** `https://redis.io/docs/latest/develop/`

---

## 3C. Spring Cache abstraction — @Cacheable/@CacheEvict/@CachePut

**Input cần biết trước:** 3A, 3B.

**Khái niệm cốt lõi:** Spring tách biệt **abstraction** (annotation `@Cacheable` v.v.) khỏi **provider cụ thể** (Redis, Caffeine, ...) — đổi provider sau này không cần sửa code nghiệp vụ, chỉ đổi config.
- `@Cacheable("products")` — cache kết quả trả về, key mặc định theo tham số method
- `@CacheEvict("products")` — xoá cache khi method chạy xong (dùng ở update/delete)
- `@CachePut` — luôn chạy method VÀ cập nhật cache (ít dùng hơn 2 cái trên)

**Output cần đạt:** Cấu hình `spring-boot-starter-cache` + `spring-boot-starter-data-redis`, hiểu key generation mặc định (dựa trên tham số method) và khi nào cần tự định nghĩa key qua SpEL.

**Keyword tra cứu:** `Spring Cache abstraction annotations tutorial`, `Spring Data Redis cache configuration`
**Doc chính thức:** `https://docs.spring.io/spring-boot/reference/io/caching.html`

---

## 3D. Áp dụng vào Product/Category/Analytics

**Input cần biết trước:** 3C.

**Output cần đạt:**
1. `@Cacheable` cho `GET /products/{id}`, `GET /categories`
2. Cache kết quả Analytics (top-buy, revenue) với TTL ngắn (VD: 5 phút) — data tính toán nặng, chấp nhận không real-time tuyệt đối
3. Đo thử: gọi API lần 1 (query DB, chậm hơn) vs lần 2 (từ cache, nhanh hơn hẳn) — log thời gian ra để tự thấy hiệu quả rõ ràng

**Keyword tra cứu:** `Spring @Cacheable custom TTL Redis`

---

## 3E. Cache Invalidation khi có Concurrent Update (nối với Giai đoạn 2)

**Input cần biết trước:** 3D, Giai đoạn Lớn 2 (Concurrency).

**Vấn đề:** Nếu `decreaseStock()` (đã học Optimistic Locking ở 2D) sửa `stock` thành công nhưng **quên evict cache** — client vẫn thấy `GET /products/{id}` trả stock cũ (stale data) dù DB đã đúng.

**Output cần đạt:** Thêm `@CacheEvict("products")` vào đúng những method làm thay đổi Product (`update`, `delete`, `increaseStock`, `decreaseStock`) — đây chính là điểm giao thoa thực tế giữa 2 mảng kiến thức Concurrency và Caching, không phải 2 chủ đề tách rời.

**Keyword tra cứu:** `cache invalidation strategies`, `stale cache problem`

---

# GIAI ĐOẠN LỚN 4 — Async Processing với @Async

## 4A. Blocking vs Non-blocking — vì sao gửi mail không nên chặn response

**Input cần biết trước:** Không cần gì, điểm xuất phát Giai đoạn 4.

**Khái niệm cốt lõi:** Mặc định, mọi dòng code trong 1 request chạy **tuần tự trên cùng 1 thread** — nếu bước gửi email (phụ thuộc mail server bên ngoài, có thể chậm/timeout) nằm giữa luồng tạo Order, client phải **chờ** email gửi xong mới nhận được response, dù việc tạo Order (nghiệp vụ chính) đã xong từ lâu.

**Output cần đạt:** Vẽ được sơ đồ so sánh: luồng đồng bộ (client chờ cả 2 việc) vs luồng bất đồng bộ (client nhận response ngay sau khi Order lưu xong, email gửi "ngầm" phía sau).

**Keyword tra cứu:** `blocking vs non-blocking IO`, `synchronous vs asynchronous processing`

---

## 4B. @Async + @EnableAsync

**Input cần biết trước:** 4A. Hiểu proxy pattern cơ bản (Spring dùng AOP proxy để "chèn" hành vi async vào method, giống cách `@Transactional` hoạt động).

**Khái niệm cốt lõi:** Đánh dấu `@Async` lên 1 method → Spring chạy method đó trên **thread khác** (từ thread pool), method gọi nó return ngay lập tức không chờ.

**Output cần đạt:** Bật `@EnableAsync` ở class config, viết thử 1 method `@Async` đơn giản, verify bằng cách log `Thread.currentThread().getName()` để thấy nó khác thread xử lý HTTP request.

**Keyword tra cứu:** `Spring @Async @EnableAsync tutorial`
**Doc chính thức:** `https://docs.spring.io/spring-framework/reference/integration/scheduling.html#scheduling-annotation-support-async`

---

## 4C. ThreadPoolTaskExecutor riêng cho async task

**Input cần biết trước:** 4B.

**Khái niệm cốt lõi:** Nếu không cấu hình riêng, `@Async` dùng `SimpleAsyncTaskExecutor` mặc định — **tạo thread mới không giới hạn** cho mỗi lời gọi, rất nguy hiểm ở production (có thể làm sập hệ thống khi traffic cao). Cần định nghĩa `ThreadPoolTaskExecutor` với core/max pool size và queue capacity rõ ràng.

**Output cần đạt:** Cấu hình 1 `Executor` Bean riêng (VD: `emailTaskExecutor`), gán vào `@Async("emailTaskExecutor")`, hiểu ý nghĩa từng tham số (`corePoolSize`, `maxPoolSize`, `queueCapacity`).

**Keyword tra cứu:** `ThreadPoolTaskExecutor configuration Spring Boot`

---

## 4D. Self-invocation problem — giới hạn của AOP Proxy

**Input cần biết trước:** 4B, 4C.

**Khái niệm cốt lõi:** `@Async` (và cả `@Transactional`, `@Cacheable`) hoạt động qua **proxy** — Spring tạo 1 object bọc ngoài class thật để chèn logic. Nếu method A gọi method B `@Async` **trong cùng 1 class** (`this.methodB()`), lời gọi đó đi thẳng vào object thật, **bỏ qua proxy** → `@Async` không có tác dụng, chạy đồng bộ như bình thường mà không báo lỗi gì (rất khó debug nếu không biết trước).

**Output cần đạt:** Tự giải thích lại được hiện tượng này bằng lời, và biết cách tránh: tách method `@Async` sang 1 Service/Bean **riêng**, gọi qua Spring-managed bean (dependency injection), không gọi `this.xxx()`.

**Keyword tra cứu:** `Spring AOP proxy self-invocation limitation`, `@Async not working same class`

---

## 4E. Áp dụng: gửi email xác nhận đơn hàng bất đồng bộ

**Input cần biết trước:** 4A-4D.

**Output cần đạt:**
1. Tạo `EmailService` riêng (tách class, tránh 4D), method `sendOrderConfirmation()` đánh dấu `@Async`
2. Gọi từ `OrderServiceImpl` **sau khi** transaction tạo Order đã commit thành công — không để lỗi gửi mail làm rollback việc tạo Order (nghiệp vụ chính phải độc lập với việc phụ)
3. Log rõ ràng nếu gửi mail thất bại (không throw exception ra ngoài làm ảnh hưởng luồng chính)

**Keyword tra cứu:** `send email after transaction commit Spring`, `@TransactionalEventListener AFTER_COMMIT` (khái niệm nâng cao, tùy chọn nếu muốn tách rời hoàn toàn khỏi Service)

---

# GIAI ĐOẠN LỚN 5 — Docker + CI cơ bản

## 5A. Docker Image vs Container — khái niệm nền

**Input cần biết trước:** Không cần gì (nếu đã chạy `docker run redis` ở 3B thì đã có trải nghiệm thực tế rồi).

**Khái niệm cốt lõi:** **Image** = bản thiết kế đóng gói sẵn (code + runtime + dependencies), bất biến. **Container** = 1 instance đang chạy từ Image đó, giống quan hệ class–object trong OOP mà bạn đã quen.

**Output cần đạt:** Phân biệt được `docker build` (tạo Image) vs `docker run` (tạo & chạy Container từ Image).

**Keyword tra cứu:** `Docker image vs container difference`

---

## 5B. Dockerfile multi-stage cho Spring Boot

**Input cần biết trước:** 5A.

**Khái niệm cốt lõi:** Tách 2 giai đoạn trong 1 Dockerfile — Stage 1 (builder, có Maven+JDK) build ra `.jar`; Stage 2 (runtime, chỉ JRE gọn nhẹ) copy `.jar` từ Stage 1 sang. Image cuối cùng không mang theo Maven/source code/build cache.

**Output cần đạt:** `docker build` chạy thành công, `docker run` khởi động app.

**Keyword tra cứu:** `Docker multi-stage build Java Spring Boot`
**Doc chính thức:** `https://docs.docker.com/build/building/multi-stage/`

---

## 5C. docker-compose — app + PostgreSQL + Redis

**Input cần biết trước:** 5B.

**Output cần đạt:** `docker-compose.yml` định nghĩa 3 service (`app`, `db` postgres:16, `redis`), network chung để gọi nhau qua tên service (không phải `localhost`), volume cho Postgres để không mất data khi restart.

**Keyword tra cứu:** `docker-compose Spring Boot PostgreSQL Redis example`

---

## 5D. Config qua biến môi trường trong container

**Input cần biết trước:** 5C, mục 0.4 (đã đổi config sang env var ở Giai đoạn 0).

**Output cần đạt:** `.env` (gitignored) chứa secret thật, `docker-compose.yml` đọc qua `${VAR_NAME}`. Người khác clone repo chỉ cần copy `.env.example` → `.env`, chạy `docker compose up` — không cần cài Java/Maven/Postgres/Redis trên máy họ.

**Keyword tra cứu:** `docker-compose env_file variable substitution`

---

## 5E. Healthcheck & .dockerignore

**Input cần biết trước:** 5C, 5D.

**Output cần đạt:** `.dockerignore` loại `target/`, `.git/`, `.idea/`. Dùng `PingController` có sẵn làm `healthcheck` trong compose — tránh race condition app start trước khi DB sẵn sàng.

**Keyword tra cứu:** `Docker HEALTHCHECK instruction`

---

## 5F. CI cơ bản với GitHub Actions

**Input cần biết trước:** 5A-5E, Giai đoạn Lớn 6 (Testing) — thực tế bước này nên hoàn thiện SAU khi có ít nhất vài test chạy được (mvn test cần có gì đó để chạy), có thể quay lại làm sau nếu 6 chưa xong.

**Khái niệm cốt lõi:** CI (Continuous Integration) — mỗi lần push/PR, GitHub tự động chạy `mvn test`, báo kết quả pass/fail ngay trên PR, không cần tự chạy tay.

**Output cần đạt:** File `.github/workflows/ci.yml` chạy được, badge "build passing" hiển thị trên README.

**Keyword tra cứu:** `GitHub Actions Maven Spring Boot CI`
**Doc chính thức:** `https://docs.github.com/en/actions/use-cases-and-examples/building-and-testing/building-and-testing-java-with-maven`

---

# GIAI ĐOẠN LỚN 6 — Testing (Unit & Integration)

*Đến đây bộ test sẽ giàu hơn hẳn 1 plan testing thông thường — vì có sẵn các case sinh ra từ Giai đoạn 2-4 (concurrency, cache, async), không chỉ test CRUD đơn giản.*

## 6A. Testing Pyramid

**Khái niệm cốt lõi:**
```
        ▲  Chậm, ít test    E2E / Manual (đã làm thủ công ở 2E)
        │                   Integration Test (@SpringBootTest)
        │                   Unit Test (@Mock — nhanh, nhiều nhất)
        ▼  Nhanh, nhiều test
```
**Output cần đạt:** Hiểu vì sao ưu tiên Unit Test cho Service (logic nghiệp vụ), Integration Test chỉ cho luồng quan trọng.

**Keyword tra cứu:** `testing pyramid Martin Fowler`

---

## 6B. Unit Test Service layer — JUnit 5 + Mockito

**Input cần biết trước:** 6A.

**Output cần đạt — ưu tiên theo rủi ro, không test CRUD đơn giản không có logic:**
- `ProductServiceImpl`: `decreaseStock()` — case bình thường, case hết hàng, **case `OptimisticLockException`** (chính là bug đã fix ở 2D — test này "khoá" bug không tái diễn)
- `OrderServiceImpl`: tạo order thành công/hết hàng/state machine hợp lệ & không hợp lệ, **hoàn kho khi CANCELLED** (0.5)
- `AuthServiceImpl`: login đúng/sai, refresh token hết hạn/revoked
- `EmailService`: verify method được gọi (dùng `Mockito.verify()`) mà không cần mail server thật

**Lưu ý:** Tên test theo convention `<hành_động>_<điều_kiện>_<kết_quả_mong_đợi>`.

**Keyword tra cứu:** `Mockito @Mock @InjectMocks tutorial`, `AAA pattern unit test`

---

## 6C. Repository Test — @DataJpaTest + Testcontainers

**Input cần biết trước:** 6B, Giai đoạn Lớn 5 (Docker — Testcontainers chạy Postgres thật trong container chỉ cho lúc test).

**Khái niệm cốt lõi:** `@DataJpaTest` chỉ khởi động phần JPA, không toàn bộ context. Vì project dùng `CHECK` constraint đặc thù Postgres, H2 in-memory mặc định không mô phỏng đủ chính xác → dùng Testcontainers.

**Output cần đạt:** Test các custom query trong `ProductRepository`/`OrderRepository` (đặc biệt Analytics), và **test hành vi `@Version`** (2B) — 2 transaction cùng update 1 row, verify đúng 1 cái thành công.

**Keyword tra cứu:** `Testcontainers PostgreSQL Spring Boot integration`

---

## 6D. Integration Test Controller — @SpringBootTest + MockMvc

**Input cần biết trước:** 6B, 6C.

**Output cần đạt:**
- Register → Login → gọi API cần token → thành công
- Gọi API cần ADMIN bằng token USER → 403
- **Cache trả đúng data sau `@CacheEvict`** (3E) — verify không có stale data
- **Đo thời gian response tạo Order không bị chặn bởi gửi mail** (4E)

**Keyword tra cứu:** `Spring Boot @SpringBootTest MockMvc example`

---

## 6E. Test riêng cho Security — 401/403/JWT filter

**Output cần đạt:** Token hết hạn → 401, chữ ký sai → 401, thiếu quyền → 403, thiếu header → 401.

**Keyword tra cứu:** `test Spring Security JWT filter unit test`

---

## 6F. Coverage có chọn lọc — JaCoCo

**Output cần đạt:** Cấu hình JaCoCo, Service layer đạt ~70-80%, không test Entity/DTO thuần data.

**Keyword tra cứu:** `JaCoCo Maven plugin setup`

---

# GIAI ĐOẠN LỚN 7 — Frontend React (hoàn chỉnh sản phẩm FE-BE)

*Đặt cuối cùng vì cần Backend đã ổn định (đủ tính năng, đủ test) — tránh vừa code FE vừa phải sửa API liên tục. Bạn đã có nền React Native + TypeScript, nên giai đoạn này chủ yếu là chuyển kiến thức component/state/hooks đã quen sang môi trường Web (khác Native ở routing, DOM, không có navigation container).*

## 7A. Kiến trúc FE gọi BE — điểm khác biệt so với React Native

**Input cần biết trước:** CORS đã cấu hình sẵn ở BE (Auth plan mục D1).

**Khái niệm cốt lõi:** Web FE và BE là 2 origin khác nhau (khác port khi dev) → cần CORS đúng (đã có). Khác với React Native (không có khái niệm "trình duyệt", không có CORS, không có cookie theo domain), Web FE cần quyết định: lưu access token ở đâu (memory/localStorage) và refresh token ở đâu (httpOnly cookie được khuyến nghị hơn localStorage vì tránh XSS).

**Output cần đạt:** Quyết định rõ chiến lược lưu token, giải thích được vì sao (liên hệ lại kiến thức Refresh Token đã học ở Auth plan mục E1).

**Keyword tra cứu:** `httpOnly cookie vs localStorage JWT storage security`, `CORS explained`

---

## 7B. Setup React (Vite) + Routing

**Input cần biết trước:** 7A. Kinh nghiệm React Native/TS đã có sẵn — phần JSX/hooks/component không cần học lại.

**Khái niệm cốt lõi mới (khác RN):** React Router (điều hướng qua URL thay vì navigation stack như React Navigation), khái niệm SPA (Single Page Application).

**Output cần đạt:** Project React (Vite + TS) chạy được, có các route cơ bản: `/login`, `/register`, `/products`, `/products/:id`, `/cart`, `/orders`, `/admin/*`.

**Keyword tra cứu:** `React Router v6 tutorial`, `Vite React TypeScript setup`

---

## 7C. Auth Flow ở FE — lưu token, tự động refresh khi 401

**Input cần biết trước:** 7A, 7B, toàn bộ luồng Refresh Token đã học ở BE (Auth plan Giai đoạn E).

**Khái niệm cốt lõi:** Dùng Axios interceptor — bắt response lỗi 401, tự động gọi `/auth/refresh`, nếu thành công thì **retry lại request gốc** với access token mới, transparent với người dùng (không văng ra trang login trừ khi refresh token cũng hết hạn).

**Output cần đạt:** Login → lưu token đúng chiến lược đã chọn ở 7A → gọi API cần auth → khi access token hết hạn, tự refresh mà người dùng không nhận ra gián đoạn.

**Keyword tra cứu:** `Axios interceptor refresh token pattern`

---

## 7D. Xây trang chính — Catalog, Cart, Order

**Output cần đạt:** Danh sách sản phẩm (gọi `GET /products`, đã có cache ở BE — có thể đo thử thấy nhanh hơn), giỏ hàng (state ở FE, chưa cần lưu DB), tạo Order, xem lịch sử Order của mình (đã có pagination nếu làm ở giai đoạn trước).

**Keyword tra cứu:** (không cần, đây là phần bạn đã quen thuộc từ React Native)

---

## 7E. Trang Admin — quản lý Product/Category/Order/User

**Output cần đạt:** CRUD Product/Category, cập nhật trạng thái Order, quản lý User (khoá/mở) — tái sử dụng toàn bộ API ADMIN đã xây ở BE.

---

## 7F. Đồng bộ UI theo Role — ẩn/hiện chức năng theo ADMIN/USER

**Input cần biết trước:** 7C (đã có role trong token/state).

**Khái niệm cốt lõi:** Phân quyền ở FE **chỉ là UX** (ẩn nút không có quyền cho gọn giao diện) — **không phải bảo mật thật**, vì FE chạy trên máy người dùng, có thể bị bypass. Bảo mật thật luôn nằm ở BE (`@PreAuthorize` đã làm).

**Output cần đạt:** Tự giải thích được điểm này khi bị hỏi phỏng vấn "phân quyền ở FE có đủ an toàn không?" — câu trả lời chuẩn là "không, FE chỉ ẩn UI, BE mới là nơi chặn thật".

**Keyword tra cứu:** `frontend authorization is not security`

---

## 📌 Ghi chú — Ôn phỏng vấn

Phần ôn phỏng vấn (giải thích luồng Auth, câu hỏi Spring Security/JPA/Transaction, checklist demo trực tiếp) **không nằm trong plan này** — sẽ làm thành 1 plan riêng, chuyên biệt, sau khi các Giai đoạn Lớn 0-7 ở trên đã có sản phẩm thật để nói về (không ôn lý thuyết suông khi chưa có gì để dẫn chứng).

---

*Hết plan v2. Đi tuần tự từng Giai đoạn Lớn 0 → 7, trong mỗi giai đoạn lớn đi tuần tự từng giai đoạn nhỏ. Nếu vướng ở bước nào, quay lại hỏi đúng mã giai đoạn (VD: "tôi đang vướng ở 2B" hoặc "tôi đang vướng ở 4D") để được hỗ trợ đúng trọng tâm.*
