import { json } from './request';
import Config from 'consts/index';

export const ProcessDefService = {
    find,
    getBPMNXml
};

async function find(keycloak) {
    if (keycloak.isTokenExpired()) {
        keycloak.logout({ redirectUri: window.location.origin });
    }

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${Config.CaseEngineUrl}/process-definition/`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}

async function getBPMNXml(keycloak, processDefId) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${Config.CaseEngineUrl}/process-definition/${processDefId}/xml`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp.text());
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}
