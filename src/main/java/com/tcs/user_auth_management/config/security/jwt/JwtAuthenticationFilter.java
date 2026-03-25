package com.tcs.user_auth_management.config.security.jwt;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.service.AuthService;
import com.tcs.user_auth_management.service.TokenJwtVerifyService;
import com.tcs.user_auth_management.service.user.UserSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final UserSessionService service;
  private final AuthService authService;
  private final TokenJwtVerifyService verifyService;
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userEmail;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    jwt = authHeader.substring(7);
    var claim = verifyService.verifyToken(jwt, JwtTokenType.ACCESS_TOKEN);

  }
}
