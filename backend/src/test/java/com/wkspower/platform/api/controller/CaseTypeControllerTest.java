package com.wkspower.platform.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.security.AuthenticatedUser;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig;
import com.wkspower.platform.security.WksUserPrincipal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Slice test for {@link CaseTypeController} (Story 2.5 AC9). */
@WebMvcTest(CaseTypeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@ActiveProfiles("dev")
class CaseTypeControllerTest {

  private static final UUID ACTOR_ID = UUID.randomUUID();

  @Autowired MockMvc mockMvc;

  @MockitoBean CaseTypeReader caseTypeReader;

  @MockitoBean(name = "caseTypePermissionEvaluator")
  CaseTypePermissionEvaluator caseTypePermissionEvaluator;

  @MockitoBean JwtTokenProvider jwtTokenProvider;
  @MockitoBean UserRepository userRepository;

  // ---- GET /api/case-types (list) ---------------------------------------

  @Test
  void listReturns200AndFiltersToViewVerbHolders() throws Exception {
    CaseTypeConfig loan = loanType();
    CaseTypeConfig hr = hrType();
    when(caseTypeReader.all()).thenReturn(List.of(loan, hr));
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("hr-onboarding"), eq("view")))
        .thenReturn(false);

    mockMvc
        .perform(get("/api/case-types").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].id").value("loan-application"))
        .andExpect(jsonPath("$.data[0].displayName").value("Loan Application"))
        .andExpect(jsonPath("$.data[0].version").value(1))
        .andExpect(jsonPath("$.data[0].statusCount").value(1))
        .andExpect(jsonPath("$.data[0].fieldCount").value(1));
  }

  @Test
  void listSortsByDisplayNameAsc() throws Exception {
    CaseTypeConfig loan = loanType(); // "Loan Application"
    CaseTypeConfig hr = hrType(); // "HR Onboarding"
    when(caseTypeReader.all()).thenReturn(List.of(loan, hr));
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("hr-onboarding"), eq("view")))
        .thenReturn(true);

    mockMvc
        .perform(get("/api/case-types").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].displayName").value("HR Onboarding"))
        .andExpect(jsonPath("$.data[1].displayName").value("Loan Application"));
  }

  @Test
  void listReturns401WhenAnonymous() throws Exception {
    mockMvc.perform(get("/api/case-types")).andExpect(status().isUnauthorized());
  }

  // ---- GET /api/case-types/{id} -----------------------------------------

  @Test
  void getReturns200WithFullViewDto() throws Exception {
    when(caseTypeReader.find("loan-application")).thenReturn(Optional.of(loanType()));
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);

    mockMvc
        .perform(get("/api/case-types/loan-application").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value("loan-application"))
        .andExpect(jsonPath("$.data.displayName").value("Loan Application"))
        .andExpect(jsonPath("$.data.version").value(1))
        .andExpect(jsonPath("$.data.fields.length()").value(1))
        .andExpect(jsonPath("$.data.fields[0].type").value("text"))
        .andExpect(jsonPath("$.data.statuses.length()").value(1))
        .andExpect(jsonPath("$.data.statuses[0].color").value("zinc"))
        .andExpect(jsonPath("$.data.listColumns.length()").value(1))
        .andExpect(jsonPath("$.data.roles").doesNotExist());
  }

  @Test
  void getReturns404WhenIdUnknown() throws Exception {
    when(caseTypeReader.find("missing")).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/case-types/missing").with(officerAuth()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("WKS-API-404"));
  }

  @Test
  void getReturns403WhenViewVerbDenied() throws Exception {
    when(caseTypeReader.find("loan-application")).thenReturn(Optional.of(loanType()));
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(false);

    mockMvc
        .perform(get("/api/case-types/loan-application").with(officerAuth()))
        .andExpect(status().isForbidden());
  }

  @Test
  void getReturns401WhenAnonymous() throws Exception {
    mockMvc.perform(get("/api/case-types/loan-application")).andExpect(status().isUnauthorized());
  }

  // ---- helpers ----------------------------------------------------------

  private static CaseTypeConfig loanType() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("officer", List.of(Permission.VIEW, Permission.CREATE))));
  }

  private static CaseTypeConfig hrType() {
    return new CaseTypeConfig(
        "hr-onboarding",
        "HR Onboarding",
        2,
        null,
        new WorkflowRef("hr-onboarding.bpmn"),
        List.of(
            new FieldDefinition("employee", "Employee", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("startDate", "Start", FieldType.DATE, true, 1, List.of(), null)),
        List.of(
            new StatusDefinition("draft", "Draft", StatusColor.ZINC),
            new StatusDefinition("active", "Active", StatusColor.EMERALD)),
        List.of("employee", "startDate"),
        List.of(new RoleDefinition("hr", List.of(Permission.VIEW))));
  }

  private static org.springframework.test.web.servlet.request.RequestPostProcessor officerAuth() {
    AuthenticatedUser user = new AuthenticatedUser(ACTOR_ID, "officer@x", Set.of("officer"));
    WksUserPrincipal principal = new WksUserPrincipal(user);
    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            principal,
            principal.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_officer")));
    return authentication(auth);
  }
}
