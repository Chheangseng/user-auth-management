package com.tcs.user_auth_management.model.dto;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import java.util.UUID;
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

  public static UUID getJwtId(Jwt jwt) {
    return UUID.fromString(jwt.getId());
  }

  public static UUID getUserId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }

  public static String getTokenType(Jwt jwt) {
    return jwt.getClaimAsString("type");
  }
}
