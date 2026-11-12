package com.tcs.user_auth_management.exception.errorcode;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
  String getCode();
  HttpStatus getStatus();
  String getMessage();
}
