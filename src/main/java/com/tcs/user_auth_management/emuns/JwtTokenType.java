package com.tcs.user_auth_management.emuns;

import java.util.Optional;
import lombok.Getter;

@Getter
public enum JwtTokenType {
    ACCESS_TOKEN("access_token"),
    REFRESH("refresh"),
    TEMPORARY("temporary"),
    RESET_PASSWORD("reset_password"),
    VERIFY_EMAIL("verify_email"); // new reset password token type

    private final String type;

    JwtTokenType(String type) {
        this.type = type;
    }

    public static Optional<JwtTokenType> fromType(String type) {
        for (JwtTokenType t : JwtTokenType.values()) {
            if (t.type.equalsIgnoreCase(type)) {
                return Optional.of(t);
            }
        }
       return Optional.empty();
    }
}

