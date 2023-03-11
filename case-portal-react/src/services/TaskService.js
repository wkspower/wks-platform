import { json } from './request';

export const TaskService = {
    getActivityInstancesById,
    createTaskClaim,
    createTaskUnclaim,
    createTaskComplete,
    filterTasks,
    filterProcessInstances
};

async function getActivityInstancesById(keycloak, bpmEngineId, processInstanceId) {
    const url = `${process.env.REACT_APP_API_URL}/process-instance/${bpmEngineId}/${processInstanceId}/activity-instances`;

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

async function createTaskClaim(keycloak, bpmEngineId, taskId) {
    const url = `${process.env.REACT_APP_API_URL}/task/${bpmEngineId}/${taskId}/claim/${keycloak.idTokenParsed.name}`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, { method: 'POST', headers });
        return json(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function createTaskUnclaim(keycloak, bpmEngineId, taskId) {
    const url = `${process.env.REACT_APP_API_URL}/task/${bpmEngineId}/${taskId}/unclaim/${keycloak.idTokenParsed.name}`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, { method: 'POST', headers });
        return json(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function createTaskComplete(keycloak, bpmEngineId, taskId, body) {
    const url = `${process.env.REACT_APP_API_URL}/task/${bpmEngineId}/${taskId}/complete`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers,
            body: JSON.stringify({
                variables: body
            })
        });
        return json(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function filterTasks(keycloak, bpmEngineId, businessKey) {
    let query = '';

    if (!!businessKey) {
        query = query + bpmEngineId ? 'bpmEngineId=' + bpmEngineId + '&' : '';
    }

    if (!!businessKey) {
        query = query + (businessKey ? 'processInstanceBusinessKey=' + businessKey : '');
    }

    const url = `${process.env.REACT_APP_API_URL}/task/?${query}`;

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

async function filterProcessInstances(keycloak, bpmEngineId, businessKey) {
    if (!bpmEngineId) {
        return Promise.resolve([]);
    }

    if (!businessKey) {
        businessKey = '';
    }

    const url = `${process.env.REACT_APP_API_URL}/process-instance/${bpmEngineId}?businessKey=${businessKey}`;

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
