import Keycloak from 'keycloak-js';

function bootstrap() {
    let realm = 'wks-platform';
    const clientId = 'wks-portal';
    const hostname = window.location.hostname;

    if (hostname.includes('.wkspower.')) {
        if (!hostname.startsWith('app.wkspower')) {
            realm = hostname.substring(0, hostname.indexOf('.'));
        }
    }

    const kc = Keycloak({
        url: process.env.REACT_APP_KEYCLOAK_URL,
        realm: realm,
        clientId: clientId
    });

    return {
        keycloak: kc,
        realm,
        clientId
    };
}

export default {
    bootstrap
};
