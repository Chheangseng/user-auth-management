package com.tcs.user_auth_management.config.oidc;

import com.tcs.user_auth_management.repository.AuthorizationCodeRepository;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class OidcCleanupScheduler {
  private final AuthorizationCodeRepository authorizationCodeRepository;

  @Scheduled(cron = "0 0 * * * *") // Every hour
  @Transactional
  public void cleanupExpiredCodes() {
    try {
      log.info("Running scheduled cleanup of expired authorization codes at UTC: {}", Instant.now());
      authorizationCodeRepository.deleteByExpiresAtBefore(Instant.now());
    } catch (Exception e) {
      log.error("Unexpected error during scheduled cleanup of expired authorization codes: {}", e.getMessage(), e);
    }
  }
}
