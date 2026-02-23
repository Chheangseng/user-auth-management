package com.tcs.user_auth_management.model.entity.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table
public class UserAuth {
  @Id private UUID id;

  private String username;

  private String password;

  private String fullName;

  private String email;

  private boolean activate = true;

  private boolean emailVerified = false;

  private int risk = 0;
}
