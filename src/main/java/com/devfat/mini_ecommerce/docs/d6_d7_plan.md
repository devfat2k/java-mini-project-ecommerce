# 📋 Plan D6 + D7 — Swagger Bearer Token & End-to-End Test

> File bám theo để hoàn thành 2 giai đoạn cuối của module Auth/Security.
> Quy tắc: làm xong từng checkbox rồi mới xuống dòng tiếp theo.

---

## D6 — Swagger + Bearer Token

### Mục tiêu
Thêm nút **"Authorize 🔓"** vào Swagger UI để nhập JWT token 1 lần,
dùng được cho toàn bộ API cần login mà không phải copy thủ công từng request.

### File cần sửa
- `config/OpenApiConfig.java` — file DUY NHẤT cần sửa ở D6

---

### Checklist D6

#### Bước 1 — Thêm import cần thiết vào `OpenApiConfig.java`

- [ ] Import `io.swagger.v3.oas.models.Components`
- [ ] Import `io.swagger.v3.oas.models.security.SecurityRequirement`
- [ ] Import `io.swagger.v3.oas.models.security.SecurityScheme`

> IDE sẽ gợi ý tự động khi bạn gõ tên class — chọn đúng package `io.swagger.v3.oas.models.*`

---

#### Bước 2 — Khai báo SecurityScheme trong `customOpenAPI()`

Thêm vào `OpenAPI` bean hiện có:

```java
.components(
    new Components()
        .addSecuritySchemes("bearerAuth",
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        )
)
```

> **Giải thích từng dòng:**
> - `"bearerAuth"` → tên scheme, phải nhất quán với Bước 3 bên dưới
> - `type(HTTP)` → loại xác thực là HTTP (không phải API Key hay OAuth2)
> - `scheme("bearer")` → sub-type là Bearer token
> - `bearerFormat("JWT")` → chỉ là hint cho UI, không ảnh hưởng logic

---

#### Bước 3 — Khai báo SecurityRequirement global

Thêm tiếp vào cùng `OpenAPI` bean:

```java
.addSecurityItem(
    new SecurityRequirement().addList("bearerAuth")
)
```

> `"bearerAuth"` ở đây phải KHỚP tên đã đặt ở Bước 2.
> `addSecurityItem` = áp dụng yêu cầu xác thực cho TOÀN BỘ API trong Swagger.

---

#### Bước 4 — Kiểm tra SecurityConfig không chặn Swagger

Mở `SecurityConfig.java`, tìm dòng:
```java
.requestMatchers("/swagger-ui/**", "/v1/api-docs/**").permitAll()
```

- [ ] Xác nhận path `/v1/api-docs` khớp với `application.yaml` → `springdoc.api-docs.path: /v1/api-docs` (đã đúng)
- [ ] Nếu sau này Swagger bị 401 thì quay lại đây thêm pattern `/v1/api-docs` (không có `/**`)

---

#### Bước 5 — Verify D6

- [ ] Restart app
- [ ] Mở `http://localhost:8080/swagger-ui.html`
- [ ] **Phải thấy nút "Authorize 🔓"** ở góc phải trên cùng của trang Swagger
- [ ] Gọi `POST /api/v1/auth/login` → copy giá trị `accessToken` từ response
- [ ] Bấm nút **Authorize** → paste token (KHÔNG gõ chữ "Bearer" trước) → bấm **Authorize** → Close
- [ ] Gọi thử 1 API cần login (VD: `POST /api/v1/orders`) → phải không bị 401 nữa

> ⚠️ **Lưu ý khi paste token:**
> ```
> ĐÚNG:  eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
> SAI:   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
> ```
> Swagger tự thêm "Bearer " vì đã khai báo `scheme("bearer")` ở Bước 2.

---

## D7 — End-to-End Test Checklist

> Làm **tuần tự**, không bỏ dòng nào.

### Chuẩn bị trước khi test

- [ ] Có sẵn 2 tài khoản trong DB: 1 USER + 1 ADMIN
  - Nếu chưa có ADMIN: update thẳng trong DB
    ```sql
    UPDATE users SET role = 'ADMIN' WHERE email = 'admin@test.com';
    ```
- [ ] App đang chạy, Swagger UI mở sẵn

---

### Test #1 — Register user mới

- [ ] Gọi `POST /api/v1/auth/register` với email chưa tồn tại
- [ ] Kết quả: `201 Created`, response có `userId`, `email`, `fullName` — **KHÔNG có field password**
- [ ] Vào DB kiểm tra: cột `password` phải là chuỗi `$2a$10$...` (hash BCrypt thật)

---

### Test #2 — Register email trùng

- [ ] Gọi lại `POST /api/v1/auth/register` với **cùng email** ở Test #1
- [ ] Kết quả: lỗi Duplicate, format `ErrorResponse` giống các lỗi khác — **KHÔNG** phải 500

---

### Test #3 — Login đúng

- [ ] Gọi `POST /api/v1/auth/login` với email/password đúng
- [ ] Kết quả: `200 OK`, nhận `accessToken`
- [ ] Mở `https://jwt.io`, paste token → Payload phải thấy: `userId`, `sub` (email), `role`, `iat`, `exp`

---

### Test #4 — Login sai password

- [ ] Gọi `POST /api/v1/auth/login` với password sai
- [ ] Kết quả: lỗi chung chung "Email hoặc mật khẩu không đúng"
- [ ] **Không** được lộ message phân biệt "email không tồn tại" vs "password sai"

---

### Test #5 — Gọi API cần login, không có token

- [ ] Logout khỏi Swagger (hoặc mở tab ẩn danh)
- [ ] Gọi `POST /api/v1/orders` mà không Authorize
- [ ] Kết quả: `401 Unauthorized`, format `ErrorResponse` đúng chuẩn

---

### Test #6 — Token hết hạn

> **Chuẩn bị:** Vào `application.yaml`, tạm đổi `expiration: 5000` (5 giây), restart app

- [ ] Login → lấy token → chờ 6 giây
- [ ] Gọi API cần login với token đó
- [ ] Kết quả: `401` với message liên quan đến token hết hạn
- [ ] **Sau khi test xong:** đổi lại `expiration: 3600000`, restart app

---

### Test #7 — USER gọi API chỉ dành ADMIN

- [ ] Authorize bằng tài khoản **USER**
- [ ] Gọi `DELETE /api/v1/products/{id}`
- [ ] Kết quả: `403 Forbidden`, format `ErrorResponse` đúng chuẩn

---

### Test #8 — ADMIN gọi API quản trị

- [ ] Authorize bằng tài khoản **ADMIN**
- [ ] Gọi `DELETE /api/v1/products/{id}` (dùng id sản phẩm có thật trong DB)
- [ ] Kết quả: `200 OK`, sản phẩm bị xóa thành công

---

### Test #9 — IDOR: USER xem đơn hàng của người khác

- [ ] Authorize bằng tài khoản **USER A**
- [ ] Gọi `GET /api/v1/orders/{userId}` nhưng truyền `userId` của **USER B**
- [ ] Kết quả: `403 Forbidden` — không được xem đơn người khác

---

### Test #10 — POST /orders không cần truyền userId

- [ ] Authorize bằng tài khoản **USER**
- [ ] Gọi `POST /api/v1/orders` với body **không có field `userId`**
- [ ] Kết quả: `201 Created`, `userId` trong DB đúng với user đang đăng nhập

---

### Test #11 — Format lỗi 401 / 403 đồng nhất

- [ ] So sánh format response của lỗi `401` với lỗi validation `400`
- [ ] Kết quả: cùng cấu trúc JSON (`ErrorResponse`) — KHÔNG để Security trả HTML hoặc JSON khác lạ

---

### Test #12 — Toàn bộ luồng trên Swagger UI

- [ ] Xác nhận đã hoàn thành Test #1 → #11 **hoàn toàn qua Swagger UI**
- [ ] Bấm Authorize, nhập token, test được mà không cần mở Postman hay curl

---

## 🏁 Kết quả khi hoàn thành

| Tiêu chí | Trạng thái |
|---|---|
| Password hash BCrypt thật trong DB | ✅ |
| Stateless JWT (không session) | ✅ |
| 401 vs 403 đúng ngữ nghĩa | ✅ |
| Format lỗi đồng nhất toàn project | ✅ |
| IDOR được vá (không truyền userId tay) | ✅ |
| Generic error message khi login sai | ✅ |
| Swagger UI test được không cần Postman | ✅ |
