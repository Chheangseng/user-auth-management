package com.tcs.user_auth_management.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OidcErrorCode implements ErrorCode {
  INVALID_CLIENT("OIDC_001", HttpStatus.UNAUTHORIZED, "Invalid client credentials"),
  CLIENT_DISABLED("OIDC_002", HttpStatus.UNAUTHORIZED, "Client application is disabled"),
  CLIENT_NOT_FOUND("OIDC_003", HttpStatus.NOT_FOUND, "Client application not found"),
  INVALID_REDIRECT_URI("OIDC_004", HttpStatus.BAD_REQUEST, "Invalid or missing redirect URI"),
  UNSUPPORTED_RESPONSE_TYPE("OIDC_005", HttpStatus.BAD_REQUEST, "Unsupported response_type"),
  UNSUPPORTED_GRANT_TYPE("OIDC_006", HttpStatus.BAD_REQUEST, "Unsupported grant_type"),
  INVALID_AUTHORIZATION_CODE("OIDC_007", HttpStatus.BAD_REQUEST, "Invalid authorization code"),
  CODE_ALREADY_USED("OIDC_008", HttpStatus.BAD_REQUEST, "Authorization code has already been used"),
  CODE_EXPIRED("OIDC_009", HttpStatus.BAD_REQUEST, "Authorization code has expired"),
  CODE_CLIENT_MISMATCH("OIDC_010", HttpStatus.BAD_REQUEST, "Authorization code does not belong to this client"),
  PKCE_VERIFICATION_FAILED("OIDC_011", HttpStatus.BAD_REQUEST, "Invalid PKCE code_verifier"),
  PKCE_REQUIRED("OIDC_012", HttpStatus.BAD_REQUEST, "code_challenge is required for this client (PKCE)"),
  INVALID_SCOPE("OIDC_013", HttpStatus.BAD_REQUEST, "No valid or authorized scopes requested"),
  PUBLIC_CLIENT_RESTRICTION("OIDC_014", HttpStatus.BAD_REQUEST, "Public clients cannot use this grant type");

  private final String code;
  private final HttpStatus status;
  private final String message;

  OidcErrorCode(String code, HttpStatus status, String message) {
    this.code = code;
    this.status = status;
    this.message = message;
  }
}
