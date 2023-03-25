import { json, nop } from './request';
import Config from '../consts';

export const BpmService = {
    create,
    update,
    remove,
    getAll,
    getAllTypes
};

async function getAllTypes(keycloak) {
    const url = `${Config.EngineUrl}/bpm-engine-type/`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
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

async function getAll(keycloak) {
    const url = `${Config.EngineUrl}/bpm-engine/`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
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

async function create(keycloak, body) {
    const url = `${Config.EngineUrl}/bpm-engine/`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, { method: 'POST', headers, body: JSON.stringify(body) });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function update(keycloak, id, body) {
    const url = `${Config.EngineUrl}/bpm-engine/${id}`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, {
            method: 'PATCH',
            headers,
            body: JSON.stringify(body)
        });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function remove(keycloak, id) {
    const url = `${Config.EngineUrl}/bpm-engine/${id}`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, {
            method: 'DELETE',
            headers
        });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}
