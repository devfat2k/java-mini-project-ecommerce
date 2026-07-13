package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.dto.request.LoginRequestDto;
import com.devfat.mini_ecommerce.dto.request.RegisterRequestDto;
import com.devfat.mini_ecommerce.dto.response.AuthResponseDto;
import com.devfat.mini_ecommerce.dto.response.UserResponseDto;
import com.devfat.mini_ecommerce.entity.UserEntity;
import com.devfat.mini_ecommerce.exception.DuplicateResourceException;
import com.devfat.mini_ecommerce.repository.UserRepository;
import com.devfat.mini_ecommerce.security.JwtProvider;
import com.devfat.mini_ecommerce.security.UserPrincipal;
import com.devfat.mini_ecommerce.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;


@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;

    private UserResponseDto toResponseDto(UserEntity userEntity) {
      return UserResponseDto.builder()
                .userId(userEntity.getId())
                .fullName(userEntity.getFullName())
                .email(userEntity.getEmail())
                .phoneNumber(String.valueOf(userEntity.getPhoneNumber()))
                .role(userEntity.getRole())
                .isActive(userEntity.isActive())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto registerRequestDto) {
        String email = registerRequestDto.email().trim().toLowerCase(Locale.ROOT);
        String phone = registerRequestDto.phoneNumber().trim();

        boolean presentEmail = userRepository.findByEmail(email).isPresent();
        boolean presentPhone = userRepository.findByPhoneNumber(phone).isPresent();
        if (presentEmail) throw new DuplicateResourceException("User already exists with email: " + email);
        if (presentPhone) throw new DuplicateResourceException("User already exists with phone number: " + phone);


        String rawPassword = registerRequestDto.password();
        String hashedPassword = passwordEncoder.encode(rawPassword);

        UserEntity newUser = new UserEntity();
        newUser.setFullName(registerRequestDto.fullName());
        newUser.setEmail(email);
        newUser.setPhoneNumber(phone);
        newUser.setRole(UserEntity.Role.USER); //Mặc định luồng đăng ký này là user bình thường
        newUser.setActive(true); // Mặc định tạo sẽ đang hoạt động - Nếu có thay update cờ này về false
        newUser.setPassword(hashedPassword);

        return toResponseDto(userRepository.save(newUser));
    }

    /**
     *
     * @param loginRequestDto
     * @return
     * 1. Tạo UsernamePasswordAuthenticationToken(email, password) — token "thô" chưa xác thực
     * 2. Gọi authenticationManager.authenticate(token đó)
     *    -> Bên trong Spring tự gọi CustomUserDetailsService (B1) + PasswordEncoder (B2)
     *    -> Nếu sai -> tự động ném BadCredentialsException (bạn KHÔNG tự viết if/else so sánh)
     * 3. Lấy Authentication trả về, cast/lấy Principal ra thành UserPrincipal
     * 4. Gọi jwtProvider.generateToken(userPrincipal) (C3) -> nhận accessToken
     * 5. Build AuthResponseDto trả về
     */

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.email().trim().toLowerCase(Locale.ROOT);
        String password = loginRequestDto.password().trim().toLowerCase(Locale.ROOT);

        UsernamePasswordAuthenticationToken tokenRequest = new UsernamePasswordAuthenticationToken(email, password);
        Authentication lastestResult = authenticationManager.authenticate(tokenRequest);
        UserPrincipal userPrincipal = (UserPrincipal) lastestResult.getPrincipal();
        String accessToken = jwtProvider.generateToken(userPrincipal);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getExpirationMs())
                .build();
    }
}
