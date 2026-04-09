package com.tcs.user_auth_management.model.dto;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.emuns.UserType;
import com.tcs.user_auth_management.exception.jwt.JwtInvalidPayloadException;
import java.util.Objects;
import java.util.Optional;
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
      this.type = jwt.getClaim("type");
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
            .claim("type", type.getType());
    if (Objects.nonNull(sessionId)) {
      base.claim("session-Id", this.sessionId);
    }
    return base;
  }

  public Optional<UUID> getSessionId() {
    return Optional.ofNullable(this.sessionId);
  }

  public static String jwtType(Jwt jwt){
    return jwt.getClaim("type");
  }
}
