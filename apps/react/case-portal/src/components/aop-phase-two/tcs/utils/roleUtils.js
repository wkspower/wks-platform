// Role definitions and utilities for TCS workflow

export const ROLES = {
  // CTS_HEAD: 'cts_head',
  EPS_HEAD: 'eps_head',
  EPS_ENGINEER: 'eps_engineer',
  PLANT_MANAGER: 'plant_manager',
  CTS_ADMIN: 'cts_admin',
}

/**
 * Extract user's primary workflow role from keycloak roles based on priority
 * Priority order: cts_head > eps_head > eps_engineer > plant_manager > cts_admin
 * @param {Array} keycloakRoles - Array of roles from keycloak
 * @returns {string|null} - Primary workflow role or null
 */
export const getUserRole = (keycloakRoles = []) => {
  if (!Array.isArray(keycloakRoles) || keycloakRoles.length === 0) {
    return null
  }

  const rolePriority = [
    ROLES.PLANT_MANAGER,
    ROLES.EPS_ENGINEER,
    // ROLES.CTS_HEAD,
    ROLES.EPS_HEAD,
    ROLES.CTS_ADMIN,
  ]

  for (const role of rolePriority) {
    if (keycloakRoles.includes(role)) {
      return role
    }
  }

  return null
}
