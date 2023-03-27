const MGMT_ROLES = [
    'mgmt_case_def',
    'mgmt_record_type',
    'mgmt_form'
];

const CLT_ROLES = ['client_case', 'client_task', 'client_record'];

function isManagerUser(keycloak) {
    const count = MGMT_ROLES.filter((role) => keycloak.hasRealmRole(role));
    return count.length > 0;
}

function hasRole(keycloak, role) {
    return keycloak.hasRealmRole(role);
}

function hasAnyRole(keycloak) {
    const roles = [...CLT_ROLES, ...MGMT_ROLES];
    const count = roles.filter((role) => keycloak.hasRealmRole(role));
    return count.length > 0;
}

export default { hasRole, hasAnyRole, isManagerUser };
