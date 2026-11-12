package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.oidc.OidcClient;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OidcClientRepository extends JpaRepository<OidcClient, UUID> {
  Optional<OidcClient> findByClientId(String clientId);
  boolean existsByClientId(String clientId);
}
