export const getRoleName = (keycloak) => {
  // return keycloak?.realmAccess?.roles?.includes('cts_admin')
  return keycloak?.realmAccess?.roles?.includes('read_only')
  // return false
}
