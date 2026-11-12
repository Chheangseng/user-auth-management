package com.tcs.user_auth_management.model.entity.oidc;

import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
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
    name = "user_consents",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_auth_id", "client_id"}))
public class UserConsent extends BaseEntityUUID {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_auth_id", nullable = false)
  private UserAuth userAuth;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private OidcClient client;

  // Space-separated approved scopes
  @Column(nullable = false)
  private String approvedScopes;

  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant consentedAt;

  public Set<String> getApprovedScopeSet() {
    if (approvedScopes == null || approvedScopes.isBlank()) {
      return Collections.emptySet();
    }
    return Arrays.stream(approvedScopes.split(" "))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());
  }
}
