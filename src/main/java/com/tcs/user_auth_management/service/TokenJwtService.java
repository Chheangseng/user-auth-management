package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.DtoJwtTokenResponse;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.service.user.UserSessionService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
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
  public final long refreshTokenExpireInSeconds = 900;

  public DtoJwtTokenResponse generateTokenJwt(Authentication authentication) {
    var user = UserSecurity.userSecurityInfo(authentication);
    Instant now = Instant.now();
    UUID sessionId =
        userSessionService.generateSession(
            user.getUserId(), now.plusSeconds(refreshTokenExpireInSeconds));
    return new DtoJwtTokenResponse(
        accessToken(authentication, sessionId, now),
        expireInSeconds,
        this.refreshToken(authentication, sessionId, now),
        refreshTokenExpireInSeconds);
  }

  public DtoJwtTokenResponse generateTokenJwt(Authentication authentication, Jwt jwt) {
    Instant now = Instant.now();
    UUID sessionId = UUID.fromString(jwt.getId());
    userSessionService.updateSessionExpiry(sessionId, now.plusSeconds(refreshTokenExpireInSeconds));
    return new DtoJwtTokenResponse(
        accessToken(authentication, sessionId, now),
        expireInSeconds,
        this.refreshToken(authentication, sessionId, now),
        refreshTokenExpireInSeconds);
  }

  private String accessToken(Authentication authentication, UUID sessionId, Instant now) {
    return generateToken(authentication, sessionId, now, expireInSeconds, JwtTokenType.ACCESS);
  }

  private String refreshToken(Authentication authentication, UUID sessionId, Instant now) {
    return generateToken(
        authentication, sessionId, now, refreshTokenExpireInSeconds, JwtTokenType.REFRESH);
  }

  private String generateToken(
      Authentication authentication,
      UUID sessionId,
      Instant now,
      long duration,
      JwtTokenType type) {
    UserSecurity userSecurity = UserSecurity.userSecurityInfo(authentication);

    JwtClaimsSet.Builder claimsBuilder =
        JwtClaimsSet.builder()
            .id(sessionId.toString())
            .issuer("authentication-server")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(duration))
            .subject(userSecurity.getUserId().toString())
            .claim("type", type.getType());

    // Add specific claims only for Access Tokens
    if (type == JwtTokenType.ACCESS) {
      claimsBuilder
          .claim("scope", this.getScope(authentication))
          .claim("roles", this.getRoles(authentication));
    }

    return encoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).getTokenValue();
  }

  public String resetToken(Authentication authentication) {
    Instant now = Instant.now();
    //  5 min
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
    //  5 min
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

  public List<String> getRoles(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .filter(grantedAuthority -> grantedAuthority.getAuthority().startsWith("ROLE_"))
        .map(auth -> auth.getAuthority().substring(5))
        .collect(Collectors.toList());
  }

  public List<String> getScope(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .filter(grantedAuthority -> grantedAuthority.getAuthority().startsWith("SCOPE_"))
        .map(auth -> auth.getAuthority().substring(6))
        .collect(Collectors.toList());
  }

  public Jwt verifyResetPasswordToken(String resetToken) {
    var decode = verifyToken(resetToken);
    var type = validateType(decode);
    if (type != JwtTokenType.RESET_PASSWORD) {
      throw new ApiExceptionStatusException("Incorrect Token Type or format", 401);
    }
    return decode;
  }

  public Jwt verifyEmailToken(String token) {
    var decode = verifyToken(token);
    var type = validateType(decode);
    if (type != JwtTokenType.VERIFY_EMAIL) {
      throw new ApiExceptionStatusException("Incorrect Token Type or format", 401);
    }
    return decode;
  }

  public Jwt verifyToken(String token) {
    try {
      return decoder.decode(token);
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
      // Token invalid or expired
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
}
