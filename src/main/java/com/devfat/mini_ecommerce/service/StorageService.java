package com.devfat.mini_ecommerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String folder, boolean resizeImage);
}
