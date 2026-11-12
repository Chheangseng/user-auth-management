package com.tcs.user_auth_management.service.oidc;

import com.tcs.user_auth_management.model.entity.oidc.OidcClient;
import com.tcs.user_auth_management.model.entity.oidc.UserConsent;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.repository.UserConsentRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserConsentService {
  private final UserConsentRepository consentRepository;

  public boolean hasConsent(UUID userAuthId, UUID clientId, Set<String> requestedScopes) {
    Optional<UserConsent> consentOpt = consentRepository.findByUserAuthIdAndClientId(userAuthId, clientId);
    if (consentOpt.isEmpty()) {
      return false;
    }
    Set<String> approved = consentOpt.get().getApprovedScopeSet();
    return approved.containsAll(requestedScopes);
  }

  @Transactional
  public UserConsent grantConsent(UserAuth userAuth, OidcClient client, Set<String> approvedScopes) {
    Optional<UserConsent> existingOpt = consentRepository.findByUserAuthIdAndClientId(userAuth.getId(), client.getId());
    UserConsent consent = existingOpt.orElseGet(() -> {
      UserConsent newConsent = new UserConsent();
      newConsent.setUserAuth(userAuth);
      newConsent.setClient(client);
      return newConsent;
    });

    consent.setApprovedScopes(String.join(" ", approvedScopes));
    consent.setConsentedAt(Instant.now());
    return consentRepository.save(consent);
  }

  @Transactional
  public void revokeConsent(UUID userAuthId, UUID clientId) {
    consentRepository.findByUserAuthIdAndClientId(userAuthId, clientId)
        .ifPresent(consentRepository::delete);
  }
}
