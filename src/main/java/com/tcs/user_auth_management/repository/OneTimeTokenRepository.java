package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.OneTimeToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneTimeTokenRepository extends JpaRepository<OneTimeToken, UUID> {}
