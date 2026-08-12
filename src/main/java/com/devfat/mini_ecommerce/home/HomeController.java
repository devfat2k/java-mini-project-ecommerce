package com.devfat.mini_ecommerce.home;

import com.devfat.mini_ecommerce.home.dto.HomePageDataDto;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "Public Home Page Data API")
public class HomeController {

    private final HomeService homeService;

    @Operation(
            summary = "Get home page aggregated data",
            description = "Public API — Retrieve 8 aggregated sections for rendering home page."
    )
    @SecurityRequirements({})
    @GetMapping
    public ResponseEntity<ApiResponse<HomePageDataDto>> getHome() {
        return ResponseEntity.ok(ApiResponse.success(
                homeService.getHomePageData(),
                "Get home page data successfully"
        ));
    }
}
