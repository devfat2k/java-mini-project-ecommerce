package com.devfat.mini_ecommerce.user.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT DISTINCT r FROM RoleEntity r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<RoleEntity> findByIdWithPermissions(@Param("id") Long id);
}
