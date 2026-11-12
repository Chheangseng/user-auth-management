package com.tcs.user_auth_management.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    HttpStatus status,
    String path,
    Instant timestamp,
    String traceId,
    List<FieldErrorDetail> details
) {
  public record FieldErrorDetail(String field, String message) {}

  public static ErrorResponse of(
      String code,
      String message,
      HttpStatus status,
      String path,
      String traceId) {
    return new ErrorResponse(code, message, status, path, Instant.now(), traceId, null);
  }

  public static ErrorResponse of(
      String code,
      String message,
      HttpStatus status,
      String path,
      String traceId,
      List<FieldErrorDetail> details) {
    return new ErrorResponse(code, message, status, path, Instant.now(), traceId, details);
  }
}
