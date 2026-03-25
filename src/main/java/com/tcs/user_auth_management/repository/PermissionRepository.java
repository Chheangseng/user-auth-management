package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.user.authorization.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {}
