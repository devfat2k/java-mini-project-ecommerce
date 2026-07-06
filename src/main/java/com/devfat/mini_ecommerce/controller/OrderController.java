package com.devfat.mini_ecommerce.controller;

import com.devfat.mini_ecommerce.dto.request.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateOrderStatusRequestDto;
import com.devfat.mini_ecommerce.dto.response.OrderResponseDto;
import com.devfat.mini_ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Quản lý dơn hàng")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getOrderResponse(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findByUserIdWithDetails(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable("id") Long orderId
    ) {
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
           @Valid @RequestBody() CreateOrderRequestDto createOrderRequest
    ) {
        OrderResponseDto orderResponse = orderService.create(createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequestDto updateOrderStatusRequestDto
            ) {
        OrderResponseDto orderResponse = orderService.changeStatus(id, updateOrderStatusRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }
}
