package com.tcs.user_auth_management.emuns;

public enum UserType {
    NORMAL_USER("ROLE_NORMAL", "Normal User"),
    SYSTEM_USER("ROLE_SYSTEM", "System User");

    private final String role;
    private final String description;

    UserType(String role, String description) {
        this.role = role;
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public String getDescription() {
        return description;
    }

    // Helper method to check if user has system privileges
    public boolean isSystemUser() {
        return this == SYSTEM_USER;
    }

    // Get role prefix for Spring Security
    public String getAuthority() {
        return role;
    }
}
