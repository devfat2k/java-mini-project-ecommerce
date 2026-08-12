package com.devfat.mini_ecommerce.home.dailyarrival;

import com.devfat.mini_ecommerce.home.dailyarrival.dto.CreateDailyArrivalRequestDto;
import com.devfat.mini_ecommerce.home.dailyarrival.dto.DailyArrivalResponseDto;
import com.devfat.mini_ecommerce.home.dailyarrival.dto.UpdateDailyArrivalRequestDto;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/daily-arrivals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Daily Arrival", description = "Admin Daily Seafood Arrivals Management API")
public class AdminDailyArrivalController {

    private final DailyArrivalService dailyArrivalService;

    @Operation(summary = "Get daily arrivals by date", description = "Admin API — Retrieve daily arrivals for a specific date.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyArrivalResponseDto>>> getByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(dailyArrivalService.getByDate(targetDate), "Get daily arrivals successfully"));
    }

    @Operation(summary = "Create daily arrival item", description = "Admin API — Add a product to daily arrivals.")
    @PostMapping
    public ResponseEntity<ApiResponse<DailyArrivalResponseDto>> create(@RequestBody CreateDailyArrivalRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dailyArrivalService.create(request), "Create daily arrival item successfully"));
    }

    @Operation(summary = "Update daily arrival item", description = "Admin API — Update an existing daily arrival item.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyArrivalResponseDto>> update(@PathVariable Long id, @RequestBody UpdateDailyArrivalRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(dailyArrivalService.update(id, request), "Update daily arrival item successfully"));
    }

    @Operation(summary = "Delete daily arrival item", description = "Admin API — Remove an item from daily arrivals.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        dailyArrivalService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Delete daily arrival item successfully"));
    }
}
