export function json(keycloak, resp) {
    if (resp.status === 401) {
        forceLogoutWhenTokenExpired(keycloak, resp);
        return Promise.reject(resp);
    }

    if (resp.ok) {
        return resp.json();
    }

    return resp;
}

export function nop(keycloak, resp) {
    if (resp.status === 401) {
        forceLogoutWhenTokenExpired(keycloak, resp);
        return Promise.reject(resp);
    }

    return resp;
}

function forceLogoutWhenTokenExpired(keycloak, resp) {
    if (keycloak.isTokenExpired()) {
        console.error(resp);
        keycloak.logout({ redirectUri: window.location.origin });
    }
}
