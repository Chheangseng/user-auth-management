package com.tcs.user_auth_management;

import com.tcs.user_auth_management.config.mailSender.MailConfigProperties;
import com.tcs.user_auth_management.config.oidc.OidcProperties;
import com.tcs.user_auth_management.config.security.jwt.RSAProperties;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({RSAProperties.class, MailConfigProperties.class, OidcProperties.class})
@EnableScheduling
public class UserAuthManagementApplication {

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    SpringApplication.run(UserAuthManagementApplication.class, args);
  }
}
