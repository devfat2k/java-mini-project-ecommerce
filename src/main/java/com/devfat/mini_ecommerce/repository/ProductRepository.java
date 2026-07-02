package com.devfat.mini_ecommerce.repository;

import com.devfat.mini_ecommerce.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
