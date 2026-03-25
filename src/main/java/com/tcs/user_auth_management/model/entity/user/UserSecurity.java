package com.tcs.user_auth_management.model.entity.user;

import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.entity.user.authorization.Role;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public record UserSecurity(UserAuth userAuth, UUID jwtId, UUID sessionId)
    implements UserDetails {
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();
    Role role = userAuth.getRole();
    if(Objects.isNull(role)) return authorities;
    // Add role authority
    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

    authorities.addAll(
        role.getPermissions().stream()
            .map(permission -> new SimpleGrantedAuthority(permission.getPermissionCode()))
            .collect(Collectors.toSet()));
    return authorities;
  }

  public UUID getUserId() {
    return this.userAuth.getId();
  }

  @Override
  public String getPassword() {
    return this.userAuth.getPassword();
  }

  @Override
  public String getUsername() {
    return this.userAuth.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return this.userAuth.isEnabled();
  }

  @Override
  public boolean isAccountNonLocked() {
    return this.userAuth.isEnabled();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return this.userAuth.isEnabled();
  }

  @Override
  public boolean isEnabled() {
    return this.userAuth.isEnabled();
  }

  public static Optional<UserSecurity> getCurrentUser() {
    return UserSecurity.extractUserFromAuthentication(
        SecurityContextHolder.getContext().getAuthentication());
  }

  public static UserSecurity getRequiredCurrentUser() {
    var user = UserSecurity.getCurrentUser();
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
