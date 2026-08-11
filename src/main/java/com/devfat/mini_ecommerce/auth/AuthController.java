package com.devfat.mini_ecommerce.auth;

import com.devfat.mini_ecommerce.auth.dto.*;
import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.security.UserPrincipal;
import com.devfat.mini_ecommerce.user.dto.ChangePasswordRequestDto;
import com.devfat.mini_ecommerce.user.dto.UserResponseDto;
import com.devfat.mini_ecommerce.shared.ratelimit.RateLimit;
import com.devfat.mini_ecommerce.shared.ratelimit.RateLimitType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@SecurityRequirements({})
@Tag(name = "Auth", description = "Authentication & Password Management")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register user", description = "Register a new user account.")
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

    @Operation(summary = "Login user", description = "Authenticate user credentials and return JWT tokens.")
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

    @Operation(summary = "Refresh token", description = "Obtain a new access token using a valid refresh token.")
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

    @Operation(summary = "Logout user", description = "Invalidate user refresh token and logout.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        authService.logout(request);
        return ResponseEntity.ok().body(ApiResponse.success(null, "Logout Successfully!"));
    }

    @Operation(summary = "Verify OTP", description = "Verify one-time password for account activation.")
    @RateLimit(type = RateLimitType.OTP, byIp = true)
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponseDto>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.verifyOtp(dto),
                "Verify Otp Successfully!"
        ));
    }

    @Operation(summary = "Forgot password", description = "Request password reset OTP code.")
    @RateLimit(type = RateLimitType.PUBLIC_API, byIp = true)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto dto) {
        authService.forgotPassword(dto);
        return ResponseEntity.ok(ApiResponse.success(
                null,
                "Forgot Password Successfully!"
        ));
    }

    @Operation(summary = "Resend OTP", description = "Resend a new verification OTP code.")
    @RateLimit(type = RateLimitType.OTP, byIp = true)
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<ResendOtpResponseDto>> resendOtp(@Valid @RequestBody ResendOtpRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.resendOtp(dto),
                "Resend Otp Successfully!"
        ));
    }

    @Operation(summary = "Reset password", description = "Reset user password using valid token or OTP.")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto resetPasswordRequest
    ) {
        authService.resetPassword(resetPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(
                null,
                "Reset Password Successfully!"
        ));
    }


}
