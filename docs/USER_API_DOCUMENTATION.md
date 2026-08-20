# 🛍️ HƯỚNG DẪN TÍCH HỢP API — PHÍA CLIENT / USER STOREFRONT

> **Dành cho**: Đội ngũ phát triển Frontend Storefront (ReactJS, Next.js, Vue.js, Mobile App dành cho Khách hàng)  
> **Phiên bản Backend**: `v1.2.0` | **Base URL**: `http://localhost:8085` (Cấu hình qua `NEXT_PUBLIC_API_URL` hoặc `VITE_API_URL`)  
> **Mục tiêu**: Hướng dẫn chi tiết 100% các API công khai và API tài khoản dành cho Khách hàng mua sắm.

---

## 1. 📌 QUY CHUẨN KỸ THUẬT CHUNG & BẢO MẬT

### 1.1 Headers Chuẩn Mỗi Request
- **Mặc định Request Body JSON**: `Content-Type: application/json`
- **Upload File**: `Content-Type: multipart/form-data`
- **Xác thực JWT**: `Authorization: Bearer <accessToken>` (Bắt buộc với các API yêu cầu đăng nhập).

---

### 1.2 Cấu Trúc Khung Trả Về Chuẩn (`ApiResponse<T>`)

Mọi API Response từ Backend (Dù thành công hay thất bại) đều được bọc trong khung JSON duy nhất:

#### ✅ Response Thành Công (`HTTP 200 OK` / `201 Created`):
```json
{
  "success": true,
  "message": "Mô tả thông điệp thành công",
  "data": { ... }, // Dữ liệu trả về (Object, Array, hoặc PageResponse)
  "timestamp": "2026-08-19T16:00:00.123456"
}
```

#### ❌ Response Thất Bại / Lỗi Hệ Thống (`HTTP 401` / `403` / `404` / `409` / `500`):
```json
{
  "success": false,
  "message": "Mô tả nguyên nhân lỗi chi tiết từ Server",
  "data": null,
  "timestamp": "2026-08-19T16:00:00.123456"
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
  "timestamp": "2026-08-19T16:00:00"
}
```

---

### 1.3 Cấu Trúc Phân Trang Chuẩn (`PageResponse<T>`)

Xuất hiện ở thuộc tính `data` của các API trả về danh sách phân trang (Sản phẩm, Đơn hàng):

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
  "timestamp": "2026-08-19T16:00:00"
}
```

---

### 1.4 Luồng Tự Động Refresh Token (Axios Interceptor Lỗi 401)
1. Lưu `accessToken` trong Memory / State và `refreshToken` trong Storage / Cookie (httpOnly).
2. Khi gọi API bị trả về `401 Unauthorized`:
   - Bắt lỗi qua Axios Interceptor và gọi `POST /api/v1/auth/refresh-token` với body `{ "refreshToken": "..." }`.
   - Nhận về `accessToken` mới, cập nhật lại Storage.
   - Thử lại (retry) Request ban đầu bị thất bại với token mới.
   - Nếu gọi API refresh token cũng trả về `401` $\rightarrow$ Xóa token và redirect người dùng về trang `/login`.

---

## 2. 🔐 MODULE 1: AUTHENTICATION & XÁC THỰC KHÁCH HÀNG (`/api/v1/auth`)

### 2.1 Quy Trình Đăng Ký & Xác Thực OTP (OTP Flow)
```
[1. Đăng ký]                [2. Nhận OTP qua Email]          [3. Xác thực OTP]
POST /api/v1/auth/register --------> Email (6 chữ số) -------> POST /api/v1/auth/verify-otp
                                                                  (purpose: REGISTER_VERIFICATION)
                                                                  |
                                                                  v
                                                             Trả về accessToken & refreshToken
                                                             (Tự động Login vào Storefront)
```

---

### 2.2 Chi Tiết Các API Auth Khách Hàng

#### 1. Đăng Ký Tài Khoản Khách Hàng (Self-Registration)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/register`
- **Security**: Public
- **Mô tả**: Tạo tài khoản khách hàng mới với Role mặc định `ROLE_USER` / `CUSTOMER`. Hệ thống tự động kích hoạt gửi mã OTP xác thực qua email.
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
    "message": "Create User Successfully!",
    "data": {
      "id": 10,
      "fullName": "Nguyễn Văn A",
      "email": "user@example.com",
      "phoneNumber": "0987654321",
      "avatarUrl": null,
      "active": true,
      "emailVerified": false,
      "roles": ["ROLE_USER"]
    },
    "timestamp": "2026-08-19T16:00:00"
  }
  ```

---

#### 2. Xác Thực Mã OTP (Verify OTP)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/verify-otp`
- **Security**: Public (Rate Limit: 3 requests / phút)
- **Mô tả**: Dùng để kích hoạt tài khoản sau khi đăng ký hoặc xác thực để lấy `actionToken` đổi mật khẩu.
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "otpCode": "123456",
    "purpose": "REGISTER_VERIFICATION" // "REGISTER_VERIFICATION" | "RESET_PASSWORD"
  }
  ```
- **Response (200 OK) khi `REGISTER_VERIFICATION`**:
  ```json
  {
    "success": true,
    "message": "Verify Otp Successfully!",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "4a7b8c9d-1234-5678-90ab-cdef12345678"
    },
    "timestamp": "2026-08-19T16:00:00"
  }
  ```
- **Response (200 OK) khi `RESET_PASSWORD`**:
  ```json
  {
    "success": true,
    "message": "Verify Otp Successfully!",
    "data": {
      "actionToken": "eyJhbGciOiJIUzI1NiJ9..." // Vé tạm thời dùng cho bước đổi mật khẩu (hết hạn trong 10 phút)
    },
    "timestamp": "2026-08-19T16:00:00"
  }
  ```

---

#### 3. Đăng Nhập Khách Hàng (Login)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/login`
- **Security**: Public (Rate Limit: 10 requests / phút / IP)
- **Mô tả**: Đăng nhập bằng email và mật khẩu. Nếu tài khoản chưa xác thực email (`emailVerified = false`), server trả lỗi `403 AccountNotVerifiedException`.
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
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "4a7b8c9d-1234-5678-90ab-cdef12345678",
      "tokenType": "Bearer",
      "expiresIn": 86400000
    },
    "timestamp": "2026-08-19T16:00:00"
  }
  ```

---

#### 4. Gửi Lại Mã OTP (Resend OTP)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/resend-otp`
- **Security**: Public (Rate Limit OTP)
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "purpose": "REGISTER_VERIFICATION" // "REGISTER_VERIFICATION" | "RESET_PASSWORD"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Resend Otp Successfully!",
    "data": {
      "message": "New verification code has been sent!"
    }
  }
  ```

---

#### 5. Quên Mật Khẩu (Forgot Password)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/forgot-password`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "email": "user@example.com"
  }
  ```
- **Response (200 OK)**: Gửi mã OTP xác nhận đặt lại mật khẩu qua email.

---

#### 6. Đặt Lại Mật Khẩu (Reset Password)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/reset-password`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "actionToken": "action_token_nhan_duoc_tu_verify_otp",
    "newPassword": "NewPassword123@"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Reset Password Successfully!",
    "data": null
  }
  ```

---

#### 7. Làm Mới Access Token (Refresh Token)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/refresh-token`
- **Security**: Public
- **Request Body**:
  ```json
  {
    "refreshToken": "4a7b8c9d-1234-5678-90ab-cdef12345678"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Refresh Token Successfully!",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
    }
  }
  ```

---

#### 8. Đăng Xuất (Logout)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/auth/logout`
- **Security**: Public / Bearer Token
- **Request Body**:
  ```json
  {
    "refreshToken": "4a7b8c9d-1234-5678-90ab-cdef12345678"
  }
  ```
- **Response (200 OK)**: Thu hồi Refresh Token trong Database.

---

## 3. 👤 MODULE 2: USER PROFILE & SỔ ĐỊA CHỈ GIAO HÀNG

### 3.1 Thông Tin Cá Nhân (`/api/v1/users`)

#### 1. Lấy Profile Người Dùng Hiện Tại
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/users/me`
- **Security**: Bearer Token
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Get profile successfully",
    "data": {
      "id": 10,
      "fullName": "Nguyễn Văn A",
      "email": "user@example.com",
      "phoneNumber": "0987654321",
      "avatarUrl": "https://minio.yourdomain.com/avatars/abc.jpg",
      "active": true,
      "emailVerified": true,
      "roles": ["ROLE_USER"]
    }
  }
  ```

---

#### 2. Cập Nhật Profile
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/users/update-profile`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "fullName": "Nguyễn Văn B",
    "phoneNumber": "0912345678"
  }
  ```

---

#### 3. Upload Ảnh Đại Diện (Avatar)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/users/upload-avatar`
- **Security**: Bearer Token
- **Content-Type**: `multipart/form-data`
- **Form Data Field**: `file` (File hình ảnh PNG/JPG/WEBP, tối đa 5MB).
- **Response (200 OK)**: Trả về `UserResponseDto` với URL ảnh mới.

---

#### 4. Đổi Mật Khẩu (Khi Đang Đăng Nhập)
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

#### 1. Danh Sách Địa Chỉ Của Tôi
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/addresses/me`
- **Security**: Bearer Token
- **Response (200 OK)**: `ApiResponse<List<AddressResponseDto>>`
  ```json
  {
    "success": true,
    "message": "Get addresses successfully",
    "data": [
      {
        "id": 1,
        "recipientName": "Nguyễn Văn A",
        "phone": "0987654321",
        "province": "Thành phố Hồ Chí Minh",
        "district": "Quận 1",
        "ward": "Phường Bến Nghé",
        "addressDetail": "123 Đường Lê Lợi",
        "defaultAddress": true,
        "tag": "Nhà Riêng"
      }
    ]
  }
  ```

---

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

---

#### 3. Cập Nhật Địa Chỉ
- **HTTP Method**: `PATCH` | **Endpoint**: `/api/v1/addresses/{id}`
- **Security**: Bearer Token
- **Request Body**: Tương tự như Thêm địa chỉ mới.

---

#### 4. Xóa Địa Chỉ
- **HTTP Method**: `DELETE` | **Endpoint**: `/api/v1/addresses/delete/{id}`
- **Security**: Bearer Token

---

#### 5. Đặt Làm Địa Chỉ Mặc Định
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/addresses/change-address-default/{id}`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "defaultAddress": true
  }
  ```

---

## 4. 🏠 MODULE 3: TRANG CHỦ & CATALOG SẢN PHẨM PUBLIC

### 4.1 Toàn Bộ Dữ Liệu Trang Chủ Aggregated (`/api/v1/home`)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/home`
- **Security**: Public (Cache Redis 10 phút, phản hồi cực nhanh < 20ms)
- **Cấu Trúc 8 Khối Dữ Liệu Trả Về**:
  1. `heroSlides`: Banner slide chính trình chiếu (`id`, `title`, `subtitle`, `ctaText`, `ctaLink`, `imageUrl`, `badge`...).
  2. `categories`: Danh mục hiển thị dạng Bento Grid (`id`, `name`, `slug`, `imageUrl`, `badge`, `homeDisplayStyle`...).
  3. `dailyArrivals`: Danh sách hải sản tươi cập bến theo ngày hôm nay.
  4. `featuredProducts`: Sản phẩm nổi bật ghim trang chủ (`isFeatured = true`).
  5. `featuredProductTabs`: Danh sách các Tab lọc sản phẩm nổi bật (Tất cả, Bán chạy, Tôm cua, Cá biển...).
  6. `comboSets`: Danh sách các Set Combo tiệc/ăn uống ưu đãi (`productType = COMBO`).
  7. `featuredReviews`: Đánh giá 5 sao từ khách hàng uy tín.
  8. `stats`: Thống kê hệ thống (`totalOrdersDelivered`, `averageRating`, `totalReviews`).

---

### 4.2 Danh Mục Sản Phẩm Public (`/api/v1/categories`)

#### 1. Lấy Tất Cả Danh Mục Đang Hoạt Động
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/categories`
- **Security**: Public
- **Response (200 OK)**: `ApiResponse<List<CategoryResponseDto>>`

#### 2. Chi Tiết 1 Danh Mục
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/categories/{id}`
- **Security**: Public

---

### 4.3 Danh Sách & Tìm Kiếm Động Sản Phẩm (`/api/v1/products`)

#### 1. Tìm Kiếm & Lọc Động Sản Phẩm (Dynamic Criteria Search)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/products`
- **Security**: Public (Rate Limit: 60 requests / phút / IP)
- **Query Parameters**:
  - `search` *(String, optional)*: Tìm kiếm theo tên sản phẩm (không dấu/có dấu).
  - `categoryId` *(List<Long>, optional)*: Lọc theo 1 hoặc nhiều ID danh mục (`?categoryId=1&categoryId=2`).
  - `minPrice` *(BigDecimal, optional)*: Mức giá tối thiểu.
  - `maxPrice` *(BigDecimal, optional)*: Mức giá tối đa.
  - `inStock` *(Boolean, optional)*: `true` nếu chỉ muốn lấy sản phẩm còn hàng (`stock > 0`).
  - `page` *(Integer, default 0)*: Số trang (bắt đầu từ 0).
  - `size` *(Integer, default 10)*: Số lượng sản phẩm mỗi trang.
  - `sort` *(String, default "createdAt,desc")*: Quy tắc sắp xếp (`price,asc`, `price,desc`, `name,asc`, `createdAt,desc`).
- **Response (200 OK)**: `ApiResponse<PageResponse<ProductResponseDto>>`

---

#### 2. Chi Tiết Sản Phẩm Theo ID
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/products/{id}`
- **Security**: Public
- **Response (200 OK)**: `ApiResponse<ProductResponseDto>`
  ```json
  {
    "success": true,
    "message": "Get Product Successfully!",
    "data": {
      "id": 1,
      "name": "Cua Gạch Cà Mau Premium",
      "description": "Cua gạch son tự nhiên...",
      "price": 450000.00,
      "originalPrice": 520000.00,
      "stock": 35,
      "unit": "kg",
      "spec": "Size 2-3 con/kg",
      "origin": "Cà Mau",
      "weightOptions": ["1kg", "2kg", "3kg"],
      "imageUrl": "https://minio.yourdomain.com/products/cua-gach.jpg",
      "averageRating": 4.9,
      "reviewCount": 18,
      "isFeatured": true,
      "productType": "REGULAR"
    }
  }
  ```

---

## 5. 🛒 MODULE 4: ĐẶT HÀNG & QUẢN LÝ ĐƠN CỦA KHÁCH (`/api/v1/orders`)

> ⚠️ **ĐIỀU KIỆN TIÊN QUYẾT KHI TẠO ĐƠN**:  
> Backend sẽ tự động lấy **Địa chỉ mặc định (`defaultAddress = true`)** của tài khoản hiện tại để lưu Snapshot cho đơn hàng.  
> Nếu tài khoản chưa có địa chỉ mặc định, API sẽ trả về lỗi `404 Not Found: Address not found!`. FE cần kiểm tra và hướng dẫn khách thêm địa chỉ trước khi bấm Đặt hàng.

---

### 1. Tạo Đơn Hàng Mới (Checkout)
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/orders`
- **Security**: Bearer Token
- **Request Body**:
  ```json
  {
    "paymentMethod": "VNPAY", // "COD" | "VNPAY" | "MOMO" | "ZALOPAY"
    "note": "Giao hàng giờ hành chính, gọi trước khi giao",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 5,
        "quantity": 1
      }
    ]
  }
  ```
- **Response (201 Created)**:
  ```json
  {
    "success": true,
    "message": "Create Order Successfully!",
    "data": {
      "id": 105,
      "totalAmount": 950000.00,
      "status": "PENDING",
      "paymentMethod": "VNPAY",
      "note": "Giao hàng giờ hành chính",
      "shippingAddress": {
        "recipientName": "Nguyễn Văn A",
        "phone": "0987654321",
        "addressDetail": "123 Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM"
      },
      "items": [ ... ]
    }
  }
  ```

---

### 2. Lấy Danh Sách Đơn Hàng Của Tôi
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/orders/my-orders?page=0&size=10`
- **Security**: Bearer Token
- **Response (200 OK)**: `ApiResponse<PageResponse<OrderResponseDto>>`

---

### 3. Chi Tiết Đơn Hàng Theo ID
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/orders/{id}`
- **Security**: Bearer Token (Tự động kiểm tra quyền sở hữu đơn hàng).

---

### 4. Hủy Đơn Hàng
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/orders/{id}/cancel-order`
- **Security**: Bearer Token
- **Mô tả**: Khách hàng chỉ được phép hủy đơn khi trạng thái còn là `PENDING`.

---

## 6. 💳 MODULE 5: THANH TOÁN ONLINE VNPAY (`/api/v1/payments`)

### 1. Tạo Link Thanh Toán VNPay
- **HTTP Method**: `POST` | **Endpoint**: `/api/v1/payments/{orderId}/create`
- **Security**: Bearer Token
- **Request URL Param**: `orderId` (ID của đơn hàng vừa tạo ở bước Checkout).
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Create Payment Successfully!",
    "data": {
      "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=95000000&vnp_Command=pay&..."
    }
  }
  ```
- **Xử lý trên Frontend**: Redirect toàn màn hình trình duyệt sang URL `paymentUrl` để khách hàng thanh toán qua Ngân hàng / QR VNPay.

---

### 2. Trang Kết Quả Thanh Toán Callback (`vnpay-return`)
- **HTTP Method**: `GET` | **Endpoint**: `/api/v1/payments/vnpay-return`
- **Luồng hoạt động**:
  1. Khách hàng thanh toán xong trên cổng VNPay.
  2. VNPay redirect trình duyệt về Backend (`/api/v1/payments/vnpay-return`).
  3. Backend kiểm tra chữ ký HMAC-SHA512 hợp lệ, cập nhật trạng thái đơn hàng.
  4. Backend tự động redirect (302) trình duyệt của khách hàng về Frontend URL:
     `http://localhost:3000/payment-result?paymentId=12&status=SUCCESS&orderId=105`
- **FE Integration**:
  - Tạo trang `/payment-result` trên Storefront.
  - Đọc `status` từ URL: nếu `status === 'SUCCESS'` $\rightarrow$ hiển thị Chúc mừng đặt hàng & thanh toán thành công; nếu `status === 'FAILED'` $\rightarrow$ hiển thị thông báo lỗi và nút "Thử thanh toán lại".

---

## 7. ⚠️ BẢNG MÃ LỖI & CÁCH XỬ LÝ TẠI FRONTEND STOREFRONT

| HTTP Status | Exception / Error Code | Nguyên Nhân | Cách Xử Lý Ở Giao Diện Client |
|:---:|---|---|---|
| `400` | `Validation failed` | Nhập sai định dạng email, mật khẩu yếu... | Hiển thị thông báo lỗi dưới từng ô Input tương ứng với key trong `data`. |
| `400` | `INSUFFICIENT_STOCK` | Sản phẩm trong giỏ đã hết hàng hoặc vượt tồn kho. | Báo Toast đỏ và cập nhật lại số lượng giỏ hàng. |
| `401` | `UNAUTHORIZED` / `ExpiredToken` | Access token hết hạn hoặc chưa đăng nhập. | Tự động kích hoạt Interceptor gọi refresh-token hoặc chuyển về `/login`. |
| `403` | `AccountNotVerifiedException` | Chưa xác thực OTP sau khi đăng ký. | Chuyển hướng người dùng sang trang nhập mã OTP (`/verify-otp`). |
| `404` | `Address not found!` | Khách chưa có địa chỉ giao hàng mặc định khi đặt hàng. | Modal Popup nhắc khách: "Vui lòng thêm địa chỉ giao hàng trước khi thanh toán". |
| `409` | `DuplicateResourceException` | Email hoặc Số điện thoại đã được đăng ký trước đó. | Báo lỗi tại ô Email / SĐT: "Email/Số điện thoại này đã được sử dụng". |
| `429` | `TOO_MANY_REQUESTS` | Gửi OTP hoặc Login quá số lần cho phép trong 1 phút. | Khóa nút gửi mã và hiển thị đồng hồ đếm ngược chờ 60 giây. |
| `500` | `INTERNAL_SERVER_ERROR` | Lỗi máy chủ không mong muốn. | Báo Toast: "Hệ thống đang bận, vui lòng thử lại sau ít phút". |
