package com.tcs.user_auth_management.model.entity;

import com.tcs.user_auth_management.model.entity.common.Auditable;
import java.time.Instant;
import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Table
@Data
public class UserSession extends Auditable {
  /// session id
  @Id private UUID id;

  private Instant expiryDate;

  private UUID userAuthId;

  private boolean invoked = false;
}
