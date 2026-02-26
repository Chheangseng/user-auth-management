package com.tcs.user_auth_management.model.dto;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public record DtoJwtPayload(Jwt jwt) {
  public UUID getJwtId(){
    return UUID.fromString(jwt.getId());
  }
  public UUID getUserId() {
    return UUID.fromString(jwt.getSubject());
  }

  public UUID getSessionId() {
    return UUID.fromString(jwt.getId());
  }
}
