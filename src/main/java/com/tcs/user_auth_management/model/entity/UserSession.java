package com.tcs.user_auth_management.model.entity;

import com.tcs.user_auth_management.config.jpa.JsonbConverter;
import com.tcs.user_auth_management.model.dto.DtoLocation;
import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_sessions")
public class UserSession extends BaseEntityUUID {
  @Column(nullable = false)
  private Instant expiryDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_auth_id", nullable = false)
  private UserAuth userAuth;

  @Column(nullable = false)
  private boolean invoked = false;

  private String ipAddress;

  @Column @Lob private String userAgent;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private DtoLocation location;
}
