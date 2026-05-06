package com.wkspower.platform.infrastructure.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Production-profile boot-time validator (Story 14.1.1, fail-closed posture).
 *
 * <p>Runs once on {@link ApplicationReadyEvent} when {@code --profile production} is active. Two
 * orthogonal assertions; either failure throws {@link IllegalStateException} which propagates out
 * of the listener and aborts startup (Spring boot fails the application context).
 *
 * <ul>
 *   <li>AC5 (finding I18) — wire code {@code WKS-API-053}. The active {@link DataSource} MUST
 *       resolve to a non-H2 JDBC driver. {@code application-production.yml} hard-overrides the
 *       driver to {@code org.postgresql.Driver}, but a misconfigured {@code WKS_DB_URL=jdbc:h2:...}
 *       could still drive the autoconfigured DataSource toward H2 with an opaque connect-time
 *       failure. Boot-time positive assertion makes the failure mode loud and explicit.
 *   <li>AC7 (finding I20) — wire code {@code WKS-API-055}. Every required production env var MUST
 *       be non-empty AND not equal to the {@code <MUST-BE-ROTATED>} sentinel that ships in {@code
 *       docker/.env.production.example}. Without this guard, an operator who copies the example
 *       file and forgets to rotate ships production with documented insecure defaults ({@code
 *       minioadmin}, {@code admin}/{@code admin}).
 * </ul>
 *
 * <p>The component lives outside the {@code security} package because AC5's surface is
 * datasource-shape and AC7's surface is operator-supplied env vars; both are bootstrap config
 * concerns. {@code KeycloakSeamAnnouncer} is the existing sibling at this address.
 *
 * <p>Wire-code rationale: WKS-API-053 was already the H2/Postgres bug code in Story 14.7 §8
 * narrative and the production JWT-secret validator. The story (locked 2026-05-06) reuses it for
 * the H2 datasource assertion — same security/startup band, same fail-closed semantic, same
 * operator runbook entry point. WKS-API-055 is freshly minted in the security/startup
 * string-literal band (050..054 are taken; 055 is the next free slot) per "Error Codes Are a Wire
 * Contract".
 */
@Component
@Profile("production")
@ConditionalOnProperty(
    name = "wks.bootstrap.production-validation.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ProductionBootstrapValidator {

  /** Sentinel value shipped in {@code docker/.env.production.example} — Story 14.1.1 AC7. */
  static final String ROTATION_SENTINEL = "<MUST-BE-ROTATED>";

  /**
   * Required env vars that the production deployment MUST rotate before boot. Order is stable for
   * log readability; the iteration produces deterministic error messages on multi-var failures.
   */
  static final String[] REQUIRED_ENV_KEYS = {
    "WKS_DB_PASSWORD",
    "WKS_STORAGE_KEY",
    "WKS_ADMIN_EMAIL",
    "WKS_ADMIN_PASSWORD",
    "WKS_JWT_SECRET",
    "WKS_MINIO_ROOT_USER",
    "WKS_MINIO_ROOT_PASSWORD",
  };

  private static final Logger LOG = LoggerFactory.getLogger(ProductionBootstrapValidator.class);

  private final DataSource dataSource;
  private final Environment env;
  private final Function<String, String> envReader;

  @Autowired
  public ProductionBootstrapValidator(DataSource dataSource, Environment env) {
    this(dataSource, env, System::getenv);
  }

  /** Package-private — allows tests to inject a controlled env-var source. */
  ProductionBootstrapValidator(
      DataSource dataSource, Environment env, Function<String, String> envReader) {
    this.dataSource = dataSource;
    this.env = env;
    this.envReader = envReader;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void validate() {
    assertNoH2Datasource();
    assertSecretsRotated();
    LOG.info("RUNTIME INVARIANT OK — production bootstrap validators passed.");
  }

  private void assertNoH2Datasource() {
    String productName;
    try (Connection connection = dataSource.getConnection()) {
      productName = connection.getMetaData().getDatabaseProductName();
    } catch (SQLException ex) {
      throw new IllegalStateException(
          "WKS-API-053 production datasource probe failed — could not read database product name."
              + " Verify WKS_DB_URL points at a reachable Postgres instance.",
          ex);
    }
    if (productName != null && productName.toUpperCase().contains("H2")) {
      throw new IllegalStateException(
          "WKS-API-053 production datasource resolved to H2 (databaseProductName='"
              + productName
              + "'). Production MUST run on Postgres. Fix WKS_DB_URL"
              + " (expected jdbc:postgresql://...) and ensure no H2 autoconfigure path"
              + " overrides the production driver.");
    }
    LOG.info("WKS-API-053 datasource assertion OK (databaseProductName='{}')", productName);
  }

  private void assertSecretsRotated() {
    Map<String, String> offences = new LinkedHashMap<>();
    for (String key : REQUIRED_ENV_KEYS) {
      // Read from the OS environment directly — Spring's Environment resolves any property source
      // (YAML defaults, -D flags) which would allow a default in application-production.yml to
      // bypass this guard. AC7 says "operator-supplied env var"; only System.getenv enforces that.
      String value = envReader.apply(key);
      if (value == null || value.isBlank()) {
        offences.put(key, "unset");
      } else if (ROTATION_SENTINEL.equals(value.strip())) {
        offences.put(key, "equals " + ROTATION_SENTINEL);
      }
    }
    if (!offences.isEmpty()) {
      StringBuilder msg = new StringBuilder("WKS-API-055 production env vars must be rotated:");
      offences.forEach((k, why) -> msg.append("\n  - ").append(k).append(" → ").append(why));
      msg.append(
          "\nReplace each sentinel value in docker/.env with a real secret"
              + " (`openssl rand -base64 32`). See docker/README.md → Production rotation"
              + " guidance.");
      throw new IllegalStateException(msg.toString());
    }
    LOG.info(
        "WKS-API-055 secret-rotation assertion OK ({} required env vars present and rotated).",
        REQUIRED_ENV_KEYS.length);
  }
}
