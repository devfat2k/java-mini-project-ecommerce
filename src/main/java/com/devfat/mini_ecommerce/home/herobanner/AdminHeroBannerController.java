package com.devfat.mini_ecommerce.home.herobanner;

import com.devfat.mini_ecommerce.home.herobanner.dto.CreateHeroBannerRequestDto;
import com.devfat.mini_ecommerce.home.herobanner.dto.HeroBannerResponseDto;
import com.devfat.mini_ecommerce.home.herobanner.dto.UpdateHeroBannerRequestDto;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/hero-banners")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Hero Banner", description = "Admin Hero Banner Management API")
public class AdminHeroBannerController {

    private final HeroBannerService heroBannerService;

    @Operation(summary = "Get all hero banners", description = "Admin API — Retrieve all hero banner slides.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<HeroBannerResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                heroBannerService.getAllBanners(),
                "Get hero banners successfully"
        ));
    }

    @Operation(summary = "Create hero banner", description = "Admin API — Create a new hero banner slide.")
    @PostMapping
    public ResponseEntity<ApiResponse<HeroBannerResponseDto>> create(@RequestBody CreateHeroBannerRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(heroBannerService.create(request), "Create hero banner successfully"));
    }

    @Operation(summary = "Update hero banner", description = "Admin API — Update an existing hero banner slide.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<HeroBannerResponseDto>> update(@PathVariable Long id, @RequestBody UpdateHeroBannerRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(heroBannerService.update(id, request), "Update hero banner successfully"));
    }

    @Operation(summary = "Delete hero banner", description = "Admin API — Delete a hero banner slide by ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        heroBannerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Delete hero banner successfully"));
    }

    @Operation(summary = "Toggle Banner Active", description = "Admin API - Toggle Admin Active Home")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Boolean>> toggleBannerActive(@PathVariable Long id) {
        heroBannerService.toggleBannerActive(id);
        return ResponseEntity.ok(ApiResponse.success(true, "Toggle Admin Active Home"));
    }

    @Operation(summary = "Upload banner image",
            description = "Admin API — Upload an image file for a hero banner slide.")
    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<HeroBannerResponseDto>> updateBannerImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        HeroBannerResponseDto response = heroBannerService.uploadBannerImage(id, file);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Upload banner image successfully")
        );
    }
}
