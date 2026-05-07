package com.wkspower.platform.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig;
import java.net.URL;
import java.nio.file.Paths;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice tests for {@code GET /api/theme.css} (Story 14.5).
 *
 * <p>Three branches:
 *
 * <ul>
 *   <li>{@link WhenNoCssPathConfigured} — blank path → 204 No Content.
 *   <li>{@link WhenFileIsMissing} — path configured but file absent → 204 No Content.
 *   <li>{@link WhenCssFileConfigured} — valid file path → 200 + {@code text/css} body.
 * </ul>
 *
 * <p>Each branch also asserts that the endpoint is reachable without authentication (M3 — {@code
 * permitAll} guard).
 */
class ThemeControllerTest {

  // ---------------------------------------------------------------------------
  // Branch 1: no WKS_THEME_CSS_PATH configured → 204
  // ---------------------------------------------------------------------------

  @WebMvcTest(ThemeController.class)
  @Import({SecurityConfig.class, JwtAuthenticationFilter.class})
  @TestPropertySource(properties = "wks.theme.css-path=")
  static class WhenNoCssPathConfigured {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserRepository userRepository;

    @Test
    void returns204WhenNoThemeConfigured() throws Exception {
      mockMvc.perform(get("/api/theme.css")).andExpect(status().isNoContent());
    }

    /** M3 — anonymous users must not receive 401/403 (permitAll assertion). */
    @Test
    @WithAnonymousUser
    void anonymousUserIsPermitted() throws Exception {
      mockMvc
          .perform(get("/api/theme.css"))
          .andExpect(
              status().is(Matchers.not(Matchers.either(Matchers.is(401)).or(Matchers.is(403)))));
    }
  }

  // ---------------------------------------------------------------------------
  // Branch 2: path configured but file does not exist → 204
  // ---------------------------------------------------------------------------

  @WebMvcTest(ThemeController.class)
  @Import({SecurityConfig.class, JwtAuthenticationFilter.class})
  @TestPropertySource(properties = "wks.theme.css-path=/tmp/nonexistent-99999.css")
  static class WhenFileIsMissing {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserRepository userRepository;

    @Test
    void returns204WhenFileIsMissing() throws Exception {
      mockMvc.perform(get("/api/theme.css")).andExpect(status().isNoContent());
    }
  }

  // ---------------------------------------------------------------------------
  // Branch 3: WKS_THEME_CSS_PATH points to test-theme.css → 200 + CSS body
  // ---------------------------------------------------------------------------

  /**
   * Resolves the absolute filesystem path of {@code test-theme.css} from the test classpath and
   * registers it as {@code wks.theme.css-path} before the application context starts.
   *
   * <p>Uses {@code Paths.get(url.toURI())} (M1) instead of {@code url.getPath()} to avoid
   * URL-encoding issues when the workspace path contains spaces.
   */
  static class TestThemePathInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
      URL url = getClass().getClassLoader().getResource("test-theme.css");
      if (url == null) {
        throw new IllegalStateException("test-theme.css not found in test classpath");
      }
      String path;
      try {
        path = Paths.get(url.toURI()).toString(); // M1: safe for paths with spaces
      } catch (java.net.URISyntaxException e) {
        throw new IllegalStateException("Cannot resolve test-theme.css URI: " + url, e);
      }
      ctx.getEnvironment()
          .getPropertySources()
          .addFirst(
              new MapPropertySource(
                  "theme-test-props", java.util.Map.of("wks.theme.css-path", path)));
    }
  }

  @WebMvcTest(ThemeController.class)
  @Import({SecurityConfig.class, JwtAuthenticationFilter.class})
  @ContextConfiguration(initializers = TestThemePathInitializer.class)
  static class WhenCssFileConfigured {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserRepository userRepository;

    @Test
    void returnsCssContentWhenThemeFileConfigured() throws Exception {
      mockMvc
          .perform(get("/api/theme.css"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith("text/css"))
          .andExpect(content().string(Matchers.containsString("--primary: #ff0000")))
          .andExpect(header().string("Cache-Control", "public, max-age=300")); // I2
    }

    /** M3 — anonymous users must receive the CSS, not a 401/403. */
    @Test
    @WithAnonymousUser
    void anonymousUserReceivesCss() throws Exception {
      mockMvc
          .perform(get("/api/theme.css"))
          .andExpect(
              status().is(Matchers.not(Matchers.either(Matchers.is(401)).or(Matchers.is(403)))));
    }
  }
}
