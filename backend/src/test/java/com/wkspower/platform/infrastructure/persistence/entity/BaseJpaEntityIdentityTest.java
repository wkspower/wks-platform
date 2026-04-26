package com.wkspower.platform.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Identity contract for {@link BaseJpaEntity} (AC3, pinned debt from Epic 1 retro action #5 + Story
 * 1.4 chunk-3 deferred). Pure unit — no Spring, no database. The integration side of the same
 * contract (Set membership across persist + reload) lives in {@link
 * com.wkspower.platform.infrastructure.persistence.UserRepositoryPostgresIT}-class repository
 * tests.
 */
class BaseJpaEntityIdentityTest {

  @Test
  @DisplayName("two transient instances are never equal")
  void twoTransientInstancesNotEqual() {
    RoleEntity a = new RoleEntity(null, "ADMIN");
    RoleEntity b = new RoleEntity(null, "ADMIN");

    assertThat(a).isNotEqualTo(b);
    assertThat(b).isNotEqualTo(a);
  }

  @Test
  @DisplayName("two managed instances with the same id are equal across contexts")
  void twoManagedInstancesWithSameIdAreEqual() {
    UUID shared = UUID.randomUUID();
    RoleEntity a = new RoleEntity(shared, "ADMIN");
    RoleEntity b = new RoleEntity(shared, "ADMIN");

    assertThat(a).isEqualTo(b);
    assertThat(b).isEqualTo(a);
  }

  @Test
  @DisplayName("transient → managed transition keeps the same hashCode")
  void hashCodeIsStableAcrossTransientToManagedTransition() {
    RoleEntity role = new RoleEntity(null, "ADMIN");
    int beforePersist = role.hashCode();

    // Simulate Hibernate's flush: id is now assigned by the persistence context.
    role.setId(UUID.randomUUID());

    int afterPersist = role.hashCode();
    assertThat(afterPersist).isEqualTo(beforePersist);
  }

  @Test
  @DisplayName("Set membership: same managed entity reloaded does not duplicate")
  void setContainsManagedEntityAfterReload() {
    UUID id = UUID.randomUUID();
    RoleEntity firstLoad = new RoleEntity(id, "ADMIN");
    RoleEntity reloaded = new RoleEntity(id, "ADMIN");

    Set<RoleEntity> roles = new HashSet<>();
    roles.add(firstLoad);

    assertThat(roles).contains(reloaded);
    roles.add(reloaded);
    assertThat(roles).hasSize(1);
  }

  @Test
  @DisplayName("different concrete entity classes with the same id are not equal")
  void differentClassesNotEqualEvenWithSameId() {
    UUID shared = UUID.randomUUID();
    RoleEntity role = new RoleEntity(shared, "ADMIN");
    UserEntity user = new UserEntity(shared, "a@b.c", "x", true, null, null, new HashSet<>());

    assertThat(role).isNotEqualTo(user);
    assertThat(user).isNotEqualTo(role);
  }

  @Test
  @DisplayName("equals against null is false")
  void equalsAgainstNullIsFalse() {
    RoleEntity role = new RoleEntity(UUID.randomUUID(), "ADMIN");
    assertThat(role).isNotEqualTo(null);
  }

  @Test
  @DisplayName("equals against unrelated type is false")
  void equalsAgainstUnrelatedTypeIsFalse() {
    RoleEntity role = new RoleEntity(UUID.randomUUID(), "ADMIN");
    assertThat(role).isNotEqualTo("ADMIN");
  }
}
