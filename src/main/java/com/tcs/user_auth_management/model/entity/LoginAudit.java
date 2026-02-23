package com.tcs.user_auth_management.model.entity;

import com.tcs.user_auth_management.emuns.AuthenticationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

import com.tcs.user_auth_management.model.entity.common.Auditable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@Table
public class LoginAudit extends Auditable {

  @Id private Long id;

  private UUID userAuthId;

  private String ipAddress; // IPv4 or IPv6

  private String userAgent;

  private AuthenticationStatus status; // SUCCESS / FAILURE / LOCKED

  private LocalDateTime loginTime;

  private LocalDateTime logoutTime;

  private String countryName;
  private String countryCode;
  private String region;
  private String city;

  private double latitude = 0.0;

  private double longitude = 0.0;

  private String timeZone;
}
