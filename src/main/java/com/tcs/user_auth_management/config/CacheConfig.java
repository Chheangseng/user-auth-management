package com.tcs.user_auth_management.config;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class CacheConfig {
  private final JdbcTemplate jdbcTemplate;

  @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
  @Transactional(rollbackFor = Exception.class)
  public void removeExpiredCache() {
    try {
      String sql = "DELETE FROM cache_store WHERE expires_at is not null and expires_at < NOW()";
      int deletedCount = jdbcTemplate.update(sql);
      if (deletedCount > 0) {
        log.info("Removed {} expired cache entries", deletedCount);
      }
    } catch (DataAccessException e) {
      log.error("Failed to remove expired cache entries", e);
      throw e;
    }
  }
}
