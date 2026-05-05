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
            "Domain must be pure Java — no Spring, no JPA, no embedded BPMN engine SDK. "
                + "This is NFR36 and is non-negotiable. The org.cibseven.. literal is the current "
                + "engine distribution; the rule purpose is generic — keep the engine SDK out of "
                + "domain regardless of vendor.")
        .check(CLASSES);
  }

  @Test
  void onlyEngineAdapterImportsTheBpmnEngineSdk() {
    noClasses()
        .that()
        .resideOutsideOfPackage("..engine..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.cibseven..")
        .because(
            "BPMN engine SDK imports belong exclusively to engine/. This is NFR35 and isolates "
                + "the engine behind a port. The org.cibseven.. literal is the current engine "
                + "distribution; the boundary is generic — only engine/ may import the SDK "
                + "regardless of which vendor it is.")
        .check(CLASSES);
  }

  @Test
  void taskDomainHasNoSpringOrEngineImports() {
    // Story 2.4 AC8 — explicit gate on the new task-domain types. Covered by the blanket
    // domainHasNoFrameworkImports rule; this rule pins it specifically so a regression surfaces a
    // Story-2.4-attributable failure message.
    noClasses()
        .that()
        .haveSimpleName("Task")
        .or()
        .haveSimpleName("TaskService")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springframework..", "org.cibseven..")
        .because(
            "Task domain model + TaskService stay framework-free — Story 2.4 AC8. CIB seven types"
                + " never reach domain code; the engine adapter translates engine exceptions into"
                + " WksConflictException / WksNotFoundException at the boundary.")
        .check(CLASSES);
  }

  @Test
  void onlyEngineAdapterAndListenersImportTheEngineDelegateApi() {
    // Story 2.4 AC8 — the engine-callback hexagonal pattern requires that ONLY the engine adapter
    // (CibSevenWorkflowEngine + CaseStatusEnginePlugin) and engine/listeners/* hold imports of
    // org.cibseven.bpm.engine.delegate.* (the API CIB seven calls into via reflection).
    // Everything else goes through the WorkflowEngine port + the CaseStatusUpdater port.
    //
    // Scope is narrower than ..engine.. on purpose (Story 2.4 review): a regression that adds a
    // delegate import to a sibling class under engine/ (e.g. engine/CibSevenJobAdapter) would
    // otherwise pass the rule.
    noClasses()
        .that()
        .resideOutsideOfPackage("..engine.listeners..")
        .and()
        .haveSimpleNameNotEndingWith("WorkflowEngine")
        .and()
        .haveSimpleNameNotEndingWith("EnginePlugin")
        .and()
        .haveSimpleNameNotEndingWith("BpmnParseListener")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.cibseven.bpm.engine.delegate..")
        .because(
            "ExecutionListener + DelegateExecution are engine-callback types — they belong inside"
                + " CibSevenWorkflowEngine, CaseStatusEnginePlugin, the BpmnParseListener, and"
                + " engine/listeners/ (the CaseStatusListener). Story 2.4 AC8.")
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
  void workflowDomainHasNoSpringOrEngineImports() {
    // Story 2.2 AC2: domain/workflow records (DeploymentRequest, DeploymentResult, DeploymentInfo)
    // are plain Java. Engine-specific types belong only inside engine/.
    noClasses()
        .that()
        .resideInAPackage("..domain.workflow..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springframework..", "org.cibseven..")
        .because("Workflow domain records stay framework-free — Story 2.2 AC2.")
        .check(CLASSES);
  }

  @Test
  void workflowEngineImplementationsLiveOnlyInEnginePackage() {
    // Story 2.2 AC2/AC3: the WorkflowEngine port lives in domain/port, but every implementer
    // must reside in ..engine.. so the only-engine-imports-cib-seven rule remains airtight.
    classes()
        .that()
        .implement("com.wkspower.platform.domain.port.WorkflowEngine")
        .should()
        .resideInAPackage("..engine..")
        .allowEmptyShould(true)
        .because("WorkflowEngine implementations belong inside engine/ — Story 2.2 AC2/AC3.")
        .check(CLASSES);
  }

  @Test
  void workflowEngineAdapterLivesOnlyInEnginePackage() {
    // Story 2.2 AC3: the adapter is the sole non-test class allowed to import the engine SDK's
    // RepositoryService — pin its location explicitly so a misplaced future copy fails the build
    // with a clearer message than the blanket boundary rule.
    classes()
        .that()
        .haveSimpleName("CibSevenWorkflowEngine")
        .should()
        .resideInAPackage("..engine..")
        .allowEmptyShould(true)
        .because("Workflow engine adapter resides exclusively in engine/ — Story 2.2 AC3.")
        .check(CLASSES);
  }

  @Test
  void caseDomainHasNoSpringOrJpaImports() {
    // Story 2.3 AC9 — explicit gate on the new case-domain types. Covered by
    // domainHasNoFrameworkImports as a blanket; this rule pins it specifically so a regression
    // surfaces a Story-2.3-attributable failure message.
    noClasses()
        .that()
        .resideInAnyPackage(
            "..domain.model..",
            "..domain.service..",
            "..domain.event..",
            "..domain.page..",
            "..domain.exception..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework..",
            "jakarta.persistence..",
            "com.fasterxml.jackson.databind..",
            "com.networknt..",
            "org.cibseven..")
        .because(
            "Case domain (model, service, events, pagination, exceptions) stays framework-free — "
                + "Story 2.3 AC9. Engine-first ordering and JSON-schema validation live behind"
                + " ports.")
        .check(CLASSES);
  }

  @Test
  void caseRepositoryImplementationsLiveInInfrastructure() {
    // Story 2.3 AC9 — defense against a future repository-shaped class showing up in engine/ or
    // api/. Implementations of CaseRepository must sit in infrastructure/persistence/ only.
    classes()
        .that()
        .implement("com.wkspower.platform.domain.port.CaseRepository")
        .should()
        .resideInAPackage("..infrastructure.persistence..")
        .allowEmptyShould(true)
        .because(
            "CaseRepository implementations belong inside infrastructure/persistence/ — Story 2.3"
                + " AC9.")
        .check(CLASSES);
  }

  @Test
  void mappingDomainModelHasNoFrameworkImports() {
    // Story 4.2 AC10 task 5 / AC4 — the new mapping value-object types (MappingDefinition,
    // AttachmentDefinition, MappingChangeClass) stay framework-free. Reuse BackendSignalKind
    // from domain/port; never duplicate the enum.
    //
    // Scope is pinned by simple-name on purpose: FieldType / StatusColor (Story 2.1) carry
    // Jackson annotations as a documented pre-existing exception. Pinning the rule to the
    // Story-4.2 types keeps the assertion tight without weakening 2.1's posture.
    noClasses()
        .that()
        .haveSimpleName("MappingDefinition")
        .or()
        .haveSimpleName("AttachmentDefinition")
        .or()
        .haveSimpleName("MappingChangeClass")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework..",
            "com.fasterxml.jackson..",
            "jakarta.persistence..",
            "org.cibseven..")
        .because(
            "MappingDefinition / AttachmentDefinition / MappingChangeClass stay pure Java —"
                + " Story 4.2 AC4 / NFR36. Reuse BackendSignalKind from domain/port; never"
                + " duplicate the enum.")
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
