package com.tcs.user_auth_management.service.user;

import com.tcs.user_auth_management.emuns.AuthenticationStatus;
import com.tcs.user_auth_management.model.dto.DtoUserRequestInfo;
import com.tcs.user_auth_management.model.entity.LoginAudit;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.mapper.LoginAuditMapper;
import com.tcs.user_auth_management.repository.LoginAuditRepository;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import com.tcs.user_auth_management.util.GeoUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserActivityService {
  private final LoginAuditRepository repository;
  private final LoginAuditMapper mapper;
  private final UserAuthRepository authRepository;

  @Async
  public void asyncLoginSuccess(DtoUserRequestInfo request, String authId) {
    try{
      authRepository
              .findById(authId)
              .ifPresent(
                      userAuth -> saveAudit(request, userAuth, AuthenticationStatus.SUCCESS, true));
    } catch (Exception e) {
      log.warn("Fail to save Audit logging for authId={}", authId, e);
    }
  }

  @Async
  public void asyncLoginFail(DtoUserRequestInfo request, String authId) {
    try{
      authRepository
              .findById(authId)
              .ifPresent(
                      userAuth -> saveAudit(request, userAuth, AuthenticationStatus.FAILURE, true));
    } catch (Exception e) {
      log.warn("Fail to save Audit logging for authId={}", authId, e);
    }
  }

  @Async
  public void asyncLogout(DtoUserRequestInfo request, UserAuth userAuth) {
    saveAudit(request, userAuth, AuthenticationStatus.SUCCESS, false);
  }

  private void saveAudit(
          DtoUserRequestInfo request, UserAuth userAuth, AuthenticationStatus status, boolean isLogin) {
    LoginAudit audit = mapper.toEntity(request);
    audit.setUserAuthId(userAuth.getId());
    audit.setStatus(status);

    if (isLogin) {
      audit.setLoginTime(LocalDateTime.now());
    } else {
      audit.setLogoutTime(LocalDateTime.now());
    }
    repository.save(audit);
    validationLoginRisk(userAuth);
  }

  private void validationLoginRisk(UserAuth userAuth) {
    List<LoginAudit> loginAudits =
        repository.findTop2ByUserAuthIdOrderByLoginTimeDesc(userAuth.getId().toString());
    if (loginAudits.size() == 2) {
      int risk = calculateRisk(loginAudits.get(0), loginAudits.get(1));
      userAuth.setRisk(risk);
      authRepository.save(userAuth);
    }
  }

  private int calculateRisk(LoginAudit current, LoginAudit previous) {
    int risk = 0;

    // New location
    if (!Objects.equals(current.getCountryCode(), previous.getCountryCode())) {
      risk += 20;
    }

    // Impossible travel (distance & time)
    double km =
        GeoUtils.distanceKm(
            current.getLatitude(), current.getLongitude(),
            previous.getLatitude(), previous.getLongitude());

    long hours = Duration.between(previous.getLoginTime(), current.getLoginTime()).toHours();

    if (hours > 0 && km / hours > 800) { // >800 km/h ~ impossible travel
      risk += 30;
    }

    // New device / user agent
    if (!Objects.equals(current.getUserAgent(), previous.getUserAgent())) {
      risk += 25;
    }

    // Odd login time (e.g., user usually logs in daytime)
    int hour = current.getLoginTime().getHour();

    if (hour < 6) {
      risk += 10;
    }

    // Multiple failures before success
    if (AuthenticationStatus.FAILURE.equals(current.getStatus())) {
      risk += 15;
    }

    return Math.min(risk, 100);
  }
}
