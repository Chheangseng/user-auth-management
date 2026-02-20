package com.tcs.user_management;

import com.tcs.user_management.config.jwt.RSAProperties;
import com.tcs.user_management.config.mailSender.MailConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
  RSAProperties.class,
  MailConfigProperties.class,
})
public class UserManagementApplication {

  public static void main(String[] args) {
    SpringApplication.run(UserManagementApplication.class, args);
  }
}
