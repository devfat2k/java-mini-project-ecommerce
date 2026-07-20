package com.devfat.mini_ecommerce.util;

import com.devfat.mini_ecommerce.exception.BadRequestException;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileValidationUtil {

    @Value("${app.upload.max-image-size}")
    private DataSize maxImageSize;   // bỏ static — giờ Spring bơm được giá trị

    public static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    public static final byte[] PNG_SIGNATURE  = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    // 2 hằng số này quay lại static final: không phụ thuộc config,
    // giữ static cho đúng convention "constant" trong Java
// GIF có 2 phiên bản phổ biến
    public static final byte[] GIF87A_SIGNATURE = {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x37, (byte) 0x61};
    public static final byte[] GIF89A_SIGNATURE = {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61};

    public boolean matchesSignature(byte[] fileBytes, byte[] signature) {
        if (fileBytes.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (fileBytes[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean isWebp(byte[] fileBytes) {
        if (fileBytes.length < 12) {
            return false;
        }
        // Kiểm tra chữ "RIFF"
        boolean hasRiff = fileBytes[0] == 0x52 && fileBytes[1] == 0x49 && fileBytes[2] == 0x46 && fileBytes[3] == 0x46;
        // Kiểm tra chữ "WEBP"
        boolean hasWebp = fileBytes[8] == 0x57 && fileBytes[9] == 0x45 && fileBytes[10] == 0x42 && fileBytes[11] == 0x50;

        return hasRiff && hasWebp;
    }

    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty!");
        }

        if (file.getSize() > maxImageSize.toBytes()) {
            throw new BadRequestException("File is too large!");
        }

        byte[] fileBytes = getFileBytes(file);
        boolean isValidJpeg = matchesSignature(fileBytes, JPEG_SIGNATURE);
        boolean isValidPng = matchesSignature(fileBytes, PNG_SIGNATURE);
        boolean isValidGif = matchesSignature(fileBytes, GIF87A_SIGNATURE) || matchesSignature(fileBytes, GIF89A_SIGNATURE);
        boolean isValidWebp = isWebp(fileBytes);
        if (!isValidJpeg && !isValidPng && !isValidGif && !isValidWebp) {
            throw new BadRequestException("Format is invalid! Only JPEG, PNG, GIF, and WebP are allowed.");
        }
    }

    private static byte @NonNull [] getFileBytes(MultipartFile file) {
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            // Giữ nguyên RuntimeException (không đổi sang BadRequestException) —
            // vì đây là lỗi đọc file tạm hỏng ở tầng hạ tầng, không phải lỗi
            // do client nhập sai. RuntimeException → rơi vào catch-all Exception
            // → 500, đúng semantic đã thống nhất ở bước trước.
            throw new RuntimeException("Not read format file!", e);
        }
        return fileBytes;
    }
}