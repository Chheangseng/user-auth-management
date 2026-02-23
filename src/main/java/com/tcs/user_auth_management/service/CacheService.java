package com.tcs.user_auth_management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.user_auth_management.repository.CacheStoreRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CacheService {
  private final CacheStoreRepository repository;
  private final ObjectMapper objectMapper;

  public void put(String key, Map<String, Object> value, long ttlSecond) {
    repository.insertOrUpdateCache(key, convertToString(value), Instant.now().plusSeconds(ttlSecond));
  }

  public <T> Optional<T> get(String key, Class<T> type) {
    var json = repository.getValidCacheValue(key);
    json.ifPresent(value -> {
      log.info("Cache Data: {}", value);
    });
    if (json.isEmpty()) {
      log.info("Cache Data not found");
      return Optional.empty();
    }
    try {
      // 2. Convert the JSON string back to your Class (e.g., UserAuth)
      return Optional.of(objectMapper.readValue(json.get(), type));
    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize cache for key: {}", key, e);
      return Optional.empty();
    }
  }
  public <T> T getCachedOrFetch(String key, Class<T> type, Supplier<T> dbFetch) {
    return this.get(key, type)
            .orElseGet(() -> {
              T value = dbFetch.get();
              this.put(key, value);
              return value;
            });
  }

  public <T> Optional<T> get(String key, Class<T> type, Supplier<T> callback) {
    Optional<T> cached = this.get(key, type);
    if (cached.isPresent()) {
      return cached;
    }
    try {
      T callbackResult = callback.get();
      if (callbackResult != null) {
        // Store in cache
        Map<String, Object> valueMap =
            objectMapper.convertValue(callbackResult, new TypeReference<Map<String, Object>>() {});
        put(key, valueMap);
        return Optional.of(callbackResult);
      }
    } catch (Exception e) {
      log.error("Error executing callback for key: {}", key, e);
    }
    return Optional.empty();
  }

  public void put(String key, Object value) {
    repository.insertOrUpdateCache(key, convertToString(value), Instant.now().plusSeconds(60));
  }

  public void del(String key) {
    repository.deleteById(key);
  }

  public void dels(Set<String> keys) {
    repository.deleteByCacheKeys(keys);
  }

  public String convertToString(Object value) {
    try {
      // This adds the necessary quotes and formatting for JSON
      return objectMapper.writeValueAsString(value);

    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize cache value", e);
    }
  }
}
