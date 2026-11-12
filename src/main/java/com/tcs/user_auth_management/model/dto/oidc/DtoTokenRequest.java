package com.tcs.user_auth_management.model.dto.oidc;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DtoTokenRequest(
    @NotBlank(message = "grant_type is required")
    @Schema(description = "OAuth2 grant type", example = "authorization_code")
    String grantType,

    @Schema(description = "Authorization code for authorization_code grant")
    String code,

    @Schema(description = "Redirect URI matching the authorization request")
    String redirectUri,

    @Schema(description = "Client ID")
    String clientId,

    @Schema(description = "Client secret (for confidential clients)")
    String clientSecret,

    @Schema(description = "PKCE code verifier")
    String codeVerifier,

    @Schema(description = "Refresh token for refresh_token grant")
    String refreshToken,

    @Schema(description = "Requested scope for client_credentials or refresh_token")
    String scope
) {}
