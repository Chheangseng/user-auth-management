package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.user.UserAuth;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, UUID> {
  Optional<UserAuth> findByUsername(String username);

  boolean existsByUsername(String username);

  Optional<UserAuth> findByEmail(String email);

  boolean existsByEmail(String email);
}
