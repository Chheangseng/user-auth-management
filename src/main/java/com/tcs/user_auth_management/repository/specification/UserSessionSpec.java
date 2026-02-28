package com.tcs.user_auth_management.repository.specification;

import com.tcs.user_auth_management.model.entity.user.UserSession;
import com.tcs.user_auth_management.model.entity.user.UserSession_;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.UpdateSpecification;

public class UserSessionSpec {
  public  static UpdateSpecification<UserSession> invokeAllSessionByUserAuthId(UUID userAuthId) {
    return UpdateSpecification.<UserSession>update(
            (root, update, criteriaBuilder) -> {
              update.set(root.get(UserSession_.invoked), true);
              update.set(root.get(UserSession_.invokedTime), Instant.now());
            })
        .where(
            (from, criteriaBuilder) ->
                criteriaBuilder.equal(from.get(UserSession_.userAuth), userAuthId));
  }
}
