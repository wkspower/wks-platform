package com.wkspower.platform.security;

import com.wkspower.platform.domain.model.AuthenticationMaterial;
import com.wkspower.platform.domain.port.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class SecurityConfig {

  private static final String[] PERMIT_ALL_API =
      new String[] {"/api/health", "/api/auth/login", "/api/auth/logout"};

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      CorsConfigurationSource corsConfigurationSource,
      WksAuthenticationEntryPoint authenticationEntryPoint)
      throws Exception {
    return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .logout(logout -> logout.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.GET, "/api/health")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout")
                    .permitAll()
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public WksAuthenticationEntryPoint wksAuthenticationEntryPoint() {
    return new WksAuthenticationEntryPoint();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }

  @Bean
  public UserDetailsService userDetailsService(UserRepository users) {
    return email ->
        users
            .findAuthMaterialByEmail(email)
            .filter(AuthenticationMaterial::active)
            .map(SecurityConfig::toSpringUser)
            .orElseThrow(
                () -> new UsernameNotFoundException("Authentication failed")); // generic — no enum
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
      @Value("${wks.cors.origins:http://localhost:5173}") String originsCsv,
      Environment environment) {
    List<String> origins =
        Arrays.stream(originsCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toUnmodifiableList());

    CorsConfiguration config = new CorsConfiguration();
    if (!origins.isEmpty()) {
      config.setAllowedOrigins(origins);
    }
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
    return new User(
        m.email(),
        m.passwordHash(),
        true, /* active */
        true,
        true,
        true,
        m.roles().stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase(Locale.ROOT)))
            .collect(Collectors.toUnmodifiableSet()));
  }
}
