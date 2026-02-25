package com.tcs.user_auth_management.util.sql_builder;

import com.tcs.user_auth_management.util.Util;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class SqlParam<T> {
  @Getter private final MapSqlParameterSource params = new MapSqlParameterSource();
  private final String prefix;

  public SqlParam(String prefix) {
    this.prefix = prefix + "_";
  }

  public String add(SFunction<T, ?> column, Object value) {
    String fieldName = LambdaUtils.getPropertyName(column);
    String columnName = Util.camelToSnake(fieldName);
    String paramName = this.prefix + columnName;
    if (Objects.isNull(value)) return paramName;
    if (value instanceof Instant instant) {
      params.addValue(paramName, java.sql.Timestamp.from(instant));
    } else {
      params.addValue(paramName, value);
    }
    return paramName;
  }

  public void combineParam(SqlParam<T> newParam) {
    params.addValues(newParam.getParams().getValues());
  }
}
