package com.devfat.mini_ecommerce.user.internal;

import com.devfat.mini_ecommerce.shared.security.UserPrincipal;



import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole().name())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
