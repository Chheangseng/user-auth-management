package com.tcs.user_auth_management.emuns;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum OidcScope {
  OPENID("openid"),
  PROFILE("profile"),
  EMAIL("email"),
  ADDRESS("address"),
  PHONE("phone"),
  OFFLINE_ACCESS("offline_access");

  private final String value;

  OidcScope(String value) {
    this.value = value;
  }

  public static Optional<OidcScope> fromValue(String value) {
    return Arrays.stream(values())
        .filter(scope -> scope.value.equalsIgnoreCase(value))
        .findFirst();
  }
}
