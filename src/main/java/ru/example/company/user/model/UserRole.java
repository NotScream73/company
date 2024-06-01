package ru.example.company.user.model;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    USER,
    MODERATOR;

    private static final String PREFIX = "ROLE_";

    @Override
    public String getAuthority() {
        return PREFIX + this.name();
    }

    public static final class AsString {
        public static final String USER = PREFIX + "USER";
        public static final String MODERATOR = PREFIX + "MODERATOR";
    }
}
