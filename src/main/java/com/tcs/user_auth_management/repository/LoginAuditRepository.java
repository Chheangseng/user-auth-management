package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.LoginAudit;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
  List<LoginAudit> findTop2ByUserAuthIdOrderByLoginTimeDesc(UUID userAuthId);
}
