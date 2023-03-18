import Keycloak from 'keycloak-js';

function bootstrap() {
    let realm = '';
    const clientId = 'wks-portal';
    const hostname = window.location.hostname;

    if (hostname !== 'localhost') {
        realm = hostname.substring(0, hostname.indexOf('.'));
    } else {
        realm = hostname;
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
