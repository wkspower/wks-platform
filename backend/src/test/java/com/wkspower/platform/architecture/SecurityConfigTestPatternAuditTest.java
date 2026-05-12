package com.wkspower.platform.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Story 7-6 AC-4 — ArchUnit regression guard for the LicenseService mock pattern.
 *
 * <p>Any test class that (a) is annotated with {@code @WebMvcTest} AND (b) depends on {@link
 * com.wkspower.platform.security.SecurityConfig} MUST declare a {@code @MockitoBean LicenseService}
 * field. Without it, the {@link com.wkspower.platform.security.SamlGatingFilter} (which depends on
 * {@code LicenseService}) will fail context startup with an unsatisfied-bean or
 * NullPointerException error.
 *
 * <p>This rule codifies the N=4 structural threshold (Sprint 10 retro Action 5): occurrence count
 * hit 4 across PRs #422, #431, #432, and Story 7-6.
 *
 * <p>Path chosen: (a) apply the mock verbatim + file follow-up story {@code
 * 7-6-1-security-config-test-support-annotation} for a structural meta-annotation option.
 *
 * <p>Test classes are imported from the test classpath; production classes are excluded.
 */
class SecurityConfigTestPatternAuditTest {

  private static final JavaClasses TEST_CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.ONLY_INCLUDE_TESTS)
          .importPackages("com.wkspower.platform");

  private static final String SECURITY_CONFIG_FQN = "com.wkspower.platform.security.SecurityConfig";
  private static final String LICENSE_SERVICE_FQN =
      "com.wkspower.platform.domain.service.LicenseService";

  /** Condition: the test class must have a @MockitoBean LicenseService field. */
  private static final ArchCondition<JavaClass> MOCKS_LICENSE_SERVICE =
      new ArchCondition<>("have a @MockitoBean LicenseService field") {
        @Override
        public void check(JavaClass clazz, ConditionEvents events) {
          boolean found =
              clazz.getAllFields().stream()
                  .anyMatch(SecurityConfigTestPatternAuditTest::isMockitoLicenseServiceField);
          if (!found) {
            events.add(
                SimpleConditionEvent.violated(
                    clazz,
                    clazz.getFullName()
                        + " imports SecurityConfig but is missing @MockitoBean LicenseService."
                        + " SamlGatingFilter requires LicenseService — omitting the mock causes"
                        + " context-startup failure. Story 7-6 AC-4: add"
                        + " '@MockitoBean LicenseService licenseService;' to the test class."));
          }
        }
      };

  @Test
  void all_securityConfig_importing_slices_mock_licenseService() {
    ArchRuleDefinition.classes()
        .that()
        .areAnnotatedWith(org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest.class)
        .and(importSecurityConfig())
        .should(MOCKS_LICENSE_SERVICE)
        .allowEmptyShould(true)
        .because(
            "Story 7-6 AC-4: any @WebMvcTest slice that imports SecurityConfig must mock"
                + " LicenseService. SamlGatingFilter is registered in the security filter chain"
                + " and depends on LicenseService. This rule catches the omission at build time"
                + " rather than at context-startup with a cryptic NullPointerException.")
        .check(TEST_CLASSES);
  }

  /**
   * Predicate: the class has a direct class-level dependency on {@code SecurityConfig}.
   *
   * <p>This covers both {@code @Import(SecurityConfig.class)} (which makes SecurityConfig a direct
   * class dependency) and any other form of static reference to SecurityConfig.
   */
  private static com.tngtech.archunit.base.DescribedPredicate<JavaClass> importSecurityConfig() {
    return new com.tngtech.archunit.base.DescribedPredicate<>("import SecurityConfig") {
      @Override
      public boolean test(JavaClass clazz) {
        return clazz.getDirectDependenciesFromSelf().stream()
            .anyMatch(dep -> dep.getTargetClass().getFullName().equals(SECURITY_CONFIG_FQN));
      }
    };
  }

  private static boolean isMockitoLicenseServiceField(JavaField field) {
    boolean isLicenseService = field.getRawType().getFullName().equals(LICENSE_SERVICE_FQN);
    if (!isLicenseService) {
      return false;
    }
    boolean hasMockitoBean = field.isAnnotatedWith(MockitoBean.class);
    // Tolerate the deprecated @MockBean (pre-Boot-3.4) for legacy test classes already in the repo.
    @SuppressWarnings("deprecation")
    boolean hasLegacyMockBean =
        field.isAnnotatedWith(org.springframework.boot.test.mock.mockito.MockBean.class);
    return hasMockitoBean || hasLegacyMockBean;
  }
}
