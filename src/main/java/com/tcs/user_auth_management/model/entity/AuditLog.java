package com.tcs.user_auth_management.model.entity;

import com.tcs.user_auth_management.emuns.AuditLogEvent;
import com.tcs.user_auth_management.model.dto.DtoLocation;
import com.tcs.user_auth_management.model.entity.common.BaseEntity;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_auth_id")
  private UserAuth userAuth;

  @Column(length = 45, nullable = false)
  private String ipAddress; // IPv4 or IPv6

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private DtoLocation location;

  @Enumerated(EnumType.STRING)
  private AuditLogEvent auditLogEvent;
}
