package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.LoginAudit;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAuditRepository extends CrudRepository<LoginAudit, Long> {
  List<LoginAudit> findTop2ByUserAuthIdOrderByLoginTimeDesc(String userAuthId);
}
