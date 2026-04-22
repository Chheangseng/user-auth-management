package com.tcs.user_auth_management.model.entity.sys_user;

import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "system_users")
@Entity
public class SystemUser extends BaseEntityUUID {
  private boolean isSuperAdmin = false;
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_auth_id", referencedColumnName = "id", nullable = false)
  private UserAuth userAuth;
}
