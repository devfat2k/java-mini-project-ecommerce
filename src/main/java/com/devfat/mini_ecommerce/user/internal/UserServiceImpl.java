package com.devfat.mini_ecommerce.user.internal;

import com.devfat.mini_ecommerce.auth.OtpPurpose;
import com.devfat.mini_ecommerce.auth.OtpService;
import com.devfat.mini_ecommerce.auth.internal.RefreshTokenEntity;
import com.devfat.mini_ecommerce.auth.internal.RefreshTokenRepository;
import com.devfat.mini_ecommerce.shared.exception.BadRequestException;
import com.devfat.mini_ecommerce.shared.exception.ResourceNotFoundException;
import com.devfat.mini_ecommerce.storage.StorageService;
import com.devfat.mini_ecommerce.user.UserService;
import com.devfat.mini_ecommerce.user.dto.ChangePasswordRequestDto;
import com.devfat.mini_ecommerce.user.dto.UpdateProfileRequestDto;
import com.devfat.mini_ecommerce.user.dto.UserResponseDto;










import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    private final StorageService storageService;
    private final OtpService otpService;
    private final UserMapper userMapper;

//    public UserResponseDto toResponseDto(UserEntity userEntity) {
//        return UserResponseDto.builder()
//                .userId(userEntity.getId())
//                .email(userEntity.getEmail())
//                .fullName(userEntity.getFullName())
//                .avatarUrl(userEntity.getAvatarUrl())
//                .phoneNumber(userEntity.getPhoneNumber())
//                .role(userEntity.getRole())
//                .isActive(userEntity.isActive())
//                .createdAt(userEntity.getCreatedAt())
//                .build();
//    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getMe(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDto);
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

    @Override
    public UserResponseDto uploadUserImage(Long id, MultipartFile file) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        String url = storageService.uploadFile(file, "UserImage", true);
        user.setAvatarUrl(url);
        userRepository.save(user);
        return userMapper.toResponseDto(user);
    }

    @Override
    public void requestChangePasswordOtp(Long currentUserId) {
        UserEntity user = userRepository.findById(currentUserId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        otpService.generateAndSendOtp(user, OtpPurpose.CHANGE_PASSWORD_CONFIRMATION);
    }
}
