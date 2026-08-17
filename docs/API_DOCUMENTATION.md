# 📘 HƯỚNG DẪN TÍCH HỢP & MAPPING API TOÀN BỘ DỰ ÁN (FOR FRONTEND FE)

> **Dành cho**: Đội ngũ phát triển Frontend (ReactJS / Next.js / Vue.js / Mobile App)  
> **Phiên bản Backend**: `v1.2.0` | **Base URL**: `http://localhost:8085` (Cấu hình qua `NEXT_PUBLIC_API_URL` hoặc `VITE_API_URL`)  
> **Độ bao phủ**: 100% Endpoints thực tế thuộc **16 Controllers** Backend.

---

## 1. 📌 QUY CHUẨN KỸ THUẬT CHUNG & BẢO MẬT

### 1.1 Headers Chuẩn Mỗi Request
- **Mặc định Request Body JSON**: `Content-Type: application/json`
- **Upload File**: `Content-Type: multipart/form-data`
- **Xác thực JWT**: `Authorization: Bearer <accessToken>` (Bắt buộc với các API có yêu cầu đăng nhập).

---

### 1.2 Cấu Trúc Khung Trả Về Chuẩn (`ApiResponse<T>`)

Mọi API Response từ Backend (Dù thành công hay thất bại) đều được bọc trong khung JSON duy nhất:

#### ✅ Response Thành Công (`HTTP 200 OK` / `201 Created`):
```json
{
  "success": true,
  "message": "Mô tả thông điệp thành công",
  "data": { ... }, // Dữ liệu trả về (Object, Array, hoặc PageResponse)
  "timestamp": "2026-08-13T23:45:00.123456"
}
```

#### ❌ Response Thất Bại / Lỗi Hệ Thống (`HTTP 401` / `403` / `404` / `409` / `500`):
```json
{
  "success": false,
  "message": "Mô tả nguyên nhân lỗi chi tiết từ Server",
  "data": null,
  "timestamp": "2026-08-13T23:45:00.123456"
}
```

#### ⚠️ Response Lỗi Validation Dữ Liệu Form (`HTTP 400 Bad Request`):
Khi gửi Request Body vi phạm Validation (ví dụ: vi phạm `@NotBlank`, `@Email`, `@Min`), thuộc tính `data` sẽ chứa Map các trường bị lỗi:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email format is invalid",
    "password": "Password is min 8 and max 100 character!"
  },
  "timestamp": "2026-08-13T23:45:00"
}
```

---

### 1.3 Cấu Trúc Phân Trang Chuẩn (`PageResponse<T>`)

Xuất hiện ở thuộc tính `data` của các API trả về danh sách phân trang (Sản phẩm, Đơn hàng, Người dùng):

```json
{
  "success": true,
  "message": "Get products successfully",
  "data": {
    "content": [ ... ],       // Danh sách phần tử của trang hiện tại
    "page": 0,               // Trang hiện tại (0-indexed)
    "size": 10,              // Kích thước trang
    "totalElements": 45,     // Tổng số bản ghi trong DB
    "totalPages": 5,         // Tổng số trang
    "last": false            // Đã đến trang cuối chưa
  },
  "timestamp": "2026-08-13T23:45:00"
}
```

---

### 1.4 Luồng Tự Động Refresh Token (Axios Interceptor Lỗi 401)
1. Lưu `accessToken` trong Memory / State và `refreshToken` trong Storage / Cookie.
2. Khi gọi API bị trả về `401 Unauthorized`:
   - Bắt lỗi qua Interceptor và gọi `POST /api/v1/auth/refresh-token` với `{ "refreshToken": "..." }`.
   - Nhận về `accessToken` mới (và `refreshToken` mới), cập nhật lại Storage.
   - Thử lại (retry) Request ban đầu bị thất bại.

---

### 1.5 Quy Trình Xác Thực OTP (OTP Flow for FE)
- **Khi Đăng ký (`REGISTER_VERIFICATION`)**:
  1. Người dùng Đăng ký thành công (`POST /api/v1/auth/register`).
  2. Mã OTP 6 chữ số tự động gửi tới Email.
  3. Người dùng nhập mã OTP (`POST /api/v1/auth/verify-otp` với `purpose: "REGISTER_VERIFICATION"`).
  4. Response trả về ngay `accessToken` & `refreshToken` $\rightarrow$ FE tự động đăng nhập người dùng mà không cần bắt đăng nhập lại.
- **Khi Quên Mật Khẩu (`RESET_PASSWORD`)**:
  1. Gửi yêu cầu quên mật khẩu (`POST /api/v1/auth/forgot-password`).
  2. Người dùng nhập OTP xác nhận (`POST /api/v1/auth/verify-otp` với `purpose: "RESET_PASSWORD"`).
  3. Response trả về `actionToken`.
  4. FE dùng `actionToken` này gửi kèm mật khẩu mới tới `POST /api/v1/auth/reset-password`.

---

### 1.6 Điều Kiện Bắt Bắt Tạo Đơn Hàng (Checkout Requirement)
> ⚠️ **LƯU Ý QUAN TRỌNG KHI TẠO ĐƠN**: Khi gọi `POST /api/v1/orders`, backend sẽ lấy **Địa chỉ mặc định (`defaultAddress = true`)** của người dùng hiện tại để lưu Snapshot cho đơn hàng. Nếu tài khoản người dùng chưa có địa chỉ mặc định nào, backend sẽ trả lỗi `404 - Address not found!`. FE cần nhắc người dùng thêm/thiết lập địa chỉ mặc định trước khi Checkout.

---

## 2. 🔐 MODULE 1: AUTHENTICATION & SECURITY (`/api/v1/auth`)

### 1. Đăng Ký Tài Khoản (Register)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/register`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "fullName": "Nguyễn Văn A",
    "email": "user@example.com",
    "phoneNumber": "0987654321",
    "password": "Password123@"
  }
  ```
- **Response (201 Created)**: `ApiResponse<UserResponseDto>`

---

### 2. Đăng Nhập (Login)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/login`
- **Security**: Public (Áp dụng Rate Limit Login)
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "Password123@"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Login Successfully!",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
      "refreshToken": "7f9a8b1c-3d2e-4f5a-6b7c-8d9e0f1a2b3c",
      "tokenType": "Bearer",
      "expiresIn": 86400,
      "user": {
        "id": 1,
        "fullName": "Nguyễn Văn A",
        "email": "user@example.com",
        "roles": ["ROLE_CUSTOMER"]
      }
    }
  }
  ```

---

### 3. Làm Mới Token (Refresh Token)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/refresh-token`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "refreshToken": "7f9a8b1c-3d2e-4f5a-6b7c-8d9e0f1a2b3c"
  }
  ```
- **Response (200 OK)**: Trả về `accessToken` và `refreshToken` mới.

---

### 4. Đăng Xuất (Logout)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/logout`
- **Security**: Public / Authenticated
- **Request Body**:
  ```json
  {
    "refreshToken": "7f9a8b1c-3d2e-4f5a-6b7c-8d9e0f1a2b3c"
  }
  ```
- **Response (200 OK)**: Xóa Refresh Token trong hệ thống.

---

### 5. Xác Thực Mã OTP (Verify OTP)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/verify-otp`
- **Security**: Public (Rate Limit OTP)
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "otpCode": "123456",
    "purpose": "REGISTER_VERIFICATION" // Hoặc "RESET_PASSWORD"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Verify Otp Successfully!",
    "data": {
      "accessToken": "...",   // Trả về nếu REGISTER_VERIFICATION
      "refreshToken": "...",  // Trả về nếu REGISTER_VERIFICATION
      "actionToken": "..."    // Trả về nếu RESET_PASSWORD
    }
  }
  ```

---

### 6. Quên Mật Khẩu (Forgot Password)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/forgot-password`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "email": "user@example.com"
  }
  ```
- **Response (200 OK)**: Gửi mã OTP khôi phục mật khẩu qua Email.

---

### 7. Gửi Lại Mã OTP (Resend OTP)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/resend-otp`
- **Security**: Public (Rate Limit OTP)
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "purpose": "REGISTER_VERIFICATION" // Hoặc "RESET_PASSWORD"
  }
  ```

---

### 8. Đặt Lại Mật Khẩu (Reset Password)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/reset-password`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "actionToken": "action_token_tu_verify_otp",
    "newPassword": "NewPassword123@"
  }
  ```

---

## 3. 👤 MODULE 2: USER PROFILE & ADDRESS MANAGEMENT

### 3.1 Profile Người Dùng (`/api/v1/users`)

#### 1. Lấy Thông Tin Cá Nhân Hiện Tại
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/users/me`
- **Security**: Bearer Token
- **Response (200 OK)**: Trả về chi tiết `UserResponseDto` (id, fullName, email, phoneNumber, avatarUrl, isActive, roles).

#### 2. Cập Nhật Thông Tin Profile
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/users/update-profile`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "fullName": "Nguyễn Văn B",
    "phoneNumber": "0912345678"
  }
  ```

#### 3. Upload Ảnh Đại Diện (Avatar)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/users/upload-avatar`
- **Content-Type**: `multipart/form-data`
- **Form Data**: `file` (File hình ảnh PNG/JPG/WEBP).

#### 4. Đổi Mật Khẩu
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/users/change-password`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "oldPassword": "OldPassword123@",
    "newPassword": "NewPassword123@"
  }
  ```

---

### 3.2 Sổ Địa Chỉ Giao Hàng (`/api/v1/addresses`)

#### 1. Danh Sách Địa Chỉ Cá Nhân
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/addresses/me`
- **Security**: Bearer Token
- **Response (200 OK)**: `ApiResponse<List<AddressResponseDto>>`

#### 2. Thêm Địa Chỉ Mới
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/addresses`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "recipientName": "Nguyễn Văn A",
    "phone": "0987654321",
    "province": "Thành phố Hồ Chí Minh",
    "district": "Quận 1",
    "ward": "Phường Bến Nghé",
    "addressDetail": "123 Đường Lê Lợi",
    "defaultAddress": true,
    "tag": "Nhà Riêng" // "Nhà Riêng" | "Văn Phòng"
  }
  ```

#### 3. Cập Nhật Địa Chỉ
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/addresses/{id}`
- **Security**: Bearer Token
- **Request Body**: Tương tự như DTO tạo mới địa chỉ.

#### 4. Xóa Địa Chỉ
- **HTTP Method**: `DELETE` | **Endpoint**: `/api/v1/addresses/delete/{id}`
- **Security**: Bearer Token

#### 5. Đặt Địa Chỉ Mặc Định
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/addresses/change-address-default/{id}`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "defaultAddress": true
  }
  ```

---

### 3.3 Quản Trị Người Dùng & RBAC Admin (`/api/v1/admin/users` & `/api/v1/admin/rbac`)

#### 1. Danh Sách Người Dùng (Admin)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/users?page=0&size=10&sort=createdAt,desc`
- **Security**: Admin (`ROLE_ADMIN`)

#### 2. Khóa / Kích Hoạt Tài Khoản
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/users/{id}/status?isActive=false`
- **Security**: Admin (`ROLE_ADMIN`)

#### 3. Lấy Danh Sách Roles
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/rbac/roles`
- **Security**: `rbac:manage` hoặc `ROLE_ADMIN`

#### 4. Tạo Role Mới
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/rbac/roles`
- **Request Body**: `{ "name": "ROLE_STAFF", "description": "Nhân viên hỗ trợ" }`

#### 5. Lấy Danh Sách Permissions
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/rbac/permissions`

#### 6. Cập Nhật Permission Cho Role
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/rbac/roles/{roleId}/permissions`
- **Request Body**: `{ "permissionIds": [1, 2, 3] }`

#### 7. Gán Role Cho Người Dùng
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/rbac/users/{userId}/roles`
- **Request Body**: `{ "roleIds": [1, 2] }`

---

## 4. 🏷️ MODULE 3: CATEGORIES (`/api/v1/categories` & `/api/v1/admin/categories`)

### 4.1 Public APIs

#### 1. Lấy Danh Sách Danh Mục Đang Hoạt Động
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/categories`
- **Security**: Public

#### 2. Chi Tiết Danh Mục Theo ID
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/categories/{id}`
- **Security**: Public

---

### 4.2 Admin Management APIs

#### 1. Tạo Danh Mục Mới
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/categories`
- **Request Body**:
  ```json
  {
    "name": "Tôm & Cua",
    "description": "Các loại tôm cua tươi sống",
    "active": true
  }
  ```

#### 2. Cập Nhật Danh Mục
- **HTTP Method**: `PUT` | **Endpoint**: `/api/v1/admin/categories/{id}`

#### 3. Xóa Danh Mục
- **HTTP Method**: `DELETE` | **Endpoint**: `/api/v1/admin/categories/{id}`

#### 4. Upload Ảnh Danh Mục
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/categories/{id}/image`
- **Content-Type**: `multipart/form-data` | Form Field: `file`

#### 5. Cấu Hình Hiển Thị Bento / Trang Chủ Cho Danh Mục
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/categories/{id}/home-config`
- **Request Body**:
  ```json
  {
    "badge": "TOP 1",
    "badgeType": "hot", // "hot" | "new" | "sale"
    "iconName": "utensils",
    "homeDisplayStyle": "main", // "main" | "sub" | "banner"
    "sortOrder": 1
  }
  ```

---

## 5. 📦 MODULE 4: PRODUCTS & SEARCH ENGINE (`/api/v1/products` & `/api/v1/admin/products`)

### 5.1 Public APIs

#### 1. Tìm Kiếm & Lọc Động Sản Phẩm (Dynamic Criteria Search)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/products`
- **Security**: Public (Rate Limit Public API)
- **Query Parameters**:
  - `search` *(String, optional)*: Từ khóa tìm kiếm theo tên.
  - `categoryId` *(List<Long>, optional)*: Lọc theo 1 hoặc nhiều ID danh mục (`?categoryId=1&categoryId=2`).
  - `minPrice` *(BigDecimal, optional)*: Giá tối thiểu.
  - `maxPrice` *(BigDecimal, optional)*: Giá tối đa.
  - `inStock` *(Boolean, optional)*: Lọc sản phẩm còn hàng (`stock > 0`).
  - `page` *(Integer, default 0)*: Trang số.
  - `size` *(Integer, default 10)*: Số lượng/trang.
  - `sort` *(String, default "createdAt,desc")*: Sắp xếp (`price,asc`, `price,desc`, `name,asc`, `createdAt,desc`).
- **Response (200 OK)**: `ApiResponse<PageResponse<ProductResponseDto>>`

#### 2. Chi Tiết Sản Phẩm
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/products/{id}`
- **Security**: Public

---

### 5.2 Admin Product Management & Dashboard Analytics

#### 1. Tạo Sản Phẩm Mới
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/products`
- **Security**: Permission `product:create`
- **Request Body**:
  ```json
  {
    "name": "Cua Gạch Cà Mau Premium",
    "description": "Cua thịt chắc, ngọt, nhiều gạch...",
    "price": 450000,
    "stock": 50,
    "categoryId": 1,
    "spec": "Size 2-3 con/kg",
    "origin": "Cà Mau, Việt Nam",
    "weightOptions": ["1kg", "2kg", "5kg"]
  }
  ```

#### 2. Cập Nhật Sản Phẩm
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/products/{id}`
- **Security**: Permission `product:update`

#### 3. Xóa Mềm Sản Phẩm (Soft Delete)
- **HTTP Method**: `DELETE` | **Endpoint**: `/api/v1/admin/products/{id}`
- **Security**: Permission `product:delete`

#### 4. Tăng / Giảm Tồn Kho
- `PATCH /api/v1/admin/products/increase/{id}?quantity=10`
- `PATCH /api/v1/admin/products/decrease/{id}?quantity=5`

#### 5. Upload Ảnh Sản Phẩm
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/products/{id}/image`
- **Content-Type**: `multipart/form-data` | Form Field: `file`

#### 6. Bật / Tắt Cờ Nổi Bật (Featured Product)
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/products/{id}/featured`

#### 7. Cấu Hình Gói Combo Nổi Bật Trang Chủ
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/products/{id}/combo-config`
- **Request Body**:
  ```json
  {
    "isCombo": true,
    "comboBadge": "COMBO TIẾC",
    "comboTitle": "Set Hải Sản Hoàng Gia",
    "comboSubtitle": "Dành cho 4-6 người ăn",
    "comboOriginalPrice": 1200000,
    "comboSaveText": "Tiết kiệm 200k",
    "comboItems": ["1kg Cua Gạch", "500g Tôm Hùm", "1kg Sò Dương"]
  }
  ```

#### 8. Thống Kê Dashboard Admin
- `GET /api/v1/admin/products/top-buy?limit=10`: Top sản phẩm bán chạy nhất.
- `GET /api/v1/admin/products/revenue-by-category`: Doanh thu phân chia theo danh mục.
- `GET /api/v1/admin/products/revenue-in-month`: Doanh thu tổng hợp theo tháng.

---

## 6. 🏠 MODULE 5: HOME PAGE CONTENT MANAGEMENT (`/api/v1/home`)

### 1. Lấy Toàn Bộ Dữ Liệu Trang Chủ Aggregated (Public API)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/home`
- **Security**: Public
- **Mô tả**: API gộp trả về **8 khối dữ liệu hoàn chỉnh** cho Trang chủ (Được lưu Cache Redis tối ưu tốc độ < 20ms):
  1. `heroSlides`: Danh sách banner chính trình chiếu.
  2. `categories`: Danh mục hiển thị dạng Bento Grid.
  3. `dailyArrivals`: Hải sản tươi mới cập bến trong ngày.
  4. `featuredProducts`: Sản phẩm nổi bật.
  5. `featuredProductTabs`: Danh sách tabs phân loại sản phẩm trang chủ.
  6. `comboSets`: Danh sách các gói Combo ưu đãi.
  7. `featuredReviews`: Đánh giá của khách hàng.
  8. `stats`: Thống kê tổng số đơn delivered, rating trung bình.

---

### 2. Admin Quản Lý Nội Dung Trang Chủ & Redis Cache

#### A. Xóa Clean Cache Trang Chủ (Evict Cache)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/home/cache/evict`
- **Security**: Admin (`ROLE_ADMIN`)

#### B. Quản Lý Hero Banners (`/api/v1/admin/hero-banners`)
- `GET /api/v1/admin/hero-banners`: Lấy tất cả banner slides.
- `POST /api/v1/admin/hero-banners`: Tạo banner slide mới.
- `PATCH /api/v1/admin/hero-banners/{id}`: Cập nhật banner slide.
- `DELETE /api/v1/admin/hero-banners/{id}`: Xóa banner slide.
- `PATCH /api/v1/admin/hero-banners/{id}/toggle`: Bật/Tắt trạng thái hiển thị banner.
- `POST /api/v1/admin/hero-banners/{id}/image`: Upload ảnh banner (Multipart `file`).

#### C. Quản Lý Hải Sản Cập Bến Ngày (`/api/v1/admin/daily-arrivals`)
- `GET /api/v1/admin/daily-arrivals?date=YYYY-MM-DD`: Danh sách hải sản cập bến theo ngày.
- `POST /api/v1/admin/daily-arrivals`: Thêm sản phẩm vào danh sách cập bến (`productId`, `date`, `badge`, `title`, `description`...).
- `PATCH /api/v1/admin/daily-arrivals/{id}`: Cập nhật thông tin cập bến.
- `DELETE /api/v1/admin/daily-arrivals/{id}`: Xóa khỏi danh sách cập bến.

---

## 7. 🛒 MODULE 6: ORDERS & CHECKOUT (`/api/v1/orders` & `/api/v1/admin/orders`)

### 7.1 Customer Order APIs

#### 1. Tạo Đơn Hàng Mới (Checkout)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/orders`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "paymentMethod": "VNPAY", // "COD" | "VNPAY" | "MOMO" | "ZALOPAY"
    "note": "Giao hàng giờ hành chính, gọi trước 15 phút",
    "items": [
      { "productId": 10, "quantity": 2 },
      { "productId": 15, "quantity": 1 }
    ]
  }
  ```
- **Response (201 Created)**: Trả về thông tin đơn hàng với `status: "PENDING"`.

#### 2. Danh Sách Đơn Hàng Của Tôi
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/orders/my-orders?page=0&size=10`
- **Security**: Bearer Token

#### 3. Chi Tiết Đơn Hàng
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/orders/{id}`
- **Security**: Bearer Token (Kiểm tra quyền sở hữu đơn hàng).

#### 4. Hủy Đơn Hàng
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/orders/{id}/cancel-order`
- **Security**: Bearer Token

#### 5. Lọc Đơn Hàng Cá Nhân Theo Status
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/orders/me/{userId}?status=PENDING&page=0&size=10`

---

### 7.2 Admin Order Management

#### 1. Lấy Toàn Bộ Đơn Hàng Hệ Thống
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/orders?page=0&size=10`

#### 2. Lọc Đơn Hàng Theo User / Status
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/orders/{userId}?status=CONFIRMED`

#### 3. Cập Nhật Trạng Thái Đơn Hàng
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/orders/{id}/update-status`
- **Request Body**:
  ```json
  {
    "status": "CONFIRMED" // Trạng thái hợp lệ: PENDING -> CONFIRMED -> SHIPPED -> DONE (hoặc CANCELLED)
  }
  ```

---

## 8. 💳 MODULE 7: PAYMENT PROCESSING & VNPAY (`/api/v1/payments`)

### 1. Tạo Đường Dẫn Thanh Toán VNPay
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/payments/{orderId}/create`
- **Security**: Bearer Token
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Create Payment Successfully!",
    "data": {
      "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=..."
    }
  }
  ```
- **FE Integration**: Redirect người dùng trực tiếp sang URL `paymentUrl` để thanh toán qua cổng VNPay.

---

### 2. VNPay Return Callback Redirect (Trang Kết Quả)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/payments/vnpay-return`
- **Mô tả**: Sau khi khách hoàn tất thanh toán trên VNPay, VNPay redirect về endpoint này. Backend kiểm tra chữ ký checksum và tự động 302 Redirect trình duyệt của khách hàng về trang Frontend:
  `http://localhost:3000/payment-result?paymentId=...&status=SUCCESS&orderId=...`
- **FE Integration**: Xây dựng Route `/payment-result` trên Frontend để đọc Query Parameters và hiển thị giao diện "Thanh toán thành công" hoặc "Thanh toán thất bại".

---

### 3. VNPay IPN Webhook
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/payments/vnpay-ipn`
- **Mô tả**: Webhook ngầm nhận thông báo từ server VNPay. FE không gọi API này.

---

## 9. ⚠️ MODULE 8: ERROR HANDLING & HTTP STATUS CODES

### Bảng Mã Lỗi & Cách Hướng Dẫn Xử Lý Tại Frontend:

| Code / Exception | HTTP Status | Nguyên Nhân | Cách Xử Lý Chi Tiết Ở Frontend |
|---|:---:|---|---|
| `VALIDATION_ERROR` | `400 Bad Request` | Dữ liệu Form không đúng định dạng. | Hiển thị lỗi đỏ dưới từng ô Input tương ứng với key trong `data`. |
| `UNAUTHORIZED` | `401 Unauthorized` | Token hết hạn / Chưa đăng nhập. | Tự động kích hoạt Refresh Token Interceptor hoặc redirect về `/login`. |
| `FORBIDDEN` | `403 Forbidden` | Tài khoản không có quyền truy cập. | Hiển thị Toast Notification: "Bạn không có quyền thực hiện thao tác này". |
| `RESOURCE_NOT_FOUND` | `404 Not Found` | Không tìm thấy ID / Thiếu địa chỉ mặc định. | Nếu thiếu địa chỉ: Nhắc chuyển sang trang Thêm Địa Chỉ. Nếu ID lỗi: Trang 404. |
| `CONFLICT_ERROR` | `409 Conflict` | Email / Phone đã tồn tại trong DB. | Thông báo Email/Số điện thoại đã được đăng ký. |
| `INSUFFICIENT_STOCK` | `400 Bad Request` | Số lượng tồn kho sản phẩm không đủ. | Thông báo sản phẩm đã hết hàng hoặc giảm số lượng trong giỏ. |
| `TOO_MANY_REQUESTS` | `429 Too Many Requests` | Gửi request quá giới hạn Rate Limit. | Khóa tạm thời nút bấm và hiển thị đếm ngược 60 giây. |
| `INTERNAL_SERVER_ERROR`| `500 Server Error` | Lỗi không mong muốn từ Backend. | Hiển thị thông báo "Hệ thống đang bảo trì, xin thử lại sau". |

