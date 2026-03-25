package com.tcs.user_auth_management.controller.user;

import com.tcs.user_auth_management.model.dto.DtoUserSession;
import com.tcs.user_auth_management.model.dto.user.DtoChangePassword;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.service.AuthService;
import com.tcs.user_auth_management.service.user.UserSessionService;
import com.tcs.user_auth_management.util.pagination.PaginationEntityResponse;
import com.tcs.user_auth_management.util.pagination.PaginationParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user/me")
@Tag(name = "Self session information")
public class SelfSessionController {
  private final UserSessionService userSessionService;

  @GetMapping("/sessions")
  public ResponseEntity<PaginationEntityResponse<DtoUserSession>> pagination(
      @ParameterObject PaginationParam paginationParam) {
    return ResponseEntity.ok(
        new PaginationEntityResponse<>(userSessionService.userSessionPage(paginationParam)));
  }

  @PutMapping("/sessions/all/invoke")
  @Operation(summary = "logout all session")
  public ResponseEntity<Void> invokeALLSession() {
    userSessionService.invokeSessionAllByUserAuthId(
        UserSecurity.getRequiredCurrentUser().getUserId());
    return ResponseEntity.ok().build();
  }

  @PutMapping("/sessions/current/invoke")
  @Operation(summary = "logout current session")
  public ResponseEntity<Void> invokeCurrentSession() {
    userSessionService.invokeSessionAllByUserAuthId(
            UserSecurity.getRequiredCurrentUser().getUserId());
    return ResponseEntity.ok().build();
  }

  @PutMapping("/sessions/{sessionId}/invoke")
  @Operation(summary = "logout session by id")
  public ResponseEntity<Void> invokeSession(@PathVariable UUID sessionId) {
    userSessionService.invokeSessionById(sessionId,
            UserSecurity.getRequiredCurrentUser().getUserId());
    return ResponseEntity.ok().build();
  }
}
