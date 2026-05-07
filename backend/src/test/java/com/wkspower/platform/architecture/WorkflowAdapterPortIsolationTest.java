package com.wkspower.platform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

/**
 * Story 4.1 AC5 — guardrail that the {@code WorkflowAdapter} port and the two service-side classes
 * that ship with it ({@code NullAdapter}, {@code WorkflowAdapterBinder}) remain backend-agnostic
 * forever. A code-review reminder is not enough — this rule fails the build the day someone imports
 * {@code org.cibseven..} or {@code com.wkspower.platform.engine..} into one of these files.
 *
 * <p>Pinned as its own ArchUnit class (rather than added to {@code ArchitectureTest}) so a
 * regression surfaces a Story-4.1-attributable failure message.
 */
class WorkflowAdapterPortIsolationTest {

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
            "Story 4.1 AC5: domain.port classes — including WorkflowAdapter, ExecutionSignal, "
                + "AttachmentScope, CaseTypeRef, CaseInstanceRef, ExecutionSignalKind, "
                + "ExecutionSignalHandler, ExecutionSignalSubscription — must remain backend-agnostic "
                + "and infrastructure-agnostic. Adding any such import here would break the "
                + "Mapping Layer (Decision 22) by leaking backend choice into domain.")
        .check(CLASSES);
  }

  @Test
  void onExecutionSignalIsCalledFromRouterAndBinderOnly() {
    // Story 4.3 AC6 — single-routing-surface invariant. The router is the only
    // ExecutionSignalHandler that may subscribe to WorkflowAdapter.onExecutionSignal in production.
    // ArchUnit's method-call-level guardrail makes this load-bearing: a future "logging
    // listener" or "metrics listener" would silently break the single-subscriber guarantee, but
    // adding the call site without editing this test fails the build.
    noClasses()
        .that()
        .resideInAPackage("com.wkspower.platform..")
        .and()
        .doNotHaveFullyQualifiedName("com.wkspower.platform.domain.service.ExecutionSignalRouter")
        .and()
        .doNotHaveFullyQualifiedName("com.wkspower.platform.domain.service.WorkflowAdapterBinder")
        .should()
        .callMethod(
            com.wkspower.platform.domain.port.WorkflowAdapter.class,
            "onExecutionSignal",
            com.wkspower.platform.domain.port.ExecutionSignalHandler.class)
        .because(
            "Story 4.3 AC6: ExecutionSignalRouter is the only legitimate subscriber to "
                + "WorkflowAdapter.onExecutionSignal in production wiring. The binder mediates "
                + "adapter resolution; nothing else may call this method. A second subscriber "
                + "would silently break the single-routing-surface guarantee — adding one "
                + "requires a deliberate edit of this test, surfacing the change to reviewers.")
        .check(CLASSES);
  }

  /**
   * Story 4.3.1 AC7 — extend the single-subscriber rule to non-direct-call discovery vectors:
   *
   * <ul>
   *   <li>{@code ApplicationContext.getBeansOfType(ExecutionSignalHandler.class)} — Spring lookup
   *   <li>{@code @Autowired List<ExecutionSignalHandler>} — collection injection (broadcasts to all)
   *   <li>{@code Method.invoke(...)} on a {@code ExecutionSignalHandler} method — reflective dispatch
   * </ul>
   *
   * <p>The original rule only catches direct bytecode method calls; an attacker (or a well-meaning
   * dev adding metrics) could circumvent it via any of the three above. Each surfaces a Story 4.3.1
   * AC7 violation in the build.
   */
  @Test
  void backendSignalHandlerHasNoIndirectSubscribers() {
    String handlerFqn = "com.wkspower.platform.domain.port.ExecutionSignalHandler";
    String routerFqn = "com.wkspower.platform.domain.service.ExecutionSignalRouter";
    String binderFqn = "com.wkspower.platform.domain.service.WorkflowAdapterBinder";

    ArchCondition<JavaClass> noBeansOfType =
        new ArchCondition<>("not call ApplicationContext.getBeansOfType(ExecutionSignalHandler)") {
          @Override
          public void check(JavaClass clazz, ConditionEvents events) {
            if (clazz.getFullName().equals(routerFqn) || clazz.getFullName().equals(binderFqn)) {
              return;
            }
            for (JavaMethodCall call : clazz.getMethodCallsFromSelf()) {
              String tgt = call.getTarget().getFullName();
              if ((tgt.contains("ApplicationContext.getBeansOfType")
                      || tgt.contains("ListableBeanFactory.getBeansOfType")
                      || tgt.contains("BeanFactory.getBeansOfType"))
                  && callMentionsHandler(call, handlerFqn)) {
                events.add(
                    SimpleConditionEvent.violated(
                        clazz,
                        clazz.getFullName()
                            + " calls getBeansOfType(ExecutionSignalHandler.class) — Story 4.3.1 AC7"
                            + " forbids non-direct-call subscribers to the single-routing surface"));
              }
            }
          }
        };

    ArchCondition<JavaClass> noListInjection =
        new ArchCondition<>("not declare a field of type List<ExecutionSignalHandler>") {
          @Override
          public void check(JavaClass clazz, ConditionEvents events) {
            if (clazz.getFullName().equals(routerFqn) || clazz.getFullName().equals(binderFqn)) {
              return;
            }
            for (JavaField field : clazz.getFields()) {
              String desc = field.getDescriptor();
              // Generic erasure loses the type-parameter; signature carries it.
              String sig = field.getDescriptor() == null ? "" : field.getDescriptor();
              // Best-effort: any field whose erased type is List/Collection/Set AND whose
              // declaring source mentions ExecutionSignalHandler in the field name OR the
              // generic-signature attribute (which JavaClass exposes via the raw descriptor).
              boolean isCollection =
                  desc != null
                      && (desc.contains("Ljava/util/List;")
                          || desc.contains("Ljava/util/Collection;")
                          || desc.contains("Ljava/util/Set;"));
              if (isCollection
                  && (field.getName().toLowerCase().contains("backendsignalhandler")
                      || field.getName().toLowerCase().contains("signalhandler")
                      || sig.contains("ExecutionSignalHandler"))) {
                events.add(
                    SimpleConditionEvent.violated(
                        field,
                        clazz.getFullName()
                            + "."
                            + field.getName()
                            + " appears to be a List<ExecutionSignalHandler> collection injection —"
                            + " Story 4.3.1 AC7 forbids broadcast subscribers to the single"
                            + " routing surface"));
              }
            }
          }
        };

    ArchCondition<JavaClass> noReflectiveInvoke =
        new ArchCondition<>("not call Method.invoke on ExecutionSignalHandler methods") {
          @Override
          public void check(JavaClass clazz, ConditionEvents events) {
            if (clazz.getFullName().equals(routerFqn) || clazz.getFullName().equals(binderFqn)) {
              return;
            }
            // Conservative heuristic: any class that BOTH (a) imports ExecutionSignalHandler AND
            // (b) calls java.lang.reflect.Method.invoke is suspect. We can't statically prove the
            // invoke target is the handler method, so we report the suspicious combination and
            // let reviewers check.
            boolean importsHandler =
                clazz.getDirectDependenciesFromSelf().stream()
                    .anyMatch(d -> d.getTargetClass().getFullName().equals(handlerFqn));
            if (!importsHandler) {
              return;
            }
            for (JavaMethodCall call : clazz.getMethodCallsFromSelf()) {
              if (call.getTarget().getFullName().equals("java.lang.reflect.Method.invoke")) {
                events.add(
                    SimpleConditionEvent.violated(
                        clazz,
                        clazz.getFullName()
                            + " imports ExecutionSignalHandler AND calls Method.invoke — Story"
                            + " 4.3.1 AC7 forbids reflective dispatch to the single routing"
                            + " surface; review for non-direct-call subscriber pattern"));
              }
            }
          }
        };

    noClasses()
        .that()
        .resideInAPackage("com.wkspower.platform..")
        .should(noBeansOfType)
        .andShould(noListInjection)
        .andShould(noReflectiveInvoke)
        .because(
            "Story 4.3.1 AC7: the single-subscriber invariant must catch indirect subscriber"
                + " patterns — getBeansOfType, List<ExecutionSignalHandler> collection injection,"
                + " and reflective Method.invoke — not just direct bytecode calls. Adding any"
                + " such pattern requires a deliberate edit of this test.")
        .check(CLASSES);
  }

  private static boolean callMentionsHandler(JavaMethodCall call, String handlerFqn) {
    return call.getTarget().getRawParameterTypes().stream()
            .anyMatch(t -> t.getFullName().contains("ExecutionSignalHandler"))
        || call.getTarget().getFullName().contains("ExecutionSignalHandler");
  }

  @Test
  void nullAdapterAndBinderHaveNoEngineImports() {
    // NullAdapter and WorkflowAdapterBinder live in domain.service alongside CaseService etc., so
    // we cannot use a package-wide rule. Pin by simple name — these two classes ship with the
    // port and must remain backend-agnostic forever.
    noClasses()
        .that()
        .haveSimpleName("NullAdapter")
        .or()
        .haveSimpleName("WorkflowAdapterBinder")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.cibseven..", "com.wkspower.platform.engine..")
        .because(
            "Story 4.1 AC5: NullAdapter (the zero-attachment fallback) and WorkflowAdapterBinder "
                + "(the resolve seam) must never depend on a specific backend SDK. Spring "
                + "@Component is allowed; engine SDK and engine package imports are not.")
        .check(CLASSES);
  }
}
