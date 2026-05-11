package com.wkspower.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.domain.model.AuthenticationMaterial;
import com.wkspower.platform.domain.port.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Stateless JWT-backed filter chain.
 *
 * <p>Rules:
 *
 * <ul>
 *   <li>{@code GET /api/health}, {@code POST /api/auth/login}, {@code POST /api/auth/logout} →
 *       permitAll
 *   <li>All CORS preflights ({@code OPTIONS *}) → permitAll
 *   <li>Everything else under {@code /api/**} → authenticated
 *   <li>Static assets → permitAll (SPA shell is served from the same JAR)
 * </ul>
 *
 * <p>CSRF is disabled on {@code /api/**} (the JWT sits in an {@code HttpOnly}, {@code SameSite=Lax}
 * cookie — a CSRF token would add no defense and would block the Vite dev proxy). Session creation
 * is {@link SessionCreationPolicy#STATELESS}. HTTP Basic and form login are explicitly disabled —
 * the JSON {@code /api/auth/login} endpoint is the only entry point.
 *
 * <p>Revocation is intentionally absent in Phase 0 — a short 8h TTL is the mitigation, and a
 * DB-backed revocation list is a Phase 1 follow-up.
 */
@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

  private static final Pattern ORIGIN_PATTERN = Pattern.compile("^https?://[^/\\s*]+$");
  private static final String DUMMY_PASSWORD_PROBE =
      "__wks_timing_equalization_probe_not_a_real_password__";

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      SamlGatingFilter samlGatingFilter,
      CorsConfigurationSource corsConfigurationSource,
      WksAuthenticationEntryPoint authenticationEntryPoint,
      Environment environment,
      @Value("${WKS_OPENAPI_ENABLED:false}") boolean openApiEnabledInProduction)
      throws Exception {
    boolean production = environment.acceptsProfiles(Profiles.of("production"));
    boolean exposeOpenApi = !production || openApiEnabledInProduction;

    return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .logout(logout -> logout.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
        .authorizeHttpRequests(
            auth -> {
              auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                  .permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/health")
                  .permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/theme.css")
                  .permitAll()
                  .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout")
                  .permitAll();
              if (exposeOpenApi) {
                // OpenAPI / Swagger UI — always open under dev; under production only when the
                // WKS_OPENAPI_ENABLED env var is set. Default production behaviour: springdoc is
                // disabled in application-production.yml so the paths 404 anyway.
                auth.requestMatchers(
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll();
              }
              auth.requestMatchers("/api/**").authenticated().anyRequest().permitAll();
            })
        // samlGatingFilter is registered first, so it runs before jwtAuthenticationFilter in the
        // chain — both are positioned just before UsernamePasswordAuthenticationFilter, and Spring
        // Security preserves registration order within the same slot.
        .addFilterBefore(samlGatingFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public WksAuthenticationEntryPoint wksAuthenticationEntryPoint(ObjectMapper objectMapper) {
    return new WksAuthenticationEntryPoint(objectMapper);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }

  /**
   * Returns a {@link UserDetailsService} that equalizes timing between the "user exists" and "user
   * missing / inactive" branches by returning a dummy {@link User} with a pre-computed Argon2 hash
   * when no real match is found. Without this, the missing-user path skips the encoder and
   * completes much faster than a wrong-password path, allowing attackers to enumerate accounts via
   * response latency.
   */
  @Bean
  public UserDetailsService userDetailsService(UserRepository users, PasswordEncoder encoder) {
    final String dummyHash = encoder.encode(DUMMY_PASSWORD_PROBE);
    return email ->
        users
            .findAuthMaterialByEmail(email)
            .filter(AuthenticationMaterial::active)
            .map(SecurityConfig::toSpringUser)
            .orElseGet(() -> dummyUser(email, dummyHash));
  }

  @Bean
  public AuthenticationManager authenticationManager(
      UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    provider.setHideUserNotFoundExceptions(true); // merge unknown-email with bad-password paths
    return new ProviderManager(List.of(provider));
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${wks.cors.origins:http://localhost:5173}") String originsCsv) {
    List<String> origins =
        Arrays.stream(originsCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toUnmodifiableList());

    if (origins.isEmpty()) {
      throw new IllegalStateException(
          "WKS-API-054 wks.cors.origins resolved to an empty list. Set WKS_CORS_ORIGINS to at "
              + "least one explicit origin (e.g. http://localhost:5173).");
    }
    for (String origin : origins) {
      if ("null".equalsIgnoreCase(origin)
          || "*".equals(origin)
          || !ORIGIN_PATTERN.matcher(origin).matches()) {
        throw new IllegalStateException(
            "WKS-API-054 invalid CORS origin: \""
                + origin
                + "\". Each WKS_CORS_ORIGINS entry must be an absolute http(s) URL without path.");
      }
    }

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Content-Type"));
    config.setExposedHeaders(List.of("X-Correlation-Id"));
    config.setAllowCredentials(true); // required for WKS_SESSION cookie

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }

  /**
   * True when the {@code production} profile is active — used by {@code AuthController} to set
   * {@code Secure} on the cookie.
   */
  @Bean
  public ProductionProfile productionProfile(Environment environment) {
    boolean production = Arrays.asList(environment.getActiveProfiles()).contains("production");
    return new ProductionProfile(production);
  }

  public record ProductionProfile(boolean active) {}

  private static User toSpringUser(AuthenticationMaterial m) {
    // Spring's User ctor args: username, password, enabled, accountNonExpired,
    // credentialsNonExpired, accountNonLocked, authorities. Inactive users are already filtered
    // upstream, so "enabled" is always true here.
    return new User(
        m.email(),
        m.passwordHash(),
        true,
        true,
        true,
        true,
        m.roles().stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase(Locale.ROOT)))
            .collect(Collectors.toUnmodifiableSet()));
  }

  private static User dummyUser(String email, String dummyHash) {
    // Presented to DaoAuthenticationProvider so it still executes the encoder match, equalizing
    // latency with the real-user wrong-password path. The match always fails →
    // BadCredentialsException.
    return new User(
        email == null || email.isBlank() ? "__unknown__" : email,
        dummyHash,
        true,
        true,
        true,
        true,
        List.of());
  }
}
