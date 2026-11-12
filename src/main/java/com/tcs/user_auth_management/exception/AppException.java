package com.tcs.user_auth_management.exception;

import com.tcs.user_auth_management.exception.dto.ErrorResponse;
import com.tcs.user_auth_management.exception.errorcode.ErrorCode;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
  private final ErrorCode errorCode;
  private final List<ErrorResponse.FieldErrorDetail> details;

  public AppException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = null;
  }

  public AppException(ErrorCode errorCode, String customMessage) {
    super(customMessage != null ? customMessage : errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = null;
  }

  public AppException(ErrorCode errorCode, String customMessage, Throwable cause) {
    super(customMessage != null ? customMessage : errorCode.getMessage(), cause);
    this.errorCode = errorCode;
    this.details = null;
  }

  public AppException(ErrorCode errorCode, List<ErrorResponse.FieldErrorDetail> details) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = details;
  }

  public HttpStatus getStatus() {
    return errorCode.getStatus();
  }

  public String getCode() {
    return errorCode.getCode();
  }
}
