package com.wkspower.platform.domain.config.model;

import java.util.List;
import java.util.Optional;

/**
 * One field on a case of this type. Immutable. Type-specific slots live in {@link TypeSlots} to
 * keep this record flat without exploding into seven subtypes — validation has already ensured only
 * the slots relevant to {@link #type} are populated by the time this record is built.
 *
 * <p>Story 2.7 introduces {@link #requiredOnCreate()} — controls whether the create-form dialog
 * (driven by the case-type YAML) asks for this field at case creation time. Defaults to {@link
 * #required()} when the YAML omits the slot, preserving backwards-compatible behavior for seed
 * YAMLs that pre-date the grammar extension.
 *
 * <p>Story 5.6 introduces {@link #editableBy()} — declares the role-id allow-list authorised to
 * change this field on form submit. Empty list means "no {@code editableBy} declared" — gated by
 * the case-type's {@code defaultFieldEditability} (AC4): editable-by-default → unrestricted;
 * locked-by-default → read-only for all. Entries are wire strings {@code role:&lt;roleId&gt;}; the
 * validator cross-references each {@code &lt;roleId&gt;} against the case-type's {@code roles[]}
 * declaration.
 */
public record FieldDefinition(
    String id,
    String displayName,
    FieldType type,
    boolean required,
    boolean requiredOnCreate,
    int order,
    List<FieldOption> options,
    TypeSlots slots,
    /** Story 5.6 AC1 — role-id allow-list for write authorization. */
    List<String> editableBy) {

  public FieldDefinition {
    options = options == null ? List.of() : List.copyOf(options);
    editableBy = editableBy == null ? List.of() : List.copyOf(editableBy);
  }

  /**
   * Backwards-compatible secondary constructor for callers (mostly tests + Story 2.1/2.2/2.3
   * fixtures) that pre-date the {@code requiredOnCreate} grammar extension. Defaults {@code
   * requiredOnCreate} to {@code required}, mirroring the YAML default-on-omit behavior in {@link
   * com.wkspower.platform.infrastructure.config.ConfigValidator}.
   */
  public FieldDefinition(
      String id,
      String displayName,
      FieldType type,
      boolean required,
      int order,
      List<FieldOption> options,
      TypeSlots slots) {
    this(id, displayName, type, required, required, order, options, slots, List.of());
  }

  /**
   * Story 2.7 era — 8-arg ctor (with {@code requiredOnCreate}, no {@code editableBy}). Defaults
   * {@code editableBy} to empty list per Story 5.6 additive-default pattern.
   */
  public FieldDefinition(
      String id,
      String displayName,
      FieldType type,
      boolean required,
      boolean requiredOnCreate,
      int order,
      List<FieldOption> options,
      TypeSlots slots) {
    this(id, displayName, type, required, requiredOnCreate, order, options, slots, List.of());
  }

  /**
   * Optional per-type constraints captured from YAML. All values boxed because most are absent on
   * most fields — validator and schema generator read {@link Optional#ofNullable(Object)}.
   */
  public record TypeSlots(
      Integer minLength,
      Integer maxLength,
      Double min,
      Double max,
      Double step,
      String dateMin,
      String dateMax,
      Long maxBytes,
      List<String> allowedMimeTypes) {

    public TypeSlots {
      allowedMimeTypes = allowedMimeTypes == null ? List.of() : List.copyOf(allowedMimeTypes);
    }

    public static TypeSlots empty() {
      return new TypeSlots(null, null, null, null, null, null, null, null, List.of());
    }
  }
}
