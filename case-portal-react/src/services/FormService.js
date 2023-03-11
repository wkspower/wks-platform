import { json } from './request';

export const FormService = {
    getByKey,
    getVariableById
};

function addHeader(component) {}

async function getByKey(keycloak, formKey) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${process.env.REACT_APP_API_URL}/form/${formKey}`;

    try {
        const resp = await fetch(url, { headers });
        const json = await resp.json();

        json.structure.components.forEach((comp) => {
            console.log(comp);
        });

        // return json(keycloak, resp);
        return Promise.resolve(json);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}

async function getVariableById(keycloak, bpmEngineId, processInstanceId) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${process.env.REACT_APP_API_URL}/variable/${bpmEngineId}/${processInstanceId}`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}
