package com.tcs.user_auth_management.util.sql_builder;

import com.tcs.user_auth_management.util.Util;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Getter
public class LambdaUpdateWrapper<T> extends WhereQueryWrapper<T> {
  private final List<String> sets = new ArrayList<>();
  private final String prefixUpdate = "upd_";
  private final SqlParam<T> updateParam = new SqlParam<T>(prefixUpdate);
  private final Class<T> entityClass;

  public LambdaUpdateWrapper(Class<T> entityClass) {
    this.entityClass = entityClass;
  }

  public LambdaUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object value) {
    if (!condition) return this;
    String fieldName = LambdaUtils.getPropertyName(column);
    String columnName = Util.camelToSnake(fieldName);
    var keyParam = updateParam.add(column, value);
    sets.add(columnName + " = :" + keyParam);
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

  public MapSqlParameterSource buildParm() {
    updateParam.combineParam(this.getWhereParams());
    return updateParam.getParams();
  }
}
