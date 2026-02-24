package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.AuditLogEvent;
import com.tcs.user_auth_management.model.dto.DtoAuditLogCreate;
import com.tcs.user_auth_management.model.dto.DtoUserRequestInfo;
import com.tcs.user_auth_management.model.entity.AuditLog;
import com.tcs.user_auth_management.util.sql_builder.LambdaInsertWrapper;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuditLogService {
  private final ObjectMapperService mapper;
  private final JdbcCRUDService service;

  @Async
  public void auditEvent(UUID userAuthId, AuditLogEvent event, DtoUserRequestInfo userInfo) {
    var create = new DtoAuditLogCreate();
    create.setIpAddress(userInfo.getIp());
    create.setUserAuthId(userAuthId);
    create.setUserAgent(userInfo.getUserAgent());
    create.setAction(event.getAction());
    if (Objects.nonNull(userInfo.getLocation())) {
      create.setLocation(mapper.convertToMapNotNull(userInfo.getLocation()));
    }
    var query = new LambdaInsertWrapper<>(AuditLog.class, mapper.convertToMapNotNull(create));
    service.execute(query);
  }

  @Async
  public void auditEvent(AuditLogEvent event, DtoUserRequestInfo userInfo) {
    var create = new DtoAuditLogCreate();
    create.setIpAddress(userInfo.getIp());
    create.setUserAgent(userInfo.getUserAgent());
    create.setAction(event.getAction());
    if (Objects.nonNull(userInfo.getLocation())) {
      create.setLocation(mapper.convertToMap(userInfo.getLocation()));
    }
    var query = new LambdaInsertWrapper<>(AuditLog.class, mapper.convertToMapNotNull(create));
    service.execute(query);
  }
}
