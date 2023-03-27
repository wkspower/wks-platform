import Config from '../consts';
import i18n from '../i18n';
import { json, nop } from './request';

export const CaseService = {
    getAllByStatus,
    getCaseDefinitions,
    getCaseDefinitionsById,
    getCaseById,
    filterCase,
    createCase,
    updateCaseStatusById,
    addDocuments,
    addComment,
    updateComment,
    deleteComment
};

async function getAllByStatus(keycloak, status, limit) {
    if (!status) {
        return Promise.resolve([]);
    }

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${Config.EngineUrl}/case/?status=${status}&limit=${limit}`;

    try {
        const resp = await fetch(url, { headers });
        const data = json(keycloak, resp);
        return mapperToCase(data);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function getCaseDefinitions(keycloak) {
    const url = `${Config.EngineUrl}/case-definition/`;

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

async function getCaseDefinitionsById(keycloak, caseDefId) {
    const url = `${Config.EngineUrl}/case-definition/${caseDefId || ''}`;

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

async function getCaseById(keycloak, id) {
    let url = `${Config.EngineUrl}/case/${id}`;

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

async function filterCase(keycloak, caseDefId, status) {
    let url = `${Config.EngineUrl}/case/?`;
    url = url + (status ? `status=${status}` : '');
    url = url + (caseDefId ? `&caseDefinitionId=${caseDefId}` : '');

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, { headers });
        const data = json(keycloak, resp);
        return mapperToCase(data);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function updateCaseStatusById(keycloak, id, body) {
    const url = `${Config.EngineUrl}/case/${id}`;

    try {
        const resp = await fetch(url, {
            method: 'PATCH',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: body
        });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function createCase(keycloak, body) {
    const url = `${Config.EngineUrl}/case/`;

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: body
        });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}

function mapperToCase(data) {
    if (!data.length) {
        return Promise.resolve(data);
    }

    const toStatus = (status) => {
        const mapper = {
            WIP_CASE_STATUS: i18n.t('general.case.status.wip'),
            CLOSED_CASE_STATUS: i18n.t('general.case.status.closed'),
            ARCHIVED_CASE_STATUS: i18n.t('general.case.status.archived')
        };

        return mapper[status] || 'Indefinido';
    };

    const toCase = data.map((element) => {
        const createdAt = element.attributes.find((attribute) => attribute.name === 'createdAt');
        element.createdAt = createdAt ? createdAt.value : '11/12/2022';
        element.statusDescription = toStatus(element.status);
        return element;
    });

    return Promise.resolve(toCase);
}

async function addDocuments(keycloak, id, files) {
    const url = `${process.env.REACT_APP_API_URL}/case/upload/${id}`;

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: JSON.stringify(files)
        });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function addComment(keycloak, text, parentId, businessKey) {
    const url = `${process.env.REACT_APP_API_URL}/case/${businessKey}/comment`;

    const comment = {
        body: text,
        parentId,
        userId: keycloak.tokenParsed.preferred_username,
        userName: keycloak.tokenParsed.given_name,
        caseId: businessKey
    };

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: JSON.stringify(comment)
        });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function updateComment(keycloak, text, commentId, businessKey) {
    const url = `${process.env.REACT_APP_API_URL}/case/${businessKey}/comment/${commentId}`;

    const comment = {
        id: commentId,
        body: text,
        userId: keycloak.tokenParsed.preferred_username,
        caseId: businessKey
    };

    try {
        const resp = await fetch(url, {
            method: 'PATCH',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: JSON.stringify(comment)
        });
        return nop(keycloak, resp);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function deleteComment(keycloak, commentId, businessKey) {
    const url = `${process.env.REACT_APP_API_URL}/case/${businessKey}/comment/${commentId}`;

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
