package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.oidc.AuthorizationCode;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, UUID> {
  Optional<AuthorizationCode> findByCode(String code);

  @Modifying
  @Query("DELETE FROM AuthorizationCode a WHERE a.expiresAt < :now")
  void deleteByExpiresAtBefore(@Param("now") Instant now);
}
