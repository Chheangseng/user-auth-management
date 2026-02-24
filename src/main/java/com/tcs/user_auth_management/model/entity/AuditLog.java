package com.tcs.user_auth_management.model.entity;

import java.time.Instant;
import java.util.Map;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Data
public class AuditLog {
  @Id private Long id;
  private String userAuthId;
  private String action;
  private String ipAddress;
  private String userAgent;
  private Map<String, Object> metadata;
  private Map<String, Object> location;
  private Instant createdAt;
}
