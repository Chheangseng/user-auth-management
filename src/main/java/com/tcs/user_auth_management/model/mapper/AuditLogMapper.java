package com.tcs.user_auth_management.model.mapper;

import com.tcs.user_auth_management.model.dto.user.DtoAuditLog;
import com.tcs.user_auth_management.model.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

  AuditLogMapper INSTANCE = Mappers.getMapper(AuditLogMapper.class);

  @Mapping(source = "userAuth.username", target = "username")
  DtoAuditLog toDto(AuditLog auditLog);
}
