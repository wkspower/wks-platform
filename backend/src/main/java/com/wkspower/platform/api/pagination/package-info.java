/**
 * Shared pagination + sort machinery for list endpoints. {@link
 * com.wkspower.platform.api.pagination.PageRequestParams} is the common query-string contract
 * ({@code page}, {@code size}, {@code sort}); {@link
 * com.wkspower.platform.api.pagination.SortWhitelist} enforces per-resource allow-lists; {@link
 * com.wkspower.platform.api.pagination.PageMetaBuilder} turns a Spring Data {@code Page} into the
 * WKS envelope with {@code meta.total|page|size}.
 */
package com.wkspower.platform.api.pagination;
