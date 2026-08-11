package com.devfat.mini_ecommerce.user;

import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.base.PageResponse;
import com.devfat.mini_ecommerce.shared.security.UserPrincipal;
import com.devfat.mini_ecommerce.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User", description = "Admin: Quản lý tài khoản người dùng")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getAllUsers(
            Pageable pageable
    ) {
        Page<UserResponseDto> userResponse = userService.getAllUsers(pageable);
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        PageResponse.of(userResponse),
                        "Get All User Successfully!"
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable("id") Long userId,
            @RequestParam boolean isActive,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long idInToken = userPrincipal.getUserId();
        userService.updateStatusUser(userId, idInToken, isActive);
        return ResponseEntity.noContent().build();
    }
}
