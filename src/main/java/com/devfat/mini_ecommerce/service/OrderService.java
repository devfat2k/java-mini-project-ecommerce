package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.request.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateOrderStatusRequestDto;
import com.devfat.mini_ecommerce.dto.response.OrderResponseDto;

import java.util.List;

public interface OrderService {
    List<OrderResponseDto> findByUserIdWithDetails(Long userId);
    OrderResponseDto findById(Long id);
    OrderResponseDto create(CreateOrderRequestDto createOrderRequestDto);
    OrderResponseDto changeStatus(Long id, UpdateOrderStatusRequestDto updateOrderStatusRequestDto);
}
