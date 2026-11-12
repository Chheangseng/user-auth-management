package com.tcs.user_auth_management.model.dto.oidc;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DtoAuthorizationRequest(
    @NotBlank(message = "response_type is required")
    @Schema(description = "Response type, e.g. code", example = "code")
    String responseType,

    @NotBlank(message = "client_id is required")
    @Schema(description = "OIDC client ID")
    String clientId,

    @NotBlank(message = "redirect_uri is required")
    @Schema(description = "Client redirect URI")
    String redirectUri,

    @Schema(description = "Space-separated scopes, e.g. 'openid profile email'", example = "openid profile email")
    String scope,

    @Schema(description = "Opaque state value to prevent CSRF")
    String state,

    @Schema(description = "String value used to associate a Client session with an ID Token")
    String nonce,

    @Schema(description = "PKCE code challenge")
    String codeChallenge,

    @Schema(description = "PKCE code challenge method: S256 or plain", example = "S256")
    String codeChallengeMethod
) {}
