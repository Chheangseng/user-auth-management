package com.tcs.user_auth_management.model.entity.user;

import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public record UserSecurity(UserAuth userAccount) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public String getPassword() {
    return this.userAccount.getPassword();
  }

  @Override
  public String getUsername() {
    return this.userAccount.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return this.userAccount.isActivate();
  }

  @Override
  public boolean isAccountNonLocked() {
    return this.userAccount.isActivate();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return this.userAccount.isActivate();
  }

  @Override
  public boolean isEnabled() {
    return this.userAccount.isActivate();
  }

  public UUID getUserId() {
    return this.userAccount.getId();
  }

  public static Optional<UserSecurity> getUserSecurity() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserSecurity) {
      return Optional.of((UserSecurity) authentication.getPrincipal());
    }
    return Optional.empty();
  }

  public static Optional<UserSecurity> getUserSecurity(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserSecurity) {
      return Optional.of((UserSecurity) authentication.getPrincipal());
    }
    return Optional.empty();
  }

  public static UserSecurity userSecurityInfo() {
    return UserSecurity.getUserSecurity()
        .orElseThrow(
            () ->
                new ApiExceptionStatusException(
                    "Fail to get user authentication context information",
                    HttpStatus.INTERNAL_SERVER_ERROR));
  }

  public static UserSecurity userSecurityInfo(Authentication authentication) {
    return UserSecurity.getUserSecurity(authentication)
        .orElseThrow(
            () ->
                new ApiExceptionStatusException(
                    "Fail to get user authentication context information",
                    HttpStatus.INTERNAL_SERVER_ERROR));
  }

  public static Authentication getAuthenticationByUserAuth(UserAuth userAuth) {
    var securityUser = new UserSecurity(userAuth);
    return new UsernamePasswordAuthenticationToken(
        securityUser, userAuth.getPassword(), securityUser.getAuthorities());
  }
}
