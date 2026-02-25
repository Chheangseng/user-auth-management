package com.tcs.user_auth_management.controller;

import com.tcs.user_auth_management.model.dto.DtoJwtTokenResponse;
import com.tcs.user_auth_management.model.dto.user.DtoEmail;
import com.tcs.user_auth_management.model.dto.user.DtoResetPassword;
import com.tcs.user_auth_management.model.dto.user.DtoUserLogin;
import com.tcs.user_auth_management.model.dto.user.DtoUserRegister;
import com.tcs.user_auth_management.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/public/auth")
@AllArgsConstructor
@RestController
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "Login user", description = "Authenticate user and return JWT tokens")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Login successful",
                  content = @Content(schema = @Schema(implementation = DtoJwtTokenResponse.class))),
          @ApiResponse(responseCode = "401", description = "Invalid credentials")
  })
  public ResponseEntity<DtoJwtTokenResponse> login(@Valid @RequestBody DtoUserLogin login) {
    return ResponseEntity.ok(authService.loginUser(login));
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout from current session",
          description = "Invalidate the provided refresh token.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Logout successful"),
          @ApiResponse(responseCode = "400", description = "Invalid refresh token")
  })
  public ResponseEntity<Void> logout(@RequestParam String refreshToken) {
    authService.logout(refreshToken);
    return ResponseEntity.ok().build();
  }

  @PostMapping("logout-all-session")
  @Operation(summary = "Logout from all sessions",
          description = "Invalidate all refresh tokens for the current user.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "All sessions logged out"),
          @ApiResponse(responseCode = "400", description = "Invalid refresh token")
  })
  public ResponseEntity<Void> logoutAll(@RequestParam String refreshToken) {
    authService.logoutAll(refreshToken);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/register")
  @Operation(summary = "Register new account",
          description = "Create a new user and return initial JWT tokens.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Registration successful",
                  content = @Content(schema = @Schema(implementation = DtoJwtTokenResponse.class))),
          @ApiResponse(responseCode = "409", description = "Email already exists")
  })
  public ResponseEntity<DtoJwtTokenResponse> register(
          @Valid @RequestBody DtoUserRegister register) {
    return ResponseEntity.ok(authService.registerAccount(register));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Refresh JWT token",
          description = "Generate new access token using the refresh token.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Token refreshed",
                  content = @Content(schema = @Schema(implementation = DtoJwtTokenResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid refresh token")
  })
  public ResponseEntity<DtoJwtTokenResponse> refreshToken(@RequestParam String refreshToken) {
    return ResponseEntity.ok(authService.refreshToken(refreshToken));
  }

  @PostMapping("/forgot")
  @Operation(summary = "Forgot password",
          description = "Sends a password reset token to user email.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Reset email sent"),
          @ApiResponse(responseCode = "404", description = "Email not registered")
  })
  public ResponseEntity<String> forgotPassword(@Valid @RequestBody DtoEmail email) {
    authService.forgotPassword(email.email());
    return ResponseEntity.ok("We will send you a reset password token");
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Reset password",
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