package com.tcs.user_auth_management.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {
  USER_NOT_FOUND("USER_001", HttpStatus.NOT_FOUND, "User not found"),
  USER_ALREADY_EXISTS("USER_002", HttpStatus.CONFLICT, "User already exists"),
  USERNAME_ALREADY_EXISTS("USER_003", HttpStatus.BAD_REQUEST, "Username has already been used"),
  EMAIL_ALREADY_EXISTS("USER_004", HttpStatus.BAD_REQUEST, "Email has already been used"),
  INVALID_PASSWORD("USER_005", HttpStatus.BAD_REQUEST, "Invalid password"),
  ACCOUNT_LOCKED("USER_006", HttpStatus.UNAUTHORIZED, "Your account has been locked or disabled"),
  USER_CHECK_ERROR("USER_007", HttpStatus.INTERNAL_SERVER_ERROR, "Error checking user information");

  private final String code;
  private final HttpStatus status;
  private final String message;

  UserErrorCode(String code, HttpStatus status, String message) {
    this.code = code;
    this.status = status;
    this.message = message;
  }
}
