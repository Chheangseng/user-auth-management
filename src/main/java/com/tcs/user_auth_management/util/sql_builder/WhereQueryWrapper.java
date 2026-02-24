package com.tcs.user_auth_management.util.sql_builder;

import java.util.ArrayList;
import java.util.List;

import com.tcs.user_auth_management.util.Util;
import lombok.Getter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class WhereQueryWrapper<T> {
  private final List<String> conditions = new ArrayList<>();
  @Getter private final MapSqlParameterSource params = new MapSqlParameterSource();
  private int paramCounter = 0;

  public WhereQueryWrapper<T> eq(SFunction<T, ?> column, Object value) {
    return addCondition(column, "=", value);
  }

  public WhereQueryWrapper<T> gt(SFunction<T, ?> column, Object value) {
    return addCondition(column, ">", value);
  }

  public WhereQueryWrapper<T> gte(SFunction<T, ?> column, Object value) {
    return addCondition(column, ">=", value);
  }

  public WhereQueryWrapper<T> lt(SFunction<T, ?> column, Object value) {
    return addCondition(column, "<", value);
  }

  public WhereQueryWrapper<T> lte(SFunction<T, ?> column, Object value) {
    return addCondition(column, "<=", value);
  }

  public WhereQueryWrapper<T> isNull(SFunction<T, ?> column) {
    return addNoValueCondition(column, "IS NULL");
  }

  public WhereQueryWrapper<T> isNotNull(SFunction<T, ?> column) {
    return addNoValueCondition(column, "IS NOT NULL");
  }

  private WhereQueryWrapper<T> addCondition(
      SFunction<T, ?> column, String operator, Object value) {
    if (value != null) {
      String fieldName = LambdaUtils.getPropertyName(column);
      String columnName = Util.camelToSnake(fieldName);
      String paramName = "where_" + fieldName + "_" + (paramCounter++);

      conditions.add(columnName + " " + operator + " :" + paramName);
      params.addValue(paramName, value);
    }
    return this;
  }

  private WhereQueryWrapper<T> addNoValueCondition(SFunction<T, ?> column, String operator) {
    String fieldName = LambdaUtils.getPropertyName(column);
    String columnName = Util.camelToSnake(fieldName);

    conditions.add(columnName + " " + operator);
    return this;
  }

  public String getWhereSql() {
    return conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
  }
}
