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

    @PatchMapping("/update-profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequestDto requestDto
    ) {
        Long userId = userPrincipal.getUserId();
        UserResponseDto result = userService.updateProfile(userId, requestDto);
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        result,
                        "Update User Successfully!"
                )
        );
    }

    @PostMapping(value="/upload-avatar", consumes = "multipart/form-data")
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

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto
    ){
        Long userId = userPrincipal.getUserId();
        userService.changePassword(userId, changePasswordRequestDto);
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        null,
                        "Change Password Successfully!"
                )
        );
    }
}
