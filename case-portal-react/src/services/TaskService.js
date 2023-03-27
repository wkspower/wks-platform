import { json, nop } from './request';

export const TaskService = {
    getActivityInstancesById,
    createTaskClaim,
    createTaskUnclaim,
    createTaskComplete,
    filterTasks,
    filterProcessInstances
};

async function getActivityInstancesById(keycloak, processInstanceId) {
    const url = `${process.env.REACT_APP_API_URL}/process-instance/${processInstanceId}/activity-instances`;

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

async function createTaskClaim(keycloak, taskId) {
    const url = `${process.env.REACT_APP_API_URL}/task/${taskId}/claim/${keycloak.idTokenParsed.name}`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, { method: 'POST', headers });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function createTaskUnclaim(keycloak, taskId) {
    const url = `${process.env.REACT_APP_API_URL}/task/${taskId}/unclaim/${keycloak.idTokenParsed.name}`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, { method: 'POST', headers });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function createTaskComplete(keycloak, taskId, body) {
    const url = `${process.env.REACT_APP_API_URL}/task/${taskId}/complete`;

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
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function filterTasks(keycloak, businessKey) {
    let query = '';

    if (!!businessKey) {
        query = query + (businessKey ? 'businessKey=' + businessKey : '');
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

async function filterProcessInstances(keycloak, businessKey) {
    if (!businessKey) {
        businessKey = '';
    }

    const url = `${process.env.REACT_APP_API_URL}/process-instance/?businessKey=${businessKey}`;

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
