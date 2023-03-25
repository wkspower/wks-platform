import { Formio } from 'formiojs';

export function RegisteOptions(keycloak) {
    Formio.setToken(keycloak.token);
}
