package com.wkspower.platform.security;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.port.CaseTypeReader;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Light-weight permission gate for case-type verbs (Story 2.3 AC5). Used from
 * {@code @PreAuthorize("@caseTypePermissionEvaluator.hasVerb(...)")} on {@code CaseController}.
 *
 * <p>Looks up the case type via the {@link CaseTypeReader} port (NOT the registry directly — keeps
 * the security layer port-only) and checks whether any of the user's roles holds the requested
 * permission verb. Phase 0 RBAC is role-only — case-level access is Story 5.2.
 */
@Component
public class CaseTypePermissionEvaluator {

  private final CaseTypeReader caseTypeReader;

  public CaseTypePermissionEvaluator(CaseTypeReader caseTypeReader) {
    this.caseTypeReader = caseTypeReader;
  }

  /**
   * @param user authenticated principal
   * @param caseTypeId case type id from the request body / path
   * @param verb wire form of the permission (e.g. {@code "create"}, {@code "view"})
   * @return {@code true} if any of the user's roles holds the verb on this case type. Returns
   *     {@code false} for null inputs and for unknown case-type ids — the missing-case-type 404 is
   *     surfaced from the service layer (e.g., {@code CaseService.requireCaseType}) rather than
   *     thrown from inside SpEL, which Spring Security 6 wraps as 500/403.
   */
  public boolean hasVerb(AuthenticatedUser user, String caseTypeId, String verb) {
    if (user == null || caseTypeId == null || verb == null) {
      return false;
    }
    Optional<CaseTypeConfig> config = caseTypeReader.find(caseTypeId);
    if (config.isEmpty()) {
      return false;
    }
    return config.get().roles().stream()
        .filter(r -> user.roles().contains(r.name()))
        .flatMap(r -> r.permissions().stream())
        .map(Permission::wire)
        .anyMatch(verb::equals);
  }

  /**
   * Returns the verb subset (as wire strings) that the caller holds on the given case-type. Used by
   * {@code GET /api/case-types} to populate {@code CaseTypeSummaryDto.permissions[]} so the
   * frontend can filter the Create-Case dropdown to entries the user can actually act on without
   * round-tripping per case-type. Story 2.7 introduces this method.
   *
   * @return immutable set of verb wire strings; empty set for null inputs or unknown case-types.
   */
  public Set<String> verbsOf(AuthenticatedUser user, String caseTypeId) {
    if (user == null || caseTypeId == null) {
      return Set.of();
    }
    Optional<CaseTypeConfig> config = caseTypeReader.find(caseTypeId);
    if (config.isEmpty()) {
      return Set.of();
    }
    Set<String> verbs = new LinkedHashSet<>();
    config.get().roles().stream()
        .filter(r -> user.roles().contains(r.name()))
        .flatMap(r -> r.permissions().stream())
        .map(Permission::wire)
        .forEach(verbs::add);
    return Set.copyOf(verbs);
  }
}
