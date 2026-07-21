import Keycloak from 'keycloak-js'
import Config from '../../consts'
import { createDevTokenAdapter } from './devTokenAdapter'
import { insecureContextInitOptions } from './insecureContext'

function bootstrap() {
  let realm = ''
  const clientId = 'wks-portal'
  const hostname = window.location.hostname

  // Explicit realm wins; the hostname derivation below requires a
  // fully-qualified DNS name (an IP or dotless hostname yields a bogus realm).
  if (Config.Realm) {
    realm = Config.Realm
  } else if (hostname !== 'localhost') {
    realm = hostname.substring(0, hostname.indexOf('.'))
  } else {
    realm = hostname
  }

  if (Config.AuthMode === 'dev-token') {
    const kc = createDevTokenAdapter(realm)

    return {
      keycloak: kc,
      realm,
      clientId,
      initOptions: {},
    }
  }

  const kc = new Keycloak({
    url: Config.AuthIssuerUrl,
    realm: realm,
    clientId: clientId,
  })

  return {
    keycloak: kc,
    realm,
    clientId,
    // Extra keycloak.init() options — non-empty only in insecure (plain-HTTP)
    // contexts, where Web Crypto is restricted; see insecureContext.js.
    initOptions: insecureContextInitOptions(),
  }
}

export default {
  bootstrap,
}
