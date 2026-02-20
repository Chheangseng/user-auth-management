package com.tcs.user_management.exception;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.tcs.user_management.exception.dto.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
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
}
