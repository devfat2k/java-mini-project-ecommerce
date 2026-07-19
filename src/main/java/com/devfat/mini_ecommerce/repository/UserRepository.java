package com.devfat.mini_ecommerce.repository;

import com.devfat.mini_ecommerce.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email); // DÙNG Ở AUTH
    Optional<UserEntity> findByPhoneNumber(String phoneNumber); // DÙNG Ở AUTH
}
