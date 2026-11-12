package com.tcs.user_auth_management.service.oidc;

import com.tcs.user_auth_management.exception.ApiExceptionStatusException;
import com.tcs.user_auth_management.model.dto.oidc.DtoUserInfoResponse;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserInfoService {
  private final UserAuthRepository userAuthRepository;

  public DtoUserInfoResponse getUserInfo(UUID userId, String scopeString) {
    UserAuth user = userAuthRepository.findById(userId)
        .orElseThrow(() -> new ApiExceptionStatusException("User not found", HttpStatus.NOT_FOUND));

    Set<String> scopes = (scopeString == null || scopeString.isBlank())
        ? Collections.emptySet()
        : Arrays.stream(scopeString.split(" ")).collect(Collectors.toSet());

    String sub = user.getId().toString();
    String name = null;
    String preferredUsername = null;
    String picture = null;
    String locale = null;
    String zoneinfo = null;
    Long updatedAt = null;
    String email = null;
    Boolean emailVerified = null;
    String phoneNumber = null;
    String address = null;

    if (scopes.contains("profile") || scopes.isEmpty()) {
      preferredUsername = user.getUsername();
      name = user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
      picture = user.getProfilePictureUrl();
      locale = user.getLocale();
      zoneinfo = user.getZoneinfo();
      if (user.getUpdatedAt() != null) {
        updatedAt = user.getUpdatedAt().getEpochSecond();
      }
    }

    if (scopes.contains("email") || scopes.isEmpty()) {
      email = user.getRecoveryEmail();
      emailVerified = user.isEmailVerified();
    }

    if (scopes.contains("phone") || scopes.isEmpty()) {
      phoneNumber = user.getPhoneNumber();
    }

    if (scopes.contains("address") || scopes.isEmpty()) {
      address = user.getAddress();
    }

    return new DtoUserInfoResponse(
        sub,
        name,
        preferredUsername,
        email,
        emailVerified,
        picture,
        phoneNumber,
        address,
        locale,
        zoneinfo,
        updatedAt
    );
  }
}
