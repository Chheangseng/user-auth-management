package com.tcs.user_auth_management.util.sql_builder;

import com.tcs.user_auth_management.util.Util;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class LambdaInsertWrapper<T> {
  private final Class<T> entityClass;
  private final Map<String, Object> data;

  public LambdaInsertWrapper(Class<T> entityClass, Map<String, Object> data) {
    this.entityClass = entityClass;
    this.data = data;
  }

  public String buildSql(RelationalMappingContext mappingContext) {
    if (data == null || data.isEmpty()) {
      throw new IllegalStateException("No columns to insert.");
    }

    String tableName =
        mappingContext
            .getRequiredPersistentEntity(entityClass)
            .getTableName()
            .toSql(IdentifierProcessing.NONE);

    // Map the map keys (camelCase) to snake_case for SQL columns
    String columns =
        data.keySet().stream().map(Util::camelToSnake).collect(Collectors.joining(", "));

    // Create named parameters based on the snake_case column names
    String placeholders =
        data.keySet().stream()
            .map(key -> ":" + Util.camelToSnake(key))
            .collect(Collectors.joining(", "));

    return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
  }

  public Map<String, Object> baseParam() {
    return data.entrySet().stream()
        .collect(Collectors.toMap(entry -> Util.camelToSnake(entry.getKey()), Map.Entry::getValue));
  }

  public MapSqlParameterSource buildParm(){
    Map<String,Object> rawParam = baseParam();
    MapSqlParameterSource params = new MapSqlParameterSource();
    rawParam.forEach((key, value) -> {
      if (value instanceof Instant instant) {
        params.addValue(key, java.sql.Timestamp.from(instant));
      } else {
        params.addValue(key, value);
      }
    });
    return params;
  }
}
