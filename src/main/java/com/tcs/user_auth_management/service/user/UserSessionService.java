package com.tcs.user_auth_management.service.user;

import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.user.DtoUserSessionCreate;
import com.tcs.user_auth_management.model.entity.UserSession;
import com.tcs.user_auth_management.repository.UserSessionRepository;
import com.tcs.user_auth_management.service.JdbcCRUDService;
import com.tcs.user_auth_management.service.ObjectMapperService;
import com.tcs.user_auth_management.util.sql_builder.LambdaInsertWrapper;
import com.tcs.user_auth_management.util.sql_builder.LambdaUpdateWrapper;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserSessionService {
  private final UserSessionRepository repository;
  private final JdbcCRUDService jdbcCRUDService;
  private final ObjectMapperService objectMapperService;

  public UUID generateSession(UUID userAuthId, Instant expiryDate) {
    var dto = new DtoUserSessionCreate();
    dto.setUserAuthId(userAuthId);
    dto.setExpiryDate(expiryDate);
    return jdbcCRUDService.executeAndGetId(
        new LambdaInsertWrapper<>(UserSession.class, objectMapperService.convertToMapNotNull(dto)));
  }

  public void updateSessionExpiry(UUID sessionId, Instant expiryDate) {
    var updateQuery = new LambdaUpdateWrapper<>(UserSession.class);
    updateQuery
        .set(UserSession::getExpiryDate, expiryDate)
        .eq(UserSession::getId, sessionId);
    int effected = jdbcCRUDService.execute(updateQuery);
    if (effected == 0) {
      throw new ApiExceptionStatusException("Invalid id: " + sessionId, HttpStatus.NOT_FOUND);
    }
  }
  public void invokeToken(String sessionId) {
    if (Objects.isNull(sessionId)) return;
    repository.invokedSessionById(sessionId);
  }

  public void invokeAllToken(String userId) {
    repository.invokedSessionAllByUserId(userId);
  }
}
