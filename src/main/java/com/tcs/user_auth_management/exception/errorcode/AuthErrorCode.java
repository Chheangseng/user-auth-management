package com.tcs.user_auth_management.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
  INVALID_CREDENTIALS("AUTH_001", HttpStatus.UNAUTHORIZED, "Invalid username or password"),
  INCORRECT_OLD_PASSWORD("AUTH_002", HttpStatus.UNAUTHORIZED, "Incorrect old password"),
  INVALID_TOKEN("AUTH_003", HttpStatus.UNAUTHORIZED, "Invalid token"),
  TOKEN_EXPIRED("AUTH_004", HttpStatus.UNAUTHORIZED, "Token has expired"),
  TOKEN_REVOKED("AUTH_005", HttpStatus.UNAUTHORIZED, "Token has been revoked"),
  INVALID_TOKEN_TYPE("AUTH_006", HttpStatus.UNAUTHORIZED, "Invalid token type"),
  INVALID_TOKEN_PAYLOAD("AUTH_007", HttpStatus.UNAUTHORIZED, "Invalid token payload format"),
  SESSION_EXPIRED("AUTH_008", HttpStatus.UNAUTHORIZED, "Session has expired"),
  SESSION_REVOKED("AUTH_009", HttpStatus.UNAUTHORIZED, "Session has been revoked"),
  UNAUTHORIZED_SESSION_ACCESS("AUTH_010", HttpStatus.UNAUTHORIZED, "Cannot access or invoke session belonging to another user"),
  INVALID_ONE_TIME_TOKEN("AUTH_011", HttpStatus.UNAUTHORIZED, "Invalid or expired one-time token");

  private final String code;
  private final HttpStatus status;
  private final String message;

  AuthErrorCode(String code, HttpStatus status, String message) {
    this.code = code;
    this.status = status;
    this.message = message;
  }
}
