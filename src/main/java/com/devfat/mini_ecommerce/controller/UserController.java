package com.devfat.mini_ecommerce.controller;


import com.devfat.mini_ecommerce.common.ApiResponse;
import com.devfat.mini_ecommerce.dto.request.ChangePasswordRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateProfileRequestDto;
import com.devfat.mini_ecommerce.dto.response.UserResponseDto;
import com.devfat.mini_ecommerce.security.UserPrincipal;
import com.devfat.mini_ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User Manager")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        userService.getMe(userPrincipal.getUserId()),
                        "Get User Successfully!"
                )
        );
    }


    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequestDto requestDto
            ) {
        Long userId = userPrincipal.getUserId();
        userService.changePassword(userId, requestDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/update")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequestDto requestDto
    ) {
        Long userId = userPrincipal.getUserId();
        userService.updateProfile(userId, requestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsers(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = direction.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortBy = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);

        return ResponseEntity.ok().body(
                ApiResponse.success(
                        userService.getAllUsers(pageable),
                        "Get All User Successfully!"
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable Long userId,
            @RequestParam boolean isActive,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long idInToken = userPrincipal.getUserId();
        userService.updateStatusUser(userId, idInToken, isActive);
        return ResponseEntity.noContent().build();
    }
}
