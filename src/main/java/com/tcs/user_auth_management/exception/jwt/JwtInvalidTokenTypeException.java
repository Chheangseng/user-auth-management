package com.tcs.user_auth_management.exception.jwt;

import org.springframework.security.core.AuthenticationException;

public class JwtInvalidTokenTypeException extends AuthenticationException {
  public JwtInvalidTokenTypeException() {
    super("Invalid Jwt token type");
  }
}
