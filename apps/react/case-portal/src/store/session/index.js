import Keycloak from 'keycloak-js';
import Config from '../../consts';

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
        url: Config.LoginUrl,
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
