package com.wkspower.platform.security;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security {@link UserDetails} wrapper for an {@link AuthenticatedUser}. Role names are
 * upper-cased with the {@code ROLE_} prefix as Spring Security expects.
 */
public class WksUserPrincipal implements UserDetails {

  private final AuthenticatedUser user;

  public WksUserPrincipal(AuthenticatedUser user) {
    this.user = user;
  }

  public UUID id() {
    return user.id();
  }

  public AuthenticatedUser authenticated() {
    return user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.roles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return user.email();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
