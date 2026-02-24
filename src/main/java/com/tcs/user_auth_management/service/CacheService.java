package com.tcs.user_auth_management.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.user_auth_management.model.entity.CacheStore;
import com.tcs.user_auth_management.repository.CacheStoreRepository;
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
    repository.save(new CacheStore(key, value, ttlSecond));
  }

  public <T> Optional<T> get(String key, Class<T> type) {
    return repository
        .findById(key)
        .map(CacheStore::getCacheValue)
        .map(value -> objectMapper.convertValue(value, type));
  }

  public <T> T getOrFetch(String key, Class<T> type, Supplier<T> callback) {
    var result =  repository
            .findById(key)
            .map(CacheStore::getCacheValue)
            .map(value -> objectMapper.convertValue(value, type));
      return result.orElseGet(callback);
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
    repository.save(
        new CacheStore(
            key,
            objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {}),
            60));
  }

  public void putNoExpired(String key, Object value) {
    repository.save(
        new CacheStore(
            key, objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {}), 0));
  }

  public void del(String key) {
    repository.deleteById(key);
  }

  public void dels(Set<String> keys) {
    repository.deleteAllByIdInBatch(keys);
  }
}
