package com.tcs.user_auth_management.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.user_auth_management.exception.dto.ErrorResponse;
import com.tcs.user_auth_management.exception.errorcode.AuthErrorCode;
import com.tcs.user_auth_management.exception.errorcode.CommonErrorCode;
import com.tcs.user_auth_management.util.TraceIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

  private final ObjectMapper mapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    writeErrorResponse(
        request,
        response,
        AuthErrorCode.INVALID_TOKEN.getCode(),
        authException.getMessage() != null ? authException.getMessage() : AuthErrorCode.INVALID_TOKEN.getMessage(),
        HttpStatus.UNAUTHORIZED
    );
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    writeErrorResponse(
        request,
        response,
        CommonErrorCode.FORBIDDEN.getCode(),
        accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : CommonErrorCode.FORBIDDEN.getMessage(),
        HttpStatus.FORBIDDEN
    );
  }

  private void writeErrorResponse(
      HttpServletRequest request,
      HttpServletResponse response,
      String code,
      String message,
      HttpStatus status)
      throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(status.value());

    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    ErrorResponse errorResponse = ErrorResponse.of(
        code,
        message,
        status,
        request.getRequestURI(),
        traceId
    );

    String json = mapper.writeValueAsString(errorResponse);
    PrintWriter writer = response.getWriter();
    writer.write(json);
    writer.flush();
  }
}
