# 📘 HƯỚNG DẪN TÍCH HỢP & MAPPING API TOÀN BỘ DỰ ÁN (FOR FRONTEND FE)

> **Dành cho**: Đội ngũ thiết kế & phát triển Frontend (ReactJS / Next.js / Vue.js / Mobile App)  
> **Phiên bản API**: `v1.2.0` | **Base URL**: `http://localhost:8085` (Hoặc cấu hình qua `.env`)  
> **Tỷ lệ bao phủ**: 100% Endpoints hiện có trong 16 Controllers backend.

---

## 1. 📌 QUY CHUẨN KỸ THUẬT CHUNG & BẢO MẬT

### 1.1 Headers Chuẩn Mỗi Request
- **Mặc định**: `Content-Type: application/json`
- **Tệp tin Upload**: `Content-Type: multipart/form-data`
- **Xác thực JWT**: `Authorization: Bearer <accessToken>` (Bắt buộc với các API bảo mật).

---

### 1.2 Cấu Trúc Khung Trả Về Chuẩn (`ApiResponse<T>`)

Mọi Response từ Backend (dù Thành công hay Thất bại) đều được đóng gói trong một JSON Object duy nhất:

#### ✅ Response Thành Công (`HTTP 200 OK` / `201 Created` / `200 OK with Data`):
```json
{
  "success": true,
  "message": "Mô tả thông điệp thành công",
  "data": { ... }, // Dữ liệu trả về (Object, Array, hoặc Record)
  "timestamp": "2026-08-13T01:45:00.123456"
}
```

#### ❌ Response Thất Bại / Lỗi (`HTTP 400` / `401` / `403` / `404` / `409` / `500`):
```json
{
  "success": false,
  "message": "Mô tả nguyên nhân lỗi chi tiết từ Server",
  "data": null,
  "timestamp": "2026-08-13T01:45:00.123456"
}
```

#### ⚠️ Response Lỗi Validation Dữ Liệu (`HTTP 400 Bad Request`):
Khi gửi Request Body không thỏa mãn ràng buộc Validation (ví dụ: vi phạm `@NotBlank`, `@Email`, `@Min`), server sẽ trả về danh sách các trường bị lỗi chi tiết trong thuộc tính `data`:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email format is invalid",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2026-08-13T01:45:00"
}
```

---

### 1.3 Cấu Trúc Phân Trang Chuẩn (`PageResponse<T>`)

Nằm bên trong thuộc tính `data` của `ApiResponse<PageResponse<T>>` đối với các API danh sách (Sản phẩm, Đơn hàng, Người dùng):

```json
{
  "success": true,
  "message": "Get list successfully",
  "data": {
    "content": [ ... ],       // Danh sách phần tử của trang hiện tại
    "page": 0,               // Trang hiện tại (0-indexed)
    "size": 10,              // Kích thước trang
    "totalElements": 45,     // Tổng số phần tử tìm thấy trong DB
    "totalPages": 5,         // Tổng số trang
    "last": false            // Boolean: Đã là trang cuối cùng chưa
  },
  "timestamp": "2026-08-13T01:45:00"
}
```

---

## 2. 🔐 MODULE 1: AUTHENTICATION & SECURITY (`/api/v1/auth`)

---

### 1. Đăng Ký Tài Khoản (Register)
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/auth/register`
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
- **Response (201 Created)**:
  ```json
  {
    "success": true,
    "message": "User registered successfully. Please verify your email with OTP.",
    "data": {
      "userId": 1,
      "fullName": "Nguyễn Văn A",
      "email": "user@example.com",
      "emailVerified": false
    }
  }
  ```
- **FE UI Mapping**: Form đăng ký người dùng mới. Sau khi đăng ký thành công, chuyển màn hình sang nhập OTP xác thực.

---

### 2. Đăng Nhập System (Login)
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/auth/login`
- **Security**: Public
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
    "message": "Login successfully",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
      "refreshToken": "7f9a8b1c-3d2e-4f5a-6b7c-8d9e0f1a2b3c",
      "tokenType": "Bearer",
      "expiresIn": 3600,
      "user": {
        "id": 1,
        "fullName": "Nguyễn Văn A",
        "email": "user@example.com",
        "roles": ["ROLE_CUSTOMER"]
      }
    }
  }
  ```
- **FE UI Mapping**: Form đăng nhập. Lưu `accessToken` vào Memory / Secure State, lưu `refreshToken` vào Secure Cookie / LocalStorage.

---

### 3. Làm Mới Token (Refresh Token)
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/auth/refresh-token`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "refreshToken": "7f9a8b1c-3d2e-4f5a-6b7c-8d9e0f1a2b3c"
  }
  ```
- **Response (200 OK)**: Trả về `accessToken` mới và `refreshToken` mới (Token Rotation).
- **FE UI Mapping**: Axios Interceptor tự động bắt lỗi `401 Unauthorized` để âm thầm gọi API này refresh token trước khi gọi lại request cũ.

---

### 4. Đăng Xuất (Logout)
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/auth/logout`
- **Security**: Authenticated (`Bearer <accessToken>`)
- **Request Body**:
  ```json
  {
    "refreshToken": "7f9a8b1c-3d2e-4f5a-6b7c-8d9e0f1a2b3c"
  }
  ```
- **Response (200 OK)**: Thu hồi Refresh Token trên DB.
- **FE UI Mapping**: Nút Đăng xuất trên Navigation / Profile page.

---

### 5. Gửi Mã OTP Qua Email (Send OTP)
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/auth/otp/send`
- **Security**: Public (Áp dụng Rate Limit 3 requests / min)
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "purpose": "REGISTER_VERIFICATION" // "REGISTER_VERIFICATION" | "RESET_PASSWORD"
  }
  ```
- **Response (200 OK)**: Mã OTP 6 chữ số gửi qua Email.

---

### 6. Xác Thực Mã OTP (Verify OTP)
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/auth/otp/verify`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "otpCode": "123456",
    "purpose": "REGISTER_VERIFICATION"
  }
  ```
- **Response (200 OK)**: Cập nhật trạng thái verified hoặc cấp Reset Token.

---

### 7. Đặt Lại Mật Khẩu (Reset Password)
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/auth/reset-password`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "otpCode": "123456",
    "newPassword": "NewPassword123@"
  }
  ```

---

## 👤 MODULE 2: USER PROFILE & ADDRESSES (`/api/v1/users`)

---

### 1. Lấy Thông Tin Profile
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/users/me`
- **Security**: Authenticated (`ROLE_CUSTOMER` / `ROLE_ADMIN`)
- **Response (200 OK)**: Trả về fullName, email, phoneNumber, avatarUrl, roles, permissions.

---

### 2. Cập Nhật Thông Tin Profile
- **HTTP Method**: `PUT` | **Endpoint**: `/api/v1/users/me`
- **Request Body**: `{ "fullName": "Nguyễn Văn B", "phoneNumber": "0912345678" }`

---

### 3. Upload Avatar
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/users/me/avatar`
- **Content-Type**: `multipart/form-data`
- **Form Data**: `file` (File ảnh PNG/JPEG, max 5MB).

---

### 4. Đổi Mật Khẩu
- **HTTP Method**: `PUT` | **Endpoint**: `/api/v1/users/change-password`
- **Request Body**: `{ "oldPassword": "...", "newPassword": "..." }`

---

### 5. Quản Lý Sổ Địa Chỉ (Addresses CRUD)
- `GET /api/v1/users/addresses`: Lấy danh sách địa chỉ giao hàng của người dùng.
- `POST /api/v1/users/addresses`: Tạo địa chỉ mới (Tỉnh/Thành, Quận/Huyện, Phường/Xã, Chi tiết, tag "Nhà riêng"/"Cơ quan").
- `PUT /api/v1/users/addresses/{id}`: Cập nhật địa chỉ.
- `DELETE /api/v1/users/addresses/{id}`: Xóa địa chỉ.
- `PATCH /api/v1/users/addresses/{id}/default`: Đặt địa chỉ làm mặc định.

---

## 🔍 MODULE 3: PRODUCTS & DYNAMIC SEARCH ENGINE (`/api/v1/products`)

---

### 1. Tìm Kiếm & Lọc Động Sản Phẩm (Dynamic Criteria Search API)
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/products`
- **Security**: Public (Rate Limit 60 req/min)
- **Query Parameters**:
  - `search` *(String, Optional)*: Từ khóa tìm kiếm tên sản phẩm.
  - `categoryId` *(List<Long>, Optional)*: Danh sách ID danh mục (Ví dụ: `?categoryId=1&categoryId=2`).
  - `minPrice` *(BigDecimal, Optional)*: Giá tối thiểu.
  - `maxPrice` *(BigDecimal, Optional)*: Giá tối đa.
  - `inStock` *(Boolean, Optional)*: Lọc sản phẩm còn hàng (`stock > 0`).
  - `page` *(Integer, default 0)*: Trang hiện tại.
  - `size` *(Integer, default 10)*: Số lượng item/trang.
  - `sort` *(String, default "createdAt,desc")*: Sắp xếp theo Whitelist (`price,asc`, `price,desc`, `name,asc`, `createdAt,desc`).
- **FE UI Mapping**: Trang Tìm kiếm & Danh mục sản phẩm (Filter Bar bên trái + Danh sách Grid bên phải).

---

### 2. Chi Tiết Sản Phẩm
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/products/{id}`
- **Security**: Public
- **Response (200 OK)**: Trả về thông tin sản phẩm, danh mục, hình ảnh, thông số (`spec`), xuất xứ (`origin`), mảng tùy chọn khối lượng (`weightOptions`).

---

### 3. Admin Quản Lý Sản Phẩm (`/api/v1/admin/products`)
- `POST /api/v1/admin/products`: Tạo sản phẩm mới.
- `PUT /api/v1/admin/products/{id}`: Cập nhật thông tin sản phẩm.
- `DELETE /api/v1/admin/products/{id}`: Xóa sản phẩm.
- `POST /api/v1/admin/products/{id}/image`: Upload ảnh sản phẩm lên MinIO.
- `PATCH /api/v1/admin/products/{id}/toggle-featured`: Bật/tắt cờ Nổi bật trang chủ.

---

## 🏠 MODULE 4: PUBLIC HOME & CONTENT MANAGEMENT (`/api/v1/home`)

---

### 1. Lấy Toàn Bộ Dữ Liệu Trang Chủ (Aggregate Home Data)
- **HTTP Method**: `GET`
- **Endpoint**: `/api/v1/home`
- **Security**: Public
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Get home page data successfully",
    "data": {
      "heroSlides": [
        {
          "id": 1,
          "badgeText": "HẢI SẢN TƯƠI SỐNG",
          "badgeIcon": "Sparkles",
          "titlePrefix": "Đặc Sản",
          "titleHighlight": "Cua Cà Mau",
          "titleSuffix": "Thượng Hạng",
          "description": "Cua thịt chắc, ngọt, giao sống tận nơi...",
          "primaryCtaLabel": "Mua Ngay",
          "primaryCtaHref": "/products?search=cua",
          "primaryCtaIcon": "ShoppingBag",
          "cardImageUrl": "https://minio.domain.com/banners/cua.jpg",
          "cardOriginalPrice": 550000,
          "cardSalePrice": 450000,
          "cardTitle": "Cua Gạch Đất Mũi",
          "cardSubtitle": "Size 2-3 con/kg"
        }
      ],
      "categories": [
        {
          "id": 1,
          "name": "Tôm & Cua",
          "slug": "tom-cua",
          "imageUrl": "...",
          "badge": "TOP 1",
          "badgeType": "hot",
          "iconName": "utensils",
          "homeDisplayStyle": "main"
        }
      ],
      "dailyArrivals": [...],
      "featuredProducts": [...],
      "featuredProductTabs": [
        { "slug": "all", "label": "Tất cả", "sortOrder": 0 },
        { "slug": "tom-cua", "label": "Tôm & Cua", "sortOrder": 1 },
        { "slug": "muc-bach-tuoc", "label": "Mực & Bạch tuộc", "sortOrder": 2 },
        { "slug": "sot-tiec", "label": "Sốt Tiệc", "sortOrder": 3 },
        { "slug": "so-oc", "label": "Sò & Ốc", "sortOrder": 4 }
      ],
      "comboSets": [...],
      "featuredReviews": [],
      "stats": {
        "totalOrdersDelivered": 1250,
        "averageRating": 5.0,
        "totalReviews": 0
      }
    }
  }
  ```
- **FE UI Mapping**: Trang chủ (`HomePage`). Map 8 phần dữ liệu tương ứng vào các UI Components (Hero Carousel, Category Grid, Daily Arrival Cards, Featured Products Grid, Combo Banner, Stats Footer).

---

### 2. Admin Quản Lý Hero Banner (`/api/v1/admin/hero-banners`)
- `GET /api/v1/admin/hero-banners`: Lấy tất cả banner (Active & Inactive).
- `POST /api/v1/admin/hero-banners`: Tạo banner slide mới.
- `PATCH /api/v1/admin/hero-banners/{id}`: Cập nhật thông tin banner.
- `DELETE /api/v1/admin/hero-banners/{id}`: Xóa banner.
- `PATCH /api/v1/admin/hero-banners/{id}/toggle`: Bật/Tắt hiển thị banner.
- `POST /api/v1/admin/hero-banners/{id}/image`: Upload hình ảnh banner slide.

---

### 3. Admin Quản Lý Hải Sản Cập Bến Ngày (`/api/v1/admin/daily-arrivals`)
- `GET /api/v1/admin/daily-arrivals?date=yyyy-MM-dd`: Lấy danh sách hải sản cập bến theo ngày.
- `POST /api/v1/admin/daily-arrivals`: Thêm sản phẩm vào danh sách cập bến (`productId`, `date`, `badge`, `title`, `description`...).
- `PATCH /api/v1/admin/daily-arrivals/{id}`: Cập nhật thông tin cập bến.
- `DELETE /api/v1/admin/daily-arrivals/{id}`: Xóa sản phẩm khỏi cập bến.

---

### 4. Admin Evict Home Redis Cache (`/api/v1/admin/home/cache/evict`)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/admin/home/cache/evict`
- **Security**: Admin (`ROLE_ADMIN`)
- **Mục đích**: Làm mới/xóa sạch toàn bộ Redis Cache trang chủ để nội dung cập nhật ngay lập tức.

---

## 🛒 MODULE 5: ORDERS & CHECKOUT (`/api/v1/orders`)

---

### 1. Tạo Đơn Hàng Mới (Checkout)
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/orders`
- **Security**: Authenticated (`ROLE_CUSTOMER`)
- **Request Body**:
  ```json
  {
    "shippingAddressId": 1,
    "paymentMethod": "VNPAY", // "COD" | "VNPAY" | "MOMO" | "ZALOPAY"
    "note": "Giao hàng giờ hành chính",
    "items": [
      { "productId": 10, "quantity": 2 },
      { "productId": 15, "quantity": 1 }
    ]
  }
  ```
- **Response (201 Created)**: Trả về thông tin đơn hàng với `status: "PENDING"`.

---

### 2. Danh Sách Đơn Hàng Của Tôi
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/orders/my-orders?page=0&size=10`
- **Response (200 OK)**: Trả về danh sách đơn hàng phân trang kèm chi tiết từng sản phẩm trong đơn.

---

### 3. Hủy Đơn Hàng
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/orders/{id}/cancel`
- **Request Body**: `{ "reason": "Duyệt đổi sản phẩm khác" }`

---

### 4. Admin Quản Lý Đơn Hàng (`/api/v1/admin/orders`)
- `GET /api/v1/admin/orders`: Lấy toàn bộ đơn hàng hệ thống (hỗ trợ lọc status, user).
- `PATCH /api/v1/admin/orders/{id}/status`: Cập nhật trạng thái đơn hàng (`PENDING` ➔ `CONFIRMED` ➔ `SHIPPED` ➔ `DONE` / `CANCELLED`).

---

## 💳 MODULE 6: PAYMENT & VNPAY INTEGRATION (`/api/v1/payments`)

---

### 1. Tạo URL Thanh Toán VNPay
- **HTTP Method**: `POST`
- **Endpoint**: `/api/v1/payments/create-vnpay-url`
- **Security**: Authenticated
- **Request Body**:
  ```json
  {
    "orderId": 1001,
    "bankCode": "NCB" // Optional: "NCB", "VISA", "VNPAYQR" hoặc null để khách tự chọn
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Create payment URL successfully",
    "data": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=..."
  }
  ```
- **FE UI Mapping**: Redirect trình duyệt của người dùng sang đường dẫn `data` (VNPay Sandbox Page).

---

### 2. VNPay Return Callback Redirect (Trang kết quả thanh toán)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/payments/vnpay-return`
- **FE UI Mapping**: Trang thông báo kết quả thanh toán (`/checkout/success` hoặc `/checkout/failed`). Backend xác thực chữ ký checksum và cập nhật đơn hàng.

---

## 📝 7. BẢNG MÃ LỖI CHI TIẾT (ERROR CODE REFERENCE)

| Code | HTTP Status | Nguyên Nhân & Cách Xử Lý Ở Frontend |
|---|:---:|---|
| `VALIDATION_ERROR` | 400 | Dữ liệu Form không hợp lệ. Hiển thị thông báo dưới từng Input. |
| `UNAUTHORIZED` | 401 | Token hết hạn hoặc không hợp lệ. Chuyển hướng về trang Đăng nhập. |
| `FORBIDDEN` | 403 | Tài khoản không đủ quyền. Hiển thị thông báo "Truy cập bị từ chối". |
| `RESOURCE_NOT_FOUND` | 404 | Không tìm thấy Sản phẩm / Đơn hàng / Banner ID. Hiển thị trang 404. |
| `CONFLICT_ERROR` | 409 | Email/Phone đã tồn tại hoặc Xung đột tồn kho Optimistic Locking. |
| `TOO_MANY_REQUESTS` | 429 | Spam gửi request quá nhanh. Hiển thị đếm ngược `Retry-After`. |
