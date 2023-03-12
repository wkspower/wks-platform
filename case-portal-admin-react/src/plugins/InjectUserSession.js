import { Formio } from 'formiojs';

export function registerInjectUserSession(keycloak) {
    const name = 'injectUserSession';

    if (Formio.getPlugin(name)) {
        Formio.deregisterPlugin(name);
    }

    Formio.registerPlugin(
        {
            priority: 0,
            preRequest: function (args) {
                return new Promise(function (resolve, reject) {
                    args.opts.header.set('Authorization', `Bearer ${keycloak.token}`);
                    return resolve(args);
                });
            }
        },
        'injectUserSession'
    );
}
