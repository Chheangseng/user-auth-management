package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.exception.AppException;
import com.tcs.user_auth_management.exception.errorcode.AuthErrorCode;
import com.tcs.user_auth_management.exception.jwt.JwtInvalidPayloadException;
import com.tcs.user_auth_management.exception.jwt.JwtInvalidTokenTypeException;
import com.tcs.user_auth_management.model.dto.JwtPayload;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TokenJwtVerifyService {
  private final JwtDecoder decoder;

  public Jwt verifyToken(String token) {
    return this.verifyToken(token, null);
  }

  public Jwt verifyToken(String token, JwtTokenType type) {
    try {
      var decode = decoder.decode(token);
      var typeJwt = validateType(decode);
      if (Objects.nonNull(type) && !typeJwt.equals(type)) {
        throw new JwtInvalidTokenTypeException();
      }
      return decode;
    } catch (JwtInvalidTokenTypeException | JwtInvalidPayloadException e) {
      throw e;
    } catch (JwtException e) {
      throw new AppException(AuthErrorCode.INVALID_TOKEN, e.getMessage(), e);
    } catch (Exception e) {
      log.error("Unexpected error during token verification: {}", e.getMessage(), e);
      throw new AppException(AuthErrorCode.INVALID_TOKEN, "Invalid Jwt token", e);
    }
  }

  private JwtTokenType validateType(Jwt jwt) {
    if (Objects.isNull(jwt))
      throw new JwtInvalidPayloadException();
    return JwtTokenType.fromType(JwtPayload.jwtType(jwt))
        .orElseThrow(JwtInvalidPayloadException::new);
  }
}
