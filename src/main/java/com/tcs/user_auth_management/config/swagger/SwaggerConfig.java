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
        .info(new Info().title("My API").description("My API Documentation").version("1.0"))
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
    return groupedOpenApi("Oauth 2 Endpoints", "/**/.well-known/**");
  }
}
