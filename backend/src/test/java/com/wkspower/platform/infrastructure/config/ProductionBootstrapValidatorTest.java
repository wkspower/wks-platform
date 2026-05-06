package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * Unit tests for {@link ProductionBootstrapValidator} (Story 14.1.1 AC5 + AC7).
 *
 * <p>Covers the four production-bootstrap fail-closed paths:
 *
 * <ul>
 *   <li>WKS-API-053 H2 datasource — assertion fails fast.
 *   <li>WKS-API-053 Postgres datasource — assertion passes.
 *   <li>WKS-API-055 unset env var — assertion fails fast naming the var.
 *   <li>WKS-API-055 sentinel env var — assertion fails fast naming the var.
 * </ul>
 *
 * <p>Happy-path test covers all required env vars rotated AND non-H2 datasource.
 */
class ProductionBootstrapValidatorTest {

  private static MockEnvironment rotatedEnv() {
    MockEnvironment env = new MockEnvironment();
    for (String key : ProductionBootstrapValidator.REQUIRED_ENV_KEYS) {
      env.setProperty(key, "rotated-" + key.toLowerCase());
    }
    return env;
  }

  private static DataSource datasourceWithProductName(String productName) {
    DataSource ds = mock(DataSource.class);
    Connection conn = mock(Connection.class);
    DatabaseMetaData md = mock(DatabaseMetaData.class);
    try {
      when(ds.getConnection()).thenReturn(conn);
      when(conn.getMetaData()).thenReturn(md);
      when(md.getDatabaseProductName()).thenReturn(productName);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return ds;
  }

  @Test
  void happyPath_postgresDatasourceAndRotatedSecrets_passes() {
    DataSource ds = datasourceWithProductName("PostgreSQL");
    new ProductionBootstrapValidator(ds, rotatedEnv()).validate();
  }

  @Test
  void h2Datasource_failsFastWithApi053() {
    DataSource ds = datasourceWithProductName("H2");
    assertThatThrownBy(() -> new ProductionBootstrapValidator(ds, rotatedEnv()).validate())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("WKS-API-053")
        .hasMessageContaining("H2");
  }

  @Test
  void unsetSecret_failsFastWithApi055NamingTheVar() {
    DataSource ds = datasourceWithProductName("PostgreSQL");
    MockEnvironment env = rotatedEnv();
    env.setProperty("WKS_JWT_SECRET", "");
    assertThatThrownBy(() -> new ProductionBootstrapValidator(ds, env).validate())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("WKS-API-055")
        .hasMessageContaining("WKS_JWT_SECRET")
        .hasMessageContaining("unset");
  }

  @Test
  void sentinelSecret_failsFastWithApi055NamingTheVar() {
    DataSource ds = datasourceWithProductName("PostgreSQL");
    MockEnvironment env = rotatedEnv();
    env.setProperty("WKS_MINIO_ROOT_PASSWORD", "<MUST-BE-ROTATED>");
    assertThatThrownBy(() -> new ProductionBootstrapValidator(ds, env).validate())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("WKS-API-055")
        .hasMessageContaining("WKS_MINIO_ROOT_PASSWORD")
        .hasMessageContaining("<MUST-BE-ROTATED>");
  }

  @Test
  void multipleOffences_listedTogether() {
    DataSource ds = datasourceWithProductName("PostgreSQL");
    MockEnvironment env = rotatedEnv();
    env.setProperty("WKS_DB_PASSWORD", "<MUST-BE-ROTATED>");
    env.setProperty("WKS_ADMIN_EMAIL", "");
    assertThatThrownBy(() -> new ProductionBootstrapValidator(ds, env).validate())
        .isInstanceOf(IllegalStateException.class)
        .satisfies(
            ex ->
                assertThat(ex.getMessage())
                    .contains("WKS-API-055")
                    .contains("WKS_DB_PASSWORD")
                    .contains("WKS_ADMIN_EMAIL"));
  }
}
