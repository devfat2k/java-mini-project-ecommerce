# 🔐 PLAN CHUYÊN SÂU: Account Verification — Xác thực tài khoản sau Đăng ký (Email OTP)

> Đây là phần **nối tiếp** `auth_security_jwt_plan.md` (Giai đoạn A→E đã hoàn thành: Register, Login, JWT Filter, Role-based Authorization, Refresh Token). Plan này **không thay thế** gì đã có — chỉ **chèn thêm 1 bước** vào giữa Register và Login: user đăng ký xong phải xác thực (verify) tài khoản bằng mã OTP gửi qua email, mới được phép Login.
> Nguyên tắc giữ nguyên: không đốt giai đoạn, không code mẫu đầy đủ, ưu tiên hiểu bản chất để tự viết và tự bảo vệ được trước nhà tuyển dụng.
> Điểm khác biệt so với plan cũ: project của bạn **đã có sẵn** `EmailService` + `MailTransport` (Strategy Pattern, Async) — giai đoạn này **tái sử dụng** hạ tầng đó, không xây lại từ đầu.

---

## 🗺️ Bản đồ tổng thể — Giai đoạn Lớn F, 11 giai đoạn nhỏ

```
GIAI ĐOẠN LỚN F: Account Verification (OTP qua Email)
  F1. Vì sao cần xác thực tài khoản — bài toán & lựa chọn thiết kế
  F2. Thiết kế trạng thái tài khoản — tách "email_verified" khỏi "isActive" hiện có
  F3. Thiết kế bảng lưu OTP — Flyway migration mới
  F4. Sinh mã OTP an toàn — random + hash trước khi lưu (tái dùng nguyên lý A2/E6)
  F5. Gửi OTP sau Register — tái sử dụng EmailService/MailTransport có sẵn
  F6. API Verify OTP — so khớp, đánh dấu tài khoản đã verified
  F7. API Resend OTP — chống spam bằng rate limit / cooldown
  F8. Chặn Login khi chưa verified — sửa luồng Authentication hiện có
  F9. Exception Handling riêng cho Verification — đồng bộ ErrorResponse
  F10. Dọn dẹp OTP hết hạn — Scheduled Job (tái dùng kinh nghiệm VNPay expiry job)
  F11. End-to-end test — checklist xác nhận hoàn thành
```

**Vị trí trong roadmap tổng (`plan_tong_hop_v4.md`):** Đề xuất chèn Giai đoạn F này **trước Phase 8 (Testing)** — vì Testing nên viết test cho luồng Auth đã hoàn chỉnh 100% (bao gồm cả verification), không nên viết test rồi phải sửa lại. Có thể đặt tên nội bộ là **Phase 7.5** trong roadmap của bạn.

---

# F1. Vì sao cần xác thực tài khoản — bài toán & lựa chọn thiết kế

**Input cần biết trước:** Đã hoàn thành Giai đoạn B (Register) và C (Login) trong plan cũ.

**Khái niệm cốt lõi:**
Hiện tại, `POST /auth/register` tạo user xong là **tin luôn** email đó có thật và thuộc về người đang đăng ký. Vấn đề:
- Ai đó có thể đăng ký bằng email **không phải của họ** (email người khác, email rác, email không tồn tại) → spam DB, gửi nhầm thông báo cho người không liên quan.
- Không có cách nào chắc chắn user **thực sự sở hữu** email đã nhập, trong khi email lại chính là **username đăng nhập** (`UserDetailsService` load theo email) — nếu email sai/giả, sau này không thể liên lạc lại được (quên mật khẩu, thông báo đơn hàng...).

**3 lựa chọn thiết kế phổ biến (biết cả 3 để trả lời phỏng vấn, chỉ làm 1):**

| Cách | Cơ chế | Ưu điểm | Nhược điểm |
|---|---|---|---|
| **Email OTP (chọn làm)** | Gửi mã số ngắn (VD: 6 số) qua email, user nhập lại vào form | UX tốt trên mobile/SPA, không cần deep-link xử lý phức tạp | Cần thêm bước nhập tay |
| **Email Verification Link** | Gửi link chứa token dài, user bấm vào link → verify tự động | Không cần nhập gì, 1 click | Cần xử lý redirect, phức tạp hơn nếu chưa có FE domain thật |
| **SMS OTP** | Gửi mã qua số điện thoại | Nhanh, không phụ thuộc email có bị vào spam folder | Tốn phí SMS Gateway (Twilio, eSMS...), ngoài phạm vi project hiện tại |

**Vì sao chọn Email OTP:** Project đã có `EmailService`/`MailTransport` sẵn (không cần tích hợp SMS Gateway mới), và OTP dạng số phù hợp cho cả kịch bản sau này có FE mobile-first lẫn web — không phụ thuộc việc FE domain đã cấu hình xong deep-link/redirect hay chưa.

**Output cần đạt:** Giải thích được vì sao chọn OTP thay vì Link nếu bị hỏi trong phỏng vấn — câu trả lời chuẩn: "vì hạ tầng gửi email đã sẵn có, và OTP không phụ thuộc vào việc FE đã có domain/route xử lý redirect hay chưa, phù hợp giai đoạn BE-first của project."

**Keyword tra cứu:** `email verification OTP vs verification link UX`, `account verification flow best practice`

---

# F2. Thiết kế trạng thái tài khoản — tách `email_verified` khỏi `isActive` hiện có

**Input cần biết trước:** F1, đã biết cấu trúc `UserEntity` hiện tại (có sẵn cột `is_active`)

**Khái niệm cốt lõi — điểm dễ nhầm nhất của giai đoạn này:**
`isActive` hiện tại trong `UserEntity` đang mang nghĩa **"ADMIN chủ động khoá tài khoản"** (soft disable, dùng ở `PATCH /users/{userId}/status`). Đây là **2 khái niệm hoàn toàn khác nhau** về mặt nghiệp vụ dù nghe giống nhau:

| Field | Ai thay đổi? | Ý nghĩa | Trạng thái mặc định khi Register |
|---|---|---|---|
| `isActive` (đã có) | ADMIN | Tài khoản có bị khoá bởi quản trị viên không | `true` |
| `emailVerified` (cần thêm mới) | HỆ THỐNG (sau khi user nhập đúng OTP) | Email có được xác thực là thật không | `false` |

**Vì sao không tái dùng `isActive` cho việc này:** Nếu dùng chung 1 cột, bạn sẽ không phân biệt được 2 tình huống rất khác nhau khi debug: "tài khoản này `isActive=false` vì ADMIN khoá" hay "vì user chưa verify email" — về sau khi viết logic hiển thị lỗi cho user (VD: "Tài khoản bị khoá, liên hệ admin" khác hẳn "Vui lòng xác thực email trước khi đăng nhập") sẽ không tách được message đúng.

**Việc cần làm ở giai đoạn này (chỉ là thiết kế, code thật ở F3):**
- Thêm cột mới `email_verified` (boolean, default `false`) vào `UserEntity`/bảng `users`
- Giữ nguyên `is_active` như cũ, không đổi ý nghĩa

**Output cần đạt:** Vẽ lại được bảng trên bằng lời của mình, xác nhận hiểu rõ 2 field độc lập nhau — 1 user có thể `isActive=true` nhưng `emailVerified=false` (vừa đăng ký xong, chưa kịp verify) hoặc `isActive=false` nhưng `emailVerified=true` (verify rồi nhưng sau đó bị admin khoá).

**Keyword tra cứu:** `account status vs email verification separate fields design`

---

# F3. Thiết kế bảng lưu OTP — Flyway migration mới

**Input cần biết trước:** F2, đã quen viết Flyway migration (đã làm ở `V1__init_mini_shop.sql`, và ở E2 của plan cũ với `refresh_tokens`)

**Khái niệm cốt lõi:** Tương tự `refresh_tokens`, OTP cũng cần 1 bảng riêng vì phải **lưu, đối chiếu, và hết hạn được** — không thể nhúng vào JWT (vì lúc này user còn chưa có token nào, thậm chí còn chưa login lần nào).

**Thiết kế bảng `otp_verifications` (đặt tên gợi ý, có thể đổi tên nếu bạn thấy tên khác rõ hơn):**

| Cột | Kiểu | Mục đích |
|---|---|---|
| `id` | BIGSERIAL PK | |
| `user_id` (FK → `users.id`) | BIGINT | OTP này thuộc về ai |
| `otp_hash` | VARCHAR | **Hash** của mã OTP (không lưu raw — giống nguyên lý A2/E6 đã học) |
| `purpose` | VARCHAR/ENUM | Dự phòng mở rộng: `REGISTER_VERIFICATION` (dùng ngay), có thể thêm `RESET_PASSWORD` sau này dùng chung cơ chế |
| `expires_at` | TIMESTAMP | Thời điểm hết hạn (ngắn, VD 5–10 phút — khác hẳn refresh token sống dài ngày) |
| `attempts` | INT default 0 | Đếm số lần nhập sai, phục vụ khoá tạm sau N lần (F6) |
| `consumed` | BOOLEAN default false | Đánh dấu đã dùng để verify thành công chưa (chống dùng lại 1 OTP nhiều lần) |
| `created_at` | TIMESTAMP | Audit |

**Output cần đạt:** File migration mới (VD: `V3__add_otp_verifications_table.sql`) chạy được qua Flyway (nhớ: KHÔNG sửa lại `V1`/`V2` đã chạy production — đúng convention `ddl-auto: validate` + Flyway đã thống nhất từ đầu project), có FK tới `users`, có index trên `user_id` (query OTP mới nhất theo user) và `otp_hash` (đối chiếu lúc verify).

**Lưu ý:** Cột `purpose` là thiết kế "phòng thủ tương lai" — bạn đang có TODO "Reset Password" chưa làm trong roadmap; nếu chỉ làm Register Verification thì có thể bỏ cột này cho gọn (thêm sau khi cần cũng không tốn công), nhưng nếu biết chắc sẽ làm Reset Password sau, thêm ngay từ đầu sẽ đỡ phải migrate lại bảng lần 2.

**Keyword tra cứu:** `Flyway migration add table with enum column`, `OTP database schema design`

---

# F4. Sinh mã OTP an toàn — random + hash trước khi lưu

**Input cần biết trước:** F3, đã hiểu nguyên lý hash ở A2 (BCrypt cho password) và E6 (SHA-256 cho refresh token)

**Khái niệm cốt lõi:**
- OTP thường là chuỗi **6 chữ số** (VD: `482913`) — dùng `SecureRandom` để sinh, **không dùng** `Math.random()` (không đủ ngẫu nhiên về mặt bảo mật, dễ đoán được seed).
- Giống bài học ở E6: **không lưu OTP raw vào DB** — hash trước khi lưu (dùng SHA-256, không cần BCrypt vì OTP sống rất ngắn và không cần độ chậm chống brute-force ở mức password, tương tự lý do đã chọn SHA-256 cho refresh token).
- OTP raw (chưa hash) chỉ tồn tại đúng 1 lần: lúc sinh ra để gửi email — sau đó KHÔNG được log ra console/log file dưới bất kỳ hình thức nào (lỗi bảo mật hay gặp: developer log OTP ra để debug rồi quên xoá).

**Flow hoạt động:**
```
sinh otpRaw = SecureRandom → 6 chữ số (VD: "482913")
   → hash(otpRaw) bằng SHA-256 → otpHash
   → lưu vào bảng otp_verifications: user_id, otp_hash=otpHash, expires_at = now + 5 phút
   → otpRaw (chưa hash) được truyền tiếp sang bước gửi email (F5) — KHÔNG lưu raw vào DB, KHÔNG log ra console
```

**Output cần đạt:** Có 1 class/method (VD: `OtpGenerator` hoặc gộp vào `AuthService`) sinh được mã 6 số bằng `SecureRandom`, và method hash riêng biệt tái sử dụng được (có thể tái dùng chung 1 `HashUtil` với phần hash refresh token ở E6 nếu cả 2 đều dùng SHA-256 — tránh code trùng lặp).

**Lưu ý:** Nếu bạn đã có sẵn utility hash SHA-256 cho refresh token từ Giai đoạn E, **tái sử dụng lại**, đừng viết method hash mới trùng chức năng — đây là điểm cộng khi phỏng vấn hỏi về code reuse/DRY principle.

**Keyword tra cứu:** `Java SecureRandom generate numeric OTP`, `SHA-256 hashing Java MessageDigest`

---

# F5. Gửi OTP sau Register — tái sử dụng EmailService/MailTransport có sẵn

**Input cần biết trước:** F4, đã có sẵn `EmailService`/`EmailServiceImpl` (Async, gửi qua `MailTransport`) và template `SendEmailTemplate.html`

**Khái niệm cốt lõi:** Đây là điểm khác biệt lớn nhất so với việc xây từ đầu — bạn **không cần viết lại hạ tầng gửi mail**, chỉ cần **gọi thêm** `EmailService.sendHtmlEmail(...)` đã có, ngay sau bước tạo `UserEntity` thành công trong `AuthService.register()`.

**Flow hoạt động (mở rộng luồng Register B4 đã có trong plan cũ):**
```
POST /api/v1/auth/register (như B4 cũ, không đổi bước đầu)
   → Validate DTO, check trùng email/phone, hash password, lưu UserEntity
     (THÊM: emailVerified = false mặc định — theo F2)
   → SINH THÊM (F4): otpRaw, otpHash → lưu vào otp_verifications
   → Gọi EmailService.sendHtmlEmail() — @Async, KHÔNG block response
     (tái dùng nguyên luồng Async + ThreadPool đã có ở EmailServiceImpl,
     có thể tái dùng luôn template HTML cũ hoặc tạo thêm 1 template mới
     riêng cho OTP nếu muốn giao diện email đẹp hơn — không bắt buộc)
   → Trả response cho Client: đăng ký thành công, "vui lòng kiểm tra email để lấy mã xác thực"
     (KHÔNG trả accessToken ở bước này nữa — vì tài khoản CHƯA verify,
     đây là thay đổi so với 1 số flow cũ trả token luôn sau register)
```

**Output cần đạt:** Register xong, nhận được email thật trong hộp thư (dev: qua Gmail SMTP đã cấu hình sẵn) chứa mã OTP 6 số, kiểm tra DB thấy `otp_verifications` có dòng mới với `otp_hash` (không đọc được raw), `users.email_verified = false`.

**Lưu ý quan trọng:** Vì `sendHtmlEmail` đã là `@Async`, nếu gửi mail thất bại (SMTP lỗi, Brevo API lỗi) thì **không được làm rollback việc tạo user** — user vẫn được tạo thành công trong DB, chỉ là chưa nhận được mail. Cần có cơ chế Resend (F7) để xử lý tình huống này, không thiết kế theo kiểu "gửi mail thất bại thì huỷ luôn cả transaction tạo user" (vì gửi mail là async, đã tách khỏi transaction chính từ Giai đoạn 6 cũ rồi).

**Keyword tra cứu:** `Spring Boot async email after transaction commit`, `@TransactionalEventListener AFTER_COMMIT` (nâng cao, tìm hiểu nếu muốn đảm bảo chỉ gửi mail SAU KHI transaction tạo user chắc chắn commit thành công, tránh trường hợp gửi mail cho user mà transaction sau đó lại rollback)

---

# F6. API Verify OTP — so khớp, đánh dấu tài khoản đã verified

**Input cần biết trước:** F5

**Khái niệm cốt lõi:** Endpoint mới, public (không cần JWT — vì user lúc này chưa có token nào): `POST /api/v1/auth/verify-otp`

**Flow hoạt động:**
```
Client gửi { email, otpCode } (KHÔNG cần accessToken vì đang public endpoint)
   → Tìm user theo email
   → Tìm OTP record mới nhất, chưa consumed, theo user_id
   → Kiểm tra tuần tự (dừng ngay khi fail 1 điều kiện, trả lỗi tương ứng):
     1. Có tồn tại OTP record nào không? (user gõ email sai / chưa từng register)
     2. expires_at còn hạn không? (OTP hết hạn)
     3. attempts đã vượt giới hạn chưa? (VD: quá 5 lần nhập sai → khoá tạm, bắt phải Resend mới)
     4. hash(otpCode nhập vào) có khớp otp_hash trong DB không?
   → Nếu khớp:
     - UPDATE users SET email_verified = true
     - UPDATE otp_verifications SET consumed = true (không xoá — giữ để audit,
       giống lý do đã chọn "đánh dấu revoked" thay vì xoá cứng ở E5)
     - Trả về thành công — CÓ THỂ trả luôn accessToken + refreshToken tại đây
       (để user không cần login lại lần nữa sau khi verify — tối ưu UX,
       tái dùng lại JwtProvider.generateToken() đã có từ C3)
   → Nếu KHÔNG khớp: tăng attempts += 1, trả lỗi "mã OTP không đúng"
     (thông báo chung chung, không tiết lộ lý do cụ thể — cùng nguyên tắc
     "generic error message" đã áp dụng cho Login sai password ở D7)
```

**Output cần đạt:** Gọi thử qua Swagger với OTP đúng lấy từ email thật → `users.email_verified` chuyển thành `true`, nhận lại token dùng gọi API nghiệp vụ khác thành công ngay không cần login lại.

**Lưu ý:** Quyết định thiết kế cần chốt rõ (ghi vào README khi làm xong): **verify xong có tự động login luôn không, hay bắt user phải gọi lại `/auth/login`?** Tự động login (trả token luôn) là UX tốt hơn và tái dùng code đã có (không tốn công), khuyến nghị chọn cách này.

**Keyword tra cứu:** `OTP verification endpoint design`, `generic error message security best practice`

---

# F7. API Resend OTP — chống spam bằng rate limit / cooldown

**Input cần biết trước:** F6

**Khái niệm cốt lõi:** Endpoint mới: `POST /api/v1/auth/resend-otp`, xử lý 2 tình huống: user không nhận được mail (spam folder, gõ sai lúc register nhưng vẫn đúng email...), hoặc OTP cũ đã hết hạn.

**Vấn đề bảo mật cần giải quyết — vì sao KHÔNG được để gọi tự do:** Nếu không giới hạn, endpoint này có thể bị lợi dụng để **spam email** người khác (gọi resend liên tục cho 1 email không phải của mình → nạn nhân bị dội hàng trăm mail lạ) hoặc **brute-force tài nguyên gửi mail** (tốn quota Brevo API — đã ghi trong Project Overview là "no rate limit" ở tầng Brevo nhưng vẫn tốn chi phí/tài nguyên thật).

**Flow hoạt động:**
```
Client gọi POST /auth/resend-otp { email }
   → Tìm OTP record MỚI NHẤT của user (theo created_at DESC)
   → Kiểm tra cooldown: created_at của OTP gần nhất có cách hiện tại
     ĐỦ THỜI GIAN chưa (VD: tối thiểu 60 giây giữa 2 lần gửi)?
     → Nếu chưa đủ → trả lỗi 429 Too Many Requests, kèm số giây còn phải chờ
   → Nếu đủ điều kiện: sinh OTP MỚI (như F4), invalidate (đánh dấu consumed
     hoặc chỉ đơn giản là để hết hạn tự nhiên) OTP cũ, gửi email mới (F5)
```

**Output cần đạt:** Gọi Resend 2 lần liên tiếp trong vòng 60 giây → lần 2 bị chặn với thông báo rõ ràng thời gian còn lại. Gọi lại sau khi hết cooldown → thành công, nhận OTP mới, OTP cũ không còn dùng verify được nữa.

**Lưu ý:** Đây là dạng rate-limit đơn giản dựa vào `created_at` trong chính bảng `otp_verifications` (không cần thêm hạ tầng gì mới) — đủ dùng cho quy mô Fresher. Rate-limit nâng cao hơn (dựa vào Redis, giới hạn theo IP thay vì theo email) là kiến thức tốt để **biết tên**, không bắt buộc implement — project đã có Redis sẵn (Giai đoạn 5 cũ) nên đây là hướng mở rộng tự nhiên nếu muốn nâng cấp sau này.

**Keyword tra cứu:** `HTTP 429 Too Many Requests`, `simple cooldown rate limiting without Redis`, `API rate limiting strategies`

---

# F8. Chặn Login khi chưa verified — sửa luồng Authentication hiện có

**Input cần biết trước:** F6, đã có Login API hoàn chỉnh (C2-C4 trong plan cũ)

**Khái niệm cốt lõi:** Đây là bước **bắt buộc** để tính năng có tác dụng thật — nếu chỉ thêm API Verify mà không chặn Login, user vẫn login bình thường dù chưa verify, khiến toàn bộ tính năng vô nghĩa.

**2 cách implement, chọn 1 (nêu rõ lý do chọn trong code comment/README):**

| Cách | Cơ chế | Ưu điểm |
|---|---|---|
| **Cách A — trong `UserDetails`** | Override `isEnabled()` trả về `emailVerified` thay vì hard-code `true` | Spring Security **tự động** chặn ở tầng `AuthenticationManager` (C2), báo lỗi chuẩn `DisabledException`, không cần sửa `AuthService` thủ công |
| **Cách B — check thủ công trong `AuthService.login()`** | Sau khi `AuthenticationManager` xác thực đúng password, check thêm `if (!user.isEmailVerified()) throw ...` trước khi generate token | Chủ động hơn, dễ custom message lỗi riêng biệt rõ ràng ("vui lòng verify email") |

**Khuyến nghị:** Cách A là "đúng chuẩn Spring Security" hơn (tận dụng cơ chế có sẵn của framework thay vì tự check tay), nhưng Cách B dễ kiểm soát message lỗi trả về Client hơn — do project bạn đã quen custom exception + `GlobalExceptionHandler`, **Cách B phù hợp hơn với convention hiện tại của project**.

**Flow hoạt động (Cách B — mở rộng C2/C4 cũ):**
```
POST /auth/login { email, password } (như C2-C4 cũ)
   → AuthenticationManager xác thực email/password đúng chưa (không đổi)
   → THÊM: nếu đúng, check thêm user.isEmailVerified()
     → false → throw custom exception (VD: AccountNotVerifiedException) → 403 Forbidden
       (KHÔNG phải 401 — vì user ĐÃ xác thực đúng danh tính (đúng password),
       chỉ là CHƯA đủ điều kiện (chưa verify) → đúng bản chất Authorization,
       không phải Authentication — nhớ lại A1 đã học)
     → true → generate token như cũ (C3-C4), login thành công
```

**Output cần đạt:** Đăng ký user mới, KHÔNG verify, thử Login ngay → bị chặn với message rõ ràng "vui lòng xác thực email trước khi đăng nhập", không phải lỗi 500 hay lỗi password sai chung chung.

**Lưu ý:** Đây chính là lúc `isActive` (đã có) và `emailVerified` (mới thêm) **cùng xuất hiện** trong luồng Login — cần check **cả 2 điều kiện độc lập**: `isActive = false` (bị admin khoá) trả lỗi khác, `emailVerified = false` (chưa verify) trả lỗi khác — không được gộp chung 1 message vì nguyên nhân và hướng xử lý cho user hoàn toàn khác nhau (liên hệ admin vs. tự verify lại).

**Keyword tra cứu:** `UserDetails isEnabled override Spring Security`, `custom exception disabled account Spring Security`

---

# F9. Exception Handling riêng cho Verification — đồng bộ ErrorResponse

**Input cần biết trước:** F6-F8, `GlobalExceptionHandler` đã có sẵn (dùng cho D5 cũ)

**Khái niệm cốt lõi:** Thêm các exception mới, xử lý qua `GlobalExceptionHandler` có sẵn (không tạo handler riêng biệt như D5 — vì các lỗi này xảy ra ở tầng Service/Controller bình thường, không phải tầng Filter như 401/403 của D5):

| Exception gợi ý | HTTP Status | Tình huống |
|---|---|---|
| `OtpNotFoundException` hoặc tái dùng `ResourceNotFoundException` có sẵn | 404 | Không tìm thấy OTP record nào cho user |
| `OtpExpiredException` | 400 Bad Request | OTP đã hết hạn |
| `OtpInvalidException` | 400 Bad Request | Mã nhập sai (generic message, không tiết lộ chi tiết) |
| `OtpAttemptsExceededException` | 429 Too Many Requests | Nhập sai quá số lần cho phép |
| `AccountNotVerifiedException` | 403 Forbidden | Login khi chưa verify (dùng ở F8) |
| `ResendCooldownException` | 429 Too Many Requests | Resend quá sớm (dùng ở F7) |

**Output cần đạt:** Toàn bộ exception mới đều trả về đúng format `ErrorResponse` đồng nhất với các exception cũ trong project (tái sử dụng `GlobalExceptionHandler.@ExceptionHandler` pattern đã quen).

**Lưu ý:** Không nhất thiết phải tạo đủ 6 class exception riêng biệt nếu thấy dư thừa — có thể gộp `OtpNotFoundException`/`OtpExpiredException`/`OtpInvalidException` thành 1 `InvalidOtpException` chung với message khác nhau tuỳ tình huống, miễn là Client vẫn phân biệt được qua nội dung message. Đây là quyết định thiết kế cá nhân, không có đúng/sai tuyệt đối — chỉ cần nhất quán.

**Keyword tra cứu:** `Spring @ExceptionHandler custom business exception`, `HTTP status code for expired token 400 vs 410 Gone` (tìm hiểu thêm: 1 số API dùng `410 Gone` riêng cho "đã hết hạn" thay vì `400`, kiến thức mở rộng không bắt buộc)

---

# F10. Dọn dẹp OTP hết hạn — Scheduled Job

**Input cần biết trước:** F3-F9, đã có kinh nghiệm viết `@Scheduled` job (từ luồng VNPay expiry job trong Giai đoạn Payment cũ)

**Khái niệm cốt lõi:** Theo thời gian, bảng `otp_verifications` sẽ tích luỹ rất nhiều record cũ (đã hết hạn, đã consumed, hoặc bị bỏ dở không verify) — cần job định kỳ dọn dẹp, tránh bảng phình to vô hạn.

**Flow hoạt động:**
```
@Scheduled(cron = "...") chạy định kỳ (VD: mỗi ngày 1 lần vào giờ ít traffic)
   → DELETE FROM otp_verifications WHERE expires_at < now() - X ngày
     (giữ lại 1 khoảng đệm sau khi hết hạn, VD 7 ngày, để phục vụ audit/debug
     nếu cần tra cứu, không xoá ngay lập tức khi vừa hết hạn)
```

**Output cần đạt:** Job chạy đúng lịch, xác nhận qua log hoặc test thủ công bằng cách set `cron` chạy mỗi phút tạm thời để verify logic đúng trước khi đổi lại lịch thật.

**Lưu ý:** Đây là công việc **tối ưu**, không bắt buộc phải làm ngay để tính năng chạy đúng — có thể xếp làm cuối cùng sau khi F1-F9 đã hoàn chỉnh và đã test kỹ, tương tự cách VNPay expiry job đã được làm sau khi luồng thanh toán chính chạy ổn định.

**Keyword tra cứu:** `Spring @Scheduled cron expression`, `database cleanup job best practice retention period`

---

# F11. End-to-end test — checklist xác nhận hoàn thành

**Input cần biết trước:** F1-F10

**Checklist tự kiểm tra (làm tuần tự):**

- [ ] Register user mới → nhận email thật chứa OTP 6 số, DB có `users.email_verified = false`
- [ ] DB `otp_verifications` có dòng mới, `otp_hash` không đọc được raw OTP
- [ ] Login ngay sau register (chưa verify) → bị chặn 403, message rõ ràng "chưa xác thực email"
- [ ] Gọi `/auth/verify-otp` với OTP **sai** → lỗi generic, `attempts` tăng lên trong DB
- [ ] Gọi `/auth/verify-otp` với OTP sai đủ N lần (giới hạn đã đặt) → bị khoá tạm, phải Resend
- [ ] Gọi `/auth/verify-otp` với OTP **đúng** → `email_verified = true`, nhận token luôn (nếu chọn auto-login ở F6)
- [ ] Dùng token vừa nhận gọi API nghiệp vụ khác → thành công
- [ ] Gọi `/auth/verify-otp` lại lần 2 với OTP đã dùng (consumed) → bị từ chối, không verify lại được
- [ ] Đợi OTP hết hạn (hoặc set thời gian ngắn để test nhanh) → verify → lỗi hết hạn, không phải lỗi sai mã
- [ ] Gọi `/auth/resend-otp` 2 lần liên tiếp trong cooldown → lần 2 bị 429
- [ ] Gọi `/auth/resend-otp` sau cooldown → nhận OTP mới, OTP cũ không còn verify được
- [ ] User bị ADMIN khoá (`isActive=false`) nhưng đã verify email → login vẫn bị chặn, message KHÁC với message "chưa verify"
- [ ] Swagger test được toàn bộ luồng trên qua UI, không cần Postman

> ✅ Khi checklist F11 xanh hết, luồng Auth của bạn đã hoàn chỉnh: Đăng ký → Xác thực email → Login → JWT mỗi request → Refresh phiên → Logout — đúng chuẩn 1 hệ thống Auth thực chiến đầy đủ vòng đời, không chỉ dừng ở "đăng ký xong login được luôn" như bản MVP ban đầu.

---

## 📋 Tóm tắt thay đổi cần thực hiện trong codebase (tổng hợp từ F1-F10)

| Thành phần | Loại thay đổi |
|---|---|
| `UserEntity` | Thêm field `emailVerified` (boolean, default false) |
| `V3__add_otp_verifications_table.sql` | Migration mới — bảng `otp_verifications` |
| `OtpVerificationEntity` + `OtpVerificationRepository` | Mới — theo pattern đã quen (giống `RefreshTokenEntity`/`Repository`) |
| `AuthService`/`AuthServiceImpl` | Sửa `register()` (thêm sinh + gửi OTP), sửa `login()` (thêm check `emailVerified`), thêm `verifyOtp()`, thêm `resendOtp()` |
| `AuthController` | Thêm 2 endpoint: `POST /auth/verify-otp`, `POST /auth/resend-otp` |
| `SecurityFilterChain` | Thêm 2 endpoint mới vào danh sách `permitAll()` (public, giống `/auth/register`, `/auth/login`) |
| `GlobalExceptionHandler` | Thêm handler cho các exception mới ở F9 |
| `EmailService` | KHÔNG cần sửa — tái sử dụng nguyên trạng, chỉ gọi thêm từ `AuthServiceImpl` |
| `OtpCleanupScheduler` (hoặc gộp vào scheduler VNPay đã có) | Mới — job dọn OTP hết hạn (F10) |

---

## 📌 Ghi chú — những gì KHÔNG cần làm vội ở vòng này

| Khái niệm | Vì sao gác lại |
|---|---|
| SMS OTP | Cần tích hợp SMS Gateway trả phí (Twilio, eSMS...), ngoài phạm vi — chỉ cần biết tên để không lạ khi phỏng vấn |
| Reset Password bằng OTP | Cùng cơ chế OTP, nhưng là 1 flow nghiệp vụ khác (không cần login, không tạo user mới) — nên làm thành 1 giai đoạn riêng sau khi F xong, có thể tái dùng 80% hạ tầng đã xây (đây là lý do đã thiết kế sẵn cột `purpose` ở F3) |
| Rate-limit theo IP với Redis | Redis đã có sẵn trong project nhưng dùng cho Cache — mở rộng sang rate-limit là use case khác, nâng cao hơn cooldown đơn giản ở F7, không bắt buộc |
| Email Verification Link (thay vì OTP) | Đã phân tích trade-off ở F1, chỉ cần biết khi nào nên chọn hướng này thay vì OTP |

---

*Hết plan Account Verification. Đề xuất: hoàn thành tuần tự F1 → F11, không nhảy cóc — đặc biệt F2 (tách rõ 2 field) và F8 (chặn Login đúng cách) là 2 bước dễ gây lỗi logic nhất nếu làm vội. Nếu vướng ở bước nào, quay lại hỏi đúng mã giai đoạn (VD: "tôi đang vướng ở F6") để được hỗ trợ đúng trọng tâm.*
