package com.tcs.user_auth_management.service.oidc;

import com.tcs.user_auth_management.emuns.AuditLogEvent;
import com.tcs.user_auth_management.exception.AppException;
import com.tcs.user_auth_management.exception.errorcode.OidcErrorCode;
import com.tcs.user_auth_management.exception.errorcode.UserErrorCode;
import com.tcs.user_auth_management.model.dto.DtoUserRequestInfo;
import com.tcs.user_auth_management.model.dto.oidc.DtoAuthorizationRequest;
import com.tcs.user_auth_management.model.dto.oidc.DtoConsentResponse;
import com.tcs.user_auth_management.model.entity.oidc.AuthorizationCode;
import com.tcs.user_auth_management.model.entity.oidc.OidcClient;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.repository.AuthorizationCodeRepository;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import com.tcs.user_auth_management.service.user.UserActivityService;
import com.tcs.user_auth_management.service.user.UserRequestInfoService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@AllArgsConstructor
public class AuthorizationService {
  private final OidcClientService clientService;
  private final AuthorizationCodeRepository authCodeRepository;
  private final UserAuthRepository userAuthRepository;
  private final UserConsentService consentService;
  private final UserActivityService activityService;
  private final UserRequestInfoService requestInfoService;
  private final HttpServletRequest request;

  private static final SecureRandom secureRandom = new SecureRandom();

  @Transactional
  public String processAuthorization(DtoAuthorizationRequest authReq, UUID userAuthId) {
    if (!"code".equalsIgnoreCase(authReq.responseType())) {
      throw new AppException(OidcErrorCode.UNSUPPORTED_RESPONSE_TYPE, "Unsupported response_type: " + authReq.responseType());
    }

    OidcClient client = clientService.getClientByClientId(authReq.clientId());
    if (!client.isEnabled()) {
      throw new AppException(OidcErrorCode.CLIENT_DISABLED);
    }
    clientService.validateRedirectUri(client, authReq.redirectUri());
    clientService.validateGrantType(client, "authorization_code");

    if (client.isRequirePkce() && (authReq.codeChallenge() == null || authReq.codeChallenge().isBlank())) {
      throw new AppException(OidcErrorCode.PKCE_REQUIRED);
    }

    Set<String> validScopes = clientService.validateAndFilterScopes(client, authReq.scope());

    UserAuth user = userAuthRepository.findById(userAuthId)
        .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

    boolean hasConsent = consentService.hasConsent(user.getId(), client.getId(), validScopes);
    if (!hasConsent) {
      consentService.grantConsent(user, client, validScopes);
      DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
      activityService.saveAudit(requestInfo, user.getId(), AuditLogEvent.OIDC_CONSENT_GRANTED);
    }

    AuthorizationCode authCode = createAuthorizationCode(
        client,
        user,
        authReq.redirectUri(),
        validScopes,
        authReq.codeChallenge(),
        authReq.codeChallengeMethod(),
        authReq.nonce(),
        authReq.state()
    );

    DtoUserRequestInfo requestInfo = requestInfoService.userRequestInfo(request);
    activityService.saveAudit(requestInfo, user.getId(), AuditLogEvent.OIDC_AUTHORIZATION);

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(authReq.redirectUri())
        .queryParam("code", authCode.getCode());
    if (authReq.state() != null && !authReq.state().isBlank()) {
      uriBuilder.queryParam("state", authReq.state());
    }

    return uriBuilder.build().toUriString();
  }

  public DtoConsentResponse prepareConsentInfo(DtoAuthorizationRequest authReq) {
    OidcClient client = clientService.getClientByClientId(authReq.clientId());
    clientService.validateRedirectUri(client, authReq.redirectUri());
    Set<String> scopes = clientService.validateAndFilterScopes(client, authReq.scope());

    return new DtoConsentResponse(
        client.getClientId(),
        client.getClientName(),
        new ArrayList<>(scopes),
        authReq.state(),
        authReq.redirectUri()
    );
  }

  @Transactional
  public AuthorizationCode createAuthorizationCode(
      OidcClient client,
      UserAuth user,
      String redirectUri,
      Set<String> scopes,
      String codeChallenge,
      String codeChallengeMethod,
      String nonce,
      String state) {
    byte[] codeBytes = new byte[32];
    secureRandom.nextBytes(codeBytes);
    String code = Base64.getUrlEncoder().withoutPadding().encodeToString(codeBytes);

    AuthorizationCode authCode = new AuthorizationCode();
    authCode.setCode(code);
    authCode.setClient(client);
    authCode.setUserAuth(user);
    authCode.setRedirectUri(redirectUri);
    authCode.setScopes(String.join(" ", scopes));
    authCode.setCodeChallenge(codeChallenge);
    authCode.setCodeChallengeMethod(codeChallengeMethod);
    authCode.setNonce(nonce);
    authCode.setState(state);
    authCode.setExpiresAt(Instant.now().plusSeconds(client.getAuthorizationCodeValiditySeconds()));
    authCode.setUsed(false);

    return authCodeRepository.save(authCode);
  }

  @Transactional
  public AuthorizationCode consumeAuthorizationCode(String code) {
    AuthorizationCode authCode = authCodeRepository.findByCode(code)
        .orElseThrow(() -> new AppException(OidcErrorCode.INVALID_AUTHORIZATION_CODE));

    if (authCode.isUsed()) {
      throw new AppException(OidcErrorCode.CODE_ALREADY_USED);
    }

    if (authCode.getExpiresAt().isBefore(Instant.now())) {
      throw new AppException(OidcErrorCode.CODE_EXPIRED);
    }

    authCode.setUsed(true);
    authCodeRepository.save(authCode);
    return authCode;
  }
}
