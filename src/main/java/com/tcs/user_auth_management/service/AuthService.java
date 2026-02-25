package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.emuns.JwtTokenType;
import com.tcs.user_auth_management.emuns.Role;
import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.DtoJwtPayload;
import com.tcs.user_auth_management.model.dto.DtoJwtTokenResponse;
import com.tcs.user_auth_management.model.dto.DtoUserRequestInfo;
import com.tcs.user_auth_management.model.dto.user.DtoResetPassword;
import com.tcs.user_auth_management.model.dto.user.DtoUserLogin;
import com.tcs.user_auth_management.model.dto.user.DtoUserRegister;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.model.mapper.UserAuthMapper;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import com.tcs.user_auth_management.repository.UserSessionRepository;
import com.tcs.user_auth_management.service.user.UserActivityService;
import com.tcs.user_auth_management.service.user.UserRequestInfoService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
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
  private final UserSessionRepository userSessionRepository;
  private final MailService mailService;
  private final UserAuthMapper userAuthMapper;
  private final UserActivityService activityService;
  private final HttpServletRequest request;
  private final Executor executor;

  public DtoJwtTokenResponse loginUser(DtoUserLogin login) {
    var user = this.authenticationUsernameAndPassword(login);
    return tokenService.generateToken(user);
  }

  @Transactional
  public DtoJwtTokenResponse registerAccount(DtoUserRegister register) {
    validateUserDuplication(register);
    UserAuth userAuth = userAuthMapper.toEntity(register, passwordEncoder);
    userAuth.addRole(Role.USER);
    repository.save(userAuth);
    return tokenService.generateToken(userAuth);
  }

  public void logout(String refreshToken) {
    auditLogoutUserAccount(refreshToken);
    CompletableFuture.runAsync(
        () -> {
          var jwt = new DtoJwtPayload(this.tokenService.verifyRefreshToken(refreshToken));
          userSessionRepository.updateInvokedBySessionId(jwt.getSessionId(), true);
        },
        executor);
  }

  public void logoutAll(String refreshToken) {
    auditLogoutUserAccount(refreshToken);
    CompletableFuture.runAsync(
        () -> {
          var jwt = new DtoJwtPayload(this.tokenService.verifyRefreshToken(refreshToken));
          userSessionRepository.updateInvokedByUserAuthId(jwt.getUserId(), true);
        },
        executor);
  }

  private void auditLogoutUserAccount(String refreshToken) {
    Jwt jwt = tokenService.verifyRefreshToken(refreshToken);
    UserAuth userAuth = isUserActive(new DtoJwtPayload(jwt).getUserId());
    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    activityService.asyncLogout(requestInfo, userAuth);
  }

  public void resetUserPassword(DtoResetPassword resetPassword) {
    var jwt = tokenService.verifyTokenPayload(resetPassword.resetToken(), JwtTokenType.RESET_PASSWORD);
    UserAuth userAuth = isUserActive(jwt.getUserId());
    userAuth.setPassword(passwordEncoder.encode(resetPassword.newPassword()));
    userAuth.setEmailVerified(true);
    repository.save(userAuth);
  }

  public void verifyUserEmail(String verifyToken) {
    var jwt = tokenService.verifyTokenPayload(verifyToken, JwtTokenType.VERIFY_EMAIL);
    UserAuth userAuth = isUserActive(jwt.getUserId());
    userAuth.setEmailVerified(true);
    repository.save(userAuth);
  }

  public void sendVerifyEmailToken(UUID userId) {
    UserAuth userAuth = isUserActive(userId);
    mailService.asyncSendEmailVerify(
        userAuth.getUsername(),
        userAuth.getEmail(),
        tokenService.generateVerifyEmailToken(this.authenticationUser(userAuth)));
  }

  public DtoJwtTokenResponse refreshToken(String refreshToken) {
    var jwt = tokenService.verifyRefreshToken(refreshToken);
    var payload = new DtoJwtPayload(jwt);
    UserAuth userAuth = isUserActive(payload.getUserId());
    return tokenService.generateToken(userAuth, jwt);
  }

  public void forgotPassword(String email) {
    Optional<UserAuth> userOptional = repository.findByEmail(email);
    if (userOptional.isEmpty()) return;
    UserAuth userAuth = userOptional.get();
    String resetPasswordToken = this.tokenService.resetToken(this.authenticationUser(userAuth));
    mailService.asyncSendForgotPassword(email, userAuth.getUsername(), resetPasswordToken);
  }

  public UserAuth authenticationUsernameAndPassword(DtoUserLogin login) {
    var user = this.isUserActiveByUsername(login.username());
    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    if (!passwordEncoder.matches(login.password(), user.getPassword())) {
      activityService.asyncLoginFail(requestInfo, user.getId());
      throw new ApiExceptionStatusException(
          "Invalid username or password.", HttpStatus.UNAUTHORIZED.value());
    }
    activityService.asyncLoginSuccess(requestInfo, user.getId());
    return user;
  }

  public UserAuth isUserActive(UUID userId) {
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

  public UserAuth isUserActiveByUsername(String username) {
    var user = findByUsername(username);
    if (!user.isActivate()) {
      throw new ApiExceptionStatusException(
          "Your account have been locked.", HttpStatus.UNAUTHORIZED.value());
    }
    return user;
  }

  public UserAuth findByUsername(String username) {
    return this.repository
        .findByUsername(username)
        .orElseThrow(
            () ->
                new ApiExceptionStatusException(
                    "Invalid username", HttpStatus.UNAUTHORIZED.value()));
  }

  private Authentication authenticationUser(UserAuth user) {
    return UserSecurity.getAuthenticationByUserAuth(user);
  }

  private void validateUserDuplication(DtoUserRegister register) {
    // Run both checks concurrently

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
