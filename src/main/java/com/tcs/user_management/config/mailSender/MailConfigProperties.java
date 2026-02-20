package com.tcs.user_management.config.mailSender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail-config-properties")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailConfigProperties {
    private String mailHost;
    private String mailSecret;
}
