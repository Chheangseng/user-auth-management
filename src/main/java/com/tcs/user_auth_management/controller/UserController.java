package com.tcs.user_auth_management.controller;

import com.tcs.user_auth_management.model.dto.user.DtoResetPassword;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
  private final AuthService authService;

  @PostMapping("/send-verify-email")
  @Operation(
      summary = "Send verification email",
      description = "Send an email with a verification link/token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Verification email sent"),
    @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<Void> sendVerifyEmail() {
    authService.sendVerifyEmailToken(UserSecurity.userSecurityContext().getUserId());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/verify-email")
  @Operation(
      summary = "Verify user email",
      description = "Verify a user's email address using verification token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Email verified"),
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
  })
  public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
    authService.verifyUserEmail(token);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reset-password")
  @Operation(
      summary = "Reset password",
      description = "Reset user password using reset token from email.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Password reset successful"),
    @ApiResponse(responseCode = "400", description = "Invalid or expired reset token")
  })
  public ResponseEntity<Void> resetUserPassword(
      @Valid @RequestBody DtoResetPassword resetPassword) {
    authService.resetUserPassword(resetPassword);
    return ResponseEntity.ok().build();
  }
}
