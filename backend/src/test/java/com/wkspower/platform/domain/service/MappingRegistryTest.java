package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.port.CaseTypeRef;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Story 4.3 Task 2 — unit coverage for {@link MappingRegistry}. */
class MappingRegistryTest {

  private static final CaseTypeRef CASE_TYPE = new CaseTypeRef("loan-application", "v1");

  private static MappingDefinition definitionWithOneAttachment() {
    return new MappingDefinition(
        List.of(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                Map.of(),
                List.of(),
                Map.of())));
  }

  @Test
  void registerThenResolveReturnsTheDefinition() {
    MappingRegistry registry = new MappingRegistry();
    MappingDefinition def = definitionWithOneAttachment();

    registry.register(CASE_TYPE, "v1", def);

    assertThat(registry.resolve(CASE_TYPE, "v1")).contains(def);
  }

  @Test
  void resolveMissingKeyReturnsEmpty() {
    MappingRegistry registry = new MappingRegistry();

    assertThat(registry.resolve(CASE_TYPE, "v1")).isEmpty();
  }

  @Test
  void unregisterRemovesTheMapping() {
    MappingRegistry registry = new MappingRegistry();
    registry.register(CASE_TYPE, "v1", definitionWithOneAttachment());

    registry.unregister(CASE_TYPE, "v1");

    assertThat(registry.resolve(CASE_TYPE, "v1")).isEmpty();
  }

  @Test
  void unregisterUnknownKeyIsIdempotent() {
    MappingRegistry registry = new MappingRegistry();

    registry.unregister(CASE_TYPE, "v1");

    assertThat(registry.resolve(CASE_TYPE, "v1")).isEmpty();
  }

  @Test
  void registerEmptyMappingRoundTripsAsEmpty() {
    MappingRegistry registry = new MappingRegistry();

    registry.register(CASE_TYPE, "v1", MappingDefinition.empty());

    assertThat(registry.resolve(CASE_TYPE, "v1")).contains(MappingDefinition.empty());
  }

  @Test
  void differentVersionsAreSeparateKeys() {
    MappingRegistry registry = new MappingRegistry();
    MappingDefinition v1 = definitionWithOneAttachment();
    MappingDefinition v2 = MappingDefinition.empty();

    registry.register(CASE_TYPE, "v1", v1);
    registry.register(CASE_TYPE, "v2", v2);

    assertThat(registry.resolve(CASE_TYPE, "v1")).contains(v1);
    assertThat(registry.resolve(CASE_TYPE, "v2")).contains(v2);
  }

  @Test
  void concurrentRegistersFromTwoThreadsBothLand() throws Exception {
    MappingRegistry registry = new MappingRegistry();
    CaseTypeRef ctA = new CaseTypeRef("a", "v1");
    CaseTypeRef ctB = new CaseTypeRef("b", "v1");
    MappingDefinition defA = definitionWithOneAttachment();
    MappingDefinition defB = MappingDefinition.empty();

    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      pool.submit(
          () -> {
            start.await();
            registry.register(ctA, "v1", defA);
            return null;
          });
      pool.submit(
          () -> {
            start.await();
            registry.register(ctB, "v1", defB);
            return null;
          });
      start.countDown();
      pool.shutdown();
      assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    } finally {
      pool.shutdownNow();
    }

    assertThat(registry.resolve(ctA, "v1")).contains(defA);
    assertThat(registry.resolve(ctB, "v1")).contains(defB);
  }

  @Test
  void nullArgumentsAreRejected() {
    MappingRegistry registry = new MappingRegistry();
    MappingDefinition def = MappingDefinition.empty();

    assertThatThrownBy(() -> registry.register(null, "v1", def))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> registry.register(CASE_TYPE, null, def))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> registry.register(CASE_TYPE, "v1", null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> registry.resolve(null, "v1")).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> registry.unregister(null, "v1"))
        .isInstanceOf(NullPointerException.class);
  }
}
