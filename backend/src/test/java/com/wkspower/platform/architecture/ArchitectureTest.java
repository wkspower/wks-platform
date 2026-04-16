package com.wkspower.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

/**
 * Hexagonal boundary enforcement. Build fails if any rule is violated — if this test ever fails,
 * fix the offending import, do NOT weaken the rule.
 */
@AnalyzeClasses(
    packages = "com.wkspower.platform",
    importOptions = {ImportOption.DoNotIncludeTests.class})
public class ArchitectureTest {

  @ArchTest
  static final ArchRule domainHasNoFrameworkImports =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..", "jakarta.persistence..", "org.cibseven..")
          .because(
              "Domain must be pure Java — no Spring, no JPA, no CIB seven imports. "
                  + "This is NFR36 and is non-negotiable.");

  @ArchTest
  static final ArchRule onlyEngineImportsCibSeven =
      noClasses()
          .that()
          .resideOutsideOfPackage("..engine..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.cibseven..")
          .because(
              "CIB seven imports belong exclusively to engine/. "
                  + "This is NFR35 and isolates the BPMN engine behind a port.");

  @ArchTest
  static final ArchRule hexagonalLayering =
      Architectures.layeredArchitecture()
          .consideringAllDependencies()
          .layer("api")
          .definedBy("..api..")
          .layer("domain")
          .definedBy("..domain..")
          .layer("infrastructure")
          .definedBy("..infrastructure..")
          .layer("engine")
          .definedBy("..engine..")
          .whereLayer("api")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("infrastructure")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("engine")
          .mayNotBeAccessedByAnyLayer();
}
