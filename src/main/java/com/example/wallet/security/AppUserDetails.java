package com.example.wallet.security;

import com.example.wallet.model.enums.UserRole;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class AppUserDetails extends User {

    private final Long userId;
    private final UserRole role;

    public AppUserDetails(Long userId, String email, String password, UserRole role) {
        super(email, password, authorities(role));
        this.userId = userId;
        this.role = role;
    }

    private static Collection<? extends GrantedAuthority> authorities(UserRole role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }
}
