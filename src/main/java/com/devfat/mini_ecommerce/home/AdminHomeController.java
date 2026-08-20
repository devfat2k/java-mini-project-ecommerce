package com.devfat.mini_ecommerce.home;

import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/home")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Home", description = "Admin Home Content Management API")
public class AdminHomeController {

    private final HomeService homeService;

    @Operation(
            summary = "Refresh home page cache",
            description = "Admin API — Evict and refresh all cached home page sections."
    )
    @PostMapping("/cache/evict")
    public ResponseEntity<ApiResponse<Void>> evictHomeCache() {
        homeService.evictAllHomeCaches();
        return ResponseEntity.ok(ApiResponse.success(null, "Home cache evicted successfully"));
    }
}
