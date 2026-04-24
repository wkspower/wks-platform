package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import java.util.List;
import org.junit.jupiter.api.Test;

class CaseTypeRegistryTest {

  private final JsonSchemaGenerator schemas = new JsonSchemaGenerator();
  private final CaseTypeRegistry registry = new CaseTypeRegistry(schemas);

  @Test
  void registerThenFind() {
    var cfg = config("loan-application", 1);
    assertThat(registry.register(cfg).outcome()).isEqualTo(RegistrationResult.Outcome.REGISTERED);
    assertThat(registry.find("loan-application")).contains(cfg);
    assertThat(registry.schemaFor("loan-application")).isPresent();
  }

  @Test
  void allReturnsUnmodifiableSnapshot() {
    registry.register(config("a", 1));
    registry.register(config("b", 1));
    var snap = registry.all();
    assertThat(snap).hasSize(2);
    org.junit.jupiter.api.Assertions.assertThrows(
        UnsupportedOperationException.class, () -> snap.clear());
    // Snapshot is decoupled: mutating the registry afterwards doesn't change the snapshot size.
    registry.remove("a");
    assertThat(snap).hasSize(2);
  }

  @Test
  void sameVersionIsIdempotent() {
    var v1 = config("x", 1);
    registry.register(v1);
    assertThat(registry.register(v1).outcome()).isEqualTo(RegistrationResult.Outcome.IDEMPOTENT);
  }

  @Test
  void higherVersionReplaces() {
    registry.register(config("x", 1));
    assertThat(registry.register(config("x", 2)).outcome())
        .isEqualTo(RegistrationResult.Outcome.REPLACED);
    assertThat(registry.find("x").orElseThrow().version()).isEqualTo(2);
  }

  @Test
  void lowerVersionRejectsWithCfg011() {
    registry.register(config("x", 2));
    var res = registry.register(config("x", 1));
    assertThat(res.outcome()).isEqualTo(RegistrationResult.Outcome.REJECTED_OLDER_VERSION);
    assertThat(res.error()).isPresent();
    assertThat(res.error().get().code()).isEqualTo("WKS-CFG-011");
    assertThat(registry.find("x").orElseThrow().version())
        .as("registry retains the higher version")
        .isEqualTo(2);
  }

  @Test
  void concurrentReadersNeverSeeTornState() throws Exception {
    registry.register(config("x", 1));
    int readers = 4;
    var start = new java.util.concurrent.CyclicBarrier(readers + 1);
    var stop = new java.util.concurrent.atomic.AtomicBoolean(false);
    var failures = new java.util.concurrent.atomic.AtomicInteger(0);

    var pool = java.util.concurrent.Executors.newFixedThreadPool(readers + 1);
    try {
      for (int i = 0; i < readers; i++) {
        pool.submit(
            () -> {
              try {
                start.await();
                while (!stop.get()) {
                  var found = registry.find("x");
                  if (found.isEmpty()) {
                    failures.incrementAndGet();
                  } else {
                    int v = found.get().version();
                    if (v != 1 && v != 2) {
                      failures.incrementAndGet();
                    }
                  }
                }
              } catch (Exception e) {
                failures.incrementAndGet();
              }
            });
      }

      pool.submit(
          () -> {
            try {
              start.await();
              long deadline = System.currentTimeMillis() + 100;
              int v = 2;
              while (System.currentTimeMillis() < deadline) {
                registry.register(config("x", v));
                v++;
              }
            } catch (Exception e) {
              failures.incrementAndGet();
            }
          });

      start.await();
      Thread.sleep(120);
      stop.set(true);
    } finally {
      pool.shutdown();
      pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }
    assertThat(failures.get())
        .as("readers must never see torn state nor throw under concurrent register")
        .isZero();
  }

  private static CaseTypeConfig config(String id, int version) {
    return new CaseTypeConfig(
        id,
        "Display",
        version,
        null,
        new WorkflowRef("x.bpmn"),
        List.of(
            new FieldDefinition(
                "a", "A", FieldType.TEXT, false, 0, List.of(), FieldDefinition.TypeSlots.empty())),
        List.of(new StatusDefinition("open", "Open", StatusColor.AMBER)),
        List.of("a"),
        List.of(new RoleDefinition("officer", List.of(Permission.VIEW))));
  }
}
