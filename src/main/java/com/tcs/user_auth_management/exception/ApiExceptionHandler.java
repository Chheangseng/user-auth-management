package com.tcs.user_auth_management.exception;

import com.tcs.user_auth_management.exception.dto.ApiException;
import java.time.ZoneOffset;
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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleAllUncaughtExceptions(Exception ex) {
    return switch (ex) {
      case MethodArgumentNotValidException methodArgumentNotValidException ->
          buildErrorResponse(
              methodArgumentNotValidException
                  .getBindingResult()
                  .getAllErrors()
                  .getFirst()
                  .getDefaultMessage(),
              HttpStatus.BAD_REQUEST);

      case ApiExceptionStatusException apiEx ->
          buildErrorResponse(apiEx.getMessage(), HttpStatus.valueOf(apiEx.getStatusCode()));

      default -> {
        log.error("CRITICAL UNEXPECTED ERROR: ", ex);
        yield buildErrorResponse(
            "An internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    };
  }

  private ResponseEntity<Object> buildErrorResponse(String message, HttpStatus httpStatus) {
    return new ResponseEntity<>(
        new ApiException(
            message, httpStatus.value(), httpStatus, ZonedDateTime.now(ZoneOffset.UTC)),
        httpStatus);
  }
}
