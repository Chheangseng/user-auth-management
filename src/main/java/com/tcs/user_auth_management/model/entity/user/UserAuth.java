package com.tcs.user_auth_management.model.entity.user;

import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import com.tcs.user_auth_management.model.entity.user.authorization.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "user_auth",
    indexes = {@Index(name = "idx_userauth_username", columnList = "username")})
public class UserAuth extends BaseEntityUUID {
  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(unique = true, nullable = false)
  private String email;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "role_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(name = "fk_role"))
  private Role role;

  private boolean enabled = true;

  private boolean emailVerified = false;

  @Column(nullable = false, columnDefinition = "int default 0")
  private int risk = 0;
}
