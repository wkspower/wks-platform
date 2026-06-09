import Keycloak from 'keycloak-js'
import Config from '../../consts'
import { createDevTokenAdapter } from './devTokenAdapter'

function bootstrap() {
  let realm = ''
  const clientId = 'wks-portal'
  const hostname = window.location.hostname

  if (hostname !== 'localhost') {
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
