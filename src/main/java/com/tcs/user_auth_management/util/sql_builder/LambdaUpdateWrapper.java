package com.tcs.user_auth_management.util.sql_builder;

import com.tcs.user_auth_management.util.Util;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

@Getter
public class LambdaUpdateWrapper<T> extends WhereQueryWrapper<T> {
  private final List<String> sets = new ArrayList<>();
  private final Class<T> entityClass;

  public LambdaUpdateWrapper(Class<T> entityClass) {
    this.entityClass = entityClass;
  }

  public LambdaUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object value) {
    if (!condition) return this;
    String fieldName = LambdaUtils.getPropertyName(column);
    String columnName = Util.camelToSnake(fieldName);
    if (value != null) {
      sets.add(columnName + " = :" + fieldName);
    } else {
      sets.add(columnName + " = NULL");
    }
    return this;
  }

  public LambdaUpdateWrapper<T> set(SFunction<T, ?> column, Object value) {
    return this.set(true, column, value);
  }

  public String buildSql(RelationalMappingContext mappingContext) {
    if (sets.isEmpty()) {
      throw new IllegalStateException("No columns to update. Call set() at least once.");
    }

    String tableName =
        mappingContext
            .getRequiredPersistentEntity(entityClass)
            .getTableName()
            .toSql(IdentifierProcessing.NONE);

    return String.format(
        "UPDATE %s SET %s %s", tableName, String.join(", ", sets), this.getWhereSql());
  }
}
