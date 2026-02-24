package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.AuditLogEvent;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.DtoJwtTokenResponse;
import com.tcs.user_auth_management.model.dto.user.DtoResetPassword;
import com.tcs.user_auth_management.model.dto.user.DtoUserLogin;
import com.tcs.user_auth_management.model.dto.user.DtoUserRegister;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.model.mapper.UserAuthMapper;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import com.tcs.user_auth_management.service.user.UserRequestInfoService;
import com.tcs.user_auth_management.service.user.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
  private final AuditLogService auditLogService;
  private final UserSessionService userSessionService;
  private final HttpServletRequest request;
  private final Executor executor;
  private final CacheService cacheService;

  public DtoJwtTokenResponse loginUser(DtoUserLogin login) {
    var user = this.authenticationUsernameAndPassword(login);
    return tokenService.generateTokenJwt(this.authenticationUser(user));
  }

  @Transactional
  public DtoJwtTokenResponse registerAccount(DtoUserRegister register) {
    validateUserDuplication(register);
    UserAuth userAuth = userAuthMapper.toEntity(register, passwordEncoder);
    repository.save(userAuth);
    auditLogService.auditEvent(
        AuditLogEvent.USER_REGISTRATION, requestInfoService.userRequestInfo(request));
    return tokenService.generateTokenJwt(this.authenticationUser(userAuth));
  }

  public void logout(String refreshToken) {
    auditLogoutUserAccount(refreshToken);
    CompletableFuture.runAsync(() -> userSessionService.invokeToken(refreshToken), executor);
  }

  public void logoutAll(String refreshToken) {
    auditLogoutUserAccount(refreshToken);
    CompletableFuture.runAsync(() -> userSessionService.invokeAllToken(refreshToken), executor);
  }

  private void auditLogoutUserAccount(String refreshToken) {
    Jwt jwt = tokenService.verifyRefreshToken(refreshToken);
    UserAuth userAuth = checkUserActiveById(jwt.getSubject());
    auditLogService.auditEvent(
        userAuth.getId(), AuditLogEvent.LOGOUT, requestInfoService.userRequestInfo(request));
  }

  public void resetUserPassword(DtoResetPassword resetPassword) {
    Jwt jwt = tokenService.verifyResetPasswordToken(resetPassword.resetToken());
    UserAuth userAuth = checkUserActiveById(jwt.getSubject());
    userAuth.setPassword(passwordEncoder.encode(resetPassword.newPassword()));
    userAuth.setEmailVerified(true);
    repository.save(userAuth);
  }

  public void verifyUserEmail(String verifyToken) {
    Jwt jwt = tokenService.verifyEmailToken(verifyToken);
    UserAuth userAuth = findByUsername(jwt.getSubject());
    userAuth.setEmailVerified(true);
    repository.save(userAuth);
  }

  public void sendVerifyEmailToken(String token) {
    Jwt jwt = tokenService.verifyToken(token);
    UserAuth userAuth = checkUserActiveById(jwt.getSubject());
    mailService.asyncSendEmailVerify(
        userAuth.getUsername(),
        userAuth.getEmail(),
        tokenService.generateVerifyEmailToken(this.authenticationUser(userAuth)));
  }

  public DtoJwtTokenResponse refreshToken(String refreshToken) {
    Jwt jwt = tokenService.verifyRefreshToken(refreshToken);
    UserAuth userAuth = checkUserActiveById(jwt.getSubject());
    return tokenService.generateTokenJwt(this.authenticationUser(userAuth), jwt);
  }

  public void forgotPassword(String email) {
    Optional<UserAuth> userOptional = repository.findByEmail(email);
    if (userOptional.isEmpty()) return;
    UserAuth userAuth = userOptional.get();
    String resetPasswordToken = this.tokenService.resetToken(this.authenticationUser(userAuth));
    mailService.asyncSendForgotPassword(email, userAuth.getUsername(), resetPasswordToken);
  }

  public UserAuth authenticationUsernameAndPassword(DtoUserLogin login) {
    var user = this.checkUserActiveByUsername(login.username());
    if (!passwordEncoder.matches(login.password(), user.getPassword())) {
      auditLogService.auditEvent(
          user.getId(), AuditLogEvent.LOGIN_FAILURE, requestInfoService.userRequestInfo(request));
      throw new ApiExceptionStatusException(
          "Invalid username or password.", HttpStatus.UNAUTHORIZED.value());
    }
    auditLogService.auditEvent(
        user.getId(), AuditLogEvent.LOGIN_SUCCESS, requestInfoService.userRequestInfo(request));
    return user;
  }

  public UserAuth checkUserActiveByUsername(String username) {
    var user = this.findByUsername(username);
    if (!user.isActivate()) {
      throw new ApiExceptionStatusException(
              "Your account have been locked.", HttpStatus.UNAUTHORIZED.value());
    }
    return user;
  }

  public UserAuth checkUserActiveById(String userId) {
    var user =
        repository
            .findById(userId)
            .orElseThrow(
                () -> new ApiExceptionStatusException("User not found", HttpStatus.NOT_FOUND));
    if (!user.isActivate()) {
      throw new ApiExceptionStatusException(
          "Your account have been locked.", HttpStatus.UNAUTHORIZED.value());
    }
    return user;
  }

  public UserAuth findByUsername(String username) {
    return cacheService.getCachedOrFetch(
        "username:" + username,
        UserAuth.class,
        () ->
            repository
                .findByUsername(username)
                .orElseThrow(
                    () -> new ApiExceptionStatusException("Invalid username: " + username, 401)));
  }

  private Authentication authenticationUser(UserAuth user) {
    return UserSecurity.getAuthenticationByUserAuth(user);
  }

  private void validateUserDuplication(DtoUserRegister register) {
    CompletableFuture<Boolean> usernameExistsFuture =
        CompletableFuture.supplyAsync(
            () -> repository.existsByUsername(register.username()), executor);

    CompletableFuture<Boolean> emailExistsFuture =
        CompletableFuture.supplyAsync(() -> repository.existsByEmail(register.email()), executor);

    CompletableFuture.allOf(usernameExistsFuture, emailExistsFuture).join();

    try {
      boolean usernameExists = usernameExistsFuture.get();
      boolean emailExists = emailExistsFuture.get();

      if (usernameExists) {
        throw new ApiExceptionStatusException(
            String.format("This username %s has already been used", register.username()), 400);
      }

      if (emailExists) {
        throw new ApiExceptionStatusException(
            String.format("This Email %s has already been used", register.email()), 400);
      }

    } catch (InterruptedException | ExecutionException e) {
      throw new ApiExceptionStatusException("Error checking duplicate user info", 500);
    }
  }
}
