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

  // -- Story 2.5 AC11 #1 --------------------------------------------------

  /**
   * Story 2.5 AC11 #1 — when a single token has both a bad direction AND a bad property the
   * direction error fires first ({@code WKS-API-005}), letting the client correct one fault per
   * round-trip rather than a confusing simultaneous-error UX. Locks in the contract so a
   * future refactor of {@link SortSpec#parse(String)} cannot silently flip the precedence.
   */
  @Test
  void badDirectionWinsOverBadPropertyOnSameToken() {
    assertThatThrownBy(
            () ->
                new PageRequestParams(0, 20, List.of("mysteryField,sideways"))
                    .toPageable(WHITELIST))
        .isInstanceOf(WksValidationException.class)
        .satisfies(
            ex -> {
              WksValidationException e = (WksValidationException) ex;
              assertThat(e.getCode()).isEqualTo(ErrorCode.WKS_API_005.wire());
              assertThat(e.getField()).isEqualTo("sort");
            });
  }

  /**
   * Story 2.5 AC11 #1 — duplicate-sort dedup with last-wins. {@code ?sort=updatedAt,asc&sort=updatedAt,desc}
   * resolves to a single ORDER BY updatedAt DESC, not two. Insertion position of the first
   * occurrence is preserved so the user-visible "primary sort" doesn't shuffle on a duplicate
   * write — only the direction updates.
   */
  @Test
  void duplicateSortPropertyDedupsLastWins() {
    Pageable p =
        new PageRequestParams(0, 20, List.of("updatedAt,asc", "updatedAt,desc"))
            .toPageable(WHITELIST);

    assertThat(p.getSort()).hasSize(1);
    Sort.Order order = p.getSort().getOrderFor("updatedAt");
    assertThat(order).isNotNull();
    assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
  }

  @Test
  void duplicateSortPropertyPreservesFirstSlotOrder() {
    Pageable p =
        new PageRequestParams(
                0,
                20,
                List.of("updatedAt,asc", "createdAt,asc", "updatedAt,desc"))
            .toPageable(WHITELIST);

    // updatedAt was first; despite the dedup pulling its later (desc) value, it retains slot #0.
    List<Sort.Order> orders = p.getSort().toList();
    assertThat(orders).hasSize(2);
    assertThat(orders.get(0).getProperty()).isEqualTo("updatedAt");
    assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
    assertThat(orders.get(1).getProperty()).isEqualTo("createdAt");
  }
}
