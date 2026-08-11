package com.devfat.mini_ecommerce.payment.internal;

import com.devfat.mini_ecommerce.payment.PaymentStatus;










import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(Long orderId);
    boolean existsByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);
    List<PaymentEntity> findAllByPaymentStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime createdAt);
}
