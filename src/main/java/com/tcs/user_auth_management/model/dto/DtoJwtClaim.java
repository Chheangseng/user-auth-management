package com.tcs.user_auth_management.model.dto;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;

import java.util.Objects;
import java.util.UUID;

import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;

public class DtoJwtClaim {
  public static JwtClaimsSet.Builder baseClaim(String tokenId, String userId, JwtTokenType type) {
    return JwtClaimsSet.builder()
        .id(tokenId)
        .issuer("authentication-server")
        .subject(userId)
        .claim("type", type.getType());
  }

  public static JwtClaimsSet.Builder buildAuthenticationJwtClaim(JwtSessionContext context) {
    var base = DtoJwtClaim.baseClaim(context.tokenId, context.userId, context.type);
    if (Objects.nonNull(context.sessionId)){
      base.claim("session-Id", context.sessionId);
    }
    if (Objects.nonNull(context.roleId)){
      base.claim("role-id",context.roleId);
    }
    return base;
  }

  public static UUID getJwtId(Jwt jwt) {
    return UUID.fromString(jwt.getId());
  }

  public static UUID getSessionId(Jwt jwt) {
    String sessionId = jwt.getClaim("session-Id");
    if (StringUtils.isBlank(sessionId)) {
      throw new ApiExceptionStatusException("Invalid jwt payload", HttpStatus.UNAUTHORIZED);
    }
    try{
      return UUID.fromString(jwt.getClaim("session-Id"));
    }catch (IllegalArgumentException e){
      throw new ApiExceptionStatusException("Invalid jwt payload",HttpStatus.UNAUTHORIZED);
    }
  }

  public static UUID getUserId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }

  public static String getTokenType(Jwt jwt) {
    return jwt.getClaimAsString("type");
  }
  public record JwtSessionContext(
          String sessionId,
          String tokenId,
          String userId,
          JwtTokenType type,
          String roleId
  ) {
    public JwtSessionContext(UserAuth auth, UserSession session, JwtTokenType type) {
      this(
              (session != null && session.getId() != null) ? session.getId().toString() : null,
              (session != null && session.getJwtTokenId() != null) ? session.getJwtTokenId().toString() : null,
              (auth != null && auth.getId() != null) ? auth.getId().toString() : null,
              type,
              (auth != null && auth.getRole() != null) ? auth.getRole().getId().toString() : null
      );
    }
  }
}
