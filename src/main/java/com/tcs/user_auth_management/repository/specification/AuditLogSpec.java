package com.tcs.user_auth_management.repository.specification;

import com.tcs.user_auth_management.model.dto.param.AuditLogSelfRequestParam;
import com.tcs.user_auth_management.model.entity.AuditLog;
import com.tcs.user_auth_management.model.entity.AuditLog_;
import com.tcs.user_auth_management.model.entity.user.UserAuth_;
import jakarta.persistence.criteria.JoinType;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class AuditLogSpec {
  public static Specification<AuditLog> filterWithUserId(
      AuditLogSelfRequestParam param, UUID userId) {
    Specification<AuditLog> spec =
        ((root, query, criteriaBuilder) -> {
          root.fetch(AuditLog_.userAuth, JoinType.LEFT);
          return criteriaBuilder.conjunction();
        });

    if (Objects.nonNull(userId)) {
      spec =
          spec.and(
              (root, query, cb) ->
                  cb.equal(root.get(AuditLog_.userAuth).get(UserAuth_.id), userId));
    }

    if (Objects.nonNull(param.getEvent())) {
      spec =
          spec.and(
              (root, query, cb) -> cb.equal(root.get(AuditLog_.auditLogEvent), param.getEvent()));
    }

    return spec;
  }
}
