package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.user.UserAuth;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends CrudRepository<UserAuth, String> {
  Optional<UserAuth> findByUsername(String username);

  boolean existsByUsername(String username);

  Optional<UserAuth> findByEmail(String email);

  boolean existsByEmail(String email);
}
