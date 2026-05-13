package com.wkspower.platform.domain.config.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

/**
 * Story 6.3 AC-1 — guard that the 8-param compat ctor introduced by Story 6.2 has been removed and
 * never re-introduced. The canonical record header is the sole constructor; {@code outcomeMappings}
 * is required on every call site. Reflection-only (no Spring, no DB).
 */
class AttachmentDefinitionCtorMigrationTest {

  @Test
  void only_nine_param_ctor_exists() {
    Constructor<?>[] ctors = AttachmentDefinition.class.getDeclaredConstructors();
    assertThat(ctors)
        .as(
            "AttachmentDefinition must expose exactly the 9-param canonical record ctor — the"
                + " Story 6.2 compat 8-param ctor was removed by Story 6.3 AC-1 and must never be"
                + " re-introduced (per memory project_raw_casetype_config_constructor_debt).")
        .hasSize(1);
    assertThat(ctors[0].getParameterCount())
        .as("Canonical AttachmentDefinition ctor has 9 parameters (the 9th is outcomeMappings).")
        .isEqualTo(9);
  }
}
