package com.devfat.mini_ecommerce.user.internal;

import com.devfat.mini_ecommerce.shared.security.UserPrincipal;



import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailWithRolesAndPermissions(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Set<String> roleNames = new HashSet<>();
        Set<String> permissionCodes = new HashSet<>();

        if(user.getRoles() != null) {
            for (RoleEntity role : user.getRoles()) {
                roleNames.add(role.getName());

                if(role.getPermissions() != null) {
                    for (PermissionEntity permission : role.getPermissions()) {
                        permissionCodes.add(permission.getCode());
                    }
                }
            }
        }

        return UserPrincipal.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .roles(roleNames)
                .permissions(permissionCodes)
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
