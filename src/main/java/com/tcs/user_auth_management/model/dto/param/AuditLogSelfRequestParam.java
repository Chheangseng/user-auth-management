package com.tcs.user_auth_management.model.dto.param;

import com.tcs.user_auth_management.emuns.AuditLogEvent;
import com.tcs.user_auth_management.util.pagination.PaginationParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogSelfRequestParam extends PaginationParam {
  private AuditLogEvent event;
}
