package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.model.dto.DtoJwtClaim;
import com.tcs.user_auth_management.model.dto.DtoJwtTokenResponse;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSession;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TokenJwtService {
  private final JwtEncoder encoder;
  private final TokenJwtVerifyService jwtVerifyService;
  private final long expireInSeconds = 120;
  @Getter private final long expireInSecondsRefresh = 60 * 30;

  public DtoJwtTokenResponse generateToken(UserAuth userAuth, UserSession session) {
    Instant now = Instant.now();
    return buildTokenResponse(userAuth, session, now);
  }

  private DtoJwtTokenResponse buildTokenResponse(
      UserAuth userAuth, UserSession session, Instant now) {
    return new DtoJwtTokenResponse(
        accessToken(userAuth, session, now),
        expireInSeconds,
        refreshToken(userAuth, session, now),
        expireInSecondsRefresh);
  }

  private String refreshToken(UserAuth userAuth, UserSession session, Instant now) {
    JwtClaimsSet claims =
        DtoJwtClaim.baseClaim(
                session.getJwtTokenId().toString(),
                userAuth.getId().toString(),
                JwtTokenType.REFRESH)
            .issuedAt(now)
            .expiresAt(session.getExpiryDate())
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  private String accessToken(UserAuth userAuth, UserSession session, Instant now) {
    JwtClaimsSet claims =
        DtoJwtClaim.baseClaim(
                session.getJwtTokenId().toString(),
                userAuth.getId().toString(),
                JwtTokenType.REFRESH)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expireInSeconds))
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public Map<String, Object> introspect(String token) {
    try {
      Jwt jwt = jwtVerifyService.verifyToken(token);
      return Map.of(
          "active",
          true,
          "iss",
          jwt.getIssuer(),
          "sub",
          jwt.getSubject(),
          "exp",
          jwt.getExpiresAt() != null ? jwt.getExpiresAt().getEpochSecond() : 0,
          "iat",
          jwt.getIssuedAt() != null ? jwt.getIssuedAt().getEpochSecond() : 0,
          "scope",
          jwt.getClaimAsString("scope"));
    } catch (JwtException e) {
      return Map.of("active", false);
    }
  }
}
