package com.tcs.user_auth_management.repository;

import com.tcs.user_auth_management.model.entity.CacheStore;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheStoreRepository extends CrudRepository<CacheStore, String> {
  @Modifying
  @Query("DELETE FROM cache_store WHERE cache_key IN (:keys)")
  void deleteByCacheKeys(Iterable<String> keys);

  @Modifying
  @Query("INSERT INTO cache_store (cache_key, cache_value, expires_at) " +
          "VALUES (:key, CAST(:value AS jsonb), :expires) " +
          "ON CONFLICT (cache_key) " +
          "DO UPDATE SET " +
          "cache_value = EXCLUDED.cache_value, " +
          "expires_at = EXCLUDED.expires_at")
  void insertOrUpdateCache(
          @Param("key") String key,
          @Param("value") String value,
          @Param("expires") Instant expires
  );

  @Query("SELECT cache_value FROM cache_store WHERE cache_key = :key")
  Optional<String> getValidCacheValue(@Param("key") String key);
}
