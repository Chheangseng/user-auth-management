package com.tcs.user_auth_management.exception.jwt;

import org.springframework.security.core.AuthenticationException;

public class JwtInvalidPayloadException extends AuthenticationException {
  public JwtInvalidPayloadException() {
    super("Invalid jwt payload format");
  }
}
