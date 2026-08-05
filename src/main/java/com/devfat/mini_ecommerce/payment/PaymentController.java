package com.devfat.mini_ecommerce.payment;

import com.devfat.mini_ecommerce.payment.dto.CreatePaymentResponseDto;
import com.devfat.mini_ecommerce.payment.internal.PaymentEntity;
import com.devfat.mini_ecommerce.payment.internal.PaymentRepository;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.exception.ResourceNotFoundException;
import com.devfat.mini_ecommerce.shared.security.UserPrincipal;











import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/v1/payments")
@RestController
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment manager")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;


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

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(
            @RequestParam Map<String, String> allParams
            ) {
        // Bước 1: lấy vnp_TxnRef từ URL — đây là paymentId mình đã tự đặt lúc tạo Payment
        Long paymentId = Long.parseLong(allParams.get("vnp_TxnRef"));

        // Bước 2: dùng paymentId đó, query lại bảng payments trong DB
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Not Found"));

        // Bước 3: lấy payment.getStatus() — đây là giá trị đang NẰM TRONG DATABASE
        // KHÔNG dùng allParams.get("vnp_ResponseCode") ở bước này
        return ResponseEntity.ok(Map.of(
                "status", payment.getPaymentStatus(),
                "message", "Kiểm tra trạng thái đơn hàng"
        ));
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> allParams) {
        try {
            paymentService.handleVnPayIpn(allParams);
            // VNPay yêu cầu response đúng định dạng này để biết webhook đã nhận thành công
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("RspCode", "99", "Message", "Unknown error"));
        }
    }
}
