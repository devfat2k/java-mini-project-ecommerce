package com.devfat.mini_ecommerce.controller;

import com.devfat.mini_ecommerce.common.ApiResponse;
import com.devfat.mini_ecommerce.service.StorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Tag(name = "TEST", description = "Test any api")
public class TestUploadController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> testUpload(
            @RequestParam("file") MultipartFile file
    ) {
        String url = storageService.uploadFile(file, "test", true);
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        url,
                        "Upload image success!"
                )
        );
    }
}