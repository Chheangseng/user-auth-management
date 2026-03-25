package com.tcs.user_auth_management.model.dto.user;

import jakarta.validation.constraints.NotBlank;

public record DtoChangePassword(
    @NotBlank(message = "old password is required") String oldPassword,
    @NotBlank(message = "new password is required") String newPassword) {}
