package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.entity.UserSession;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.repository.UserSessionRepository;
import com.tcs.user_auth_management.service.user.UserRequestInfoService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserSessionService {
  private final UserSessionRepository repository;
  private final HttpServletRequest request;
  private final UserRequestInfoService requestInfoService;

  public UserSession createNewSession(UserAuth userAuth, Instant expireTime) {
    UserSession session = new UserSession();
    session.setUserAuth(userAuth);
    session.setExpiryDate(expireTime);
    session.setInvoked(false);
    var info = requestInfoService.userRequestInfo(request);
    session.setUserAgent(info.getUserAgent());
    session.setLocation(info.getLocation());
    session.setIpAddress(info.getIp());
    return repository.save(session);
  }

  public UserSession updateSessionExpiredTime(UUID sessionId, Instant expireTime) {
    UserSession session =
        repository
            .findById(sessionId)
            .orElseThrow(
                () -> new ApiExceptionStatusException("Invalid session Id", HttpStatus.NOT_FOUND));
    session.setExpiryDate(expireTime);
    var info = requestInfoService.userRequestInfo(request);
    session.setUserAgent(info.getUserAgent());
    session.setLocation(info.getLocation());
    session.setIpAddress(info.getIp());
    return repository.save(session);
  }
}
