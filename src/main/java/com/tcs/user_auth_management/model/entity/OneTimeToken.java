package com.tcs.user_auth_management.model.entity;

import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "one_time_tokens")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class OneTimeToken extends BaseEntityUUID {
  private Instant expireTime;
  private boolean isUsed = false;
  /// allow token to be use if have matching bindingId
  private String bindingId;
}
