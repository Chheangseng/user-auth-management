package com.tcs.user_auth_management.model.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@MappedSuperclass
public abstract class EntityAudit {
  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", updatable = false)
  @CreatedBy
  private String createdBy;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "updated_by")
  @LastModifiedBy
  private String updatedBy;

  @Column(name = "is_deleted")
  private boolean deleted = false;

  public void softDelete() {
    this.deleted = true;
  }

  public void restore() {
    this.deleted = false;
  }

  @PrePersist
  public void prePersist() {
    if (this.getCreatedAt() == null) {
      this.setCreatedAt(Instant.now());
    }
    if (this.getUpdatedAt() == null) {
      this.setUpdatedAt(Instant.now());
    }
  }

  // Pre-update hook
  @PreUpdate
  public void preUpdate() {
    this.setUpdatedAt(Instant.now());
  }
}
