package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.UserSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
  @Query("SELECT us FROM UserSession us WHERE us.id = :id")
  Optional<UserSession> findByIdWithUserAuth(@Param("id") UUID id);

  // Update by session ID
  @Modifying
  @Query("UPDATE UserSession us SET us.invoked = :invoked WHERE us.id = :sessionId")
  int updateInvokedBySessionId(@Param("sessionId") UUID sessionId,
                               @Param("invoked") boolean invoked);

  // Update by userAuth ID
  @Modifying
  @Query("UPDATE UserSession us SET us.invoked = :invoked WHERE us.userAuth.id = :userAuthId")
  int updateInvokedByUserAuthId(@Param("userAuthId") UUID userAuthId,
                                @Param("invoked") boolean invoked);
}
