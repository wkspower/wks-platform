const MGMT_ROLES = ['mgmt_case_def', 'mgmt_record_type', 'mgmt_form']

const CLT_ROLES = ['client_case', 'client_task', 'client_record']
const AOP_ROLES = [
  'plant_manager',
  'operation_head',
  'maintenance_head',
  'cts_head',
  'ces_head',
  'safety_head',
  'pci_head',
  'mmc_head',
  'sor_head',
  'technology_vertical_head',
  'engineering_head',
  'business_head',
  'site_head',
  'gms_head',
]

function isManagerUser(keycloak) {
  const count = MGMT_ROLES.filter((role) => keycloak.hasRealmRole(role))
  return count.length > 0
}

function hasRole(keycloak, role) {
  return keycloak.hasRealmRole(role)
}

function hasAnyRole(keycloak) {
  const roles = [...AOP_ROLES]
  const count = roles.filter((role) => keycloak.hasRealmRole(role))
  return count.length > 0
}

export default { hasRole, hasAnyRole, isManagerUser }
