package com.tcs.user_auth_management.model.dto.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DtoOidcTokenResponse(
    @JsonProperty("access_token")
    @Schema(description = "OAuth2 access token")
    String accessToken,

    @JsonProperty("token_type")
    @Schema(description = "Token type", example = "Bearer")
    String tokenType,

    @JsonProperty("expires_in")
    @Schema(description = "Access token lifetime in seconds")
    long expiresIn,

    @JsonProperty("refresh_token")
    @Schema(description = "OAuth2 refresh token")
    String refreshToken,

    @JsonProperty("id_token")
    @Schema(description = "OIDC ID token (signed JWT)")
    String idToken,

    @JsonProperty("scope")
    @Schema(description = "Granted scopes")
    String scope
) {
  public static DtoOidcTokenResponse of(
      String accessToken,
      long expiresIn,
      String refreshToken,
      String idToken,
      String scope) {
    return new DtoOidcTokenResponse(accessToken, "Bearer", expiresIn, refreshToken, idToken, scope);
  }
}
