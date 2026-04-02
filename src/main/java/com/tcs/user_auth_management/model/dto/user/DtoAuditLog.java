package com.tcs.user_auth_management.model.dto.user;

import com.tcs.user_auth_management.emuns.AuditLogEvent;
import com.tcs.user_auth_management.model.dto.DtoLocation;
import lombok.Data;

@Data
public class DtoAuditLog {

  private String username;

  private String ipAddress; // IPv4 or IPv6

  private String userAgent;

  private DtoLocation location;

  private AuditLogEvent auditLogEvent;
}
