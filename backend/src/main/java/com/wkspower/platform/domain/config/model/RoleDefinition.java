package com.wkspower.platform.domain.config.model;

import java.util.List;

/** One role with its permission set. */
public record RoleDefinition(String name, List<Permission> permissions) {

  public RoleDefinition {
    permissions = permissions == null ? List.of() : List.copyOf(permissions);
  }
}
