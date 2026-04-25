package com.wkspower.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;
import jakarta.persistence.Entity;
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
  void apiAndSecurityDoNotDependOnPersistenceEntities() {
    noClasses()
        .that()
        .resideInAnyPackage("..api..", "..security..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..infrastructure.persistence.entity..")
        .because(
            "Controllers and the security package use the UserRepository port and domain "
                + "records — not JPA entities. Keeping api/security off entity imports prevents "
                + "password-hash leaks into responses and keeps the hexagonal boundary honest.")
        .check(CLASSES);
  }

  @Test
  void onlySecurityImportsJjwt() {
    noClasses()
        .that()
        .resideOutsideOfPackage("..security..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("io.jsonwebtoken..")
        .because(
            "JJWT is a security-package implementation detail. Limiting imports to security/ "
                + "makes future JWT library upgrades a local change.")
        .check(CLASSES);
  }

  @Test
  void domainDoesNotDependOnJjwtOrSpringSecurity() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("io.jsonwebtoken..", "org.springframework.security..")
        .because(
            "Domain is framework-free. Covered by the blanket rule but made explicit here so a "
                + "regression surfaces a clearer failure message.")
        .check(CLASSES);
  }

  @Test
  void domainDoesNotDependOnSpringdocOrSwagger() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springdoc..", "io.swagger..")
        .because(
            "OpenAPI / Swagger annotations are a transport concern and belong in api/ controllers "
                + "and infrastructure/config. Domain stays framework-free — Story 1.4 AC12.")
        .check(CLASSES);
  }

  @Test
  void jpaEntitiesLiveOnlyInPersistenceEntityPackage() {
    classes()
        .that()
        .areAnnotatedWith(Entity.class)
        .should()
        .resideInAPackage("..infrastructure.persistence.entity..")
        .because(
            "@Entity classes (and therefore BaseJpaEntity subclasses) must stay inside the "
                + "persistence-entity package so the hexagonal boundary between domain and JPA "
                + "remains honest — Story 1.4 AC12.")
        .check(CLASSES);
  }

  @Test
  void caseTypeDomainDoesNotDependOnYamlOrJacksonOrNetworknt() {
    // AC11 scope: the case-type config subtree (records + service + ports) must not import
    // YAML, Jackson databind, or networknt. ErrorDetail's @JsonInclude annotation is the
    // pre-existing 1.4 exception — deliberately limited by package.
    noClasses()
        .that()
        .resideInAnyPackage("..domain.config..", "..domain.service..", "..domain.port..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.yaml..",
            "com.fasterxml.jackson.databind..",
            "com.fasterxml.jackson.dataformat..",
            "com.networknt..")
        .because(
            "Case-type config records + service + ports stay pure Java. YAML parsing, JSON "
                + "Schema trees, and JSON-Schema validation are infrastructure concerns. "
                + "Story 2.1 AC11.")
        .check(CLASSES);
  }

  @Test
  void caseTypeConfigPlumbingLivesInInfrastructureConfig() {
    classes()
        .that()
        .haveSimpleName("CaseTypeYamlLoader")
        .or()
        .haveSimpleName("ConfigValidator")
        .or()
        .haveSimpleName("JsonSchemaGenerator")
        .or()
        .haveSimpleName("CaseTypeRegistry")
        .or()
        .haveSimpleName("CaseTypeStartupLoader")
        .or()
        .haveSimpleName("YamlLineIndex")
        .or()
        .haveSimpleName("RawCaseTypeConfig")
        .should()
        .resideInAPackage("..infrastructure.config..")
        .because(
            "Story 2.1 AC11: YAML/Jackson/networknt mechanics must not leak out of "
                + "infrastructure/config/.")
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
