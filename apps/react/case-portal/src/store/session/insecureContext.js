/*
 * WKS Platform - Open-Source Project
 *
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 *
 * WKS Platform is licensed under the MIT License.
 *
 * © 2021 WKS Power. All rights reserved.
 *
 * For licensing information, see the LICENSE file in the root directory of the project.
 */

// keycloak-js 26 requires crypto.randomUUID (login state/nonce) and, for PKCE,
// crypto.subtle — browsers expose both ONLY in secure contexts (HTTPS or
// localhost), so a portal served over plain HTTP on a remote host fails login
// with "Web Crypto API is not available". crypto.getRandomValues IS available
// in insecure contexts: back-fill randomUUID from it (same CSPRNG, RFC 4122
// v4) and disable PKCE, whose SHA-256 digest cannot be polyfilled without
// crypto.subtle. Secure contexts are left completely untouched.
export function insecureContextInitOptions() {
  if (window.isSecureContext) {
    return {}
  }

  console.warn(
    '[wks] Portal is served over plain HTTP (insecure context): ' +
      'polyfilling crypto.randomUUID and disabling PKCE for login. ' +
      'Serve the portal over HTTPS to restore full Web Crypto support.',
  )

  if (
    typeof crypto !== 'undefined' &&
    typeof crypto.randomUUID === 'undefined' &&
    typeof crypto.getRandomValues === 'function'
  ) {
    crypto.randomUUID = function randomUUID() {
      const bytes = crypto.getRandomValues(new Uint8Array(16))
      bytes[6] = (bytes[6] & 0x0f) | 0x40
      bytes[8] = (bytes[8] & 0x3f) | 0x80
      const hex = [...bytes].map((b) => b.toString(16).padStart(2, '0'))
      return (
        hex.slice(0, 4).join('') +
        '-' +
        hex.slice(4, 6).join('') +
        '-' +
        hex.slice(6, 8).join('') +
        '-' +
        hex.slice(8, 10).join('') +
        '-' +
        hex.slice(10, 16).join('')
      )
    }
  }

  return { pkceMethod: false }
}
