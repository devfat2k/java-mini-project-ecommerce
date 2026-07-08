package com.devfat.mini_ecommerce;


import com.devfat.mini_ecommerce.entity.UserEntity;
import com.devfat.mini_ecommerce.repository.UserRepository;
import com.devfat.mini_ecommerce.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;


    @Test
    void loadUserByUsername_shouldReturnUserDetails() {

        // fake user
        UserEntity user = new UserEntity();

        user.setEmail("admin@gmail.com");
        user.setPassword("123456");
        user.setActive(true);
        user.setRole(UserEntity.Role.ADMIN);


        // giả lập repository trả user
        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(user));


        CustomUserDetailsService service =
                new CustomUserDetailsService(userRepository);


        // gọi hàm cần test
        UserDetails result =
                service.loadUserByUsername("admin@gmail.com");


        // kiểm tra
        assertEquals(
                "admin@gmail.com",
                result.getUsername()
        );

        assertEquals(
                "123456",
                result.getPassword()
        );

        assertTrue(
                result.isEnabled()
        );

        assertTrue(
                result.getAuthorities()
                        .stream()
                        .anyMatch(
                                a -> a.getAuthority()
                                        .equals("ROLE_ADMIN")
                        )
        );
    }
}
