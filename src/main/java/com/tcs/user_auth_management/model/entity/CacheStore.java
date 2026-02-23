package com.tcs.user_auth_management.model.entity;

import java.time.Instant;
import java.util.Map;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Data
public class CacheStore {
  @Id private String cacheKey;

  private Map<String, Object> cacheValue;

  private Instant expiresAt;

  private Instant createdAt;
}
