# 📋 TỔNG QUAN DỰ ÁN — MINI E-COMMERCE BACKEND API

> **Phiên bản hệ thống**: v1.2.0 (Production-Ready Backend API)  
> **Kiến trúc**: Clean Layered Architecture (Stateless RESTful Web Service)  
> **Môi trường**: Java 21 (LTS), Spring Boot 3.5.16, PostgreSQL 16, Redis 7, MinIO S3  

---

## 1. 📌 TỔNG QUAN HỆ THỐNG

**Mini E-Commerce Backend** là hệ thống Backend phục vụ cho nền tảng thương mại điện tử chuyên kinh doanh **hải sản tươi sống & đồ tiệc chế biến sẵn**. Hệ thống được thiết kế theo tiêu chuẩn RESTful API hiện đại, phân lớp rõ ràng, áp dụng các Best Practices về bảo mật (JWT Dual-Token, RBAC động), tối ưu truy vấn cơ sở dữ liệu (Avoid N+1, Composite Indexing, JPA Specification Criteria Engine), hạ tầng Caching đa tầng (Redis 7) và lưu trữ tệp tin đám mây tương thích S3 (MinIO).

### 🏆 Mục Tiêu Kỹ Thuật Đạt Được
- **Xác thực & Bảo mật**: Dual-Token System (Access Token 1h, Opaque Refresh Token 7 ngày mã hóa SHA-256), OTP 6 chữ số qua Email (Brevo SMTP API) với Rate Limiting (Token Bucket qua Redis).
- **Phân quyền Động (RBAC)**: Hệ thống `Users` ↔ `Roles` ↔ `Permissions` linh hoạt với 18+ mã quyền chuẩn enterprise (`product:create`, `order:update_status`, `rbac:manage`...).
- **Cơ chế Tìm kiếm & Lọc Động (Search & Filter Engine)**: Tích hợp JPA Criteria API (`ProductSpecification`) cho phép lọc kết hợp nhiều điều kiện (tên sản phẩm, danh mục đa tùy chọn, khoảng giá `minPrice` - `maxPrice`, còn hàng `inStock`) kèm phân trang & sắp xếp an toàn qua Whitelist.
- **Home Content Management & Orchestration**: Aggregate API `GET /api/v1/home` tổng hợp 8 danh mục nội dung trang chủ, ứng dụng Spring AOP Cache Helper phân vùng Redis TTL chuyên biệt.
- **Thanh toán & State Machine đơn hàng**: Tích hợp thanh toán VNPay Sandbox (chữ ký số HMAC-SHA512, Idempotency IPN Webhook) kết hợp Cron Scheduler dọn dẹp đơn hết hạn tự động.
- **Object Storage & Media Security**: Tích hợp MinIO SDK, kiểm tra định dạng ảnh bằng **Magic Bytes Header Validation** (chống giả mạo tập tin), tự động nén & resize 800x800 chuẩn thương mại điện tử.

---

## 2. 🛠️ ĐỘI NGŨ CÔNG NGHỆ (TECH STACK & DEPENDENCIES)

```
                               ┌────────────────────────────────────────────────────────┐
                               │                 CLIENT APPLICATION (FE)                │
                               └───────────────────────────┬────────────────────────────┘
                                                           │ HTTP / RESTful API (JSON)
                                                           ▼
                               ┌────────────────────────────────────────────────────────┐
                               │         SPRING SECURITY & JWT FILTER (STATELESS)       │
                               └───────────────────────────┬────────────────────────────┘
                                                           │
                                                           ▼
                               ┌────────────────────────────────────────────────────────┐
                               │           CONTROLLER LAYER (16 REST CONTROLLERS)       │
                               └───────────────────────────┬────────────────────────────┘
                                                           │ DTO Records / Validation
                                                           ▼
                               ┌────────────────────────────────────────────────────────┐
                               │            SERVICE LAYER & CACHE HELPER                │
                               │     (Business Logic, State Machine, Image Processing)  │
                               └─────────┬─────────────────┬──────────────────┬─────────┘
                                         │                 │                  │
                         MinIO Java SDK  │   Spring Cache  │  Spring Data JPA │ Redis RateLimit
                                         ▼                 ▼                  ▼
                               ┌─────────────────┐ ┌──────────────┐ ┌───────────────────┐
                               │  MinIO Storage  │ │   Redis 7    │ │   PostgreSQL 16   │
                               │(S3 Upload Bucket│ │(8 Home Keys) │ │(Flyway V1, V2, V3)│
                               └─────────────────┘ └──────────────┘ └───────────────────┘
```

| Phân Loại | Công Nghệ / Thư Viện | Phiên Bản | Vai Trò & Mục Đích Kỹ Thuật |
|---|---|:---:|---|
| **Core Platform** | Java | 21 LTS | Ngôn ngữ chính (Sử dụng Record DTOs, Pattern Matching, Sealed Classes). |
| | Spring Boot | 3.5.16 | Core Framework, Dependency Injection, Auto-configuration. |
| **Security & Auth** | Spring Security | 6.x | Tích hợp Stateless Authentication, Phân quyền Role & Permission qua `@PreAuthorize`. |
| | JJWT (io.jsonwebtoken) | 0.12.6 | Tạo, mã hóa RSA/HMAC và giải mã Access Tokens. |
| **Database & Migration** | PostgreSQL | 16 | RDBMS chính lưu trữ quan hệ người dùng, sản phẩm, đơn hàng, banner. |
| | Flyway Migration | 10.x | Quản lý phiên bản Schema tự động (V1 Baseline, V2 RBAC Data, V3 Home Tables). |
| | Spring Data JPA | (Hibernate) | ORM Engine, JPA Criteria API Engine, Entity Graph JOIN FETCH. |
| **Caching & Rate Limit** | Redis | 7.x | Caching đa vùng (`home:*`, `products`, `categories`), Distributed Rate Limiting. |
| | Bucket4j | 8.10.1 | Thuật toán Token Bucket giới hạn tần suất gửi request OTP & Search. |
| **Storage & Image** | MinIO Java SDK | 8.5.17 | Object Storage chuẩn S3 lưu trữ avatar người dùng, ảnh sản phẩm, ảnh banner. |
| | Thumbnailator | 0.4.20 | Tự động resize (800x800) và nén định dạng JPEG/PNG trước khi lưu MinIO. |
| **Payment Gateway** | VNPay Payment API | Custom | Cổng thanh toán trực tuyến qua Chữ ký số HMAC-SHA512 & IPN Callback. |
| **Communication** | Spring Mail | 3.5.16 | Gửi mã OTP xác thực qua Email (SMTP / Brevo API Integration). |
| **Documentation** | Springdoc OpenAPI | 2.7.0 | Tự động sinh giao diện thử nghiệm Swagger UI (`/swagger-ui.html`). |

---

## 3. 🏛️ CẤU TRÚC GÓI MÃ NGUỒN (PACKAGE ARCHITECTURE)

Mã nguồn dự án được tổ chức theo từng **Feature Package (Feature-First Clean Architecture)** nhằm tăng tính độc lập và dễ mở rộng:

```text
src/main/java/com/devfat/mini_ecommerce/
├── auth/                       # Module Xác thực & Đăng nhập (Auth, OTP, Password Reset)
│   ├── dto/                    # Request/Response Records cho Auth
│   └── internal/               # Service Implementation & Tokens Management
├── user/                       # Module Người dùng, Sổ địa chỉ & Phân quyền RBAC
│   ├── address/                # Quản lý địa chỉ giao hàng
│   ├── dto/                    # User Profile & Address DTOs
│   └── internal/               # User Entity, Address Entity, Role/Permission Services
├── category/                   # Module Danh mục sản phẩm (Public & Admin Config)
│   ├── dto/
│   └── internal/               # Category Entity & Category Repositories
├── product/                    # Module Sản phẩm & Tìm kiếm động JPA Criteria
│   ├── dto/                    # Product Search Criteria & Product DTOs
│   └── internal/               # Product Entity, Specification Engine, Repositories
├── home/                       # Module Trang chủ & Content Orchestration
│   ├── dailyarrival/           # Sub-module Hải sản cập bến theo ngày (Public & Admin)
│   ├── herobanner/             # Sub-module Hero Banner Slides (Public & Admin)
│   ├── dto/                    # 10 Record DTOs tổng hợp 8 sections trang chủ
│   └── internal/               # HomeCacheHelper, HomeServiceImpl, HomeMapper
├── order/                      # Module Đơn hàng & State Machine
│   ├── dto/
│   └── internal/               # Order Entity, OrderItem Entity, Repositories
├── payment/                    # Module Thanh toán VNPay (Sandbox Integration)
│   ├── dto/
│   └── internal/               # VNPay Service Implementation, IPN Handler
├── storage/                    # Module Storage MinIO & Magic Bytes Validation
└── shared/                     # Shared Core Utilities & Configurations
    ├── base/                   # ApiResponse<T>, PageResponse<T>, BaseEntity
    ├── config/                 # SecurityConfig, RedisCacheConfig, SwaggerConfig...
    ├── exception/              # GlobalExceptionHandler, Custom Exceptions
    └── security/               # CustomUserDetailsService, JwtProvider, RbacEvaluator
```

---

## 4. 🗄️ MÔ HÌNH DỮ LIỆU & SCHEMAS (DATABASE MIGRATIONS)

Cơ sở dữ liệu được quản lý 100% bằng **Flyway Versioned Migrations**, tuân thủ nguyên tắc **Zero ALTER TABLE** trên các bảng baseline:

```
                  ┌─────────────────────────────────────────────────────────┐
                  │           V1__init_mini_shop.sql (BASELINE)             │
                  └────────────────────────────┬────────────────────────────┘
                                               │
             ┌─────────────────────────────────┼─────────────────────────────────┐
             ▼                                 ▼                                 ▼
      ┌─────────────┐                   ┌─────────────┐                   ┌─────────────┐
      │    users    │                   │ categories  │                   │  products   │
      └──────┬──────┘                   └──────┬──────┘                   └──────┬──────┘
             │ 1:N                             │ 1:N                             │ 1:N
             ▼                                 ▼                                 ▼
┌─────────────────────────┐             ┌─────────────┐             ┌─────────────────────────┐
│     user_addresses      │             │  products   │             │       order_items       │
└─────────────────────────┘             └──────┬──────┘             └────────────▲────────────┘
                                               │ 1:N                             │ N:1
                                               ▼                                 │
                                    ┌────────────────────┐             ┌─────────┴──────────┐
                                    │   daily_arrivals   │             │       orders       │
                                    │    (Added in V3)   │             └────────────────────┘
                                    └────────────────────┘
```

### 📜 Chi Tiết Bảng Dữ Liệu Trong DB:
1. `users`: Thông tin tài khoản người dùng, email, mật khẩu BCrypt, avatar, trạng thái kích hoạt, xác minh email.
2. `roles` & `permissions`: Phân quyền RBAC động. Bảng trung gian `user_roles` và `role_permissions`.
3. `user_addresses`: Sổ địa chỉ giao hàng của người dùng (Tỉnh/Thành, Quận/Huyện, Phường/Xã, Chi tiết, Mặc định).
4. `categories`: Danh mục sản phẩm kèm cấu hình hiển thị trang chủ (`home_display_style`, `home_sort_order`, `home_is_active`).
5. `products`: Sản phẩm với đầy đủ thuộc tính thương mại (`price`, `original_price`, `stock`, `unit`, `tags`, `is_featured`, `product_type`, `combo_*`, `average_rating`, `review_count`).
6. `hero_banners` *(V3)*: Banner slide trang chủ (23 thuộc tính chi tiết: tiêu đề, phụ đề, CTA, badge, giá khuyến mãi).
7. `daily_arrivals` *(V3)*: Danh sách hải sản cập bến theo ngày (Khóa UNIQUE `product_id` + `arrival_date`).
8. `orders` & `order_items`: Đơn hàng, ảnh chụp địa chỉ giao hàng, phương thức thanh toán, trạng thái đơn (`PENDING`, `CONFIRMED`, `SHIPPED`, `DONE`, `CANCELLED`).
9. `payments`: Giao dịch thanh toán online (VNPay provider, mã giao dịch, số tiền, trạng thái).
10. `refresh_tokens`: Token làm mới đã mã hóa SHA-256, thời gian hết hạn, trạng thái thu hồi.

---

## 5. 💾 THIẾT KẾ CACHING & REDIS DOMAIN REGIONS

Hệ thống cấu hình 1 Bean `RedisCacheManager` tập trung với `GenericJackson2JsonRedisSerializer` hỗ trợ `JavaTimeModule` & `NON_FINAL` class typing. Mọi tệp dữ liệu lưu trong Redis đều ở dạng JSON chuẩn.

| Cache Key | TTL | Mục Đích Sử Dụng | Phương Thức Xóa Cache (Eviction) |
|---|:---:|---|---|
| `home:heroSlides` | 10 phút | Danh sách Banner Slide trang chủ đang kích hoạt. | Admin sửa/xóa/toggle banner hoặc gọi Evict API. |
| `home:categories` | 10 phút | Danh mục hiển thị trang chủ. | Admin thay đổi cấu hình danh mục hoặc gọi Evict API. |
| `home:dailyArrivals` | 6 giờ | Hải sản cập bến theo ngày hiện tại. | Tự động hết hạn theo ngày hoặc gọi Evict API. |
| `home:featuredProducts` | 5 phút | Danh sách sản phẩm nổi bật (`isFeatured = true`). | Admin sửa thông tin sản phẩm hoặc gọi Evict API. |
| `home:featuredProductTabs` | 10 phút | Các tab phân loại sản phẩm nổi bật. | Gọi Evict API. |
| `home:comboSets` | 10 phút | Danh sách Combo tiệc / ăn trưa (`productType = COMBO`). | Admin cập nhật sản phẩm combo hoặc gọi Evict API. |
| `home:stats` | 30 phút | Thống kê số đơn hàng đã giao thành công (`OrderStatus.DONE`). | Tự động làm mới theo TTL. |
| `products` | 30 phút | Chi tiết thông tin 1 sản phẩm theo `id`. | Sửa/xóa sản phẩm (`@CacheEvict(key = "#id")`). |
| `categories` | 30 phút | Danh sách tất cả danh mục hệ thống. | Sửa/xóa/tạo danh mục (`@CacheEvict(allEntries = true)`). |

---

## 6. 🚦 RATE LIMITING & SECURITY SPECIFICATION

### 🔒 Rate Limiting Configuration (Token Bucket via Bucket4j)
- **Endpoint Gửi OTP**: Giới hạn **3 requests / 1 phút / IP** (Tránh spam gửi mail SMS/SMTP).
- **Endpoint Tìm kiếm Sản phẩm**: Giới hạn **60 requests / 1 phút / IP** (Chống DDoS / Crawl dữ liệu).

### 🛡️ Cross-Origin Resource Sharing (CORS) & Headers
- Hỗ trợ Frontend các domain local (`http://localhost:3000`, `http://localhost:5173`) và production domain.
- Trả về tiêu đề HTTP `Cache-Control` cho các public GET API.

---

## 7. 📈 TRẠNG THÁI HIỆN TẠI & HƯỚNG PHÁT TRIỂN

| Module | Trạng Thái | Mô Tả |
|---|:---:|---|
| **Auth & Security** | 🟢 **100% DONE** | Đăng ký, Đăng nhập Dual-Token, OTP Mail, Quên mật khẩu, RBAC. |
| **Product Search & Filter** | 🟢 **100% DONE** | Dynamic Criteria API, Phân trang, Whitelist Sắp xếp, Native Composite Indexes. |
| **Home Module** | 🟢 **100% DONE** | Aggregate `GET /api/v1/home`, Admin Hero Banner CRUD, Admin Daily Arrival CRUD, Evict Cache API. |
| **Category & Product Admin** | 🟢 **100% DONE** | CRUD Danh mục/Sản phẩm, Upload ảnh MinIO, Dynamic Validation. |
| **Order & Payment** | 🟢 **100% DONE** | Đặt hàng, VNPay Payment Sandbox Integration, Auto-cancel Scheduler. |
| **Review System** | 🟡 **Tách rời** | Thuộc phạm vi nâng cấp tiếp theo của Module Product (chưa bao gồm trong Home). |
