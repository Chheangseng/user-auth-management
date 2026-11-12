package com.tcs.user_auth_management.controller.oidc;

import com.tcs.user_auth_management.config.oidc.OidcProperties;
import com.tcs.user_auth_management.model.dto.oidc.DtoAuthorizationRequest;
import com.tcs.user_auth_management.model.dto.oidc.DtoConsentRequest;
import com.tcs.user_auth_management.model.dto.oidc.DtoConsentResponse;
import com.tcs.user_auth_management.model.entity.oidc.OidcClient;
import com.tcs.user_auth_management.model.entity.user.UserAuth;
import com.tcs.user_auth_management.model.entity.user.UserSecurity;
import com.tcs.user_auth_management.repository.UserAuthRepository;
import com.tcs.user_auth_management.service.oidc.AuthorizationService;
import com.tcs.user_auth_management.service.oidc.OidcClientService;
import com.tcs.user_auth_management.service.oidc.UserConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/oauth2")
@AllArgsConstructor
@Tag(name = "OIDC Authorization", description = "OAuth2 / OIDC Authorization endpoints")
public class AuthorizationController {
  private final AuthorizationService authorizationService;
  private final OidcClientService clientService;
  private final UserConsentService consentService;
  private final UserAuthRepository userAuthRepository;
  private final OidcProperties oidcProperties;

  @GetMapping("/authorize")
  @Operation(summary = "OIDC Authorization Endpoint", description = "Initiates Authorization Code Flow with PKCE")
  public ResponseEntity<?> authorize(@Valid @ModelAttribute DtoAuthorizationRequest authReq) {
    Optional<UserSecurity> currentUser = UserSecurity.getCurrentUser();

    if (currentUser.isEmpty()) {
      // If user is not authenticated, redirect to frontend login page with return parameter
      String returnUrl = UriComponentsBuilder.fromPath("/oauth2/authorize")
          .queryParam("response_type", authReq.responseType())
          .queryParam("client_id", authReq.clientId())
          .queryParam("redirect_uri", authReq.redirectUri())
          .queryParam("scope", authReq.scope())
          .queryParam("state", authReq.state())
          .queryParam("nonce", authReq.nonce())
          .queryParam("code_challenge", authReq.codeChallenge())
          .queryParam("code_challenge_method", authReq.codeChallengeMethod())
          .build().toUriString();

      String loginRedirectUrl = UriComponentsBuilder.fromUriString(oidcProperties.frontendUrl())
          .path("/login")
          .queryParam("return_url", returnUrl)
          .build().toUriString();

      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(URI.create(loginRedirectUrl));
      return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    String redirectTarget = authorizationService.processAuthorization(
        authReq,
        currentUser.get().getUserId()
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create(redirectTarget));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }

  @GetMapping("/consent-info")
  @Operation(summary = "Get consent dialog info", description = "Returns client and scope info for consent presentation")
  public ResponseEntity<DtoConsentResponse> getConsentInfo(@Valid @ModelAttribute DtoAuthorizationRequest authReq) {
    return ResponseEntity.ok(authorizationService.prepareConsentInfo(authReq));
  }

  @PostMapping("/consent")
  @Operation(summary = "Submit user consent", description = "Processes user consent decision")
  public ResponseEntity<?> submitConsent(@Valid @RequestBody DtoConsentRequest consentReq) {
    UserSecurity currentUser = UserSecurity.getRequiredCurrentUser();
    UserAuth user = userAuthRepository.findById(currentUser.getUserId())
        .orElseThrow();
    OidcClient client = clientService.getClientByClientId(consentReq.clientId());

    consentService.grantConsent(user, client, new HashSet<>(consentReq.approvedScopes()));

    DtoAuthorizationRequest authReq = new DtoAuthorizationRequest(
        "code",
        consentReq.clientId(),
        consentReq.redirectUri(),
        String.join(" ", consentReq.approvedScopes()),
        consentReq.state(),
        consentReq.nonce(),
        consentReq.codeChallenge(),
        consentReq.codeChallengeMethod()
    );

    String redirectTarget = authorizationService.processAuthorization(authReq, currentUser.getUserId());
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create(redirectTarget));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }
}
