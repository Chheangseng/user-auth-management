package com.tcs.user_auth_management.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI customOpenAPI() {
    final String securitySchemeName = "bearerAuth";

    return new OpenAPI()
        .info(new Info().title("User Auth & OIDC API").description("Authentication and OpenID Connect Provider Documentation").version("1.0"))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }

  private GroupedOpenApi groupedOpenApi(String group, String... pathToMatch) {
    return GroupedOpenApi.builder()
            .group(group)
            .pathsToMatch(pathToMatch)
            .addOpenApiCustomizer(openApi -> openApi.setSecurity(new ArrayList<>()))
            .build();
  }

  private GroupedOpenApi groupedOpenApiJwt(String group, String... pathToMatch) {
    return GroupedOpenApi.builder()
            .group(group)
            .pathsToMatch(pathToMatch)
            .build();
  }

  @Bean
  GroupedOpenApi authentication() {
    return groupedOpenApi("Authentication", "/api/auth/**");
  }

  @Bean
  GroupedOpenApi userInformation() {
    return groupedOpenApiJwt("User Information", "/api/user/me/**");
  }

  @Bean
  GroupedOpenApi oauth2Endpoint() {
    return groupedOpenApi("OAuth2 & OIDC Public Endpoints", "/.well-known/**", "/oauth2/authorize", "/oauth2/token", "/oauth2/revoke", "/oauth2/introspect");
  }

  @Bean
  GroupedOpenApi oidcSecuredEndpoint() {
    return groupedOpenApiJwt("OIDC Protected Endpoints", "/oauth2/userinfo", "/oauth2/consent", "/admin/oidc/**");
  }
}
