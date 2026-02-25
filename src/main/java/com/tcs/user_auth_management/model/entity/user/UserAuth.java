package com.tcs.user_auth_management.model.entity.user;

import com.tcs.user_auth_management.emuns.Role;
import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
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

  @ElementCollection(targetClass = Role.class, fetch = FetchType.LAZY)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  private Set<Role> roles = new HashSet<>();

  private boolean activate = true;

  private boolean emailVerified = false;

  @Column(nullable = false, columnDefinition = "int default 0")
  private int risk = 0;

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
      mappedBy = "userAuth",
      orphanRemoval = true)
  private Set<UserSession> userSessions = new HashSet<>();

  public void addRole(Role role) {
    if (roles != null) {
      roles.add(role);
    }
  }
}
