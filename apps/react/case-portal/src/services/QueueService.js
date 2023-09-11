import Config from '../consts';
import { json, nop } from './request';

export const QueueService = {
    find,
    get,
    update,
    remove,
    save
};

async function save(keycloak, body) {
    const url = `${Config.CaseEngineUrl}/queue/`;

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: JSON.stringify(body)
        });
        return nop(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}

async function update(keycloak, id, body) {
    const url = `${Config.CaseEngineUrl}/queue/${id}`;

    try {
        const resp = await fetch(url, {
            method: 'PUT',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: JSON.stringify(body)
        });
        return nop(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}

async function remove(keycloak, id) {
    const url = `${Config.CaseEngineUrl}/queue/${id}`;

    try {
        const resp = await fetch(url, {
            method: 'DELETE',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            }
        });
        return nop(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}

async function find(keycloak) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${Config.CaseEngineUrl}/queue/`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}

async function get(keycloak, id) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${Config.CaseEngineUrl}/queue/${id}`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}