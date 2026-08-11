package com.devfat.mini_ecommerce.user.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email); // DÙNG Ở AUTH
    Optional<UserEntity> findByPhoneNumber(String phoneNumber); // DÙNG Ở AUTH

    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithRolesAndPermissions(@Param("email") String username);
}
