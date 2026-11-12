package com.tcs.user_auth_management.service.user;

import com.tcs.user_auth_management.exception.AppException;
import com.tcs.user_auth_management.exception.errorcode.AuthErrorCode;
import com.tcs.user_auth_management.model.dto.DtoUserSession;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSession;
import com.tcs.user_auth_management.repository.UserSessionRepository;
import com.tcs.user_auth_management.repository.specification.UserSessionSpec;
import com.tcs.user_auth_management.util.pagination.PaginationEntityResponse;
import com.tcs.user_auth_management.util.pagination.PaginationParam;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserSessionService {
  private final UserSessionRepository repository;
  private final UserRequestInfoService requestInfoService;

  public PaginationEntityResponse<DtoUserSession> userSessionPage(PaginationParam pagination) {
    var response = repository.findAll(pagination.toPageable());
    return PaginationEntityResponse.toResponse(response.map(DtoUserSession::new));
  }

  public UserSession createSession(UserAuth userAuth, long expireInSec) {
    UserSession session = new UserSession();
    session.setUserAuth(userAuth);
    session.setExpiryDate(Instant.now().plusSeconds(expireInSec));
    session.setInvoked(false);
    session.generateJwtTokenId();
    var info = requestInfoService.userRequestInfo();
    session.setUserAgent(info.getUserAgent());
    session.setLocation(info.getLocation());
    session.setIpAddress(info.getIp());
    return repository.save(session);
  }

  public UserSession rotateSessionToken(UUID oldJwtId, long expireInSec) {
    UserSession session = getUserSessionByJwtId(oldJwtId);
    Instant now = Instant.now();
    boolean isInGracePeriod =
        session.getUpdateJwtTokenIdAt() != null
            && session.getUpdateJwtTokenIdAt().isAfter(now.minusSeconds(30));

    if (!isInGracePeriod) {
      session.generateJwtTokenId();
      session.setUpdateJwtTokenIdAt(now);
    }
    session.setExpiryDate(Instant.now().plusSeconds(expireInSec));
    session.setUpdateJwtTokenIdAt(Instant.now());
    repository.save(session);
    return session;
  }

  public void invokeSession(UUID jwtId) {
    UserSession userSession = this.getUserSessionByJwtId(jwtId);
    userSession.setInvoked(true);
    userSession.setInvokedTime(Instant.now());
    repository.save(userSession);
  }

  public void invokeSessionById(UUID sessionId) {
    invokeSessionById(sessionId, null);
  }

  public void invokeSessionById(UUID sessionId, UUID userId) {
    var userSession = getUserSessionBySessionId(sessionId);
    if (Objects.nonNull(userId) && !userId.equals(userSession.getUserAuth().getId())) {
      throw new AppException(AuthErrorCode.UNAUTHORIZED_SESSION_ACCESS);
    }
    userSession.setInvoked(true);
    userSession.setInvokedTime(Instant.now());
    repository.save(userSession);
  }

  public void invokeSessionAllByUserAuthId(UUID userAuthId) {
    repository.update(UserSessionSpec.invokeAllSessionByUserAuthId(userAuthId));
  }

  public UserSession getUserSessionByJwtId(UUID jwtId) {
    return repository
        .findByJwtId(jwtId)
        .orElseThrow(
            () -> new AppException(AuthErrorCode.INVALID_TOKEN));
  }

  public UserSession verifyUserSession(UUID sessionId, UUID jwtId) {
    var userSession = getUserSessionBySessionId(sessionId);
    validateSession(userSession);
    if (!jwtId.equals(userSession.getJwtTokenId())) {
      throw new AppException(AuthErrorCode.INVALID_TOKEN, "Invalid JWT token ID");
    }

    return userSession;
  }

  public UserSession getUserSessionBySessionId(UUID sessionId) {
    return repository
        .findById(sessionId)
        .orElseThrow(
            () -> new AppException(AuthErrorCode.INVALID_TOKEN));
  }

  public void validateSession(UserSession userSession) {
    // Check if session has expired
    if (userSession.getExpiryDate().isBefore(Instant.now())) {
      throw new AppException(AuthErrorCode.SESSION_EXPIRED);
    }
    // Check if session is revoked
    if (userSession.isInvoked()) {
      throw new AppException(AuthErrorCode.SESSION_REVOKED);
    }
  }
}
