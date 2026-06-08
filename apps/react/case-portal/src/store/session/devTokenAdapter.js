import Config from '../../consts'

// Decodes the payload of a JWT (base64url) into a plain object.
function decodeJwtPayload(token) {
  try {
    const payload = token.split('.')[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(
      base64.length + ((4 - (base64.length % 4)) % 4),
      '=',
    )
    const json = decodeURIComponent(
      atob(padded)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    )
    return JSON.parse(json)
  } catch (e) {
    console.error('Failed to decode dev token payload', e)
    return {}
  }
}

// Returns a Keycloak-shaped adapter that authenticates against the dev-token
// HTTP endpoint instead of a real Keycloak server. The surface mirrors exactly
// what the app consumes from keycloak-js, so nothing downstream changes.
export function createDevTokenAdapter(realm) {
  const base = Config.AuthIssuerUrl

  const adapter = {
    token: undefined,
    tokenParsed: undefined,
    idTokenParsed: undefined,
    subject: undefined,
    authenticated: false,
    realmAccess: undefined,
    timeSkew: 0,
    onTokenExpired: undefined,
    onAuthRefreshError: undefined,

    hasRealmRole(role) {
      const roles =
        (this.tokenParsed &&
          this.tokenParsed.realm_access &&
          this.tokenParsed.realm_access.roles) ||
        []
      return roles.includes(role)
    },

    isTokenExpired() {
      if (!this.tokenParsed || !this.tokenParsed.exp) {
        return true
      }
      return this.tokenParsed.exp < Math.floor(Date.now() / 1000)
    },

    async init() {
      await fetchToken(adapter, base, realm)
      adapter.authenticated = !!adapter.token
      return adapter.authenticated
    },

    async updateToken() {
      // Re-fetch a fresh token; resolves true when a new token was obtained.
      const previous = adapter.token
      try {
        await fetchToken(adapter, base, realm)
        return adapter.token !== previous
      } catch (e) {
        console.error('Failed to refresh dev token', e)
        return false
      }
    },

    login() {
      return Promise.resolve()
    },

    logout() {
      return Promise.resolve()
    },
  }

  return adapter
}

async function fetchToken(adapter, base, realm) {
  const url = `${base}/realms/${realm}/protocol/openid-connect/token`

  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`Dev token request failed. Status: ${response.status}`)
  }

  const data = await response.json()
  const parsed = decodeJwtPayload(data.access_token)

  adapter.token = data.access_token
  adapter.tokenParsed = parsed
  adapter.idTokenParsed = parsed
  adapter.subject = parsed.sub
  adapter.realmAccess = parsed.realm_access
}
