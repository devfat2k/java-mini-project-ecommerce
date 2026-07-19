package com.devfat.mini_ecommerce.controller;

import com.devfat.mini_ecommerce.common.ApiResponse;
import com.devfat.mini_ecommerce.dto.request.CreateOrderRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateOrderStatusRequestDto;
import com.devfat.mini_ecommerce.dto.response.OrderResponseDto;
import com.devfat.mini_ecommerce.security.UserPrincipal;
import com.devfat.mini_ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Quản lý dơn hàng")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrderResponse(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    )  {
        String role = userPrincipal.getRole();
        Long userIdInToken = userPrincipal.getUserId();
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        orderService.findByUserIdWithDetails(userId, userIdInToken, role),
                        "Get Order By User ID Successfully!"
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(
            @PathVariable("id") Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userIdInToken = userPrincipal.getUserId();
        String role = userPrincipal.getRole();

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        orderService.findById(orderId, userIdInToken, role),
                        "Get Order Successfully!"
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody() CreateOrderRequestDto createOrderRequest
    ) {
        Long currentUserId = userPrincipal.getUserId();
        OrderResponseDto orderResponse = orderService.create(currentUserId, createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(orderResponse, "Create Order Successfully!")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequestDto updateOrderStatusRequestDto
            ) {
        OrderResponseDto orderResponse = orderService.changeStatus(id, updateOrderStatusRequestDto);
        return ResponseEntity.ok().body(
                ApiResponse.success(orderResponse, "Update Order Status Successfully!")
        );
    }
}
