package com.devfat.mini_ecommerce.repository;

import com.devfat.mini_ecommerce.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
