package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.UserSession;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserSessionRepository extends CrudRepository<UserSession, String> {
  @Modifying
  @Query("UPDATE user_session SET invoked = true WHERE id = :id")
  int invokedSessionById(String id);

  @Modifying
  @Query("UPDATE user_session SET invoked = true WHERE user_auth_id = :userId")
  int invokedSessionAllByUserId(String userId);
}
