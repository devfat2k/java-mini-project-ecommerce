package com.devfat.mini_ecommerce.controller;

import com.devfat.mini_ecommerce.common.ApiResponse;
import com.devfat.mini_ecommerce.dto.request.LoginRequestDto;
import com.devfat.mini_ecommerce.dto.request.RefreshTokenRequestDto;
import com.devfat.mini_ecommerce.dto.request.RegisterRequestDto;
import com.devfat.mini_ecommerce.dto.response.AuthResponseDto;
import com.devfat.mini_ecommerce.dto.response.RefreshTokenResponseDto;
import com.devfat.mini_ecommerce.dto.response.UserResponseDto;
import com.devfat.mini_ecommerce.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
@SecurityRequirements({})
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
                "Create User Successfully!"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto
            ) {
        AuthResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(
                response,
                "Login Successfully!"
        ));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDto>>  refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto
    ) {
       RefreshTokenResponseDto responseDto = authService.refreshToken(refreshTokenRequestDto);
        return ResponseEntity.ok().body(ApiResponse.success(
                responseDto,
                "Refresh Token Successfully!"
        ));
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        authService.logout(request);
        return ResponseEntity.ok().body(ApiResponse.success(null, "Logout Successfully!"));
    }
}
