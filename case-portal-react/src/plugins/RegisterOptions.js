import MemoryTokenManager from './MemoryTokenManager';

export function RegisteOptions(keycloak) {
    if (keycloak == null) {
        MemoryTokenManager.setToken(null);
    } else {
        MemoryTokenManager.setToken(keycloak.token);
    }
}
