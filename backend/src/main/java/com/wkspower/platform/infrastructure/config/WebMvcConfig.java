package com.wkspower.platform.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 *
 * <p>Spring Boot's default static resource handler serves {@code classpath:/static/index.html} and
 * its assets at {@code /}. The Dockerfile {@code backend-build} stage copies {@code
 * frontend/dist/*} into {@code src/main/resources/static/} so the SPA ships inside the JAR.
 *
 * <p>React Router 7 runs in history mode — route transitions never hit the server, but a page
 * reload on a client-side route (e.g. {@code /tasks}) becomes a direct GET. Spring MVC has no
 * static file at that path and falls through to a JSON 404 {@code ProblemDetail}. This config
 * forwards the five known Phase-0 SPA routes (and their sub-paths) to {@code /index.html} so the
 * React Router tree resolves them client-side.
 *
 * <p>Routes mirror {@code frontend/src/routes.tsx}. When a new top-level route lands in a later
 * story, add it here in the same commit. Everything under {@code /api/**} is unaffected — explicit
 * controller mappings always win over view controllers.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private static final String[] SPA_ROUTES = {
    "/login", "/cases", "/cases/**", "/tasks", "/tasks/**", "/admin", "/admin/**", "/dev", "/dev/**"
  };

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    for (String route : SPA_ROUTES) {
      registry.addViewController(route).setViewName("forward:/index.html");
    }
  }
}
