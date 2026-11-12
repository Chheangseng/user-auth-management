package com.tcs.user_auth_management.emuns;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum GrantType {
  AUTHORIZATION_CODE("authorization_code"),
  CLIENT_CREDENTIALS("client_credentials"),
  REFRESH_TOKEN("refresh_token");

  private final String value;

  GrantType(String value) {
    this.value = value;
  }

  public static Optional<GrantType> fromValue(String value) {
    return Arrays.stream(values())
        .filter(grant -> grant.value.equalsIgnoreCase(value))
        .findFirst();
  }
}
