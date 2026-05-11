package com.wkspower.platform.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.WksFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Story 7-5 AC1 / AC2 / AC3 — verifies the SAML-path gate.
 *
 * <ul>
 *   <li>AC1: feature off → 404 + {@code code=WKS-LIC-003}
 *   <li>AC2: feature on → 401 fallthrough via {@link WksAuthenticationEntryPoint}
 *   <li>AC3: hot-reload — flipping the mock between calls in the same Spring context changes the
 *       gate state without {@code @DirtiesContext}
 * </ul>
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:samlgating;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      "wks.bootstrap.production-validation.enabled=false"
    })
class SecurityConfigSamlGatingTest {

  @Autowired private TestRestTemplate rest;
  @Autowired private ObjectMapper json;
  @Autowired private ApplicationContext ctx;
  @MockBean private LicenseService licenseService;

  @Test
  void disabledFeatureReturnsNotFoundWithWksLic003() throws Exception {
    when(licenseService.isFeatureEnabled(WksFeature.AUTH_SSO)).thenReturn(false);
    when(licenseService.getTier()).thenReturn("oss");

    ResponseEntity<String> response = rest.getForEntity("/api/auth/saml/metadata", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    JsonNode body = json.readTree(response.getBody());
    assertThat(body.path("code").asText()).isEqualTo("WKS-LIC-003");
    assertThat(body.path("message").asText())
        .isEqualTo("SSO/SAML is not available on the current license.");
    assertThat(body.has("field")).isTrue();
    assertThat(body.path("field").isNull()).isTrue();
  }

  @Test
  void enabledFeatureFallsThroughToUnauthorized() {
    when(licenseService.isFeatureEnabled(WksFeature.AUTH_SSO)).thenReturn(true);
    when(licenseService.getTier()).thenReturn("enterprise");

    ResponseEntity<String> response = rest.getForEntity("/api/auth/saml/metadata", String.class);

    // No SAML provider on classpath yet (Story 10.4) — chain reaches FilterSecurityInterceptor
    // which sees /api/**.authenticated() and the EntryPoint returns 401.
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void hotReloadFlipsGateWithoutContextRebuild() throws Exception {
    // Capture the SamlGatingFilter bean identity BEFORE the first hit.
    SamlGatingFilter filterBefore = ctx.getBean(SamlGatingFilter.class);

    when(licenseService.isFeatureEnabled(WksFeature.AUTH_SSO)).thenReturn(false);
    when(licenseService.getTier()).thenReturn("oss");

    ResponseEntity<String> first = rest.getForEntity("/api/auth/saml/metadata", String.class);
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(json.readTree(first.getBody()).path("code").asText()).isEqualTo("WKS-LIC-003");

    // Simulate license hot-reload — same Spring context, same SecurityFilterChain bean.
    when(licenseService.isFeatureEnabled(WksFeature.AUTH_SSO)).thenReturn(true);
    when(licenseService.getTier()).thenReturn("enterprise");

    ResponseEntity<String> second = rest.getForEntity("/api/auth/saml/metadata", String.class);
    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    // The exact same SamlGatingFilter bean instance handled both requests — no context rebuild,
    // no proxy swap, no bean re-creation between the OSS hit and the Enterprise hit.
    SamlGatingFilter filterAfter = ctx.getBean(SamlGatingFilter.class);
    assertThat(filterAfter).isSameAs(filterBefore);

    // Per-request consultation: the filter must call LicenseService.isFeatureEnabled(AUTH_SSO)
    // exactly once per SAML-path request — never cached on the filter side. Two SAML hits ⇒
    // two consultations.
    verify(licenseService, times(2)).isFeatureEnabled(WksFeature.AUTH_SSO);
  }

  @Test
  void subPathsAreAlsoGated() {
    when(licenseService.isFeatureEnabled(WksFeature.AUTH_SSO)).thenReturn(false);
    when(licenseService.getTier()).thenReturn("oss");

    for (String path :
        new String[] {
          "/api/auth/saml/metadata",
          "/api/auth/saml/init",
          "/api/auth/saml/acs",
          "/api/auth/saml/logout"
        }) {
      ResponseEntity<String> r = rest.getForEntity(path, String.class);
      assertThat(r.getStatusCode()).as("path=%s", path).isEqualTo(HttpStatus.NOT_FOUND);
    }
  }

  @Test
  void nonSamlPathsAreUnaffected() {
    when(licenseService.isFeatureEnabled(WksFeature.AUTH_SSO)).thenReturn(false);

    // /api/cases is /api/** but not /api/auth/saml/** — must still 401 via the normal entry
    // point, not 404 from the gate.
    ResponseEntity<String> response = rest.getForEntity("/api/cases", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
