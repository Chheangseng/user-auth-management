package com.tcs.user_auth_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ObjectMapperService {
  private final ObjectMapper mapper;

  public Map<String, Object> convertToMap(Object obj) {
    if (obj == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new HashMap<>();
    for (Field field : obj.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      try {
        result.put(field.getName(), field.get(obj));
      } catch (IllegalAccessException e) {
        log.error("Failed to access field: {}", field.getName(), e);
      }
    }

    return result;
  }

  public Map<String, Object> convertToMapNotNull(Object obj) {
    if (obj == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new HashMap<>();
    for (Field field : obj.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Object value = field.get(obj);
        if (Objects.nonNull(value)) {
          result.put(field.getName(), value);
        }
      } catch (IllegalAccessException e) {
        log.error("Failed to access field: {}", field.getName(), e);
      }
    }

    return result;
  }

  public <T> T convertFromMap(Map<String, Object> map, Class<T> targetClass) {
    if (map == null || map.isEmpty()) {
      return null;
    }

    try {
      return mapper.convertValue(map, targetClass);
    } catch (IllegalArgumentException e) {
      log.error(
          "Failed to convert Map to type [{}]: {}", targetClass.getSimpleName(), e.getMessage());

      throw new ApiExceptionStatusException(
          "Error reconstructing object from data", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
