package com.tcs.user_auth_management.service;

import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.DtoJwtTokenResponse;
import com.tcs.user_auth_management.model.dto.DtoUserRequestInfo;
import com.tcs.user_auth_management.model.dto.user.DtoResetPassword;
import com.tcs.user_auth_management.model.dto.user.DtoUserLogin;
import com.tcs.user_auth_management.model.dto.user.DtoUserRegister;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.model.mapper.UserAuthMapper;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import com.tcs.user_auth_management.service.user.UserActivityService;
import com.tcs.user_auth_management.service.user.UserRequestInfoService;
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
  private final UserActivityService activityService;
  private final RefreshTokenSessionService refreshTokenSessionService;
  private final HttpServletRequest request;
  private final Executor executor;
  private final CacheService cacheService;

  public DtoJwtTokenResponse loginUser(DtoUserLogin login) {
    var user = this.authenticationUsernameAndPassword(login);
    return tokenService.generateToken(this.authenticationUser(user));
  }

  @Transactional
  public DtoJwtTokenResponse registerAccount(DtoUserRegister register) {
    validateUserDuplication(register);
    UserAuth userAuth = userAuthMapper.toEntity(register, passwordEncoder);
    repository.save(userAuth);
    return tokenService.generateToken(this.authenticationUser(userAuth));
  }

  public void logout(String refreshToken) {
    auditLogoutUserAccount(refreshToken);
    CompletableFuture.runAsync(
        () -> refreshTokenSessionService.invokeToken(refreshToken), executor);
  }

  public void logoutAll(String refreshToken) {
    auditLogoutUserAccount(refreshToken);
    CompletableFuture.runAsync(
        () -> refreshTokenSessionService.invokeAllToken(refreshToken), executor);
  }

  private void auditLogoutUserAccount(String refreshToken) {
    Jwt jwt = tokenService.verifyRefreshToken(refreshToken);
    UserAuth userAuth = isUserActive(jwt.getSubject());
    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    activityService.asyncLogout(requestInfo, userAuth);
  }

  public void resetUserPassword(DtoResetPassword resetPassword) {
    Jwt jwt = tokenService.verifyResetPasswordToken(resetPassword.resetToken());
    UserAuth userAuth = isUserActive(jwt.getSubject());
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
    UserAuth userAuth = isUserActive(jwt.getSubject());
    mailService.asyncSendEmailVerify(
        userAuth.getUsername(),
        userAuth.getEmail(),
        tokenService.generateVerifyEmailToken(this.authenticationUser(userAuth)));
  }

  public DtoJwtTokenResponse refreshToken(String refreshToken) {
    Jwt jwt = tokenService.verifyRefreshToken(refreshToken);
    UserAuth userAuth = isUserActive(jwt.getSubject());
    return tokenService.refreshToken(this.authenticationUser(userAuth), jwt);
  }

  public void forgotPassword(String email) {
    Optional<UserAuth> userOptional = repository.findByEmail(email);
    if (userOptional.isEmpty()) return;
    UserAuth userAuth = userOptional.get();
    String resetPasswordToken = this.tokenService.resetToken(this.authenticationUser(userAuth));
    mailService.asyncSendForgotPassword(email, userAuth.getUsername(), resetPasswordToken);
  }

  public UserAuth authenticationUsernameAndPassword(DtoUserLogin login) {
    var user = this.isUserActive(login.username());
    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    if (!passwordEncoder.matches(login.password(), user.getPassword())) {
      activityService.asyncLoginFail(requestInfo, user.getId().toString());
      throw new ApiExceptionStatusException(
          "Invalid username or password.", HttpStatus.UNAUTHORIZED.value());
    }
    activityService.asyncLoginSuccess(requestInfo, user.getId().toString());
    return user;
  }

  public UserAuth isUserActive(String username) {
    var user = findByUsername(username);
    if (!user.isActivate()) {
      throw new ApiExceptionStatusException(
          "Your account have been locked.", HttpStatus.UNAUTHORIZED.value());
    }
    return user;
  }

  public UserAuth findByUsername(String username) {
    return cacheService.getCachedOrFetch("username:" + username, UserAuth.class, () ->
            repository.findByUsername(username)
                    .orElseThrow(() -> new ApiExceptionStatusException("Invalid username", 401))
    );
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
