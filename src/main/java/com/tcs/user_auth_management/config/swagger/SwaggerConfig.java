package com.tcs.user_auth_management.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  @Bean
  protected OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .description("swagger-description")
                .title("User profile management")
                .version("1.2.50")
                .termsOfService("tcs-system user profile"));
  }

  protected GroupedOpenApi groupedOpenApi(String group, String... pathToMatch) {
    return GroupedOpenApi.builder().group(group).pathsToMatch(pathToMatch).build();
  }

  protected GroupedOpenApi groupedOpenApiJwt(String group, String... pathToMatch) {
    return GroupedOpenApi.builder()
        .group(group)
        .pathsToMatch(pathToMatch)
        .addOpenApiCustomizer(
            openApi ->
                openApi.setComponents(
                    new Components()
                        .addSecuritySchemes(
                            "bearer-key",
                            new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))))
        .build();
  }

  @Bean
  GroupedOpenApi authentication() {
    return groupedOpenApi("Authentication", "/api/public/auth/**");
  }

  @Bean
  GroupedOpenApi userInformation() {
    return groupedOpenApiJwt("User Information", "/api/user/**");
  }

  @Bean
  GroupedOpenApi oauth2Endpoint() {
    return groupedOpenApi("Oauth 2 Endpoints", "/**/.well-known/**");
  }
}
