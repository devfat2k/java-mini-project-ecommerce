package com.devfat.mini_ecommerce.user;

import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.base.PageResponse;
import com.devfat.mini_ecommerce.shared.security.UserPrincipal;
import com.devfat.mini_ecommerce.user.dto.ChangePasswordRequestDto;
import com.devfat.mini_ecommerce.user.dto.UpdateProfileRequestDto;
import com.devfat.mini_ecommerce.user.dto.UserResponseDto;











import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value="/me/avatar", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UserResponseDto>> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file
    ){
        long userId = userPrincipal.getUserId();
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        userService.uploadUserImage(userId, file),
                        "Upload User Successfully!"
                )
        );
    }
}
