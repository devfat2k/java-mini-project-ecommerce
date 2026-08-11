package com.devfat.mini_ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionCacheDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String email;
    private Set<String> roles;
    private Set<String> permissions;
}
