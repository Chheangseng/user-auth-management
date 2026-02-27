package com.tcs.user_auth_management.exception;

import com.tcs.user_auth_management.exception.dto.ApiException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {
  @ExceptionHandler(value = {ApiExceptionStatusException.class})
  public ResponseEntity<Object> handleTechnicalException(ApiExceptionStatusException e) {
    HttpStatus httpStatus = HttpStatus.valueOf(e.getStatusCode());
    var apiException =
        new ApiException(
            e.getMessage(), e.getStatusCode(), httpStatus, ZonedDateTime.now(ZoneId.of("Z")));
    return new ResponseEntity<>(apiException, httpStatus);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    var apiException =
        new ApiException(
            e.getBindingResult().getAllErrors().getFirst().getDefaultMessage(),
            400,
            httpStatus,
            ZonedDateTime.now(ZoneId.of("Z")));
    return new ResponseEntity<>(apiException, httpStatus);
  }
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleAllUncaughtExceptions(Exception ex) {
    // This ensures that ANY unexpected 500 error always shows a trace
    log.error("CRITICAL UNEXPECTED ERROR: ", ex);
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    var apiException =
            new ApiException(
                   "An internal server error occurred",
                    httpStatus.value(),
                    httpStatus,
                    ZonedDateTime.now(ZoneId.of("Z")));
    return new ResponseEntity<>(apiException, httpStatus);
  }
}
