package com.wkspower.platform.domain.config.diff;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldDefinition.TypeSlots;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.MappingChangeClass;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CaseTypeDiff}. Covers all 14 scenarios per Story 3.8 AC1 + AC5 unit
 * requirement. Pure Java — no Spring context.
 */
class CaseTypeDiffTest {

  // ---- helpers -----------------------------------------------------------

  private static CaseTypeConfig base() {
    return new CaseTypeConfig(
        "test-ct",
        "Test",
        1,
        null,
        null,
        List.of(),
        List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
        List.of(),
        List.of(),
        List.of(),
        List.of());
  }

  private static FieldDefinition textField(String id, boolean required) {
    return new FieldDefinition(id, id, FieldType.TEXT, required, 0, List.of(), TypeSlots.empty());
  }

  private static FieldDefinition typedField(String id, FieldType type, boolean required) {
    return new FieldDefinition(id, id, type, required, 0, List.of(), TypeSlots.empty());
  }

  // ---- test 1: identical configs → no deltas ----------------------------

  @Test
  void identicalConfigs_noDeltas() {
    CaseTypeConfig prev = base();
    CaseTypeConfig next = base();
    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.appendDeltas()).isEmpty();
    assertThat(report.mutateDeltas()).isEmpty();
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.APPEND_CLASS);
  }

  // ---- test 2: new status added (case-type level) → STATUS_ADDED --------

  @Test
  void newStatusAdded_caseTypeLevel_appendClass() {
    CaseTypeConfig prev = base();
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(
                new StatusDefinition("open", "Open", StatusColor.BLUE),
                new StatusDefinition("review", "Review", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.appendDeltas())
        .extracting(Delta::kind)
        .containsExactly(DeltaKind.STATUS_ADDED);
    assertThat(report.mutateDeltas()).isEmpty();
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.APPEND_CLASS);
  }

  // ---- test 3: new status added (stage-scoped) → STATUS_ADDED -----------

  @Test
  void newStatusAdded_stageScoped_appendClass() {
    StageDefinition stagePrev =
        new StageDefinition(
            "intake",
            "Intake",
            0,
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            Optional.of("open"));
    StageDefinition stageNext =
        new StageDefinition(
            "intake",
            "Intake",
            0,
            List.of(
                new StatusDefinition("open", "Open", StatusColor.BLUE),
                new StatusDefinition("review", "Review", StatusColor.BLUE)),
            Optional.of("open"));

    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(stagePrev),
            List.of());
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(stageNext),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.appendDeltas())
        .extracting(Delta::kind)
        .containsExactly(DeltaKind.STATUS_ADDED);
    assertThat(report.mutateDeltas()).isEmpty();
  }

  // ---- test 4: status removed → STATUS_REMOVED (mutate) -----------------

  @Test
  void statusRemoved_mutateClass() {
    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(
                new StatusDefinition("open", "Open", StatusColor.BLUE),
                new StatusDefinition("rejected", "Rejected", StatusColor.RED)),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    CaseTypeConfig next = base(); // only 'open'

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.mutateDeltas())
        .extracting(Delta::kind)
        .containsExactly(DeltaKind.STATUS_REMOVED);
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  // ---- test 5: status terminal flag flipped → STATUS_TERMINAL_FLIP ------

  @Test
  void statusTerminalFlipped_mutateClass() {
    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE, false)),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE, true)),
            List.of(),
            List.of(),
            List.of(),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.mutateDeltas())
        .extracting(Delta::kind)
        .containsExactly(DeltaKind.STATUS_TERMINAL_FLIP);
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  // ---- test 6: status retargeted across stages → STATUS_RETARGETED ------

  @Test
  void statusRetargetedAcrossStages_mutateClass() {
    StageDefinition stageA =
        new StageDefinition(
            "intake",
            "Intake",
            0,
            List.of(new StatusDefinition("pending", "Pending", StatusColor.BLUE)),
            Optional.of("pending"));
    StageDefinition stageB =
        new StageDefinition(
            "review",
            "Review",
            1,
            List.of(new StatusDefinition("approved", "Approved", StatusColor.EMERALD)),
            Optional.of("approved"));

    // In next, 'pending' moves from intake to review
    StageDefinition stageANext =
        new StageDefinition(
            "intake",
            "Intake",
            0,
            List.of(new StatusDefinition("drafted", "Drafted", StatusColor.BLUE)),
            Optional.of("drafted"));
    StageDefinition stageBNext =
        new StageDefinition(
            "review",
            "Review",
            1,
            List.of(
                new StatusDefinition("approved", "Approved", StatusColor.EMERALD),
                new StatusDefinition("pending", "Pending", StatusColor.BLUE)),
            Optional.of("approved"));

    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(stageA, stageB),
            List.of());
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(stageANext, stageBNext),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.mutateDeltas()).extracting(Delta::kind).contains(DeltaKind.STATUS_RETARGETED);
  }

  // ---- test 7: new field added (required=true) → FIELD_ADDED (append) ---

  @Test
  void newFieldAdded_requiredTrue_appendClass() {
    CaseTypeConfig prev = base();
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(textField("name", true)),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.appendDeltas())
        .extracting(Delta::kind)
        .containsExactly(DeltaKind.FIELD_ADDED);
    assertThat(report.mutateDeltas()).isEmpty();
  }

  // ---- test 8: field removed → FIELD_REMOVED (mutate) -------------------

  @Test
  void fieldRemoved_mutateClass() {
    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(textField("name", false)),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    CaseTypeConfig next = base(); // no fields

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.mutateDeltas())
        .extracting(Delta::kind)
        .containsExactly(DeltaKind.FIELD_REMOVED);
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  // ---- test 9: field type changed → FIELD_RETYPED (mutate) --------------

  @Test
  void fieldTypeChanged_mutateClass() {
    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(typedField("score", FieldType.TEXT, false)),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(typedField("score", FieldType.NUMBER, false)),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.mutateDeltas())
        .extracting(Delta::kind)
        .containsExactly(DeltaKind.FIELD_RETYPED);
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  // ---- test 10: field required-ness flipped → FIELD_REQUIRED_FLIPPED ----

  @Test
  void fieldRequiredFlipped_mutateClass() {
    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(textField("notes", false)),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(textField("notes", true)),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.mutateDeltas())
        .extracting(Delta::kind)
        .containsExactly(DeltaKind.FIELD_REQUIRED_FLIPPED);
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  // ---- test 11: new stage appended at tail → STAGE_APPENDED (append) ----

  @Test
  void newStageAppendedAtTail_appendClass() {
    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(new StageDefinition("intake", "Intake", 0)),
            List.of());
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(
                new StageDefinition("intake", "Intake", 0),
                new StageDefinition("review", "Review", 1)),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.appendDeltas())
        .extracting(Delta::kind)
        .containsExactly(DeltaKind.STAGE_APPENDED);
    assertThat(report.mutateDeltas()).isEmpty();
  }

  // ---- test 12: stage inserted in middle → STAGE_INSERTED_MIDDLE (mutate)

  @Test
  void stageInsertedInMiddle_mutateClass() {
    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(
                new StageDefinition("intake", "Intake", 0),
                new StageDefinition("decision", "Decision", 1)),
            List.of());
    // 'review' is inserted between 'intake' and 'decision'
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(
                new StageDefinition("intake", "Intake", 0),
                new StageDefinition("review", "Review", 1),
                new StageDefinition("decision", "Decision", 2)),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    // 'review' inserted in middle → STAGE_INSERTED_MIDDLE
    // 'decision' ordinal changed → STAGE_REORDERED
    assertThat(report.mutateDeltas())
        .extracting(Delta::kind)
        .contains(DeltaKind.STAGE_INSERTED_MIDDLE);
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  // ---- test 13: mapping mutate-class delegate → MAPPING (mutate) --------

  @Test
  void mappingMutateClassDelegate_mutateClass() {
    // Create a prev mapping and a next mapping that MappingDiff would classify as MUTATE_CLASS.
    // Simplest way: prev has an attachment, next has none (removal = mutate).
    com.wkspower.platform.domain.config.model.AttachmentDefinition prevAttachment =
        new com.wkspower.platform.domain.config.model.AttachmentDefinition(
            "bpmn",
            "loan.bpmn",
            "case",
            java.util.Optional.empty(),
            java.util.Map.of(),
            java.util.Optional.empty(),
            java.util.Map.of(),
            List.of());
    MappingDefinition prevMapping = new MappingDefinition(List.of(prevAttachment));
    MappingDefinition nextMapping = MappingDefinition.empty(); // removed → MUTATE_CLASS

    CaseTypeConfig config = base();
    BlastRadiusReport report = CaseTypeDiff.classify(config, config, prevMapping, nextMapping);

    assertThat(report.mutateDeltas()).extracting(Delta::kind).containsExactly(DeltaKind.MAPPING);
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  // ---- test 14: new role added → ROLE_ADDED (append) --------------------

  @Test
  void newRoleAdded_appendClass() {
    CaseTypeConfig prev =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(new RoleDefinition("admin", List.of())),
            List.of(),
            List.of());
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(
                new RoleDefinition("admin", List.of()), new RoleDefinition("reviewer", List.of())),
            List.of(),
            List.of());

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.appendDeltas()).extracting(Delta::kind).containsExactly(DeltaKind.ROLE_ADDED);
    assertThat(report.mutateDeltas()).isEmpty();
  }

  // ---- extra: new form added → FORM_ADDED (append) ----------------------

  @Test
  void newFormAdded_appendClass() {
    CaseTypeConfig prev = base();
    CaseTypeConfig next =
        new CaseTypeConfig(
            "test-ct",
            "Test",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(),
            List.of(),
            List.of(
                new FormDefinition(
                    "intake-form", "single", "monolithic", "single-page", List.of(), List.of(), null)));

    BlastRadiusReport report =
        CaseTypeDiff.classify(prev, next, MappingDefinition.empty(), MappingDefinition.empty());

    assertThat(report.appendDeltas()).extracting(Delta::kind).containsExactly(DeltaKind.FORM_ADDED);
    assertThat(report.mutateDeltas()).isEmpty();
  }

  // ---- BlastRadiusReport.changeClass() ----------------------------------------

  @Test
  void changeClass_appendWhenNoMutateDeltas() {
    BlastRadiusReport report =
        new BlastRadiusReport(
            List.of(new Delta(DeltaKind.STATUS_ADDED, "/statuses/x/id", "added")), List.of());
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.APPEND_CLASS);
  }

  @Test
  void changeClass_mutateWhenMutateDeltasPresent() {
    BlastRadiusReport report =
        new BlastRadiusReport(
            List.of(), List.of(new Delta(DeltaKind.STATUS_REMOVED, "/statuses/x/id", "removed")));
    assertThat(report.changeClass()).isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }
}
