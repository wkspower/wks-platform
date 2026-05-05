package com.wkspower.platform.domain.port;

import java.util.Objects;

/**
 * Scope of a {@link BackendAdapter} attachment to a CaseType, per architecture §786 (Decision 22):
 * an adapter is attached either at the case scope (lifecycle of the entire case) or at a stage
 * scope (lifecycle of a single stage within the case).
 *
 * <p>Sealed hierarchy intentionally — the two variants are exhaustive and the router (Story 4.3)
 * can pattern-match on them.
 *
 * <p>Story 3.1 owns the {@code Stage} entity; this port carries only the {@code stageId} string so
 * Story 4.1 ships in parallel with Story 3.1 without pulling a domain aggregate into the port. A
 * mechanical rename to {@code Stage stage} is a Story 4.5 follow-up if/when needed.
 */
public sealed interface AttachmentScope permits AttachmentScope.Case, AttachmentScope.Stage {

  /** Adapter attached for the lifecycle of the entire case. */
  record Case() implements AttachmentScope {
    public static final Case INSTANCE = new Case();
  }

  /**
   * Adapter attached for the lifecycle of a single stage. {@code stageId} is the WKS-side stage id
   * (string, not the {@code Stage} entity — see class Javadoc).
   */
  record Stage(String stageId) implements AttachmentScope {
    public Stage {
      Objects.requireNonNull(stageId, "stageId");
      if (stageId.isBlank()) {
        throw new IllegalArgumentException("stageId must not be blank");
      }
    }
  }

  /** Convenience factory for the case-scope singleton. */
  static AttachmentScope ofCase() {
    return Case.INSTANCE;
  }

  /** Convenience factory for a stage-scope attachment. */
  static AttachmentScope ofStage(String stageId) {
    return new Stage(stageId);
  }
}
