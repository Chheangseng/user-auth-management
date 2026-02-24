package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.util.sql_builder.LambdaInsertWrapper;
import com.tcs.user_auth_management.util.sql_builder.LambdaUpdateWrapper;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JdbcCRUDService {
  private final NamedParameterJdbcTemplate namedJdbc;
  private final JdbcTemplate jdbcTemplate;
  private final RelationalMappingContext mappingContext;

  public int execute(LambdaInsertWrapper<?> wrapper) {
    String sql = wrapper.buildSql(mappingContext);
    return namedJdbc.update(sql, wrapper.buildParm());
  }

  public <T> T executeAndGetId(LambdaInsertWrapper<?> wrapper, String idColumnName) {
    String sql = wrapper.buildSql(mappingContext);
    KeyHolder keyHolder = new GeneratedKeyHolder();

    // Execute with the specific column name provided by the user
    namedJdbc.update(sql, wrapper.buildParm(), keyHolder, new String[] {idColumnName});

    Map<String, Object> keys = keyHolder.getKeys();

    if (keys != null && keys.containsKey(idColumnName)) {
      // Automatically casts to whatever type the user assigned it to
      return (T) keys.get(idColumnName);
    }
    return null;
  }

  public <T> T executeAndGetId(LambdaInsertWrapper<?> wrapper) {
    return this.executeAndGetId(wrapper, "id");
  }

  public int execute(LambdaUpdateWrapper<?> wrapper) {
    return jdbcTemplate.update(wrapper.buildSql(mappingContext), wrapper.getParams());
  }
}
