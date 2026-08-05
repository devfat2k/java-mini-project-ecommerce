package com.devfat.mini_ecommerce.user;

import com.devfat.mini_ecommerce.user.dto.ChangePasswordRequestDto;
import com.devfat.mini_ecommerce.user.dto.UpdateProfileRequestDto;
import com.devfat.mini_ecommerce.user.dto.UserResponseDto;










import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {
    UserResponseDto getMe(Long id);
    void changePassword(Long id, ChangePasswordRequestDto changePasswordRequestDto);
    void updateProfile(Long id, UpdateProfileRequestDto updateProfileRequestDto);
    //admin
    Page<UserResponseDto> getAllUsers(Pageable pageable);
    void updateStatusUser(Long id, Long idInToken, Boolean isActive);
    UserResponseDto uploadUserImage(Long id, MultipartFile file);
    void requestChangePasswordOtp(Long currentUserId);
}
