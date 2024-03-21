import { json, nop } from './request';
import Config from 'consts/index';

export const TaskService = {
    getActivityInstancesById,
    claim,
    unclaim,
    complete,
    createNewTask,
    filterTasks,
    filterProcessInstances
};

async function getActivityInstancesById(keycloak, processInstanceId) {
    const url = `${Config.CaseEngineUrl}/process-instance/${processInstanceId}/activity-instances`;

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

async function claim(keycloak, taskId) {
    const url = `${Config.CaseEngineUrl}/task/${taskId}/claim/${keycloak.idTokenParsed.given_name}`;

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

async function unclaim(keycloak, taskId) {
    const url = `${Config.CaseEngineUrl}/task/${taskId}/unclaim`;

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

async function complete(keycloak, taskId, body) {
    const url = `${Config.CaseEngineUrl}/task/${taskId}/complete`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers,
            body: JSON.stringify(body)
        });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function createNewTask(keycloak, body) {
    const url = `${Config.CaseEngineUrl}/task/create`;

    const headers = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers,
            body: JSON.stringify(body)
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

    const url = `${Config.CaseEngineUrl}/task?${query}`;

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

    const url = `${Config.CaseEngineUrl}/process-instance?businessKey=${businessKey}`;

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
