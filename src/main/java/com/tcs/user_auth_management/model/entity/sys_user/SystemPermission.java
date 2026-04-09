package com.tcs.user_auth_management.model.entity.sys_user;

import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class SystemPermission extends BaseEntityUUID {
  @Column(unique = true, nullable = false)
  private String name;

  private String description;

  @Column(unique = true, nullable = false)
  private String permissionCode;
}
