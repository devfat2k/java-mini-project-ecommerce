# Tài Liệu Dự Án: Mini Ecommerce (Java Spring Boot)

Đây là tài liệu mô tả chi tiết toàn cảnh về dự án Mini Ecommerce. Dự án là một side project cá nhân phục vụ mục tiêu rèn luyện kiến thức Java Backend, áp dụng các best practices thực tế và làm portfolio ứng tuyển cho vị trí Java Backend Developer.

---

## 1. Công Nghệ & Phiên Bản (Tech Stack)

Dự án được xây dựng trên hệ sinh thái Java hiện đại, tập trung vào tính ổn định và chuẩn mực:

- **Ngôn ngữ:** Java 21
- **Framework Core:** Spring Boot 3.5.16
- **Build Tool:** Maven
- **Cơ sở dữ liệu:** PostgreSQL (Relational Database)
- **Quản lý Database Schema:** Flyway Migration (versioning tự động `V1__init_mini_shop.sql`)
- **ORM Framework:** Spring Data JPA, Hibernate
- **RESTful API Documentation:** SpringDoc OpenAPI 2.7.0 (Swagger UI)
- **Tiện ích:** Lombok (giảm boilerplate code), Spring Boot Validation (validate Request Body), Spring Boot DevTools.

---

## 2. Cấu Trúc Mã Nguồn Chi Tiết (Project Structure)

Dự án tuân thủ nghiêm ngặt **Layered Architecture** (Kiến trúc phân tầng) của Spring Boot. Không lộ Entity ra ngoài Controller, toàn bộ dữ liệu giao tiếp với Client đều qua DTO.

```text
src/main/java/com/devfat/mini_ecommerce/
├── config/                 # Các thiết lập cấu hình bean
│   └── OpenApiConfig.java          # Cấu hình Swagger/OpenAPI docs
├── controller/             # Controller layer (Routing & HTTP Method mapping)
│   ├── CategoryController.java     # Quản lý API Danh mục
│   ├── OrderController.java        # Quản lý API Đơn hàng
│   ├── ProductController.java      # Quản lý API Sản phẩm
│   ├── UserController.java         # (Đang khởi tạo) Quản lý API Người dùng
│   └── PingController.java         # Health check system
├── dto/                    # Data Transfer Objects (Request/Response)
│   ├── request/                    # Record classes: CreateProduct, UpdateOrder...
│   └── response/                   # Record/Class: ProductResponse, OrderItemResponse...
├── entity/                 # Database mapping (Hibernate Entities)
│   ├── CategoryEntity.java
│   ├── OrderEntity.java
│   ├── OrderItemEntity.java
│   ├── ProductEntity.java
│   └── UserEntity.java
├── exception/              # Global Error Handling
│   ├── GlobalExceptionHandler.java # Bắt lỗi toàn cục @ControllerAdvice
│   ├── ErrorResponse.java          # Format trả về khi có lỗi
│   └── (Các Custom Exception: ResourceNotFound, InsufficientStock, InvalidStatusTransition)
├── repository/             # Data Access Layer (Kế thừa JpaRepository)
│   ├── CategoryRepository.java
│   ├── OrderRepository.java
│   ├── ProductRepository.java      # Chứa các custom native query tính doanh thu
│   └── UserRepository.java
├── service/                # Business Logic Layer (Interface)
│   ├── CategoryService.java, OrderService.java...
│   └── impl/                       # Implementation của Service
│       ├── CategoryServiceImpl.java
│       ├── OrderServiceImpl.java
│       ├── ProductServiceImpl.java
│       └── UserServiceImpl.java
└── MiniEcommerceApplication.java   # Main class khởi chạy Spring Boot

src/main/resources/
├── db/migration/           # File SQL Flyway khởi tạo bảng và seed data (V1__init_mini_shop.sql)
├── templates/email/        # Chứa template HTML gửi email thông báo
└── application.yaml        # File cấu hình (kết nối DB PostgreSQL, Mail, active profiles)
```

---

## 3. Cấu Trúc Database (Schema & Constraints)

Hệ thống bao gồm 5 bảng chính được thiết kế tối ưu với Index và Foreign Key rõ ràng.

1. **`users` (Người dùng)**
   - `id` (PK), `full_name`, `email` (UNIQUE), `phone_number` (UNIQUE), `password` (Lưu hashed).
   - `role`: Phân quyền (USER, ADMIN).
   - `is_active`: Soft delete người dùng.
2. **`categories` (Danh mục)**
   - `id` (PK), `name` (UNIQUE)
3. **`products` (Sản phẩm)**
   - `id` (PK), `name`, `description`, `price` (CHECK >= 0).
   - `stock` (CHECK >= 0): Quản lý tồn kho.
   - `category_id` (FK -> categories.id).
   - `is_active`: Xóa mềm (ẩn sản phẩm khỏi User).
4. **`orders` (Đơn hàng)**
   - `id` (PK), `user_id` (FK -> users.id), `note`.
   - `total_amount`: Tổng tiền đơn hàng (Tính ở Backend).
   - `status`: Enum (PENDING, CONFIRMED, SHIPPED, DONE, CANCELLED).
5. **`order_items` (Chi tiết đơn hàng - Bảng trung gian N:N)**
   - Lặp lại theo từng sản phẩm người dùng mua trong đơn.
   - `order_id` (FK), `product_id` (FK), `quantity` (Số lượng mua), `unit_price` (Giá chốt tại thời điểm mua).

---

## 4. Flow Hoạt Động (Business Workflows)

### 4.1. Luồng mua hàng (Order Creation Flow)

1. Client gửi `CreateOrderRequestDto` gồm `userId` và List `items` (mỗi item có `productId`, `quantity`).
2. Server check `User` có tồn tại không. Tạo một `Order` rỗng với trạng thái mặc định `PENDING`.
3. Server lặp qua danh sách `items`:
   - Tìm `Product` trong DB. Check xem `product.getStock() >= quantity` hay không. Nếu không đủ -> Quăng lỗi `InsufficientStockException`.
   - Trừ tồn kho trong DB: `product.setStock(stock - quantity)`.
   - Tạo `OrderItemEntity` ghi nhận giá `unit_price` chính là `product.getPrice()` lúc này.
4. Server tính tổng `totalAmount` của tất cả `OrderItem` và lưu vào `Order`. Trả về cho Client.

### 4.2. Luồng cập nhật trạng thái đơn (State Machine)

Hệ thống sử dụng MAP Validation tĩnh để ép buộc luồng cập nhật đơn hàng:

- `PENDING` -> `CONFIRMED`
- `CONFIRMED` -> `SHIPPED`
- `SHIPPED` -> `DONE`
- _Lưu ý: Bất kỳ nỗ lực nhảy cóc trạng thái nào (vd: PENDING -> DONE) sẽ bị văng lỗi `InvalidStatusTransitionException`._

---

## 5. Danh Sách Các API Đã Cung Cấp (Endpoints)

Toàn bộ API đều trả về dạng JSON và được handle Exception chuẩn mực.

### 🛍️ Product API (`/api/v1/products`)

- `POST /`: Tạo mới sản phẩm (Kèm check Validation DTO).
- `GET /`: Lấy danh sách sản phẩm (Hỗ trợ Pagination, Sorting và Search keyword).
- `GET /{id}`: Lấy chi tiết 1 sản phẩm.
- `PATCH /{id}`: Cập nhật thông tin sản phẩm (Chỉ cập nhật field được truyền).
- `DELETE /{id}`: Xóa mềm sản phẩm (`isActive = false`).
- `PATCH /increase/{id}?quantity=...`: Nhập thêm hàng vào kho.
- `PATCH /decrease/{id}?quantity=...`: Giảm hàng trong kho.
- `GET /top-buy`: Lấy top sản phẩm bán chạy nhất.
- `GET /revenue-by-category`: Báo cáo doanh thu theo danh mục.
- `GET /revenue-in-month`: Báo cáo doanh thu theo tháng.

### 🗂️ Category API (`/api/v1/categories`)

- `POST /`: Tạo danh mục mới.
- `GET /`: Lấy danh sách danh mục (Pagination, Sorting, Search).
- `GET /{id}`: Chi tiết danh mục.
- `PUT /{id}`: Cập nhật danh mục.
- `DELETE /{id}`: Xóa cứng danh mục trong DB.

### 📦 Order API (`/api/v1/orders`)

- `POST /`: Tạo đơn hàng mới (Mua nhiều sản phẩm cùng lúc).
- `GET /{userId}`: Lấy danh sách đơn hàng và chi tiết (`orderItems`) của một user.
- `GET /detail/{id}`: Xem chi tiết 1 mã đơn hàng (Tương lai/Hiện tại map với `/{id}`).
- `PATCH /{id}/status`: Cập nhật trạng thái đơn (PENDING -> CONFIRMED...).

### 👤 User API (`/api/v1/users`)

- _(Đã có Controller class trống, sẵn sàng triển khai logic)_.

---
