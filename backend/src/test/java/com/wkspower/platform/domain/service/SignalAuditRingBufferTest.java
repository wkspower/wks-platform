package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.model.RecentSignalEntry;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/** Story 4.6 AC3 — unit tests for {@link SignalAuditRingBuffer}. */
class SignalAuditRingBufferTest {

  private static RecentSignalEntry entry(int seq) {
    return new RecentSignalEntry(
        Instant.parse("2026-05-09T00:00:00Z").plusSeconds(seq),
        ExecutionSignalKind.STAGE_TRANSITION,
        "endEvent_" + seq,
        "matched-rule",
        "endEventMapping",
        "stageTransition:next",
        UUID.randomUUID(),
        null);
  }

  @Test
  void recordFive_recentReturnsFiveNewestFirst() {
    SignalAuditRingBuffer buf = new SignalAuditRingBuffer();
    for (int i = 0; i < 5; i++) {
      buf.record("ct-A", entry(i));
    }
    List<RecentSignalEntry> got = buf.recent("ct-A");
    assertThat(got).hasSize(5);
    // newest-first: seq 4 .. 0
    assertThat(got.get(0).source()).isEqualTo("endEvent_4");
    assertThat(got.get(4).source()).isEqualTo("endEvent_0");
  }

  @Test
  void recordSixty_recentCappedAtFiftyNewest() {
    SignalAuditRingBuffer buf = new SignalAuditRingBuffer();
    for (int i = 0; i < 60; i++) {
      buf.record("ct-A", entry(i));
    }
    List<RecentSignalEntry> got = buf.recent("ct-A");
    assertThat(got).hasSize(SignalAuditRingBuffer.CAPACITY);
    // newest is seq 59; oldest retained is seq 10 (60 - 50 = 10).
    assertThat(got.get(0).source()).isEqualTo("endEvent_59");
    assertThat(got.get(49).source()).isEqualTo("endEvent_10");
  }

  @Test
  void twoCaseTypesDoNotCrossContaminate() {
    SignalAuditRingBuffer buf = new SignalAuditRingBuffer();
    buf.record("ct-A", entry(1));
    buf.record("ct-A", entry(2));
    buf.record("ct-B", entry(100));
    assertThat(buf.recent("ct-A")).hasSize(2);
    assertThat(buf.recent("ct-B")).hasSize(1);
    assertThat(buf.recent("ct-A").get(0).source()).isEqualTo("endEvent_2");
    assertThat(buf.recent("ct-B").get(0).source()).isEqualTo("endEvent_100");
    assertThat(buf.recent("ct-unknown")).isEmpty();
  }

  @Test
  void concurrentRecordAndRecentDoNotThrow() throws Exception {
    SignalAuditRingBuffer buf = new SignalAuditRingBuffer();
    int threads = 8;
    int opsPerThread = 1000;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    AtomicReference<Throwable> failure = new AtomicReference<>();

    for (int t = 0; t < threads; t++) {
      final int threadId = t;
      pool.submit(
          () -> {
            try {
              start.await();
              for (int i = 0; i < opsPerThread; i++) {
                if ((i & 1) == 0) {
                  buf.record("ct-A", entry(threadId * opsPerThread + i));
                } else {
                  buf.recent("ct-A");
                }
              }
            } catch (Throwable e) {
              failure.compareAndSet(null, e);
            } finally {
              done.countDown();
            }
          });
    }
    start.countDown();
    boolean finished = done.await(30, TimeUnit.SECONDS);
    pool.shutdownNow();
    assertThat(finished).as("threads completed").isTrue();
    assertThat(failure.get()).as("no exception").isNull();
    // Final size capped.
    assertThat(buf.recent("ct-A").size()).isLessThanOrEqualTo(SignalAuditRingBuffer.CAPACITY);
  }
}
