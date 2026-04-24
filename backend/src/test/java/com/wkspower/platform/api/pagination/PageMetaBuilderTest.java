package com.wkspower.platform.api.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.api.dto.ApiResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageMetaBuilderTest {

  @Test
  void metaContainsTotalPageAndSize() {
    Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(2, 5), 42);
    Map<String, Object> meta = PageMetaBuilder.meta(page);
    assertThat(meta).containsEntry("total", 42L).containsEntry("page", 2).containsEntry("size", 5);
  }

  @Test
  void pagedWrapsContentAndMeta() {
    Page<Integer> source = new PageImpl<>(List.of(1, 2, 3), PageRequest.of(0, 10), 3);
    ApiResponse<List<String>> response = PageMetaBuilder.paged(source, i -> "#" + i);
    assertThat(response.data()).containsExactly("#1", "#2", "#3");
    assertThat(response.meta()).containsEntry("total", 3L).containsEntry("page", 0);
    assertThat(response.error()).isNull();
  }
}
