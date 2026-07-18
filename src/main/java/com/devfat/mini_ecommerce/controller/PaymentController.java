package com.devfat.mini_ecommerce.controller;


import com.devfat.mini_ecommerce.common.ApiResponse;
import com.devfat.mini_ecommerce.dto.response.CreatePaymentResponseDto;
import com.devfat.mini_ecommerce.repository.PaymentRepository;
import com.devfat.mini_ecommerce.security.UserPrincipal;
import com.devfat.mini_ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/payments")
@RestController
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment manager")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/create")
    public ResponseEntity<ApiResponse<CreatePaymentResponseDto>> createPayment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long orderId,
            HttpServletRequest request
            ) {
        Long userId = userPrincipal.getUserId();
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        paymentService.createPayment(userId, orderId, request),
                        "Create Payment Successfully!"
                )
        );
    }
}
