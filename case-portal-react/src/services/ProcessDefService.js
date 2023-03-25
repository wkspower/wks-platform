import { json } from './request';
import Config from '../consts';

export const ProcessDefService = {
    getById
};

async function getById(keycloak, id) {
    if (!id) {
        return Promise.reject('empty id');
    }

    const url = `${Config.EngineUrl}/process-definition/${id}/`;

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
