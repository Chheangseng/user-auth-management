package com.tcs.user_auth_management.model.dto.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tcs.user_auth_management.model.entity.oidc.OidcClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DtoOidcClientResponse(
    UUID id,
    String clientId,
    String clientSecret, // Only returned on registration or explicit reset
    String clientName,
    List<String> redirectUris,
    List<String> allowedScopes,
    List<String> grantTypes,
    boolean publicClient,
    boolean requirePkce,
    boolean enabled,
    long accessTokenValiditySeconds,
    long refreshTokenValiditySeconds
) {
  public static DtoOidcClientResponse fromEntity(OidcClient client, String rawSecret) {
    return new DtoOidcClientResponse(
        client.getId(),
        client.getClientId(),
        rawSecret,
        client.getClientName(),
        new ArrayList<>(client.getRedirectUriSet()),
        new ArrayList<>(client.getAllowedScopeSet()),
        new ArrayList<>(client.getGrantTypeSet()),
        client.isPublicClient(),
        client.isRequirePkce(),
        client.isEnabled(),
        client.getAccessTokenValiditySeconds(),
        client.getRefreshTokenValiditySeconds()
    );
  }
}
