package com.wkspower.platform.domain.config.model;

/**
 * Classification returned by {@code MappingDiff.classify(prev, next)} (Story 4.2 AC8 / D20). Two
 * branches:
 *
 * <ul>
 *   <li>{@link #APPEND_CLASS} — only additive mapping changes (new attachment, new userTaskMapping
 *       under existing attachment, new property emission rule). Safe to deploy without a CaseType
 *       version bump.
 *   <li>{@link #MUTATE_CLASS} — at least one existing entry was modified or removed (mapping
 *       deleted, {@code wksTask} value changed, {@code stageTransition} payload changed, {@code
 *       scope} changed, {@code file} reference changed). Requires a CaseType version bump per D20.
 * </ul>
 *
 * <p>Story 4.2 ships the helper unwired. Story 3.8's blast-radius validator imports {@code
 * MappingDiff} and emits {@link com.wkspower.platform.domain.exception.ErrorCode#WKS_CFG_029} when
 * the result is {@code MUTATE_CLASS} and the deployer did not supply {@code --bump}.
 */
public enum MappingChangeClass {

  /** Only additive changes — safe to redeploy without a CaseType version bump. */
  APPEND_CLASS,

  /** At least one existing entry was modified or removed — requires a version bump (D20). */
  MUTATE_CLASS
}
