package com.wkspower.platform.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration placeholder.
 *
 * <p>Spring Boot's default static resource handler already serves {@code
 * classpath:/static/index.html} and its assets at {@code /}. The Dockerfile {@code backend-build}
 * stage copies {@code frontend/dist/*} into {@code src/main/resources/static/} so the SPA ships
 * inside the JAR.
 *
 * <p>SPA-style fallback routing (forward non-API paths to {@code index.html}) arrives in Story 1.3
 * when the React Router tree lands.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {}
