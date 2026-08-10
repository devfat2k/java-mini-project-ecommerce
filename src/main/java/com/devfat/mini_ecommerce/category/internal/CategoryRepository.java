package com.devfat.mini_ecommerce.category.internal;

import com.devfat.mini_ecommerce.category.dto.CategoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Page<CategoryEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);


    @Query("""
        SELECT new com.devfat.mini_ecommerce.category.dto.CategoryResponseDto(
            c.id, c.name, COUNT(p.id))
        FROM CategoryEntity c
        LEFT JOIN c.products p ON p.isActive = true
        GROUP BY c.id, c.name
        """)
    List<CategoryResponseDto> countActiveCategories();
}
