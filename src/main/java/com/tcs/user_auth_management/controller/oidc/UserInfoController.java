package com.tcs.user_auth_management.controller.oidc;

import com.tcs.user_auth_management.model.dto.oidc.DtoUserInfoResponse;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.service.oidc.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2/userinfo")
@AllArgsConstructor
@Tag(name = "OIDC UserInfo", description = "OpenID Connect UserInfo 1.0 endpoint")
@SecurityRequirement(name = "bearerAuth")
public class UserInfoController {
  private final UserInfoService userInfoService;

  @GetMapping
  @Operation(summary = "Get UserInfo (GET)", description = "Returns user claims based on authorized scopes")
  public ResponseEntity<DtoUserInfoResponse> getUserInfoGet() {
    return ResponseEntity.ok(resolveUserInfo());
  }

  @PostMapping
  @Operation(summary = "Get UserInfo (POST)", description = "Returns user claims based on authorized scopes")
  public ResponseEntity<DtoUserInfoResponse> getUserInfoPost() {
    return ResponseEntity.ok(resolveUserInfo());
  }

  private DtoUserInfoResponse resolveUserInfo() {
    UserSecurity currentUser = UserSecurity.getRequiredCurrentUser();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String scopes = null;
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      scopes = jwtAuth.getToken().getClaimAsString("scope");
    }
    return userInfoService.getUserInfo(currentUser.getUserId(), scopes);
  }
}
