package com.tcs.user_auth_management.service.user;

import com.tcs.user_auth_management.emuns.AuditLogEvent;
import com.tcs.user_auth_management.model.dto.DtoUserRequestInfo;
import com.tcs.user_auth_management.model.dto.param.AuditLogSelfRequestParam;
import com.tcs.user_auth_management.model.dto.user.DtoAuditLog;
import com.tcs.user_auth_management.model.entity.AuditLog;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.model.mapper.AuditLogMapper;
import com.tcs.user_auth_management.repository.AuditLogRepository;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import com.tcs.user_auth_management.repository.specification.AuditLogSpec;
import com.tcs.user_auth_management.util.pagination.PaginationEntityResponse;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserActivityService {
  private final AuditLogRepository repository;
  private final UserAuthRepository authRepository;
  private final AuditLogMapper logMapper;

  public PaginationEntityResponse<DtoAuditLog> pageByUserId(UUID userId,AuditLogSelfRequestParam param) {
    return PaginationEntityResponse.toResponse(repository
        .findAll(
            AuditLogSpec.filterWithUserId(param, userId),
            param.toPageable())
        .map(logMapper::toDto));
  }

  public void saveAudit(DtoUserRequestInfo request, UUID userAuthId, AuditLogEvent event) {
    AuditLog auditLog = new AuditLog();
    auditLog.setUserAgent(request.getUserAgent());
    auditLog.setUserAuth(authRepository.getReferenceById(userAuthId));
    auditLog.setLocation(request.getLocation());
    auditLog.setIpAddress(request.getIp());
    auditLog.setAuditLogEvent(event);
    repository.save(auditLog);
  }

  public void saveAudit(DtoUserRequestInfo request, AuditLogEvent event) {
    AuditLog auditLog = new AuditLog();
    auditLog.setUserAgent(request.getUserAgent());
    auditLog.setLocation(request.getLocation());
    auditLog.setIpAddress(auditLog.getIpAddress());
    auditLog.setAuditLogEvent(event);
    repository.save(auditLog);
  }

  @Async
  public void asyncSaveAudit(DtoUserRequestInfo request, UUID userAuthId, AuditLogEvent event) {
    AuditLog auditLog = new AuditLog();
    auditLog.setUserAgent(request.getUserAgent());
    auditLog.setUserAuth(authRepository.getReferenceById(userAuthId));
    auditLog.setLocation(request.getLocation());
    auditLog.setIpAddress(request.getIp());
    auditLog.setAuditLogEvent(event);
    repository.save(auditLog);
  }

  @Async
  public void asyncSaveAudit(DtoUserRequestInfo request, AuditLogEvent event) {
    AuditLog auditLog = new AuditLog();
    auditLog.setUserAgent(request.getUserAgent());
    auditLog.setLocation(request.getLocation());
    auditLog.setIpAddress(auditLog.getIpAddress());
    auditLog.setAuditLogEvent(event);
    repository.save(auditLog);
  }
}
