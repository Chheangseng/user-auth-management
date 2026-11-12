package com.tcs.user_auth_management.model.entity.oidc;

import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "authorization_codes",
    indexes = {@Index(name = "idx_auth_code_code", columnList = "code")})
public class AuthorizationCode extends BaseEntityUUID {

  @Column(unique = true, nullable = false)
  private String code;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private OidcClient client;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_auth_id", nullable = false)
  private UserAuth userAuth;

  @Column(nullable = false)
  private String redirectUri;

  // Space-separated approved scopes
  @Column(nullable = false)
  private String scopes;

  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant expiresAt;

  private boolean used = false;

  // PKCE fields
  private String codeChallenge;
  private String codeChallengeMethod; // "S256" or "plain"

  // Nonce for ID token
  private String nonce;

  // State for CSRF protection
  private String state;
}
