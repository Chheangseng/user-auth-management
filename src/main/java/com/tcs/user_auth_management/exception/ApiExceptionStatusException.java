package com.tcs.user_auth_management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiExceptionStatusException extends RuntimeException {
  private final int statusCode;

  public ApiExceptionStatusException(int statusCode) {
    this.statusCode = statusCode;
  }

  public ApiExceptionStatusException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }
  public ApiExceptionStatusException(String message, HttpStatus statusCode) {
    super(message);
    this.statusCode = statusCode.value();
  }
  public ApiExceptionStatusException(String message,int statusCode, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  public ApiExceptionStatusException(Throwable cause, int statusCode) {
    super(cause);
    this.statusCode = statusCode;
  }

  public ApiExceptionStatusException(
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace,
      int statusCode) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.statusCode = statusCode;
  }
}
