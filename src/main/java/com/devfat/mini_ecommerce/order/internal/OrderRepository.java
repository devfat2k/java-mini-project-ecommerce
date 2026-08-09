package com.devfat.mini_ecommerce.order.internal;

import com.devfat.mini_ecommerce.order.OrderStatus;


import com.devfat.mini_ecommerce.order.dto.OrderResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    //tìm orders theo userId
    Page<OrderEntity> findAllByUserId(Long userId, Pageable pageable);

    OrderEntity findByUserId(Long userId);
    //tìm orders theo status
    List<OrderEntity> findAllByStatus(OrderStatus status);

    //Tìm orders theo userId VÀ status
    //@Query JPQL: lấy orders kèm user và items (JOIN FETCH cả 2), lọc theo userId
    @Query("SELECT o from OrderEntity o JOIN FETCH o.user where o.user.id = :userId AND o.status = :status")
    List<OrderEntity> findAllByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);


    @Query("SELECT DISTINCT o FROM OrderEntity o " +
            "JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.items " +
            "WHERE o.user.id = :userId")
    List<OrderEntity> findByUserIdWithDetails(@Param("userId") Long userId);

}
