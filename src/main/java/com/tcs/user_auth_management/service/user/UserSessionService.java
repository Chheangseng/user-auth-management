package com.tcs.user_auth_management.service.user;

import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.DtoUserSession;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSession;
import com.tcs.user_auth_management.repository.UserSessionRepository;
import com.tcs.user_auth_management.repository.specification.UserSessionSpec;
import com.tcs.user_auth_management.util.pagination.PaginationParam;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserSessionService {
  private final UserSessionRepository repository;
  private final UserRequestInfoService requestInfoService;

  public Page<DtoUserSession> userSessionPage(PaginationParam pagination) {
    var response = repository.findAll(pagination.toPageable());
    return response.map(DtoUserSession::new);
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
    // Check if we already rotated this token very recently
    // reason to check 30sec because if user have fail connection they can retry to get new jwt
    // token
    // without have to Re-login again
    boolean isInGracePeriod =
        session.getUpdateJwtTokenIdAt() != null
            && session.getUpdateJwtTokenIdAt().isAfter(now.minusSeconds(30));

    if (!isInGracePeriod) {
      // generate new jwt token id so user can't use their old token
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

  public void invokeSessionAllByUserAuthId(UUID userAuthId) {
    repository.update(UserSessionSpec.invokeAllSessionByUserAuthId(userAuthId));
  }

  public UserSession getUserSessionByJwtId(UUID jwtId) {
    return repository
        .findByJwtId(jwtId)
        .orElseThrow(
            () -> new ApiExceptionStatusException("Invalid Jwt token", HttpStatus.BAD_REQUEST));
  }
}
