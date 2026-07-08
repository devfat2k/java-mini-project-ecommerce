package com.devfat.mini_ecommerce.controller;

import com.devfat.mini_ecommerce.common.ApiResponse;
import com.devfat.mini_ecommerce.dto.request.RegisterRequestDto;
import com.devfat.mini_ecommerce.dto.response.UserResponseDto;
import com.devfat.mini_ecommerce.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Tag(name = "Auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto registerRequestDto)
    {
        UserResponseDto response = authService.register(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                response,
                "Create User successfully!"
        ));
    }
}
