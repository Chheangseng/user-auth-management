package com.tcs.user_auth_management.model.dto;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import java.util.UUID;
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

  public static JwtClaimsSet.Builder baseClaimSessionId(
      String sessionId, String tokenId, String userId, JwtTokenType type) {
    return DtoJwtClaim.baseClaim(tokenId, userId, type).claim("session-Id", sessionId);
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
}
