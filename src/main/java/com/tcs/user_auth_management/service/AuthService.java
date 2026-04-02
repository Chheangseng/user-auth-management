package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.config.taskConfig.VirtualExecutor;
import com.tcs.user_auth_management.emuns.AuditLogEvent;
import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.DtoJwtClaim;
import com.tcs.user_auth_management.model.dto.DtoJwtTokenResponse;
import com.tcs.user_auth_management.model.dto.DtoUserRequestInfo;
import com.tcs.user_auth_management.model.dto.user.DtoChangePassword;
import com.tcs.user_auth_management.model.dto.user.DtoResetPassword;
import com.tcs.user_auth_management.model.dto.user.DtoUserLogin;
import com.tcs.user_auth_management.model.dto.user.DtoUserRegister;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.model.mapper.UserAuthMapper;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import com.tcs.user_auth_management.service.user.UserActivityService;
import com.tcs.user_auth_management.service.user.UserRequestInfoService;
import com.tcs.user_auth_management.service.user.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthService {
  private final UserRequestInfoService requestInfoService;
  private final UserAuthRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final TokenJwtService tokenService;
  private final MailService mailService;
  private final UserAuthMapper userAuthMapper;
  private final UserActivityService activityService;
  private final HttpServletRequest request;
  private final UserSessionService userSessionService;
  private final OneTimeTokenService oneTimeTokenService;
  private final TokenJwtVerifyService jwtVerifyService;
  private final VirtualExecutor executor;

  @Transactional
  public DtoJwtTokenResponse loginUser(DtoUserLogin login) {
    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    var user = this.authenticationUsernameAndPassword(login, requestInfo);
    var session = userSessionService.createSession(user, tokenService.getExpireInSecondsRefresh());
    return tokenService.generateToken(user, session);
  }

  @Transactional
  public DtoJwtTokenResponse registerAccount(DtoUserRegister register) {
    validateUserDuplication(register);
    UserAuth userAuth = userAuthMapper.toEntity(register, passwordEncoder);
    repository.save(userAuth);
    var session =
        userSessionService.createSession(userAuth, tokenService.getExpireInSecondsRefresh());
    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    activityService.saveAudit(requestInfo, userAuth.getId(), AuditLogEvent.USER_REGISTRATION);
    return tokenService.generateToken(userAuth, session);
  }

  @Transactional
  public void changePassword(UUID userId, DtoChangePassword changePassword) {
    var user = isUserActive(userId);
    if (!passwordEncoder.matches(changePassword.oldPassword(), user.getPassword())) {
      throw new ApiExceptionStatusException("Incorrect old password", HttpStatus.UNAUTHORIZED);
    }
    user.setPassword(passwordEncoder.encode(changePassword.newPassword()));
    repository.save(user);
    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    activityService.saveAudit(requestInfo, user.getId(), AuditLogEvent.PASSWORD_RESET_BY_OLD_PASSWORD);
  }

  public UserSecurity authenticationCheck(Jwt source) {
    UUID sessionId = DtoJwtClaim.getSessionId(source);
    UUID jwtId = DtoJwtClaim.getJwtId(source);
    UUID userId = DtoJwtClaim.getUserId(source);
    userSessionService.verifyUserSession(sessionId, jwtId);
    var user =
        repository
            .findByIdWithRole(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid user") {});
    if (!user.isEnabled()) {
      throw new DisabledException("This account have been disable");
    }
    return new UserSecurity(user, jwtId, sessionId);
  }

  @Transactional
  public void logout(String refreshToken, boolean logoutAll) {
    Jwt jwt = jwtVerifyService.verifyToken(refreshToken, JwtTokenType.REFRESH);
    UserAuth userAuth = isUserActive(DtoJwtClaim.getUserId(jwt));

    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);

    if (logoutAll) {
      userSessionService.invokeSessionAllByUserAuthId(DtoJwtClaim.getUserId(jwt));
      activityService.saveAudit(requestInfo, userAuth.getId(), AuditLogEvent.LOGOUT_ALL_SESSION);
    } else {
      userSessionService.invokeSession(DtoJwtClaim.getJwtId(jwt));
      activityService.saveAudit(requestInfo, userAuth.getId(), AuditLogEvent.LOGOUT);
    }
  }

  @Transactional
  public void resetUserPassword(DtoResetPassword resetPassword) {
    var jwt =
        oneTimeTokenService.useOneTimeToken(
            resetPassword.resetToken(), JwtTokenType.RESET_PASSWORD);
    UserAuth userAuth = isUserActive(DtoJwtClaim.getUserId(jwt));
    userAuth.setPassword(passwordEncoder.encode(resetPassword.newPassword()));
    userAuth.setEmailVerified(true);
    repository.save(userAuth);
    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    activityService.saveAudit(requestInfo, userAuth.getId(), AuditLogEvent.PASSWORD_RESET_BY_EMAIL);
  }

  @Transactional
  public void verifyUserEmail(String verifyToken) {
    var jwt = oneTimeTokenService.useOneTimeToken(verifyToken, JwtTokenType.VERIFY_EMAIL);
    UserAuth userAuth = isUserActive(DtoJwtClaim.getUserId(jwt));
    userAuth.setEmailVerified(true);
    repository.save(userAuth);
    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    activityService.saveAudit(requestInfo, userAuth.getId(), AuditLogEvent.VERIFY_EMAIL);
  }

  public void sendVerifyEmailToken(UUID userId) {
    UserAuth userAuth = isUserActive(userId);
    mailService.asyncSendEmailVerify(
        userAuth.getUsername(),
        userAuth.getRecoveryEmail(),
        oneTimeTokenService.verifyEmailToken(userAuth));
  }

  @Transactional
  public DtoJwtTokenResponse refreshToken(String refreshToken) {
    var jwt = jwtVerifyService.verifyToken(refreshToken, JwtTokenType.REFRESH);
    UserAuth userAuth = isUserActive(DtoJwtClaim.getUserId(jwt));
    var session =
        userSessionService.rotateSessionToken(
            DtoJwtClaim.getJwtId(jwt), tokenService.getExpireInSecondsRefresh());
    return tokenService.generateToken(userAuth, session);
  }

  public void forgotPassword(String email) {
    repository
        .findByRecoveryEmail(email)
        .ifPresent(
            userAuth -> {
              mailService.asyncSendForgotPassword(
                  email, userAuth.getUsername(), oneTimeTokenService.resetToken(userAuth));
              DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
              activityService.saveAudit(requestInfo, userAuth.getId(), AuditLogEvent.SEND_FORGOT_PASSWORD_EMAIL);
            });
  }

  public UserAuth authenticationUsernameAndPassword(
      DtoUserLogin login, DtoUserRequestInfo requestInfo) {
    var user = this.isUserActiveByUsername(login.username());
    if (!passwordEncoder.matches(login.password(), user.getPassword())) {
      activityService.asyncSaveAudit(requestInfo, user.getId(), AuditLogEvent.LOGIN_FAILURE);
      throw new ApiExceptionStatusException(
          "Invalid username or password.", HttpStatus.UNAUTHORIZED);
    }
    activityService.asyncSaveAudit(requestInfo, user.getId(), AuditLogEvent.LOGIN_SUCCESS);
    return user;
  }

  public UserAuth isUserActive(UUID userId) {
    var user =
        repository
            .findById(userId)
            .orElseThrow(
                () -> new ApiExceptionStatusException("User not found", HttpStatus.NOT_FOUND));
    if (!user.isEnabled()) {
      throw new ApiExceptionStatusException(
          "Your account have been locked.", HttpStatus.UNAUTHORIZED);
    }
    return user;
  }

  public UserAuth isUserActiveByUsername(String username) {
    var user = findByUsername(username);
    if (!user.isEnabled()) {
      throw new ApiExceptionStatusException(
          "Your account have been locked.", HttpStatus.UNAUTHORIZED);
    }
    return user;
  }

  public UserAuth findByUsername(String username) {
    return this.repository
        .findByUsername(username)
        .orElseThrow(
            () -> new ApiExceptionStatusException("Invalid username", HttpStatus.UNAUTHORIZED));
  }

  private void validateUserDuplication(DtoUserRegister register) {
    CompletableFuture<Boolean> usernameExistsFuture =
        CompletableFuture.supplyAsync(
            () -> repository.existsByUsername(register.username()), executor);

    CompletableFuture<Boolean> emailExistsFuture =
        CompletableFuture.supplyAsync(() -> repository.existsByRecoveryEmail(register.email()), executor);

    CompletableFuture.allOf(usernameExistsFuture, emailExistsFuture).join();

    try {
      boolean usernameExists = usernameExistsFuture.get();
      boolean emailExists = emailExistsFuture.get();

      if (usernameExists) {
        throw new ApiExceptionStatusException(
            String.format("This username %s has already been used", register.username()),
            HttpStatus.BAD_REQUEST);
      }

      if (emailExists) {
        throw new ApiExceptionStatusException(
            String.format("This Email %s has already been used", register.email()),
            HttpStatus.BAD_REQUEST);
      }

    } catch (InterruptedException | ExecutionException e) {
      throw new ApiExceptionStatusException(
          "Error checking duplicate user info", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
