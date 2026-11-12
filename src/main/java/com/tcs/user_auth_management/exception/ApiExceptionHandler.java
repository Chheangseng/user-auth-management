package com.tcs.user_auth_management.exception;

import com.tcs.user_auth_management.exception.dto.ErrorResponse;
import com.tcs.user_auth_management.exception.errorcode.AuthErrorCode;
import com.tcs.user_auth_management.exception.errorcode.CommonErrorCode;
import com.tcs.user_auth_management.exception.errorcode.UserErrorCode;
import com.tcs.user_auth_management.util.TraceIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    ErrorResponse response = ErrorResponse.of(
        ex.getCode(),
        ex.getMessage(),
        ex.getStatus(),
        request.getRequestURI(),
        traceId,
        ex.getDetails()
    );
    return new ResponseEntity<>(response, ex.getStatus());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    List<ErrorResponse.FieldErrorDetail> details = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> new ErrorResponse.FieldErrorDetail(error.getField(), error.getDefaultMessage()))
        .toList();

    ErrorResponse response = ErrorResponse.of(
        CommonErrorCode.VALIDATION_ERROR.getCode(),
        CommonErrorCode.VALIDATION_ERROR.getMessage(),
        CommonErrorCode.VALIDATION_ERROR.getStatus(),
        request.getRequestURI(),
        traceId,
        details
    );
    return new ResponseEntity<>(response, CommonErrorCode.VALIDATION_ERROR.getStatus());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(
      BadCredentialsException ex, HttpServletRequest request) {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    ErrorResponse response = ErrorResponse.of(
        AuthErrorCode.INVALID_CREDENTIALS.getCode(),
        ex.getMessage() != null ? ex.getMessage() : AuthErrorCode.INVALID_CREDENTIALS.getMessage(),
        AuthErrorCode.INVALID_CREDENTIALS.getStatus(),
        request.getRequestURI(),
        traceId
    );
    return new ResponseEntity<>(response, AuthErrorCode.INVALID_CREDENTIALS.getStatus());
  }

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ErrorResponse> handleDisabledException(
      DisabledException ex, HttpServletRequest request) {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    ErrorResponse response = ErrorResponse.of(
        UserErrorCode.ACCOUNT_LOCKED.getCode(),
        UserErrorCode.ACCOUNT_LOCKED.getMessage(),
        UserErrorCode.ACCOUNT_LOCKED.getStatus(),
        request.getRequestURI(),
        traceId
    );
    return new ResponseEntity<>(response, UserErrorCode.ACCOUNT_LOCKED.getStatus());
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFound(
      UsernameNotFoundException ex, HttpServletRequest request) {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    ErrorResponse response = ErrorResponse.of(
        UserErrorCode.USER_NOT_FOUND.getCode(),
        ex.getMessage() != null ? ex.getMessage() : UserErrorCode.USER_NOT_FOUND.getMessage(),
        UserErrorCode.USER_NOT_FOUND.getStatus(),
        request.getRequestURI(),
        traceId
    );
    return new ResponseEntity<>(response, UserErrorCode.USER_NOT_FOUND.getStatus());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    ErrorResponse response = ErrorResponse.of(
        CommonErrorCode.FORBIDDEN.getCode(),
        CommonErrorCode.FORBIDDEN.getMessage(),
        CommonErrorCode.FORBIDDEN.getStatus(),
        request.getRequestURI(),
        traceId
    );
    return new ResponseEntity<>(response, CommonErrorCode.FORBIDDEN.getStatus());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    ErrorResponse response = ErrorResponse.of(
        CommonErrorCode.METHOD_NOT_ALLOWED.getCode(),
        ex.getMessage(),
        CommonErrorCode.METHOD_NOT_ALLOWED.getStatus(),
        request.getRequestURI(),
        traceId
    );
    return new ResponseEntity<>(response, CommonErrorCode.METHOD_NOT_ALLOWED.getStatus());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      NoResourceFoundException ex, HttpServletRequest request) {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    ErrorResponse response = ErrorResponse.of(
        CommonErrorCode.RESOURCE_NOT_FOUND.getCode(),
        ex.getMessage(),
        CommonErrorCode.RESOURCE_NOT_FOUND.getStatus(),
        request.getRequestURI(),
        traceId
    );
    return new ResponseEntity<>(response, CommonErrorCode.RESOURCE_NOT_FOUND.getStatus());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(
      Exception ex, HttpServletRequest request) {
    log.error("CRITICAL UNEXPECTED ERROR: ", ex);
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    ErrorResponse response = ErrorResponse.of(
        CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
        CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
        CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
        request.getRequestURI(),
        traceId
    );
    return new ResponseEntity<>(response, CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus());
  }
}
