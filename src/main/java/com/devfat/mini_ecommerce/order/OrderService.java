package com.devfat.mini_ecommerce.order;

import com.devfat.mini_ecommerce.order.dto.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.order.dto.OrderResponseDto;
import com.devfat.mini_ecommerce.order.dto.UpdateOrderStatusRequestDto;


import com.devfat.mini_ecommerce.shared.base.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface OrderService {
    List<OrderResponseDto> findByUserIdWithDetails(Long userIdPath, Long userIdInToken, String role) throws AccessDeniedException;
    OrderResponseDto findById(Long id, Long userIdInToken, String role) throws AccessDeniedException;
    OrderResponseDto create(Long userId, CreateOrderRequestDto createOrderRequestDto);
    OrderResponseDto changeStatus(Long id, UpdateOrderStatusRequestDto updateOrderStatusRequestDto);
    PageResponse<OrderResponseDto> getAllByUserId(Long userId, Pageable pageable);
}
