import Keycloak from 'keycloak-js'
import Config from '../../consts'
import { createDevTokenAdapter } from './devTokenAdapter'

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
  }
}

export default {
  bootstrap,
}
