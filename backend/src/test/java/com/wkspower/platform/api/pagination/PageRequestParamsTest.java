package com.wkspower.platform.api.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksValidationException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageRequestParamsTest {

  private static final SortWhitelist WHITELIST = () -> Set.of("updatedAt", "createdAt");

  @Test
  void defaultsApplyWhenNothingProvided() {
    Pageable p = new PageRequestParams(0, 20, null).toPageable(WHITELIST);
    assertThat(p.getPageNumber()).isZero();
    assertThat(p.getPageSize()).isEqualTo(20);
    assertThat(p.getSort().isSorted()).isFalse();
  }

  @Test
  void sizeAbove100IsSilentlyClamped() {
    Pageable p = new PageRequestParams(0, 500, null).toPageable(WHITELIST);
    assertThat(p.getPageSize()).isEqualTo(100);
  }

  @Test
  void sizeBelow1Fails() {
    assertThatThrownBy(() -> new PageRequestParams(0, 0, null).toPageable(WHITELIST))
        .isInstanceOf(WksValidationException.class)
        .satisfies(
            ex -> {
              WksValidationException e = (WksValidationException) ex;
              assertThat(e.getCode()).isEqualTo(ErrorCode.WKS_API_003.wire());
              assertThat(e.getField()).isEqualTo("size");
            });
  }

  @Test
  void negativePageFails() {
    assertThatThrownBy(() -> new PageRequestParams(-1, 20, null).toPageable(WHITELIST))
        .isInstanceOf(WksValidationException.class)
        .satisfies(
            ex -> {
              WksValidationException e = (WksValidationException) ex;
              assertThat(e.getCode()).isEqualTo(ErrorCode.WKS_API_003.wire());
              assertThat(e.getField()).isEqualTo("page");
            });
  }

  @Test
  void unknownSortPropertyFails() {
    assertThatThrownBy(
            () -> new PageRequestParams(0, 20, List.of("mysteryField,desc")).toPageable(WHITELIST))
        .isInstanceOf(WksValidationException.class)
        .satisfies(
            ex -> {
              WksValidationException e = (WksValidationException) ex;
              assertThat(e.getCode()).isEqualTo(ErrorCode.WKS_API_004.wire());
              assertThat(e.getField()).isEqualTo("sort");
            });
  }

  @Test
  void unknownSortDirectionFails() {
    assertThatThrownBy(
            () -> new PageRequestParams(0, 20, List.of("updatedAt,sideways")).toPageable(WHITELIST))
        .isInstanceOf(WksValidationException.class)
        .satisfies(
            ex -> {
              WksValidationException e = (WksValidationException) ex;
              assertThat(e.getCode()).isEqualTo(ErrorCode.WKS_API_005.wire());
            });
  }

  @Test
  void sortDirectionDefaultsToAsc() {
    Pageable p = new PageRequestParams(0, 20, List.of("updatedAt")).toPageable(WHITELIST);
    Sort.Order order = p.getSort().getOrderFor("updatedAt");
    assertThat(order).isNotNull();
    assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
  }

  @Test
  void multipleSortSegmentsAreHonoured() {
    Pageable p =
        new PageRequestParams(1, 25, List.of("updatedAt,desc", "createdAt,asc"))
            .toPageable(WHITELIST);
    assertThat(p.getPageNumber()).isEqualTo(1);
    assertThat(p.getPageSize()).isEqualTo(25);
    assertThat(p.getSort().getOrderFor("updatedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    assertThat(p.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.ASC);
  }
}
