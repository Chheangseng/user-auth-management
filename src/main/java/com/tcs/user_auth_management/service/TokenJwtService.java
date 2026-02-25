package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.DtoJwtPayload;
import com.tcs.user_auth_management.model.dto.DtoJwtTokenResponse;
import com.tcs.user_auth_management.model.entity.user.UserSession;
import com.tcs.user_auth_management.model.entity.user.UserAuth;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import com.tcs.user_auth_management.service.user.UserSessionService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TokenJwtService {
  private final String issuer = "authentication-server";
  private final JwtEncoder encoder;
  private final JwtDecoder decoder;
  private final UserSessionService userSessionService;
  // 3 min
  private final long expireInSeconds = 120;
  private final long expireInSecondsRefresh = 60 * 30;

  public DtoJwtTokenResponse generateToken(UserAuth userAuth) {
    Instant now = Instant.now();
    UserSession session = userSessionService.createNewSession(userAuth, now);
    return buildTokenResponse(userAuth, session, now);
  }

  public DtoJwtTokenResponse generateToken(UserAuth userAuth, Jwt jwt) {
    Instant now = Instant.now();
    UserSession session =
        userSessionService.updateSessionExpiredTime(new DtoJwtPayload(jwt).getSessionId(), now);
    return buildTokenResponse(userAuth, session, now);
  }

  private DtoJwtTokenResponse buildTokenResponse(
          UserAuth userAuth, UserSession session, Instant now) {
    return new DtoJwtTokenResponse(
        accessToken(userAuth, session.getId().toString(), now),
        expireInSeconds,
        refreshToken(userAuth, session.getId().toString(), now),
        expireInSecondsRefresh);
  }

  private String refreshToken(UserAuth userAuth, String sessionId, Instant now) {
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .id(sessionId)
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expireInSecondsRefresh))
            .subject(userAuth.getId().toString())
            .claim("type", JwtTokenType.REFRESH.getType())
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  private String accessToken(UserAuth userAuth, String sessionId, Instant now) {
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .id(sessionId)
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expireInSeconds))
            .subject(userAuth.getId().toString())
            .claim("type", JwtTokenType.ACCESS.getType())
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String resetToken(Authentication authentication) {
    Instant now = Instant.now();
    long resetPasswordToken = 300;
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(resetPasswordToken))
            .subject(authentication.getName())
            .claim("type", JwtTokenType.RESET_PASSWORD.getType())
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String generateVerifyEmailToken(Authentication authentication) {
    Instant now = Instant.now();
    long resetPasswordToken = 300;
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(resetPasswordToken))
            .subject(authentication.getName())
            .claim("type", JwtTokenType.VERIFY_EMAIL.getType())
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  private JwtTokenType validateType(Jwt jwt) {
    if (Objects.isNull(jwt))
      throw new ApiExceptionStatusException("Incorrect Token Type or format", 401);
    return JwtTokenType.fromType(jwt.getClaim("type"))
        .orElseThrow(() -> new ApiExceptionStatusException("Incorrect Token Type or format", 401));
  }

  public Map<String, Object> introspect(String token) {
    try {
      Jwt jwt = verifyToken(token);
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

  public Jwt verifyRefreshToken(String refreshToken) {
    try {
      var decode = decoder.decode(refreshToken);
      var type = validateType(decode);
      if (type != JwtTokenType.REFRESH) {
        throw new ApiExceptionStatusException("Incorrect Token Type or format", 400);
      }
      return decode;
    } catch (JwtException e) {
      throw new ApiExceptionStatusException(e.getMessage(), 400, e);
    }
  }

  public DtoJwtPayload verifyTokenPayload(String token, JwtTokenType jwtTokenType) {
    try {
      var decode = decoder.decode(token);
      var type = validateType(decode);
      if (type != JwtTokenType.REFRESH) {
        throw new ApiExceptionStatusException("Incorrect Token Type or format", 400);
      }
      return new DtoJwtPayload(decode);
    } catch (JwtException e) {
      throw new ApiExceptionStatusException(e.getMessage(), 400, e);
    }
  }

  public DtoJwtPayload verifyTokenPayload(String token) {
    try {
      var decode = decoder.decode(token);
      validateType(decode);
      return new DtoJwtPayload(decode);
    } catch (JwtException e) {
      throw new ApiExceptionStatusException(e.getMessage(), 400, e);
    }
  }

  public Jwt verifyToken(String token) {
    try {
      var decode = decoder.decode(token);
      validateType(decode);
      return decode;
    } catch (JwtException e) {
      throw new ApiExceptionStatusException(e.getMessage(), 400, e);
    }
  }
}
