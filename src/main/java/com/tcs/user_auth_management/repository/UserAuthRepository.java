package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.user.UserAuth;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, UUID> {
  Optional<UserAuth> findByUsername(String username);
  @Query("SELECT u FROM UserAuth u JOIN FETCH u.role WHERE u.username = :username")
  Optional<UserAuth> findByUsernameWithRole(@Param("username") String username);

  boolean existsByUsername(String username);

  Optional<UserAuth> findByRecoveryEmail(String recoveryEmail);

  boolean existsByRecoveryEmail(String email);

  @Query("SELECT u FROM UserAuth u LEFT JOIN FETCH u.role WHERE u.id = :id")
  Optional<UserAuth> findByIdWithRole(@Param("id") UUID id);
}
