# 🛡️ HƯỚNG DẪN TÍCH HỢP API — PHÍA ADMIN PORTAL & DASHBOARD

> **Dành cho**: Đội ngũ phát triển Frontend Admin Dashboard / Backoffice Portal (React Admin, Next.js Admin, Vue Admin)  
> **Phiên bản Backend**: `v1.2.0` | **Base URL**: `http://localhost:8085` (Cấu hình qua `NEXT_PUBLIC_API_URL` hoặc `VITE_API_URL`)  
> **Mục tiêu**: Hướng dẫn toàn bộ 100% các API quản trị, cơ chế bảo vệ RBAC, phân quyền Role & Permission, và luồng đăng nhập Admin.

---

## 1. 📌 QUY CHUẨN KỸ THUẬT & BẢO MẬT PHÍA ADMIN

### 1.1 Headers Bắt Buộc Mỗi Request Quản Trị
- **Mặc định Body JSON**: `Content-Type: application/json`
- **Upload File Ảnh (Sản phẩm, Banner, Danh mục)**: `Content-Type: multipart/form-data`
- **Xác thực Admin JWT (BẮT BUỘC)**: `Authorization: Bearer <accessToken>`

---

### 1.2 Cấu Trúc Khung Trả Về Chuẩn (`ApiResponse<T>`)

#### ✅ Response Thành Công (`HTTP 200 OK` / `201 Created`):
```json
{
  "success": true,
  "message": "Thao tác quản trị thành công",
  "data": { ... },
  "timestamp": "2026-08-19T16:00:00.123456"
}
```

#### ❌ Response Thất Bại / Không Đủ Quyền (`HTTP 401 Unauthorized` / `HTTP 403 Forbidden`):
```json
{
  "success": false,
  "message": "Access Denied: You do not have permission to access this resource",
  "data": null,
  "timestamp": "2026-08-19T16:00:00.123456"
}
```

---

### 1.3 Cơ Chế Phân Quyền RBAC (Role-Based Access Control)
Mọi tài khoản đăng nhập vào Admin Portal đều có danh sách `roles` và `permissions` đi kèm trong token:
- **Vai trò cấp cao nhất (`ROLE_ADMIN`)**: Cho phép truy cập tất cả các endpoint có tiền tố `/api/v1/admin/**`.
- **Mã quyền chi tiết (Fine-grained Permissions)**:
  - `product:create`: Quyền tạo mới sản phẩm.
  - `product:update`: Quyền chỉnh sửa thông tin sản phẩm.
  - `product:delete`: Quyền xóa mềm sản phẩm.
  - `rbac:manage`: Quyền quản lý Roles, gán Permissions và phân quyền người dùng.

---

## 2. 🔐 MODULE 1: AUTHENTICATION PHÍA ADMIN PORTAL

> 🚨 **LƯU Ý QUAN TRỌNG VỀ AUTH ADMIN**:  
> - **KHÔNG CÓ LUỒNG ĐĂNG KÝ CÔNG KHAI (No Public Register)**: Phía Admin không có chức năng tự đăng ký để tránh rò rỉ và leo thang đặc quyền.
> - Tài khoản Admin được cấp sẵn thông qua Database Seed hoặc do Super Admin khởi tạo trực tiếp.

---

### 2.1 Luồng Xác Thực Đăng Nhập & Bảo Vệ Router Trên Frontend Admin
```
[1. Form Login Admin]
POST /api/v1/auth/login { email, password }
  │
  ├──> Sai mật khẩu / không tồn tại ──> 401 Unauthorized
  │
  └──> Thành công (200 OK) ──> Nhận accessToken & refreshToken
         │
         ▼
[2. Kiểm tra quyền hạn trên Frontend Router Guard]
Gọi GET /api/v1/users/me (với Bearer Token)
  │
  ├──> roles KHÔNG chứa 'ROLE_ADMIN' ──> Đăng xuất ngay + Báo lỗi 403 Forbidden
  │
  └──> roles CÓ 'ROLE_ADMIN' ──> Lưu token vào Storage + Cho phép truy cập Dashboard
```

---

### 2.2 Chi Tiết Các API Auth Admin

#### 1. Đăng Nhập Quản Trị Viên (Admin Login)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/login`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "email": "huuphat263@gmail.com",
    "password": "Admin@123"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Login Successfully!",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "7f9a8b1c-3d2e-4f5a-6b7c-8d9e0f1a2b3c",
      "tokenType": "Bearer",
      "expiresIn": 86400000
    },
    "timestamp": "2026-08-19T16:00:00"
  }
  ```

---

#### 2. Lấy Thông Tin Admin Đang Đăng Nhập
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/users/me`
- **Security**: Bearer Token
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Get profile successfully",
    "data": {
      "id": 1,
      "fullName": "Admin Huu Phat",
      "email": "huuphat263@gmail.com",
      "phoneNumber": "0929292423",
      "avatarUrl": null,
      "active": true,
      "emailVerified": true,
      "roles": ["ROLE_ADMIN"]
    }
  }
  ```

---

#### 3. Làm Mới Token (Refresh Token)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/refresh-token`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "refreshToken": "7f9a8b1c-3d2e-4f5a-6b7c-8d9e0f1a2b3c"
  }
  ```
- **Response (200 OK)**: Trả về `accessToken` mới.

---

#### 4. Đăng Xuất (Logout Admin)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/logout`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "refreshToken": "7f9a8b1c-3d2e-4f5a-6b7c-8d9e0f1a2b3c"
  }
  ```
- **Response (200 OK)**: Hủy bỏ Refresh Token.

---

#### 5. Đổi Mật Khẩu Admin
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/users/change-password`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "oldPassword": "Admin@123",
    "newPassword": "NewAdminPassword123@"
  }
  ```

---

## 3. 👥 MODULE 2: QUẢN LÝ NGƯỜI DÙNG & PHÂN QUYỀN RBAC

### 3.1 Quản Lý Tài Khoản Người Dùng (`/api/v1/admin/users`)

#### 1. Lấy Danh Sách Người Dùng (Phân trang & Sắp xếp)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/users`
- **Security**: `ROLE_ADMIN`
- **Query Parameters**:
  - `page` *(Integer, default 0)*: Số trang.
  - `size` *(Integer, default 10)*: Số lượng user / trang.
  - `sort` *(String, default "createdAt,desc")*: Quy tắc sắp xếp.
- **Response (200 OK)**: `ApiResponse<PageResponse<UserResponseDto>>`

---

#### 2. Khóa / Mở Khóa Tài Khoản Người Dùng
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/users/{id}/status`
- **Security**: `ROLE_ADMIN`
- **Query Parameter**: `isActive` (`true` = Kích hoạt, `false` = Khóa tài khoản).
- **Lưu ý Backend**: Admin không được phép tự khóa chính tài khoản của mình đang đăng nhập.
- **Response (204 No Content)**

---

### 3.2 Quản Lý Roles & Permissions RBAC (`/api/v1/admin/rbac`)

#### 1. Lấy Danh Sách Tất Cả Roles
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/rbac/roles`
- **Security**: `rbac:manage` hoặc `ROLE_ADMIN`
- **Response (200 OK)**: `ApiResponse<List<RoleResponseDto>>`
  ```json
  {
    "success": true,
    "message": "Get all roles successfully",
    "data": [
      {
        "id": 1,
        "name": "ROLE_ADMIN",
        "description": "Quản trị viên toàn quyền",
        "permissions": [
          { "id": 1, "code": "product:create", "description": "Tạo sản phẩm" },
          { "id": 2, "code": "product:update", "description": "Sửa sản phẩm" }
        ]
      }
    ]
  }
  ```

---

#### 2. Tạo Role Mới
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/rbac/roles`
- **Security**: `rbac:manage` hoặc `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "name": "ROLE_MODERATOR",
    "description": "Kiểm duyệt viên nội dung"
  }
  ```

---

#### 3. Lấy Danh Sách Toàn Bộ Permissions Hệ Thống
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/rbac/permissions`
- **Security**: `rbac:manage` hoặc `ROLE_ADMIN`
- **Response (200 OK)**: `ApiResponse<List<PermissionResponseDto>>`

---

#### 4. Cập Nhật Danh Sách Permissions Cho 1 Role
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/rbac/roles/{roleId}/permissions`
- **Security**: `rbac:manage` hoặc `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "permissionIds": [1, 2, 3, 5, 8]
  }
  ```

---

#### 5. Gán Danh Sách Roles Cho Người Dùng
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/rbac/users/{userId}/roles`
- **Security**: `rbac:manage` hoặc `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "roleIds": [1, 2]
  }
  ```

---

## 4. 🏷️ MODULE 3: QUẢN LÝ DANH MỤC SẢN PHẨM (`/api/v1/admin/categories`)

#### 1. Tạo Danh Mục Mới
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/categories`
- **Security**: `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "name": "Tôm & Cua Hoàng Gia",
    "description": "Các loại tôm cua hải sản tươi sống cao cấp",
    "active": true
  }
  ```
- **Response (201 Created)**: `ApiResponse<CategoryResponseDto>`

---

#### 2. Cập Nhật Thông Tin Danh Mục
- **HTTP Method**: `PUT` | **Endpoint**: `/api/v1/admin/categories/{id}`
- **Security**: `ROLE_ADMIN`
- **Request Body**: Tương tự như Tạo danh mục mới.

---

#### 3. Xóa Danh Mục
- **HTTP Method**: `DELETE` | **Endpoint**: `/api/v1/admin/categories/{id}`
- **Security**: `ROLE_ADMIN`

---

#### 4. Upload Hình Ảnh Cho Danh Mục
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/categories/{id}/image`
- **Security**: `ROLE_ADMIN`
- **Content-Type**: `multipart/form-data` | Form Field: `file`

---

#### 5. Cấu Hình Hiển Thị Bento Grid / Trang Chủ Cho Danh Mục
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/categories/{id}/home-config`
- **Security**: `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "badge": "BÁN CHẠY #1",
    "badgeType": "hot", // "hot" | "fresh" | "dry" | "number"
    "iconName": "fish",
    "homeDisplayStyle": "main", // "main" (Ô lớn) | "card" (Thẻ vừa) | "icon" (Nút tròn nhỏ)
    "homeSortOrder": 1,
    "homeIsActive": true
  }
  ```

---

## 5. 📦 MODULE 4: QUẢN LÝ SẢN PHẨM & DASHBOARD ANALYTICS

### 5.1 Quản Lý Sản Phẩm (`/api/v1/admin/products`)

#### 1. Thêm Sản Phẩm Mới
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/products`
- **Security**: Yêu cầu Permission `product:create`
- **Request Body**:
  ```json
  {
    "name": "Cua Gạch Cà Mau Tuyển Chọn",
    "description": "Cua gạch đầy 100%, thịt ngọt chắc...",
    "price": 480000.00,
    "originalPrice": 550000.00,
    "stock": 50,
    "categoryId": 1,
    "unit": "kg",
    "spec": "Size 2-3 con/kg",
    "origin": "Cà Mau, Việt Nam",
    "weightOptions": ["1kg", "2kg", "3kg", "5kg"],
    "productType": "REGULAR" // "REGULAR" | "COMBO"
  }
  ```
- **Response (201 Created)**: `ApiResponse<ProductResponseDto>`

---

#### 2. Cập Nhật Thông Tin Sản Phẩm (Partial Update)
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/products/{id}`
- **Security**: Yêu cầu Permission `product:update`
- **Request Body**: Gửi các trường cần cập nhật (DTO `UpdateProductRequestDto`).

---

#### 3. Xóa Mềm Sản Phẩm (Soft Delete)
- **HTTP Method**: `DELETE` | **Endpoint**: `/api/v1/admin/products/{id}`
- **Security**: Yêu cầu Permission `product:delete`
- **Mô tả**: Chuyển cờ `isActive = false`, sản phẩm sẽ ẩn khỏi Storefront nhưng giữ nguyên dữ liệu lịch sử đơn hàng.

---

#### 4. Tăng / Giảm Tồn Kho Nhanh
- **Tăng tồn kho**: `PATCH /api/v1/admin/products/increase/{id}?quantity=20`
- **Giảm tồn kho**: `PATCH /api/v1/admin/products/decrease/{id}?quantity=5`

---

#### 5. Upload Hình Ảnh Sản Phẩm
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/products/{id}/image`
- **Content-Type**: `multipart/form-data` | Form Field: `file`

---

#### 6. Bật / Tắt Ghim Nổi Bật Trang Chủ
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/products/{id}/featured`
- **Mô tả**: Đảo ngược cờ `isFeatured` (true $\leftrightarrow$ false).

---

#### 7. Cấu Hình Gói Combo Nổi Bật Trang Chủ
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/products/{id}/combo-config`
- **Request Body**:
  ```json
  {
    "comboCategory": "COMBO TIỆC GIA ĐÌNH",
    "comboTheme": "dark", // "light" | "dark"
    "comboTag": "TIẾT KIỆM 20%",
    "comboCtaText": "Đặt Set Ngay",
    "comboHref": "/combos/set-hoang-gia",
    "isBreakout": false,
    "comboSortOrder": 1
  }
  ```

---

### 5.2 Báo Cáo & Thống Kê Dashboard Doanh Thu

#### 1. Top Sản Phẩm Bán Chạy Nhất
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/products/top-buy?limit=10`
- **Response (200 OK)**: Trả về danh sách top sản phẩm có lượt mua cao nhất kèm tổng số lượng bán.

#### 2. Doanh Thu Phân Bổ Theo Danh Mục
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/products/revenue-by-category`
- **Response (200 OK)**: Danh sách tổng doanh thu từng danh mục sản phẩm.

#### 3. Doanh Thu Tổng Hợp Theo Từng Tháng
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/products/revenue-in-month`
- **Response (200 OK)**: Biểu đồ doanh thu theo 12 tháng gần nhất.

---

## 6. 🏠 MODULE 5: QUẢN LÝ NỘI DUNG TRANG CHỦ & REDIS CACHE

### 6.1 Xóa Clean Toàn Bộ Cache Trang Chủ (Evict Cache)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/home/cache/evict`
- **Security**: `ROLE_ADMIN`
- **Mô tả**: Xóa sạch toàn bộ 8 phân vùng Cache Redis của trang chủ để cập nhật ngay lập tức các thay đổi về Banner, Danh mục, Sản phẩm lên Storefront.

---

### 6.2 Quản Lý Hero Banners (`/api/v1/admin/hero-banners`)
- **Lấy danh sách tất cả banners**: `GET /api/v1/admin/hero-banners`
- **Tạo banner mới**:
  - `POST /api/v1/admin/hero-banners`
  - Body: `{ "title": "Đại Tiệc Hải Sản", "subtitle": "Giảm ngay 20% cho đơn hàng đầu tiên", "ctaText": "Mua Ngay", "ctaLink": "/products", "sortOrder": 1, "isActive": true }`
- **Cập nhật banner**: `PATCH /api/v1/admin/hero-banners/{id}`
- **Bật / Tắt hiển thị banner**: `PATCH /api/v1/admin/hero-banners/{id}/toggle`
- **Upload ảnh banner**: `POST /api/v1/admin/hero-banners/{id}/image` (Form Field: `file`)
- **Xóa banner**: `DELETE /api/v1/admin/hero-banners/{id}`

---

### 6.3 Quản Lý Hải Sản Cập Bến Theo Ngày (`/api/v1/admin/daily-arrivals`)
- **Lấy danh sách theo ngày**: `GET /api/v1/admin/daily-arrivals?date=2026-08-19`
- **Thêm sản phẩm vào danh sách cập bến**:
  - `POST /api/v1/admin/daily-arrivals`
  - Body:
    ```json
    {
      "productId": 1,
      "arrivalDate": "2026-08-19",
      "badge": "CHUYẾN ĐÊM HÔM NAY",
      "title": "Tôm Hùm Bông Phú Yên",
      "description": "Vừa cập bến lúc 4h sáng, sống 100%..."
    }
    ```
- **Cập nhật thông tin cập bến**: `PATCH /api/v1/admin/daily-arrivals/{id}`
- **Xóa khỏi danh sách cập bến**: `DELETE /api/v1/admin/daily-arrivals/{id}`

---

## 7. 📑 MODULE 6: QUẢN LÝ & XỬ LÝ ĐƠN HÀNG (`/api/v1/admin/orders`)

### 7.1 State Machine Vòng Đời Đơn Hàng:
```
           ┌──────────────┐
           │   PENDING    │ (Chờ xác nhận / Chờ thanh toán)
           └──────┬───────┘
                  │
                  ▼
           ┌──────────────┐
           │  CONFIRMED   │ (Admin xác nhận đơn / Đã thanh toán)
           └──────┬───────┘
                  │
                  ▼
           ┌──────────────┐
           │   SHIPPED    │ (Đang giao hàng)
           └──────┬───────┘
                  │
                  ▼
           ┌──────────────┐
           │     DONE     │ (Giao hàng thành công)
           └──────────────┘
           
     * Có thể chuyển CANCELLED từ PENDING hoặc CONFIRMED
```

---

### 7.2 Chi Tiết Các API Quản Lý Đơn Hàng

#### 1. Lấy Toàn Bộ Đơn Hàng Hệ Thống (Phân trang)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/orders`
- **Query Parameters**: `page=0&size=10&sort=createdAt,desc`
- **Response (200 OK)**: `ApiResponse<PageResponse<OrderResponseDto>>`

---

#### 2. Lọc Đơn Hàng Theo Khách Hàng hoặc Trạng Thái
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/admin/orders/{userId}`
- **Query Parameters**:
  - `userId` *(Path / Query)*: ID khách hàng.
  - `status` *(Query, optional)*: `PENDING`, `CONFIRMED`, `SHIPPED`, `DONE`, `CANCELLED`.
  - `page` & `size`.

---

#### 3. Cập Nhật Trạng Thái Đơn Hàng
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/admin/orders/{id}/update-status`
- **Request Body**:
  ```json
  {
    "status": "CONFIRMED" // "CONFIRMED" | "SHIPPED" | "DONE" | "CANCELLED"
  }
  ```
- **Response (200 OK)**: Trả về chi tiết đơn hàng đã cập nhật trạng thái.

---

## 8. ⚠️ BẢNG MÃ LỖI PHÍA ADMIN & CÁCH XỬ LÝ

| HTTP Status | Exception / Mã Lỗi | Nguyên Nhân Thường Gặp | Cách Xử Lý Ở Frontend Admin |
|:---:|---|---|---|
| `400` | `Validation failed` | Điền thiếu giá sản phẩm, mã quyền, hoặc sai định dạng. | Báo đỏ viền Input tương ứng và hiển thị thông điệp lỗi. |
| `401` | `UNAUTHORIZED` | Token Admin đã hết hạn hoặc phiên đăng nhập bị hủy. | Tự động refresh token hoặc chuyển hướng về trang `/admin/login`. |
| `403` | `AccessDeniedException` | Tài khoản Admin thiếu Permission cụ thể (VD: thiếu `product:delete`). | Hiển thị thông báo Toast cảnh báo: "Bạn không có quyền thực hiện hành động này". |
| `404` | `ResourceNotFoundException` | Không tìm thấy Product ID, Role ID, hoặc Order ID. | Thông báo dữ liệu không tồn tại và tải lại bảng danh sách. |
| `409` | `DuplicateResourceException` | Tên Role (`name`), Tên Danh mục (`name`) hoặc Mã Permission bị trùng lặp. | Nhắc người quản trị nhập tên khác không bị trùng. |
| `500` | `INTERNAL_SERVER_ERROR` | Lỗi kết nối Database / MinIO S3 / Redis. | Báo Toast lỗi và ghi log chi tiết mã lỗi ra console. |
