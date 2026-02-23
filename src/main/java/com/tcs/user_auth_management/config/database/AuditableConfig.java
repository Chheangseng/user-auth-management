package com.tcs.user_auth_management.config.database;

import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@Configuration
@EnableJdbcAuditing
public class AuditableConfig {
  @Bean
  public AuditorAware<String> auditorProvider() {
    return () -> {
      var user = UserSecurity.getUserSecurity();
      return user.map(userSecurity -> userSecurity.getUserId().toString()).or(() -> Optional.of("SYSTEM"));
    };
  }
}
