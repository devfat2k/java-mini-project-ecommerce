<div align="center">

# 🛒 Mini E-Commerce Backend API

> **A modern, high-performance, enterprise-ready RESTful E-Commerce Backend service built with Java 21 & Spring Boot 3.5.**  
> Designed following **Clean Layered Architecture**, **Production Best Practices**, **DevOps Standards**, and **State-of-the-Art Caching/Security Systems**.

[![Java](https://img.shields.io/badge/Java-21_LTS-orange?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=for-the-badge&logo=redis)](https://redis.io/)
[![MinIO](https://img.shields.io/badge/MinIO-S3%20Compatible-C72C48?style=for-the-badge&logo=minio)](https://min.io/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com/)

[📖 Features](#-tính-năng-nổi-bật--key-features) • [🏛 Architecture](#-kiến-trúc--tech-stack) • [📂 Structure](#-cấu-trúc-thư-mục-project-structure) • [🌐 API Docs](#-danh-sách-api-endpoints) • [🚀 Quick Start](#-hướng-dẫn-cài-đặt--chạy-dự-án)

---

</div>

## 📌 Tổng Quan Dự Án (Project Overview)

**Mini E-Commerce Backend** là hệ thống xử lý nghiệp vụ kinh doanh thương mại điện tử chuyên nghiệp (**Chuyên hải sản tươi sống & đồ tiệc chế biến sẵn**), xây dựng chuẩn **RESTful JSON API**, hoàn toàn **Stateless** và áp dụng kiến trúc phân lớp sạch sẽ (**Clean Layered Architecture**).

Hệ thống được phát triển nhằm đáp ứng đầy đủ các yêu cầu khắt khe của ứng dụng E-Commerce doanh nghiệp:
- **Xác thực Đa tầng & RBAC Động:** Dual Token (Access Token 1h, Opaque Refresh Token 7 ngày mã hóa SHA-256) + Phân quyền Role & Permission linh hoạt với `@PreAuthorize`.
- **Search & Filter Engine Động:** Sử dụng JPA Criteria API (`ProductSpecification`) hỗ trợ lọc đa tiêu chí (tên sản phẩm, danh mục đa tùy chọn, khoảng giá, còn hàng) kết hợp phân trang & sắp xếp an toàn qua Whitelist.
- **Home Content Orchestration & Redis Caching:** Aggregate API `GET /api/v1/home` tổng hợp 8 phần nội dung trang chủ, phân vùng Redis Cache TTL chuyên biệt qua Spring AOP Helper.
- **Thanh Toán Online VNPay:** Xử lý chữ ký số **HMAC-SHA512**, IPN Webhook Idempotency, kết hợp Background Task tự động hủy đơn quá hạn.
- **Object Storage (MinIO) & Media Security:** Upload tệp tin lên MinIO chuẩn S3, kiểm tra bảo mật bằng **Magic Bytes Header Validation** (chống giả mạo đuôi file) và nén/resize 800x800 chuẩn thương mại.

---

## 🛠️ Kiến Trúc & Tech Stack (Architecture & Technologies)

```text
                               ┌──────────────────────────────────────────┐
                               │           Client / Frontend App          │
                               └────────────────────┬─────────────────────┘
                                                    │ HTTP / RESTful API (JSON)
                                                    ▼
                               ┌──────────────────────────────────────────┐
                               │     Spring Security & JWT Filter         │
                               └────────────────────┬─────────────────────┘
                                                    │
                                                    ▼
                               ┌──────────────────────────────────────────┐
                               │           Controller Layer               │
                               │        (16 REST Controllers)             │
                               └────────────────────┬─────────────────────┘
                                                    │ DTOs (Records)
                                                    ▼
                               ┌──────────────────────────────────────────┐
                               │       Service Layer & Cache Helper       │
                               │   (Business Logic, State Machine...)     │
                               └────────┬───────────┬───────────┬─────────┘
                                        │           │           │
                        MinIO SDK Stream│           │SpringCache│ JPA / Hibernate
                                        ▼           ▼           ▼
                               ┌────────────────┐ ┌───────┐ ┌──────────────┐
                               │ MinIO Storage  │ │Redis 7│ │PostgreSQL 16 │
                               │(Product/Banner)│ │(Cache)│ │ (Flyway V1-3)│
                               └────────────────┘ └───────┘ └──────────────┘
```

### 🧰 Công Nghệ Sử Dụng

| Nhóm | Công nghệ / Thư viện | Phiên bản | Vai trò & Mục đích |
| --- | --- | --- | --- |
| **Core Framework** | Java | 21 (LTS) | Virtual Threads, Records, Pattern Matching, Sealed Classes |
| | Spring Boot | 3.5.16 | Core framework, Dependency Injection, Auto-configuration |
| | Spring Security | 6.x | Stateless Authentication, Phân quyền RBAC Role/Permission |
| | Spring Data JPA | (Hibernate) | ORM Engine, JPA Criteria API Engine, Entity Graph JOIN FETCH |
| **Database & Migration** | PostgreSQL | 16 | RDBMS chính lưu trữ quan hệ |
| | Flyway Migration | 10.x | Quản lý phiên bản Database Schema (V1 Baseline, V2 RBAC, V3 Home) |
| **Caching & Rate Limit** | Redis | 7.x | Caching đa vùng (`home:*`, `products`, `categories`), Token Bucket Rate Limiting |
| | Bucket4j | 8.10.1 | Distributed Rate Limiting chống spam request OTP & Search |
| **Security & Auth** | JJWT | 0.12.6 | Mã hóa & Giải mã JWT Access Tokens |
| **Storage & Image** | MinIO Java SDK | 8.5.17 | Object Storage tương thích S3 lưu trữ ảnh sản phẩm/avatar/banner |
| | Thumbnailator | 0.4.20 | Resize và nén hình ảnh tự động 800x800 trước khi lưu trữ |
| **Payment Gateway** | VNPay Integration | Custom | Thanh toán trực tuyến qua Chữ ký số HMAC-SHA512 & IPN Callback |
| **DevOps & Infrastructure** | Docker & Compose | Multi-stage | Containerization ứng dụng & services với Healthchecks |
| **Documentation** | Springdoc OpenAPI | 2.7.0 | Tự động sinh giao diện thử nghiệm Swagger UI API Documentation |

---

## ✨ Tính Năng Nổi Bật (Key Features)

### 🔐 1. Xác Thực & Phân Quyền (Auth & RBAC)
- Đăng ký, Đăng nhập mã hóa mật khẩu bằng **BCrypt**.
- Cơ chế **Dual Token (Access Token 1h + Refresh Token 7 ngày)** mã hóa SHA-256.
- Phân quyền RBAC động theo Vai trò (`ROLE_CUSTOMER`, `ROLE_ADMIN`) và 18+ Mã quyền (`product:create`, `order:update_status`...).
- Xác thực Email bằng mã OTP 6 chữ số gửi qua SMTP Brevo.

### 🔍 2. Product Search & Dynamic Filter Engine
- Động hóa truy vấn tìm kiếm bằng **JPA Criteria API (`ProductSpecification`)**.
- Lọc kết hợp: Từ khóa tên sản phẩm, Danh mục đa lựa chọn (`categoryId`), Khoảng giá (`minPrice` - `maxPrice`), Trạng thái còn hàng (`inStock`).
- Phân trang chuẩn `PageResponse<T>` kèm Whitelist Sắp xếp (`price,asc`, `price,desc`, `createdAt,desc`).

### 🏠 3. Home Content Management & Caching
- Aggregate API `GET /api/v1/home` tổng hợp 8 phần nội dung trang chủ (Hero Banner, Categories, Daily Arrivals, Featured Products, Product Tabs, Combo Sets, Stats).
- Phân vùng Caching Redis TTL chuyên biệt (`home:heroSlides`, `home:dailyArrivals`, `home:featuredProducts`...).
- Admin APIs CRUD Hero Banners, Daily Arrivals và Evict Refresh Cache trang chủ.

### 🛒 4. Đơn Hàng & Thanh Toán Online VNPay
- Tạo đơn hàng, kiểm tra kho Optimistic Locking (`@Version`).
- Tích hợp cổng thanh toán **VNPay Sandbox**: Tạo URL thanh toán, xử lý Webhook IPN Idempotent, tự động chuyển đơn hàng `PENDING` ➔ `CONFIRMED`.
- Background Scheduler tự động dọn dẹp đơn quá hạn thanh toán sau 15 phút.

---

## 📂 Cấu Trúc Thư Mục (Project Structure)

```text
mini-ecommerce/
├── docs/                       # Tài liệu Kỹ thuật, API Mapping Guide & DB Specs
│   ├── PROJECT_OVERVIEW.md     # Tổng quan dự án chi tiết
│   ├── API_DOCUMENTATION.md    # API Integration Guide cho Frontend (35+ APIs)
│   └── FEATURE_IMPLEMENTATION_TICKETS.md # Ticket Matrix
├── src/main/java/com/devfat/mini_ecommerce/
│   ├── auth/                   # Auth & OTP Controller, Service, Tokens
│   ├── user/                   # User Profile, Addresses, Admin RBAC
│   ├── category/               # Category Public & Admin APIs
│   ├── product/                # Product Criteria Engine & Search APIs
│   ├── home/                   # Home Page Aggregate & Sub-modules (Banners, Daily Arrivals)
│   ├── order/                  # Order State Machine & Checkout
│   ├── payment/                # VNPay Integration & IPN Callbacks
│   ├── storage/                # MinIO SDK & Magic Bytes Validation
│   └── shared/                 # Configs (Security, Redis, Swagger), Exceptions
└── src/main/resources/
    ├── db/migration/           # Flyway SQL Migration (V1 Baseline, V2 RBAC, V3 Home)
    └── application.yml         # Application Properties Configuration
```

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án (Quick Start)

### 📋 Yêu Cầu Tiền Đề (Prerequisites)
- **Java 21 LTS** trở lên (`java -version`).
- **Maven 3.9+** (Hoặc dùng `./mvnw` đính kèm).
- **Docker & Docker Compose** (Để chạy PostgreSQL, Redis, MinIO).

---

### 1️⃣ Clone Dự Án & Cấu Hình Biến Môi Trường

```bash
git clone https://github.com/huuphat26/java-mini-project-ecommerce.git
cd mini-ecommerce
```

Tạo file `.env` từ file `.env.example`:

```bash
cp .env.example .env
```

Cấu hình các tham số quan trọng trong `.env`:
```env
PORT=8085
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mini_shop
DB_USERNAME=postgres
DB_PASSWORD=postgres

REDIS_HOST=localhost
REDIS_PORT=6379

MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

VNPAY_TMN_CODE=YOUR_TMN_CODE
VNPAY_HASH_SECRET=YOUR_HASH_SECRET
```

---

### 2️⃣ Khởi Chạy Hạ Tầng Bằng Docker Compose

Chạy PostgreSQL, Redis, và MinIO trong container:

```bash
docker compose up -d
```

Kiểm tra trạng thái container:
```bash
docker compose ps
```

---

### 3️⃣ Biên Dịch & Chạy Ứng Dụng Backend

Chạy ứng dụng bằng Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Sau khi ứng dụng khởi chạy thành công, mở trình duyệt truy cập:
- 🌐 **Swagger UI API Documentation**: [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)
- 🗄️ **MinIO Web Console**: [http://localhost:9001](http://localhost:9001) (User/Pass: `minioadmin` / `minioadmin`)

---

## 🌐 Danh Sách API Endpoints Chính (API Overview)

| Module | HTTP Method | Endpoint | Mô Tả | Quyền |
|---|:---:|---|---|:---:|
| **Auth** | `POST` | `/api/v1/auth/register` | Đăng ký tài khoản người dùng mới | Public |
| | `POST` | `/api/v1/auth/login` | Đăng nhập hệ thống (Nhận Access & Refresh Token) | Public |
| | `POST` | `/api/v1/auth/refresh-token` | Làm mới Access Token | Public |
| **Search/Catalog** | `GET` | `/api/v1/products` | Dynamic Criteria Search & Filter sản phẩm | Public |
| | `GET` | `/api/v1/products/{id}` | Lấy chi tiết thông tin sản phẩm | Public |
| **Home Page** | `GET` | `/api/v1/home` | Lấy 8 danh mục dữ liệu trang chủ tổng hợp | Public |
| | `POST` | `/api/v1/admin/home/cache/evict` | Refresh / Evict toàn bộ Redis Cache Trang chủ | `ADMIN` |
| **Hero Banner** | `GET` | `/api/v1/admin/hero-banners` | Lấy danh sách tất cả Banner | `ADMIN` |
| | `POST` | `/api/v1/admin/hero-banners` | Tạo Hero Banner slide mới | `ADMIN` |
| | `PATCH` | `/api/v1/admin/hero-banners/{id}/toggle` | Bật/tắt trạng thái Banner | `ADMIN` |
| **Daily Arrival**| `GET` | `/api/v1/admin/daily-arrivals` | Danh sách Hải sản cập bến theo ngày | `ADMIN` |
| | `POST` | `/api/v1/admin/daily-arrivals` | Thêm sản phẩm vào danh sách Cập bến | `ADMIN` |
| **Order & Payment**| `POST` | `/api/v1/orders` | Đặt hàng mới (Checkout) | `CUSTOMER` |
| | `POST` | `/api/v1/payments/create-vnpay-url` | Tạo liên kết thanh toán VNPay Sandbox | `CUSTOMER` |

---

## 📄 License & Contact

Distributed under the **MIT License**.  
Design & Developed by **DevFat Team** — All rights reserved.
