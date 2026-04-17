package com.wkspower.platform.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminUserSeederTest {

  private final UserRepository users = mock(UserRepository.class);
  private final PasswordEncoder encoder = mock(PasswordEncoder.class);

  @Test
  void seedsAdminWhenNoneExistsInDevProfileUsingFallbackCredentials() {
    when(users.existsWithRole("admin")).thenReturn(false);
    when(encoder.encode(anyString())).thenReturn("hashed-password");

    AdminUserSeeder seeder = new AdminUserSeeder(users, encoder, new MockEnvironment(), "", "");

    seeder.run(null);

    ArgumentCaptor<User> userArg = ArgumentCaptor.forClass(User.class);
    verify(users).save(userArg.capture(), anyString());
    assertThat(userArg.getValue().email()).isEqualTo(AdminUserSeeder.DEV_DEFAULT_EMAIL);
    assertThat(userArg.getValue().roles()).containsExactly("admin");
  }

  @Test
  void skipsSeedingWhenAdminAlreadyExists() {
    when(users.existsWithRole("admin")).thenReturn(true);

    AdminUserSeeder seeder = new AdminUserSeeder(users, encoder, new MockEnvironment(), "", "");

    seeder.run(null);

    verify(users, never()).save(any(User.class), anyString());
  }

  @Test
  void usesConfiguredCredentialsWhenProvided() {
    when(users.existsWithRole("admin")).thenReturn(false);
    when(encoder.encode("s3cret")).thenReturn("hashed-password");

    AdminUserSeeder seeder =
        new AdminUserSeeder(users, encoder, new MockEnvironment(), "ops@example.com", "s3cret");

    seeder.run(null);

    ArgumentCaptor<User> userArg = ArgumentCaptor.forClass(User.class);
    verify(users, times(1)).save(userArg.capture(), anyString());
    assertThat(userArg.getValue().email()).isEqualTo("ops@example.com");
  }

  @Test
  void productionWithoutCredentialsThrows() {
    when(users.existsWithRole("admin")).thenReturn(false);
    MockEnvironment prod = new MockEnvironment();
    prod.setActiveProfiles("production");

    AdminUserSeeder seeder = new AdminUserSeeder(users, encoder, prod, "", "");

    assertThatThrownBy(() -> seeder.run(null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("WKS-API-051");

    verify(users, never()).save(any(User.class), anyString());
  }
}
