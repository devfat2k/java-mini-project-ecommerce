package com.devfat.mini_ecommerce.service.impl;

import com.devfat.mini_ecommerce.dto.request.ChangePasswordRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateProfileRequestDto;
import com.devfat.mini_ecommerce.dto.response.UserResponseDto;
import com.devfat.mini_ecommerce.entity.RefreshTokenEntity;
import com.devfat.mini_ecommerce.entity.UserEntity;
import com.devfat.mini_ecommerce.exception.BadRequestException;
import com.devfat.mini_ecommerce.exception.ResourceNotFoundException;
import com.devfat.mini_ecommerce.repository.RefreshTokenRepository;
import com.devfat.mini_ecommerce.repository.UserRepository;
import com.devfat.mini_ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto toResponseDto(UserEntity userEntity) {
        return UserResponseDto.builder()
                .userId(userEntity.getId())
                .email(userEntity.getEmail())
                .fullName(userEntity.getFullName())
                .phoneNumber(userEntity.getPhoneNumber())
                .role(userEntity.getRole())
                .isActive(userEntity.isActive())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getMe(Long id) {
        return userRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponseDto);
    }



    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequestDto changePasswordRequestDto) {
        String passwordOld = changePasswordRequestDto.oldPassword().trim();
        String newPassword = changePasswordRequestDto.newPassword().trim();

        UserEntity user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isMatch = passwordEncoder.matches(passwordOld, user.getPassword());
        if(!isMatch) {
            throw new BadRequestException("Old password not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long id, UpdateProfileRequestDto updateProfileRequestDto) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if(updateProfileRequestDto.phoneNumber() != null) {
            user.setPhoneNumber(updateProfileRequestDto.phoneNumber());
        }
        if(updateProfileRequestDto.fullName() != null) {
            user.setFullName(updateProfileRequestDto.fullName());
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateStatusUser(Long idInQuery, Long idInToken, Boolean isActive) {
        UserEntity user = userRepository.findById(idInQuery).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(idInToken.equals(user.getId())) {
            throw new BadRequestException("User cannot change status for me!");
        }

        if(user.isActive() == isActive) {
            throw new BadRequestException("User cannot change status current!");
        }

        if(!isActive) {
            List<RefreshTokenEntity> token = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
            token.forEach(tokenEntity -> {
                tokenEntity.setRevoked(true);
            });
            refreshTokenRepository.saveAll(token);
        }
        user.setActive(isActive);
        userRepository.save(user);
    }
}
