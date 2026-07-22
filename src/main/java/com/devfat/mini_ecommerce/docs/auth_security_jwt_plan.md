# 🔐 PLAN CHUYÊN SÂU: Spring Security + JWT cho Mini Ecommerce

> Mục tiêu tối thượng: không chỉ "chạy được", mà **hiểu vì sao nó chạy** — để trả lời được khi nhà tuyển dụng hỏi "giải thích luồng JWT của bạn đi", và để áp dụng lại được ở công việc sau này.
> Nguyên tắc: KHÔNG đốt giai đoạn. Mỗi giai đoạn nhỏ có Input (cần biết gì trước) → Output (đạt được gì) → phải đạt Output rồi mới sang giai đoạn kế.
> Rule xuyên suốt: Không code mẫu đầy đủ để copy — chỉ code tham khảo dạng khung/pseudo khi thực sự cần thiết để hình dung, còn lại là khái niệm, flow, keyword tự tra.

---

## 🗺️ Bản đồ tổng thể — 5 giai đoạn lớn, 24 giai đoạn nhỏ

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

GIAI ĐOẠN LỚN E: Refresh Token — duy trì phiên đăng nhập an toàn & tiện lợi
  E1. Vì sao cần 2 loại token — bài toán đánh đổi bảo mật vs trải nghiệm
  E2. Thiết kế bảng refresh_tokens trong PostgreSQL — Flyway migration
  E3. Cấp cả 2 token lúc Login — sinh, hash, lưu refresh token
  E4. API Refresh Token — luồng làm mới access token
  E5. Revoke Token — Logout thật sự (không chỉ xoá token ở Client)
  E6. Bảo mật nâng cao — hash refresh token & giới thiệu Token Rotation
  E7. End-to-end test — checklist xác nhận hoàn thành
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
Đây là class Config trung tâm, nơi bạn "vẽ luật chơi" cho toàn bộ app. Class này **bắt buộc** phải đánh dấu `@Configuration` + `@EnableWebSecurity` — thiếu 2 annotation này, `SecurityFilterChain` Bean bạn khai báo sẽ không được Spring Boot nhận diện và toàn bộ cấu hình bên trong vô nghĩa (lỗi rất hay gặp, không báo exception rõ ràng, chỉ đơn giản là "code không có tác dụng gì").

Nội dung cần cấu hình trong class này:
- Endpoint nào public (VD: `/api/v1/auth/register`, `/api/v1/auth/login`, và cả `/swagger-ui/**`, `/v3/api-docs/**` để còn xem docs)
- Endpoint nào bắt buộc phải đăng nhập
- Tắt CSRF (vì CSRF protection chủ yếu dành cho session-based, JWT stateless không cần)
- Bật `SessionCreationPolicy.STATELESS` (đúc kết từ A4)
- **CORS (Cross-Origin Resource Sharing)** — cấu hình luôn từ bước này, dù project hiện tại chưa có FE

**Vì sao phải làm CORS ngay bây giờ, dù chưa có FE:** Trình duyệt áp dụng **Same-Origin Policy** — mặc định chặn request JS gọi từ 1 domain/port khác tới API của bạn (VD: FE chạy `localhost:3000` gọi BE `localhost:8085` sẽ bị chặn nếu server không khai báo rõ "tôi cho phép origin này gọi tôi"). Đây không phải lỗi của Spring Security mà là cơ chế bảo mật của trình duyệt — nhưng phải khai báo `CorsConfigurationSource` **trong cùng** `SecurityFilterChain` thì Security mới không chặn nhầm request OPTIONS (preflight request) mà trình duyệt tự động gửi trước mỗi request thật.

**Flow hoạt động (ở mức chưa có JWT, mới chỉ setup khung):**
```
Request tới → (nếu là request từ trình duyệt cross-origin: preflight OPTIONS check CORS trước)
   → SecurityFilterChain kiểm tra endpoint
   → nếu nằm trong permitAll() → cho qua thẳng, không cần auth
   → nếu không → yêu cầu phải authenticated (nhưng lúc này CHƯA có JwtFilter,
     nên tạm thời sẽ luôn bị chặn — điều này BÌNH THƯỜNG ở giai đoạn này,
     sẽ được nối tiếp hoàn chỉnh ở Giai đoạn D1)
```

**Output cần đạt:** App chạy lên được, gọi API cũ như `GET /products` hiện đang là public thì set `permitAll()` tạm thời để không vỡ chức năng cũ trong lúc đang xây Auth song song. Đã khai báo `CorsConfigurationSource` cho phép origin dev (VD: `http://localhost:3000`, hoặc để `*` tạm thời cho môi trường dev, siết lại origin cụ thể khi có FE thật/deploy production). Không cố hoàn thiện phân quyền chi tiết ở bước này — đó là việc của D2.

**Lưu ý quan trọng:**
- Đây là bước dễ khiến người mới "vỡ trận" nhất — vì sau khi bật Security, TOÀN BỘ API cũ (Product, Category, Order) tự nhiên sẽ bị chặn nếu bạn không khai báo `permitAll()` cho chúng tạm thời. Đừng hoảng — đây là hành vi đúng, chỉ cần tạm thời mở hết ra `permitAll()` ở bước này, siết lại chi tiết theo role ở D2-D3.
- Không dùng `@CrossOrigin` rải rác trên từng Controller — khai báo tập trung 1 lần trong `SecurityFilterChain` là cách chuẩn, tránh việc quên khai báo ở Controller mới thêm sau này.

**Keyword tra cứu:** `SecurityFilterChain Bean configuration Spring Boot 3`, `HttpSecurity permitAll authenticated`, `csrf disable stateless API`, `Spring Security CORS configuration CorsConfigurationSource`, `@EnableWebSecurity vs @Configuration`
**Doc chính thức CORS:** `https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html`

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

# GIAI ĐOẠN LỚN E — Refresh Token: duy trì phiên đăng nhập an toàn & tiện lợi

*Chỉ bắt đầu Giai đoạn E sau khi D7 đã xanh hết. Refresh Token xây trên nền JWT cơ bản đã hoàn thiện — nếu nền chưa vững, thêm refresh token sẽ chỉ khiến hệ thống rối hơn, không an toàn hơn.*

## E1. Vì sao cần 2 loại token — bài toán đánh đổi bảo mật vs trải nghiệm

**Input cần biết trước:** C1 (cấu trúc JWT), C3 (JwtProvider), D7 hoàn chỉnh

**Khái niệm cốt lõi — bài toán đánh đổi (trade-off) cốt lõi của mọi hệ thống Auth:**
- Access token sống **càng ngắn** → nếu bị đánh cắp, hacker chỉ lợi dụng được trong thời gian ngắn → **an toàn hơn**
- Nhưng access token sống ngắn → user bị bắt đăng nhập lại liên tục → **trải nghiệm tệ**

**Giải pháp 2 tầng token:**

| Loại token | Thời gian sống | Vai trò | Nơi lưu ở Server |
|---|---|---|---|
| **Access Token** | Ngắn (15–60 phút) | Đính kèm mọi request, dùng để xác thực (giống C3-C5 đã làm) | Không lưu — đúng tinh thần stateless (A4) |
| **Refresh Token** | Dài (7–30 ngày) | CHỈ dùng để xin access token mới khi access token hết hạn — không dùng trực tiếp gọi API nghiệp vụ | **Có lưu** trong DB (bảng `refresh_tokens`) |

**Điểm mấu chốt cần hiểu:** Refresh token phá vỡ 1 phần nguyên tắc "stateless thuần tuý" đã học ở A4 — vì giờ Server phải "nhớ" refresh token nào còn hợp lệ. Đây là đánh đổi có chủ đích: chấp nhận lưu trạng thái ở **1 phần nhỏ, ít bị gọi** (chỉ gọi lúc refresh) để đổi lấy khả năng **thu hồi quyền truy cập** (revoke) — điều mà JWT thuần (stateless 100%) không làm được, vì access token JWT đã phát ra thì không thể "gọi lại" giữa chừng.

**Output cần đạt:** Giải thích lại được bằng lời của mình câu hỏi phỏng vấn kinh điển: *"JWT hết hạn thì làm sao mà không bắt user login lại liên tục, mà vẫn an toàn nếu token bị lộ?"*

**Lưu ý:** Refresh token **không** thay thế access token, nó chỉ là "chìa khoá xin cấp lại vé mới" — Client vẫn phải dùng access token cho mọi request nghiệp vụ như luồng cũ (C4, C5) không đổi gì cả.

**Keyword tra cứu:** `refresh token vs access token`, `JWT token expiration strategy`
**Doc tham khảo uy tín:** OWASP JWT Cheat Sheet — `https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html`

---

## E2. Thiết kế bảng `refresh_tokens` trong PostgreSQL — Flyway migration

**Input cần biết trước:** E1, đã quen viết Flyway migration (`V1__init_mini_shop.sql` đã có sẵn kinh nghiệm)

**Khái niệm cốt lõi:** Vì refresh token cần **lưu và thu hồi được** (khác access token), cần 1 bảng riêng liên kết với `users`. Các cột tối thiểu cần có:

| Cột | Mục đích |
|---|---|
| `id` | PK |
| `user_id` (FK → `users.id`) | Refresh token này thuộc về ai |
| `token_hash` | **Hash** của refresh token (không lưu raw — xem kỹ lý do ở E6), dùng để đối chiếu lúc verify |
| `expires_at` | Thời điểm hết hạn (7–30 ngày sau khi tạo) |
| `revoked` (boolean, default false) | Đánh dấu đã bị thu hồi (logout hoặc bị phát hiện bất thường) chưa |
| `created_at` | Thời điểm cấp, phục vụ audit/debug |

**Output cần đạt:** File migration mới (VD: `V2__add_refresh_tokens_table.sql`) chạy thành công qua Flyway, bảng xuất hiện đúng trong `mini_shop` DB, có FK và index trên `user_id` (vì sẽ query theo user thường xuyên) và trên `token_hash` (vì sẽ query để verify mỗi lần refresh).

**Lưu ý:** Một `user_id` có thể có **nhiều** refresh token cùng lúc còn hiệu lực (VD: đăng nhập trên cả điện thoại và web) — đừng thiết kế theo kiểu 1 user chỉ 1 refresh token, trừ khi bạn cố tình muốn giới hạn 1 thiết bị đăng nhập (một lựa chọn thiết kế hợp lệ khác, nhưng cần nêu rõ trong README nếu chọn hướng này).

**Keyword tra cứu:** `Flyway migration add table foreign key index`, `PostgreSQL index on foreign key column`
**Doc chính thức:** PostgreSQL CREATE INDEX — `https://www.postgresql.org/docs/current/sql-createindex.html`; Flyway migration — `https://documentation.red-gate.com/fd/migrations-184127470.html`

---

## E3. Cấp cả 2 token lúc Login — sinh, hash, lưu refresh token

**Input cần biết trước:** C4 (Login API đã có access token), E2

**Khái niệm cốt lõi:** Mở rộng luồng Login (C4) đã có — không viết lại từ đầu, chỉ **thêm bước** sau khi access token đã được sinh ra thành công:

**Flow hoạt động:**
```
POST /api/v1/auth/login (như C4)
   → AuthenticationManager xác thực (C2) → thành công
   → JwtProvider.generateToken() → accessToken (như cũ)
   → SINH THÊM: refreshToken = chuỗi ngẫu nhiên an toàn (UUID hoặc SecureRandom,
     KHÔNG cần cấu trúc JWT có payload — refresh token chỉ cần là 1 chuỗi
     đủ ngẫu nhiên, khó đoán, vì bản thân nó không mang thông tin gì,
     chỉ là "khoá" để tra trong DB)
   → Hash refreshToken (giống cách hash password ở A2/B2) → lưu token_hash vào bảng refresh_tokens
   → Response trả về Client: { accessToken, refreshToken (bản RAW, chưa hash), expiresIn }
```

**Output cần đạt:** Login xong, kiểm tra DB thấy có dòng mới trong `refresh_tokens` với `token_hash` (không phải chuỗi refresh token gốc), Client nhận được refresh token dạng raw để lưu (thường ở FE là httpOnly cookie hoặc secure storage — bạn chỉ cần biết khái niệm này, chưa cần làm FE).

**Lưu ý quan trọng:** Đừng nhầm giữa 2 việc "sinh access token" (đã học ở C3, dùng JJWT) và "sinh refresh token" (ở đây, có thể chỉ cần `UUID.randomUUID()` hoặc `SecureRandom` — refresh token **không bắt buộc** phải có cấu trúc JWT, vì nó không cần tự chứa thông tin, chỉ cần đủ khó đoán và được đối chiếu qua DB).

**Keyword tra cứu:** `Java SecureRandom generate secure token`, `UUID randomUUID Java`

---

## E4. API Refresh Token — luồng làm mới access token

**Input cần biết trước:** E3

**Khái niệm cốt lõi:** Đây chính là API mới hoàn toàn, endpoint riêng (VD: `POST /api/v1/auth/refresh`), KHÔNG cần đi qua `JwtFilter` (C5) như các API nghiệp vụ khác — vì input của nó là refresh token, không phải access token.

**Flow hoạt động (đúng như trong ảnh bạn gửi):**
```
Client gọi API bất kỳ với accessToken đã hết hạn → nhận 401
   → Client tự động gọi POST /auth/refresh { refreshToken }
   → Server: hash refreshToken nhận được, tra trong bảng refresh_tokens theo token_hash
   → Kiểm tra: có tồn tại? chưa hết hạn (expires_at)? chưa bị revoked?
   → Nếu hợp lệ → JwtProvider.generateToken() sinh accessToken MỚI (tái dùng C3)
   → Trả accessToken mới cho Client, Client dùng tiếp mà KHÔNG cần bắt user login lại
   → Nếu refresh token cũng không hợp lệ (hết hạn/bị revoke) → 401,
     lúc này mới thực sự bắt buộc user đăng nhập lại từ đầu
```

**Output cần đạt:** Gọi thử API refresh qua Swagger với refresh token vừa lấy từ E3, nhận được access token mới, verify token mới này vẫn hoạt động đúng ở các API nghiệp vụ khác.

**Lưu ý:** Cần quyết định rõ: **refresh token có được cấp mới lại mỗi lần refresh không, hay giữ nguyên tới khi hết hạn?** Cách đơn giản (giữ nguyên) là đủ cho phạm vi project này — cách nâng cao hơn (cấp mới mỗi lần, gọi là Token Rotation) chỉ giới thiệu ở E6, không bắt buộc.

**Keyword tra cứu:** `refresh token endpoint design REST API`, `Spring Boot refresh token flow example`

---

## E5. Revoke Token — Logout thật sự (không chỉ xoá token ở Client)

**Input cần biết trước:** E2, E4

**Khái niệm cốt lõi:** Đây là lợi ích lớn nhất mà lưu refresh token vào DB mang lại so với JWT thuần "stateless 100%" (đã nhắc ở E1) — **Server có khả năng chủ động vô hiệu hoá phiên đăng nhập**, kể cả khi access token trên tay hacker vẫn còn hạn.

**Flow hoạt động:**
```
POST /api/v1/auth/logout { refreshToken }
   → Server hash refreshToken nhận được, tìm đúng dòng trong bảng refresh_tokens
   → Set revoked = true (hoặc xoá hẳn dòng đó — cả 2 cách đều chấp nhận được,
     nhưng giữ lại + đánh dấu revoked giúp audit/debug tốt hơn xoá cứng)
   → Từ giờ, API refresh (E4) gọi với refresh token này sẽ luôn bị từ chối
```

**Output cần đạt:** Gọi Logout xong, thử gọi lại API refresh với đúng refresh token vừa logout → phải bị từ chối (401), dù `expires_at` vẫn còn hạn.

**Lưu ý:** Access token đã phát ra **trước đó** vẫn còn hiệu lực tới khi tự hết hạn tự nhiên (vì access token không tra DB mỗi request — đây chính là giới hạn cố hữu của JWT stateless, không phải bug). Đây là lý do vì sao access token nên sống **ngắn** (E1) — hạn chế "cửa sổ rủi ro" này càng nhỏ càng tốt. Nếu bị hỏi "vậy logout xong mà access token vẫn dùng được vài phút thì sao?" — đây chính là câu trả lời chuẩn, không phải bạn code sai.

**Keyword tra cứu:** `JWT logout server side revoke strategy`, `stateless JWT limitation revocation`

---

## E6. Bảo mật nâng cao — hash refresh token & giới thiệu Token Rotation

**Input cần biết trước:** E2-E5 hoàn chỉnh

**Khái niệm cốt lõi — vì sao phải HASH refresh token trước khi lưu DB (đã làm ở E3, giải thích rõ ở đây):**
Refresh token có thời gian sống dài (7-30 ngày) và có quyền lực tương đương password (dùng để "xin" access token mới) — nếu DB của bạn không may bị lộ (SQL injection, backup file rò rỉ...), refresh token lưu dạng **raw** sẽ bị hacker dùng trực tiếp ngay lập tức. Áp dụng đúng nguyên tắc đã học ở A2/B2: **hash trước khi lưu, chỉ so sánh bằng hash**, y hệt cách xử lý password.

**Khái niệm giới thiệu (không bắt buộc code, chỉ cần biết tên và ý tưởng) — Token Rotation:**
Thay vì giữ nguyên 1 refresh token tới khi hết hạn, mỗi lần gọi `/auth/refresh` thành công, Server **thu hồi refresh token cũ và cấp refresh token mới** luôn trong cùng response. Lợi ích: nếu 1 refresh token bị đánh cắp và cả hacker lẫn user thật đều dùng nó để refresh, Server có thể phát hiện bất thường (2 lần refresh từ cùng 1 token gốc) và chủ động revoke toàn bộ chuỗi token liên quan.

**Output cần đạt:** Xác nhận lại code E3 đã hash đúng, không có chỗ nào lưu refresh token raw vào DB. Hiểu được khái niệm Token Rotation đủ để trình bày trong phỏng vấn nếu bị hỏi sâu, không cần implement.

**Lưu ý:** Đừng dùng lại `BCryptPasswordEncoder` cho refresh token nếu muốn tối ưu tốc độ — BCrypt cố tình chậm (đúng cho password, ít bị gọi), nhưng refresh token có thể bị gọi khá thường xuyên (mỗi lần access token hết hạn); nhiều hệ thống thực tế dùng **SHA-256** (nhanh hơn) cho việc hash refresh token, vì bản chất refresh token đã đủ ngẫu nhiên (không như password do người dùng tự đặt, dễ đoán hơn) nên không cần độ chậm chống brute-force của BCrypt. Đây là điểm tinh tế, nếu dùng BCrypt cho cả 2 cũng không sai, chỉ là chưa tối ưu.

**Keyword tra cứu:** `SHA-256 vs BCrypt use case difference`, `refresh token rotation OWASP`
**Doc tham khảo uy tín:** OWASP Session Management / Refresh Token rotation — `https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html`

---

## E7. End-to-end test — checklist xác nhận hoàn thành

**Input cần biết trước:** E1-E6

**Checklist tự kiểm tra:**

- [ ] Login → response có cả `accessToken` và `refreshToken`
- [ ] Kiểm tra DB: bảng `refresh_tokens` có dòng mới, `token_hash` không phải chuỗi gốc đọc được
- [ ] Đợi access token hết hạn (hoặc set thời gian ngắn để test nhanh, VD 30 giây) → gọi API nghiệp vụ → 401
- [ ] Gọi `POST /auth/refresh` với refresh token hợp lệ → nhận access token mới, dùng gọi API nghiệp vụ thành công
- [ ] Gọi `POST /auth/refresh` với refresh token **sai/không tồn tại** → 401
- [ ] Gọi `POST /auth/logout` → sau đó gọi lại `/auth/refresh` với đúng token đó → phải bị từ chối
- [ ] Refresh token hết hạn tự nhiên (`expires_at` đã qua) → gọi `/auth/refresh` → 401, không tự gia hạn ngầm

> ✅ Khi checklist E7 xanh hết, hệ thống Auth của bạn đã có đầy đủ vòng đời: Đăng ký → Login → Xác thực mỗi request → Làm mới phiên → Đăng xuất thu hồi — đúng chuẩn một hệ thống Auth thực chiến, không chỉ là bài tập JWT cơ bản.

---

## 📌 Ghi chú bổ sung — những khái niệm KHÔNG cần học vội ở vòng này

Để tránh việc lan man khiến giai đoạn Auth kéo dài quá mức trong timeline gấp, các khái niệm sau **cố tình gác lại**, chỉ nên biết tên để không bị lạ nếu gặp trong phỏng vấn, không cần implement:

| Khái niệm | Vì sao gác lại |
|---|---|
| OAuth2 / Login bằng Google/Facebook | Là 1 chuẩn khác hẳn, không liên quan trực tiếp tới JWT tự viết đang làm |
| Redis lưu blacklist/refresh token | PostgreSQL đã đủ dùng cho quy mô Fresher (xem Giai đoạn E) — Redis chỉ cần khi lượng token/tốc độ truy vấn thật sự lớn |
| Multi-factor Authentication (OTP, 2FA) | Nâng cao, không phải yêu cầu chuẩn cho Fresher |
| Token Rotation tự động (xoay refresh token mỗi lần dùng) | Giới thiệu khái niệm ở E6 để biết tên, nhưng KHÔNG bắt buộc implement — độ phức tạp tăng đáng kể so với lợi ích ở quy mô project này |

*(Refresh Token đã được đưa vào chính thức ở Giai đoạn Lớn E bên dưới, không còn nằm trong nhóm gác lại.)*

---

*Hết plan chuyên sâu Auth/Security. Đề xuất: đi từng Giai đoạn Lớn (A → B → C → D → E), trong mỗi giai đoạn lớn đi tuần tự từng giai đoạn nhỏ, không nhảy cóc. D (Auth cơ bản) là phần bắt buộc phải xong trước khi động vào E (Refresh Token) — E xây trên nền D, không thay thế. Nếu vướng ở bước nào, quay lại hỏi đúng mã giai đoạn (VD: "tôi đang vướng ở C5" hoặc "tôi đang vướng ở E4") để được hỗ trợ đúng trọng tâm.*
