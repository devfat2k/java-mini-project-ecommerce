package com.devfat.mini_ecommerce.service;

import com.devfat.mini_ecommerce.dto.request.ChangePasswordRequestDto;
import com.devfat.mini_ecommerce.dto.request.UpdateProfileRequestDto;
import com.devfat.mini_ecommerce.dto.response.UserResponseDto;
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
