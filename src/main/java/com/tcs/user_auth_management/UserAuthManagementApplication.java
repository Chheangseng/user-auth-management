package com.tcs.user_auth_management;

import com.tcs.user_auth_management.config.mailSender.MailConfigProperties;
import com.tcs.user_auth_management.config.security.jwt.RSAProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({RSAProperties.class, MailConfigProperties.class})
@EnableScheduling
public class UserAuthManagementApplication {
  public static void main(String[] args) {
    SpringApplication.run(UserAuthManagementApplication.class, args);
  }
}
