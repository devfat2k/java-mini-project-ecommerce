package com.devfat.mini_ecommerce.payment;

import com.devfat.mini_ecommerce.payment.dto.CreatePaymentResponseDto;










import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {
    CreatePaymentResponseDto createPayment(Long userId, Long orderId, HttpServletRequest request);
    void handleVnPayIpn(Map<String, String> params);
}
