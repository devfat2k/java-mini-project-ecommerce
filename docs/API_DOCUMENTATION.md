# HƯỚNG DẪN VÀ TÀI LIỆU TÍCH HỢP API TOÀN BỘ DỰ ÁN (FOR FRONTEND FE)

Document này tổng hợp chi tiết **toàn bộ 35 API** hiện có của dự án **Mini E-Commerce Backend (Spring Boot)**. Giúp đội ngũ Frontend (FE) hiểu rõ cấu trúc Request/Response, quy chuẩn bảo mật, phân quyền, xử lý lỗi và cách mapping API vào giao diện người dùng.

---

## I. TỔNG QUAN VÀ QUY CHUẨN KỸ THUẬT CHUNG

### 1. Thông Tin Cơ Bản
* **Base URL**: `http://localhost:8080` (Development)
* **Content-Type**: `application/json` (mặc định ngoại trừ các API Upload File sử dụng `multipart/form-data`).
* **Authentication**: Sử dụng **JWT (JSON Web Token)**.
  * Đưa Token vào Request Header: `Authorization: Bearer <accessToken>`
  * Phân quyền dựa trên Roles (`ROLE_USER`, `ROLE_ADMIN`) và Permissions (`rbac:manage`, `product:create`, `product:update`, `product:delete`).

---

### 2. Cấu Trúc Khung Response Chuẩn (`ApiResponse<T>`)
Tất cả các API trả về data đều bọc trong object `ApiResponse` chuẩn:

#### Success Response Format:
```json
{
  "success": true,
  "message": "Thông điệp thành công",
  "data": { ... }, // T hoặc null
  "timestamp": "2026-08-10T01:30:00.123456"
}
```

#### Error Response Format (4xx / 5xx):
```json
{
  "success": false,
  "message": "Mô tả lỗi từ Server",
  "data": null,
  "timestamp": "2026-08-10T01:30:00.123456"
}
```

#### Response Phân Trang Chuẩn (`PageResponse<T>`) (Ví dụ danh sách Sản Phẩm, Đơn Hàng, User):
Nằm trong thuộc tính `data` của `ApiResponse`:
```json
{
  "success": true,
  "message": "Get list successfully",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "last": false
  },
  "timestamp": "2026-08-10T01:30:00"
}
```

---

### 3. Cấu Trúc Lỗi Validation (Status Code `400 Bad Request`)
Khi gửi dữ liệu không hợp lệ (vi phạm `@Valid`), server trả về danh sách các field bị lỗi trong `data`:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email is valid patent!",
    "password": "Password is min 8 and max 100 character!"
  },
  "timestamp": "2026-08-10T01:30:00"
}
```

---

### 4. Quy Định HTTP Status Codes
* `200 OK`: Truy vấn thành công, cập nhật/lấy dữ liệu thành công.
* `201 Created`: Tạo mới thành công (Đăng ký, Tạo sản phẩm, Tạo đơn hàng, Tạo Role).
* `240 No Content (204)`: Cập nhật thông tin profile/trạng thái thành công.
* `400 Bad Request`: Dữ liệu gửi lên vi phạm validation hoặc file vượt dung lượng cho phép.
* `401 Unauthorized`: Chưa đăng nhập, Access Token hết hạn hoặc sai thông tin đăng nhập.
* `403 Forbidden`: Token hợp lệ nhưng tài khoản không có quyền truy cập (thiếu Role/Permission) hoặc bị khóa.
* `409 Conflict`: Dữ liệu bị trùng lặp (ví dụ email đã đăng ký) hoặc xung đột Optimistic Locking.
* `429 Too Many Requests`: Gửi request quá nhanh (Rate limit), đi kèm Header `Retry-After: <số_giây>`.
* `500 Internal Server Error`: Lỗi hệ thống backend.

---

### 5. Các Enum Quan Trọng
* **`OtpPurpose`**:
  * `REGISTER_VERIFICATION`: Xác thực Email sau khi đăng ký tài khoản.
  * `RESET_PASSWORD`: Quên mật khẩu.
  * `CHANGE_PASSWORD_CONFIRMATION`: Xác nhận đổi mật khẩu (nếu sử dụng OTP).
* **`OrderStatus`**:
  * `PENDING`: Đơn hàng mới tạo, chờ xử lý/thanh toán.
  * `CONFIRMED`: Đã xác nhận đơn hàng.
  * `SHIPPED`: Đang giao hàng.
  * `DONE`: Đơn hàng hoàn tất.
  * `CANCELLED`: Đã hủy đơn.

---

## II. DANH SÁCH CHI TIẾT CÁC MODULE API

---

### MODULE 1: AUTHENTICATION (`/api/v1/auth`)

#### 1. Đăng ký tài khoản (`POST /api/v1/auth/register`)
* **Quyền truy cập**: Public
* **Mô tả**: Đăng ký người dùng mới. Tài khoản sẽ được tạo nhưng ở trạng thái chờ xác thực OTP (`emailVerified = false`).
* **Request Body**:
```json
{
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "phoneNumber": "0912345678",
  "password": "Password123"
}
```
* **Validation**: `email` chuẩn định dạng, `phoneNumber` 10-11 chữ số, `password` từ 8-100 ký tự.
* **Response Status**: `201 Created`
* **Response Body**: `ApiResponse<UserResponseDto>`

---

#### 2. Đăng nhập (`POST /api/v1/auth/login`)
* **Quyền truy cập**: Public *(Rate limited theo IP)*
* **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Login Successfully!",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "d8a7c2e...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  },
  "timestamp": "2026-08-10T01:30:00"
}
```

---

#### 3. Làm mới Access Token (`POST /api/v1/auth/refresh-token`)
* **Quyền truy cập**: Public
* **Request Body**:
```json
{
  "refreshToken": "d8a7c2e..."
}
```
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Refresh Token Successfully!",
  "data": {
    "accessToken": "eyJhbGciOi..."
  },
  "timestamp": "2026-08-10T01:30:00"
}
```

---

#### 4. Đăng xuất (`POST /api/v1/auth/logout`)
* **Quyền truy cập**: Public / Authenticated
* **Request Body**:
```json
{
  "refreshToken": "d8a7c2e..."
}
```
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<Void>` (data = null). FE thực hiện xóa token ở local storage / cookie.

---

#### 5. Xác thực OTP (`POST /api/v1/auth/verify-otp`)
* **Quyền truy cập**: Public *(Rate limited theo IP)*
* **Mô tả**: Dùng để xác thực OTP 6 chữ số gửi qua email.
* **Request Body**:
```json
{
  "email": "user@example.com",
  "otpCode": "123456",
  "purpose": "REGISTER_VERIFICATION" // Hoặc RESET_PASSWORD
}
```
* **Response Status**: `200 OK`
* **Response Body**:
  * Nếu `purpose = REGISTER_VERIFICATION`: Trả về `accessToken` và `refreshToken` (đã kích hoạt tài khoản).
  * Nếu `purpose = RESET_PASSWORD`: Trả về `actionToken` để FE dùng cho bước gọi API reset password.
```json
{
  "success": true,
  "message": "Verify Otp Successfully!",
  "data": {
    "accessToken": "eyJhbGci...", // Có nếu REGISTER_VERIFICATION
    "refreshToken": "d8a7c2...", // Có nếu REGISTER_VERIFICATION
    "actionToken": "act_89f72..." // Có nếu RESET_PASSWORD
  },
  "timestamp": "2026-08-10T01:30:00"
}
```

---

#### 6. Gửi lại mã OTP (`POST /api/v1/auth/resend-otp`)
* **Quyền truy cập**: Public *(Rate limited theo IP)*
* **Request Body**:
```json
{
  "email": "user@example.com",
  "purpose": "REGISTER_VERIFICATION" // Hoặc RESET_PASSWORD
}
```
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Resend Otp Successfully!",
  "data": {
    "message": "OTP resent successfully"
  },
  "timestamp": "2026-08-10T01:30:00"
}
```

---

#### 7. Yêu cầu Quên mật khẩu (`POST /api/v1/auth/forgot-password`)
* **Quyền truy cập**: Public *(Rate limited theo IP)*
* **Mô tả**: Nhập email để nhận mã OTP khôi phục mật khẩu.
* **Request Body**:
```json
{
  "email": "user@example.com"
}
```
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<String>` (data = null).

---

#### 8. Đặt lại mật khẩu (`POST /api/v1/auth/reset-password`)
* **Quyền truy cập**: Public
* **Mô tả**: Sử dụng `actionToken` (nhận được sau khi verify OTP mục 5) để cập nhật mật khẩu mới.
* **Request Body**:
```json
{
  "actionToken": "act_89f72...",
  "newPassword": "NewPassword123"
}
```
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<Void>` (data = null).

---

### MODULE 2: USER MANAGEMENT (`/api/v1/users`)

#### 1. Lấy thông tin tài khoản hiện tại (`GET /api/v1/users/me`)
* **Quyền truy cập**: Bearer Token (User / Admin)
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Get User Successfully!",
  "data": {
    "userId": 1,
    "fullName": "Nguyen Van A",
    "avatarUrl": "https://res.cloudinary.com/...",
    "email": "user@example.com",
    "phoneNumber": "0912345678",
    "roles": ["ROLE_USER"],
    "active": true,
    "createdAt": "2026-08-01T10:00:00",
    "emailVerified": true
  },
  "timestamp": "2026-08-10T01:30:00"
}
```

---

#### 2. Cập nhật thông tin cá nhân (`PATCH /api/v1/users/me/update`)
* **Quyền truy cập**: Bearer Token
* **Request Body**:
```json
{
  "fullName": "Nguyen Van B",
  "phoneNumber": "0987654321"
}
```
* **Response Status**: `204 No Content` (Không có body)

---

#### 3. Upload ảnh đại diện (`POST /api/v1/users/me/avatar`)
* **Quyền truy cập**: Bearer Token
* **Content-Type**: `multipart/form-data`
* **Request Form Data**:
  * `file`: File ảnh (PNG/JPG, max 5MB).
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<UserResponseDto>` (UserResponseDto đã cập nhật `avatarUrl`).

---

#### 4. Đổi mật khẩu (`POST /api/v1/users/change-password`)
* **Quyền truy cập**: Bearer Token
* **Request Body**:
```json
{
  "oldPassword": "Password123",
  "newPassword": "NewPassword456"
}
```
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<Void>` (data = null).

---

#### 5. Xem danh sách toàn bộ User (`GET /api/v1/users`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Query Parameters**:
  * `page`: Trang cần lấy (0-indexed, mặc định 0)
  * `size`: Số bản ghi/trang (mặc định 10)
  * `sort`: Sắp xếp (VD: `createdAt,desc`)
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<PageResponse<UserResponseDto>>`

---

#### 6. Khóa / Kích hoạt tài khoản User (`PATCH /api/v1/users/{userId}/status`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Query Parameters**:
  * `isActive`: `true` (mở khóa) hoặc `false` (khóa tài khoản)
* **Example URL**: `/api/v1/users/5/status?isActive=false`
* **Response Status**: `204 No Content`

---

### MODULE 3: RBAC ADMIN MANAGEMENT (`/api/v1/admin/rbac`)

#### 1. Lấy danh sách Vai trò (Roles) (`GET /api/v1/admin/rbac/roles`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')` hoặc `hasAuthority('rbac:manage')`)
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Get all roles successfully",
  "data": [
    {
      "id": 1,
      "name": "ROLE_ADMIN",
      "description": "Administrator Role",
      "permissions": [
        { "id": 1, "code": "product:create", "description": "Tạo sản phẩm" }
      ]
    }
  ]
}
```

---

#### 2. Tạo Vai trò mới (`POST /api/v1/admin/rbac/roles`)
* **Quyền truy cập**: Admin / `rbac:manage`
* **Request Body**:
```json
{
  "name": "ROLE_MANAGER",
  "description": "Quản lý cửa hàng"
}
```
* **Response Status**: `201 Created`
* **Response Body**: `ApiResponse<RoleResponseDto>`

---

#### 3. Lấy danh sách Quyền (Permissions) (`GET /api/v1/admin/rbac/permissions`)
* **Quyền truy cập**: Admin / `rbac:manage`
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Get all permissions successfully",
  "data": [
    { "id": 1, "code": "product:create", "description": "Tạo sản phẩm" },
    { "id": 2, "code": "product:update", "description": "Sửa sản phẩm" },
    { "id": 3, "code": "product:delete", "description": "Xóa sản phẩm" },
    { "id": 4, "code": "rbac:manage", "description": "Quản lý vai trò & phân quyền" }
  ]
}
```

---

#### 4. Cập nhật Quyền cho Vai trò (`PATCH /api/v1/admin/rbac/roles/{roleId}/permissions`)
* **Quyền truy cập**: Admin / `rbac:manage`
* **Request Body**:
```json
{
  "permissionIds": [1, 2, 3]
}
```
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<RoleResponseDto>`

---

#### 5. Cập nhật Vai trò cho Người dùng (`PATCH /api/v1/admin/rbac/users/{userId}/roles`)
* **Quyền truy cập**: Admin / `rbac:manage`
* **Request Body**:
```json
{
  "roleIds": [1, 2]
}
```
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<UserResponseDto>`

---

### MODULE 4: CATEGORY MANAGEMENT (`/api/v1/categories`)

#### 1. Lấy danh sách Danh mục (`GET /api/v1/categories`)
* **Quyền truy cập**: Public
* **Query Parameters**:
  * `search`: Từ khóa tìm kiếm theo tên (VD: `tôm`)
  * `page`: Trang (0-indexed)
  * `size`: Số lượng/trang
  * `sort`: Ví dụ `categoryName,asc`
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Get Category Successfully!",
  "data": {
    "content": [
      { "id": 1, "categoryName": "Hải sản tươi sống" },
      { "id": 2, "categoryName": "Hải sản đông lạnh" }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  }
}
```

---

#### 2. Lấy chi tiết Danh mục (`GET /api/v1/categories/{id}`)
* **Quyền truy cập**: Public
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<CategoryResponseDto>`

---

#### 3. Tạo mới Danh mục (`POST /api/v1/categories`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Request Body**:
```json
{
  "name": "Cua & Ghẹ"
}
```
* **Response Status**: `201 Created`
* **Response Body**: `ApiResponse<CategoryResponseDto>`

---

#### 4. Cập nhật Danh mục (`PUT /api/v1/categories/{id}`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Request Body**:
```json
{
  "name": "Cua, Ghẹ & Tôm Hùm"
}
```
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<CategoryResponseDto>`

---

#### 5. Xóa Danh mục (`DELETE /api/v1/categories/{id}`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Delete Category Successfully!",
  "data": true
}
```

---

### MODULE 5: PRODUCT MANAGEMENT (`/api/v1/products`)

#### 1. Lấy danh sách Sản phẩm (Search & Phân trang) (`GET /api/v1/products`)
* **Quyền truy cập**: Public *(Rate limited theo IP)*
* **Query Parameters**:
  * `search`: Tìm kiếm tên sản phẩm (không phân biệt hoa/thường).
  * `categoryId`: Lọc theo ID danh mục.
  * `page`: Trang hiện tại (bắt đầu từ 0).
  * `size`: Số lượng hiển thị (mặc định 10).
  * `sort`: Sắp xếp (VD: `price,asc` hoặc `createdAt,desc`).
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<PageResponse<ProductResponseDto>>`

```json
{
  "success": true,
  "message": "Get product successfully",
  "data": {
    "content": [
      {
        "id": 10,
        "name": "Tôm Hùm Alaska",
        "price": 1250000.00,
        "stock": 50,
        "description": "Tôm hùm Alaska tươi sống nhập khẩu",
        "imageUrl": "https://res.cloudinary.com/...",
        "active": true,
        "category": {
          "id": 1,
          "categoryName": "Hải sản tươi sống"
        },
        "createdAt": "2026-08-05 14:30:00",
        "updatedAt": "2026-08-05 14:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

#### 2. Lấy chi tiết Sản phẩm (`GET /api/v1/products/{id}`)
* **Quyền truy cập**: Public *(Rate limited theo IP)*
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<ProductResponseDto>`

---

#### 3. Tạo mới Sản phẩm (`POST /api/v1/products`)
* **Quyền truy cập**: Admin / `product:create`
* **Request Body**:
```json
{
  "name": "Cua Cà Mau Kẹp Chặt",
  "description": "Cua thịt Cà Mau tươi ngon loại 1",
  "price": 450000.00,
  "stock": 100,
  "categoryId": 1,
  "isActive": true
}
```
* **Response Status**: `201 Created`
* **Response Body**: `ApiResponse<ProductResponseDto>`

---

#### 4. Cập nhật Sản phẩm (`PATCH /api/v1/products/{id}`)
* **Quyền truy cập**: Admin / `product:update`
* **Request Body**: (Chỉ truyền các trường cần sửa)
```json
{
  "price": 480000.00,
  "stock": 80
}
```
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<ProductResponseDto>`

---

#### 5. Xóa mềm Sản phẩm (`DELETE /api/v1/products/{id}`)
* **Quyền truy cập**: Admin / `product:delete`
* **Mô tả**: Chuyển `isActive = false`, sản phẩm sẽ ẩn khỏi danh sách khách hàng.
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<Boolean>` (data = true).

---

#### 6. Tăng tồn kho Sản phẩm (`PATCH /api/v1/products/increase/{id}`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Query Parameters**: `quantity` (Số lượng tăng, integer)
* **Example URL**: `/api/v1/products/increase/10?quantity=20`
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<ProductResponseDto>`

---

#### 7. Giảm tồn kho Sản phẩm (`PATCH /api/v1/products/decrease/{id}`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Query Parameters**: `quantity` (Số lượng giảm, integer)
* **Example URL**: `/api/v1/products/decrease/10?quantity=5`
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<ProductResponseDto>`

---

#### 8. Upload ảnh Sản phẩm (`POST /api/v1/products/{id}/image`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Content-Type**: `multipart/form-data`
* **Request Form Data**: `file` (File ảnh)
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<ProductResponseDto>`

---

#### 9. Thống kê: Top Sản phẩm bán chạy (`GET /api/v1/products/top-buy`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Query Parameters**: `limit` (mặc định 10)
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Get Top Product Successfully!",
  "data": [
    {
      "name": "Tôm Hùm Alaska",
      "price": 1250000.00,
      "mostBuy": 150
    }
  ]
}
```

---

#### 10. Thống kê: Doanh thu theo Danh mục (`GET /api/v1/products/revenue-by-category`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Get Revenue By Category Success!",
  "data": [
    {
      "name": "Hải sản tươi sống",
      "revenue": 154000000.00
    }
  ]
}
```

---

#### 11. Thống kê: Doanh thu theo Tháng (`GET /api/v1/products/revenue-in-month`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Get Monthly Revenue Success!",
  "data": [
    {
      "month": "2026-08-01T00:00:00",
      "revenue": 45000000.00
    }
  ]
}
```

---

### MODULE 6: ORDER MANAGEMENT (`/api/v1/orders`)

#### 1. Tạo mới Đơn hàng (`POST /api/v1/orders`)
* **Quyền truy cập**: Authenticated User (Lấy UserId tự động từ Bearer Token)
* **Request Body**:
```json
{
  "items": [
    {
      "productId": 10,
      "quantity": 2
    },
    {
      "productId": 12,
      "quantity": 1
    }
  ]
}
```
* **Response Status**: `201 Created`
* **Response Body**:
```json
{
  "success": true,
  "message": "Create Order Successfully!",
  "data": {
    "id": 101,
    "status": "PENDING",
    "totalAmount": 2950000.00,
    "createdAt": "2026-08-10T01:25:00",
    "orderItems": [
      {
        "productName": "Tôm Hùm Alaska",
        "quantity": 2,
        "unitPrice": 1250000.00
      },
      {
        "productName": "Cua Cà Mau Kẹp Chặt",
        "quantity": 1,
        "unitPrice": 450000.00
      }
    ]
  }
}
```

---

#### 2. Lấy đơn hàng theo UserId (`GET /api/v1/orders/user/{userId}`)
* **Quyền truy cập**: User chính chủ hoặc Admin.
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<List<OrderResponseDto>>`

---

#### 3. Xem chi tiết Đơn hàng (`GET /api/v1/orders/{id}`)
* **Quyền truy cập**: User sở hữu đơn hàng đó hoặc Admin.
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<OrderResponseDto>`

---

#### 4. Quản lý: Lấy toàn bộ Đơn hàng (`GET /api/v1/orders`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Query Parameters**: `page`, `size`, `sort`
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<PageResponse<OrderResponseDto>>`

---

#### 5. Cập nhật trạng thái Đơn hàng (`PATCH /api/v1/orders/{id}/status`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Request Body**:
```json
{
  "orderStatus": "CONFIRMED" // CONFIRMED, SHIPPED, DONE, CANCELLED
}
```
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<OrderResponseDto>`

---

### MODULE 7: PAYMENT VNPAY (`/api/v1/payments`)

#### 1. Tạo Link Thanh toán VNPay (`POST /api/v1/payments/{orderId}/create`)
* **Quyền truy cập**: Authenticated User
* **Mô tả**: Tạo thanh toán VNPay cho đơn hàng `orderId`. Backend tự động tính tiền và tạo URL thanh toán điều hướng khách hàng sang cổng VNPay.
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "success": true,
  "message": "Create Payment Successfully!",
  "data": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=295000000&vnp_Command=pay&..."
  }
}
```
* **FE Action**: Khi nhận được `paymentUrl`, FE chuyển hướng trình duyệt (`window.location.href = data.paymentUrl`).

---

#### 2. Callback Kết quả Thanh toán từ VNPay (`GET /api/v1/payments/vnpay-return`)
* **Quyền truy cập**: Public (Được gọi sau khi người dùng thực hiện thanh toán trên cổng VNPay và chuyển hướng về FE).
* **Query Parameters**: Tự động nhận từ tham số VNPay trả về trên URL (`vnp_TxnRef`, `vnp_ResponseCode`, v.v.).
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "status": "SUCCESS", // Hoặc FAILED, PENDING
  "message": "Kiểm tra trạng thái đơn hàng"
}
```

---

#### 3. Webhook IPN từ Server VNPay (`GET /api/v1/payments/vnpay-ipn`)
* **Quyền truy cập**: Public (Dành cho Server VNPay gọi ngầm sang Backend để cập nhật trạng thái đơn hàng).
* **Response Status**: `200 OK`
* **Response Body**:
```json
{
  "RspCode": "00",
  "Message": "Confirm Success"
}
```

---

### MODULE 8: SYSTEM HEALTH & TEST UPLOAD (`/api/v1/health`, `/api/v1/test`)

#### 1. Check Health (`GET /api/v1/health`)
* **Quyền truy cập**: Public
* **Response**: Text `"pong"` (HTTP Status 200).

#### 2. Test Upload File (`POST /api/v1/test/upload`)
* **Quyền truy cập**: Admin (`hasRole('ADMIN')`)
* **Content-Type**: `multipart/form-data` (`file`)
* **Response Status**: `200 OK`
* **Response Body**: `ApiResponse<String>` (Trả về Cloudinary / Storage URL).

---

## III. HƯỚNG DẪN MAPPING VÀ TÍCH HỢP CHO FRONTEND (FE INTEGRATION GUIDE)

### 1. Cấu hình Axios / Fetch Interceptor

#### a) Request Interceptor (Gửi Token & Content-Type):
```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

#### b) Response Interceptor (Auto Refresh Token khi gặp lỗi `401 Unauthorized`):
```typescript
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error('No refresh token');

        const res = await axios.post('http://localhost:8080/api/v1/auth/refresh-token', {
          refreshToken,
        });

        const newAccessToken = res.data.data.accessToken;
        localStorage.setItem('accessToken', newAccessToken);

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);
```

---

### 2. Luồng Xác Thực OTP & Đổi Mật Khẩu (Flow Mapping)

```
[Đăng ký mới] -> Gửi POST /auth/register
              -> Chuyển màn hình OTP
              -> Gửi POST /auth/verify-otp (purpose: "REGISTER_VERIFICATION")
              -> Nhận accessToken & refreshToken -> Đăng nhập thành công!

[Quên mật khẩu] -> Gửi POST /auth/forgot-password (nhận OTP qua email)
                 -> Chuyển màn hình OTP
                 -> Gửi POST /auth/verify-otp (purpose: "RESET_PASSWORD")
                 -> Nhận actionToken từ response
                 -> Chuyển màn hình Mật khẩu mới
                 -> Gửi POST /auth/reset-password (gửi actionToken + newPassword)
```

---

### 3. Hướng Dẫn Mapping API Upload File / Avatar / Product Image

Khi gọi API upload ảnh (Ví dụ Avatar hoặc Ảnh sản phẩm):
* **Lưu ý quan trọng**: KHÔNG set `Content-Type: application/json` thủ công. Hãy để browser tự thêm `multipart/form-data` kèm `boundary`.

```typescript
export const uploadAvatar = async (file: File) => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await api.post('/users/me/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};
```

---

### 4. Hướng Dẫn Phân Trang & Tìm Kiếm (Pagination Mapping)
Đối với danh sách Sản phẩm (`/products`) và Danh mục (`/categories`):
* Chuyển đổi tham số từ UI (Vd: React state) sang Query Param:
  * `page`: Trang hiện tại (Lưu ý Spring Boot bắt đầu từ `0`).
  * `size`: Số bản ghi mỗi trang (Mặc định `10`).
  * `sort`: Cú pháp `<fieldName>,<direction>` (Ví dụ: `price,asc` hoặc `createdAt,desc`).
  * `search`: Giá trị ô input tìm kiếm.

---

## IV. BẢNG TỔNG HỢP QUICK-REFERENCE TOÀN BỘ API

| STT | Module | Method | Endpoint Path | Authen / Role Required | Mô tả tóm tắt |
|---|---|---|---|---|---|
| 1 | Auth | `POST` | `/api/v1/auth/register` | Public | Đăng ký người dùng mới |
| 2 | Auth | `POST` | `/api/v1/auth/login` | Public | Đăng nhập hệ thống |
| 3 | Auth | `POST` | `/api/v1/auth/refresh-token` | Public | Đổi Access Token mới |
| 4 | Auth | `POST` | `/api/v1/auth/logout` | Public / Authen | Đăng xuất |
| 5 | Auth | `POST` | `/api/v1/auth/verify-otp` | Public | Xác thực mã OTP |
| 6 | Auth | `POST` | `/api/v1/auth/resend-otp` | Public | Gửi lại mã OTP |
| 7 | Auth | `POST` | `/api/v1/auth/forgot-password` | Public | Quên mật khẩu |
| 8 | Auth | `POST` | `/api/v1/auth/reset-password` | Public | Đặt lại mật khẩu mới |
| 9 | User | `GET` | `/api/v1/users/me` | Bearer Token | Lấy thông tin user hiện tại |
| 10 | User | `PATCH` | `/api/v1/users/me/update` | Bearer Token | Cập nhật thông tin profile |
| 11 | User | `POST` | `/api/v1/users/me/avatar` | Bearer Token | Upload avatar cá nhân |
| 12 | User | `POST` | `/api/v1/users/change-password` | Bearer Token | Đổi mật khẩu |
| 13 | User | `GET` | `/api/v1/users` | Admin | Quản lý: Lấy danh sách users |
| 14 | User | `PATCH` | `/api/v1/users/{userId}/status` | Admin | Quản lý: Khóa / Mở tài khoản |
| 15 | RBAC | `GET` | `/api/v1/admin/rbac/roles` | Admin / rbac:manage | Lấy danh sách vai trò |
| 16 | RBAC | `POST` | `/api/v1/admin/rbac/roles` | Admin / rbac:manage | Tạo vai trò mới |
| 17 | RBAC | `GET` | `/api/v1/admin/rbac/permissions` | Admin / rbac:manage | Lấy danh sách quyền |
| 18 | RBAC | `PATCH` | `/api/v1/admin/rbac/roles/{roleId}/permissions` | Admin / rbac:manage | Cập nhật quyền cho vai trò |
| 19 | RBAC | `PATCH` | `/api/v1/admin/rbac/users/{userId}/roles` | Admin / rbac:manage | Gán vai trò cho người dùng |
| 20 | Category | `GET` | `/api/v1/categories` | Public | Lấy danh sách danh mục |
| 21 | Category | `GET` | `/api/v1/categories/{id}` | Public | Lấy chi tiết danh mục |
| 22 | Category | `POST` | `/api/v1/categories` | Admin | Tạo danh mục mới |
| 23 | Category | `PUT` | `/api/v1/categories/{id}` | Admin | Cập nhật danh mục |
| 24 | Category | `DELETE` | `/api/v1/categories/{id}` | Admin | Xóa danh mục |
| 25 | Product | `GET` | `/api/v1/products` | Public | Danh sách SP (search, filter) |
| 26 | Product | `GET` | `/api/v1/products/{id}` | Public | Chi tiết sản phẩm |
| 27 | Product | `POST` | `/api/v1/products` | Admin / product:create | Tạo mới sản phẩm |
| 28 | Product | `PATCH` | `/api/v1/products/{id}` | Admin / product:update | Cập nhật sản phẩm |
| 29 | Product | `DELETE` | `/api/v1/products/{id}` | Admin / product:delete | Xóa mềm sản phẩm |
| 30 | Product | `PATCH` | `/api/v1/products/increase/{id}` | Admin | Tăng số lượng kho |
| 31 | Product | `PATCH` | `/api/v1/products/decrease/{id}` | Admin | Giảm số lượng kho |
| 32 | Product | `POST` | `/api/v1/products/{id}/image` | Admin | Upload ảnh sản phẩm |
| 33 | Product | `GET` | `/api/v1/products/top-buy` | Admin | Thống kê Top SP bán chạy |
| 34 | Product | `GET` | `/api/v1/products/revenue-by-category` | Admin | Thống kê doanh thu danh mục |
| 35 | Product | `GET` | `/api/v1/products/revenue-in-month` | Admin | Thống kê doanh thu theo tháng |
| 36 | Order | `POST` | `/api/v1/orders` | Bearer Token | Tạo đơn hàng mới |
| 37 | Order | `GET` | `/api/v1/orders/user/{userId}` | User chính chủ / Admin | Xem đơn hàng theo User |
| 38 | Order | `GET` | `/api/v1/orders/{id}` | User chính chủ / Admin | Xem chi tiết đơn hàng |
| 39 | Order | `GET` | `/api/v1/orders` | Admin | Quản lý: Lấy toàn bộ đơn hàng |
| 40 | Order | `PATCH` | `/api/v1/orders/{id}/status` | Admin | Cập nhật trạng thái đơn hàng |
| 41 | Payment | `POST` | `/api/v1/payments/{orderId}/create` | Bearer Token | Tạo URL thanh toán VNPay |
| 42 | Payment | `GET` | `/api/v1/payments/vnpay-return` | Public | VNPay return callback URL |
| 43 | Payment | `GET` | `/api/v1/payments/vnpay-ipn` | Public | VNPay IPN Webhook |
| 44 | Health | `GET` | `/api/v1/health` | Public | Ping/Health check |
| 45 | Test | `POST` | `/api/v1/test/upload` | Admin | Test upload file |
