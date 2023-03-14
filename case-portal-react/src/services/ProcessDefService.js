import { json } from './request';

export const ProcessDefService = {
    getById
};

async function getById(keycloak, id) {
    if (!id) {
        return Promise.reject('empty id');
    }

    const url = `${process.env.REACT_APP_API_URL}/process-definition/${id}/`;

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
