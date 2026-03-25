package com.tcs.user_auth_management.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Password reset request")
public record DtoResetPassword(
        @Schema(description = "Password reset token received via email",
                example = "abc-123-def-456")
        @NotBlank(message = "Reset token is required")
        String resetToken,

        @Schema(description = "New password",
                example = "NewPassword123!")
        @NotBlank(message = "New Password is required")
        String newPassword
) {}
