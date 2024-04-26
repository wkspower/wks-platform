import { Formio } from 'formiojs'

export function RegisterInjectUserSession(keycloak) {
  const name = 'injectUserSession'

  if (Formio.getPlugin(name)) {
    Formio.deregisterPlugin(name)
  }

  Formio.registerPlugin(
    {
      priority: 0,
      preRequest: function (args) {
        return new Promise(function (resolve) {
          if (args.opts && args.opts.header) {
            args.opts.header.set('Authorization', `Bearer ${keycloak.token}`)
          }
          return resolve(args)
        })
      },
    },
    'injectUserSession',
  )
}
