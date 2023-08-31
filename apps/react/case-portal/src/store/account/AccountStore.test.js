import store from './index';

class Keycloak {
    constructor(role) {
        this.hasRole = role;
    }

    hasRealmRole(role) {
        return this.hasRole === role;
    }
}

test('should be true when user has managament role', () => {
    expect(store.isManagerUser(new Keycloak('mgmt_case_def'))).toEqual(true);
    expect(store.isManagerUser(new Keycloak('mgmt_record_type'))).toEqual(true);
    expect(store.isManagerUser(new Keycloak('mgmt_form'))).toEqual(true);
});

test('should be false when user dont has managament role', () => {
    const results = store.isManagerUser(new Keycloak('client_user'));

    expect(results).toEqual(false);
});

test('should be true if user has some role', () => {
    expect(store.hasRole(new Keycloak('mgmt_case_def'), 'mgmt_case_def')).toEqual(true);
    expect(store.hasRole(new Keycloak('client_task'), 'client_task')).toEqual(true);
});

test('should be false if user dont have some role', () => {
    expect(store.hasRole(new Keycloak('client_task', 'unknown'))).toEqual(false);
});
