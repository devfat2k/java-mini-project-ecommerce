package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.request.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.dto.response.OrderResponseDto;
import com.devfat.mini_ecommerce.entity.OrderEntity;

import java.util.List;

public interface OrderService {
    List<OrderResponseDto> findByUserIdWithDetails(Long userId);
    OrderResponseDto findById(Long id);
    OrderResponseDto create(CreateOrderRequestDto createOrderRequestDto);
}
