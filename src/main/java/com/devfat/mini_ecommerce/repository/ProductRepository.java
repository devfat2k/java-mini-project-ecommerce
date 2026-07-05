package com.devfat.mini_ecommerce.repository;
import org.springframework.data.domain.Pageable;
import com.devfat.mini_ecommerce.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


@Repository
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

}
