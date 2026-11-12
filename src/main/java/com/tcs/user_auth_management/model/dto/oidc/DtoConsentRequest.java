package com.tcs.user_auth_management.model.dto.oidc;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DtoConsentRequest(
    @NotBlank(message = "client_id is required")
    @Schema(description = "OIDC client ID")
    String clientId,

    @NotEmpty(message = "approved_scopes must not be empty")
    @Schema(description = "List of approved scopes")
    List<String> approvedScopes,

    @Schema(description = "State parameter from the authorization request")
    String state,

    @NotBlank(message = "redirect_uri is required")
    @Schema(description = "Redirect URI from the authorization request")
    String redirectUri,

    @Schema(description = "PKCE code challenge")
    String codeChallenge,

    @Schema(description = "PKCE code challenge method")
    String codeChallengeMethod,

    @Schema(description = "Nonce")
    String nonce
) {}
