package com.tcs.user_auth_management.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ErrorCode {
  INTERNAL_SERVER_ERROR("GLOBAL_001", HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred"),
  BAD_REQUEST("GLOBAL_002", HttpStatus.BAD_REQUEST, "Bad request"),
  UNAUTHORIZED("GLOBAL_003", HttpStatus.UNAUTHORIZED, "Unauthorized"),
  FORBIDDEN("GLOBAL_004", HttpStatus.FORBIDDEN, "Access denied"),
  VALIDATION_ERROR("GLOBAL_005", HttpStatus.BAD_REQUEST, "Validation failed for one or more fields"),
  RESOURCE_NOT_FOUND("GLOBAL_006", HttpStatus.NOT_FOUND, "Resource not found"),
  METHOD_NOT_ALLOWED("GLOBAL_007", HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");

  private final String code;
  private final HttpStatus status;
  private final String message;

  CommonErrorCode(String code, HttpStatus status, String message) {
    this.code = code;
    this.status = status;
    this.message = message;
  }
}
