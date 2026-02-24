package com.tcs.user_auth_management.emuns;

import lombok.Getter;

@Getter
public enum AuditLogEvent {
    LOGIN_SUCCESS("LOGIN_SUCCESS"),
    LOGIN_FAILURE("LOGIN_FAILURE"),
    LOGOUT("LOGOUT"),
    PASSWORD_CHANGE("PASSWORD_CHANGE"),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED"),
    ACCOUNT_UNLOCKED("ACCOUNT_UNLOCKED"),
    MFA_ENABLED("MFA_ENABLED"),
    MFA_DISABLED("MFA_DISABLED"),
    PROFILE_UPDATE("PROFILE_UPDATE"),
    USER_REGISTRATION("USER_REGISTRATION");

    private final String action;

    AuditLogEvent(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return this.action;
    }
}
