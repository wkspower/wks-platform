package com.wkspower.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.Test;

/**
 * Hexagonal boundary enforcement. Build fails if any rule is violated — if this test ever fails,
 * fix the offending import, do NOT weaken the rule.
 *
 * <p>Uses plain JUnit Jupiter {@code @Test} methods (not ArchUnit's {@code @ArchTest} /
 * {@code @AnalyzeClasses}) because the ArchUnit JUnit5 engine is not reliably auto-discovered by
 * Surefire's {@code JUnitPlatformProvider} when mixed with Spring Boot's managed JUnit stack. The
 * Jupiter-native form is portable and debuggable.
 */
class ArchitectureTest {

  private static final JavaClasses CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("com.wkspower.platform");

  @Test
  void domainHasNoFrameworkImports() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.cibseven..")
        .because(
            "Domain must be pure Java — no Spring, no JPA, no CIB seven imports. "
                + "This is NFR36 and is non-negotiable.")
        .check(CLASSES);
  }

  @Test
  void onlyEngineImportsCibSeven() {
    noClasses()
        .that()
        .resideOutsideOfPackage("..engine..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.cibseven..")
        .because(
            "CIB seven imports belong exclusively to engine/. "
                + "This is NFR35 and isolates the BPMN engine behind a port.")
        .check(CLASSES);
  }

  @Test
  void hexagonalLayering() {
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
        // No declared layer (domain, infrastructure, engine) may reach into api.
        .whereLayer("api")
        .mayNotBeAccessedByAnyLayer()
        // No declared layer (api, domain, engine) may reach into infrastructure.
        // This prevents api→infrastructure shortcuts that bypass domain.
        .whereLayer("infrastructure")
        .mayNotBeAccessedByAnyLayer()
        // engine is accessed only by infrastructure — no declared layer bypasses the port.
        .whereLayer("engine")
        .mayNotBeAccessedByAnyLayer()
        .check(CLASSES);
  }
}
