# API Contract — `GET /api/v1/home`

> Mọi field được cross-check trực tiếp từ source code component FE.  
> Không có field thừa, không thiếu field nào FE đang dùng.

---

## Response Envelope (theo contract chung)

```json
{
  "success": true,
  "message": "OK",
  "data": { /* HomePageData */ }
}
```

---

## `HomePageData` — Root Object

Gồm **7 sub-data** tương ứng với 7 section cần API:

| Key | Kiểu | Section tương ứng |
|---|---|---|
| `heroSlides` | `HeroSlide[]` | HeroSection |
| `categories` | `CategoryItem[]` | BentoCategories |
| `dailyArrivals` | `DailyArrival[]` | DailySeafoodStory |
| `featuredProducts` | `FeaturedProduct[]` | FeaturedProducts + QuickViewModal |
| `comboSets` | `ComboSet[]` | ComboSetsSection |
| `featuredReviews` | `FeaturedReview[]` | SocialProofSection |
| `stats` | `HomeStats` | SocialProofSection (stats bar) |

---

## Sub-data 1 — `HeroSlide[]`

**Source**: `hero-slides.data.ts` → `HeroSection.tsx`  
**Số lượng**: 3 slides (admin quản lý)

```ts
type HeroSlide = {
  id:             string;    // "combo-bbq-dai-duong"
  sortOrder:      number;    // thứ tự hiển thị, tăng dần
  isActive:       boolean;   // admin ẩn/hiện slide

  badge: {
    text:         string;    // "SET TIỆC BBQ CUỐI TUẦN BÁN CHẠY NHẤT"
    icon:         string;    // "sparkles" — Lucide icon name
  };

  // Tiêu đề 3 dòng (render riêng để highlight màu vàng dòng giữa)
  titlePrefix:    string;    // "Cảng cá Phan Thiết"
  titleHighlight: string;    // "Gõ cửa nhà bạn"
  titleSuffix:    string;    // "trong 2 giờ!"

  description:    string;    // Đoạn mô tả ngắn ~2 dòng

  primaryCta: {
    label:        string;    // "Đặt Combo BBQ Ngay"
    href:         string;    // "/products?category=set-combo"
    icon:         string;    // "arrow-right"
  };
  secondaryCta: {
    label:        string;    // "Xem Thực Đơn"
    href:         string;    // "/products"
    icon:         string;    // "fish"
  };

  productCard: {
    imageUrl:      string;   // URL ảnh sản phẩm hero (tỉ lệ 3:4 ngang)
    imageAlt:      string;   // Alt text SEO
    comboBadge:    string;   // "COMBO TIẾT KIỆM"
    discountBadge: string;   // "-15% GIẢM SỐC"
    originalPrice: number;   // 1150000 (VND, FE tự format)
    salePrice:     number;   // 980000
    title:         string;   // "Set Hải Sản BBQ \"Đại Dương Xanh\""
    subtitle:      string;   // "Tôm hùm bơi, Mực lá Phan Thiết..."
  };
};
```

> **DB gợi ý**: Bảng `hero_banners`. Admin CRUD qua dashboard.

---

## Sub-data 2 — `CategoryItem[]`

**Source**: `home-mock.ts` → `BentoCategories.tsx`  
**Số lượng**: 6 items (layout bento: 1 main + 2 medium + 3 icon cards)

```ts
type CategoryItem = {
  id:            string;    // "bento-combo"
  name:          string;    // "SET HẢI SẢN NHẬU & BBQ"
  description:   string;    // "Đầy đủ tôm, mực, hàu, sò kèm sốt muối ớt..."
  slug:          string;    // "set-combo" — dùng để build href: /products?category={slug}
  imageUrl:      string;    // URL ảnh (dùng cho main & medium cards)
  badge:         string | null;  // "HOT COMBO" | "45+ sản phẩm" | null
  badgeType:     "hot" | "number" | "fresh" | "dry" | null;
  iconName:      string | null;  // "shell" | "flame" | "sun" — Lucide icon (chỉ icon cards)
  productCount:  number;    // Số sản phẩm trong danh mục (để hiện badge "45+")
  displayStyle:  "main" | "card" | "icon";  // layout card khác nhau
  sortOrder:     number;    // thứ tự trong bento grid
  isActive:      boolean;
};
```

**Mapping layout theo `displayStyle`:**
| displayStyle | Vị trí | Component style |
|---|---|---|
| `main` | Ô lớn (lg:col-span-2) | Ảnh full + nút CTA |
| `card` | 2 ô trung | Ảnh + title + link |
| `icon` | 3 ô dưới | Icon + title + badge + link |

> **DB gợi ý**: Bảng `categories` thêm cột `home_display_style`, `home_sort_order`, `home_is_active`.

---

## Sub-data 3 — `DailyArrival[]`

**Source**: `DailySeafoodStory.tsx` (type `StoryItem`)  
**Số lượng**: 3 items — cập nhật mỗi sáng

```ts
type DailyArrival = {
  id:            string;    // ID của daily arrival record
  productId:     string;    // FK → products.id (dùng để add-to-cart)
  arrivedAt:     string;    // ISO datetime: "2026-08-11T04:30:00+07:00"
                            // FE format thành "04:30 Sáng nay"
  badge:         string;    // "VỪA CẬP BẾN" | "BƠI BỂ 100%" | "THỊT CHẮC GẠCH BÉO"
  title:         string;    // "Cá Thu Cắt Lát Cảng Phan Thiết"
  description:   string;    // Đoạn storytelling ~3-4 dòng (giọng văn cảm xúc)
  weight:        string;    // "500g / Khay (2-3 lát)"
  origin:        string;    // "Cảng cá Phan Thiết, Bình Thuận"
  price:         number;    // 185000
  originalPrice: number;    // 220000 (FE tính % giảm tự động)
  imageUrl:      string;    // URL ảnh (tỉ lệ 1:1 hoặc 4:3)
  imageAlt:      string;    // Alt text SEO
};
```

> **DB gợi ý**: Bảng `daily_arrivals` với cột `date` (DATE). Admin pick sản phẩm mỗi sáng.  
> **Reset logic**: FE chỉ hiện items có `date = TODAY`. BE filter theo ngày hiện tại server.

---

## Sub-data 4 — `FeaturedProduct[]`

**Source**: `home-mock.ts` (type `Product`) → `FeaturedProducts.tsx` + `ProductCard.tsx` + `QuickViewModal.tsx`  
**Số lượng**: 4–8 sản phẩm, admin đánh dấu `isFeatured`

> ⚠️ **Lưu ý**: QuickViewModal yêu cầu thêm field so với ProductCard thông thường.

```ts
type FeaturedProduct = {
  // ── Dùng cho ProductCard ──────────────────────────────────
  id:            string;    // "prod-1" — dùng cho href /products/{id}
  name:          string;    // "Tôm Hùm Bông Phan Thiết (Size 1-1.2kg/con)"
  categoryLabel: string;    // "TÔM & CUA" — label UPPERCASE hiển thị trên card
  categorySlug:  string;    // "tom-cua" — dùng để filter tab FE
  badges:        string[];  // ["Bán chạy số 1", "Tươi sống"] — màu badge tự động theo nội dung
  spec:          string;    // "Giao sống tận nơi • Thùng xốp 4l"
  price:         number;    // 890000
  unit:          string;    // "1kg"
  imageUrl:      string;    // URL ảnh sản phẩm

  // ── Thêm cho QuickViewModal (hiện khi hover → "Xem nhanh") ──
  originalPrice:  number;    // 1023500 (FE tính: price * 1.15 nếu null → BE nên trả về)
  rating:         number;    // 4.9
  reviewCount:    number;    // 42
  origin:         string;    // "Cảng cá Phan Thiết, Bình Thuận"
  description:    string;    // Mô tả ngắn ~2 dòng cho modal
  weightOptions:  string[];  // ["500g / Khay", "1kg / Túi oxy", "Combo 2kg"]
                             // — các quy cách để user chọn trong modal
};
```

**Tab filter categories cần kèm theo:**

```ts
type FeaturedProductTab = {
  slug:       string;   // "tom-cua" | "muc-bach-tuoc" | "sot-tiec" | "so-oc"
  label:      string;   // "Tôm & Cua" | "Mực & Bạch tuộc" | "Sốt Tiệc"
  sortOrder:  number;
};
```

> BE có thể trả về `tabs` riêng hoặc FE tự derive từ `categorySlug` của products.  
> **Đề xuất**: trả về `tabs` riêng để dễ quản lý label tiếng Việt.

---

## Sub-data 5 — `ComboSet[]`

**Source**: `home-mock.ts` (type `ComboSet`) → `ComboSetsSection.tsx`  
**Số lượng**: 4 combos, filter theo category ở FE

```ts
type ComboSet = {
  id:          string;    // "combo-eatclean"
  tag:         string;    // "SỨC KHỎE" | "TIỆC TÙNG" | "GIA ĐÌNH" | "HỎA TỐC"
  title:       string;    // "Set Văn Phòng Eat-Clean"
  description: string;    // Mô tả ~2 dòng
  price:       number;    // 185000
  unit:        string;    // "phần" | "set 2-3 người" | "set 4-6 người"
  ctaText:     string;    // "Đặt phần ăn trưa" — label nút CTA
  href:        string;    // "/products?category=sot-tiec"
  imageUrl:    string;    // URL ảnh (tỉ lệ 1:1)
  theme:       "light" | "dark";  // màu nền card
  isBreakout:  boolean;   // true = ảnh breakout (kích thước lớn, object-contain)
  category:    "lunch" | "party" | "family";  // dùng để filter tab
  sortOrder:   number;
  isActive:    boolean;
};
```

**Tab filter:**
| `category` value | Label hiển thị |
|---|---|
| `lunch` | Ăn Trưa |
| `party` | Tiệc BBQ |
| `family` | Gia Đình |

> **DB gợi ý**: Combo là sản phẩm type `"combo"` trong bảng `products`, thêm cột `combo_category`, `combo_theme`, `combo_tag`, `is_breakout`.

---

## Sub-data 6 — `FeaturedReview[]`

**Source**: `SocialProofSection.tsx` (type `Testimonial`)  
**Số lượng**: 3 reviews do admin chọn `isFeatured = true`

```ts
type FeaturedReview = {
  id:              string;    // "rev-1"
  customerName:    string;    // "Nguyễn Thanh Hà"
  customerLocation:string;    // "Quận 2, TP. Hồ Chí Minh"
  avatarInitials:  string;    // "TH" — 2 ký tự (FE render avatar text)
  rating:          number;    // 5 (1–5)
  productName:     string;    // "Set Hải Sản BBQ \"Đại Dương Xanh\""
  comment:         string;    // Nội dung đánh giá (không giới hạn nhưng ~3-4 dòng)
  createdAt:       string;    // ISO datetime — FE format thành "Hôm qua", "3 ngày trước"
};
```

> **DB gợi ý**: Bảng `reviews` thêm cột `is_featured_home: boolean`. Admin toggle trong dashboard.

---

## Sub-data 7 — `HomeStats`

**Source**: `SocialProofSection.tsx` (stats bar section)  
**Số lượng**: Object đơn, 3 số liệu

```ts
type HomeStats = {
  totalOrdersDelivered: number;  // 12000 — đếm từ orders WHERE status = DELIVERED
  averageRating:        number;  // 4.9 — AVG(rating) FROM reviews
  totalReviews:         number;  // 10000 — COUNT(*) FROM reviews
};
```

> **Lưu ý cache**: Stats nên cache Redis TTL 30 phút. Không cần real-time.

---

## Full Response Schema (hoàn chỉnh)

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "heroSlides": [
      {
        "id": "combo-bbq-dai-duong",
        "sortOrder": 1,
        "isActive": true,
        "badge": {
          "text": "SET TIỆC BBQ CUỐI TUẦN BÁN CHẠY NHẤT",
          "icon": "sparkles"
        },
        "titlePrefix": "Cảng cá Phan Thiết",
        "titleHighlight": "Gõ cửa nhà bạn",
        "titleSuffix": "trong 2 giờ!",
        "description": "Hải sản tươi rói đánh bắt trong đêm...",
        "primaryCta": {
          "label": "Đặt Combo BBQ Ngay",
          "href": "/products?category=set-combo",
          "icon": "arrow-right"
        },
        "secondaryCta": {
          "label": "Xem Thực Đơn",
          "href": "/products",
          "icon": "fish"
        },
        "productCard": {
          "imageUrl": "https://...",
          "imageAlt": "Tôm hùm và hải sản tươi sống",
          "comboBadge": "COMBO TIẾT KIỆM",
          "discountBadge": "-15% GIẢM SỐC",
          "originalPrice": 1150000,
          "salePrice": 980000,
          "title": "Set Hải Sản BBQ \"Đại Dương Xanh\"",
          "subtitle": "Tôm hùm bơi, Mực lá Phan Thiết, Sò điệp Nhật"
        }
      }
    ],

    "categories": [
      {
        "id": "cat-1",
        "name": "SET HẢI SẢN NHẬU & BBQ",
        "description": "Đầy đủ tôm, mực, hàu, sò kèm sốt muối ớt...",
        "slug": "set-combo",
        "imageUrl": "https://...",
        "badge": "HOT COMBO",
        "badgeType": "hot",
        "iconName": null,
        "productCount": 12,
        "displayStyle": "main",
        "sortOrder": 1,
        "isActive": true
      }
    ],

    "dailyArrivals": [
      {
        "id": "arrival-1",
        "productId": "prod-abc",
        "arrivedAt": "2026-08-11T04:30:00+07:00",
        "badge": "VỪA CẬP BẾN",
        "title": "Cá Thu Cắt Lát Cảng Phan Thiết",
        "description": "Cá thu tươi nguyên con vừa kéo lưới lên tại cảng Phan Thiết...",
        "weight": "500g / Khay (2-3 lát)",
        "origin": "Cảng cá Phan Thiết, Bình Thuận",
        "price": 185000,
        "originalPrice": 220000,
        "imageUrl": "https://...",
        "imageAlt": "Cá thu tươi mới cập bến"
      }
    ],

    "featuredProducts": [
      {
        "id": "prod-1",
        "name": "Tôm Hùm Bông Phan Thiết (Size 1-1.2kg/con)",
        "categoryLabel": "TÔM & CUA",
        "categorySlug": "tom-cua",
        "badges": ["Bán chạy số 1", "Tươi sống"],
        "spec": "Giao sống tận nơi • Thùng xốp 4l",
        "price": 890000,
        "originalPrice": 1023500,
        "unit": "1kg",
        "imageUrl": "https://...",
        "rating": 4.9,
        "reviewCount": 42,
        "origin": "Cảng cá Phan Thiết, Bình Thuận",
        "description": "Tôm hùm bông bơi khoẻ đóng thùng oxy...",
        "weightOptions": ["500g / Khay", "1kg / Túi oxy", "Combo 2kg"]
      }
    ],

    "featuredProductTabs": [
      { "slug": "tom-cua",       "label": "Tôm & Cua",       "sortOrder": 1 },
      { "slug": "muc-bach-tuoc", "label": "Mực & Bạch tuộc", "sortOrder": 2 },
      { "slug": "sot-tiec",      "label": "Sốt Tiệc",        "sortOrder": 3 },
      { "slug": "so-oc",         "label": "Sò & Ốc",         "sortOrder": 4 }
    ],

    "comboSets": [
      {
        "id": "combo-eatclean",
        "tag": "SỨC KHỎE",
        "title": "Set Văn Phòng Eat-Clean",
        "description": "Hải sản hấp nhẹ kèm salad và sốt bơ chanh...",
        "price": 185000,
        "unit": "phần",
        "ctaText": "Đặt phần ăn trưa",
        "href": "/products?category=sot-tiec",
        "imageUrl": "https://...",
        "theme": "light",
        "isBreakout": false,
        "category": "lunch",
        "sortOrder": 1,
        "isActive": true
      }
    ],

    "featuredReviews": [
      {
        "id": "rev-1",
        "customerName": "Nguyễn Thanh Hà",
        "customerLocation": "Quận 2, TP. Hồ Chí Minh",
        "avatarInitials": "TH",
        "rating": 5,
        "productName": "Set Hải Sản BBQ \"Đại Dương Xanh\"",
        "comment": "Tôm hùm giao tới vẫn còn giãy đành đạch trong thùng oxy!...",
        "createdAt": "2026-08-10T10:00:00+07:00"
      }
    ],

    "stats": {
      "totalOrdersDelivered": 12000,
      "averageRating": 4.9,
      "totalReviews": 10000
    }
  }
}
```

---

## Tổng kết — Số lượng sub-data & field count

| Sub-data | Count items | Số field/item | Ghi chú |
|---|---|---|---|
| `heroSlides` | 3 slides | 14 fields (incl. nested) | Bảng `hero_banners` |
| `categories` | 6 items | 11 fields | Bảng `categories` + home config |
| `dailyArrivals` | 3 items | 11 fields | Bảng `daily_arrivals` |
| `featuredProducts` | 4–8 items | 14 fields | Bảng `products` + `isFeatured` flag |
| `featuredProductTabs` | 4 tabs | 3 fields | Derive từ categories |
| `comboSets` | 4 items | 13 fields | Bảng `products` type combo |
| `featuredReviews` | 3 items | 8 fields | Bảng `reviews` + `isFeatured` flag |
| `stats` | 1 object | 3 fields | Aggregate query, cache 30 phút |

**Tổng**: **8 sub-data**, **77 fields** (bao gồm nested objects).

---

## Cache Strategy gợi ý

| Sub-data | TTL | Lý do |
|---|---|---|
| `heroSlides` | 10 phút | Ít thay đổi, admin cập nhật thủ công |
| `categories` | 10 phút | Ít thay đổi |
| `dailyArrivals` | Reset 6h sáng | Cập nhật mỗi sáng theo ngày |
| `featuredProducts` | 5 phút | Stock có thể thay đổi |
| `featuredProductTabs` | 10 phút | Ít thay đổi |
| `comboSets` | 10 phút | Ít thay đổi |
| `featuredReviews` | 30 phút | Admin curate thủ công |
| `stats` | 30 phút | Aggregate, không cần real-time |

**Header gợi ý:** `Cache-Control: s-maxage=300, stale-while-revalidate=60`
