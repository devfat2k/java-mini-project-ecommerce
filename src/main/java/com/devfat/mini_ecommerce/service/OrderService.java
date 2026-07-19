package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.request.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateOrderStatusRequestDto;
import com.devfat.mini_ecommerce.dto.response.OrderResponseDto;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface OrderService {
    List<OrderResponseDto> findByUserIdWithDetails(Long userIdPath, Long userIdInToken, String role) throws AccessDeniedException;
    OrderResponseDto findById(Long id, Long userIdInToken, String role) throws AccessDeniedException;
    OrderResponseDto create(Long userId, CreateOrderRequestDto createOrderRequestDto);
    OrderResponseDto changeStatus(Long id, UpdateOrderStatusRequestDto updateOrderStatusRequestDto);
}
