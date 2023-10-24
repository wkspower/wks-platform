import { json, nop } from './request';
import Config from '../consts';

export const RecordTypeService = {
    create,
    update,
    remove,
    getAll
};

async function getAll(keycloak) {
    const url = `${Config.CaseEngineUrl}/record-type`;

    try {
        const resp = await fetch(url, {
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            }
        });
        return json(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function create(keycloak, data) {
    const url = `${Config.CaseEngineUrl}/record-type`;

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: JSON.stringify(data)
        });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function update(keycloak, id, data) {
    const url = `${Config.CaseEngineUrl}/record-type/${id}`;

    try {
        const resp = await fetch(url, {
            method: 'PATCH',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: JSON.stringify(data)
        });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function remove(keycloak, id) {
    const url = `${Config.CaseEngineUrl}/record-type/${id}`;

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
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}
