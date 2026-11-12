package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.oidc.UserConsent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, UUID> {
  Optional<UserConsent> findByUserAuthIdAndClientId(UUID userAuthId, UUID clientId);
}
