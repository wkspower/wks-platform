package com.wkspower.platform.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the platform's OpenAPI document: title, description, version (from build-info when
 * available), license (Apache 2.0 per v2 relicensing), and the {@code cookieAuth} security scheme
 * ({@code apiKey / cookie / WKS_SESSION}) that reflects AD-6.
 *
 * <p>Without the {@code addSecurityItem} call the generated spec has no auth hint and Swagger UI's
 * "Try it out" sends no credentials — devs will waste hours. The scheme is applied globally;
 * endpoints that are genuinely public (health, auth/login) can override with
 * {@code @SecurityRequirements(@SecurityRequirement(name = ""))} if needed in a later story.
 */
@Configuration
public class OpenApiConfig {

  public static final String SECURITY_SCHEME_NAME = "cookieAuth";
  public static final String SESSION_COOKIE_NAME = "WKS_SESSION";

  @Bean
  public OpenAPI wksOpenAPI(ObjectProvider<BuildProperties> buildPropertiesProvider) {
    BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
    String version = buildProperties != null ? buildProperties.getVersion() : "0.0.0-unknown";

    return new OpenAPI()
        .info(
            new Info()
                .title("WKS Platform API")
                .description(
                    "Operations UI backend — case lifecycle, documents, users, RBAC. Every "
                        + "response uses the WKS envelope { data, error, meta }.")
                .version(version)
                .license(
                    new License()
                        .name("Apache-2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")))
        .components(
            new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name(SESSION_COOKIE_NAME)
                        .description(
                            "WKS session cookie — HttpOnly, SameSite=Lax, issued by POST "
                                + "/api/auth/login and cleared by POST /api/auth/logout.")))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
  }
}
