package com.tcs.user_auth_management.model.entity.user.authorization;

import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Permission extends BaseEntityUUID {
  @Column(unique = true, nullable = false)
  private String name;

  private String description;

  @Column(unique = true, nullable = false)
  private String permissionCode;

  @ManyToMany(mappedBy = "permissions")
  private Set<Role> roles = new HashSet<>();
}
