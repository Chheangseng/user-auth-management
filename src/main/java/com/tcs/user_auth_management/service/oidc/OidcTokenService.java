package com.tcs.user_auth_management.service.oidc;

import com.fasterxml.uuid.Generators;
import com.tcs.user_auth_management.config.oidc.OidcProperties;
import com.tcs.user_auth_management.emuns.AuditLogEvent;
import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.emuns.UserType;
import com.tcs.user_auth_management.exception.AppException;
import com.tcs.user_auth_management.exception.errorcode.AuthErrorCode;
import com.tcs.user_auth_management.exception.errorcode.OidcErrorCode;
import com.tcs.user_auth_management.exception.errorcode.UserErrorCode;
import com.tcs.user_auth_management.model.dto.DtoUserRequestInfo;
import com.tcs.user_auth_management.model.dto.JwtPayload;
import com.tcs.user_auth_management.model.dto.oidc.DtoOidcTokenResponse;
import com.tcs.user_auth_management.model.dto.oidc.DtoTokenRequest;
import com.tcs.user_auth_management.model.entity.oidc.AuthorizationCode;
import com.tcs.user_auth_management.model.entity.oidc.OidcClient;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSession;
import com.tcs.user_auth_management.service.TokenJwtVerifyService;
import com.tcs.user_auth_management.service.user.AuthService;
import com.tcs.user_auth_management.service.user.UserActivityService;
import com.tcs.user_auth_management.service.user.UserRequestInfoService;
import com.tcs.user_auth_management.service.user.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OidcTokenService {
  private final OidcClientService clientService;
  private final AuthorizationService authService;
  private final PkceValidator pkceValidator;
  private final UserSessionService sessionService;
  private final AuthService userAuthService;
  private final JwtEncoder encoder;
  private final TokenJwtVerifyService jwtVerifyService;
  private final OidcProperties oidcProperties;
  private final UserActivityService activityService;
  private final UserRequestInfoService requestInfoService;
  private final HttpServletRequest request;

  @Transactional
  public DtoOidcTokenResponse handleTokenRequest(DtoTokenRequest tokenReq) {
    String grantType = tokenReq.grantType();
    if (grantType == null || grantType.isBlank()) {
      throw new AppException(OidcErrorCode.UNSUPPORTED_GRANT_TYPE, "grant_type is required");
    }

    return switch (grantType.toLowerCase()) {
      case "authorization_code" -> exchangeAuthorizationCode(tokenReq);
      case "refresh_token" -> exchangeRefreshToken(tokenReq);
      case "client_credentials" -> exchangeClientCredentials(tokenReq);
      default -> throw new AppException(OidcErrorCode.UNSUPPORTED_GRANT_TYPE, "Unsupported grant_type: " + grantType);
    };
  }

  private DtoOidcTokenResponse exchangeAuthorizationCode(DtoTokenRequest tokenReq) {
    if (tokenReq.code() == null || tokenReq.code().isBlank()) {
      throw new AppException(OidcErrorCode.INVALID_AUTHORIZATION_CODE, "code is required");
    }
    if (tokenReq.clientId() == null || tokenReq.clientId().isBlank()) {
      throw new AppException(OidcErrorCode.INVALID_CLIENT, "client_id is required");
    }

    OidcClient client = clientService.authenticateClient(tokenReq.clientId(), tokenReq.clientSecret());
    AuthorizationCode authCode = authService.consumeAuthorizationCode(tokenReq.code());

    if (!authCode.getClient().getId().equals(client.getId())) {
      throw new AppException(OidcErrorCode.CODE_CLIENT_MISMATCH);
    }

    if (tokenReq.redirectUri() == null || !tokenReq.redirectUri().equals(authCode.getRedirectUri())) {
      throw new AppException(OidcErrorCode.INVALID_REDIRECT_URI, "redirect_uri does not match");
    }

    // PKCE verification
    if (client.isRequirePkce() || authCode.getCodeChallenge() != null) {
      if (!pkceValidator.validate(tokenReq.codeVerifier(), authCode.getCodeChallenge(), authCode.getCodeChallengeMethod())) {
        throw new AppException(OidcErrorCode.PKCE_VERIFICATION_FAILED);
      }
    }

    UserAuth user = authCode.getUserAuth();
    if (!user.isEnabled()) {
      throw new AppException(UserErrorCode.ACCOUNT_LOCKED);
    }

    Set<String> scopes = Arrays.stream(authCode.getScopes().split(" "))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());

    long accessValidity = client.getAccessTokenValiditySeconds();
    long refreshValidity = client.getRefreshTokenValiditySeconds();

    UserSession session = sessionService.createSession(user, refreshValidity);

    String accessToken = createOidcAccessToken(user, client, scopes, session, accessValidity);
    String idToken = null;
    if (scopes.contains("openid")) {
      idToken = createIdToken(user, client, authCode.getNonce(), scopes, accessValidity);
    }

    String refreshToken = null;
    if (scopes.contains("offline_access") || client.getGrantTypeSet().contains("refresh_token")) {
      refreshToken = createOidcRefreshToken(user, client, session, refreshValidity);
    }

    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    activityService.saveAudit(requestInfo, user.getId(), AuditLogEvent.OIDC_TOKEN_ISSUED);

    return DtoOidcTokenResponse.of(accessToken, accessValidity, refreshToken, idToken, String.join(" ", scopes));
  }

  private DtoOidcTokenResponse exchangeRefreshToken(DtoTokenRequest tokenReq) {
    if (tokenReq.refreshToken() == null || tokenReq.refreshToken().isBlank()) {
      throw new AppException(AuthErrorCode.INVALID_TOKEN, "refresh_token is required");
    }
    OidcClient client = clientService.authenticateClient(tokenReq.clientId(), tokenReq.clientSecret());
    clientService.validateGrantType(client, "refresh_token");

    Jwt jwt = jwtVerifyService.verifyToken(tokenReq.refreshToken(), JwtTokenType.REFRESH);
    JwtPayload jwtPayload = new JwtPayload(jwt);
    UserAuth user = userAuthService.isUserActive(jwtPayload.getUserId());

    long refreshValidity = client.getRefreshTokenValiditySeconds();
    UserSession rotatedSession = sessionService.rotateSessionToken(jwtPayload.getJwtId(), refreshValidity);

    String requestedScopeStr = jwt.getClaimAsString("scope");
    Set<String> scopes = (requestedScopeStr != null && !requestedScopeStr.isBlank())
        ? Arrays.stream(requestedScopeStr.split(" ")).collect(Collectors.toSet())
        : Set.of("openid");

    long accessValidity = client.getAccessTokenValiditySeconds();
    String accessToken = createOidcAccessToken(user, client, scopes, rotatedSession, accessValidity);
    String newRefreshToken = createOidcRefreshToken(user, client, rotatedSession, refreshValidity);
    String idToken = null;
    if (scopes.contains("openid")) {
      idToken = createIdToken(user, client, null, scopes, accessValidity);
    }

    return DtoOidcTokenResponse.of(accessToken, accessValidity, newRefreshToken, idToken, String.join(" ", scopes));
  }

  private DtoOidcTokenResponse exchangeClientCredentials(DtoTokenRequest tokenReq) {
    OidcClient client = clientService.authenticateClient(tokenReq.clientId(), tokenReq.clientSecret());
    if (client.isPublicClient()) {
      throw new AppException(OidcErrorCode.PUBLIC_CLIENT_RESTRICTION);
    }
    clientService.validateGrantType(client, "client_credentials");

    Set<String> scopes = clientService.validateAndFilterScopes(client, tokenReq.scope());
    long accessValidity = client.getAccessTokenValiditySeconds();
    Instant now = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .id(Generators.timeBasedEpochGenerator().generate().toString())
        .issuer(oidcProperties.issuer())
        .subject(client.getClientId())
        .audience(Collections.singletonList(client.getClientId()))
        .issuedAt(now)
        .expiresAt(now.plusSeconds(accessValidity))
        .claim("scope", String.join(" ", scopes))
        .claim("type", JwtTokenType.ACCESS_TOKEN.getType())
        .claim("client_id", client.getClientId())
        .build();

    String accessToken = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    return DtoOidcTokenResponse.of(accessToken, accessValidity, null, null, String.join(" ", scopes));
  }

  private String createOidcAccessToken(
      UserAuth user,
      OidcClient client,
      Set<String> scopes,
      UserSession session,
      long validitySeconds) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .id(session.getJwtTokenId().toString())
        .issuer(oidcProperties.issuer())
        .subject(user.getId().toString())
        .audience(Collections.singletonList(client.getClientId()))
        .issuedAt(now)
        .expiresAt(now.plusSeconds(validitySeconds))
        .claim("session-Id", session.getId().toString())
        .claim("client_id", client.getClientId())
        .claim("scope", String.join(" ", scopes))
        .claim("type", JwtTokenType.ACCESS_TOKEN.getType())
        .claim("user_type", UserType.NORMAL_USER.getRole())
        .build();

    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  private String createOidcRefreshToken(
      UserAuth user,
      OidcClient client,
      UserSession session,
      long validitySeconds) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .id(session.getJwtTokenId().toString())
        .issuer(oidcProperties.issuer())
        .subject(user.getId().toString())
        .audience(Collections.singletonList(client.getClientId()))
        .issuedAt(now)
        .expiresAt(now.plusSeconds(validitySeconds))
        .claim("session-Id", session.getId().toString())
        .claim("client_id", client.getClientId())
        .claim("type", JwtTokenType.REFRESH.getType())
        .claim("user_type", UserType.NORMAL_USER.getRole())
        .build();

    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  private String createIdToken(
      UserAuth user,
      OidcClient client,
      String nonce,
      Set<String> scopes,
      long validitySeconds) {
    JwtClaimsSet.Builder builder = JwtPayload.toIdTokenClaim(
        user.getId(),
        oidcProperties.issuer(),
        client.getClientId(),
        nonce,
        scopes,
        user,
        validitySeconds
    );
    return encoder.encode(JwtEncoderParameters.from(builder.build())).getTokenValue();
  }

  public Map<String, Object> introspectToken(String token) {
    try {
      Jwt jwt = jwtVerifyService.verifyToken(token);
      Map<String, Object> response = new HashMap<>();
      response.put("active", true);
      response.put("sub", jwt.getSubject());
      response.put("iss", jwt.getIssuer() != null ? jwt.getIssuer().toString() : oidcProperties.issuer());
      response.put("exp", jwt.getExpiresAt() != null ? jwt.getExpiresAt().getEpochSecond() : null);
      response.put("iat", jwt.getIssuedAt() != null ? jwt.getIssuedAt().getEpochSecond() : null);
      response.put("client_id", jwt.getClaimAsString("client_id"));
      response.put("scope", jwt.getClaimAsString("scope"));
      response.put("token_type", "Bearer");
      return response;
    } catch (JwtException | AppException e) {
      return Map.of("active", false);
    }
  }

  @Transactional
  public void revokeToken(String token) {
    try {
      Jwt jwt = jwtVerifyService.verifyToken(token);
      String sessionIdStr = jwt.getClaimAsString("session-Id");
      if (sessionIdStr != null) {
        sessionService.invokeSessionById(UUID.fromString(sessionIdStr));
      }
      DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
      try {
        UUID userId = UUID.fromString(jwt.getSubject());
        activityService.saveAudit(requestInfo, userId, AuditLogEvent.OIDC_TOKEN_REVOKED);
      } catch (Exception ignored) {}
    } catch (Exception e) {
      // Per RFC 7009, revocation of an invalid token does not return an error
    }
  }
}
