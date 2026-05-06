package com.wkspower.platform.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig;
import java.net.URL;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice tests for {@code GET /api/theme.css} (Story 14.5).
 *
 * <p>Two nested classes cover the two branches:
 *
 * <ul>
 *   <li>{@link WhenNoCssPathConfigured} — blank path → 204 No Content.
 *   <li>{@link WhenCssFileConfigured} — valid file path → 200 + {@code text/css} body.
 * </ul>
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
  }

  // ---------------------------------------------------------------------------
  // Branch 2: WKS_THEME_CSS_PATH points to test-theme.css → 200 + CSS body
  // ---------------------------------------------------------------------------

  /**
   * Resolves the absolute filesystem path of {@code test-theme.css} from the test classpath and
   * registers it as {@code wks.theme.css-path} before the application context starts.
   */
  static class TestThemePathInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
      URL url = getClass().getClassLoader().getResource("test-theme.css");
      if (url == null) {
        throw new IllegalStateException("test-theme.css not found in test classpath");
      }
      String path = url.getPath();
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
          .andExpect(content().string(Matchers.containsString("--primary: #ff0000")));
    }
  }
}
