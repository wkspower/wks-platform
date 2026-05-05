package com.wkspower.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

/**
 * Story 4.1 AC5 — guardrail that the {@code BackendAdapter} port and the two service-side classes
 * that ship with it ({@code NullAdapter}, {@code BackendAdapterBinder}) remain backend-agnostic
 * forever. A code-review reminder is not enough — this rule fails the build the day someone imports
 * {@code org.cibseven..} or {@code com.wkspower.platform.engine..} into one of these files.
 *
 * <p>Pinned as its own ArchUnit class (rather than added to {@code ArchitectureTest}) so a
 * regression surfaces a Story-4.1-attributable failure message.
 */
class BackendAdapterPortIsolationTest {

  private static final JavaClasses CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("com.wkspower.platform");

  @Test
  void backendAdapterPortHasNoEngineOrInfrastructureImports() {
    noClasses()
        .that()
        .resideInAPackage("com.wkspower.platform.domain.port..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.cibseven..",
            "com.wkspower.platform.engine..",
            "com.wkspower.platform.infrastructure..")
        .because(
            "Story 4.1 AC5: domain.port classes — including BackendAdapter, BackendSignal, "
                + "AttachmentScope, CaseTypeRef, CaseInstanceRef, BackendSignalKind, "
                + "BackendSignalHandler, BackendSignalSubscription — must remain backend-agnostic "
                + "and infrastructure-agnostic. Adding any such import here would break the "
                + "Mapping Layer (Decision 22) by leaking backend choice into domain.")
        .check(CLASSES);
  }

  @Test
  void nullAdapterAndBinderHaveNoEngineImports() {
    // NullAdapter and BackendAdapterBinder live in domain.service alongside CaseService etc., so
    // we cannot use a package-wide rule. Pin by simple name — these two classes ship with the
    // port and must remain backend-agnostic forever.
    noClasses()
        .that()
        .haveSimpleName("NullAdapter")
        .or()
        .haveSimpleName("BackendAdapterBinder")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.cibseven..", "com.wkspower.platform.engine..")
        .because(
            "Story 4.1 AC5: NullAdapter (the zero-attachment fallback) and BackendAdapterBinder "
                + "(the resolve seam) must never depend on a specific backend SDK. Spring "
                + "@Component is allowed; engine SDK and engine package imports are not.")
        .check(CLASSES);
  }
}
