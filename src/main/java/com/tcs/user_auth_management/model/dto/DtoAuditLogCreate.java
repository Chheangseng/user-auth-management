package com.tcs.user_auth_management.model.dto;

import java.util.Map;
import java.util.UUID;

import lombok.Data;

@Data
public class DtoAuditLogCreate {
  private UUID userAuthId;
  private String action;
  private String ipAddress;
  private String userAgent;
  private Map<String, Object> metadata;
  private Map<String, Object> location;
}
