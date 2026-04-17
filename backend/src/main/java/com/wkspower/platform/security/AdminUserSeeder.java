package com.wkspower.platform.security;

import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the initial {@code admin} user on first boot.
 *
 * <ul>
 *   <li>Runs as an {@link ApplicationRunner} — after Flyway, before the web server starts accepting
 *       requests.
 *   <li>Idempotent: does nothing when any user already has the {@code admin} role.
 *   <li>{@code dev} profile without credentials → falls back to {@code admin@wkspower.local}/{@code
 *       admin} and logs a WARN (WKS-API-050).
 *   <li>{@code production} profile without credentials → startup fails (WKS-API-051).
 * </ul>
 *
 * <p>Concurrent-startup race: the {@code users.email} unique constraint guarantees at most one
 * admin; a {@link DataIntegrityViolationException} is swallowed at INFO.
 */
@Component
public class AdminUserSeeder implements ApplicationRunner {

  static final String ADMIN_ROLE = "admin";
  static final String DEV_DEFAULT_EMAIL = "admin@wkspower.local";
  static final String DEV_DEFAULT_PASSWORD = "admin";
  static final String PROFILE_PRODUCTION = "production";

  private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final Environment environment;
  private final String configuredEmail;
  private final String configuredPassword;

  public AdminUserSeeder(
      UserRepository users,
      PasswordEncoder encoder,
      Environment environment,
      @Value("${wks.admin.email:}") String configuredEmail,
      @Value("${wks.admin.password:}") String configuredPassword) {
    this.users = users;
    this.encoder = encoder;
    this.environment = environment;
    this.configuredEmail = configuredEmail;
    this.configuredPassword = configuredPassword;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (users.existsWithRole(ADMIN_ROLE)) {
      log.debug("Admin user already present — seeder skipped.");
      return;
    }

    Credentials creds = resolveCredentials();
    try {
      User user = new User(UUID.randomUUID(), creds.email(), Set.of(ADMIN_ROLE), /* active */ true);
      users.save(user, encoder.encode(creds.password()));
      log.info("Seeded initial admin user ({}).", creds.email());
    } catch (DataIntegrityViolationException race) {
      log.info("Admin seed raced with a concurrent startup — continuing ({}).", race.getMessage());
    }
  }

  private Credentials resolveCredentials() {
    boolean production =
        Arrays.asList(environment.getActiveProfiles()).contains(PROFILE_PRODUCTION);

    boolean hasEmail = configuredEmail != null && !configuredEmail.isBlank();
    boolean hasPassword = configuredPassword != null && !configuredPassword.isBlank();

    if (hasEmail && hasPassword) {
      return new Credentials(configuredEmail, configuredPassword);
    }

    if (production) {
      throw new IllegalStateException(
          "WKS-API-051 WKS_ADMIN_EMAIL and WKS_ADMIN_PASSWORD are both required in production. "
              + "The application refuses to start without them — fallback defaults are never used "
              + "in production.");
    }

    log.warn(
        "WKS-API-050 Dev-only default admin credentials in use — set WKS_ADMIN_EMAIL and "
            + "WKS_ADMIN_PASSWORD.");
    return new Credentials(DEV_DEFAULT_EMAIL, DEV_DEFAULT_PASSWORD);
  }

  record Credentials(String email, String password) {}
}
