package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.model.RecentSignalEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Story 4.6 AC3 — bounded, per-CaseType, in-memory ring buffer for the most recent {@link
 * RecentSignalEntry} records emitted by {@code ExecutionSignalRouter}. Backs the admin Mapping
 * Inspector's "Recent Signals" panel (AC2 / AC4).
 *
 * <p>Pure-Java — no Spring imports (NFR36); wired as a singleton {@code @Bean} in {@code
 * infrastructure.config.WorkflowAdapterConfig} alongside {@link MappingRegistry}. Reset on
 * application restart — no persistence guarantee. Memory pressure: {@code 50 entries × N caseTypes
 * × ~250 bytes/entry ≈ 12.5 KB per 100 CaseTypes}, acceptable for an inspector primitive.
 *
 * <p>Thread-safety: a {@link ConcurrentHashMap} keyed by {@code caseTypeId}; each value is an
 * {@link ArrayDeque} guarded by a per-deque {@code synchronized} block for the trim-and-add
 * operation. {@link #recent} returns a defensive snapshot (no shared mutable state).
 *
 * <p>Lossy by design: when the buffer reaches capacity, the oldest entry is evicted (FIFO). For
 * forensic-grade signal retention see Epic 9 (Activity Feed) — out of scope here.
 */
public class SignalAuditRingBuffer {

  /** Bounded capacity per CaseType. */
  static final int CAPACITY = 50;

  private final ConcurrentMap<String, Deque<RecentSignalEntry>> byCaseType =
      new ConcurrentHashMap<>();

  /**
   * Record one routing decision. Best-effort: the caller (router) wraps this in a try/catch so an
   * audit-side exception cannot abort canonical routing.
   *
   * @param caseTypeId non-null CaseType id
   * @param entry non-null entry
   */
  public void record(String caseTypeId, RecentSignalEntry entry) {
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Objects.requireNonNull(entry, "entry");
    byCaseType.compute(
        caseTypeId,
        (k, deque) -> {
          Deque<RecentSignalEntry> d = (deque == null) ? new ArrayDeque<>(CAPACITY) : deque;
          synchronized (d) {
            d.addFirst(entry);
            while (d.size() > CAPACITY) {
              d.removeLast();
            }
          }
          return d;
        });
  }

  /**
   * Snapshot of the buffer for {@code caseTypeId}, ordered newest-first. Empty when no entries have
   * been recorded for that id.
   */
  public List<RecentSignalEntry> recent(String caseTypeId) {
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Deque<RecentSignalEntry> d = byCaseType.get(caseTypeId);
    if (d == null) {
      return List.of();
    }
    synchronized (d) {
      return List.copyOf(d);
    }
  }
}
