package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import java.util.Objects;

import com.tcs.user_auth_management.exception.jwt.JwtInvalidPayloadException;
import com.tcs.user_auth_management.exception.jwt.JwtInvalidTokenTypeException;
import com.tcs.user_auth_management.model.dto.JwtPayload;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
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
    } catch (JwtException e) {
      throw new ApiExceptionStatusException(e.getMessage(), 400, e);
    }catch (Exception e){
      throw new ApiExceptionStatusException("Invalid Jwt token",HttpStatus.UNAUTHORIZED);
    }
  }

  private JwtTokenType validateType(Jwt jwt) {
    if (Objects.isNull(jwt))
      throw new JwtInvalidPayloadException();
    return JwtTokenType.fromType(JwtPayload.jwtType(jwt))
        .orElseThrow(JwtInvalidPayloadException::new);
  }
}
