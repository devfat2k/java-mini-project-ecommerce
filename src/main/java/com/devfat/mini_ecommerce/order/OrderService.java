package com.devfat.mini_ecommerce.order;

import com.devfat.mini_ecommerce.order.dto.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.order.dto.OrderResponseDto;
import com.devfat.mini_ecommerce.order.dto.UpdateOrderStatusRequestDto;
import com.devfat.mini_ecommerce.shared.base.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

public interface OrderService {
    PageResponse<OrderResponseDto> getOrderByUserIdAndStatusWithDetails(Long userId, OrderStatus status, Pageable pageable);
    OrderResponseDto findById(Long id, Long userIdInToken, String role) throws AccessDeniedException;
    OrderResponseDto create(Long userId, CreateOrderRequestDto createOrderRequestDto) throws JsonProcessingException;
    OrderResponseDto changeStatus(Long id, UpdateOrderStatusRequestDto updateOrderStatusRequestDto);
    PageResponse<OrderResponseDto> getAllOrder( Pageable pageable);
    PageResponse<OrderResponseDto> getMyOrder(Long userId, Pageable pageable);
    void cancelOrder(Long orderId, Long userId,  UpdateOrderStatusRequestDto updateOrderStatusRequestDto) throws AccessDeniedException;
}
