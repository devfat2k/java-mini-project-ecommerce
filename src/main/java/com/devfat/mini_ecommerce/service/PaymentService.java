package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.response.CreatePaymentResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    CreatePaymentResponseDto createPayment(Long userId, Long orderId, HttpServletRequest request);
}
