package com.tcs.user_auth_management.controller.oidc;

import com.tcs.user_auth_management.config.oidc.OidcProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known")
@AllArgsConstructor
@Tag(name = "OIDC Discovery", description = "OpenID Connect Discovery 1.0 metadata")
public class OidcDiscoveryController {
  private final OidcProperties oidcProperties;

  @GetMapping("/openid-configuration")
  @Operation(summary = "OIDC Discovery Document", description = "Returns OpenID Connect provider configuration metadata")
  public ResponseEntity<Map<String, Object>> getOpenIdConfiguration() {
    String issuer = oidcProperties.issuer();
    Map<String, Object> config = new LinkedHashMap<>();
    config.put("issuer", issuer);
    config.put("authorization_endpoint", issuer + "/oauth2/authorize");
    config.put("token_endpoint", issuer + "/oauth2/token");
    config.put("userinfo_endpoint", issuer + "/oauth2/userinfo");
    config.put("jwks_uri", issuer + "/.well-known/jwks.json");
    config.put("introspection_endpoint", issuer + "/oauth2/introspect");
    config.put("revocation_endpoint", issuer + "/oauth2/revoke");

    config.put("scopes_supported", List.of(
        "openid",
        "profile",
        "email",
        "address",
        "phone",
        "offline_access"
    ));

    config.put("response_types_supported", List.of("code"));
    config.put("response_modes_supported", List.of("query"));
    config.put("grant_types_supported", List.of(
        "authorization_code",
        "client_credentials",
        "refresh_token"
    ));

    config.put("subject_types_supported", List.of("public"));
    config.put("id_token_signing_alg_values_supported", List.of("RS256"));
    config.put("token_endpoint_auth_methods_supported", List.of(
        "client_secret_basic",
        "client_secret_post",
        "none"
    ));
    config.put("code_challenge_methods_supported", List.of("S256", "plain"));
    config.put("claims_supported", List.of(
        "sub",
        "iss",
        "aud",
        "exp",
        "iat",
        "auth_time",
        "nonce",
        "name",
        "preferred_username",
        "email",
        "email_verified",
        "picture",
        "phone_number",
        "address",
        "locale",
        "zoneinfo",
        "updated_at"
    ));

    return ResponseEntity.ok(config);
  }
}
