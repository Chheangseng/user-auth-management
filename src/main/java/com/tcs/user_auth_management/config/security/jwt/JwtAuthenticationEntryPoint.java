package com.tcs.user_auth_management.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.user_auth_management.exception.dto.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
    baseMassageFormat(response, authException.getMessage(),HttpStatus.UNAUTHORIZED);
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    baseMassageFormat(response, accessDeniedException.getMessage(),HttpStatus.FORBIDDEN);
  }

  public void baseMassageFormat(HttpServletResponse response, String message, HttpStatus status)
          throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(status.value()); // Use the parameter, not hardcoded

    ApiException apiException = new ApiException(
            message,
            status.value(),
            status,
            ZonedDateTime.now(ZoneOffset.UTC)
    );

    String json = mapper.writeValueAsString(apiException);
    PrintWriter writer = response.getWriter();
    writer.write(json);
    writer.flush();
  }
}
