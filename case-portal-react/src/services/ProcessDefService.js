import { json } from './request';

export const ProcessDefService = {
<<<<<<< Updated upstream
    find
};

async function find(keycloak) {
    const url = `${process.env.REACT_APP_API_URL}/process-definition/`;
=======
    find,
    findByEngineId,
    getBPMNXml
};

async function find(keycloak) {
    if (keycloak.isTokenExpired()) {
        keycloak.logout({ redirectUri: window.location.origin });
    }

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${process.env.REACT_APP_API_URL}/process-definition/`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}

async function findByEngineId(keycloak, id) {
    if (!id) {
        return Promise.reject('empty id');
    }

    const url = `${process.env.REACT_APP_API_URL}/process-definition/${id}/`;
>>>>>>> Stashed changes

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function getBPMNXml(keycloak, bpmEngineId, processDefId) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${process.env.REACT_APP_API_URL}/process-definition/${bpmEngineId}/${processDefId}/xml`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp.text());
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}
