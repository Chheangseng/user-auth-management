package com.tcs.user_auth_management.config.jpa;

import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditConfig {
  @Bean
  public AuditorAware<String> auditorAware() {
    return () -> {
      var user = UserSecurity.getCurrentUser();

      if (user.isEmpty()) {
        return Optional.empty();
      }

      return user.map(userSecurity -> userSecurity.getUserId().toString());
    };
  }
}
