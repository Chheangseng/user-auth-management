package com.tcs.user_auth_management.config.security.jwt;

import com.tcs.user_auth_management.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthenticationJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  private final AuthService authService;

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    var userSecurity = authService.authenticationCheck(source);
    return new JwtAuthenticationToken(source, userSecurity.getAuthorities(), source.getSubject()) {
      @Override
      public Object getPrincipal() {
        return userSecurity;
      }
    };
  }
}
