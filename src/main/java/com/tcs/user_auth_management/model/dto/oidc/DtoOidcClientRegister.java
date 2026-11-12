package com.tcs.user_auth_management.model.dto.oidc;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DtoOidcClientRegister(
    @NotBlank(message = "clientName is required")
    @Schema(description = "Human-readable client application name", example = "My Frontend App")
    String clientName,

    @NotEmpty(message = "At least one redirectUri is required")
    @Schema(description = "Allowed redirect URIs", example = "[\"http://localhost:3000/callback\"]")
    List<String> redirectUris,

    @Schema(description = "Allowed scopes", example = "[\"openid\", \"profile\", \"email\"]")
    List<String> allowedScopes,

    @Schema(description = "Allowed grant types", example = "[\"authorization_code\", \"refresh_token\"]")
    List<String> grantTypes,

    @Schema(description = "Whether this is a public client (e.g. SPA, mobile) without client secret", defaultValue = "false")
    boolean publicClient,

    @Schema(description = "Whether PKCE is strictly required for this client", defaultValue = "true")
    boolean requirePkce,

    @Schema(description = "Access token validity in seconds", defaultValue = "3600")
    Long accessTokenValiditySeconds,

    @Schema(description = "Refresh token validity in seconds", defaultValue = "86400")
    Long refreshTokenValiditySeconds
) {}
