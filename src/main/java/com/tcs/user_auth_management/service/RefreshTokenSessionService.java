package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.entity.UserSession;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import com.tcs.user_auth_management.repository.UserSessionRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RefreshTokenSessionService {
  private final UserSessionRepository repository;
  private final UserAuthRepository userAuthRepository;
  private final JwtEncoder encoder;
  private final JwtDecoder decoder;
  //  15 min
  public static final long refreshTokenExpireInSeconds = 900;

  public String generateRefreshTokenSession(Authentication authentication, Instant now) {
    return generateRefreshTokenBySessionId(generateCompactId(now), authentication, now);
  }

  public void invokeToken(String token) {
    Jwt jwt = verifyRefreshToken(token);
    String sessionId = jwt.getId();
    if (Objects.isNull(sessionId)) return;
    repository.invokedSessionById(sessionId);
  }

  public void invokeAllToken(String token) {
    Jwt jwt = verifyRefreshToken(token);
    String userId = jwt.getSubject();
    if (Objects.isNull(userId)) return;
    repository.invokedSessionAllByUserId(jwt.getSubject());
  }

  public String generateRefreshTokenBySessionId(
      String sessionId, Authentication authentication, Instant now) {
    Instant expiredTime = now.plusSeconds(refreshTokenExpireInSeconds);
    String token = generateRefreshToken(authentication, sessionId, now, expiredTime);
    createRefreshTokenSession(authentication, sessionId, expiredTime);
    return token;
  }

  public String refreshTokenWithOldSession(Authentication authentication, Jwt jwt, Instant now) {
    return refreshTokenWithJwt(authentication, jwt, now);
  }

  private String refreshTokenWithJwt(Authentication authentication, Jwt jwt, Instant now) {
    String sessionId = jwt.getClaim("sessionId");

    if (sessionId == null) {
      return generateRefreshTokenSession(authentication, now);
    }

    UserSession sessionRefresh = repository.findById(sessionId).orElseGet(() -> null);

    if (sessionRefresh == null) {
      return generateRefreshTokenBySessionId(sessionId, authentication, now);
    }

    if (sessionRefresh.isInvoked()) {
      throw new ApiExceptionStatusException("Session is already invoked", 401);
    }
    Instant newExpiry = now.plusSeconds(refreshTokenExpireInSeconds);
    sessionRefresh.setExpiryDate(newExpiry);
    repository.save(sessionRefresh);

    return generateRefreshToken(authentication, sessionId, now, newExpiry);
  }

  private void createRefreshTokenSession(
      Authentication authentication, String sessionId, Instant expiredTime) {
    UserSession entity = new UserSession();
    entity.setExpiryDate(expiredTime);
    UserSecurity userSecurity = UserSecurity.userSecurityByAuthentication(authentication);
    entity.setUserAuthId(userSecurity.userAccount().getId());
    repository.save(entity);
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

  private JwtTokenType validateType(Jwt jwt) {
    if (Objects.isNull(jwt))
      throw new ApiExceptionStatusException("Incorrect Token Type or format", 401);
    return JwtTokenType.fromType(jwt.getClaim("type"))
        .orElseThrow(() -> new ApiExceptionStatusException("Incorrect Token Type or format", 401));
  }

  private String generateRefreshToken(
      Authentication authentication, String sessionId, Instant now, Instant expiredTime) {
    UserSecurity userSecurity = UserSecurity.userSecurityByAuthentication(authentication);
    String issuer = "authentication-server";
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .id(sessionId)
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(expiredTime)
            .subject(userSecurity.getUserId().toString())
            .claim("type", JwtTokenType.REFRESH.getType())
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public static String generateCompactId(Instant now) {
    long timestamp = now.toEpochMilli();
    String random = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    return timestamp + "-" + random;
  }
}
