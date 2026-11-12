package com.tcs.user_auth_management.model.dto;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.emuns.UserType;
import com.tcs.user_auth_management.exception.jwt.JwtInvalidPayloadException;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;

@Data
@Builder
public class JwtPayload {
  private static final Logger log = LoggerFactory.getLogger(JwtPayload.class);
  private UUID jwtId;

  @Getter(AccessLevel.NONE)
  private UUID sessionId;

  private UUID userId;

  private JwtTokenType type;

  private UserType userType;

  public JwtPayload(UUID userId, UUID jwtId, JwtTokenType jwtTokenType, UserType userType) {
    this(userId, null, jwtId, jwtTokenType, userType);
  }

  public JwtPayload(
      UUID userId, UUID sessionId, UUID jwtId, JwtTokenType jwtTokenType, UserType userType) {
    this.userId = userId;
    this.sessionId = sessionId;
    this.jwtId = jwtId;
    this.type = jwtTokenType;
    this.userType = userType;
  }

  public JwtPayload(Jwt jwt) {
    try {
      this.jwtId = UUID.fromString(jwt.getId());
      this.userId = UUID.fromString(jwt.getSubject());
      String typeClaim = jwt.getClaimAsString("type");
      this.type = JwtTokenType.fromType(typeClaim).orElse(null);
      var sessionIdClaim = jwt.getClaim("session-Id");
      if (Objects.nonNull(sessionIdClaim)) {
        this.sessionId = UUID.fromString(sessionIdClaim.toString());
      }
    } catch (Exception e) {
      log.error(
          "Failed to parse JWT payload - JWT ID: {}, Subject: {}, Full JWT: {}",
          jwt.getId(),
          jwt.getSubject(),
          jwt,
          e);
      throw new JwtInvalidPayloadException();
    }
  }

  public JwtClaimsSet.Builder toJwtClaim() {
    var base =
        JwtClaimsSet.builder()
            .id(this.jwtId.toString())
            .issuer("authentication-server")
            .subject(this.userId.toString())
            .claim("type", type != null ? type.getType() : null);
    if (Objects.nonNull(sessionId)) {
      base.claim("session-Id", this.sessionId);
    }
    return base;
  }

  public static JwtClaimsSet.Builder toIdTokenClaim(
      UUID userId,
      String issuer,
      String clientId,
      String nonce,
      Set<String> scopes,
      UserAuth user,
      long validitySeconds) {
    Instant now = Instant.now();
    var builder =
        JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .issuer(issuer)
            .subject(userId.toString())
            .audience(Collections.singletonList(clientId))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(validitySeconds))
            .claim("auth_time", now.getEpochSecond())
            .claim("type", JwtTokenType.ID_TOKEN.getType());

    if (nonce != null && !nonce.isBlank()) {
      builder.claim("nonce", nonce);
    }
    if (scopes != null) {
      if (scopes.contains("email") || scopes.contains("openid")) {
        builder.claim("email", user.getRecoveryEmail());
        builder.claim("email_verified", user.isEmailVerified());
      }
      if (scopes.contains("profile")) {
        builder.claim("preferred_username", user.getUsername());
        builder.claim("name", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
        if (user.getProfilePictureUrl() != null) {
          builder.claim("picture", user.getProfilePictureUrl());
        }
        if (user.getLocale() != null) {
          builder.claim("locale", user.getLocale());
        }
        if (user.getZoneinfo() != null) {
          builder.claim("zoneinfo", user.getZoneinfo());
        }
      }
      if (scopes.contains("phone") && user.getPhoneNumber() != null) {
        builder.claim("phone_number", user.getPhoneNumber());
      }
      if (scopes.contains("address") && user.getAddress() != null) {
        builder.claim("address", user.getAddress());
      }
    }
    return builder;
  }

  public Optional<UUID> getSessionId() {
    return Optional.ofNullable(this.sessionId);
  }

  public static String jwtType(Jwt jwt){
    return jwt.getClaimAsString("type");
  }
}
