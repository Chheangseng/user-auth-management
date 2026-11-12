package com.tcs.user_auth_management.model.entity.oidc;

import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "oidc_clients",
    indexes = {@Index(name = "idx_oidc_client_id", columnList = "clientId")})
public class OidcClient extends BaseEntityUUID {

  @Column(unique = true, nullable = false)
  private String clientId;

  // BCrypt-hashed client secret (null or empty for public clients)
  private String clientSecret;

  @Column(nullable = false)
  private String clientName;

  // Comma-separated redirect URIs
  @Column(columnDefinition = "TEXT", nullable = false)
  private String redirectUris;

  // Comma-separated allowed scopes (e.g. openid,profile,email)
  @Column(nullable = false)
  private String allowedScopes;

  // Comma-separated grant types (e.g. authorization_code,client_credentials,refresh_token)
  @Column(nullable = false)
  private String grantTypes;

  // Token validity durations in seconds
  private long accessTokenValiditySeconds = 3600;
  private long refreshTokenValiditySeconds = 86400;
  private long authorizationCodeValiditySeconds = 300;

  // Public client (no secret required, PKCE mandatory)
  private boolean publicClient = false;

  private boolean enabled = true;

  // PKCE requirement
  private boolean requirePkce = true;

  public Set<String> getRedirectUriSet() {
    if (redirectUris == null || redirectUris.isBlank()) {
      return Collections.emptySet();
    }
    return Arrays.stream(redirectUris.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());
  }

  public Set<String> getAllowedScopeSet() {
    if (allowedScopes == null || allowedScopes.isBlank()) {
      return Collections.emptySet();
    }
    return Arrays.stream(allowedScopes.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());
  }

  public Set<String> getGrantTypeSet() {
    if (grantTypes == null || grantTypes.isBlank()) {
      return Collections.emptySet();
    }
    return Arrays.stream(grantTypes.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());
  }
}
