# 🔐 PLAN CHUYÊN SÂU: Spring Security + JWT cho Mini Ecommerce

> Mục tiêu tối thượng: không chỉ "chạy được", mà **hiểu vì sao nó chạy** — để trả lời được khi nhà tuyển dụng hỏi "giải thích luồng JWT của bạn đi", và để áp dụng lại được ở công việc sau này.
> Nguyên tắc: KHÔNG đốt giai đoạn. Mỗi giai đoạn nhỏ có Input (cần biết gì trước) → Output (đạt được gì) → phải đạt Output rồi mới sang giai đoạn kế.
> Rule xuyên suốt: Không code mẫu đầy đủ để copy — chỉ code tham khảo dạng khung/pseudo khi thực sự cần thiết để hình dung, còn lại là khái niệm, flow, keyword tự tra.

---

## 🗺️ Bản đồ tổng thể — 4 giai đoạn lớn, 17 giai đoạn nhỏ

```
GIAI ĐOẠN LỚN A: Nền tảng lý thuyết (không code)
  A1. Authentication vs Authorization
  A2. Vì sao không lưu password thô — Hashing & BCrypt
  A3. Kiến trúc Spring Security — Filter Chain tổng quan
  A4. Session-based vs Token-based (Stateless) — vì sao chọn JWT

GIAI ĐOẠN LỚN B: Xây "danh tính" — Đăng ký & xác thực user với Spring Security
  B1. UserDetails & UserDetailsService — cầu nối giữa UserEntity và Spring Security
  B2. PasswordEncoder — mã hoá tại tầng Service
  B3. SecurityFilterChain cơ bản — bật Security, cấu hình stateless
  B4. Register API — ráp nối toàn bộ B1-B3 vào 1 luồng thực tế

GIAI ĐOẠN LỚN C: JWT — tạo và xác minh "vé thông hành"
  C1. Cấu trúc JWT (Header.Payload.Signature) — khái niệm thuần
  C2. AuthenticationManager & luồng Login — xác thực trước khi phát token
  C3. JwtProvider/JwtUtil — sinh token, đọc token, validate token
  C4. Login API hoàn chỉnh — trả token cho Client
  C5. JwtFilter — "người gác cổng" đọc token ở mọi request sau

GIAI ĐOẠN LỚN D: Áp dụng vào project thật + hoàn thiện
  D1. Đăng ký JwtFilter vào Filter Chain — đúng vị trí, đúng thứ tự
  D2. Phân quyền theo Role — @PreAuthorize / method security
  D3. Áp dụng vào API cũ — sửa lại Product/Order theo quyền hạn thật
  D4. Lấy user hiện tại từ token — bỏ userId truyền tay (vá lỗ hổng bảo mật)
  D5. Exception Handling riêng cho Auth — 401 vs 403 đúng chuẩn
  D6. Swagger + Bearer Token — test lại toàn bộ bằng UI
  D7. End-to-end test thủ công — checklist xác nhận hoàn thành
```

---

# GIAI ĐOẠN LỚN A — Nền tảng lý thuyết

*Không viết một dòng code nào ở giai đoạn này. Nếu code trước khi hiểu A, bạn sẽ chỉ copy-paste mà không giải thích được với nhà tuyển dụng.*

## A1. Authentication vs Authorization

**Input cần biết trước:** Không cần gì, đây là điểm xuất phát.

**Khái niệm cốt lõi:**
- **Authentication (Xác thực)** = "Bạn là ai?" → Login, kiểm tra email/password đúng không
- **Authorization (Phân quyền)** = "Bạn được làm gì?" → Đã login rồi, nhưng có phải ADMIN để xóa sản phẩm không

**Vì sao quan trọng:** Toàn bộ Spring Security xoay quanh việc tách biệt 2 khái niệm này thành 2 pha xử lý riêng. Nhầm lẫn 2 khái niệm này là nguyên nhân số 1 khiến người mới học Security bị rối.

**Output cần đạt:** Tự vẽ được (trên giấy hoặc note) ví dụ cụ thể trong chính project của bạn:
- Ví dụ Authentication: ai đó gọi API login
- Ví dụ Authorization: user đã login nhưng bị chặn khi gọi API `PATCH /orders/{id}/status`

**Lưu ý:** HTTP status cũng phân biệt rõ 2 lỗi này — **401 Unauthorized** = chưa xác thực (chưa login/token sai), **403 Forbidden** = đã xác thực rồi nhưng không đủ quyền. Nhớ kỹ, sẽ cần ở giai đoạn D5.

**Keyword tra cứu:** `Authentication vs Authorization difference`, `HTTP 401 vs 403`

---

## A2. Vì sao không lưu password thô — Hashing & BCrypt

**Input cần biết trước:** A1

**Khái niệm cốt lõi:**
- **Hashing** là hàm một chiều: password → chuỗi hash, nhưng KHÔNG thể đảo ngược hash → password
- **BCrypt** là thuật toán hash chuyên dụng cho password (khác với MD5/SHA thông thường) vì nó có **salt** tự động (chuỗi ngẫu nhiên trộn vào trước khi hash) và **cố tình chậm** để chống brute-force
- Khi login, hệ thống **không** giải mã hash để so sánh — mà hash lại password người dùng nhập vào, rồi so 2 chuỗi hash với nhau

**Flow hoạt động:**
```
Lúc Register:  raw password → BCrypt.hash() → lưu chuỗi hash vào DB
Lúc Login:     raw password nhập vào → BCrypt.matches(raw, hashInDB) → true/false
```

**Output cần đạt:** Giải thích được vì sao 2 user có password giống hệt nhau ("123456") thì hash trong DB của họ vẫn khác nhau (do salt ngẫu nhiên).

**Lưu ý quan trọng:** Đây chính là chỗ cần sửa trong migration hiện tại của bạn (`DEFAULT 'hashed_password'` — placeholder, chưa phải hash thật). Đừng tự tay tạo hash giả trong SQL, để logic Register tạo hash thật khi giai đoạn B4 hoàn thành.

**Keyword tra cứu:** `BCrypt hashing algorithm explained`, `why not use MD5 SHA256 for password`

---

## A3. Kiến trúc Spring Security — Filter Chain tổng quan

**Input cần biết trước:** A1, A2. Cần biết khái niệm Servlet Filter trong Java Web cơ bản (nếu chưa rõ, tra nhanh `Java Servlet Filter là gì` trước).

**Khái niệm cốt lõi:**
Mọi HTTP request trước khi chạm tới Controller của bạn phải đi qua một **chuỗi Filter** (Filter Chain) do Spring Security dựng lên. Mỗi Filter làm một nhiệm vụ nhỏ: có filter kiểm tra CSRF, có filter kiểm tra session, có filter (mà bạn sẽ tự viết ở giai đoạn C5) kiểm tra JWT token.

```
Request → [Filter 1] → [Filter 2] → ... → [Filter JWT của bạn] → ... → DispatcherServlet → Controller
```

Nếu 1 filter nào đó từ chối (VD: token không hợp lệ), request bị chặn lại **ngay tại Filter**, không bao giờ tới được Controller.

**Output cần đạt:** Hiểu được vì sao khi code Controller, bạn **không cần** viết `if (user chưa login) return 401` thủ công trong từng method — vì Security đã chặn từ Filter trước khi request đến được Controller.

**Lưu ý:** Đừng cố học thuộc tên hết tất cả các Filter mặc định — chỉ cần hiểu **nguyên lý chuỗi filter**, vì bạn chỉ can thiệp thêm 1 filter mới (JwtFilter) vào chuỗi có sẵn, không viết lại từ đầu.

**Keyword tra cứu:** `Spring Security Filter Chain architecture diagram`
**Doc chính thức (đọc kỹ trang này, có sơ đồ trực quan):** `https://docs.spring.io/spring-security/reference/servlet/architecture.html`

---

## A4. Session-based vs Token-based (Stateless) — vì sao chọn JWT

**Input cần biết trước:** A3

**Khái niệm cốt lõi:**
- **Session-based (truyền thống):** Server lưu trạng thái đăng nhập trong bộ nhớ/DB (session), Client giữ `sessionId` qua cookie. Mỗi request, server phải tra lại session đó còn sống không → **stateful**, server phải "nhớ".
- **Token-based (JWT):** Server không lưu gì cả. Toàn bộ thông tin (user là ai, quyền gì, hết hạn khi nào) được **nhúng thẳng vào token**, ký (sign) bằng secret key. Mỗi request, server chỉ cần verify chữ ký token là biết token có bị giả mạo không → **stateless**, server "không cần nhớ" ai đã login.

**Output cần đạt:** Giải thích được bằng lời của mình: vì sao JWT phù hợp với REST API (đặc biệt là API cho mobile app/SPA sau này bạn build FE), trong khi Session phù hợp hơn cho web truyền thống render HTML từ server.

**Lưu ý:** Vì chọn stateless, ở giai đoạn B3 khi cấu hình `SecurityFilterChain`, bạn sẽ phải tắt hẳn cơ chế session mặc định (`SessionCreationPolicy.STATELESS`) — nếu quên bước này, Security vẫn sẽ cố tạo session dù bạn dùng JWT, gây lãng phí và sai kiến trúc.

**Keyword tra cứu:** `JWT stateless authentication vs session`, `SessionCreationPolicy STATELESS`

> ✅ **Chốt Giai đoạn Lớn A:** Nếu bạn tự giải thích lại được 4 mục A1-A4 cho một người không biết gì mà họ hiểu, bạn đã sẵn sàng bắt tay vào code ở Giai đoạn B. Đừng vội — đây là phần lý thuyết quyết định bạn "hiểu" hay chỉ "copy" toàn bộ phần sau.

---

# GIAI ĐOẠN LỚN B — Xây "danh tính": Đăng ký & xác thực user

## B1. UserDetails & UserDetailsService — cầu nối giữa UserEntity và Spring Security

**Input cần biết trước:** A3 (Filter Chain), đã có sẵn `UserEntity` trong project.

**Khái niệm cốt lõi:**
Spring Security **không biết** entity `UserEntity` của bạn là gì — nó chỉ làm việc với 1 interface chuẩn tên `UserDetails` (chứa username, password, danh sách quyền/authorities, các cờ trạng thái tài khoản). Bạn cần:
1. Tạo 1 class implements `UserDetails`, bọc quanh `UserEntity` hiện có (hoặc để `UserEntity` implements trực tiếp — cả 2 cách đều phổ biến, class bọc riêng sạch hơn về mặt tách biệt tầng)
2. Tạo 1 class implements `UserDetailsService`, chỉ có 1 nhiệm vụ: nhận `username` (ở đây là `email`), query DB qua `UserRepository` có sẵn, trả về `UserDetails`

**Flow hoạt động:**
```
Spring Security cần biết "user này có tồn tại và quyền gì không"
   → gọi loadUserByUsername(email)
   → UserDetailsServiceImpl query UserRepository.findByEmail(email)
   → convert UserEntity → UserDetails
   → trả về cho Spring Security xử lý tiếp
```

**Output cần đạt:** Có class `CustomUserDetailsService implements UserDetailsService` chạy được, có thể tự test bằng cách gọi thử `loadUserByUsername()` trong 1 unit test đơn giản (chưa cần liên quan tới HTTP request gì cả).

**Lưu ý:** `getAuthorities()` trong `UserDetails` chính là nơi bạn convert field `role` (USER/ADMIN) trong `UserEntity` thành định dạng Spring Security hiểu (`GrantedAuthority`). Đây là chỗ hay bị làm sai lúc mới học — role phải có tiền tố `ROLE_` (VD: `ROLE_ADMIN`) nếu dùng `hasRole()` sau này ở giai đoạn D2, hoặc không cần tiền tố nếu dùng `hasAuthority()`. Chọn 1 cách và nhất quán.

**Keyword tra cứu:** `Spring Security UserDetailsService custom implementation`, `GrantedAuthority SimpleGrantedAuthority`, `hasRole vs hasAuthority prefix ROLE_`

---

## B2. PasswordEncoder — mã hoá tại tầng Service

**Input cần biết trước:** A2 (BCrypt)

**Khái niệm cốt lõi:**
`PasswordEncoder` là interface Spring Security cung cấp, bạn khai báo 1 Bean `BCryptPasswordEncoder` trong 1 Config class. Bean này sau đó được inject vào bất kỳ Service nào cần hash/verify password (chủ yếu là `AuthService`/`UserService` ở giai đoạn B4).

**Output cần đạt:** Có 1 `@Bean PasswordEncoder passwordEncoder()` khai báo trong Config class, chạy inject được vào Service khác qua constructor injection.

**Lưu ý:** Đừng new `BCryptPasswordEncoder()` rải rác nhiều nơi trong code — khai báo 1 lần thành Bean, dùng lại xuyên suốt project là best practice chuẩn của Spring (Dependency Injection).

**Keyword tra cứu:** `Spring Bean PasswordEncoder configuration`, `BCryptPasswordEncoder strength parameter` (tìm hiểu thêm tham số "strength"/"rounds" ảnh hưởng tốc độ hash thế nào — không bắt buộc chỉnh, mặc định là đủ dùng)

---

## B3. SecurityFilterChain cơ bản — bật Security, cấu hình stateless

**Input cần biết trước:** A3, A4, B1, B2

**Khái niệm cốt lõi:**
Đây là class Config trung tâm, nơi bạn "vẽ luật chơi" cho toàn bộ app:
- Endpoint nào public (VD: `/api/v1/auth/register`, `/api/v1/auth/login`, và cả `/swagger-ui/**` để còn xem docs)
- Endpoint nào bắt buộc phải đăng nhập
- Tắt CSRF (vì CSRF protection chủ yếu dành cho session-based, JWT stateless không cần)
- Bật `SessionCreationPolicy.STATELESS` (đúc kết từ A4)

**Flow hoạt động (ở mức chưa có JWT, mới chỉ setup khung):**
```
Request tới → SecurityFilterChain kiểm tra endpoint
   → nếu nằm trong permitAll() → cho qua thẳng, không cần auth
   → nếu không → yêu cầu phải authenticated (nhưng lúc này CHƯA có JwtFilter,
     nên tạm thời sẽ luôn bị chặn — điều này BÌNH THƯỜNG ở giai đoạn này,
     sẽ được nối tiếp hoàn chỉnh ở Giai đoạn D1)
```

**Output cần đạt:** App chạy lên được, gọi API cũ như `GET /products` hiện đang là public thì set `permitAll()` tạm thời để không vỡ chức năng cũ trong lúc đang xây Auth song song. Không cố hoàn thiện phân quyền chi tiết ở bước này — đó là việc của D2.

**Lưu ý quan trọng:** Đây là bước dễ khiến người mới "vỡ trận" nhất — vì sau khi bật Security, TOÀN BỘ API cũ (Product, Category, Order) tự nhiên sẽ bị chặn nếu bạn không khai báo `permitAll()` cho chúng tạm thời. Đừng hoảng — đây là hành vi đúng, chỉ cần tạm thời mở hết ra `permitAll()` ở bước này, siết lại chi tiết theo role ở D2-D3.

**Keyword tra cứu:** `SecurityFilterChain Bean configuration Spring Boot 3`, `HttpSecurity permitAll authenticated`, `csrf disable stateless API`

---

## B4. Register API — ráp nối toàn bộ B1-B3 vào 1 luồng thực tế

**Input cần biết trước:** B1, B2, B3, và các thành phần cũ đã có (`DuplicateResourceException`, `GlobalExceptionHandler`, `ApiResponse<T>`)

**Khái niệm cốt lõi:** Đây là API đầu tiên **không thuần CRUD** trong project — nó có bước xử lý bảo mật (hash password) trước khi lưu.

**Flow hoạt động:**
```
Client gửi RegisterRequest (fullName, email, phone, password)
   → Validate DTO (@Valid, tái dùng kiến thức cũ)
   → Check trùng email/phone qua UserRepository (dùng DuplicateResourceException đã có)
   → Hash password bằng PasswordEncoder (từ B2)
   → Lưu UserEntity với password đã hash, role mặc định = USER
   → Trả về response (KHÔNG trả password, kể cả đã hash, ra ngoài response)
```

**Output cần đạt:** Gọi `POST /api/v1/auth/register` qua Swagger, kiểm tra trực tiếp trong DB (bằng psql hoặc DBeaver) thấy cột `password` là 1 chuỗi hash BCrypt thật (dạng `$2a$10$...`), không còn placeholder cũ.

**Lưu ý:**
- DTO response của Register **tuyệt đối không** chứa field password dù đã hash — đây là lỗi bảo mật cơ bản hay gặp
- Nên tạo package/module riêng tên `auth/` (chứa `AuthController`, `AuthService`) tách biệt với `UserController` hiện tại đang định làm CRUD quản lý user (2 việc khác nhau: Auth là đăng ký/đăng nhập, UserController có thể dùng cho ADMIN quản lý danh sách user sau này)

**Keyword tra cứu:** `Spring Boot Register API with password encoding`, `DTO exclude sensitive field response`

> ✅ **Chốt Giai đoạn Lớn B:** Bạn đã có thể tạo user mới với password hash thật, Security đã "bật" (dù chưa siết chặt). Bước tiếp theo (Giai đoạn C) là làm sao để user đã đăng ký có thể **đăng nhập và nhận 1 tấm vé (token)** để chứng minh danh tính ở các request sau.

---

# GIAI ĐOẠN LỚN C — JWT: Tạo và xác minh "vé thông hành"

## C1. Cấu trúc JWT (Header.Payload.Signature) — khái niệm thuần

**Input cần biết trước:** Giai đoạn B hoàn chỉnh

**Khái niệm cốt lõi:**
JWT là 1 chuỗi string gồm 3 phần nối bằng dấu `.`:
```
xxxxx.yyyyy.zzzzz
Header.Payload.Signature
```
- **Header:** metadata — thuật toán ký (VD: `HS256`)
- **Payload (Claims):** dữ liệu thực sự — `userId`, `email`, `role`, `exp` (thời gian hết hạn), `iat` (thời gian tạo)
- **Signature:** chữ ký, tạo ra bằng cách hash (Header + Payload) với 1 **secret key** chỉ Server biết → dùng để verify token có bị sửa đổi hay không

**Điều quan trọng cần hiểu rõ:** Payload **không được mã hoá**, chỉ encode Base64 — ai cũng đọc được nội dung nếu có token (thử paste token vào `jwt.io` sẽ thấy). JWT không giấu thông tin, nó chỉ **đảm bảo thông tin không bị giả mạo** nhờ Signature. → Tuyệt đối không nhét thông tin nhạy cảm (password, số thẻ...) vào payload.

**Output cần đạt:** Tự tay thử vào trang `jwt.io`, dán 1 token JWT mẫu bất kỳ (có sẵn ví dụ trên trang đó) vào để thấy rõ 3 phần tách biệt và payload đọc được bằng mắt thường.

**Lưu ý:** Secret key dùng để ký token phải để trong `application.yaml`/biến môi trường, **không hardcode trong code**, và không commit lên Git public repo.

**Keyword tra cứu:** `JWT structure explained header payload signature`, `jwt.io debugger`

---

## C2. AuthenticationManager & luồng Login — xác thực trước khi phát token

**Input cần biết trước:** B1, B3, C1

**Khái niệm cốt lõi:**
Trước khi phát token, hệ thống phải **xác thực** email/password có đúng không đã (đây chính là Authentication ở A1). Spring Security cung cấp sẵn cơ chế này qua `AuthenticationManager` — bạn không tự viết logic so sánh password thủ công, mà **giao việc đó cho Spring Security xử lý**, nó sẽ tự động gọi lại `UserDetailsService` (B1) và `PasswordEncoder` (B2) bạn đã khai báo.

**Flow hoạt động:**
```
Client gửi LoginRequest (email, password)
   → Tạo 1 UsernamePasswordAuthenticationToken(email, password) — token "thô" chưa xác thực
   → Đưa cho AuthenticationManager.authenticate(...)
   → Bên trong, Spring tự động: gọi UserDetailsService lấy user thật từ DB
     → dùng PasswordEncoder so sánh password nhập vào với hash trong DB
   → Nếu khớp → trả về Authentication đã xác thực (chứa thông tin user)
   → Nếu sai → tự động ném BadCredentialsException
```

**Output cần đạt:** Hiểu rõ đây chính là điểm hội tụ của B1 + B2 — bạn không viết lại logic so sánh, chỉ khai báo đúng các thành phần rồi để Spring tự ráp nối.

**Lưu ý:** Cần khai báo thêm 1 Bean `AuthenticationManager` trong Config (thường lấy từ `AuthenticationConfiguration` có sẵn) — đây là bước hay bị quên khi mới học.

**Keyword tra cứu:** `Spring Security AuthenticationManager authenticate UsernamePasswordAuthenticationToken`, `BadCredentialsException handling`

---

## C3. JwtProvider/JwtUtil — sinh token, đọc token, validate token

**Input cần biết trước:** C1, C2

**Khái niệm cốt lõi:** Tạo 1 class riêng (thường đặt tên `JwtProvider` hoặc `JwtUtil`) — đây là "nhà máy" xử lý mọi thứ liên quan JWT, tách biệt hoàn toàn khỏi logic nghiệp vụ. Class này cần 3 chức năng chính:
1. **generateToken(user)** — nhận thông tin user đã xác thực (từ C2), tạo ra chuỗi JWT
2. **validateToken(token)** — kiểm tra chữ ký còn đúng không, token còn hạn không
3. **getUsernameFromToken(token) / getClaimsFromToken(token)** — đọc ngược lại thông tin từ token đã có

**Thư viện cần chọn (khuyên chọn 1 để tập trung, không học cả 2):**
- `io.jsonwebtoken:jjwt` (thường gọi tắt JJWT) — nhiều tutorial tiếng Việt/Anh nhất, API rõ ràng
- hoặc `com.auth0:java-jwt`

**Output cần đạt:** Viết được unit test đơn giản (chưa cần liên quan HTTP): generate 1 token từ dữ liệu giả, sau đó validate + đọc ngược lại đúng dữ liệu đã nhét vào — chứng minh class hoạt động độc lập đúng trước khi ráp vào luồng Login thực tế.

**Lưu ý:** 
- Thời gian hết hạn (`expiration`) nên để biến cấu hình trong `application.yaml`, không hardcode số cứng trong code (VD: `jwt.expiration=86400000` — 24h tính theo ms)
- Bắt riêng exception khi token hết hạn (`ExpiredJwtException` nếu dùng JJWT) — sẽ cần dùng ở D5 để trả đúng message "token hết hạn" thay vì lỗi chung chung

**Keyword tra cứu:** `jjwt Spring Boot generate parse token example`, `JWT secret key Base64 encode application.yaml`

---

## C4. Login API hoàn chỉnh — trả token cho Client

**Input cần biết trước:** C2, C3

**Khái niệm cốt lõi:** Ráp nối toàn bộ: Controller nhận request → gọi `AuthenticationManager` (C2) xác thực → nếu thành công, gọi `JwtProvider.generateToken()` (C3) → trả token trong response.

**Flow hoạt động:**
```
POST /api/v1/auth/login {email, password}
   → AuthenticationManager.authenticate(...)
   → thành công → lấy thông tin user từ Authentication trả về
   → JwtProvider.generateToken(user) → chuỗi JWT
   → Response: { accessToken: "xxx", tokenType: "Bearer", expiresIn: ... }
```

**Output cần đạt:** Gọi `POST /auth/login` qua Swagger với tài khoản vừa Register ở B4, nhận được token JWT thật, paste vào `jwt.io` để tự kiểm chứng payload đúng dữ liệu user (email, role...).

**Lưu ý:** Sai password nên trả về message chung chung kiểu "Email hoặc mật khẩu không đúng" — **không** nói rõ "email không tồn tại" hay "password sai" riêng biệt, vì tách biệt 2 lỗi này giúp hacker dò được email nào có tồn tại trong hệ thống (security best practice, gọi là "user enumeration prevention").

**Keyword tra cứu:** `Login API design security best practice generic error message`

---

## C5. JwtFilter — "người gác cổng" đọc token ở mọi request sau

**Input cần biết trước:** A3 (Filter Chain), C3

**Khái niệm cốt lõi:** Đây là mảnh ghép còn thiếu để đóng vòng lặp: sau khi Client có token (từ C4), mọi request tiếp theo phải đính token vào header `Authorization: Bearer <token>`. Cần 1 Filter tự viết (`extends OncePerRequestFilter`) chạy **trước** khi request chạm Controller, để:
1. Đọc header `Authorization`
2. Tách lấy token, gọi `JwtProvider.validateToken()` (C3)
3. Nếu hợp lệ → đọc thông tin user từ token → **set vào `SecurityContextHolder`** (đây là bước quan trọng nhất, giúp toàn bộ phần còn lại của request "biết" ai đang gọi API mà không cần query DB lại)
4. Nếu không có token/token sai → để trống SecurityContext, cho request đi tiếp (sẽ tự bị chặn sau bởi luật `authenticated()` trong SecurityFilterChain nếu endpoint yêu cầu login)

**Flow hoạt động:**
```
Request có header Authorization: Bearer xxx
   → JwtFilter.doFilterInternal() chạy trước Controller
   → tách token, validate (C3)
   → hợp lệ → tạo Authentication object → SecurityContextHolder.getContext().setAuthentication(...)
   → request đi tiếp tới Controller, lúc này Controller/Service có thể lấy user hiện tại
     từ SecurityContext (sẽ dùng ở D4)
```

**Output cần đạt:** Class `JwtAuthenticationFilter extends OncePerRequestFilter` compile được, override đúng method `doFilterInternal()`.

**Lưu ý:** Đừng quên gọi `filterChain.doFilter(request, response)` ở cuối method — quên dòng này request sẽ bị "treo" mãi mãi, không bao giờ tới được Controller. Đây là lỗi runtime rất khó đoán ra nếu chưa biết trước.

**Keyword tra cứu:** `OncePerRequestFilter custom JWT filter implementation`, `SecurityContextHolder setAuthentication`

> ✅ **Chốt Giai đoạn Lớn C:** Bạn đã có đủ 3 mảnh: sinh token (C3), phát token qua Login (C4), đọc/verify token ở mọi request sau (C5). Nhưng JwtFilter vừa viết **CHƯA được Spring Security biết tới** — nó chỉ là 1 class đứng riêng lẻ. Giai đoạn D1 sẽ nối nó vào chuỗi Filter thật.

---

# GIAI ĐOẠN LỚN D — Áp dụng vào project thật + hoàn thiện

## D1. Đăng ký JwtFilter vào Filter Chain — đúng vị trí, đúng thứ tự

**Input cần biết trước:** B3 (đã có SecurityFilterChain khung), C5 (đã có JwtFilter class)

**Khái niệm cốt lõi:** Quay lại class Config ở B3, thêm dòng đăng ký JwtFilter vào chuỗi filter có sẵn của Spring Security, đặt **trước** filter xác thực mặc định (`UsernamePasswordAuthenticationFilter`) — vì JwtFilter cần chạy sớm để set Authentication trước khi các filter khác kiểm tra.

**Output cần đạt:** Test lại toàn bộ: gọi API cần login **không kèm token** → phải bị chặn 401. Gọi kèm token hợp lệ (lấy từ C4) → phải đi qua được.

**Lưu ý:** Đây là lúc phần "tạm thời permitAll() hết" ở B3 cần được xem xét lại nghiêm túc — vì giờ đã có cơ chế xác thực thật, có thể bắt đầu siết dần từng endpoint (chính thức làm ở D3).

**Keyword tra cứu:** `addFilterBefore UsernamePasswordAuthenticationFilter Spring Security`

---

## D2. Phân quyền theo Role — @PreAuthorize / method security

**Input cần biết trước:** B1 (đã convert role → GrantedAuthority), D1

**Khái niệm cốt lõi:** Bật `@EnableMethodSecurity` ở 1 Config class, cho phép dùng annotation `@PreAuthorize("hasRole('ADMIN')")` ngay trên method của Controller/Service — Spring sẽ tự kiểm tra quyền **trước khi** method đó chạy.

**Output cần đạt:** Hiểu rõ khác biệt giữa 2 cách kiểm soát quyền: khai báo tập trung trong `SecurityFilterChain` (theo URL pattern, ví dụ B3) **vs** khai báo rải trên từng method bằng `@PreAuthorize` (linh hoạt hơn, gắn liền logic method). Dự án nhỏ như của bạn nên ưu tiên `@PreAuthorize` vì dễ đọc, dễ đối chiếu ngay tại method.

**Lưu ý:** Nhắc lại từ B1 — nếu `getAuthorities()` trả về role dạng có tiền tố `ROLE_ADMIN`, dùng `hasRole('ADMIN')` (Spring tự thêm tiền tố `ROLE_` khi so sánh); nếu để role trần không tiền tố, phải dùng `hasAuthority('ADMIN')`. Sai chỗ này là lỗi phổ biến nhất khiến `@PreAuthorize` "không hoạt động" dù code không báo lỗi gì.

**Keyword tra cứu:** `@EnableMethodSecurity @PreAuthorize Spring Boot 3`, `hasRole prefix ROLE_ common mistake`

---

## D3. Áp dụng vào API cũ — sửa lại Product/Order theo quyền hạn thật

**Input cần biết trước:** D2

**Việc cụ thể cần rà lại trên toàn bộ project hiện có** (đối chiếu với danh sách API trong `PROJECT_OVERVIEW.md` bạn đã gửi):

| API | Quyền nên áp dụng |
|---|---|
| `POST /products`, `PATCH /products/{id}`, `DELETE /products/{id}` | ADMIN |
| `PATCH /products/increase`, `/decrease` | ADMIN |
| `GET /products`, `GET /products/{id}` | Public (không cần login — khách vãng lai vẫn xem được sản phẩm) |
| `GET /products/top-buy`, `/revenue-*` | ADMIN (số liệu kinh doanh không public) |
| `POST /categories`, `PUT`, `DELETE` | ADMIN |
| `GET /categories` | Public |
| `POST /orders` | USER đã đăng nhập (bất kỳ ai đã login đều tạo được đơn cho chính mình) |
| `GET /orders/{userId}` | USER chỉ xem đơn của chính mình, hoặc ADMIN xem tất cả — **đây là ca đặc biệt**, cần D4 mới xử lý trọn vẹn |
| `PATCH /orders/{id}/status` | ADMIN |

**Output cần đạt:** Test lại bằng 2 tài khoản (1 USER, 1 ADMIN) qua Swagger, xác nhận đúng ma trận phân quyền trên.

**Lưu ý:** Dòng "USER chỉ xem đơn của chính mình" **không thể** giải quyết chỉ bằng `@PreAuthorize("hasRole('USER')")` — vì nó cần so sánh `userId` trong token với `userId` trong đường dẫn URL. Đây chính là lý do cần D4.

---

## D4. Lấy user hiện tại từ token — bỏ userId truyền tay (vá lỗ hổng bảo mật)

**Input cần biết trước:** C5 (đã set Authentication vào SecurityContext), D3

**Khái niệm cốt lõi:** Vì D1 đã đảm bảo mọi request có token hợp lệ đều có sẵn thông tin user trong `SecurityContextHolder`, Controller/Service không cần nhận `userId` từ Client gửi lên nữa — lấy trực tiếp từ context, đảm bảo user A không thể giả mạo tạo đơn hàng dưới tên user B.

**2 cách phổ biến để lấy user hiện tại trong Controller:**
1. Dùng `@AuthenticationPrincipal` trong tham số method Controller (Spring tự inject)
2. Lấy thủ công qua `SecurityContextHolder.getContext().getAuthentication()`

**Output cần đạt:**
- Sửa `POST /orders`: bỏ field `userId` khỏi `CreateOrderRequest`, lấy từ token
- Sửa `GET /orders/{userId}`: nếu role là USER, so sánh `userId` trong path với user hiện tại từ token, nếu không khớp → 403 (dùng `AccessDeniedException` hoặc tự custom); nếu role là ADMIN, cho qua luôn

**Lưu ý:** Đây chính là lỗ hổng bảo mật kinh điển gọi là **IDOR (Insecure Direct Object Reference)** — rất hay bị hỏi trong phỏng vấn hoặc bị soi trong code review, vì vậy xử lý đúng ở đây là điểm cộng lớn, không phải chi tiết nhỏ.

**Keyword tra cứu:** `@AuthenticationPrincipal Spring Security get current user`, `IDOR vulnerability explained`

---

## D5. Exception Handling riêng cho Auth — 401 vs 403 đúng chuẩn

**Input cần biết trước:** A1 (401 vs 403), `GlobalExceptionHandler` đã có sẵn

**Khái niệm cốt lõi:** Mặc định, khi Security chặn 1 request (chưa login hoặc sai quyền), nó **không** đi qua `GlobalExceptionHandler` hiện có của bạn — vì lỗi này xảy ra ở tầng Filter, trước khi tới Controller/Exception Handler của Spring MVC. Cần khai báo thêm 2 thành phần riêng:
- `AuthenticationEntryPoint` — xử lý khi chưa xác thực (401)
- `AccessDeniedHandler` — xử lý khi xác thực rồi nhưng sai quyền (403)

**Output cần đạt:** Format lỗi trả về khi bị chặn bởi Security phải **đồng nhất** với `ErrorResponse` bạn đã dùng cho toàn bộ project (không để Security trả về trang lỗi HTML mặc định hoặc JSON format khác lạ).

**Lưu ý:** Đây là chi tiết nhỏ nhưng ảnh hưởng lớn tới trải nghiệm nhất quán khi demo — nếu bỏ qua bước này, người xem sẽ thấy 90% lỗi trả về đẹp theo `ErrorResponse`, riêng lỗi Auth lại xấu khác hẳn, trông thiếu chuyên nghiệp.

**Keyword tra cứu:** `Spring Security custom AuthenticationEntryPoint AccessDeniedHandler JSON response`

---

## D6. Swagger + Bearer Token — test lại toàn bộ bằng UI

**Input cần biết trước:** Đã có `OpenApiConfig.java` sẵn trong project

**Khái niệm cốt lõi:** Thêm khai báo `SecurityScheme` kiểu `bearerFormat: JWT` vào config Swagger hiện có, để giao diện Swagger UI xuất hiện nút "Authorize" — nhập token 1 lần, dùng được cho mọi API cần login trong lúc test, không phải copy token thủ công vào từng request.

**Output cần đạt:** Vào Swagger UI, bấm Authorize, dán token lấy từ `/auth/login`, gọi thử 1 API cần ADMIN — thành công.

**Keyword tra cứu:** `springdoc-openapi bearer token Authorize button configuration`

---

## D7. End-to-end test thủ công — checklist xác nhận hoàn thành

**Input cần biết trước:** Toàn bộ D1-D6

**Checklist tự kiểm tra (làm tuần tự, không bỏ bước nào):**

- [ ] Register user mới → kiểm tra DB có password hash thật (không phải plaintext)
- [ ] Register lại email trùng → nhận đúng lỗi Duplicate, không phải lỗi 500 chung chung
- [ ] Login đúng email/password → nhận token, decode thử trên jwt.io thấy đúng claims
- [ ] Login sai password → nhận lỗi chung chung, không lộ "email đúng nhưng password sai"
- [ ] Gọi API cần login mà KHÔNG có token → 401
- [ ] Gọi API cần login với token hết hạn/sai → 401
- [ ] Login bằng USER, gọi API chỉ dành ADMIN (VD: DELETE product) → 403
- [ ] Login bằng ADMIN, gọi được toàn bộ API quản trị
- [ ] USER A cố xem đơn hàng của USER B qua `GET /orders/{userId của B}` → bị chặn 403
- [ ] `POST /orders` không cần truyền `userId` nữa, đơn được tạo đúng cho user đang đăng nhập
- [ ] Format lỗi 401/403 giống với format `ErrorResponse` của các lỗi khác trong project
- [ ] Swagger UI test được toàn bộ luồng trên bằng nút Authorize, không cần Postman

> ✅ Khi checklist trên xanh hết, module Auth/Security coi như **hoàn chỉnh và đủ chuẩn để show cho nhà tuyển dụng** — không chỉ chạy được, mà đúng nguyên tắc bảo mật cơ bản (IDOR, generic error message, stateless, hash password).

---

## 📌 Ghi chú bổ sung — những khái niệm KHÔNG cần học vội ở vòng này

Để tránh việc lan man khiến giai đoạn Auth kéo dài quá mức trong timeline gấp, các khái niệm sau **cố tình gác lại**, chỉ nên biết tên để không bị lạ nếu gặp trong phỏng vấn, không cần implement:

| Khái niệm | Vì sao gác lại |
|---|---|
| Refresh Token | Access token 1 tầng là đủ cho Fresher project; refresh token thêm độ phức tạp về vòng đời token, lưu trữ, thu hồi |
| OAuth2 / Login bằng Google/Facebook | Là 1 chuẩn khác hẳn, không liên quan trực tiếp tới JWT tự viết đang làm |
| Redis lưu blacklist token (logout thật sự) | Với JWT thuần, "logout" thường chỉ là Client tự xoá token — logout server-side cần thêm hạ tầng lưu trạng thái, đi ngược nguyên tắc stateless cơ bản |
| Multi-factor Authentication (OTP, 2FA) | Nâng cao, không phải yêu cầu chuẩn cho Fresher |

Nếu muốn, sau khi xong toàn bộ 4 tuần và có dư thời gian, có thể quay lại tìm hiểu **Refresh Token** trước tiên trong nhóm này — vì nó là câu hỏi phỏng vấn phổ biến thứ 2 sau JWT cơ bản ("JWT hết hạn thì làm sao mà không bắt user login lại liên tục?").

---

*Hết plan chuyên sâu Auth/Security. Đề xuất: đi từng Giai đoạn Lớn (A → B → C → D), trong mỗi giai đoạn lớn đi tuần tự từng giai đoạn nhỏ, không nhảy cóc. Nếu vướng ở bước nào, quay lại hỏi đúng mã giai đoạn (VD: "tôi đang vướng ở C5") để được hỗ trợ đúng trọng tâm.*
