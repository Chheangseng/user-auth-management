package com.tcs.user_auth_management.model.entity.common;

import java.time.Instant;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

public abstract class Auditable {
  @Column("created_at")
  @CreatedDate
  private Instant createdAt;

//  @CreatedBy private String createdBy;

  @Column("updated_at")
  @LastModifiedDate
  private Instant updatedAt;

//  @LastModifiedBy
//  private String updatedBy;
}
