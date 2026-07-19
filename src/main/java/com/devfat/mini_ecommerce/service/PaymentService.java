package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.response.CreatePaymentResponseDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {
    CreatePaymentResponseDto createPayment(Long userId, Long orderId, HttpServletRequest request);
    void handleVnpayIpn(Map<String, String> params);
}
