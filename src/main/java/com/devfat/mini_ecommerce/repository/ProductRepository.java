package com.devfat.mini_ecommerce.repository;
import org.springframework.data.domain.Pageable;
import com.devfat.mini_ecommerce.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Repository
@EnableJpaRepositories
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    //    Lấy tất cả sản phẩm còn hàng
    List<ProductEntity> findByStockGreaterThan(int stock);

    // Lấy sản phẩm theo category và còn hàng
    List<ProductEntity> findByCategoryIdAndStockGreaterThan(Long categoryId, int stock);

    // Lấy sản phẩm kèm category, lọc theo giá tối thiểu - @Query JPQL (JOIN 2 bảng, lấy category luôn, không N+1):
    @Query("SELECT p FROM ProductEntity p JOIN FETCH p.category WHERE p.stock > 0")
    List<ProductEntity> findAvailableWithCategory();

    // Lấy sản phẩm kèm category, lọc theo giá tối thiểu
    @Query("SELECT p from ProductEntity p JOIN FETCH p.category WHERE p.price >= :minPrice")
    List<ProductEntity> findByMinPriceWithCategory(@Param("minPrice") BigDecimal minPrice);

    // Pageable (phân trang, dùng cho API danh sách) - "Lấy sản phẩm còn hàng, có phân trang"
    Page<ProductEntity> findByStockGreaterThan(int stock, Pageable pageable);

    // Pageable - "Lấy sản phẩm có phân trang, có search theo tên sản phầm"
    Page<ProductEntity> findByNameContainsIgnoreCase(String search, Pageable pageable);

    interface TopProductView {
        String getName();
        BigDecimal getPrice();
        Integer getMostBuy();
    }
    @Query("SELECT " +
            " p.name as name," +
            " p.price as price," +
            " COALESCE(SUM(oi.quantity), 0) as mostBuy" +
            " FROM ProductEntity p " +
            " LEFT JOIN p.orderItems oi" +
            " GROUP BY p.id, p.name, p.price " +
            " ORDER BY  COALESCE(SUM(oi.quantity), 0) DESC")
    List<TopProductView> getTopViewProduct(Pageable pageable);

    //Tổng doanh thu theo category
    interface CategoryRevenueView {
        String getName();
        BigDecimal getRevenue();
    }
    @Query("SELECT c.name as name, COALESCE(SUM(oi.unitPrice * oi.quantity), 0) as revenue" +
            " FROM CategoryEntity c" +
            " LEFT JOIN c.products p" +
            " LEFT JOIN p.orderItems oi" +
            " GROUP BY  c.id, c.name" +
            " ORDER BY COALESCE(SUM(oi.unitPrice * oi.quantity), 0) DESC")
    List<CategoryRevenueView> getCategoryRevenue(Pageable pageable);

    //MonthlyRevenueView
    interface MonthlyRevenueView {
        LocalDateTime getMonth();
        BigDecimal getRevenue();
    }
    @Query("  SELECT DATE_TRUNC('month', o.createdAt) as month, COALESCE(SUM(oi.unitPrice * oi.quantity) , 0) as revenue" +
            " FROM ProductEntity o LEFT JOIN o.orderItems oi" +
            " GROUP BY DATE_TRUNC('month', o.createdAt)" +
            " ORDER BY DATE_TRUNC('month', o.createdAt)")
    List<MonthlyRevenueView> getMonthlyRevenue();
}
