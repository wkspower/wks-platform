package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksConfigException;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Story 3.9.1 AC-2 + AC-3 — unit tests for stage-remap validation in {@link CaseRebaseService}.
 * Pure-Java, no Spring context. Validates that {@code validateStageRemap} throws {@code
 * WksConfigException} with {@link ErrorCode#WKS_CFG_036} for bad from-keys and {@link
 * ErrorCode#WKS_CFG_037} for bad to-values, before any DB mutation.
 */
class CaseRebaseServiceStageRemapValidationTest {

  private static final String CT_ID = "test-remap-ct";

  private CaseRebaseService service;

  @BeforeEach
  void setUp() {
    service =
        new CaseRebaseService(
            Mockito.mock(CaseRepository.class),
            new FakeCaseTypeVersionRegistry(),
            Mockito.mock(CaseTypeSource.class),
            Mockito.mock(EventPublisher.class),
            Mockito.mock(Clock.class));
  }

  private static CaseTypeConfig configWithStages(String id, int version, List<String> stageIds) {
    StatusDefinition open = new StatusDefinition("open", "Open", StatusColor.BLUE);
    FieldDefinition nameField =
        new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, null, null);
    List<StageDefinition> stages = new java.util.ArrayList<>();
    for (int i = 0; i < stageIds.size(); i++) {
      stages.add(new StageDefinition(stageIds.get(i), stageIds.get(i), i));
    }
    return new CaseTypeConfig(
        id,
        "Test CT",
        version,
        null,
        null,
        List.of(nameField),
        List.of(open),
        List.of(),
        List.of(),
        stages,
        List.of());
  }

  // ---- AC-2: from-key not in fromVersion.stages[] → WKS-CFG-036 ----

  @Test
  void fromKey_notInFromVersion_returns422_WKS_CFG_035() {
    // Note: the AC specifies WKS-CFG-035 in the story's it:{} block, but WKS-CFG-035 is already
    // allocated to concurrent-modification (story 3.9 review remediation). The actual code mints
    // WKS-CFG-036 for this validation — the method name here preserves the story's it:{} identifier
    // for traceability while the assertion targets the correct wire code.
    CaseTypeConfig fromConfig = configWithStages(CT_ID, 1, List.of("intake", "review"));
    CaseTypeConfig toConfig = configWithStages(CT_ID, 2, List.of("intake", "review"));

    // "bad-stage" is not in fromConfig stages
    Map<String, String> remap = Map.of("bad-stage", "review");

    assertThatThrownBy(() -> service.validateStageRemap(remap, fromConfig, toConfig))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors()).hasSize(1);
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_CFG_036.wire());
              assertThat(wce.getErrors().get(0).message()).contains("bad-stage");
              assertThat(wce.getErrors().get(0).message()).contains("not present in fromVersion");
            });
  }

  // ---- AC-3: to-value not in toVersion.stages[] → WKS-CFG-037 ----

  @Test
  void toValue_notInToVersion_returns422_WKS_CFG_036() {
    // Note: method name preserves story's it:{} identifier (WKS_CFG_036); actual wire code is
    // WKS-CFG-037 because WKS-CFG-036 is the from-key code.
    CaseTypeConfig fromConfig = configWithStages(CT_ID, 1, List.of("underwriting", "review"));
    CaseTypeConfig toConfig = configWithStages(CT_ID, 2, List.of("review", "decision"));

    // "underwriting" is in fromConfig; "bad-target" is NOT in toConfig
    Map<String, String> remap = Map.of("underwriting", "bad-target");

    assertThatThrownBy(() -> service.validateStageRemap(remap, fromConfig, toConfig))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors()).hasSize(1);
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_CFG_037.wire());
              assertThat(wce.getErrors().get(0).message()).contains("bad-target");
              assertThat(wce.getErrors().get(0).message()).contains("not present in toVersion");
            });
  }

  // ---- Valid remap passes without throwing ----

  @Test
  void validRemap_noException() {
    CaseTypeConfig fromConfig = configWithStages(CT_ID, 1, List.of("underwriting", "review"));
    CaseTypeConfig toConfig = configWithStages(CT_ID, 2, List.of("review", "decision"));

    Map<String, String> remap = Map.of("underwriting", "review");

    // Should not throw
    service.validateStageRemap(remap, fromConfig, toConfig);
  }
}
