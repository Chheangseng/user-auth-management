package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.user.UserSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> , JpaSpecificationExecutor<UserSession> {
  @EntityGraph(attributePaths = {"userAuth"})
  Page<UserSession> findAll(Pageable pageable);

  @Query("SELECT us FROM UserSession us WHERE us.jwtTokenId = :jwtId")
  Optional<UserSession> findByJwtId(@Param("jwtId") UUID jwtId);
}
