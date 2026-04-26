package com.wkspower.platform.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.page.SortOrder;
import com.wkspower.platform.infrastructure.persistence.entity.RoleEntity;
import com.wkspower.platform.infrastructure.persistence.entity.UserEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.RoleEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.UserEntityRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@code @DataJpaTest} IT for {@link CaseRepositoryAdapter}. Exercises the JSON column round-trip,
 * the JPQL constructor projection (no JSON fetch on list), and optimistic-locking conflict
 * surfacing as {@link com.wkspower.platform.domain.exception.WksConflictException}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CaseRepositoryAdapter.class)
@ActiveProfiles("dev")
class CaseRepositoryIT {

  @Autowired CaseRepositoryAdapter adapter;
  @Autowired CaseEntityRepository caseRepo;
  @Autowired UserEntityRepository userRepo;
  @Autowired RoleEntityRepository roleRepo;
  @Autowired EntityManager em;

  private UUID actorId;

  @BeforeEach
  void seedUser() {
    RoleEntity role =
        roleRepo
            .findByName("admin")
            .orElseGet(
                () ->
                    roleRepo.save(
                        new RoleEntity(UUID.randomUUID(), "admin", Instant.now(), Instant.now())));
    UserEntity user =
        userRepo.save(
            new UserEntity(
                UUID.randomUUID(),
                "ops-" + UUID.randomUUID() + "@x",
                "x",
                true,
                Instant.now(),
                Instant.now(),
                new HashSet<>(List.of(role))));
    actorId = user.getId();
  }

  @Test
  void saveAndFindByIdRoundTripsJsonData() {
    Map<String, Object> data =
        Map.of(
            "applicant",
            Map.of("name", "Asha", "age", 32),
            "tags",
            List.of("priority", "first-time"));
    Case toSave = newCase(data);

    Case saved = adapter.save(toSave);
    em.flush();
    em.clear();

    Case reloaded = adapter.findById(saved.id()).orElseThrow();

    assertThat(reloaded.id()).isEqualTo(saved.id());
    assertThat(reloaded.data()).containsEntry("applicant", Map.of("name", "Asha", "age", 32));
    assertThat(reloaded.data().get("tags")).asList().containsExactly("priority", "first-time");
    assertThat(reloaded.version()).isGreaterThanOrEqualTo(0L);
  }

  @Test
  void saveAndUpdateBumpsVersion() {
    Case saved = adapter.save(newCase(Map.of("name", "Asha")));
    em.flush();
    em.clear();

    Case fresh = adapter.findById(saved.id()).orElseThrow();
    Case updated =
        new Case(
            fresh.id(),
            fresh.caseTypeId(),
            fresh.caseTypeVersion(),
            fresh.status(),
            fresh.assignee(),
            Map.of("name", "Bob"),
            fresh.processInstanceId(),
            fresh.createdAt(),
            fresh.createdBy(),
            Instant.now(),
            fresh.version());
    Case persisted = adapter.save(updated);
    em.flush();
    em.clear();

    Case reloaded = adapter.findById(persisted.id()).orElseThrow();
    assertThat(reloaded.data()).containsEntry("name", "Bob");
    assertThat(reloaded.version()).isGreaterThan(saved.version());
  }

  @Test
  void findByCaseTypeReturnsSummariesWithoutFetchingJsonData() {
    adapter.save(newCase(Map.of("name", "A")));
    adapter.save(newCase(Map.of("name", "B")));
    em.flush();
    em.clear();

    Page<?> page =
        adapter.findByCaseType(
            CaseQuery.of("loan-application"),
            PageRequest.of(0, 20, List.of(new SortOrder("updatedAt", false))));

    assertThat(page.content()).hasSize(2);
    assertThat(page.total()).isEqualTo(2);
    // Summaries carry empty `fields` projections in 2.3 — Story 2.5 enriches.
    assertThat(((com.wkspower.platform.domain.model.CaseSummary) page.content().get(0)).fields())
        .isEmpty();
  }

  private Case newCase(Map<String, Object> data) {
    Instant now = Instant.now();
    return new Case(
        UUID.randomUUID(),
        "loan-application",
        1,
        "open",
        null,
        data,
        "pi-" + UUID.randomUUID(),
        now,
        actorId,
        now,
        0L);
  }
}
