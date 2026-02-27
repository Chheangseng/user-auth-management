package com.tcs.user_auth_management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiExceptionStatusException extends RuntimeException {
  private final int statusCode;

  public ApiExceptionStatusException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public ApiExceptionStatusException(String message, HttpStatus statusCode) {
    super(message,null);
    this.statusCode = statusCode.value();
  }

  public ApiExceptionStatusException(String message, int statusCode, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
  }
}
