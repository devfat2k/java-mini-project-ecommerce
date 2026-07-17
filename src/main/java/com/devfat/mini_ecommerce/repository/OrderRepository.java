package com.devfat.mini_ecommerce.repository;

import com.devfat.mini_ecommerce.dto.response.OrderResponseDto;
import com.devfat.mini_ecommerce.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    //tìm orders theo userId
    List<OrderEntity> findAllByUserId(Long userId);
    //tìm orders theo status
    List<OrderEntity> findAllByStatus(OrderEntity.OrderStatus status);

    //Tìm orders theo userId VÀ status
    //@Query JPQL: lấy orders kèm user và items (JOIN FETCH cả 2), lọc theo userId
    @Query("SELECT o from OrderEntity o JOIN FETCH o.user where o.user.id = :userId AND o.status = :status")
    List<OrderEntity> findAllByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderEntity.OrderStatus status);


    @Query("SELECT DISTINCT o FROM OrderEntity o " +
            "JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.items " +
            "WHERE o.user.id = :userId")
    List<OrderEntity> findByUserIdWithDetails(@Param("userId") Long userId);

}
