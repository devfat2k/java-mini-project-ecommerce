# 📋 TỔNG QUAN DỰ ÁN — MINI ECOMMERCE

---

## 1. Thông tin dự án

| Mục                | Chi tiết                                                                                                                                                              |
| ------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Tên dự án**      | Mini Ecommerce                                                                                                                                                        |
| **Group ID**       | `com.devfat`                                                                                                                                                          |
| **Artifact ID**    | `mini-ecommerce`                                                                                                                                                      |
| **Mô tả**          | Hệ thống thương mại điện tử mini — bán hải sản trực tuyến, hỗ trợ quản lý sản phẩm, đơn hàng, thanh toán online (VNPay), xác thực OTP qua email, phân quyền RBAC động |
| **Ngôn ngữ**       | Java 21                                                                                                                                                               |
| **Framework**      | Spring Boot                                                                                                                                                           |
| **Build tool**     | Maven (Maven Wrapper — `mvnw`)                                                                                                                                        |
| **Database**       | PostgreSQL                                                                                                                                                            |
| **Caching**        | Redis                                                                                                                                                                 |
| **Object Storage** | MinIO (S3-compatible)                                                                                                                                                 |
| **Server Port**    | `8085` (cấu hình qua biến `PORT`)                                                                                                                                     |
| **API Base URL**   | `/api/v1/`                                                                                                                                                            |
| **Swagger UI**     | `/swagger-ui.html`                                                                                                                                                    |

---

## 2. Công nghệ sử dụng

### 2.1 Nền tảng & Ngôn ngữ

- **Java 21** — LTS, sử dụng record, pattern matching, switch expressions
- **Spring Boot** — Framework chính
- **Maven** — Quản lý dependency và build

### 2.2 Spring Ecosystem

| Module                   | Vai trò                                |
| ------------------------ | -------------------------------------- |
| **Spring Web (MVC)**     | REST API Controller                    |
| **Spring Data JPA**      | ORM, Repository pattern                |
| **Spring Security**      | Xác thực, phân quyền (JWT + RBAC)      |
| **Spring Validation**    | Validate request DTO (Bean Validation) |
| **Spring Cache**         | Abstraction layer cho caching          |
| **Spring Data Redis**    | Kết nối Redis cho cache + rate limit   |
| **Spring Mail**          | Gửi email (SMTP / Brevo API)           |
| **Spring Boot DevTools** | Hot reload khi phát triển              |
| **Spring Scheduling**    | Scheduler dọn dẹp OTP, payment hết hạn |
| **Spring AOP**           | Aspect cho Rate Limiting               |

### 2.3 Database & Migration

- **PostgreSQL** — RDBMS chính
- **Flyway** — Database migration, versioned schema management
- **HikariCP** — Connection pooling (default Spring Boot)

### 2.4 Bảo mật

- **JWT (JSON Web Token)** — Xác thực stateless (access token + refresh token)
- **BCrypt** — Hash password
- **SHA-256** — Hash refresh token, OTP
- **RBAC (Role-Based Access Control)** — Phân quyền động (Role ↔ Permission)
- **Spring Method Security** — `@PreAuthorize` bảo vệ endpoint

### 2.5 Thanh toán

- **VNPay Sandbox** — Cổng thanh toán online, xử lý IPN callback

### 2.6 Hạ tầng

- **Docker** — Containerize ứng dụng (multi-stage build)
- **Docker Compose** — Orchestrate các service (app, db, redis, minio)
- **GitHub Actions** — CI pipeline (PMD + test + build)

---

## 3. Thư viện đang sử dụng

| Thư viện                                  | Vai trò                                       |
| ----------------------------------------- | --------------------------------------------- |
| `spring-boot-starter-web`                 | REST API, embedded Tomcat                     |
| `spring-boot-starter-data-jpa`            | JPA/Hibernate ORM                             |
| `spring-boot-starter-security`            | Spring Security                               |
| `spring-boot-starter-validation`          | Bean Validation (Jakarta)                     |
| `spring-boot-starter-mail`                | Gửi email qua SMTP                            |
| `spring-boot-starter-cache`               | Abstraction caching                           |
| `spring-boot-starter-data-redis`          | Redis client (Lettuce)                        |
| `spring-boot-starter-test`                | Unit test (JUnit 5, Mockito)                  |
| `spring-boot-devtools`                    | Hot reload development                        |
| `flyway-core`                             | Database migration engine                     |
| `flyway-database-postgresql`              | Flyway adapter cho PostgreSQL                 |
| `postgresql`                              | JDBC driver cho PostgreSQL                    |
| `lombok`                                  | Giảm boilerplate code (getter/setter/builder) |
| `mapstruct`                               | Tự động generate mapper Entity ↔ DTO          |
| `lombok-mapstruct-binding`                | Tích hợp Lombok với MapStruct                 |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | Thư viện JWT (io.jsonwebtoken)                |
| `springdoc-openapi-starter-webmvc-ui`     | Swagger UI + OpenAPI 3 documentation          |
| `spring-dotenv`                           | Load biến môi trường từ file `.env`           |
| `minio`                                   | MinIO Java SDK (S3-compatible storage)        |
| `thumbnailator`                           | Resize/compress ảnh trước khi upload          |
| `bucket4j-core`                           | Token bucket algorithm cho rate limiting      |
| `bucket4j-redis`                          | Distributed rate limiting qua Redis           |

---

## 4. Cấu trúc thư mục (Chi tiết)

```
mini-ecommerce/
├── .github/
│   └── workflows/
│       └── ci.yml                          # GitHub Actions CI pipeline
│
├── .mvn/                                   # Maven Wrapper support files
│
├── docs/                                   # Tài liệu dự án
│   ├── PROJECT_OVERVIEW.md                 # File tổng quan dự án (file này)
│   ├── account_verification_otp_plan.md    # Kế hoạch kỹ thuật OTP verification
│   ├── auth_security_jwt_plan.md           # Kế hoạch kỹ thuật JWT + Security
│   ├── deploy_render_plan.md               # Kế hoạch deploy lên Render
│   └── plan_ky_thuat_tong_hop_v4.md        # Kế hoạch kỹ thuật tổng hợp
│
├── src/
│   ├── main/
│   │   ├── java/com/devfat/mini_ecommerce/
│   │   │   │
│   │   │   ├── MiniEcommerceApplication.java          # Entry point
│   │   │   │
│   │   │   ├── auth/                                  # ═══ MODULE AUTH ═══
│   │   │   │   ├── AuthController.java                # REST Controller xác thực
│   │   │   │   ├── AuthService.java                   # Interface service xác thực
│   │   │   │   ├── OtpPurpose.java                    # Enum mục đích OTP
│   │   │   │   ├── OtpService.java                    # Interface service OTP
│   │   │   │   ├── package-info.java                  # Module boundary marker
│   │   │   │   ├── dto/
│   │   │   │   │   ├── AuthResponseDto.java           # Response login (access + refresh token)
│   │   │   │   │   ├── ForgotPasswordRequestDto.java  # Request quên mật khẩu
│   │   │   │   │   ├── LoginRequestDto.java           # Request đăng nhập
│   │   │   │   │   ├── RefreshTokenRequestDto.java    # Request refresh token
│   │   │   │   │   ├── RefreshTokenResponseDto.java   # Response refresh token
│   │   │   │   │   ├── RegisterRequestDto.java        # Request đăng ký
│   │   │   │   │   ├── ResendOtpRequestDto.java       # Request gửi lại OTP
│   │   │   │   │   ├── ResendOtpResponseDto.java      # Response gửi lại OTP
│   │   │   │   │   ├── VerifyOtpRequestDto.java       # Request xác thực OTP
│   │   │   │   │   └── VerifyOtpResponseDto.java      # Response xác thực OTP
│   │   │   │   ├── exception/
│   │   │   │   │   ├── InvalidActionTokenException.java
│   │   │   │   │   ├── InvalidOtpException.java
│   │   │   │   │   ├── InvalidRefreshTokenException.java
│   │   │   │   │   ├── OtpExpiredException.java
│   │   │   │   │   └── ResendCooldownException.java
│   │   │   │   └── internal/                          # Triển khai nội bộ (không public API)
│   │   │   │       ├── AuthServiceImpl.java           # Triển khai AuthService
│   │   │   │       ├── OtpCleanupScheduler.java       # Scheduler dọn OTP hết hạn
│   │   │   │       ├── OtpServiceImpl.java            # Triển khai OtpService
│   │   │   │       ├── OtpVerificationEntity.java     # Entity bảng otp_verifications
│   │   │   │       ├── OtpVerificationRepository.java # Repository OTP
│   │   │   │       ├── RefreshTokenEntity.java        # Entity bảng refresh_tokens
│   │   │   │       ├── RefreshTokenGenerator.java     # Sinh refresh token ngẫu nhiên
│   │   │   │       └── RefreshTokenRepository.java    # Repository refresh token
│   │   │   │
│   │   │   ├── category/                              # ═══ MODULE CATEGORY ═══
│   │   │   │   ├── CategoryController.java            # REST Controller danh mục
│   │   │   │   ├── CategoryService.java               # Interface service danh mục
│   │   │   │   ├── package-info.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── CategoryResponseDto.java       # Response danh mục
│   │   │   │   │   └── CreateCategoryRequestDto.java  # Request tạo danh mục
│   │   │   │   └── internal/
│   │   │   │       ├── CategoryEntity.java            # Entity bảng categories
│   │   │   │       ├── CategoryMapper.java            # MapStruct mapper
│   │   │   │       ├── CategoryRepository.java        # JPA Repository
│   │   │   │       └── CategoryServiceImpl.java       # Triển khai CategoryService
│   │   │   │
│   │   │   ├── notification/                          # ═══ MODULE NOTIFICATION ═══
│   │   │   │   ├── EmailService.java                  # Interface service email
│   │   │   │   ├── package-info.java
│   │   │   │   ├── dto/
│   │   │   │   │   └── EmailRequestDto.java           # DTO yêu cầu gửi email
│   │   │   │   └── internal/
│   │   │   │       ├── BrevoMailTransport.java         # Gửi mail qua Brevo API
│   │   │   │       ├── EmailServiceImpl.java          # Triển khai EmailService
│   │   │   │       ├── EmailTemplateHelper.java       # Helper render template HTML
│   │   │   │       ├── MailTransport.java             # Interface abstraction gửi mail
│   │   │   │       └── SmtpMailTransport.java         # Gửi mail qua SMTP (Gmail)
│   │   │   │
│   │   │   ├── order/                                 # ═══ MODULE ORDER ═══
│   │   │   │   ├── OrderController.java               # REST Controller đơn hàng
│   │   │   │   ├── OrderService.java                  # Interface service đơn hàng
│   │   │   │   ├── OrderStatus.java                   # Enum trạng thái đơn hàng
│   │   │   │   ├── package-info.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── CreateOrderRequestDto.java     # Request tạo đơn hàng
│   │   │   │   │   ├── OrderItemRequestDto.java       # Request item trong đơn
│   │   │   │   │   ├── OrderItemResponseDto.java      # Response item trong đơn
│   │   │   │   │   ├── OrderResponseDto.java          # Response đơn hàng
│   │   │   │   │   └── UpdateOrderStatusRequestDto.java # Request cập nhật trạng thái
│   │   │   │   ├── exception/
│   │   │   │   │   └── InvalidStatusTransitionException.java
│   │   │   │   └── internal/
│   │   │   │       ├── OrderEntity.java               # Entity bảng orders
│   │   │   │       ├── OrderItemEntity.java           # Entity bảng order_items
│   │   │   │       ├── OrderItemMapper.java           # MapStruct mapper
│   │   │   │       ├── OrderMapper.java               # MapStruct mapper
│   │   │   │       ├── OrderRepository.java           # JPA Repository
│   │   │   │       └── OrderServiceImpl.java          # Triển khai OrderService
│   │   │   │
│   │   │   ├── payment/                               # ═══ MODULE PAYMENT ═══
│   │   │   │   ├── PaymentController.java             # REST Controller thanh toán
│   │   │   │   ├── PaymentMethod.java                 # Enum phương thức thanh toán
│   │   │   │   ├── PaymentProvider.java               # Enum nhà cung cấp thanh toán
│   │   │   │   ├── PaymentService.java                # Interface service thanh toán
│   │   │   │   ├── PaymentStatus.java                 # Enum trạng thái thanh toán
│   │   │   │   ├── package-info.java
│   │   │   │   ├── dto/
│   │   │   │   │   └── CreatePaymentResponseDto.java  # Response tạo thanh toán (URL)
│   │   │   │   └── internal/
│   │   │   │       ├── PaymentEntity.java             # Entity bảng payments
│   │   │   │       ├── PaymentMapper.java             # MapStruct mapper
│   │   │   │       ├── PaymentRepository.java         # JPA Repository
│   │   │   │       ├── PaymentServiceImpl.java        # Triển khai PaymentService
│   │   │   │       ├── VNPayConfig.java               # Cấu hình VNPay
│   │   │   │       └── VNPayUtil.java                 # Utility tạo HMAC, hash VNPay
│   │   │   │
│   │   │   ├── product/                               # ═══ MODULE PRODUCT ═══
│   │   │   │   ├── ProductController.java             # REST Controller sản phẩm
│   │   │   │   ├── ProductService.java                # Interface service sản phẩm
│   │   │   │   ├── package-info.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── CreateProductRequestDto.java   # Request tạo sản phẩm
│   │   │   │   │   ├── ProductResponseDto.java        # Response sản phẩm
│   │   │   │   │   └── UpdateProductRequestDto.java   # Request cập nhật sản phẩm
│   │   │   │   ├── exception/
│   │   │   │   │   └── InsufficientStockException.java
│   │   │   │   └── internal/
│   │   │   │       ├── ProductEntity.java             # Entity bảng products
│   │   │   │       ├── ProductMapper.java             # MapStruct mapper
│   │   │   │       ├── ProductRepository.java         # JPA Repository + analytics query
│   │   │   │       └── ProductServiceImpl.java        # Triển khai ProductService
│   │   │   │
│   │   │   ├── storage/                               # ═══ MODULE STORAGE ═══
│   │   │   │   ├── StorageService.java                # Interface service lưu trữ file
│   │   │   │   ├── package-info.java
│   │   │   │   └── internal/
│   │   │   │       ├── FileValidationUtil.java        # Validate file upload (type, size)
│   │   │   │       ├── MinioConfig.java               # Cấu hình MinIO client
│   │   │   │       ├── StorageServiceImpl.java        # Triển khai upload MinIO
│   │   │   │       └── TestUploadController.java      # Controller test upload
│   │   │   │
│   │   │   ├── user/                                  # ═══ MODULE USER ═══
│   │   │   │   ├── RbacAdminController.java           # REST Controller RBAC admin
│   │   │   │   ├── RbacAdminService.java              # Interface service RBAC
│   │   │   │   ├── Role.java                          # Enum vai trò (USER, ADMIN)
│   │   │   │   ├── UserController.java                # REST Controller người dùng
│   │   │   │   ├── UserService.java                   # Interface service người dùng
│   │   │   │   ├── package-info.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ChangePasswordRequestDto.java  # Request đổi mật khẩu
│   │   │   │   │   ├── CreateRoleRequestDto.java      # Request tạo role
│   │   │   │   │   ├── PermissionResponseDto.java     # Response permission
│   │   │   │   │   ├── RoleResponseDto.java           # Response role
│   │   │   │   │   ├── UpdateProfileRequestDto.java   # Request cập nhật profile
│   │   │   │   │   ├── UpdateRolePermissionsRequestDto.java # Request cập nhật quyền role
│   │   │   │   │   ├── UpdateUserRolesRequestDto.java # Request cập nhật role user
│   │   │   │   │   ├── UserPermissionCacheDto.java    # DTO cache quyền user
│   │   │   │   │   └── UserResponseDto.java           # Response thông tin user
│   │   │   │   └── internal/
│   │   │   │       ├── CustomUserDetailsService.java  # Load UserDetails từ DB
│   │   │   │       ├── PermissionEntity.java          # Entity bảng permissions
│   │   │   │       ├── PermissionRepository.java      # JPA Repository
│   │   │   │       ├── RbacAdminServiceImpl.java      # Triển khai RbacAdminService
│   │   │   │       ├── RoleEntity.java                # Entity bảng roles
│   │   │   │       ├── RoleRepository.java            # JPA Repository
│   │   │   │       ├── UserEntity.java                # Entity bảng users
│   │   │   │       ├── UserMapper.java                # MapStruct mapper
│   │   │   │       ├── UserPermissionCacheService.java # Cache quyền user trên Redis
│   │   │   │       ├── UserRepository.java            # JPA Repository
│   │   │   │       └── UserServiceImpl.java           # Triển khai UserService
│   │   │   │
│   │   │   └── shared/                                # ═══ MODULE SHARED ═══
│   │   │       ├── PingController.java                # Health check endpoint
│   │   │       ├── package-info.java
│   │   │       ├── base/
│   │   │       │   ├── ApiResponse.java               # Response wrapper chuẩn
│   │   │       │   ├── BaseEntity.java                # Auditing fields chung
│   │   │       │   ├── BasePageRequest.java           # Base pagination request
│   │   │       │   └── PageResponse.java              # Pagination response wrapper
│   │   │       ├── config/
│   │   │       │   ├── AsyncConfig.java               # Cấu hình thread pool async
│   │   │       │   ├── FlywayConfig.java              # Cấu hình Flyway migration
│   │   │       │   ├── OpenApiConfig.java             # Cấu hình Swagger/OpenAPI
│   │   │       │   ├── RedisCacheConfig.java          # Cấu hình Redis cache manager
│   │   │       │   ├── SecurityConfig.java            # Cấu hình Spring Security
│   │   │       │   └── WebConfig.java                 # Cấu hình CORS, argument resolver
│   │   │       ├── exception/
│   │   │       │   ├── AccountNotVerifiedException.java
│   │   │       │   ├── BadRequestException.java
│   │   │       │   ├── BusinessException.java         # Base exception (có HttpStatus)
│   │   │       │   ├── DuplicateResourceException.java
│   │   │       │   ├── GlobalExceptionHandler.java    # @RestControllerAdvice xử lý lỗi
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   └── TooManyRequestsException.java
│   │   │       ├── ratelimit/
│   │   │       │   ├── RateLimit.java                 # Custom annotation @RateLimit
│   │   │       │   ├── RateLimitConfig.java           # Bean cấu hình Bucket4j
│   │   │       │   ├── RateLimitProperties.java       # Properties binding rate limit
│   │   │       │   ├── RateLimitType.java             # Enum loại rate limit
│   │   │       │   ├── RateLimiterService.java        # Service xử lý rate limit
│   │   │       │   └── RateLimitingAspect.java        # AOP Aspect chặn request
│   │   │       ├── security/
│   │   │       │   ├── JwtAccessDeniedHandler.java    # Handler 403 Forbidden
│   │   │       │   ├── JwtAuthenticationEntryPoint.java # Handler 401 Unauthorized
│   │   │       │   ├── JwtAuthenticationFilter.java   # Filter xác thực JWT
│   │   │       │   ├── JwtProvider.java               # Sinh/validate/parse JWT
│   │   │       │   ├── SecurityAuditorAware.java      # Auditing — lấy email từ token
│   │   │       │   └── UserPrincipal.java             # Implements UserDetails
│   │   │       └── util/
│   │   │           └── CustomPageableArgumentResolver.java # Custom pageable resolver
│   │   │
│   │   └── resources/
│   │       ├── application.yaml                       # Config chung (chạy cả 2 profile)
│   │       ├── application-dev.yaml                   # Config development (debug, SMTP)
│   │       ├── application-prod.yaml                  # Config production (log level)
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init_mini_shop.sql          # Schema gốc (13 bảng + indexes)
│   │       │       └── V2__seed_seafood_categories_and_products.sql # Seed data + RBAC
│   │       ├── static/                                 # Static files (trống)
│   │       └── templates/
│   │           └── email/
│   │               ├── SendEmailTemplate.html          # Template email đơn hàng
│   │               └── otp-email-template.html         # Template email OTP
│   │
│   └── test/
│       └── java/                                       # Unit tests
│
├── .dockerignore                                       # Loại trừ file khi build Docker
├── .env.example                                        # Mẫu biến môi trường
├── .env                                                # Biến môi trường local (git-ignored)
├── .env.production                                     # Biến môi trường production (git-ignored)
├── .gitignore                                          # Git ignore rules
├── .gitattributes                                      # Git attributes
├── Dockerfile                                          # Multi-stage Docker build
├── docker-compose.yml                                  # Orchestrate app + db + redis + minio
├── pom.xml                                             # Maven POM (dependencies + plugins)
├── pmd-ruleset.xml                                     # PMD code quality rules
├── mvnw / mvnw.cmd                                     # Maven Wrapper (Linux/Windows)
├── HELP.md                                             # Spring Boot help reference
└── README.md                                           # README dự án
```

---

## 5. Chức năng trong dự án

### 5.1 Xác thực & Bảo mật (Auth)

- **Đăng ký tài khoản** — Validate input, hash password (BCrypt), gán role mặc định `USER`, gửi OTP xác thực email
- **Đăng nhập** — Xác thực email/password qua `AuthenticationManager`, kiểm tra email đã verified, sinh Access Token (JWT) + Refresh Token
- **Refresh Token** — Sinh lại Access Token từ Refresh Token hợp lệ (chưa revoke, chưa hết hạn)
- **Đăng xuất** — Revoke refresh token (set `revoked = true`)
- **Xác thực OTP** — Verify mã OTP cho 3 mục đích: xác thực đăng ký, reset password, xác nhận đổi password
- **Quên mật khẩu** — Gửi OTP về email, sau khi verify tạo Action Token có scope `RESET_PASSWORD`
- **Gửi lại OTP** — Gửi lại mã OTP với cooldown chống spam
- **JWT Authentication Filter** — Filter xác thực mọi request có Bearer token
- **Rate Limiting** — Giới hạn số request cho login (5 lần/15 phút), OTP (3 lần/5 phút), public API (60 lần/phút) — sử dụng Bucket4j + Redis

### 5.2 Quản lý người dùng (User)

- **Xem thông tin cá nhân** (`GET /me`) — Lấy profile user hiện tại từ JWT
- **Cập nhật profile** (`PATCH /me/update`) — Cập nhật thông tin cá nhân
- **Đổi mật khẩu** (`PATCH /password`) — Yêu cầu OTP xác nhận trước khi đổi
- **Upload avatar** (`POST /me/avatar`) — Upload ảnh qua MinIO, resize bằng Thumbnailator
- **Xem tất cả user** (Admin) — Phân trang danh sách user
- **Khóa/mở tài khoản** (Admin) — Bật/tắt trạng thái `isActive`

### 5.3 Phân quyền RBAC (Admin)

- **Xem tất cả Role** — Liệt kê role kèm danh sách permission
- **Tạo Role mới** — Tạo role tùy chỉnh
- **Xem tất cả Permission** — Liệt kê các quyền hệ thống
- **Cập nhật Permission cho Role** — Gán/cập nhật danh sách quyền cho role
- **Cập nhật Role cho User** — Gán/cập nhật role cho user cụ thể
- **Cache Permission trên Redis** — Cache `user-permissions` tự động evict khi thay đổi

### 5.4 Quản lý danh mục (Category)

- **Tạo danh mục** (Admin)
- **Xem danh mục** — Phân trang, tìm kiếm theo tên
- **Xem chi tiết danh mục** — Theo ID
- **Cập nhật danh mục** (Admin)
- **Xóa danh mục** (Admin)

### 5.5 Quản lý sản phẩm (Product)

- **Tạo sản phẩm** (Admin, cần quyền `product:create`)
- **Xem danh sách sản phẩm** — Public, phân trang, tìm kiếm theo tên, JOIN FETCH category
- **Xem chi tiết sản phẩm** — Theo ID
- **Cập nhật sản phẩm** (Admin, cần quyền `product:update`) — Partial update (PATCH)
- **Xóa sản phẩm** (Admin, cần quyền `product:delete`) — Soft delete (set `isActive = false`)
- **Tăng/giảm tồn kho** (Admin)
- **Upload ảnh sản phẩm** (Admin) — Upload ảnh qua MinIO
- **Top sản phẩm bán chạy** (Admin) — Analytics query
- **Doanh thu theo danh mục** (Admin) — Analytics query
- **Doanh thu theo tháng** (Admin) — Analytics query

### 5.6 Quản lý đơn hàng (Order)

- **Tạo đơn hàng** — Kiểm tra tồn kho (Optimistic Locking), tính tổng tiền, trừ stock tự động
- **Xem đơn hàng theo user** — User chỉ xem được đơn của mình, Admin xem tất cả
- **Xem chi tiết đơn hàng** — Bao gồm danh sách items
- **Cập nhật trạng thái đơn** (Admin) — State machine: `PENDING → CONFIRMED → SHIPPED → DONE` hoặc `→ CANCELLED`

### 5.7 Thanh toán (Payment)

- **Tạo thanh toán VNPay** — Sinh URL thanh toán VNPay sandbox, liên kết với đơn hàng
- **VNPay Return URL** — Xử lý redirect sau khi user thanh toán xong
- **VNPay IPN Callback** — Webhook nhận thông báo từ VNPay, cập nhật trạng thái payment

### 5.8 Gửi Email (Notification)

- **Gửi email text/HTML** — Hỗ trợ template HTML
- **Gửi email đính kèm file**
- **Email xác nhận đơn hàng** — Tự động gửi khi tạo đơn thành công
- **Email thanh toán thành công** — Tự động gửi khi VNPay callback thành công
- **Email OTP** — Gửi mã xác thực khi đăng ký, quên mật khẩu, đổi mật khẩu
- **Dual transport** — Hỗ trợ SMTP (Gmail) cho dev, Brevo API cho production
- **Async email** — Gửi email bất đồng bộ qua `emailTaskExecutor`

### 5.9 Lưu trữ file (Storage)

- **Upload file lên MinIO** — S3-compatible object storage
- **Validate file** — Kiểm tra loại file, kích thước (max 5MB)
- **Resize ảnh** — Tự động resize bằng Thumbnailator trước khi upload

### 5.10 Chức năng hệ thống

- **Health check** — `GET /api/v1/health` trả về "pong"
- **Scheduler dọn OTP** — Tự động xóa OTP hết hạn (cron configurable)
- **Scheduler payment hết hạn** — Tự động cập nhật payment quá hạn
- **Caching Redis** — Cache products (30 phút), categories (30 phút), analytics (5 phút), user-permissions
- **Global Exception Handler** — Xử lý tập trung tất cả exception
- **Audit Trail** — Tự động ghi `created_at`, `updated_at`, `created_by`, `updated_by` cho mọi entity

---

## 6. Các API đã hoàn thành

### 6.1 Auth APIs (`/api/v1/auth`) — Public

| Method | Endpoint           | Mô tả                            | Rate Limit           |
| ------ | ------------------ | -------------------------------- | -------------------- |
| `POST` | `/register`        | Đăng ký tài khoản mới            | —                    |
| `POST` | `/login`           | Đăng nhập                        | LOGIN (5 req/15 min) |
| `POST` | `/refresh-token`   | Làm mới access token             | —                    |
| `POST` | `/logout`          | Đăng xuất (revoke refresh token) | —                    |
| `POST` | `/verify-otp`      | Xác thực mã OTP                  | OTP (3 req/5 min)    |
| `POST` | `/forgot-password` | Yêu cầu OTP reset mật khẩu       | —                    |
| `POST` | `/resend-otp`      | Gửi lại mã OTP                   | OTP (3 req/5 min)    |

### 6.2 User APIs (`/api/v1/users`) — Authenticated

| Method  | Endpoint           | Mô tả                        | Quyền         |
| ------- | ------------------ | ---------------------------- | ------------- |
| `GET`   | `/me`              | Lấy thông tin cá nhân        | Authenticated |
| `PATCH` | `/me/update`       | Cập nhật profile             | Authenticated |
| `PATCH` | `/password`        | Đổi mật khẩu                 | Authenticated |
| `POST`  | `/me/avatar`       | Upload avatar (multipart)    | Authenticated |
| `GET`   | `/`                | Lấy tất cả user (phân trang) | ADMIN         |
| `PATCH` | `/{userId}/status` | Khóa/mở tài khoản            | ADMIN         |

### 6.3 RBAC Admin APIs (`/api/v1/admin/rbac`) — ADMIN only

| Method  | Endpoint                      | Mô tả                        |
| ------- | ----------------------------- | ---------------------------- |
| `GET`   | `/roles`                      | Lấy tất cả role              |
| `POST`  | `/roles`                      | Tạo role mới                 |
| `GET`   | `/permissions`                | Lấy tất cả permission        |
| `PATCH` | `/roles/{roleId}/permissions` | Cập nhật permission cho role |
| `PATCH` | `/users/{userId}/roles`       | Cập nhật role cho user       |

### 6.4 Category APIs (`/api/v1/categories`)

| Method   | Endpoint | Mô tả                             | Quyền  |
| -------- | -------- | --------------------------------- | ------ |
| `GET`    | `/`      | Lấy danh mục (phân trang, search) | Public |
| `GET`    | `/{id}`  | Lấy danh mục theo ID              | Public |
| `POST`   | `/`      | Tạo danh mục                      | ADMIN  |
| `PUT`    | `/{id}`  | Cập nhật danh mục                 | ADMIN  |
| `DELETE` | `/{id}`  | Xóa danh mục                      | ADMIN  |

### 6.5 Product APIs (`/api/v1/products`)

| Method   | Endpoint               | Mô tả                             | Quyền                    |
| -------- | ---------------------- | --------------------------------- | ------------------------ |
| `GET`    | `/`                    | Lấy sản phẩm (phân trang, search) | Public (rate limit)      |
| `GET`    | `/{id}`                | Lấy sản phẩm theo ID              | Authenticated            |
| `POST`   | `/`                    | Tạo sản phẩm                      | `product:create` / ADMIN |
| `PATCH`  | `/{id}`                | Cập nhật sản phẩm                 | `product:update` / ADMIN |
| `DELETE` | `/{id}`                | Soft delete sản phẩm              | `product:delete` / ADMIN |
| `PATCH`  | `/increase/{id}`       | Tăng tồn kho                      | ADMIN                    |
| `PATCH`  | `/decrease/{id}`       | Giảm tồn kho                      | ADMIN                    |
| `GET`    | `/top-buy`             | Top sản phẩm bán chạy             | ADMIN                    |
| `GET`    | `/revenue-by-category` | Doanh thu theo danh mục           | ADMIN                    |
| `GET`    | `/revenue-in-month`    | Doanh thu theo tháng              | ADMIN                    |
| `POST`   | `/{id}/image`          | Upload ảnh sản phẩm               | ADMIN                    |

### 6.6 Order APIs (`/api/v1/orders`) — Authenticated

| Method  | Endpoint         | Mô tả                   | Quyền                       |
| ------- | ---------------- | ----------------------- | --------------------------- |
| `POST`  | `/`              | Tạo đơn hàng            | Authenticated               |
| `GET`   | `/user/{userId}` | Lấy đơn hàng theo user  | Authenticated (owner/ADMIN) |
| `GET`   | `/{id}`          | Lấy chi tiết đơn hàng   | Authenticated (owner/ADMIN) |
| `PATCH` | `/{id}/status`   | Cập nhật trạng thái đơn | ADMIN                       |

### 6.7 Payment APIs (`/api/v1/payments`)

| Method | Endpoint            | Mô tả                   | Quyền         |
| ------ | ------------------- | ----------------------- | ------------- |
| `POST` | `/{orderId}/create` | Tạo thanh toán VNPay    | Authenticated |
| `GET`  | `/vnpay-return`     | VNPay redirect callback | Public        |
| `GET`  | `/vnpay-ipn`        | VNPay IPN webhook       | Public        |

### 6.8 Health API (`/api/v1/health`)

| Method | Endpoint | Mô tả                        | Quyền  |
| ------ | -------- | ---------------------------- | ------ |
| `GET`  | `/`      | Health check (trả về "pong") | Public |

---

## 7. Cấu trúc dữ liệu Database

### 7.1 Sơ đồ quan hệ (ER Diagram)

```
┌─────────────┐     N:N     ┌───────────┐     N:N     ┌──────────────┐
│   users     │─────────────│ user_roles│─────────────│    roles     │
│─────────────│             └───────────┘             │──────────────│
│ id (PK)     │                                       │ id (PK)      │
│ full_name   │                                       │ name         │
│ email (UQ)  │                                       │ description  │
│ phone (UQ)  │                                       └──────┬───────┘
│ password    │                                              │ N:N
│ is_active   │                                       ┌──────┴───────────┐
│ email_ver.  │                                       │ role_permissions  │
│ avatar_url  │                                       └──────┬───────────┘
└──────┬──────┘                                              │
       │ 1:N                                          ┌──────┴───────┐
       ├──────────────────────┐                       │ permissions  │
       │                      │                       │──────────────│
┌──────┴───────┐    ┌─────────┴──────────┐           │ id (PK)      │
│   orders     │    │ otp_verifications  │           │ code (UQ)    │
│──────────────│    │────────────────────│           │ description  │
│ id (PK)      │    │ id (PK)            │           └──────────────┘
│ user_id (FK) │    │ user_id (FK)       │
│ status       │    │ otp_hash           │
│ total_amount │    │ purpose            │
│ note         │    │ expires_at         │
│ version      │    │ attempts           │
└──────┬───────┘    │ consumed           │
       │ 1:N        └────────────────────┘
       │
┌──────┴──────────┐         ┌──────────────┐
│  order_items    │         │  categories  │
│─────────────────│         │──────────────│
│ id (PK)         │         │ id (PK)      │
│ order_id (FK)   │         │ name (UQ)    │
│ product_id (FK) │         └──────┬───────┘
│ quantity        │                │ 1:N
│ unit_price      │         ┌──────┴───────┐
└─────────────────┘         │  products    │
                            │──────────────│
┌──────────────────┐        │ id (PK)      │
│ refresh_tokens   │        │ name         │
│──────────────────│        │ description  │
│ id (PK)          │        │ price        │
│ user_id (FK)     │        │ stock        │
│ token_hash       │        │ category_id  │
│ expires_at       │        │ is_active    │
│ revoked          │        │ image_url    │
└──────────────────┘        │ version      │
                            └──────────────┘
┌──────────────┐
│  payments    │
│──────────────│
│ id (PK)      │
│ order_id(FK) │
│ amount       │
│ provider     │
│ payment_meth │
│ status       │
│ prov_txn_id  │
│ paid_at      │
└──────────────┘
```

### 7.2 Chi tiết các bảng

#### `users` — Bảng người dùng

| Cột              | Kiểu         | Ràng buộc               | Mô tả                     |
| ---------------- | ------------ | ----------------------- | ------------------------- |
| `id`             | BIGSERIAL    | PK                      | ID tự tăng                |
| `full_name`      | VARCHAR(100) | NOT NULL                | Họ tên                    |
| `email`          | VARCHAR(150) | UNIQUE, NOT NULL        | Email đăng nhập           |
| `phone_number`   | VARCHAR(15)  | UNIQUE, NOT NULL        | Số điện thoại             |
| `password`       | VARCHAR(255) | NOT NULL                | Mật khẩu đã hash (BCrypt) |
| `is_active`      | BOOLEAN      | NOT NULL, DEFAULT TRUE  | Trạng thái hoạt động      |
| `email_verified` | BOOLEAN      | NOT NULL, DEFAULT FALSE | Email đã xác thực chưa    |
| `avatar_url`     | VARCHAR(500) | —                       | URL ảnh đại diện          |
| `created_at`     | TIMESTAMP    | NOT NULL                | Ngày tạo                  |
| `updated_at`     | TIMESTAMP    | NOT NULL                | Ngày cập nhật             |
| `created_by`     | VARCHAR(150) | —                       | Người tạo                 |
| `updated_by`     | VARCHAR(150) | —                       | Người cập nhật            |

#### `roles` — Bảng vai trò

| Cột           | Kiểu         | Ràng buộc | Mô tả                  |
| ------------- | ------------ | --------- | ---------------------- |
| `id`          | BIGSERIAL    | PK        | ID tự tăng             |
| `name`        | VARCHAR(50)  | NOT NULL  | Tên role (ADMIN, USER) |
| `description` | VARCHAR(255) | —         | Mô tả role             |

#### `permissions` — Bảng quyền hệ thống

| Cột           | Kiểu         | Ràng buộc        | Mô tả                                          |
| ------------- | ------------ | ---------------- | ---------------------------------------------- |
| `id`          | BIGSERIAL    | PK               | ID tự tăng                                     |
| `code`        | VARCHAR(100) | UNIQUE, NOT NULL | Mã quyền (`product:create`, `order:manage`...) |
| `description` | VARCHAR(255) | —                | Mô tả quyền                                    |

#### `user_roles` — Bảng trung gian User ↔ Role (N:N)

| Cột       | Kiểu   | Ràng buộc          |
| --------- | ------ | ------------------ |
| `user_id` | BIGINT | FK → users(id), PK |
| `role_id` | BIGINT | FK → roles(id), PK |

#### `role_permissions` — Bảng trung gian Role ↔ Permission (N:N)

| Cột             | Kiểu   | Ràng buộc                |
| --------------- | ------ | ------------------------ |
| `role_id`       | BIGINT | FK → roles(id), PK       |
| `permission_id` | BIGINT | FK → permissions(id), PK |

#### `categories` — Bảng danh mục sản phẩm

| Cột    | Kiểu        | Ràng buộc        | Mô tả        |
| ------ | ----------- | ---------------- | ------------ |
| `id`   | BIGSERIAL   | PK               | ID tự tăng   |
| `name` | VARCHAR(50) | UNIQUE, NOT NULL | Tên danh mục |

#### `products` — Bảng sản phẩm

| Cột           | Kiểu          | Ràng buộc                      | Mô tả               |
| ------------- | ------------- | ------------------------------ | ------------------- |
| `id`          | BIGSERIAL     | PK                             | ID tự tăng          |
| `name`        | VARCHAR(150)  | NOT NULL                       | Tên sản phẩm        |
| `description` | TEXT          | —                              | Mô tả chi tiết      |
| `price`       | NUMERIC(12,2) | NOT NULL, CHECK ≥ 0            | Giá bán             |
| `stock`       | INTEGER       | NOT NULL, DEFAULT 0, CHECK ≥ 0 | Số lượng tồn kho    |
| `category_id` | BIGINT        | FK → categories(id)            | Danh mục            |
| `is_active`   | BOOLEAN       | NOT NULL, DEFAULT TRUE         | Trạng thái hiển thị |
| `image_url`   | VARCHAR(500)  | —                              | URL ảnh sản phẩm    |
| `version`     | INTEGER       | NOT NULL, DEFAULT 0            | Optimistic locking  |

#### `orders` — Bảng đơn hàng

| Cột            | Kiểu          | Ràng buộc                | Mô tả              |
| -------------- | ------------- | ------------------------ | ------------------ |
| `id`           | BIGSERIAL     | PK                       | ID tự tăng         |
| `user_id`      | BIGINT        | FK → users(id), NOT NULL | Người đặt hàng     |
| `status`       | VARCHAR(20)   | NOT NULL, CHECK IN (...) | Trạng thái đơn     |
| `total_amount` | NUMERIC(12,2) | NOT NULL, CHECK ≥ 0      | Tổng tiền          |
| `note`         | TEXT          | —                        | Ghi chú            |
| `version`      | INTEGER       | NOT NULL, DEFAULT 0      | Optimistic locking |

#### `order_items` — Bảng chi tiết đơn hàng

| Cột          | Kiểu          | Ràng buộc                          | Mô tả                     |
| ------------ | ------------- | ---------------------------------- | ------------------------- |
| `id`         | BIGSERIAL     | PK                                 | ID tự tăng                |
| `order_id`   | BIGINT        | FK → orders(id), ON DELETE CASCADE | Đơn hàng                  |
| `product_id` | BIGINT        | FK → products(id), NOT NULL        | Sản phẩm                  |
| `quantity`   | INTEGER       | NOT NULL, CHECK > 0                | Số lượng                  |
| `unit_price` | NUMERIC(12,2) | NOT NULL, CHECK ≥ 0                | Đơn giá tại thời điểm đặt |

#### `refresh_tokens` — Bảng refresh token

| Cột          | Kiểu         | Ràng buộc                | Mô tả                  |
| ------------ | ------------ | ------------------------ | ---------------------- |
| `id`         | BIGSERIAL    | PK                       | ID tự tăng             |
| `user_id`    | BIGINT       | FK → users(id), NOT NULL | User sở hữu            |
| `token_hash` | VARCHAR(255) | NOT NULL                 | SHA-256 hash của token |
| `expires_at` | TIMESTAMP    | NOT NULL                 | Thời điểm hết hạn      |
| `revoked`    | BOOLEAN      | NOT NULL, DEFAULT FALSE  | Đã thu hồi chưa        |

#### `payments` — Bảng thanh toán

| Cột                       | Kiểu          | Ràng buộc                 | Mô tả                            |
| ------------------------- | ------------- | ------------------------- | -------------------------------- |
| `id`                      | BIGSERIAL     | PK                        | ID tự tăng                       |
| `order_id`                | BIGINT        | FK → orders(id), NOT NULL | Đơn hàng                         |
| `amount`                  | NUMERIC(12,2) | NOT NULL, CHECK ≥ 0       | Số tiền                          |
| `provider`                | VARCHAR(20)   | NOT NULL, CHECK IN (...)  | Nhà cung cấp (VNPAY, MOMO...)    |
| `payment_method`          | VARCHAR(20)   | NOT NULL, CHECK IN (...)  | Phương thức (BANK, WALLET, CASH) |
| `status`                  | VARCHAR(20)   | NOT NULL, CHECK IN (...)  | Trạng thái (PENDING, SUCCESS...) |
| `provider_transaction_id` | VARCHAR(100)  | UNIQUE                    | Mã giao dịch bên thứ 3           |
| `paid_at`                 | TIMESTAMP     | —                         | Thời điểm thanh toán thành công  |

#### `otp_verifications` — Bảng mã OTP

| Cột          | Kiểu         | Ràng buộc                | Mô tả                |
| ------------ | ------------ | ------------------------ | -------------------- |
| `id`         | BIGSERIAL    | PK                       | ID tự tăng           |
| `user_id`    | BIGINT       | FK → users(id), NOT NULL | User                 |
| `otp_hash`   | VARCHAR(255) | NOT NULL                 | SHA-256 hash của OTP |
| `purpose`    | VARCHAR(150) | NOT NULL, CHECK IN (...) | Mục đích OTP         |
| `expires_at` | TIMESTAMP    | NOT NULL                 | Thời điểm hết hạn    |
| `attempts`   | INTEGER      | NOT NULL, DEFAULT 0      | Số lần thử sai       |
| `consumed`   | BOOLEAN      | NOT NULL, DEFAULT FALSE  | Đã sử dụng chưa      |

### 7.3 Database Indexes

| Index                           | Bảng              | Cột              | Mục đích                          |
| ------------------------------- | ----------------- | ---------------- | --------------------------------- |
| `idx_products_category`         | products          | category_id      | Truy vấn sản phẩm theo danh mục   |
| `idx_orders_user`               | orders            | user_id          | Truy vấn đơn hàng theo user       |
| `idx_orders_status`             | orders            | status           | Lọc đơn hàng theo trạng thái      |
| `idx_order_items_order`         | order_items       | order_id         | JOIN order ↔ items                |
| `idx_order_items_product`       | order_items       | product_id       | JOIN items ↔ product              |
| `idx_refresh_tokens_user_id`    | refresh_tokens    | user_id          | Truy vấn token theo user          |
| `idx_refresh_tokens_token_hash` | refresh_tokens    | token_hash       | Lookup token khi refresh/logout   |
| `idx_payments_order`            | payments          | order_id         | Truy vấn payment theo order       |
| `idx_payments_status`           | payments          | status           | Lọc payment theo trạng thái       |
| `idx_otp_user_purpose`          | otp_verifications | user_id, purpose | Truy vấn OTP theo user + mục đích |

### 7.4 Seed Data (V2 migration)

- **9 danh mục** hải sản: Cá biển, Tôm, Mực & Bạch tuộc, Cua & Ghẹ, Ốc & Nghêu Sò, Set hải sản văn phòng, Set hải sản nhậu, Hải sản khô, Nước mắm & Gia vị
- **36 sản phẩm** hải sản mẫu (4 sản phẩm/danh mục)
- **2 role**: ADMIN (10 quyền), USER (3 quyền: `product:read`, `order:create`, `order:read`)
- **10 permission**: `product:create`, `product:read`, `product:update`, `product:delete`, `category:manage`, `order:create`, `order:read`, `order:manage`, `user:manage`, `rbac:manage`

---

## 8. Các file cấu hình

### 8.1 `application.yaml` — Config chung

| Nhóm             | Cấu hình                                                                                 |
| ---------------- | ---------------------------------------------------------------------------------------- |
| **Datasource**   | PostgreSQL, HikariCP (pool size 10, timeout 30s)                                         |
| **JPA**          | `ddl-auto: validate` (Flyway quản lý schema), `format_sql: true`, `batch_fetch_size: 20` |
| **Flyway**       | Enabled, `baseline-on-migrate: true`                                                     |
| **Redis**        | Host, port, password qua biến env                                                        |
| **Multipart**    | Max file 5MB, max request 5MB                                                            |
| **Server**       | Port 8085                                                                                |
| **JWT**          | Secret key, expiration 1 giờ, refresh 7 ngày                                             |
| **MinIO**        | Endpoint, access key, secret key, bucket                                                 |
| **Mail (Brevo)** | API key, sender email                                                                    |
| **Rate Limit**   | Login (5/15min), OTP (3/5min), Public API (60/1min)                                      |
| **VNPay**        | Pay URL, TMN code, secret key, return/IPN URL                                            |
| **Scheduler**    | OTP cleanup cron, payment expired cron                                                   |
| **Swagger**      | API docs path, Swagger UI path                                                           |

### 8.2 `application-dev.yaml` — Config Development

- `show-sql: true` + SQL debug logging
- SMTP Gmail (host, port 587, STARTTLS)
- Flyway `repair-on-migrate: true`

### 8.3 `application-prod.yaml` — Config Production

- `show-sql: false`
- Log level: `root=INFO`, `hibernate.SQL=WARN`

### 8.4 `.env.example` — Biến môi trường mẫu

| Biến                                                          | Mô tả                           |
| ------------------------------------------------------------- | ------------------------------- |
| `DB_URL`                                                      | JDBC URL PostgreSQL             |
| `DB_USERNAME` / `DB_PASSWORD`                                 | Thông tin đăng nhập DB          |
| `REDIS_HOST` / `REDIS_PORT`                                   | Redis connection                |
| `MINIO_ENDPOINT`                                              | MinIO/S3 endpoint               |
| `MIN_IO_ACCESS_KEY` / `MIN_IO_SECRET_KEY` / `MIN_IO_BUCKET`   | MinIO credentials               |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP config                     |
| `BREVO_API_KEY`                                               | Brevo email API key             |
| `JWT_SECRET_KEY`                                              | Base64 encoded JWT secret       |
| `VNPAY_TMN_CODE` / `VNPAY_SECRET_KEY`                         | VNPay credentials               |
| `VNPAY_PAY_URL` / `VNPAY_RETURN_URL` / `VNPAY_IPN_URL`        | VNPay URLs                      |
| `CORS_ALLOWED_ORIGINS`                                        | Danh sách origin được phép CORS |

### 8.5 `Dockerfile` — Multi-stage build

- **Stage 1 (Builder)**: `eclipse-temurin:21-jdk-alpine` → cài dependency → build `.jar`
- **Stage 2 (Runtime)**: `eclipse-temurin:21-jre-alpine` → copy `.jar` → expose 8085 → run
- Tách dependency layer để tận dụng Docker cache

### 8.6 `docker-compose.yml` — Orchestrate services

| Service | Image               | Port                 |
| ------- | ------------------- | -------------------- |
| `app`   | Build từ Dockerfile | 8085:8085            |
| `db`    | postgres:16         | 5432:5432            |
| `redis` | redis:latest        | 6379:6379            |
| `minio` | quay.io/minio/minio | 9000:9000, 9001:9001 |

### 8.7 `ci.yml` — GitHub Actions CI Pipeline

- **Trigger**: Push/PR vào `main`
- **Steps**: Checkout → Setup JDK 21 → PMD check → Run tests → Build

### 8.8 `pmd-ruleset.xml` — Code Quality Rules

- `UnusedLocalVariable` — Biến local không dùng
- `UnusedPrivateField` — Field private không dùng
- `UnusedPrivateMethod` — Method private không dùng
- `UnusedFormalParameter` — Parameter không dùng
- `EmptyCatchBlock` — Catch block rỗng

---

## 9. Kiến trúc & Design Pattern

### 9.1 Package Structure — Package-by-Feature

Dự án tổ chức theo **feature module** (không phải layer), mỗi module có:

- **Public API**: Controller, Service interface, DTO, enum — nằm ở root package
- **Internal**: Entity, Repository, ServiceImpl, Mapper — nằm trong sub-package `internal/`
- **`package-info.java`**: Đánh dấu module boundary

### 9.2 Design Pattern được áp dụng

| Pattern                   | Áp dụng                                            |
| ------------------------- | -------------------------------------------------- |
| **Repository Pattern**    | JPA Repository cho mỗi entity                      |
| **Service Layer**         | Interface + Impl tách biệt                         |
| **DTO Pattern**           | Request/Response DTO tách khỏi Entity              |
| **Mapper Pattern**        | MapStruct tự động generate mapper                  |
| **Builder Pattern**       | Lombok `@Builder` cho entity và DTO                |
| **Strategy Pattern**      | `MailTransport` interface với 2 impl (SMTP, Brevo) |
| **Template Method**       | BaseEntity chung audit fields                      |
| **Aspect-Oriented (AOP)** | Rate limiting qua `@RateLimit` annotation          |
| **Token Bucket**          | Bucket4j cho distributed rate limiting             |
| **Optimistic Locking**    | `@Version` trên Product và Order entity            |

### 9.3 Security Flow

```
Request → JwtAuthenticationFilter → SecurityFilterChain → Controller
                  │
                  ├── Đọc Authorization header
                  ├── Validate JWT (JwtProvider)
                  ├── Load UserDetails (CustomUserDetailsService)
                  └── Set Authentication vào SecurityContext
```

### 9.4 Cấu trúc Response chuẩn

```json
{
    "success": true,
    "message": "Get Product Success",
    "data": { ... },
    "timestamp": "2026-08-09T06:00:00"
}
```

### 9.5 Pagination Response

```json
{
    "success": true,
    "data": {
        "content": [...],
        "page": 0,
        "size": 10,
        "totalElements": 36,
        "totalPages": 4,
        "last": false
    }
}
```

---

## 10. Enum Values

### OrderStatus

| Giá trị     | Mô tả                     |
| ----------- | ------------------------- |
| `PENDING`   | Đơn mới tạo, chờ xác nhận |
| `CONFIRMED` | Đã xác nhận               |
| `SHIPPED`   | Đang giao hàng            |
| `DONE`      | Hoàn thành                |
| `CANCELLED` | Đã hủy                    |

### PaymentStatus

| Giá trị   | Mô tả                 |
| --------- | --------------------- |
| `PENDING` | Chờ thanh toán        |
| `SUCCESS` | Thanh toán thành công |
| `FAILED`  | Thanh toán thất bại   |
| `EXPIRED` | Hết hạn thanh toán    |

### PaymentProvider

`VNPAY` · `MOMO` · `ZALOPAY` · `ACB` · `VCB`

### PaymentMethod

`BANK` · `WALLET` · `CASH`

### OtpPurpose

| Giá trị                        | Mô tả                      |
| ------------------------------ | -------------------------- |
| `REGISTER_VERIFICATION`        | Xác thực email khi đăng ký |
| `RESET_PASSWORD`               | Reset mật khẩu             |
| `CHANGE_PASSWORD_CONFIRMATION` | Xác nhận đổi mật khẩu      |

### RateLimitType

| Giá trị       | Capacity | Refill    | Duration |
| ------------- | -------- | --------- | -------- |
| `LOGIN`       | 5        | 5 tokens  | 15 phút  |
| `OTP`         | 3        | 3 tokens  | 5 phút   |
| `PUBLIC_API`  | 60       | 60 tokens | 1 phút   |
| `USER_ACTION` | 30       | 30 tokens | 1 phút   |

### Role

`USER` · `ADMIN`

---

## 11. Tổng số file Java trong dự án

| Module          | Số file                                                      |
| --------------- | ------------------------------------------------------------ |
| `auth`          | 19 file (controller, service, DTOs, exceptions, internal)    |
| `category`      | 6 file                                                       |
| `notification`  | 7 file                                                       |
| `order`         | 13 file                                                      |
| `payment`       | 11 file                                                      |
| `product`       | 10 file                                                      |
| `storage`       | 5 file                                                       |
| `user`          | 18 file                                                      |
| `shared`        | 20 file (base, config, exception, ratelimit, security, util) |
| **Entry point** | 1 file                                                       |
| **Tổng cộng**   | **~110 file Java**                                           |

---

## 12. Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) xử lý tập trung:

| Exception                                 | HTTP Status       | Mô tả                              |
| ----------------------------------------- | ----------------- | ---------------------------------- |
| `BusinessException`                       | Tùy theo subclass | Base exception nghiệp vụ           |
| `ResourceNotFoundException`               | 404               | Không tìm thấy resource            |
| `DuplicateResourceException`              | 409               | Resource đã tồn tại                |
| `BadRequestException`                     | 400               | Request không hợp lệ               |
| `AccountNotVerifiedException`             | 403               | Tài khoản chưa xác thực email      |
| `TooManyRequestsException`                | 429               | Vượt quá rate limit                |
| `InsufficientStockException`              | 400               | Không đủ tồn kho                   |
| `InvalidStatusTransitionException`        | 400               | Chuyển trạng thái đơn không hợp lệ |
| `InvalidRefreshTokenException`            | 401               | Refresh token không hợp lệ         |
| `InvalidOtpException`                     | 400               | OTP sai                            |
| `OtpExpiredException`                     | 400               | OTP hết hạn                        |
| `ResendCooldownException`                 | 429               | Gửi lại OTP quá nhanh              |
| `InvalidActionTokenException`             | 401               | Action token scope không khớp      |
| `BadCredentialsException`                 | 401               | Sai email/password                 |
| `DisabledException`                       | 403               | Tài khoản bị khóa                  |
| `AccessDeniedException`                   | 403               | Không có quyền truy cập            |
| `ObjectOptimisticLockingFailureException` | 409               | Xung đột concurrent update         |
| `MaxUploadSizeExceededException`          | 400               | File upload quá lớn                |
| `MethodArgumentNotValidException`         | 400               | Validation thất bại (trả map lỗi)  |
| `DataIntegrityViolationException`         | 409               | Vi phạm ràng buộc DB               |
| `Exception` (fallback)                    | 500               | Lỗi không xác định                 |

---

## 13. Caching Strategy

| Cache Name         | TTL               | Mô tả                                                 |
| ------------------ | ----------------- | ----------------------------------------------------- |
| `products`         | 30 phút           | Cache danh sách sản phẩm                              |
| `categories`       | 30 phút           | Cache danh sách danh mục                              |
| `analytics`        | 5 phút            | Cache dữ liệu thống kê                                |
| `user-permissions` | 30 phút (default) | Cache quyền user — evict khi cập nhật role/permission |

---
