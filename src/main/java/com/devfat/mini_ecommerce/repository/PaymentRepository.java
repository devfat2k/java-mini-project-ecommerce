package com.devfat.mini_ecommerce.repository;

import com.devfat.mini_ecommerce.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(Long orderId);
    boolean existsByOrderIdAndPaymentStatus(Long orderId, PaymentEntity.PaymentStatus  paymentStatus);
}
