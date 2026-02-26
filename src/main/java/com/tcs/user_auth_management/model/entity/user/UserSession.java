package com.tcs.user_auth_management.model.entity.user;

import com.fasterxml.uuid.Generators;
import com.tcs.user_auth_management.model.dto.DtoLocation;
import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "user_sessions",
    indexes = {@Index(name = "idx_user_session_jwt_token_id", columnList = "jwtTokenId")})
public class UserSession extends BaseEntityUUID {
  @Column(nullable = false)
  private Instant expiryDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_auth_id", nullable = false)
  private UserAuth userAuth;

  @Column(nullable = false)
  private boolean invoked = false;

  private Instant invokedTime;

  @Column(length = 45, nullable = false)
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private DtoLocation location;

  private UUID jwtTokenId;

  private Instant updateJwtTokenIdAt;

  public void generateJwtTokenId() {
    this.jwtTokenId = Generators.timeBasedEpochGenerator().generate();
  }
}
