package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.sys_user.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, UUID> {}
