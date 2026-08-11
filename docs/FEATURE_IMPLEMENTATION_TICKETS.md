# 📋 MASTER BE TASK TICKETS — PRODUCT SEARCH/FILTER & HOME MODULE

> **Document Type**: Technical Specification & Task Ticket Matrix  
> **Target Release**: FE API Integration Release  
> **Status**: Ready for Implementation  
> **Coverage**: 
> 1. 🔍 **Product Search & Dynamic Filter Module** (100% New JPA Specification Engine)  
> 2. 🏠 **Home Module & Core Dependencies** (`GET /api/v1/home`, Hero Banner, Daily Arrival, Review System)  
> **Estimated Total Effort**: ~36 giờ (~4.5 ngày làm việc)

---

## 🎯 Executive Summary & Modular Migration Architecture

```
                                ┌──────────────────────────────────────────────────────────┐
                                │             FEATURE IMPLEMENTATION BLUEPRINT             │
                                └─────────────────────────────┬────────────────────────────┘
                                                              │
                            ┌─────────────────────────────────┴─────────────────────────────────┐
                            ▼                                                                   ▼
             【TASK GROUP 1】SEARCH & FILTER ENGINE                              【TASK GROUP 2】HOME & CONTENT MODULE
             • 6 Tickets (SEARCH-001 ➔ 006)                                      • 16 Tickets (HOME-001 ➔ 016)
             • ProductSearchCriteria & Validators                                • 4 New Java Packages: home, herobanner,
             • ProductSpecification (JPA Criteria API)                             dailyarrival, review
             • Sort Field Whitelist                                              • Baseline V1 Standardized for Core Tables
             • Native Composite DB Indexes (In V1)                               • 1 Public API + 18+ Admin APIs
```

---

## 🗄️ LUỒNG MIGRATION THEO TỪNG TASK CHUẨN KỸ THUẬT

Theo đúng nguyên tắc phát triển module sạch sẽ:
1. **Bảng hiện có (`products`, `categories`, `product_reviews`)**: Đã được đưa 100% thuộc tính và Composite Search Indexes vào trực tiếp file `V1`, đảm bảo **không có bất kỳ câu lệnh `ALTER TABLE` nào**.
2. **Bảng tính năng mới (`hero_banners`, `daily_arrivals`)**: Sẽ được tạo ở file migration của đúng task đó khi tiến hành cài đặt module Home.

```
[V1__init_mini_shop.sql] (BASELINE CORE TABLES CHUẨN 100% THUỘC TÍNH 🟢)
  └─► Tạo toàn bộ các bảng cốt lõi với đầy đủ thuộc tính chuẩn từ đầu (ZERO ALTER TABLE cho bảng cũ):
      • products: average_rating, review_count, is_featured, original_price, spec, origin, weight_options, product_type, combo_*, indexes.
      • categories: description, slug, image_url, badge, badge_type, icon_name, home_display_style, home_sort_order, home_is_active.
      • product_reviews: is_featured_home.
      • indexes: Toàn bộ Composite Search Indexes (`idx_products_status_category`, `price`, `created_at`).
       │
       ▼
[V2__seed_system_roles_and_permissions.sql] (SYSTEM SECURITY DATA 🟢)
  └─► CHỈ chứa dữ liệu Phân quyền Core (Roles: ADMIN, STAFF, CUSTOMER + Permissions + Role_Permissions).
       │
       ▼
[V3__create_hero_banners_and_daily_arrivals.sql] (TASK HOME-001 — MIGRATION TẠO BẢNG MỚI)
  └─► Tạo 2 bảng nội dung hoàn toàn mới khi triển khai Task Home:
      • Table: hero_banners (23 cột)
      • Table: daily_arrivals (FK to products)
       │
       ▼
[V4__seed_home_and_catalog_initial_data.sql] (TASK HOME-016 — SEED DỮ LIỆU CHUẨN FE CONTRACT)
  └─► Nạp duy nhất 1 lần dữ liệu mẫu chuẩn khớp 100% FE Contract (Categories, Products, Banners, Daily Arrivals).
```

---

## 📊 Summary Ticket Matrix

### Task Group 1: Product Search & Dynamic Filter Engine

| Ticket ID | Ticket Name | Type | Priority | Estimate | Dependencies |
|---|---|:---:|:---:|:---:|---|
| `SEARCH-001` | Criteria DTO & Normalization Logic | ✨ Feature | 🔴 HIGH | 1.0h | — |
| `SEARCH-002` | SearchCriteriaValidator & SortValidator | 🔒 Security | 🔴 HIGH | 1.5h | SEARCH-001 |
| `SEARCH-003` | ProductSpecification Engine (JPA Criteria) | ⚙️ Core | 🔴 HIGH | 2.5h | SEARCH-001 |
| `SEARCH-004` | JpaSpecificationExecutor & Service Refactor | 🔧 Refactor | 🔴 HIGH | 1.5h | SEARCH-002, 003 |
| `SEARCH-005` | Controller Endpoint & Swagger Integration | 🌐 API | 🔴 HIGH | 1.0h | SEARCH-004 |
| `SEARCH-006` | Native Composite Indexes Verification & Spec Tests | 🗄️ Database | 🟡 MEDIUM | 1.0h | SEARCH-004 |

**Subtotal Search/Filter**: 6 Tickets — **8.5 giờ**

---

### Task Group 2: Home Module & Dependencies

| Ticket ID | Ticket Name | Type | Priority | Estimate | Dependencies |
|---|---|:---:|:---:|:---:|---|
| `HOME-001` | DB Migration V3 (Create Hero Banners & Daily Arrivals Tables) | 🗄️ DB | 🔴 BLOCKING | 1.5h | — |
| `HOME-002` | Update ProductEntity Mapping (Rating & Home Fields) | 🐛 Bugfix | 🔴 HIGH | 1.0h | — |
| `HOME-003` | Update CategoryEntity Mapping for Home Config | ✨ Feature | 🟡 MEDIUM | 1.0h | — |
| `HOME-004` | Module `herobanner` — Data Layer | ✨ Feature | 🔴 HIGH | 2.0h | HOME-001 |
| `HOME-005` | Module `herobanner` — Service & Admin APIs | 🌐 API | 🔴 HIGH | 2.0h | HOME-004 |
| `HOME-006` | Module `dailyarrival` — Data Layer | ✨ Feature | 🔴 HIGH | 2.0h | HOME-001 |
| `HOME-007` | Module `dailyarrival` — Service & Admin APIs | 🌐 API | 🔴 HIGH | 2.0h | HOME-006 |
| `HOME-008` | Home DTOs (10 Classes for 8 Sections) | ✨ Feature | 🟡 MEDIUM | 2.0h | HOME-002, 003 |
| `HOME-009` | HomeRepository — Aggregate JPQL Queries | ⚙️ Core | 🟡 MEDIUM | 1.5h | HOME-014 |
| `HOME-010` | HomeServiceImpl — Orchestrate 8 Cached Sections | ⚙️ Core | 🔴 HIGH | 3.0h | HOME-004➔009, 014 |
| `HOME-011` | HomeController — Public `GET /api/v1/home` | 🌐 API | 🔴 HIGH | 1.0h | HOME-010 |
| `HOME-012` | Admin Product & Category Home Config Endpoints | 🌐 API | 🟡 MEDIUM | 1.5h | HOME-002, 003 |
| `HOME-013` | Image Upload for Banner & Category (Storage) | 🖼️ Asset | 🟡 MEDIUM | 1.5h | HOME-003, 005 |
| `HOME-014` | Redis Cache TTL Configuration | 💾 Infrastructure | 🟡 MEDIUM | 1.0h | HOME-010 |
| `HOME-015` | **Module `review` — Toàn diện (BLOCKING PRE-REQ)** | 🏛️ Module | 🔴 BLOCKING | 4.0h | — |
| `HOME-016` | Cross-Module Redis Cache Eviction Chain | 💾 Infrastructure | 🟡 MEDIUM | 1.5h | HOME-014, 015 |
| `HOME-017` | DB Migration V4 (Consolidated Seed Data) | 🗄️ DB | 🟢 LOW | 1.0h | HOME-011 |

**Subtotal Home Module**: 17 Tickets — **29.5 giờ**

---

# 🔍 PART 1: PRODUCT SEARCH & FILTER ENGINE TICKETS

---

### 📌 [SEARCH-001] Criteria DTO & Normalization Logic

| Field | Value |
|---|---|
| **ID** | `SEARCH-001` |
| **Type** | ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **1.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.product.dto` |
| **Dependencies** | — |

#### Mô tả
Tạo `ProductSearchCriteria` làm Data Transfer Object chứa toàn bộ tham số tìm kiếm và lọc từ client. Dùng Java `record` để đảm bảo tính bất biến (immutability) và tích hợp sẵn logic chuẩn hóa chuỗi `search` trong compact constructor.

#### Các bước thực hiện
1. Tạo class `ProductSearchCriteria.java` (Java Record):
   ```java
   public record ProductSearchCriteria(
           String search,
           List<Long> categoryId,
           BigDecimal minPrice,
           BigDecimal maxPrice,
           Boolean inStock
   ) {
       public ProductSearchCriteria {
           if (search != null) {
               search = search.trim();
               if (search.isEmpty()) search = null;
           }
       }
   }
   ```
2. Thêm Javadoc giải thích business rules cho từng trường.

#### Definition of Done
- ✅ Record compile sạch, tự động trim và convert empty search string về `null`.
- ✅ Hỗ trợ truyền danh sách `categoryId` kiểu `List<Long>`.

---

### 📌 [SEARCH-002] SearchCriteriaValidator & SortValidator

| Field | Value |
|---|---|
| **ID** | `SEARCH-002` |
| **Type** | 🔒 Security / Validation |
| **Priority** | 🔴 HIGH |
| **Estimate** | **1.5 giờ** |
| **Package** | `com.devfat.mini_ecommerce.product.validation` |
| **Dependencies** | SEARCH-001 |

#### Mô tả
Xây dựng 2 validator độc lập:
1. `ProductSearchCriteriaValidator`: Kiểm tra tính hợp lệ logic của khoảng giá và độ dài từ khóa (tránh SQL injection / DoS).
2. `ProductSortValidator`: Kiểm tra Whitelist các trường được phép sắp xếp, chặn đứng việc client truyền tên cột DB không tồn tại gây lỗi 500.

#### Các bước thực hiện
1. Tạo `ProductSearchCriteriaValidator.java`:
   - `search` length <= 100 ký tự.
   - `minPrice` >= 0, `maxPrice` >= 0.
   - `minPrice` <= `maxPrice` (ném `BadRequestException` nếu `minPrice > maxPrice`).
2. Tạo `ProductSortValidator.java`:
   - Khai báo Whitelist Set: `Set.of("price", "createdAt", "name")`.
   - Lặp qua từng `Sort.Order`, ném `BadRequestException` nếu `order.getProperty()` nằm ngoài Whitelist.

#### Definition of Done
- ✅ `minPrice > maxPrice` ➔ Ném `BadRequestException` (HTTP 400).
- ✅ Sort theo trường lạ (vd: `sort=internalCost,desc`) ➔ Trả HTTP 400 kèm thông báo rõ ràng.

---

### 📌 [SEARCH-003] ProductSpecification Engine (JPA Criteria)

| Field | Value |
|---|---|
| **ID** | `SEARCH-003` |
| **Type** | ⚙️ Core Business Logic |
| **Priority** | 🔴 HIGH |
| **Estimate** | **2.5 giờ** |
| **Package** | `com.devfat.mini_ecommerce.product.specification` |
| **Dependencies** | SEARCH-001 |

#### Mô tả
Viết class `ProductSpecification` bằng JPA Criteria API. Mỗi điều kiện lọc được tách thành 1 method riêng biệt, linh hoạt kết hợp bằng `Specification.allOf(...)`.

#### Các bước thực hiện
1. Tạo `ProductSpecification.java`:
   ```java
   public final class ProductSpecification {
       private ProductSpecification() {}

       public static Specification<ProductEntity> withCriteria(ProductSearchCriteria criteria) {
           return Specification.allOf(
                   hasSearch(criteria.search()),
                   hasCategoryIn(criteria.categoryId()),
                   hasMinPrice(criteria.minPrice()),
                   hasMaxPrice(criteria.maxPrice()),
                   isInStock(criteria.inStock()),
                   isActive()
           );
       }
   }
   ```
2. Đảm bảo các helper method trả về `null` khi param rỗng để Spring Data JPA bỏ qua điều kiện đó.

#### Definition of Done
- ✅ Toàn bộ 6 predicates hoạt động chuẩn xác khi kết hợp (AND logic).
- ✅ `isActive = true` được ép cứng ở Specification, bảo vệ sản phẩm nháp/ẩn không bị rò rỉ ra Public API.

---

### 📌 [SEARCH-004] JpaSpecificationExecutor & Service Refactor

| Field | Value |
|---|---|
| **ID** | `SEARCH-004` |
| **Type** | 🔧 Refactor |
| **Priority** | 🔴 HIGH |
| **Estimate** | **1.5 giờ** |
| **Package** | `com.devfat.mini_ecommerce.product.internal` |
| **Dependencies** | SEARCH-002, SEARCH-003 |

#### Mô tả
1. Cập nhật `ProductRepository` kế thừa `JpaSpecificationExecutor<ProductEntity>`.
2. Refactor `ProductServiceImpl.searchProducts()` để thực thi `Specification`, đồng thời **loại bỏ `@Cacheable`** trên API search động.

#### Các bước thực hiện
1. Thay đổi interface `ProductRepository.java`:
   ```java
   public interface ProductRepository extends JpaRepository<ProductEntity, Long>, 
           JpaSpecificationExecutor<ProductEntity> {}
   ```
2. Cập nhật `ProductServiceImpl.java`:
   - Validate `ProductSearchCriteria` và `Pageable.getSort()`.
   - Thay thế câu query cũ bằng `productRepository.findAll(spec, pageable)`.
   - Gỡ bỏ annotation `@Cacheable` trên method search động.

#### Definition of Done
- ✅ Service gọi validator và ném lỗi 400 trước khi query DB.
- ✅ Trả về `PageResponse<ProductResponseDto>` đầy đủ thông tin phân trang.

---

### 📌 [SEARCH-005] Controller Endpoint & Swagger Integration

| Field | Value |
|---|---|
| **ID** | `SEARCH-005` |
| **Type** | 🌐 API Controller |
| **Priority** | 🔴 HIGH |
| **Estimate** | **1.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.product` |
| **Dependencies** | SEARCH-004 |

#### Mô tả
Cập nhật endpoint `GET /api/v1/products` tại `ProductController` để nhận `ProductSearchCriteria` thông qua `@ParameterObject @ModelAttribute`.

#### Các bước thực hiện
1. Sửa method `getAll()` trong `ProductController.java`:
   ```java
   @Operation(summary = "Get products with search & filters",
              description = "Public API — Retrieve active products with dynamic search and multi-filtering.")
   @SecurityRequirements({})
   @RateLimit(type = RateLimitType.PUBLIC_API, byIp = true)
   @GetMapping
   public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> getAll(
           @ParameterObject @ModelAttribute ProductSearchCriteria criteria,
           @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
       return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(criteria, pageable), "Get product successfully"));
   }
   ```

#### Definition of Done
- ✅ APISwagger UI tự động sinh form riêng biệt cho từng field (`search`, `categoryId`, `minPrice`, `maxPrice`, `inStock`).
- ✅ Public access không cần JWT Token.

---

### 📌 [SEARCH-006] Native Composite Indexes Verification & Spec Tests

| Field | Value |
|---|---|
| **ID** | `SEARCH-006` |
| **Type** | 🗄️ Database & Testing |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.0 giờ** |
| **Package** | `test/` |
| **Dependencies** | SEARCH-004 |

#### Mô tả
Viết Unit Tests cho `ProductSpecification` kiểm tra các trường hợp lọc động (Các composite indexes đã có sẵn trong `V1__init_mini_shop.sql`).

#### Definition of Done
- ✅ Integration Unit test PASS 100%.

---

# 🏠 PART 2: HOME MODULE & DEPENDENCIES TICKETS

---

### 📌 [HOME-001] DB Migration V3 (Create Hero Banners & Daily Arrivals Tables)

| Field | Value |
|---|---|
| **ID** | `HOME-001` |
| **Type** | 🗄️ Database Migration |
| **Priority** | 🔴 BLOCKING |
| **Estimate** | **1.5 giờ** |
| **Package** | `src/main/resources/db/migration` |
| **Dependencies** | — |

#### Mô tả
Tạo file migration Flyway `V3__create_hero_banners_and_daily_arrivals.sql` tạo 2 bảng nội dung hoàn toàn mới cho Home module (`hero_banners` 23 cột và `daily_arrivals`).

#### Các bước thực hiện
1. Create Table `hero_banners` (23 cột).
2. Create Table `daily_arrivals` (FK to `products`).

#### Definition of Done
- ✅ Khởi động ứng dụng, Flyway nạp file V3 thành công không dính lỗi syntax hay checksum error.

---

### 📌 [HOME-002] Update ProductEntity Mapping (Rating & Home Fields)

| Field | Value |
|---|---|
| **ID** | `HOME-002` |
| **Type** | 🐛 Bugfix & ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **1.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.product` |
| **Dependencies** | — |

#### Mô tả
Cập nhật mapping Java JPA trong `ProductEntity` khớp với các cột vừa được chuẩn hóa ở Schema V1 (averageRating, reviewCount, isFeatured, ProductType, ComboConfig...).

#### Các bước thực hiện
1. Trong `ProductEntity.java`:
   - Mapping `@Column(name = "average_rating") private BigDecimal averageRating;`
   - Mapping `@Column(name = "review_count") private Integer reviewCount;`
   - Mapping các trường home: `isFeatured`, `originalPrice`, `spec`, `origin`, `weightOptions`, `productType`, `comboCategory`, `comboTheme`, `comboTag`, `comboCtaText`, `comboHref`, `isBreakout`, `comboSortOrder`.
2. Tạo enum `ProductType { REGULAR, COMBO }`.
3. Cập nhật `ProductResponseDto` và MapStruct `ProductMapper`.

#### Definition of Done
- ✅ API `GET /api/v1/products/{id}` trả về đầy đủ `averageRating` và `reviewCount`.

---

### 📌 [HOME-003] Update CategoryEntity Mapping for Home Config

| Field | Value |
|---|---|
| **ID** | `HOME-003` |
| **Type** | ✨ Feature |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.category` |
| **Dependencies** | — |

#### Mô tả
Cập nhật mapping JPA trong `CategoryEntity` tương ứng với các thuộc tính trang chủ trong Schema V1 (Badge, Icon, Slug, Display Style).

#### Các bước thực hiện
1. Map 9 trường trong `CategoryEntity.java`: `description`, `slug`, `imageUrl`, `badge`, `badgeType`, `iconName`, `homeDisplayStyle`, `homeSortOrder`, `homeIsActive`.
2. Cập nhật `CategoryResponseDto` và `CategoryMapper`.

#### Definition of Done
- ✅ Entity map chính xác với bảng `categories` chuẩn V1.

---

### 📌 [HOME-004] Module `herobanner` — Data Layer

| Field | Value |
|---|---|
| **ID** | `HOME-004` |
| **Type** | ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **2.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.herobanner` |
| **Dependencies** | HOME-001 |

#### Mô tả
Khởi tạo package `herobanner` và tầng dữ liệu (Entity, Repository, DTOs, Mapper) cho Hero Banner Slide (Bảng `hero_banners` tạo ở V3).

#### Các bước thực hiện
1. Tạo `HeroBannerEntity.java` kế thừa `BaseEntity` (map đầy đủ 23 thuộc tính).
2. Tạo `HeroBannerRepository.java`:
   - `List<HeroBannerEntity> findByIsActiveTrueOrderBySortOrderAsc()`
3. Tạo DTOs: `HeroBannerResponseDto`, `CreateHeroBannerRequestDto`, `UpdateHeroBannerRequestDto`.
4. Tạo `HeroBannerMapper.java` (MapStruct).

#### Definition of Done
- ✅ Dữ liệu Banner sẵn sàng cho service layer tiêu thụ.

---

### 📌 [HOME-005] Module `herobanner` — Service & Admin APIs

| Field | Value |
|---|---|
| **ID** | `HOME-005` |
| **Type** | 🌐 API Controller |
| **Priority** | 🔴 HIGH |
| **Estimate** | **2.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.herobanner` |
| **Dependencies** | HOME-004 |

#### Mô tả
Xây dựng logic nghiệp vụ và 5 Admin Endpoints quản lý Hero Banner.

#### Các bước thực hiện
1. Tạo `HeroBannerService.java` & `HeroBannerServiceImpl.java`.
2. Tạo `HeroBannerController.java` (`/api/v1/admin/hero-banners`):
   - `GET /` — Lấy danh sách tất cả banners (phân trang/filter).
   - `POST /` — Tạo mới banner (HTTP 201).
   - `PATCH /{id}` — Cập nhật banner.
   - `DELETE /{id}` — Xóa banner.
   - `PATCH /{id}/toggle` — Ẩn/Hiện banner.

#### Definition of Done
- ✅ 5 Admin Endpoints chạy chuẩn xác, kiểm tra role `ADMIN`.

---

### 📌 [HOME-006] Module `dailyarrival` — Data Layer

| Field | Value |
|---|---|
| **ID** | `HOME-006` |
| **Type** | ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **2.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.dailyarrival` |
| **Dependencies** | HOME-001 |

#### Mô tả
Tạo tầng Data Access cho tính năng "Hàng Mới Về Hôm Nay" (Daily Arrivals) (Bảng `daily_arrivals` tạo ở V3).

#### Các bước thực hiện
1. Tạo `DailyArrivalEntity.java` (gồm FK `@ManyToOne ProductEntity`, `date`, `arrivedAt`, `badge`, `title`, `weight`, `origin`...).
2. Tạo `DailyArrivalRepository.java`:
   - `@Query` fetch JOIN Product tránh N+1: `findTodayActive()` theo ngày hiện tại.
3. Tạo DTOs và MapStruct `DailyArrivalMapper.java`.

#### Definition of Done
- ✅ Câu truy vấn JPQL tự động fetch thông tin Product không bị N+1 query.

---

### 📌 [HOME-007] Module `dailyarrival` — Service & Admin APIs

| Field | Value |
|---|---|
| **ID** | `HOME-007` |
| **Type** | 🌐 API Controller |
| **Priority** | 🔴 HIGH |
| **Estimate** | **2.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.dailyarrival` |
| **Dependencies** | HOME-006 |

#### Mô tả
Viết Business Logic và 4 Admin Endpoints để Admin tuyển chọn sản phẩm mới về mỗi ngày.

#### Các bước thực hiện
1. Implement `DailyArrivalServiceImpl.java` (kiểm tra trùng lặp `productId` + `date`).
2. Tạo `DailyArrivalController.java` (`/api/v1/admin/daily-arrivals`):
   - `GET /?date=YYYY-MM-DD` — Xem danh sách theo ngày.
   - `POST /` — Thêm sản phẩm vào danh sách hàng mới về.
   - `PATCH /{id}` — Sửa thông tin.
   - `DELETE /{id}` — Xóa khỏi danh sách.

#### Definition of Done
- ✅ Admin pick được sản phẩm theo từng ngày thành công.

---

### 📌 [HOME-008] Home DTOs (10 Classes for 8 Sections)

| Field | Value |
|---|---|
| **ID** | `HOME-008` |
| **Type** | ✨ Feature |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **2.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.home.dto` |
| **Dependencies** | HOME-002, 003 |
| **Reference** | [home_api_contract.md](file:///Users/andy2015bui/Desktop/mini-ecommerce/docs/home_api_contract.md) |

#### Mô tả
Tạo toàn bộ 10 DTO Classes định hình cấu trúc JSON response cho Trang chủ theo đúng Contract chuẩn 77 fields với Frontend ([home_api_contract.md](file:///Users/andy2015bui/Desktop/mini-ecommerce/docs/home_api_contract.md)).

#### Danh sách DTO Classes & Cấu trúc Field:
1. `HomePageDataDto.java`: Root DTO chứa 8 sub-data (`heroSlides`, `categories`, `dailyArrivals`, `featuredProducts`, `featuredProductTabs`, `comboSets`, `featuredReviews`, `stats`).
2. `HeroSlideDto.java`: `id`, `sortOrder`, `isActive`, `badge` (`HeroBadgeDto`), `titlePrefix`, `titleHighlight`, `titleSuffix`, `description`, `primaryCta` (`HeroCtaDto`), `secondaryCta` (`HeroCtaDto`), `productCard` (`HeroProductCardDto`).
3. `HeroBadgeDto.java`: `text`, `icon`.
4. `HeroCtaDto.java`: `label`, `href`, `icon`.
5. `HeroProductCardDto.java`: `imageUrl`, `imageAlt`, `comboBadge`, `discountBadge`, `originalPrice`, `salePrice`, `title`, `subtitle`.
6. `HomeCategoryDto.java`: `id`, `name`, `description`, `slug`, `imageUrl`, `badge`, `badgeType`, `iconName`, `productCount`, `displayStyle` (`main`/`card`/`icon`), `sortOrder`, `isActive`.
7. `DailyArrivalDto.java`: `id`, `productId`, `arrivedAt`, `badge`, `title`, `description`, `weight`, `origin`, `price`, `originalPrice`, `imageUrl`, `imageAlt`.
8. `FeaturedProductDto.java`: `id`, `name`, `categoryLabel`, `categorySlug`, `badges`, `spec`, `price`, `originalPrice`, `unit`, `imageUrl`, `rating`, `reviewCount`, `origin`, `description`, `weightOptions`.
9. `FeaturedProductTabDto.java`: `slug`, `label`, `sortOrder`.
10. `ComboSetDto.java`: `id`, `tag`, `title`, `description`, `price`, `unit`, `ctaText`, `href`, `imageUrl`, `theme` (`light`/`dark`), `isBreakout`, `category` (`lunch`/`party`/`family`), `sortOrder`, `isActive`.
11. `FeaturedReviewDto.java`: `id`, `customerName`, `customerLocation`, `avatarInitials`, `rating`, `productName`, `comment`, `createdAt`.
12. `HomeStatsDto.java`: `totalOrdersDelivered`, `averageRating`, `totalReviews`.

#### Definition of Done
- ✅ Khớp 100% định dạng JSON Contract FE yêu cầu (77 fields, 8 sub-data keys).

---

### 📌 [HOME-009] HomeRepository — Aggregate JPQL Queries

| Field | Value |
|---|---|
| **ID** | `HOME-009` |
| **Type** | ⚙️ Core Data Query |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.5 giờ** |
| **Package** | `com.devfat.mini_ecommerce.home.internal` |
| **Dependencies** | HOME-014 |

#### Mô tả
Tạo `HomeRepository` chứa các câu lệnh JPQL/Native SQL tối ưu tổng hợp dữ liệu từ nhiều bảng cho trang chủ.

#### Các bước thực hiện
1. `countDeliveredOrders()`: Đếm đơn hàng hoàn tất (`status = 'DONE'`).
2. `getAverageRating()` & `countTotalReviews()`: Thống kê từ bảng `product_reviews`.
3. `findFeaturedProducts()`: Lấy danh sách sản phẩm nổi bật (`isFeatured = true AND isActive = true`).
4. `findComboSets()`: Lấy danh sách sản phẩm loại Combo (`productType = 'COMBO'`).

#### Definition of Done
- ✅ Các câu query chạy nhanh, trả về kết quả chính xác không dính lỗi N+1.

---

### 📌 [HOME-010] HomeServiceImpl — Orchestrate 8 Cached Sections

| Field | Value |
|---|---|
| **ID** | `HOME-010` |
| **Type** | ⚙️ Core Service |
| **Priority** | 🔴 HIGH |
| **Estimate** | **3.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.home.internal` |
| **Dependencies** | HOME-004 ➔ HOME-009, HOME-014 |

#### Mô tả
Trái tim của Module Home. Gọi 8 helper public methods (được đánh `@Cacheable` riêng biệt) để lắp ghép thành `HomePageDataDto`.

#### Các bước thực hiện
1. Tạo `HomeService.java` & `HomeServiceImpl.java`.
2. Implement 8 public cached methods:
   - `getHeroSlides()` (`home:heroSlides`)
   - `getHomeCategories()` (`home:categories`)
   - `getDailyArrivals()` (`home:dailyArrivals`)
   - `getFeaturedProducts()` (`home:featuredProducts`)
   - `getFeaturedProductTabs()` (`home:featuredProductTabs`)
   - `getComboSets()` (`home:comboSets`)
   - `getFeaturedReviews()` (`home:featuredReviews`)
   - `getStats()` (`home:stats`)
3. Lắp ghép vào `getHomePageData()`.

#### Definition of Done
- ✅ Tổng hợp đủ 8 sections dữ liệu mượt mà, không bị NullPointerException khi DB rỗng.

---

### 📌 [HOME-011] HomeController — Public `GET /api/v1/home`

| Field | Value |
|---|---|
| **ID** | `HOME-011` |
| **Type** | 🌐 API Controller |
| **Priority** | 🔴 HIGH |
| **Estimate** | **1.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.home` |
| **Dependencies** | HOME-010 |

#### Mô tả
Cung cấp Public REST API duy nhất cho Frontend render trang chủ.

#### Các bước thực hiện
1. Tạo `HomeController.java`:
   ```java
   @RestController
   @RequestMapping("/api/v1/home")
   @Tag(name = "Home", description = "Public Home Page Data")
   public class HomeController {
       private final HomeService homeService;

       @SecurityRequirements({})
       @RateLimit(type = RateLimitType.PUBLIC_API, byIp = true)
       @GetMapping
       public ResponseEntity<ApiResponse<HomePageDataDto>> getHome() {
           return ResponseEntity.ok()
                   .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                   .body(ApiResponse.success(homeService.getHomePageData(), "Get home page data successfully"));
       }
   }
   ```
2. Cập nhật `SecurityConfig.java` cho phép truy cập `/api/v1/home` công khai.

#### Definition of Done
- ✅ Client gọi `GET /api/v1/home` nhận đủ JSON 8 sections không cần Auth Token.

---

### 📌 [HOME-012] Admin Product & Category Home Config Endpoints

| Field | Value |
|---|---|
| **ID** | `HOME-012` |
| **Type** | 🌐 API Controller |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.5 giờ** |
| **Package** | `com.devfat.mini_ecommerce.product` & `category` |
| **Dependencies** | HOME-002, HOME-003 |

#### Mô tả
Thêm các Admin Endpoints phụ trợ vào `AdminProductController` và `AdminCategoryController` để thiết lập hiển thị trang chủ.

#### Các bước thực hiện
1. `AdminProductController`:
   - `PATCH /api/v1/admin/products/{id}/featured` — Toggle bật/tắt sản phẩm nổi bật.
   - `PATCH /api/v1/admin/products/{id}/combo-config` — Cấu hình combo.
2. `AdminCategoryController`:
   - `PATCH /api/v1/admin/categories/{id}/home-config` — Cấu hình style/badge trang chủ.

#### Definition of Done
- ✅ Admin thao tác bật/tắt sản phẩm nổi bật hoặc cài đặt category trang chủ dễ dàng.

---

### 📌 [HOME-013] Image Upload for Banner & Category (Storage)

| Field | Value |
|---|---|
| **ID** | `HOME-013` |
| **Type** | 🖼️ Asset Upload |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.5 giờ** |
| **Package** | `com.devfat.mini_ecommerce.storage` |
| **Dependencies** | HOME-003, HOME-005 |

#### Mô tả
Tích hợp dịch vụ Storage (MinIO/Local) cho phép Admin upload ảnh trực tiếp cho Hero Banner và Category.

#### Các bước thực hiện
1. Endpoint `POST /api/v1/admin/hero-banners/{id}/image`
2. Endpoint `POST /api/v1/admin/categories/{id}/image`
3. Gọi `StorageService.uploadFile()` lưu ảnh và trả về URL công khai.

#### Definition of Done
- ✅ Admin upload ảnh `MultipartFile` thành công, lưu URL vào Database.

---

### 📌 [HOME-014] Redis Cache TTL Configuration

| Field | Value |
|---|---|
| **ID** | `HOME-014` |
| **Type** | 💾 Infrastructure |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.shared.config` |
| **Dependencies** | HOME-010 |

#### Mô tả
Cấu hình thời gian sống (TTL) riêng biệt cho từng cache key của Home Module trong Redis.

#### Các bước thực hiện
1. Trong `RedisCacheConfig.java`, thêm cấu hình TTL:
   - `home:heroSlides`: 10 phút
   - `home:categories`: 10 phút
   - `home:dailyArrivals`: 6 giờ
   - `home:featuredProducts`: 5 phút
   - `home:featuredProductTabs`: 10 phút
   - `home:comboSets`: 10 phút
   - `home:featuredReviews`: 30 phút
   - `home:stats`: 30 phút

#### Definition of Done
- ✅ Redis kiểm tra `TTL "home:*"` khớp chuẩn số giây đã cấu hình.

---

### 📌 [HOME-015] Module `review` — Toàn diện (BLOCKING PRE-REQ)

| Field | Value |
|---|---|
| **ID** | `HOME-015` |
| **Type** | 🏛️ Module Mới (Tiền Đề Block) |
| **Priority** | 🔴 BLOCKING |
| **Estimate** | **4.0 giờ** |
| **Package** | `com.devfat.mini_ecommerce.review` |
| **Dependencies** | — |

#### Mô tả
Xây dựng trọn vẹn Module Review (Đánh Giá Sản Phẩm). Bảng `product_reviews` đã được chuẩn hóa sẵn trong `V1` kèm cờ `is_featured_home`.

#### Các bước thực hiện
1. Tạo `ReviewEntity.java` map với bảng `product_reviews`.
2. Tạo `ReviewRepository.java` (đếm số review, tính điểm trung bình AVG, lấy danh sách `isFeaturedHome = true`).
3. Tạo `ReviewService.java`: Khi user gửi review ➔ Tự động tính lại `averageRating` & `reviewCount` rồi cập nhật vào `ProductEntity`.
4. Tạo `ReviewController.java` (`POST /api/v1/reviews` cho User gửi đánh giá).
5. Tạo `AdminReviewController.java` (`/api/v1/admin/reviews` cho Admin duyệt & bật `isFeaturedHome`).

#### Definition of Done
- ✅ Module Review hoàn chỉnh, tự động cập nhật Rating trung bình vào sản phẩm.

---

### 📌 [HOME-016] Cross-Module Redis Cache Eviction Chain

| Field | Value |
|---|---|
| **ID** | `HOME-016` |
| **Type** | 💾 Infrastructure |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.5 giờ** |
| **Package** | Cross-Module Services |
| **Dependencies** | HOME-014, HOME-015 |

#### Mô tả
Thiết lập cơ chế tự động xóa Cache trang chủ khi Admin thực hiện thay đổi dữ liệu ở các module khác (Product, Category, Banner, Review).

#### Các bước thực hiện
1. Khi Admin sửa/xóa Product ➔ `@CacheEvict(value = {"home:featuredProducts", "home:comboSets"})`.
2. Khi Admin sửa Banner ➔ `@CacheEvict(value = "home:heroSlides")`.
3. Khi Admin duyệt Review nổi bật ➔ `@CacheEvict(value = "home:featuredReviews")`.

#### Definition of Done
- ✅ Admin chỉnh sửa dữ liệu ở trang Admin ➔ Trang chủ (`GET /api/v1/home`) cập nhật ngay lập tức.

---

### 📌 [HOME-017] DB Migration V4 (Consolidated Seed Data)

| Field | Value |
|---|---|
| **ID** | `HOME-017` |
| **Type** | 🗄️ Database Seed |
| **Priority** | 🟢 LOW |
| **Estimate** | **1.0 giờ** |
| **Package** | `src/main/resources/db/migration` |
| **Dependencies** | HOME-011 |

#### Mô tả
Tạo duy nhất 1 file migration `V4__seed_home_and_catalog_initial_data.sql` nạp dữ liệu mẫu chuẩn khớp 100% FE Contract (Categories kèm `slug` & `displayStyle`, Products kèm `combo_*` & rating, Banners mẫu & Daily Arrivals mẫu).

#### Các bước thực hiện
1. Insert Categories mẫu chuẩn FE (`set-combo`, `tom-cua`, `muc-bach-tuoc`, `sot-tiec`, `so-oc`).
2. Insert Products & Combo Sets mẫu chuẩn.
3. Insert 3 Hero Banners mẫu & 3 Daily Arrivals mẫu.

#### Definition of Done
- ✅ Database nạp 1 lần duy nhất có đủ dữ liệu đẹp cho FE test giao diện.

---

## 📈 Combined Execution Timeline

```
Day 1:  [SEARCH-001 ➔ SEARCH-006] (Triển khai Product Search Engine + Service Refactor - 8.5h)
Day 2:  [HOME-001 ➔ HOME-003, HOME-015] (Update Entity Mappings + Create V3 Tables + Module Review Tiền Đề - 7.5h)
Day 3:  [HOME-004 ➔ HOME-006] (Module Hero Banner & Daily Arrival - 8.0h)
Day 4:  [HOME-007 ➔ HOME-010] (Home DTOs, Repository, Orchestrator & Public API - 7.5h)
Day 5:  [HOME-011 ➔ HOME-017] (Admin Config, Storage, Cache Eviction & Migration V4 Seed Data - 7.5h)
```

---

## 🏁 Definition of Done (Toàn bộ 2 Task lớn)
1. **Product Search & Filter**: `GET /api/v1/products` lọc được khoảng giá, nhiều danh mục, từ khóa, kiểm tra Whitelist Sort không lỗi.
2. **Home Module**: `GET /api/v1/home` trả về đầy đủ 8 sections JSON response không bị null, phản hồi < 50ms nhờ Redis Cache.
3. **Clean Modular Migration**:
   - `V1`: Baseline Core Tables chuẩn 100% cột + Search Composite Indexes (**Zero ALTER TABLE cho bảng cũ**).
   - `V2`: Security Roles & Permissions.
   - `V3`: Migration bảng mới (`hero_banners`, `daily_arrivals`) trong task Home.
   - `V4`: Seed data chuẩn FE Contract.
4. **Compilation & Build**: Chạy `./mvnw clean compile` và `./mvnw test` đạt **BUILD SUCCESS 100%** không dính bất kỳ cảnh báo MapStruct hay lỗi runtime.
5. **Swagger UI**: Hiển thị đầy đủ mô tả OpenAPI bằng Tiếng Anh đơn giản cho tất cả Public & Admin APIs mới.
