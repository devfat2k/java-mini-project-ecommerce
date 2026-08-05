package com.devfat.mini_ecommerce.storage;











import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String folder, boolean resizeImage);
}
