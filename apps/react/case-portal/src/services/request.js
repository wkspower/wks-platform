export function json(keycloak, resp) {
  if (resp.status === 401) {
    forceLogoutWhenTokenExpired(keycloak, resp)
    return Promise.reject(resp)
  }

  if (resp.ok) {
    // Handle empty responses (e.g., 204 No Content or void returns with 200)
    if (resp.status === 204 || resp.headers.get('content-length') === '0') {
      return Promise.resolve(null)
    }
    return resp.json()
  }

  return Promise.resolve(resp)
}

export function nop(keycloak, resp) {
  if (resp.status === 401) {
    forceLogoutWhenTokenExpired(keycloak, resp)
    return Promise.reject(resp)
  }

  return resp
}

function forceLogoutWhenTokenExpired(keycloak, resp) {
  if (keycloak.isTokenExpired) {
    console.error(resp)
    keycloak.logout({ redirectUri: window.location.origin })
  }
}
