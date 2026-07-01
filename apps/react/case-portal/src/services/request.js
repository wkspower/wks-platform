/**
 * Shared response handlers for the service layer.
 *
 * Contract: a non-OK HTTP status is a failure. These handlers reject with an
 * {@link ApiError} carrying the status and (best-effort) parsed body, so callers
 * can surface real errors instead of mistaking a 400/500 for success. A 401 still
 * triggers the token-expiry logout path. Previously a non-OK, non-401 response
 * resolved with the raw Response, so server errors silently masqueraded as data.
 */

/** Error thrown/rejected for any non-OK API response. */
export class ApiError extends Error {
  constructor(status, body, statusText) {
    const detail =
      body && typeof body === 'object' && body.message
        ? body.message
        : statusText
    super(`Request failed with status ${status}${detail ? `: ${detail}` : ''}`)
    this.name = 'ApiError'
    this.status = status
    this.body = body
    this.statusText = statusText
  }
}

// Best-effort read of an error response body: JSON when possible, else text.
async function readErrorBody(resp) {
  try {
    const contentType = resp.headers?.get?.('content-type') || ''
    if (contentType.includes('application/json')) {
      return await resp.json()
    }
    const text = await resp.text()
    return text || null
  } catch {
    return null
  }
}

/**
 * Resolve with the parsed JSON body on success; reject with an {@link ApiError}
 * on any non-OK status.
 */
export async function json(keycloak, resp) {
  if (resp.status === 401) {
    forceLogoutWhenTokenExpired(keycloak, resp)
    return Promise.reject(new ApiError(401, null, resp.statusText))
  }

  if (resp.ok) {
    return resp.json()
  }

  const body = await readErrorBody(resp)
  return Promise.reject(new ApiError(resp.status, body, resp.statusText))
}

/**
 * Resolve with the raw Response on success (for non-JSON endpoints); reject with
 * an {@link ApiError} on any non-OK status.
 */
export async function nop(keycloak, resp) {
  if (resp.status === 401) {
    forceLogoutWhenTokenExpired(keycloak, resp)
    return Promise.reject(new ApiError(401, null, resp.statusText))
  }

  if (resp.ok) {
    return resp
  }

  const body = await readErrorBody(resp)
  return Promise.reject(new ApiError(resp.status, body, resp.statusText))
}

function forceLogoutWhenTokenExpired(keycloak, resp) {
  if (keycloak.isTokenExpired()) {
    console.error(resp)
    keycloak.logout({ redirectUri: window.location.origin })
  }
}
