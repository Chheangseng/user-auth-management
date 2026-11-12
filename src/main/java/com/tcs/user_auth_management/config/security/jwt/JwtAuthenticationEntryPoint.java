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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

  private final ObjectMapper mapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    log.warn("Unauthorized filter rejection [traceId={}, uri={} {}]: {}",
        traceId, request.getMethod(), request.getRequestURI(), authException.getMessage());

    writeErrorResponse(
        request,
        response,
        AuthErrorCode.INVALID_TOKEN.getCode(),
        authException.getMessage() != null ? authException.getMessage() : AuthErrorCode.INVALID_TOKEN.getMessage(),
        HttpStatus.UNAUTHORIZED,
        traceId
    );
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    String traceId = TraceIdUtil.getOrCreateTraceId(request);
    log.warn("Access denied filter rejection [traceId={}, uri={} {}]: {}",
        traceId, request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());

    writeErrorResponse(
        request,
        response,
        CommonErrorCode.FORBIDDEN.getCode(),
        accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : CommonErrorCode.FORBIDDEN.getMessage(),
        HttpStatus.FORBIDDEN,
        traceId
    );
  }

  private void writeErrorResponse(
      HttpServletRequest request,
      HttpServletResponse response,
      String code,
      String message,
      HttpStatus status,
      String traceId)
      throws IOException {
    try {
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(status.value());

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
    } catch (IOException e) {
      log.error("Failed to write error response JSON [traceId={}, uri={}]: {}",
          traceId, request.getRequestURI(), e.getMessage(), e);
      throw e;
    }
  }
}
