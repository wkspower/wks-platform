import i18n from '../i18n';
import { json, nop } from './request';
import Config from '../consts';

export const CaseService = {
    getAllByStatus,
    getCaseDefinitions,
    getCaseDefinitionsById,
    getCaseById,
    filterCase,
    getRecordTypes,
    updateCaseStatusById,
    uploadCaseAttachById,
    createCase,
    addComment,
    editComment,
    deleteComment,
    addAttachment
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
        const data = await json(keycloak, resp);
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

async function filterCase(keycloak, caseDefId, status, cursor) {
    let url = `${Config.EngineUrl}/case/?`;
    url = url + (status ? `status=${status}` : '');
    url = url + (caseDefId ? `&caseDefinitionId=${caseDefId}` : '');
    url = url + `&before=${cursor.before || ''}`;
    url = url + `&after=${cursor.after || ''}`;
    url = url + `&sort=${cursor.sort || 'asc'}`;
    url = url + `&limit=${cursor.limit || 10}`;

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    try {
        const resp = await fetch(url, { headers });
        const data = await json(keycloak, resp);
        return mapperToCase(data);
    } catch (e) {
        console.log(e);
        return await Promise.reject(e);
    }
}

async function getRecordTypes(keycloak) {
    const url = `${Config.EngineUrl}/record-type/`;

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

async function uploadCaseAttachById(keycloak, id, files) {
    const url = `${Config.EngineUrl}/case/upload/${id}`;

    try {
        const resp = await fetch(url, {
            method: 'PUT',
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

async function addComment(keycloak, text, parentId, businessKey) {
    const url = `${Config.EngineUrl}/case/comment`;

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

async function addAttachment(keycloak, aCase, attachment) {
    const url = `${Config.EngineUrl}/case/${aCase.businessKey}/attachments`;

    const { preferred_username, given_name } = keycloak.tokenParsed;

    const data = {
        ...attachment,
        userId: preferred_username,
        userName: given_name
    };

    try {
        const resp = await fetch(url, {
            method: 'PUT',
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

async function editComment(keycloak, text, commentId, businessKey) {
    const url = `${Config.EngineUrl}/case/comment`;

    const comment = {
        id: commentId,
        body: text,
        userId: keycloak.tokenParsed.preferred_username,
        caseId: businessKey
    };

    try {
        const resp = await fetch(url, {
            method: 'PUT',
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
    const url = `${Config.EngineUrl}/case/comment/delete`;

    const comment = {
        id: commentId,
        userId: keycloak.tokenParsed.preferred_username,
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

function mapperToCase(resp) {
    const { data, paging } = resp;

    if (!data.length) {
        return Promise.resolve({ data: [], paging: {} });
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
        const createdAt = element?.attributes?.find((attribute) => attribute.name === 'createdAt');
        element.createdAt = createdAt ? createdAt.value : '';
        element.statusDescription = toStatus(element.status);
        return element;
    });

    const toPaging = {
        cursors: paging.cursors,
        hasPrevious: paging.hasPrevious,
        hasNext: paging.hasNext
    };

    return Promise.resolve({ data: toCase, paging: toPaging });
}
