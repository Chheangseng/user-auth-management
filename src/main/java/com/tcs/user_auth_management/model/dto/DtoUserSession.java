package com.tcs.user_auth_management.model.dto;

import com.tcs.user_auth_management.model.entity.user.UserSession;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class DtoUserSession {
  private UUID sessionId;

  private Instant expiryDate;

  private DtoUser user;

  private boolean invoked = false;

  private String ipAddress;

  private DtoLocation location;

  private String userAgent;

  public record DtoUser(UUID userId, String username) {}

  public DtoUserSession(UserSession session) {
    this.sessionId = session.getId();
    this.expiryDate = session.getExpiryDate();
    this.invoked = session.isInvoked();
    this.ipAddress = session.getIpAddress();
    this.location = session.getLocation();
    var userAuth = session.getUserAuth();
    this.user = new DtoUser(userAuth.getId(), userAuth.getUsername());
    this.userAgent = session.getUserAgent();
  }
}
