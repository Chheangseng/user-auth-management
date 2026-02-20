package com.tcs.user_management.config.jwt;

import io.micrometer.common.lang.NonNullApi;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@NonNullApi
@Component
public class AuthenticationJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  private static final String ROLE_PREFIX = "ROLE_";

  public Set<SimpleGrantedAuthority> authorities(Jwt jwt) {

      Set<SimpleGrantedAuthority> authorities = new HashSet<>();

    // Extract and process roles
    List<String> roles = jwt.getClaimAsStringList("roles");
    if (roles != null && !roles.isEmpty()) {
      authorities.addAll(
          roles.stream()
              .filter(role -> role != null && !role.trim().isEmpty())
              .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.trim().toUpperCase()))
              .collect(Collectors.toSet()));
    }
    return authorities;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    return new JwtAuthenticationToken(source, this.authorities(source), source.getSubject());
  }
}
