package com.devfat.mini_ecommerce;

import com.devfat.mini_ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestConnection {

    @Autowired
    private ProductRepository productRepo;

    @Test
    void testConnection() {
        long count = productRepo.count();
        System.out.println(">>> Tổng sản phẩm trong DB: " + count);
    }
}