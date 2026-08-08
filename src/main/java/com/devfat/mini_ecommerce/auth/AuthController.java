package com.devfat.mini_ecommerce.auth;

import com.devfat.mini_ecommerce.auth.dto.AuthResponseDto;
import com.devfat.mini_ecommerce.auth.dto.ForgotPasswordRequestDto;
import com.devfat.mini_ecommerce.auth.dto.LoginRequestDto;
import com.devfat.mini_ecommerce.auth.dto.RefreshTokenRequestDto;
import com.devfat.mini_ecommerce.auth.dto.RefreshTokenResponseDto;
import com.devfat.mini_ecommerce.auth.dto.RegisterRequestDto;
import com.devfat.mini_ecommerce.auth.dto.ResendOtpRequestDto;
import com.devfat.mini_ecommerce.auth.dto.ResendOtpResponseDto;
import com.devfat.mini_ecommerce.auth.dto.VerifyOtpRequestDto;
import com.devfat.mini_ecommerce.auth.dto.VerifyOtpResponseDto;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.user.dto.UserResponseDto;
import com.devfat.mini_ecommerce.shared.ratelimit.RateLimit;
import com.devfat.mini_ecommerce.shared.ratelimit.RateLimitType;
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

    @RateLimit(type = RateLimitType.LOGIN, byIp = true)
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

    @RateLimit(type = RateLimitType.OTP, byIp = true)
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponseDto>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.verifyOtp(dto),
                "Verify Otp Successfully!"
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto dto) {
        authService.forgotPassword(dto);
        return ResponseEntity.ok(ApiResponse.success(
                null,
                "Forgot Password Successfully!"
        ));
    }

    @RateLimit(type = RateLimitType.OTP, byIp = true)
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<ResendOtpResponseDto>> resendOtp(@Valid @RequestBody ResendOtpRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.resendOtp(dto),
                "Resend Otp Successfully!"
        ));
    }

}
