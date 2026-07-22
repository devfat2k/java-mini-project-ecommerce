package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.config.VNPayConfig;
import com.devfat.mini_ecommerce.config.VNPayUtil;
import com.devfat.mini_ecommerce.dto.response.CreatePaymentResponseDto;
import com.devfat.mini_ecommerce.entity.OrderEntity;
import com.devfat.mini_ecommerce.entity.PaymentEntity;
import com.devfat.mini_ecommerce.exception.BadRequestException;
import com.devfat.mini_ecommerce.exception.ResourceNotFoundException;
import com.devfat.mini_ecommerce.repository.OrderRepository;
import com.devfat.mini_ecommerce.repository.PaymentRepository;
import com.devfat.mini_ecommerce.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private final VNPayConfig vnPayConfig;


    @Scheduled(fixedRate = 120000)
    @Transactional
    public void expiredPayment() {
        // Bước A: tính mốc thời gian "15 phút trước tính từ bây giờ"
        LocalDateTime expireThreshold = LocalDateTime.now().minusMinutes(15);

        // Bước B: tìm TẤT CẢ Payment còn PENDING mà được tạo TRƯỚC mốc đó
        // (nghĩa là: đã tạo hơn 15 phút rồi mà vẫn chưa ai thanh toán)
        List<PaymentEntity> expiredPayments = paymentRepository
                .findAllByPaymentStatusAndCreatedAtBefore(
                        PaymentEntity.PaymentStatus.PENDING, expireThreshold);

        for (PaymentEntity payment : expiredPayments) {
            payment.setPaymentStatus(PaymentEntity.PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            OrderEntity order = payment.getOrder();
            order.setStatus(OrderEntity.OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
    }


    @Override
    @Transactional
    public CreatePaymentResponseDto createPayment(Long userId, Long orderId, HttpServletRequest request) {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Order Not Found!"));

        if (!order.getUser().getId().equals(userId)) throw new BadRequestException("Order Not Found!");

        if (!(order.getStatus().equals(OrderEntity.OrderStatus.PENDING)))
            throw new BadRequestException("Order Not Pending!");

        if (paymentRepository.existsByOrderIdAndPaymentStatus(order.getId(), PaymentEntity.PaymentStatus.SUCCESS))
            throw new BadRequestException("Payment already success, cannot create new payment");

        PaymentEntity payment = new PaymentEntity();
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentStatus(PaymentEntity.PaymentStatus.PENDING);
        payment.setPaymentProvider(PaymentEntity.PaymentProvider.VNPAY);
        payment.setPaymentMethod(PaymentEntity.PaymentMethod.WALLET);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);

        // ─── 3. Build Map tham số — value lấy từ vnPayConfig (biến), tên field hardcode (chuỗi) ───
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");                          // hardcode, VNPay quy định cố định
        vnpParams.put("vnp_Command", "pay");                            // hardcode
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());         // lấy từ .env qua VNPayConfig

        long amount = payment.getAmount().multiply(new BigDecimal("100")).longValue();
        vnpParams.put("vnp_Amount", String.valueOf(amount));

        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", payment.getId().toString());        // dùng paymentId làm mã giao dịch, đảm bảo unique
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());     // lấy từ .env

        // Các dòng dưới đây gọi VNPayUtil (HÀM XỬ LÝ), KHÔNG PHẢI vnPayConfig
        vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);   // hạn thanh toán 15 phút — hạt giống cho P8 sau này
        vnpParams.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        // ─── 4. Build query + ký chữ ký — gọi VNPayUtil, truyền secretKey lấy từ vnPayConfig ───
        Map<String, String> result = VNPayUtil.buildQueryAndHash(vnpParams, vnPayConfig.getSecretKey());

        // ─── 5. Nối URL cuối cùng — payUrl lấy từ vnPayConfig, phần còn lại từ VNPayUtil ───
        String paymentUrl = vnPayConfig.getPayUrl() + "?" + result.get("queryUrl")
                + "&vnp_SecureHash=" + result.get("secureHash");

        return new CreatePaymentResponseDto(paymentUrl);
    }

    @Override
    @Transactional
    public void handleVnPayIpn(Map<String, String> params) {
        // Bước 1: Verify chữ ký
        boolean isValidSignature = VNPayUtil.verifySignature(params, vnPayConfig.getSecretKey());
        if (!isValidSignature) {
            throw new BadRequestException("Invalid signature - possible fraud attempt");
        }

        // Bước 2: Chữ ký hợp lệ rồi mới tin tham số, lấy paymentId (đã lưu ở vnp_TxnRef từ P3)
        Long paymentId = Long.parseLong(params.get("vnp_TxnRef"));
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Not Found"));

        // Bước 3: Chống xử lý trùng — nếu ĐÃ SUCCESS rồi thì dừng lại, không làm gì thêm
        if (payment.getPaymentStatus().equals(PaymentEntity.PaymentStatus.SUCCESS)) {
            return; // webhook gọi lại lần 2, coi như đã xử lý xong, không báo lỗi
        }

        // Bước 4: Đọc vnp_ResponseCode để biết giao dịch thành công hay thất bại
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            payment.setPaymentStatus(PaymentEntity.PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            payment.setProviderTransactionId(params.get("vnp_TransactionNo"));
            paymentRepository.save(payment);

            // Đây là nơi DUY NHẤT trong toàn bộ hệ thống Order được chuyển sang CONFIRMED
            OrderEntity order = payment.getOrder();
            order.setStatus(OrderEntity.OrderStatus.CONFIRMED);
            orderRepository.save(order);

        } else {
            // Thanh toán thất bại
            payment.setPaymentStatus(PaymentEntity.PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }
}

