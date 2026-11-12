package com.tcs.user_auth_management.config.oidc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oidc")
public record OidcProperties(
    String issuer,
    String frontendUrl
) {
  public OidcProperties {
    if (issuer == null || issuer.isBlank()) {
      issuer = "http://localhost:8090";
    }
    if (frontendUrl == null || frontendUrl.isBlank()) {
      frontendUrl = "http://localhost:3000";
    }
  }
}
