package com.devfat.mini_ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderTest {

    @Test
    void testBCryptPasswordHashing() {
        // 1. Tạo trực tiếp instance của BCryptPasswordEncoder (chạy cực nhanh, độc lập hoàn toàn)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "123456";

        // 2. Gọi hàm encode() để thực hiện băm 1 chiều mật khẩu gốc
        String hashVuaTao = encoder.encode(rawPassword);

        // Tận mắt quan sát chuỗi băm được in ra màn hình console
        System.out.println("====== KẾT QUẢ THỰC TẾ TRÊN CONSOLE ======");
        System.out.println("Mật khẩu gốc ban đầu     : " + rawPassword);
        System.out.println("Chuỗi băm BCrypt tạo ra   : " + hashVuaTao);
        System.out.println("==========================================");

        // 3. Kiểm tra so sánh tính chính xác của cơ chế matches()

        // Trường hợp 1: Nhập đúng mật khẩu "123456" -> matches() phải trả về true
        assertTrue(encoder.matches("123456", hashVuaTao),
                "LỖI: matches() phải trả về true khi nhập đúng mật khẩu gốc!");

        // Trường hợp 2: Nhập sai mật khẩu "wrongpass" -> matches() phải trả về false
        assertFalse(encoder.matches("wrongpass", hashVuaTao),
                "LỖI: matches() phải trả về false khi nhập sai mật khẩu!");
    }
}