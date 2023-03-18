import Keycloak from 'keycloak-js';

function bootstrap() {
    let realm = 'wks';
    const clientId = 'wks-portal';
    const hostname = window.location.hostname;

    console.log(hostname);

    if (hostname !== 'localhost') {
        realm = hostname.substring(0, hostname.indexOf('.'));
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
