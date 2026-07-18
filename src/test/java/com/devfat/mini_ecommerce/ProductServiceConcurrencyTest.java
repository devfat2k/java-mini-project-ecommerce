package com.devfat.mini_ecommerce;

import com.devfat.mini_ecommerce.entity.ProductEntity;
import com.devfat.mini_ecommerce.repository.ProductRepository;
import com.devfat.mini_ecommerce.service.ProductService;
import lombok.extern.slf4j.Slf4j; // 1. Thêm import này
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j // 2. Thêm annotation để dùng biến log
@SpringBootTest
public class ProductServiceConcurrencyTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Long testItemId;

    @BeforeEach
    void setUp() {
        ProductEntity product = ProductEntity.builder()
                .name("[TEST] Tôm Hùm Race Condition")
                .price(BigDecimal.valueOf(850000.00))
                .stock(1)
                .isActive(true)
                .build();
        ProductEntity savedProduct = productRepository.save(product);
        testItemId = savedProduct.getId();

        log.info("======================================================");
        log.info("[ARRANGE] Đã dọn sẵn mâm, Tôm Hùm ID: {}, Stock: 1", testItemId);
        log.info("======================================================");
    }

    @AfterEach
    void tearDown() {
        if (testItemId != null) {
            productRepository.deleteById(testItemId);
            log.info("[TEARDOWN] Đã dọn sạch hiện trường (Xóa Tôm Hùm ID: {})", testItemId);
            log.info("======================================================\n");
        }
    }

    @Test
    public void testConcurrentDecreaseStock_WhenTwoThreadsBuyOneItem_ThenOneFails() throws InterruptedException {
        // Biến đếm để phân biệt Khách A và Khách B cho dễ nhìn
        AtomicInteger customerCounter = new AtomicInteger(1);

        log.info("[ACT] Bắt đầu mở cửa cho giật cô hồn...");

        var result = ConcurrencyTestHelper.runConcurrently(2, () -> {
            // Định danh người dùng
            String buyer = (customerCounter.getAndIncrement() == 1) ? "Khách A" : "Khách B";

            log.info(" 🏃‍♂️ [{}] Đã lao vào và đang bấm nút MUA...", buyer);

            try {
                // Gọi Service thực thi
                productService.decreaseStock(testItemId, 1);

                // Nếu code chạy lọt xuống đây nghĩa là không bị lỗi
                log.info(" ✅ [{}] MUA THÀNH CÔNG! Đã giật được Tôm Hùm.", buyer);

            } catch (ObjectOptimisticLockingFailureException e) {
                // Bắt trúng lỗi Khóa Lạc Quan
                log.warn(" ❌ [{}] BỊ ĐÁ VĂNG! Lỗi Hết hàng (Version lệch).", buyer);
                throw e; // BẮT BUỘC NÉM LẠI để Helper gom exception vào danh sách

            } catch (Exception e) {
                log.error(" ⚠️ [{}] Lỗi lạ: {}", buyer, e.getMessage());
                throw e;
            }
        });

        log.info("[ASSERT] Bắt đầu kiểm đếm kết quả...");

        assertEquals(1, result.successCount(), "Phải có chính xác 1 luồng bị lỗi văng ra");

        Exception thrownException = result.exceptions().getFirst();
        assertInstanceOf(ObjectOptimisticLockingFailureException.class, thrownException,
                "Lỗi văng ra phải là Optimistic Locking của Spring");

        ProductEntity finalProduct = productRepository.findById(testItemId).orElseThrow();
        assertEquals(0, finalProduct.getStock(), "Kho hàng cuối cùng phải về 0");

        log.info(" 🎯 [ASSERT] TEST PASSED NGON LÀNH! Kho còn đúng: {} con", finalProduct.getStock());
    }
}