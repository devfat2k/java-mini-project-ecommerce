package com.devfat.mini_ecommerce.storage.internal;

import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.storage.StorageService;










import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Tag(name = "UPLOAD", description = "Upload image to cloud")
public class TestUploadController {

    private final StorageService storageService;

    @PreAuthorize("hasRole('ADMIN')")
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
