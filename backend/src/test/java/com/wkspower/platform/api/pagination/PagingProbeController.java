package com.wkspower.platform.api.pagination;

import com.wkspower.platform.api.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only controller — exercised by {@link PagingProbeWebMvcTest} to verify the pagination
 * machinery end-to-end through a real Spring MVC slice (envelope shape, error codes, meta values).
 * Registered only under the {@code test} profile so it never ships in a production JAR.
 */
@RestController
@Profile("test")
class PagingProbeController implements SortWhitelist {

  static final Set<String> ALLOWED = Set.of("updatedAt", "createdAt");

  @Override
  public Set<String> allowedSortProperties() {
    return ALLOWED;
  }

  @GetMapping("/_test/paging-probe")
  ApiResponse<List<String>> probe(HttpServletRequest request) {
    // Read params directly from the servlet — Spring's default @RequestParam List<String> splits
    // on commas, which mangles `sort=updatedAt,desc` into two tokens. getParameterValues keeps
    // each value intact.
    Integer page = parseIntOrNull(request.getParameter("page"));
    Integer size = parseIntOrNull(request.getParameter("size"));
    String[] sortValues = request.getParameterValues("sort");
    List<String> sort = sortValues == null ? List.of() : Arrays.asList(sortValues);

    Pageable pageable = PageRequestParams.of(page, size, sort).toPageable(this);
    Page<String> page42 = new PageImpl<>(List.of("x", "y", "z"), pageable, 42L);
    return PageMetaBuilder.paged(page42, s -> s);
  }

  private static Integer parseIntOrNull(String raw) {
    return raw == null || raw.isBlank() ? null : Integer.valueOf(raw);
  }
}
