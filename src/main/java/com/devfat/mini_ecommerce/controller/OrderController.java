package com.devfat.mini_ecommerce.controller;

import com.devfat.mini_ecommerce.dto.request.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.dto.response.OrderResponseDto;
import com.devfat.mini_ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getOrderResponse(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findByUserIdWithDetails(userId));
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable("id") Long orderId
    ) {
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    @PostMapping()
    @Transactional
    public ResponseEntity<OrderResponseDto> createOrder(
           @Valid @RequestBody() CreateOrderRequestDto createOrderRequestDto
    ) {
        orderService.create(createOrderRequestDto);
        return ResponseEntity.ok().build();
    }
}
