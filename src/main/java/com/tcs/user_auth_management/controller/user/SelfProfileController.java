package com.tcs.user_auth_management.controller.user;

import com.tcs.user_auth_management.model.dto.param.AuditLogSelfRequestParam;
import com.tcs.user_auth_management.model.dto.user.DtoAuditLog;
import com.tcs.user_auth_management.model.dto.user.DtoChangePassword;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.service.AuthService;
import com.tcs.user_auth_management.service.user.UserActivityService;
import com.tcs.user_auth_management.service.user.UserSessionService;
import com.tcs.user_auth_management.util.pagination.PaginationEntityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user/me")
@Tag(name = "Self information")
public class SelfProfileController {
  private final AuthService authService;
  private final UserSessionService userSessionService;
  private final UserActivityService userActivityService;

  @GetMapping
  public ResponseEntity<UserSecurity> userSecurity() {
    return ResponseEntity.ok(UserSecurity.getRequiredCurrentUser());
  }

  @GetMapping("/audit-logs")
  public ResponseEntity<PaginationEntityResponse<DtoAuditLog>> pageAudit(
      @ParameterObject AuditLogSelfRequestParam param) {
    return ResponseEntity.ok(userActivityService.pageSelfAudit(param));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    userSessionService.invokeSession(UserSecurity.getRequiredCurrentUser().getJwtId());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/send-verify-email")
  @Operation(
      summary = "Send verification email",
      description = "Send an email with a verification link/token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Verification email sent"),
    @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<Void> sendVerifyEmail() {
    authService.sendVerifyEmailToken(UserSecurity.getRequiredCurrentUser().getUserId());
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
  public ResponseEntity<Void> resetUserPassword(
      @Valid @RequestBody DtoChangePassword resetPassword) {
    authService.changePassword(UserSecurity.getRequiredCurrentUser().getUserId(), resetPassword);
    return ResponseEntity.ok().build();
  }
}
