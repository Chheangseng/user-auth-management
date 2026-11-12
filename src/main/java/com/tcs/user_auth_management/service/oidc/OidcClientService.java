package com.tcs.user_auth_management.service.oidc;

import com.fasterxml.uuid.Generators;
import com.tcs.user_auth_management.exception.AppException;
import com.tcs.user_auth_management.exception.errorcode.OidcErrorCode;
import com.tcs.user_auth_management.model.dto.oidc.DtoOidcClientRegister;
import com.tcs.user_auth_management.model.dto.oidc.DtoOidcClientResponse;
import com.tcs.user_auth_management.model.entity.oidc.OidcClient;
import com.tcs.user_auth_management.repository.OidcClientRepository;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OidcClientService {
  private final OidcClientRepository clientRepository;
  private final PasswordEncoder passwordEncoder;

  private static final SecureRandom secureRandom = new SecureRandom();

  @Transactional
  public DtoOidcClientResponse registerClient(DtoOidcClientRegister register) {
    OidcClient client = new OidcClient();
    client.setClientId(Generators.timeBasedEpochGenerator().generate().toString());
    client.setClientName(register.clientName());
    client.setPublicClient(register.publicClient());
    client.setRequirePkce(register.requirePkce());
    client.setEnabled(true);

    if (register.accessTokenValiditySeconds() != null && register.accessTokenValiditySeconds() > 0) {
      client.setAccessTokenValiditySeconds(register.accessTokenValiditySeconds());
    }
    if (register.refreshTokenValiditySeconds() != null && register.refreshTokenValiditySeconds() > 0) {
      client.setRefreshTokenValiditySeconds(register.refreshTokenValiditySeconds());
    }

    client.setRedirectUris(String.join(",", register.redirectUris()));

    List<String> scopes = (register.allowedScopes() == null || register.allowedScopes().isEmpty())
        ? List.of("openid", "profile", "email")
        : register.allowedScopes();
    client.setAllowedScopes(String.join(",", scopes));

    List<String> grantTypes = (register.grantTypes() == null || register.grantTypes().isEmpty())
        ? List.of("authorization_code", "refresh_token")
        : register.grantTypes();
    client.setGrantTypes(String.join(",", grantTypes));

    String rawSecret = null;
    if (!register.publicClient()) {
      byte[] secretBytes = new byte[32];
      secureRandom.nextBytes(secretBytes);
      rawSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
      client.setClientSecret(passwordEncoder.encode(rawSecret));
    }

    OidcClient saved = clientRepository.save(client);
    return DtoOidcClientResponse.fromEntity(saved, rawSecret);
  }

  public OidcClient getClientByClientId(String clientId) {
    return clientRepository.findByClientId(clientId)
        .orElseThrow(() -> new AppException(OidcErrorCode.INVALID_CLIENT, "Invalid client_id"));
  }

  public OidcClient authenticateClient(String clientId, String clientSecret) {
    OidcClient client = getClientByClientId(clientId);
    if (!client.isEnabled()) {
      throw new AppException(OidcErrorCode.CLIENT_DISABLED);
    }
    if (!client.isPublicClient()) {
      if (clientSecret == null || !passwordEncoder.matches(clientSecret, client.getClientSecret())) {
        throw new AppException(OidcErrorCode.INVALID_CLIENT);
      }
    }
    return client;
  }

  public void validateRedirectUri(OidcClient client, String redirectUri) {
    if (redirectUri == null || redirectUri.isBlank()) {
      throw new AppException(OidcErrorCode.INVALID_REDIRECT_URI, "redirect_uri is required");
    }
    Set<String> allowedUris = client.getRedirectUriSet();
    if (!allowedUris.contains(redirectUri)) {
      throw new AppException(OidcErrorCode.INVALID_REDIRECT_URI, "Invalid redirect_uri for this client");
    }
  }

  public Set<String> validateAndFilterScopes(OidcClient client, String scopeString) {
    if (scopeString == null || scopeString.isBlank()) {
      return Set.of("openid");
    }
    Set<String> requested = Arrays.stream(scopeString.split(" "))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());

    Set<String> allowed = client.getAllowedScopeSet();
    Set<String> validScopes = requested.stream()
        .filter(allowed::contains)
        .collect(Collectors.toSet());

    if (validScopes.isEmpty()) {
      throw new AppException(OidcErrorCode.INVALID_SCOPE);
    }
    return validScopes;
  }

  public void validateGrantType(OidcClient client, String grantType) {
    if (!client.getGrantTypeSet().contains(grantType)) {
      throw new AppException(OidcErrorCode.UNSUPPORTED_GRANT_TYPE, "Grant type not allowed for this client: " + grantType);
    }
  }

  public List<DtoOidcClientResponse> getAllClients() {
    return clientRepository.findAll().stream()
        .map(c -> DtoOidcClientResponse.fromEntity(c, null))
        .toList();
  }

  public DtoOidcClientResponse getClientDetails(UUID id) {
    OidcClient client = clientRepository.findById(id)
        .orElseThrow(() -> new AppException(OidcErrorCode.CLIENT_NOT_FOUND));
    return DtoOidcClientResponse.fromEntity(client, null);
  }

  @Transactional
  public void disableClient(UUID id) {
    OidcClient client = clientRepository.findById(id)
        .orElseThrow(() -> new AppException(OidcErrorCode.CLIENT_NOT_FOUND));
    client.setEnabled(false);
    clientRepository.save(client);
  }
}
