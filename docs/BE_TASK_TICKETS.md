# 🏠 BE TASK TICKETS — HOME MODULE

> **Epic**: Xây dựng toàn diện module Home cho mini-ecommerce platform  
> **Mục tiêu**: Cung cấp `GET /api/v1/home` trả về 8 sub-data cho FE + Admin APIs quản lý nội dung  
> **Ưu tiên**: 🔴 HIGH — Trang chủ là điểm tiếp xúc đầu tiên với user  
> **Ước tính tổng**: ~3.5 ngày (~28 giờ làm việc)  
> **Phụ thuộc**: Auth module ✅, Product module ✅, Category module ✅, Order module ✅

---

## 📌 EPIC — [HOME-000] Home Module Tổng Thể

| Field | Value |
|---|---|
| **ID** | `HOME-000` |
| **Type** | 🏛️ Epic |
| **Priority** | 🔴 HIGH |
| **Estimate** | ~33.5 giờ (~4.2 ngày) |
| **Assignee** | Backend Dev |
| **Status** | 📋 Backlog |
| **Tickets** | 19 (14 gốc + 5 phát sinh từ cross-module analysis) |

### Mô tả

Hiện tại FE cần một API duy nhất `GET /api/v1/home` để render toàn bộ trang chủ với 8 section khác nhau. Codebase hiện tại **không có module home** và chỉ đáp ứng được ~20% yêu cầu của FE contract.

Module cần xây dựng bao gồm:
- ✅ **1 public API** cho FE: `GET /api/v1/home`
- ✅ **18+ admin APIs** quản lý nội dung trang chủ
- ✅ **5+ DB migrations** mới (V3→V8)
- ✅ **4 module Java mới**: `home/`, `herobanner/`, `dailyarrival/`, `review/`
- ✅ **Fix 2 bugs** trong ProductEntity (averageRating, reviewCount)
- ✅ **5 tickets bổ sung** phát sinh từ cross-module analysis

> ⚠️ **CRITICAL**: Bảng `product_reviews` đã có trong schema V1 nhưng **không có Java entity nào**. Module `review/` (HOME-015) phải được xây trước HOME-009 và HOME-010, vì nếu không `featuredReviews`, `averageRating`, `totalReviews` sẽ luôn rỗng/zero.

### Sub-tickets

#### Phase 1 — Foundation & Database

| Ticket | Tên | Estimate | Phụ thuộc |
|---|---|---|---|
| HOME-001 | DB Migrations V3→V7 + V8 seed | 1.5h | — |
| HOME-019 | 🆕 Seed slug cho 7 Categories hiện có | 0.5h | HOME-001 |
| HOME-018 | 🆕 RBAC Permissions cho Home Management | 0.5h | HOME-001 |

#### Phase 2 — Prerequisite: Review Module

| Ticket | Tên | Estimate | Phụ thuộc |
|---|---|---|---|
| **HOME-015** | 🆕 **Module `review` — Toàn diện (TIỀN ĐỀ BLOCKING)** | **4h** | HOME-001 |

#### Phase 3 — Entity & Module Fixes

| Ticket | Tên | Estimate | Phụ thuộc |
|---|---|---|---|
| HOME-002 | Fix ProductEntity (bug rating) + home fields | 1h | HOME-001 |
| HOME-003 | Mở rộng CategoryEntity + image upload endpoint | 1.5h | HOME-001 |
| HOME-004 | Module `herobanner` — Entity + Repo + Mapper | 2h | HOME-001 |
| HOME-005 | Module `herobanner` — Service + Admin Controller | 2h | HOME-004 |
| HOME-017 | 🆕 Image Upload cho Hero Banner & Category (MinIO) | 1h | HOME-005, HOME-003 |
| HOME-006 | Module `dailyarrival` — Entity + Repo + Mapper | 2h | HOME-001 |
| HOME-007 | Module `dailyarrival` — Service + Admin Controller | 2h | HOME-006 |

#### Phase 4 — Home Core

| Ticket | Tên | Estimate | Phụ thuộc |
|---|---|---|---|
| HOME-008 | Home DTOs (10 classes) | 2h | HOME-002, HOME-003 |
| HOME-009 | HomeRepository — Aggregate Queries | 1.5h | HOME-001, **HOME-015** |
| HOME-010 | HomeServiceImpl — Orchestrate 8 queries | 3h | HOME-004→009, HOME-015 |
| HOME-011 | HomeController — Public API | 1h | HOME-010 |

#### Phase 5 — Admin & Infrastructure

| Ticket | Tên | Estimate | Phụ thuộc |
|---|---|---|---|
| HOME-012 | Admin endpoints Product & Category home config | 1.5h | HOME-002, HOME-003 |
| ~~HOME-013~~ | ~~ProductReview Admin~~ → **Merged vào HOME-015** | — | — |
| HOME-014 | Redis Cache — Home module TTL config | 1h | HOME-010 |
| HOME-016 | 🆕 Cache Eviction Chain Cross-Module | 1.5h | HOME-014, HOME-015 |

**Tổng**: 19 tickets — **~33.5 giờ (~4.2 ngày)**

---

## 🗄️ [HOME-001] DB Migrations V3→V7

| Field | Value |
|---|---|
| **ID** | `HOME-001` |
| **Type** | 🗄️ Database |
| **Priority** | 🔴 BLOCKING — Tất cả ticket khác phụ thuộc |
| **Estimate** | **1.5 giờ** |
| **Label** | `database`, `migration`, `flyway` |

### Mô tả
Tạo 5 file migration Flyway để mở rộng schema DB cho toàn bộ home module. Không được sửa V1 và V2 đã có.

### Các bước thực hiện

**Bước 1** — Tạo `V3__alter_categories_add_home_columns.sql`
- [ ] Thêm cột `description TEXT`
- [ ] Thêm cột `slug VARCHAR(100) UNIQUE`
- [ ] Thêm cột `image_url VARCHAR(500)`
- [ ] Thêm cột `badge VARCHAR(100)`
- [ ] Thêm cột `badge_type VARCHAR(20)` với CHECK constraint `('hot','number','fresh','dry')`
- [ ] Thêm cột `icon_name VARCHAR(50)`
- [ ] Thêm cột `home_display_style VARCHAR(10)` với CHECK `('main','card','icon')`
- [ ] Thêm cột `home_sort_order INTEGER NOT NULL DEFAULT 0`
- [ ] Thêm cột `home_is_active BOOLEAN NOT NULL DEFAULT FALSE`

**Bước 2** — Tạo `V4__alter_products_add_home_columns.sql`
- [ ] Thêm `is_featured BOOLEAN NOT NULL DEFAULT FALSE`
- [ ] Thêm `original_price NUMERIC(12,2)`
- [ ] Thêm `spec VARCHAR(255)`
- [ ] Thêm `origin VARCHAR(255)`
- [ ] Thêm `weight_options TEXT[]`
- [ ] Thêm `product_type VARCHAR(20) DEFAULT 'REGULAR'` với CHECK `('REGULAR','COMBO')`
- [ ] Thêm `combo_category`, `combo_theme`, `combo_tag`, `combo_cta_text`, `combo_href`
- [ ] Thêm `is_breakout BOOLEAN NOT NULL DEFAULT FALSE`
- [ ] Thêm `combo_sort_order INTEGER NOT NULL DEFAULT 0`
- [ ] Tạo 2 indexes: `idx_products_is_featured` (partial), `idx_products_product_type`

**Bước 3** — Tạo `V5__alter_product_reviews_add_featured_home.sql`
- [ ] Thêm cột `is_featured_home BOOLEAN NOT NULL DEFAULT FALSE`
- [ ] Tạo partial index `idx_reviews_featured_home WHERE is_featured_home = TRUE`

**Bước 4** — Tạo `V6__create_hero_banners.sql`
- [ ] Tạo bảng `hero_banners` với đầy đủ 23 cột theo contract FE
- [ ] Cột audit: `created_at`, `updated_at`, `created_by`, `updated_by`

**Bước 5** — Tạo `V7__create_daily_arrivals.sql`
- [ ] Tạo bảng `daily_arrivals` với FK → `products(id)`
- [ ] Thêm cột `date DATE` để filter theo ngày
- [ ] Thêm UNIQUE constraint `(product_id, date)`
- [ ] Tạo index `idx_daily_arrivals_date`

**Bước 6** — Verify
- [ ] Chạy app → Flyway tự migrate thành công (không lỗi)
- [ ] Kiểm tra DB: tất cả bảng/cột đã xuất hiện đúng

### Definition of Done
- ✅ App khởi động không lỗi Flyway
- ✅ Tất cả 5 file migration chạy thành công tuần tự
- ✅ Không sửa V1, V2

---

## 🔧 [HOME-002] Fix ProductEntity + Mở rộng cho Home

| Field | Value |
|---|---|
| **ID** | `HOME-002` |
| **Type** | 🐛 Bug Fix + ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **1 giờ** |
| **Label** | `bug`, `entity`, `product` |
| **Phụ thuộc** | HOME-001 |

### Mô tả
`average_rating` và `review_count` đã tồn tại trong DB schema V1 nhưng **không được map vào `ProductEntity`** — `ProductResponseDto` không bao giờ trả về 2 field này. Đây là bug cần fix ngay, đồng thời thêm các field home-specific cho product.

### Các bước thực hiện

**Bước 1** — Fix bug trong `ProductEntity.java`
- [ ] Thêm `private BigDecimal averageRating;` — `@Column(name = "average_rating")`
- [ ] Thêm `private Integer reviewCount;` — `@Column(name = "review_count")`

**Bước 2** — Thêm home-specific fields vào `ProductEntity.java`
- [ ] `isFeatured`, `originalPrice`, `spec`, `origin`
- [ ] `weightOptions` — `@JdbcTypeCode(SqlTypes.ARRAY)` + `@Column(columnDefinition = "text[]")`
- [ ] `productType` (Enum) — `@Enumerated(EnumType.STRING)`
- [ ] `comboCategory`, `comboTheme`, `comboTag`, `comboCtaText`, `comboHref`
- [ ] `isBreakout`, `comboSortOrder`

**Bước 3** — Tạo Enum `ProductType.java`
```java
public enum ProductType { REGULAR, COMBO }
```

**Bước 4** — Cập nhật `ProductResponseDto.java`
- [ ] Thêm: `averageRating`, `reviewCount`, `isFeatured`, `originalPrice`, `spec`, `origin`, `weightOptions`

**Bước 5** — Cập nhật `ProductMapper.java` nếu cần thêm `@Mapping`

**Bước 6** — Cập nhật `UpdateProductRequestDto.java`
- [ ] Thêm fields optional: `isFeatured`, `originalPrice`, `spec`, `origin`

### Definition of Done
- ✅ `GET /api/v1/products/{id}` trả về `averageRating` và `reviewCount`
- ✅ Build không có lỗi compile/MapStruct
- ✅ Không phá vỡ API product hiện có

---

## 🗂️ [HOME-003] Mở Rộng CategoryEntity cho Home Config

| Field | Value |
|---|---|
| **ID** | `HOME-003` |
| **Type** | ✨ Feature |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1 giờ** |
| **Label** | `entity`, `category` |
| **Phụ thuộc** | HOME-001 |

### Mô tả
`CategoryEntity` hiện chỉ có `id` và `name`. Cần mở rộng để lưu thông tin hiển thị trên trang chủ (display style, slug, ảnh, badge...).

### Các bước thực hiện

**Bước 1** — Thêm fields vào `CategoryEntity.java`
- [ ] `description`, `slug`, `imageUrl`
- [ ] `badge`, `badgeType`, `iconName`
- [ ] `homeDisplayStyle`, `homeSortOrder`, `homeIsActive`

**Bước 2** — Tạo `UpdateCategoryHomeConfigRequestDto.java` (record, tất cả nullable)

**Bước 3** — Thêm method `updateHomeConfig()` vào `CategoryServiceImpl.java`
- [ ] Null-safe patch từng field
- [ ] `@CacheEvict(value = "categories", allEntries = true)` + `@CacheEvict("home:categories")`

**Bước 4** — Verify `CategoryMapper` tự map đúng field mới

### Definition of Done
- ✅ Entity compile thành công với 9 fields mới
- ✅ CategoryMapper map đúng
- ✅ API categories hiện có không bị phá vỡ

---

## 🦸 [HOME-004] Module `herobanner` — Entity + Repository + Mapper

| Field | Value |
|---|---|
| **ID** | `HOME-004` |
| **Type** | ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **2 giờ** |
| **Label** | `new-module`, `entity`, `herobanner` |
| **Phụ thuộc** | HOME-001 |

### Mô tả
Tạo tầng data access cho module quản lý Hero Banner (slide lớn đầu trang).

### Các bước thực hiện

**Bước 1** — Tạo package structure
```
herobanner/
├── HeroBannerController.java   ← HOME-005
├── HeroBannerService.java      ← HOME-005
├── dto/
│   ├── HeroBannerResponseDto.java
│   ├── CreateHeroBannerRequestDto.java
│   └── UpdateHeroBannerRequestDto.java
└── internal/
    ├── HeroBannerEntity.java
    ├── HeroBannerRepository.java
    ├── HeroBannerMapper.java
    └── HeroBannerServiceImpl.java  ← HOME-005
```

**Bước 2** — Tạo `HeroBannerEntity.java`
- [ ] Extend `BaseEntity`, `@DynamicUpdate @DynamicInsert`
- [ ] Map tất cả 23 cột: sortOrder, isActive, badgeText, badgeIcon, titlePrefix, titleHighlight, titleSuffix, description, primaryCta*, secondaryCta*, productCard*
- [ ] Override `equals()` và `hashCode()` theo Vlad Mihalcea pattern

**Bước 3** — Tạo `HeroBannerRepository.java`
- [ ] `List<HeroBannerEntity> findByIsActiveTrueOrderBySortOrderAsc()`

**Bước 4** — Tạo DTOs
- [ ] `HeroBannerResponseDto` — map 1-1 với entity
- [ ] `CreateHeroBannerRequestDto` (record) — `@NotBlank` cho required fields
- [ ] `UpdateHeroBannerRequestDto` (record) — tất cả nullable

**Bước 5** — Tạo `HeroBannerMapper.java` (MapStruct)
- [ ] `toResponseDto()`, `toResponseDtoList()`, `toEntity()`

### Definition of Done
- ✅ `./mvnw compile` không lỗi
- ✅ MapStruct generate đúng implementation
- ✅ Repository query `findByIsActiveTrueOrderBySortOrderAsc()` hoạt động

---

## 🎛️ [HOME-005] Module `herobanner` — Service + Admin Controller

| Field | Value |
|---|---|
| **ID** | `HOME-005` |
| **Type** | ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **2 giờ** |
| **Label** | `service`, `controller`, `admin`, `herobanner` |
| **Phụ thuộc** | HOME-004 |

### Mô tả
Business logic và 5 admin endpoints quản lý Hero Banner.

### Các bước thực hiện

**Bước 1** — Tạo `HeroBannerService.java` interface với methods:
`findAllActive()`, `findAll()`, `create()`, `update()`, `delete()`, `toggleActive()`, `updateSortOrder()`

**Bước 2** — Implement `HeroBannerServiceImpl.java`
- [ ] `findAllActive()` → `@Cacheable("home:heroSlides")`
- [ ] `create()` → save → `@CacheEvict("home:heroSlides", allEntries=true)`
- [ ] `update()` → null-safe patch → save → evict
- [ ] `delete()` → ResourceNotFoundException nếu không có → delete → evict
- [ ] `toggleActive()` → flip `isActive` → save → evict

**Bước 3** — Tạo `HeroBannerController.java`
- [ ] `GET /api/v1/admin/hero-banners` — list tất cả (kể cả inactive)
- [ ] `POST /api/v1/admin/hero-banners` — tạo mới → 201 Created
- [ ] `PATCH /api/v1/admin/hero-banners/{id}` — update
- [ ] `DELETE /api/v1/admin/hero-banners/{id}` — xóa
- [ ] `PATCH /api/v1/admin/hero-banners/{id}/toggle` — ẩn/hiện

**Bước 4** — Test thủ công đủ 5 endpoints

### Definition of Done
- ✅ Tất cả 5 endpoint admin hoạt động đúng HTTP status
- ✅ CUSTOMER token → 403 Forbidden
- ✅ Cache evict khi create/update/delete

---

## 📦 [HOME-006] Module `dailyarrival` — Entity + Repository + Mapper

| Field | Value |
|---|---|
| **ID** | `HOME-006` |
| **Type** | ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **2 giờ** |
| **Label** | `new-module`, `entity`, `dailyarrival` |
| **Phụ thuộc** | HOME-001 |

### Mô tả
Tạo tầng data access cho "Hàng Mới Về Hôm Nay" — admin pick sản phẩm mỗi sáng.

### Các bước thực hiện

**Bước 1** — Tạo `DailyArrivalEntity.java`
- [ ] Extend `BaseEntity`
- [ ] `@ManyToOne(fetch = FetchType.LAZY)` → `ProductEntity` (để lấy price, imageUrl)
- [ ] Fields riêng: `date` (LocalDate), `arrivedAt` (LocalDateTime), `badge`, `title`, `description`, `weight`, `origin`, `isActive`

**Bước 2** — Tạo `DailyArrivalRepository.java`
```java
// Hôm nay — JOIN FETCH product tránh N+1
@Query("SELECT d FROM DailyArrivalEntity d JOIN FETCH d.product p " +
       "WHERE d.date = CURRENT_DATE AND d.isActive = true " +
       "ORDER BY d.arrivedAt ASC")
List<DailyArrivalEntity> findTodayActive();

List<DailyArrivalEntity> findByDateOrderByArrivedAtAsc(LocalDate date);
```

**Bước 3** — Tạo DTOs
- [ ] `DailyArrivalResponseDto` — 12 fields theo FE contract (lấy price/imageUrl từ product)
- [ ] `CreateDailyArrivalRequestDto` (record) — productId, date, arrivedAt, badge, title, description, weight, origin
- [ ] `UpdateDailyArrivalRequestDto` (record) — tất cả nullable

**Bước 4** — Tạo `DailyArrivalMapper.java`
- [ ] Custom mapping: `price` ← `entity.getProduct().getPrice()`
- [ ] `imageUrl` ← `entity.getProduct().getImageUrl()`
- [ ] `originalPrice` ← `entity.getProduct().getOriginalPrice()`

### Definition of Done
- ✅ `findTodayActive()` query đúng filter theo ngày hiện tại
- ✅ JOIN FETCH hoạt động — kiểm tra log SQL không có N+1
- ✅ Mapper xử lý đúng nested product fields

---

## 🎛️ [HOME-007] Module `dailyarrival` — Service + Admin Controller

| Field | Value |
|---|---|
| **ID** | `HOME-007` |
| **Type** | ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **2 giờ** |
| **Label** | `service`, `controller`, `admin`, `dailyarrival` |
| **Phụ thuộc** | HOME-006 |

### Mô tả
Business logic và 4 admin endpoints để admin pick sản phẩm hàng ngày.

### Các bước thực hiện

**Bước 1** — Tạo `DailyArrivalService.java` interface:
`findToday()`, `findByDate(LocalDate)`, `create()`, `update()`, `delete()`

**Bước 2** — Implement `DailyArrivalServiceImpl.java`
- [ ] `findToday()` → `@Cacheable("home:dailyArrivals")`
- [ ] `create()`:
  - Validate `productId` tồn tại (ném ResourceNotFoundException nếu không)
  - Validate unique `(productId, date)` (ném BadRequestException nếu trùng)
  - Save → `@CacheEvict("home:dailyArrivals", allEntries=true)`
- [ ] `update()` → null-safe patch → evict
- [ ] `delete()` → find → delete → evict

**Bước 3** — Tạo `DailyArrivalController.java`
- [ ] `GET /api/v1/admin/daily-arrivals?date=2026-08-11` — list theo ngày
- [ ] `POST /api/v1/admin/daily-arrivals` — pick sản phẩm
- [ ] `PATCH /api/v1/admin/daily-arrivals/{id}` — cập nhật
- [ ] `DELETE /api/v1/admin/daily-arrivals/{id}` — xóa

### Definition of Done
- ✅ Admin có thể pick sản phẩm cho ngày bất kỳ
- ✅ Duplicate check hoạt động — cùng sản phẩm, cùng ngày → 400 Bad Request
- ✅ Cache evict khi add/delete → home API refresh

---

## 📋 [HOME-008] Home DTOs — 8 Sub-DTOs cho Public API

| Field | Value |
|---|---|
| **ID** | `HOME-008` |
| **Type** | ✨ Feature |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **2 giờ** |
| **Label** | `dto`, `home` |
| **Phụ thuộc** | HOME-002, HOME-003 |

### Mô tả
Tạo tất cả DTO objects cho `GET /api/v1/home` theo đúng FE contract. Tổng 10 class DTO.

### Các bước thực hiện

**Bước 1** — Tạo `HomePageDataDto.java` (root, 8 fields)

**Bước 2** — `HeroSlideDto.java` với 3 nested objects:
- [ ] `HeroBadgeDto` (text, icon)
- [ ] `HeroCtaDto` (label, href, icon) — dùng chung cho primaryCta + secondaryCta
- [ ] `HeroProductCardDto` (imageUrl, imageAlt, comboBadge, discountBadge, originalPrice, salePrice, title, subtitle)

**Bước 3** — `HomeCategoryDto.java` — 11 fields theo contract

**Bước 4** — `DailyArrivalDto.java` — 12 fields (thêm `imageAlt`)

**Bước 5** — `FeaturedProductDto.java` — 14 fields (thêm `weightOptions`, `origin`, `description`)

**Bước 6** — `FeaturedProductTabDto.java` — 3 fields (slug, label, sortOrder)

**Bước 7** — `ComboSetDto.java` — 13 fields

**Bước 8** — `FeaturedReviewDto.java` — 8 fields

**Bước 9** — `HomeStatsDto.java` — 3 fields

**Bước 10** — Kiểm tra `@JsonFormat` cho tất cả datetime fields (ISO 8601)

### Definition of Done
- ✅ 10 DTO classes compile thành công
- ✅ Field names khớp 100% với FE contract (verify bằng cách compare với `home_api_contract.md`)
- ✅ Nested objects serialize đúng cấu trúc JSON

---

## 🔍 [HOME-009] HomeRepository — Aggregate Queries

| Field | Value |
|---|---|
| **ID** | `HOME-009` |
| **Type** | ✨ Feature |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.5 giờ** |
| **Label** | `repository`, `query`, `home` |
| **Phụ thuộc** | HOME-001 |

### Mô tả
Tạo các JPQL/native queries cần thiết để lấy data từ các bảng hiện có.

### Các bước thực hiện

**Bước 1** — Tạo `HomeRepository.java`

**Bước 2** — Queries cho `HomeStats`:
- [ ] `countDeliveredOrders()` — COUNT orders WHERE status = 'DONE'
- [ ] `getAverageRating()` — AVG rating FROM product_reviews
- [ ] `countTotalReviews()` — COUNT visible reviews

**Bước 3** — Query cho `FeaturedProducts`:
```java
@Query("SELECT p FROM ProductEntity p JOIN FETCH p.category " +
       "WHERE p.isFeatured = true AND p.isActive = true " +
       "ORDER BY p.averageRating DESC")
List<ProductEntity> findFeaturedProducts();
```

**Bước 4** — Query cho `HomeCategories`:
- [ ] JOIN categories + count active products + filter `homeIsActive = true` + sort by `homeSortOrder`

**Bước 5** — Query cho `ComboSets`:
- [ ] Filter `productType = 'COMBO'` + `isActive = true` + sort `comboSortOrder`

**Bước 6** — Query cho `FeaturedReviews`:
- [ ] JOIN product_reviews + users + products + LEFT JOIN user_addresses (isDefault=true)
- [ ] Filter `isFeaturedHome = true` + `isVisible = true`

**Bước 7** — Query cho `FeaturedProductTabs`:
- [ ] DISTINCT categories có featured products, sorted by homeSortOrder

### Definition of Done
- ✅ Tất cả queries compile và trả đúng kiểu
- ✅ Không N+1 — verify bằng `spring.jpa.show-sql=true`
- ✅ Aggregate queries trả đúng số liệu

---

## ⚙️ [HOME-010] HomeServiceImpl — Orchestrate 8 Queries

| Field | Value |
|---|---|
| **ID** | `HOME-010` |
| **Type** | ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **3 giờ** |
| **Label** | `service`, `orchestrator`, `home` |
| **Phụ thuộc** | HOME-004, HOME-005, HOME-006, HOME-007, HOME-008, HOME-009 |

### Mô tả
Trung tâm của home module. Điều phối 8 queries với cache riêng và tổng hợp thành `HomePageDataDto`.

> ⚠️ **Lưu ý kỹ thuật**: `@Cacheable` không hoạt động với private methods (Spring AOP proxy). Tất cả 8 phương thức helper PHẢI là `public` hoặc tách thành beans riêng.

### Các bước thực hiện

**Bước 1** — Tạo `HomeService.java` interface:
```java
HomePageDataDto getHomePageData();
```

**Bước 2** — Implement `HomeServiceImpl.java`
- [ ] `@Service @RequiredArgsConstructor @Transactional(readOnly = true)`
- [ ] Inject: `HomeRepository`, `HeroBannerRepository`, `DailyArrivalRepository` + mappers

**Bước 3** — Implement `getHomePageData()` — gọi 8 methods và build root DTO

**Bước 4** — Implement 8 public cached methods:

| Method | Cache key | TTL |
|---|---|---|
| `getHeroSlides()` | `home:heroSlides` | 10 phút |
| `getHomeCategories()` | `home:categories` | 10 phút |
| `getDailyArrivals()` | `home:dailyArrivals` | 6 giờ |
| `getFeaturedProducts()` | `home:featuredProducts` | 5 phút |
| `getFeaturedProductTabs()` | `home:featuredProductTabs` | 10 phút |
| `getComboSets()` | `home:comboSets` | 10 phút |
| `getFeaturedReviews()` | `home:featuredReviews` | 30 phút |
| `getStats()` | `home:stats` | 30 phút |

**Bước 5** — Xử lý edge cases:
- [ ] Dùng `Collections.emptyList()` khi không có data (không trả null)
- [ ] Round `averageRating` về 1 decimal
- [ ] Compute `avatarInitials` từ `fullName` (lấy chữ đầu 2 từ đầu tiên)
- [ ] Build `customerLocation` = `district + ", " + province`
- [ ] Map `tags` → `badges` cho FeaturedProduct

**Bước 6** — Test: gọi `getHomePageData()` → kiểm tra tất cả 8 fields != null

### Definition of Done
- ✅ Response đầy đủ 8 sub-data, không field nào null (có thể là empty array)
- ✅ Cache hoạt động — verify bằng `redis-cli KEYS "home:*"`
- ✅ Không NPE khi DB trống (empty state)

---

## 🌐 [HOME-011] HomeController — Public API `GET /api/v1/home`

| Field | Value |
|---|---|
| **ID** | `HOME-011` |
| **Type** | ✨ Feature |
| **Priority** | 🔴 HIGH |
| **Estimate** | **1 giờ** |
| **Label** | `controller`, `public-api`, `home` |
| **Phụ thuộc** | HOME-010 |

### Mô tả
Tạo controller cho public API duy nhất mà FE sử dụng. Thêm rate limiting và cập nhật SecurityConfig.

### Các bước thực hiện

**Bước 1** — Tạo `HomeController.java`
```java
@RestController
@RequestMapping("/api/v1/home")
@Tag(name = "Home", description = "Public Home Page Data")
public class HomeController {
    @SecurityRequirements({})
    @RateLimit(type = RateLimitType.PUBLIC_API, byIp = true)
    @GetMapping
    public ResponseEntity<ApiResponse<HomePageDataDto>> getHome() { ... }
}
```

**Bước 2** — Cập nhật `SecurityConfig.java`
- [ ] Thêm `"/api/v1/home"` vào `PUBLIC_GET_URLS`

**Bước 3** — Thêm `Cache-Control` HTTP header trong response

**Bước 4** — Cập nhật `OpenApiConfig.java` — thêm vào nhóm Swagger Public

**Bước 5** — Test thủ công
- [ ] `curl http://localhost:8080/api/v1/home` không cần token → 200 OK
- [ ] Gọi lần 2 → phải nhanh hơn (Redis cache)
- [ ] Test với CUSTOMER token → vẫn 200 OK (public)

### Definition of Done
- ✅ `GET /api/v1/home` accessible công khai (không cần JWT)
- ✅ Response đúng envelope `{ success, message, data }`
- ✅ Swagger hiển thị endpoint với tag "Home"

---

## 🔧 [HOME-012] Admin Endpoints cho Product & Category Home Config

| Field | Value |
|---|---|
| **ID** | `HOME-012` |
| **Type** | ✨ Feature |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.5 giờ** |
| **Label** | `admin`, `product`, `category` |
| **Phụ thuộc** | HOME-002, HOME-003 |

### Mô tả
Bổ sung admin endpoints vào các module hiện có để quản lý home-specific config.

### Các bước thực hiện

**Bước 1** — `ProductController.java` — thêm 2 endpoints:
- [ ] `PATCH /api/v1/products/{id}/featured` — toggle `isFeatured`
- [ ] `PATCH /api/v1/products/{id}/combo-config` — cập nhật combo fields

**Bước 2** — Tạo `UpdateComboConfigRequestDto.java` (record)
- Fields: `productType`, `comboCategory`, `comboTheme`, `comboTag`, `comboCtaText`, `comboHref`, `isBreakout`, `comboSortOrder`

**Bước 3** — `ProductService.java` + `ProductServiceImpl.java` — thêm:
- [ ] `toggleFeatured(Long id)` → flip + `@CacheEvict` products + `home:featuredProducts`
- [ ] `updateComboConfig(Long id, req)` → patch combo fields + evict `home:comboSets`

**Bước 4** — `CategoryController.java` — thêm 1 endpoint:
- [ ] `PATCH /api/v1/admin/categories/{id}/home-config`

**Bước 5** — `CategoryServiceImpl.java` — thêm `updateHomeConfig()`
- [ ] Null-safe patch + `@CacheEvict` categories + `home:categories` + `home:featuredProductTabs`

### Definition of Done
- ✅ `PATCH /featured` toggle đúng + evict cache
- ✅ `PATCH /combo-config` update đúng combo fields
- ✅ `PATCH /home-config` update đúng category home config

---

## ⭐ [HOME-013] ProductReview Admin — Toggle Featured Home

| Field | Value |
|---|---|
| **ID** | `HOME-013` |
| **Type** | ✨ Feature |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1.5 giờ** |
| **Label** | `admin`, `review` |
| **Phụ thuộc** | HOME-001 |

### Mô tả
Tạo admin interface để xem và chọn reviews hiển thị trên trang chủ.

### Các bước thực hiện

**Bước 1** — Kiểm tra / tạo `ProductReviewEntity.java`
- [ ] Thêm field `private Boolean isFeaturedHome = false;`

**Bước 2** — Tạo / cập nhật `ProductReviewRepository.java`
- [ ] `findFeaturedHomeReviews()` — WHERE isFeaturedHome = true AND isVisible = true
- [ ] `findAll(Pageable)` với filter productId cho admin

**Bước 3** — Tạo `AdminReviewController.java`
- [ ] `GET /api/v1/admin/reviews?productId={id}` — list reviews để curate
- [ ] `PATCH /api/v1/admin/reviews/{id}/featured-home` — toggle `isFeaturedHome`
- [ ] `PATCH /api/v1/admin/reviews/{id}/visibility` — toggle `isVisible`

**Bước 4** — Tạo `AdminReviewService.java` + `AdminReviewServiceImpl.java`
- [ ] `toggleFeaturedHome()` → flip + `@CacheEvict("home:featuredReviews")`
- [ ] `toggleVisibility()` → flip `isVisible`

**Bước 5** — Tạo `AdminReviewResponseDto.java`
- Fields: id, customerName (JOIN user), productName (JOIN product), rating, comment, isFeaturedHome, isVisible, createdAt

### Definition of Done
- ✅ Admin có thể list và curate reviews
- ✅ Toggle featured evict cache `home:featuredReviews`
- ✅ Chỉ ADMIN mới access (403 với non-admin token)

---

## 💾 [HOME-014] Redis Cache — Cấu Hình Cho Home Module

| Field | Value |
|---|---|
| **ID** | `HOME-014` |
| **Type** | 🔧 Infrastructure |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **1 giờ** |
| **Label** | `cache`, `redis`, `infrastructure` |
| **Phụ thuộc** | HOME-010 |

### Mô tả
Cấu hình TTL riêng cho từng cache key của home module trong `RedisCacheConfig`.

### Các bước thực hiện

**Bước 1** — Cập nhật `RedisCacheConfig.java`

| Cache Key | TTL | Lý do |
|---|---|---|
| `home:heroSlides` | 10 phút | Admin cập nhật thủ công |
| `home:categories` | 10 phút | Ít thay đổi |
| `home:dailyArrivals` | 6 giờ | Cập nhật mỗi sáng |
| `home:featuredProducts` | 5 phút | Stock có thể thay đổi |
| `home:featuredProductTabs` | 10 phút | Ít thay đổi |
| `home:comboSets` | 10 phút | Ít thay đổi |
| `home:featuredReviews` | 30 phút | Admin curate thủ công |
| `home:stats` | 30 phút | Aggregate, không real-time |

**Bước 2** — Verify bằng `redis-cli`
- [ ] `KEYS "home:*"` → thấy 8 keys sau lần gọi đầu
- [ ] `TTL "home:stats"` → ~1800 giây

**Bước 3** — Thêm `Cache-Control` HTTP header vào `HomeController`
```java
.cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).mustRevalidate())
```

**Bước 4** — End-to-end cache eviction test
- [ ] Tạo hero banner → gọi `GET /api/v1/home` → hero slide mới xuất hiện
- [ ] Toggle featured product → `GET /api/v1/home` → featuredProducts cập nhật

### Definition of Done
- ✅ 8 cache keys với đúng TTL
- ✅ Cache evict đúng sau mỗi write admin operation
- ✅ Response time lần 2 < 50ms (Redis hit)

---

## 📊 TỔNG KẾT & TIMELINE

### Ước Tính Thời Gian

| Giai đoạn | Tickets | Thời gian |
|---|---|---|
| **Phase 1 — Foundation** | HOME-001, HOME-002, HOME-003 | 3.5h |
| **Phase 2 — New Modules** | HOME-004, HOME-005, HOME-006, HOME-007 | 8h |
| **Phase 3 — Home Core** | HOME-008, HOME-009, HOME-010, HOME-011 | 7.5h |
| **Phase 4 — Admin & Cache** | HOME-012, HOME-013, HOME-014 | 4h |
| **Buffer (debug, review)** | — | 5h |
| **Tổng** | **14 tickets** | **~28 giờ (~3.5 ngày)** |

### Thứ Tự Thực Hiện

```
Day 1 (8h):  HOME-001 → HOME-002 → HOME-003 → HOME-004
Day 2 (8h):  HOME-005 → HOME-006 → HOME-007 → HOME-008
Day 3 (8h):  HOME-009 → HOME-010 → HOME-011 → HOME-014
Day 4 (4h):  HOME-012 → HOME-013 → Integration Test tổng
```

### Dependency Graph

```
HOME-001 (DB)
    ├── HOME-002 (ProductEntity fix)
    │       └── HOME-008 (DTOs)
    │               └── HOME-010 (HomeServiceImpl)
    │                       └── HOME-011 (HomeController) ✅
    ├── HOME-003 (CategoryEntity)
    │       └── HOME-008 ↑
    ├── HOME-004 (HeroBanner data layer)
    │       └── HOME-005 (HeroBanner service/controller) ✅
    │               └── HOME-010 ↑
    ├── HOME-006 (DailyArrival data layer)
    │       └── HOME-007 (DailyArrival service/controller) ✅
    │               └── HOME-010 ↑
    ├── HOME-009 (HomeRepository queries)
    │       └── HOME-010 ↑
    │               └── HOME-014 (Cache config) ✅
    ├── HOME-012 (Product/Category admin endpoints)
    └── HOME-013 (Review admin endpoints)
```

### Rủi Ro & Giải Pháp

| Rủi ro | Giải pháp |
|---|---|
| N+1 query trong HomeServiceImpl | Dùng `JOIN FETCH` trong tất cả repository queries |
| `@Cacheable` không hoạt động trên private methods | Các helper methods PHẢI là `public` |
| Cache eviction bỏ sót | Test manual sau mỗi admin write operation |
| MapStruct không map đúng `TEXT[]` array | Thêm `@JdbcTypeCode(SqlTypes.ARRAY)` + test riêng |

---
---

# 🔗 PHÂN TÍCH LIÊN KẾT LIÊN MODULE — HOME × TOÀN DỰ ÁN

> **Mục đích**: Nhìn lại toàn bộ 9 module hiện có để xác định tác động chéo, phụ thuộc ẩn, và các vấn đề nghiêm trọng mà việc xây dựng Home module sẽ phơi bày.

---

## 🗺️ BẢN ĐỒ MODULE HIỆN TẠI

| Module | Controller | Service | Entity/Table | Trạng thái |
|---|---|---|---|---|
| `auth` | AuthController | AuthService | users, refresh_tokens, otp_verifications | ✅ Hoàn thiện |
| `user` | UserController, RbacAdminController | UserService, RbacAdminService | users, roles, permissions | ✅ Hoàn thiện |
| `user/address` | AddressController | AddressService | user_addresses | ✅ Hoàn thiện |
| `category` | CategoryController | CategoryService | categories | ⚠️ Thiếu home fields |
| `product` | ProductController | ProductService | products | ⚠️ Thiếu home fields + bug rating |
| `order` | OrderController | OrderService | orders, order_items | ✅ Hoàn thiện |
| `payment` | PaymentController | PaymentService | payments | ✅ Hoàn thiện (VNPay) |
| `notification` | — | EmailService | — | ✅ Tích hợp trong Order |
| `storage` | — | StorageService | MinIO | ✅ Dùng trong Product/User |
| `product_reviews` | ❌ KHÔNG CÓ | ❌ KHÔNG CÓ | ❌ KHÔNG CÓ | 🔴 **CRITICAL GAP** |

---

## 🔴 PHÁT HIỆN NGHIÊM TRỌNG (BLOCKING)

### CRITICAL-001 — Bảng `product_reviews` tồn tại trong DB nhưng **KHÔNG có Java entity nào**

Đây là vấn đề **quan trọng nhất** phơi bày khi phân tích Home module.

**Hiện trạng:**
- Schema V1 đã tạo bảng `product_reviews` với đầy đủ cột: `id`, `product_id`, `user_id`, `order_id`, `rating`, `comment`, `is_visible`, `created_at`...
- **Nhưng trong toàn bộ Java source code: không có `ProductReviewEntity`, không có `ReviewRepository`, không có `ReviewService`, không có `ReviewController`**
- `product.average_rating` và `product.review_count` đã có trong DB nhưng **không bao giờ được cập nhật** vì không có cơ chế write review

**Tác động trực tiếp lên Home module:**
- `homeStats.averageRating` → sẽ trả về `0.0` dù fix entity bug
- `homeStats.totalReviews` → sẽ trả về `0`
- `featuredReviews` → sẽ luôn là `[]` (empty)
- `featuredProduct.rating` → sẽ luôn là `0.0`
- `featuredProduct.reviewCount` → sẽ luôn là `0`

**Kết luận**: **Toàn bộ review-dependent data của Home sẽ là placeholder rỗng** cho đến khi module Review được xây dựng hoàn chỉnh.

---

### CRITICAL-002 — Cache Eviction Chain bị thiếu xuyên suốt các module

**Vấn đề**: Khi dữ liệu thay đổi ở module khác, `home:*` cache không được tự động invalidate:

| Sự kiện xảy ra ở module khác | Cache home bị stale | Hiện tại có evict không? |
|---|---|---|
| Order status → `DONE` | `home:stats.totalOrdersDelivered` | ❌ Không |
| Product stock thay đổi (order create) | `home:featuredProducts` | ❌ Không |
| Product `is_active` → false | `home:featuredProducts`, `home:comboSets` | ❌ Không |
| Category update | `home:categories`, `home:featuredProductTabs` | ❌ Không |
| Review mới được tạo | `home:stats.averageRating` | ❌ Không |

**Giải pháp**: Thêm `@CacheEvict("home:stats")` vào `OrderServiceImpl.changeStatus()` khi status = DONE; thêm `@CacheEvict("home:featuredProducts")` vào `ProductServiceImpl.decreaseStock()`.

---

## 🟠 VẤN ĐỀ ẢNH HƯỞNG TRUNG BÌNH

### IMPACT-003 — StorageService (MinIO) cần được tích hợp vào Home module

**Hiện trạng**: `StorageService` đã hoạt động tốt trong:
- `ProductController.uploadProductImage()` — upload ảnh product
- `UserServiceImpl.uploadUserImage()` — upload avatar

**Tác động với Home**:
- `HeroBannerEntity.productCardImageUrl` — admin cần upload ảnh hero banner → **cần endpoint upload ảnh trong HeroBannerController**
- `DailyArrivalEntity` → dùng imageUrl từ Product (đã có) — OK, không cần upload mới
- `CategoryEntity.imageUrl` → admin cần upload ảnh category → **cần endpoint upload trong CategoryController**

**Thiếu**: HOME-005 (HeroBannerController) và HOME-003/HOME-012 (CategoryController) cần thêm endpoint:
```
POST /api/v1/admin/hero-banners/{id}/image    ← THIẾU trong plan hiện tại
POST /api/v1/admin/categories/{id}/image      ← THIẾU trong plan hiện tại
```

---

### IMPACT-004 — RBAC Permissions chưa bao gồm Home management

**Hiện trạng**: Danh sách permissions trong V2 seed:
```
PRODUCT_CREATE, PRODUCT_UPDATE, PRODUCT_DELETE, PRODUCT_VIEW
CATEGORY_CREATE, CATEGORY_UPDATE, CATEGORY_DELETE
ORDER_VIEW, ORDER_VIEW_ALL, ORDER_UPDATE_STATUS, ORDER_CANCEL
USER_MANAGE, ROLE_MANAGE, PAYMENT_VIEW
```

**Thiếu hoàn toàn cho Home**:
- `HOME_HERO_MANAGE` — quản lý hero banners
- `HOME_ARRIVAL_MANAGE` — quản lý daily arrivals
- `HOME_REVIEW_CURATE` — chọn reviews featured
- `HOME_CONTENT_MANAGE` — toggle featured products/categories

**Tác động**: Hiện tại tất cả admin endpoints dùng `hasRole('ADMIN')` — đủ dùng ngay, nhưng nếu sau này có STAFF role quản lý nội dung home thì cần permissions này.

---

### IMPACT-005 — OrderStatus `DONE` vs tên `totalOrdersDelivered` trong HomeStats

**Phát hiện**: Home contract FE yêu cầu `totalOrdersDelivered` (đã giao), nhưng trong DB/business logic:
- `OrderStatus.DONE` = đơn hoàn thành (không phải `DELIVERED`)
- Schema không có status `DELIVERED` — flow là: `PENDING → CONFIRMED → SHIPPED → DONE`

**Tác động**: Query trong `HomeRepository`:
```java
// ✅ Đúng — DONE là trạng thái cuối = đã giao thành công
"SELECT COUNT(o) FROM OrderEntity o WHERE o.status = 'DONE'"
```
Nhưng **không có status `CANCELLED` bị tính vào** — logic đúng. Chỉ cần document rõ `DONE = delivered` để tránh nhầm.

---

### IMPACT-006 — ProductController có Analytics nhưng không liên kết với HomeStats

**Hiện trạng**: `ProductRepository` đã có:
- `getTopViewProduct()` — top bán chạy (có thể dùng để quyết định `isFeatured` auto)
- `getCategoryRevenue()` — doanh thu theo category
- `getMonthlyRevenue()` — doanh thu theo tháng

**Cơ hội tái sử dụng cho Home**:
- `getTopViewProduct(8)` → có thể auto-suggest sản phẩm để admin mark `isFeatured`
- Admin dashboard endpoint có thể aggregate: số order, doanh thu, top products → reuse trong một `AdminDashboardController` riêng

**Đề xuất**: Thêm ticket `HOME-015` — Admin Dashboard Stats API tổng hợp (optional, sau khi core home xong).

---

### IMPACT-007 — Category hiện không có `slug` — FeaturedProductTabs bị ảnh hưởng

**Hiện trạng**: `CategoryEntity` chỉ có `id` và `name` (VD: "Tôm", "Cá", "Mực & Bạch tuộc"). Không có `slug`.

**FE cần**: `categorySlug` trên mỗi `FeaturedProduct` và `slug` trên `FeaturedProductTab` (VD: `"tom-cua"`, `"muc-bach-tuoc"`).

**Tác động**: Sau khi HOME-003 thêm `slug` vào `CategoryEntity`:
- CategoryController cần cập nhật: `GET /api/v1/categories` nên trả về `slug` trong response
- `ProductResponseDto.categoryLabel` và `categorySlug` cần được map từ `category.name` và `category.slug`
- **Tất cả 7 categories hiện có** cần được update slug thủ công bởi admin sau khi migration chạy

**Cần thêm bước vào HOME-003**: Tạo data migration hoặc hướng dẫn admin seed slug cho categories hiện có.

---

## 🟡 VẤN ĐỀ NHỎ CẦN GHI NHẬN

### MINOR-008 — PaymentController hardcode FE URL

```java
// Trong PaymentController.vnPayReturn():
String feUrl = "http://localhost:3000/";  // ← Hardcoded!
```
URL FE đang hardcode → sẽ fail trong production/staging. Cần externalize vào `application.yaml`. Không liên quan Home nhưng phát hiện trong quá trình review.

### MINOR-009 — OrderServiceImpl gửi email khi tạo đơn — không rollback khi email fail

```java
// Trong OrderServiceImpl.create():
emailService.sendPaymentSuccessEmail(order.getUser().getEmail(), order.getId());
// ← Email send xảy ra TRONG @Transactional, nếu email fail sẽ rollback cả đơn hàng!
```
Email nên được gọi **sau khi commit transaction** (dùng `@TransactionalEventListener` hoặc gọi sau `save()`). Không liên quan Home nhưng là bug tiềm ẩn.

### MINOR-010 — CategoryController `GET /api/v1/categories` chỉ trả về `countActiveCategories()`

Endpoint hiện tại dùng `countActiveCategories()` thay vì list đầy đủ → response chỉ có `id`, `name`, `productCount`. Sau khi HOME-003 thêm slug và home fields, cần thêm endpoint riêng hoặc mở rộng response hiện có để FE có thể dùng slug cho navigation.

---

## 📋 TICKETS BỔ SUNG TỪ PHÂN TÍCH LIÊN MODULE

### 🔴 [HOME-015] Xây Dựng Module `review` — TIỀN ĐỀ THIẾT YẾU

| Field | Value |
|---|---|
| **ID** | `HOME-015` |
| **Type** | 🆕 New Module — TIỀN ĐỀ |
| **Priority** | 🔴 HIGH — Block HOME-009, HOME-010, HOME-013 |
| **Estimate** | **4 giờ** |
| **Label** | `new-module`, `review`, `prerequisite` |
| **Phụ thuộc** | HOME-001 (V5 migration) |

#### Mô tả

**Đây là phát hiện quan trọng nhất**: Bảng `product_reviews` tồn tại trong DB schema V1 nhưng **không có Java entity, không có repository, không có service, không có controller nào** trong toàn bộ codebase. Tất cả dữ liệu liên quan đến review trong Home (`featuredReviews`, `stats.averageRating`, `stats.totalReviews`, `featuredProduct.rating`) sẽ là **rỗng/zero** cho đến khi module này được xây dựng.

#### Các bước thực hiện

**Bước 1** — Tạo package `review/`
```
src/main/java/com/devfat/mini_ecommerce/review/
├── ReviewController.java        ← Customer: viết review sau khi đơn DONE
├── ReviewService.java
├── dto/
│   ├── CreateReviewRequestDto.java
│   ├── ReviewResponseDto.java
│   └── AdminReviewResponseDto.java
└── internal/
    ├── ProductReviewEntity.java   ← MAP bảng product_reviews đã có!
    ├── ProductReviewRepository.java
    ├── ReviewMapper.java
    └── ReviewServiceImpl.java
```

**Bước 2** — Tạo `ProductReviewEntity.java`
- [ ] Extend `BaseEntity`
- [ ] `@ManyToOne` → `ProductEntity` (product_id)
- [ ] `@ManyToOne` → `UserEntity` (user_id)
- [ ] `@ManyToOne` → `OrderEntity` (order_id) — validate chỉ review sản phẩm đã mua
- [ ] Fields: `rating` (SmallInt 1-5), `comment` (TEXT), `isVisible` (Boolean)
- [ ] Fields mới (từ V5 migration): `isFeaturedHome` (Boolean)
- [ ] UNIQUE constraint: `(user_id, order_id, product_id)` — mỗi sản phẩm trong 1 đơn chỉ review 1 lần

**Bước 3** — Tạo `ProductReviewRepository.java`
```java
// Home queries
List<ProductReviewEntity> findByIsFeaturedHomeTrueAndIsVisibleTrueOrderByCreatedAtDesc();

// Customer queries
Page<ProductReviewEntity> findByProductIdAndIsVisibleTrue(Long productId, Pageable pageable);
boolean existsByUserIdAndOrderIdAndProductId(Long userId, Long orderId, Long productId);

// Stats queries (cho HomeRepository tái sử dụng)
@Query("SELECT COALESCE(AVG(CAST(r.rating AS double)), 0.0) FROM ProductReviewEntity r WHERE r.isVisible = true")
Double getAverageRating();

@Query("SELECT COUNT(r) FROM ProductReviewEntity r WHERE r.isVisible = true")
Long countTotalReviews();
```

**Bước 4** — Tạo `ReviewServiceImpl.java`
- [ ] `createReview(userId, req)`:
  - Validate order tồn tại và thuộc về user
  - Validate order status = `DONE` (chỉ review sau khi nhận hàng)
  - Validate chưa review sản phẩm này trong đơn này
  - Save review → **cập nhật `averageRating` và `reviewCount` trên ProductEntity** (trigger update)
  - `@CacheEvict("home:stats")` và `@CacheEvict("home:featuredReviews")`
- [ ] `findByProductId(productId, pageable)` — public list reviews theo product

**Bước 5** — Tạo `ReviewController.java`
```java
// Customer API (cần auth)
POST /api/v1/reviews                           ← Viết review
GET  /api/v1/reviews/product/{productId}       ← List reviews của 1 sản phẩm (public)

// Admin APIs (cần ADMIN role)
GET  /api/v1/admin/reviews                     ← List tất cả reviews
PATCH /api/v1/admin/reviews/{id}/featured-home ← Toggle featured home
PATCH /api/v1/admin/reviews/{id}/visibility    ← Toggle visibility
```

**Bước 6** — Sau khi customer viết review, tự động cập nhật `products.average_rating` và `products.review_count`:
```java
// Trong ReviewServiceImpl.createReview():
@Query("UPDATE products SET " +
    "average_rating = (SELECT AVG(rating) FROM product_reviews WHERE product_id = :productId AND is_visible = true), " +
    "review_count = (SELECT COUNT(*) FROM product_reviews WHERE product_id = :productId AND is_visible = true) " +
    "WHERE id = :productId")
@Modifying
void recalculateProductRating(@Param("productId") Long productId);
```

**Bước 7** — Di chuyển admin review endpoints từ HOME-013 vào module này (HOME-013 merge vào HOME-015)

#### Definition of Done
- ✅ `POST /api/v1/reviews` — customer viết review sau khi đơn DONE
- ✅ `products.average_rating` tự động cập nhật sau mỗi review mới
- ✅ `GET /api/v1/reviews/product/{id}` — list reviews của product (public)
- ✅ Admin có thể toggle `isFeaturedHome` → home API phản ánh ngay
- ✅ `homeStats.averageRating` và `totalReviews` có data thực

---

### 🟠 [HOME-016] Cache Eviction Chain — Cross-Module Integration

| Field | Value |
|---|---|
| **ID** | `HOME-016` |
| **Type** | 🔧 Infrastructure — Cross Module |
| **Priority** | 🟠 MEDIUM-HIGH |
| **Estimate** | **1.5 giờ** |
| **Label** | `cache`, `cross-module`, `integration` |
| **Phụ thuộc** | HOME-014 |

#### Mô tả

Sau khi home module có cache, cần bổ sung eviction vào các module hiện có để đảm bảo tính nhất quán dữ liệu khi có sự kiện xảy ra ở module khác.

#### Các bước thực hiện

**Bước 1** — `OrderServiceImpl.changeStatus()` — khi đơn chuyển sang `DONE`:
```java
// Thêm vào method changeStatus(), sau khi status = DONE:
@CacheEvict(value = "home:stats", allEntries = true)
// hoặc dùng CacheManager để evict programmatically
```

**Bước 2** — `ProductServiceImpl.decreaseStock()` — khi stock giảm (mua hàng):
```java
@CacheEvict(value = "home:featuredProducts", allEntries = true)
// Vì featured products hiển thị từ product, khi stock thay đổi UI cần reflect
```

**Bước 3** — `ProductServiceImpl.softDelete()` — khi product bị deactivate:
```java
@CacheEvict(value = {"home:featuredProducts", "home:comboSets"}, allEntries = true)
```

**Bước 4** — `ReviewServiceImpl.createReview()` — khi có review mới:
```java
@CacheEvict(value = {"home:stats", "home:featuredProducts"}, allEntries = true)
// stats.averageRating thay đổi, product.rating thay đổi
```

**Bước 5** — Test end-to-end:
- [ ] Tạo đơn hàng → giao (DONE) → gọi `GET /api/v1/home` → `totalOrdersDelivered` tăng
- [ ] Mua sản phẩm (giảm stock) → `featuredProducts` cache evict → reload mới

#### Definition of Done
- ✅ Tất cả 4 cross-module eviction hoạt động đúng
- ✅ Home data nhất quán sau các sự kiện từ module khác
- ✅ Không có stale cache quá TTL đã cấu hình

---

### 🟠 [HOME-017] Image Upload cho Hero Banner & Category

| Field | Value |
|---|---|
| **ID** | `HOME-017` |
| **Type** | ✨ Feature |
| **Priority** | 🟠 MEDIUM |
| **Estimate** | **1 giờ** |
| **Label** | `storage`, `image`, `herobanner`, `category` |
| **Phụ thuộc** | HOME-005 (HeroBannerController), HOME-003 (CategoryController) |

#### Mô tả

`StorageService` (MinIO) đã hoạt động tốt trong Product và User module. Cần tích hợp vào 2 endpoints mới cho home content management.

#### Các bước thực hiện

**Bước 1** — Thêm vào `HeroBannerController.java`:
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
public ResponseEntity<ApiResponse<HeroBannerResponseDto>> uploadBannerImage(
    @PathVariable Long id,
    @RequestParam("file") MultipartFile file
)
```

**Bước 2** — Thêm vào `CategoryController.java`:
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
public ResponseEntity<ApiResponse<CategoryResponseDto>> uploadCategoryImage(
    @PathVariable Long id,
    @RequestParam("file") MultipartFile file
)
```

**Bước 3** — Implement trong Service: gọi `storageService.uploadFile(file, "bannerImage", true)` → lưu URL vào entity → `@CacheEvict` tương ứng

#### Definition of Done
- ✅ Admin upload ảnh hero banner → URL lưu vào `hero_banners.product_card_image_url`
- ✅ Admin upload ảnh category → URL lưu vào `categories.image_url`
- ✅ Sử dụng đúng `StorageService` pattern hiện có (không tạo mới)

---

### 🟡 [HOME-018] RBAC — Thêm Permissions cho Home Management

| Field | Value |
|---|---|
| **ID** | `HOME-018` |
| **Type** | 🔒 Security / RBAC |
| **Priority** | 🟡 LOW-MEDIUM |
| **Estimate** | **0.5 giờ** |
| **Label** | `rbac`, `permission`, `migration` |
| **Phụ thuộc** | HOME-001 |

#### Mô tả

Thêm permissions mới cho home content management để STAFF có thể được giao quyền quản lý nội dung trang chủ mà không cần quyền ADMIN toàn phần.

#### Các bước thực hiện

**Bước 1** — Thêm vào V8 migration (hoặc V7 nếu chưa tạo xong):
```sql
INSERT INTO permissions (code, description, created_by, updated_by) VALUES
('HOME_HERO_MANAGE',    'Quản lý Hero Banner trang chủ',     'system', 'system'),
('HOME_ARRIVAL_MANAGE', 'Quản lý Hàng mới về hàng ngày',    'system', 'system'),
('HOME_REVIEW_CURATE',  'Chọn đánh giá nổi bật trang chủ',  'system', 'system'),
('HOME_CONTENT_MANAGE', 'Cấu hình hiển thị trang chủ',      'system', 'system'),
('REVIEW_MODERATE',     'Kiểm duyệt đánh giá sản phẩm',     'system', 'system');
```

**Bước 2** — Grant permissions cho ADMIN role (via JOIN query trong migration)

**Bước 3** — Cập nhật `@PreAuthorize` trong các admin endpoints của Home module:
```java
// Thay vì chỉ hasRole('ADMIN'), cho phép cả STAFF có permission:
@PreAuthorize("hasRole('ADMIN') or hasAuthority('home:hero:manage')")
```

#### Definition of Done
- ✅ 5 permissions mới xuất hiện trong `GET /api/v1/admin/rbac/permissions`
- ✅ ADMIN role được grant tất cả permissions mới
- ✅ STAFF có thể được assign quyền home management qua RBAC API

---

### 🟡 [HOME-019] Seed Slug cho Categories Hiện Có

| Field | Value |
|---|---|
| **ID** | `HOME-019` |
| **Type** | 🗄️ Data Migration |
| **Priority** | 🟡 MEDIUM |
| **Estimate** | **0.5 giờ** |
| **Label** | `migration`, `data-seed`, `category` |
| **Phụ thuộc** | HOME-001 (V3 migration) |

#### Mô tả

Sau khi V3 migration thêm cột `slug` vào `categories`, 7 categories hiện có cần được seed slug ngay để `FeaturedProductTabs` và category navigation hoạt động. Đây là data migration, không phải schema migration.

#### Các bước thực hiện

**Bước 1** — Tạo V8 migration (hoặc thêm vào cuối V3):
```sql
-- Seed slug và home config cho 7 categories hiện có
UPDATE categories SET
    slug = 'tom',
    description = 'Tôm tươi sống đánh bắt từ biển Phan Thiết',
    home_sort_order = 1,
    home_is_active = TRUE,
    home_display_style = 'icon',
    icon_name = 'shell',
    badge_type = 'fresh'
WHERE name = 'Tôm';

UPDATE categories SET slug = 'ca', home_sort_order = 2, home_is_active = TRUE, home_display_style = 'card' WHERE name = 'Cá';
UPDATE categories SET slug = 'muc', home_sort_order = 3, home_is_active = TRUE, home_display_style = 'icon' WHERE name = 'Mực';
UPDATE categories SET slug = 'cua-ghe', home_sort_order = 4, home_is_active = TRUE, home_display_style = 'icon' WHERE name = 'Cua - Ghẹ';
UPDATE categories SET slug = 'oc-so-ngheu', home_sort_order = 5, home_is_active = TRUE, home_display_style = 'icon' WHERE name = 'Ốc - Sò - Nghêu';
UPDATE categories SET slug = 'hai-san-kho', home_sort_order = 6, home_is_active = TRUE, home_display_style = 'card' WHERE name = 'Hải sản khô';
UPDATE categories SET slug = 'nuoc-mam-gia-vi', home_sort_order = 7, home_is_active = FALSE WHERE name = 'Nước mắm - Gia vị';
```

**Bước 2** — Verify: `GET /api/v1/home` → `categories[].slug` không null

#### Definition of Done
- ✅ Tất cả 7 categories có slug
- ✅ `featuredProductTabs` trả về đúng tab labels
- ✅ FE có thể dùng slug để build filter URL `/products?category={slug}`

---

## 📊 BẢNG TỔNG KẾT LIÊN KẾT MODULE

| Home Sub-data | Module phụ thuộc | Trạng thái hiện tại | Rủi ro |
|---|---|---|---|
| `heroSlides` | — (độc lập) | ❌ Chưa có | Thấp — xây mới hoàn toàn |
| `categories` | Category module | ⚠️ Thiếu fields | Trung bình — cần slug + home config |
| `dailyArrivals` | Product module | ❌ Chưa có | Thấp — xây mới, JOIN product |
| `featuredProducts` | Product module | ⚠️ Thiếu fields + bug | Trung bình — fix + mở rộng |
| `featuredProductTabs` | Category module | ⚠️ Thiếu slug | Trung bình — blocker là slug |
| `comboSets` | Product module | ❌ Thiếu product_type | Trung bình — alter table |
| `featuredReviews` | **Review module** | 🔴 KHÔNG CÓ MODULE | **CRITICAL** |
| `stats.totalOrders` | Order module | ✅ Có thể query | Thấp |
| `stats.avgRating` | **Review module** | 🔴 KHÔNG CÓ MODULE | **CRITICAL** |
| `stats.totalReviews` | **Review module** | 🔴 KHÔNG CÓ MODULE | **CRITICAL** |

---

## 📋 BẢNG TICKET CẬP NHẬT ĐẦY ĐỦ (V2)

| Ticket | Tên | Estimate | Priority | Phụ thuộc |
|---|---|---|---|---|
| HOME-001 | DB Migrations V3→V7 | 1.5h | 🔴 BLOCKING | — |
| HOME-002 | Fix ProductEntity + home fields | 1h | 🔴 HIGH | HOME-001 |
| HOME-003 | Mở rộng CategoryEntity + image upload endpoint | 1.5h | 🔴 HIGH | HOME-001 |
| HOME-004 | `herobanner` — Entity + Repo + Mapper | 2h | 🔴 HIGH | HOME-001 |
| HOME-005 | `herobanner` — Service + Admin Controller | 2h | 🔴 HIGH | HOME-004 |
| HOME-006 | `dailyarrival` — Entity + Repo + Mapper | 2h | 🔴 HIGH | HOME-001 |
| HOME-007 | `dailyarrival` — Service + Admin Controller | 2h | 🔴 HIGH | HOME-006 |
| HOME-008 | Home DTOs (10 classes) | 2h | 🟡 MEDIUM | HOME-002, HOME-003 |
| HOME-009 | HomeRepository — Aggregate Queries | 1.5h | 🟡 MEDIUM | HOME-001, HOME-015 |
| HOME-010 | HomeServiceImpl — Orchestrate 8 queries | 3h | 🔴 HIGH | HOME-004→009 |
| HOME-011 | HomeController — Public API | 1h | 🔴 HIGH | HOME-010 |
| HOME-012 | Admin endpoints Product & Category home config | 1.5h | 🟡 MEDIUM | HOME-002, HOME-003 |
| ~~HOME-013~~ | ~~ProductReview Admin~~ | — | **Merge vào HOME-015** | — |
| HOME-014 | Redis Cache — Home module config | 1h | 🟡 MEDIUM | HOME-010 |
| **HOME-015** | **🆕 Module `review` — Toàn diện (TIỀN ĐỀ)** | **4h** | **🔴 CRITICAL** | **HOME-001** |
| **HOME-016** | **🆕 Cache Eviction Chain Cross-Module** | **1.5h** | **🟠 MEDIUM-HIGH** | **HOME-014, HOME-015** |
| **HOME-017** | **🆕 Image Upload Hero Banner & Category** | **1h** | **🟠 MEDIUM** | **HOME-005, HOME-003** |
| **HOME-018** | **🆕 RBAC Permissions cho Home Management** | **0.5h** | **🟡 LOW-MEDIUM** | **HOME-001** |
| **HOME-019** | **🆕 Seed Slug cho Categories Hiện Có** | **0.5h** | **🟡 MEDIUM** | **HOME-001** |

**Tổng ước tính cập nhật**: ~33.5 giờ (~4.2 ngày)

---

## 🗓️ TIMELINE CẬP NHẬT

```
Day 1 (8h):  HOME-001 → HOME-019 → HOME-015 (bắt đầu review module)
Day 2 (8h):  HOME-015 (xong) → HOME-002 → HOME-003 → HOME-018
Day 3 (8h):  HOME-004 → HOME-005 → HOME-017 → HOME-006
Day 4 (8h):  HOME-007 → HOME-008 → HOME-009 → HOME-012
Day 5 (5h):  HOME-010 → HOME-011 → HOME-014 → HOME-016
```

> ⚠️ **HOME-015 (Review module) phải làm NGAY ở Day 1-2** vì nó là tiền đề của HOME-009 (HomeRepository aggregate queries cho stats), HOME-010 (HomeServiceImpl với featuredReviews), và HOME-013 (admin review management).
