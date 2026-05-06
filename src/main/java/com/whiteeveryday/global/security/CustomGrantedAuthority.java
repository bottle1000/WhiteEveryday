package com.whiteeveryday.global.security;

import com.whiteeveryday.domain.user.entity.Role;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
public class CustomGrantedAuthority implements GrantedAuthority {

    private final Role role;

    @Override
    public String getAuthority() {
        return role.name();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof CustomGrantedAuthority) {
            return role.equals(((CustomGrantedAuthority) obj).role);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return role.hashCode();
    }

    @Override
    public String toString() {
        return this.role.name();
    }
}
