package com.devfat.mini_ecommerce.repository;

import com.devfat.mini_ecommerce.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
