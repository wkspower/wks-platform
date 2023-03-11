import { json, nop } from './request';

export const FormService = {
    getAll,
    getByKey,
    update,
    remove,
    create
};

async function create(keycloak, body) {
    const url = `${process.env.REACT_APP_API_URL}/form/`;

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
    const url = `${process.env.REACT_APP_API_URL}/form/${id}`;

    try {
        const resp = await fetch(url, {
            method: 'PATCH',
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
    const url = `${process.env.REACT_APP_API_URL}/form/${id}`;

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

async function getAll(keycloak) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${process.env.REACT_APP_API_URL}/form/`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}

async function getByKey(keycloak, formKey) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${process.env.REACT_APP_API_URL}/form/${formKey}`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}
