package com.tcs.user_auth_management.model.entity.sys_user;

import com.tcs.user_auth_management.model.entity.common.BaseEntityUUID;
import com.tcs.user_auth_management.model.entity.user.authorization.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table
@Entity
public class SystemUser extends BaseEntityUUID {
    private String username;
    private String password;
    @Column(unique = true, nullable = false)
    private String recoveryEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "role_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_role"))
    private Role role;

    private boolean enabled = true;

    private boolean emailVerified = false;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int riskScore  = 0;

    private boolean isSuperAdmin = false;
}
