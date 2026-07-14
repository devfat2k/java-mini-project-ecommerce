# Tài Liệu Dự Án: Mini Ecommerce (Phiên Bản V2 - Dành Cho Developer & AI Agents)

Đây là tài liệu tổng quan toàn diện, chuyên sâu và chi tiết về dự án **Mini Ecommerce**. Mục tiêu của tài liệu này là cung cấp bức tranh toàn cảnh từ kiến trúc, cấu trúc thư mục, luồng nghiệp vụ (business flows), bảo mật cho đến các quy chuẩn code. Bất kỳ AI Agent hoặc Developer nào mới tiếp nhận dự án đều có thể đọc tài liệu này để nắm bắt ngay lập tức bối cảnh và tiếp tục phát triển mà không làm phá vỡ cấu trúc hiện tại.

---

## 1. Bối Cảnh & Mục Tiêu Dự Án (Context & Objectives)
- **Bản chất dự án:** Một ứng dụng e-commerce thu nhỏ (Mini Ecommerce) cung cấp các API RESTful phục vụ cho quy trình mua bán hàng hóa, quản lý sản phẩm, danh mục, đơn hàng và người dùng.
- **Kiến trúc:** Layered Architecture truyền thống, tập trung vào tính ổn định, dễ bảo trì, và phân tách rõ ràng trách nhiệm giữa các tầng (Separation of Concerns).

---

## 2. Công Nghệ & Môi Trường (Tech Stack)

Hệ sinh thái Java hiện đại được áp dụng với các framework và thư viện chuẩn mực:
- **Core:** Java 21, Spring Boot 3.5.16
- **Build Tool:** Maven (Quản lý dependency qua `pom.xml`)
- **Database:** PostgreSQL (Môi trường dev/prod có thể khác nhau cấu hình trong `application.yaml`)
- **ORM & Data Access:** Spring Data JPA, Hibernate.
- **Bảo mật (Security):** Spring Security + JWT (JSON Web Tokens) cho Authentication & Authorization.
- **Database Migration:** Flyway (`V1__init_mini_shop.sql` xử lý schema và seed data ban đầu).
- **API Documentation:** SpringDoc OpenAPI 2.7.0 (Swagger UI cho phép test API trực tiếp).
- **Tiện ích:** Lombok (giảm boilerplate code), Spring Boot Validation (Jakarta Bean Validation), Spring Boot DevTools.

---

## 3. Cấu Trúc Mã Nguồn (Detailed Project Structure)

Dự án tuân thủ nghiêm ngặt **Layered Architecture**. Tuyệt đối **KHÔNG** trả trực tiếp Entity ra Controller, mọi giao tiếp HTTP đều thông qua Request/Response DTO.

```text
src/main/java/com/devfat/mini_ecommerce/
├── config/                 # Chứa các file @Configuration của Spring.
│   ├── OpenApiConfig.java          # Cấu hình Swagger UI (Thông tin docs, security schemes).
│   └── SecurityConfig.java         # Cấu hình WebSecurity, CORS, URL whitelist, và add Filter.
├── controller/             # Tiếp nhận HTTP requests, validate DTO, gọi Service layer.
│   ├── AuthController.java         # API liên quan tới đăng nhập (login), trả về JWT.
│   ├── CategoryController.java     # CRUD danh mục.
│   ├── OrderController.java        # Quản lý luồng đơn hàng, chuyển đổi trạng thái đơn.
│   ├── ProductController.java      # CRUD sản phẩm, import/export kho, thống kê doanh thu.
│   ├── UserController.java         # API liên quan tới quản lý tài khoản người dùng.
│   └── PingController.java         # Health check (ping).
├── dto/                    # Data Transfer Objects (dạng Record hoặc Class).
│   ├── request/                    # Chứa input payload (CreateProductRequest, LoginRequest...).
│   └── response/                   # Chứa output payload (ProductResponse, TokenResponse...).
├── entity/                 # Database mapping (đánh dấu @Entity, định nghĩa Table).
│   ├── CategoryEntity.java, OrderEntity.java, OrderItemEntity.java, ProductEntity.java, UserEntity.java
├── enums/                  # Các hằng số Enum sử dụng chung.
├── exception/              # Global Error Handling & Custom Exceptions.
│   ├── GlobalExceptionHandler.java # @ControllerAdvice để format lỗi toàn cục.
│   ├── ErrorResponse.java          # Cấu trúc JSON thống nhất trả về khi có exception.
│   └── (ResourceNotFoundException, InsufficientStockException, InvalidStatusTransitionException, ...)
├── repository/             # Data Access Layer (Kế thừa JpaRepository).
│   └── (Chứa các Native Query / JPQL xử lý logic query phức tạp như thống kê).
├── security/               # Core logic về xác thực và phân quyền (JWT).
│   ├── CustomUserDetailsService.java # Load user từ DB theo email.
│   ├── JwtProvider.java              # Cấp phát (generate) và giải mã/validate JWT.
│   ├── JwtAuthenticationFilter.java  # Intercept mọi request để kiểm tra Bearer token.
│   ├── JwtAuthenticationEntryPoint.java # Handle lỗi HTTP 401 (Unauthorized).
│   └── JwtAccessDeniedHandler.java      # Handle lỗi HTTP 403 (Forbidden).
├── service/                # Business Logic Interfaces.
│   └── impl/               # Logic thực thi nghiệp vụ (Transactional layer).
├── common/                 # Cấu trúc dùng chung.
│   └── ApiResponse.java    # Class chuẩn hóa format JSON trả về cho mọi API.
└── util/                   # Các helper functions, static utilities.

src/main/resources/
├── db/migration/           # Các file SQL của Flyway (V1__...sql) quản lý schema versioning.
├── application.yaml        # Cấu hình gốc (DB, Mail, JWT Secret, Logging...).
├── application-dev.yaml    # Cấu hình override cho môi trường DEV.
└── application-prod.yaml   # Cấu hình override cho môi trường PROD.
```

---

## 4. Cấu Trúc Database (Schema Design)

Cơ sở dữ liệu được quản lý qua Flyway. Bao gồm 5 bảng chính, có đánh Index đầy đủ tại các foreign key để tối ưu query.

1. **`users` (Người dùng)**
   - Lưu thông tin tài khoản: `id`, `full_name`, `email` (UNIQUE), `phone_number` (UNIQUE), `password` (BCrypt hash).
   - Quyền hạn: `role` (USER hoặc ADMIN).
   - Quản lý trạng thái: `is_active` (Soft delete), `created_at`, `updated_at`.
2. **`categories` (Danh mục)**
   - `id`, `name` (UNIQUE), `created_at`.
3. **`products` (Sản phẩm)**
   - Thông tin: `name`, `description`, `price` (>= 0), `stock` (tồn kho >= 0).
   - Khóa ngoại: `category_id` trỏ về `categories`.
   - Trạng thái: `is_active` (Dùng để soft-delete thay vì xóa cứng).
4. **`orders` (Đơn hàng)**
   - Quản lý đơn: `user_id` (người đặt), `total_amount` (Tổng giá trị), `note`.
   - Trạng thái: `status` (PENDING, CONFIRMED, SHIPPED, DONE, CANCELLED).
5. **`order_items` (Chi tiết đơn hàng - Bảng trung gian)**
   - Liên kết N-N giữa `orders` và `products`.
   - Lưu trữ: `quantity` (số lượng), `unit_price` (giá chốt tại thời điểm mua, tránh việc đổi giá sau này làm sai lịch sử).

---

## 5. Flow Nghiệp Vụ Cốt Lõi (Business Workflows)

### 5.1. Luồng Xác Thực (Authentication Flow)
1. **Login:** Người dùng gửi `email` và `password` tới `AuthController`.
2. **Authenticate:** `AuthenticationManager` kiểm tra hash password thông qua `CustomUserDetailsService`.
3. **Token Generation:** Nếu thành công, `JwtProvider` tạo ra một Bearer Token (JWT) có chứa `username` và `roles` (cấu hình secret và thời gian hết hạn lấy từ `application.yaml`).
4. **API Access:** Client đính kèm JWT vào Header `Authorization: Bearer <token>` ở các request sau. `JwtAuthenticationFilter` sẽ trích xuất token, validate và nạp context vào `SecurityContextHolder`.

### 5.2. Luồng Mua Hàng (Order Creation & Inventory)
1. **Tiếp nhận Request:** Client truyền lên `userId` và danh sách các sản phẩm (`productId`, `quantity`).
2. **Khởi tạo:** Hệ thống tạo ra một `Order` mặc định với trạng thái `PENDING`.
3. **Check Tồn Kho (Critical Point):** 
   - Duyệt qua từng sản phẩm. Nếu `stock < quantity`, ném ra `InsufficientStockException` (rollback toàn bộ quá trình do `@Transactional`).
   - Nếu đủ, tiến hành trừ kho: `stock = stock - quantity`.
4. **Ghi nhận Giá:** Tạo các `OrderItemEntity`, ghi lại giá `unit_price` chính bằng `price` hiện hành của sản phẩm.
5. **Chốt Đơn:** Tính tổng `total_amount` từ các `OrderItem`, cập nhật lại vào `Order` và lưu xuống DB.

### 5.3. State Machine Đơn Hàng (Order Status Transition)
Việc thay đổi trạng thái đơn hàng bị ràng buộc chặt chẽ thông qua Map Validation tĩnh.
- **Luồng hợp lệ:** `PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DONE`.
- **Hủy đơn:** `PENDING` -> `CANCELLED` hoặc `CONFIRMED` -> `CANCELLED`.
- *Quy tắc:* Nếu truyền trạng thái nhảy cóc (vd: `PENDING` -> `DONE`), hệ thống văng `InvalidStatusTransitionException`.

---

## 6. Quy Chuẩn Kỹ Thuật Dành Cho AI & Developer (Guidelines)

Khi thêm mới tính năng, AI Agents và Developer cần tuân thủ tuyệt đối các nguyên tắc sau:
1. **Dữ liệu trả về (Response Format):** Mọi API endpoint đều phải bọc response bên trong class `ApiResponse<T>`. KHÔNG trả về Entity trực tiếp, KHÔNG trả về định dạng khác.
2. **Xử lý Exception:** Bất kỳ lỗi nghiệp vụ nào đều phải tạo Custom Exception (extends `RuntimeException`) và handle tập trung tại `GlobalExceptionHandler` để trả về HTTP status code phù hợp.
3. **Security Constraints:** Các API tạo mới / sửa / xóa thường dành cho ADMIN. Các API GET có thể Public hoặc yêu cầu USER. Cần chú ý cấu hình trong `SecurityConfig` thông qua phương thức `requestMatchers(...)`.
4. **Soft Delete:** Các Entity như `Product`, `User` không bao giờ bị xóa bằng câu lệnh DELETE (`repository.delete()`). Thay vào đó là vô hiệu hóa flag `isActive = false` (Soft delete).
5. **OpenAPI / Swagger:** Mọi Controller và DTO phải được gắn annotaion của SpringDoc (vd: `@Operation`, `@Schema`) để Swagger UI có thể tự động tạo document đầy đủ.
6. **Flyway DB:** Nếu cần thay đổi cấu trúc bảng, KHÔNG ĐƯỢC sửa file migration cũ (ví dụ `V1__init...`). PHẢI tạo file migration mới (vd: `V2__add_new_column.sql`) để Flyway tự đồng bộ hóa.

---
*Tài liệu này là la bàn để định hướng phát triển. Bất cứ khi nào tạo module mới, hãy đối chiếu với kiến trúc và luồng dữ liệu ở trên để đảm bảo tính đồng nhất cho toàn dự án.*
