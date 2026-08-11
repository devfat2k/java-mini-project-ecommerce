package com.devfat.mini_ecommerce.user.address.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<UserAddressEntity, Long> {
    List<UserAddressEntity> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    Optional<UserAddressEntity> findByUserIdAndDefaultAddressIsTrue(Long userId);


}
