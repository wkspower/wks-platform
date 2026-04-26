/**
 * Domain pagination types. Pure records, zero Spring imports — the {@code CaseRepository} port and
 * any future paginated port returns {@link com.wkspower.platform.domain.page.Page} so the domain
 * stays infrastructure-free. Controllers map between the api-layer {@code PageRequestParams} and
 * these types at the boundary.
 */
package com.wkspower.platform.domain.page;
