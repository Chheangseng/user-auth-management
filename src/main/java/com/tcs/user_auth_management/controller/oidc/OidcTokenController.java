package com.tcs.user_auth_management.controller.oidc;

import com.tcs.user_auth_management.model.dto.oidc.DtoOidcTokenResponse;
import com.tcs.user_auth_management.model.dto.oidc.DtoTokenRequest;
import com.tcs.user_auth_management.service.oidc.OidcTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2")
@AllArgsConstructor
@Tag(name = "OIDC Token", description = "OAuth2 / OIDC Token endpoints")
public class OidcTokenController {
  private final OidcTokenService tokenService;

  @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @Operation(summary = "Token endpoint (Form-encoded)", description = "Exchanges authorization code, refresh token, or client credentials for tokens")
  public ResponseEntity<DtoOidcTokenResponse> tokenForm(
      @RequestParam("grant_type") String grantType,
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "redirect_uri", required = false) String redirectUri,
      @RequestParam(value = "client_id", required = false) String clientId,
      @RequestParam(value = "client_secret", required = false) String clientSecret,
      @RequestParam(value = "code_verifier", required = false) String codeVerifier,
      @RequestParam(value = "refresh_token", required = false) String refreshToken,
      @RequestParam(value = "scope", required = false) String scope,
      HttpServletRequest request) {

    String resolvedClientId = clientId;
    String resolvedClientSecret = clientSecret;

    String authHeader = request.getHeader("Authorization");
    if ((resolvedClientId == null || resolvedClientId.isBlank()) && authHeader != null && authHeader.startsWith("Basic ")) {
      String[] basicAuth = extractBasicAuth(authHeader);
      if (basicAuth != null) {
        resolvedClientId = basicAuth[0];
        resolvedClientSecret = basicAuth[1];
      }
    }

    DtoTokenRequest tokenReq = new DtoTokenRequest(
        grantType,
        code,
        redirectUri,
        resolvedClientId,
        resolvedClientSecret,
        codeVerifier,
        refreshToken,
        scope
    );

    return ResponseEntity.ok(tokenService.handleTokenRequest(tokenReq));
  }

  @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Token endpoint (JSON)", description = "Exchanges authorization code, refresh token, or client credentials for tokens")
  public ResponseEntity<DtoOidcTokenResponse> tokenJson(
      @RequestBody DtoTokenRequest tokenReq,
      HttpServletRequest request) {

    String resolvedClientId = tokenReq.clientId();
    String resolvedClientSecret = tokenReq.clientSecret();

    String authHeader = request.getHeader("Authorization");
    if ((resolvedClientId == null || resolvedClientId.isBlank()) && authHeader != null && authHeader.startsWith("Basic ")) {
      String[] basicAuth = extractBasicAuth(authHeader);
      if (basicAuth != null) {
        resolvedClientId = basicAuth[0];
        resolvedClientSecret = basicAuth[1];
      }
    }

    DtoTokenRequest resolvedReq = new DtoTokenRequest(
        tokenReq.grantType(),
        tokenReq.code(),
        tokenReq.redirectUri(),
        resolvedClientId,
        resolvedClientSecret,
        tokenReq.codeVerifier(),
        tokenReq.refreshToken(),
        tokenReq.scope()
    );

    return ResponseEntity.ok(tokenService.handleTokenRequest(resolvedReq));
  }

  @PostMapping("/introspect")
  @Operation(summary = "Token Introspection", description = "Query the active state and metadata of a token")
  public ResponseEntity<Map<String, Object>> introspect(@RequestParam("token") String token) {
    return ResponseEntity.ok(tokenService.introspectToken(token));
  }

  @PostMapping("/revoke")
  @Operation(summary = "Token Revocation", description = "Revoke an active token")
  public ResponseEntity<Void> revoke(@RequestParam("token") String token) {
    tokenService.revokeToken(token);
    return ResponseEntity.ok().build();
  }

  private String[] extractBasicAuth(String header) {
    try {
      String base64Credentials = header.substring("Basic ".length()).trim();
      byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
      String credentials = new String(credDecoded, StandardCharsets.UTF_8);
      String[] values = credentials.split(":", 2);
      if (values.length == 2) {
        return values;
      }
    } catch (Exception ignored) {}
    return null;
  }
}
