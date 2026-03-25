package com.tcs.user_auth_management.model.entity.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.entity.user.authorization.Role;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class UserSecurity implements UserDetails {
  private final UUID userId;
  private final String username;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private final String password;

  private final boolean enabled;
  private final Set<GrantedAuthority> authorities;
  private final UUID jwtId;
  private final UUID sessionId;

  public UserSecurity(UserAuth userAuth, UUID jwtId, UUID sessionId) {
    this.userId = userAuth.getId();
    this.username = userAuth.getUsername();
    this.password = userAuth.getPassword();
    this.enabled = userAuth.isEnabled();
    this.jwtId = jwtId;
    this.sessionId = sessionId;
    Set<GrantedAuthority> authorities = new HashSet<>();
    Role role = userAuth.getRole();
    if (Objects.isNull(role)) {
      this.authorities = authorities;
      return;
    }
    // Add role authority
    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

    // Add permission authorities
    if (role.getPermissions() != null) {
      authorities.addAll(
          role.getPermissions().stream()
              .map(permission -> new SimpleGrantedAuthority(permission.getPermissionCode()))
              .collect(Collectors.toSet()));
    }
    this.authorities = authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return enabled;
  }

  @Override
  public boolean isAccountNonLocked() {
    return enabled;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return enabled;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  // Static helper methods
  public static Optional<UserSecurity> getCurrentUser() {
    return extractUserFromAuthentication(SecurityContextHolder.getContext().getAuthentication());
  }

  public static UserSecurity getRequiredCurrentUser() {
    var user = getCurrentUser();
    if (user.isEmpty()) {
      throw new ApiExceptionStatusException("Invalid user", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return user.get();
  }

  public static Optional<UserSecurity> extractUserFromAuthentication(
      Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserSecurity) {
      return Optional.of((UserSecurity) authentication.getPrincipal());
    }
    return Optional.empty();
  }
}
