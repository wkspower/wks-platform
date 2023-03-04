import i18n from '../i18n';

export const CaseService = {
    getAllByStatus,
    getCaseDefinitions,
    getCaseDefinitionsById,
    getCaseById,
    getRecordTypes
};

function getAllByStatus(keycloak, status, limit) {
    if (!status) {
        return Promise.resolve([]);
    }

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${process.env.REACT_APP_API_URL}/case/?status=${status}&limit=${limit}`;

    return fetch(url, { headers })
        .then((response) => response.json())
        .then(mapperToCase);
}

function getCaseDefinitions(keycloak) {
    const url = `${process.env.REACT_APP_API_URL}/case-definition/`;

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    return fetch(url, { headers }).then((response) => response.json());
}

function getCaseDefinitionsById(keycloak, caseDefId) {
    const url = `${process.env.REACT_APP_API_URL}/case-definition/${caseDefId || ''}`;

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    return fetch(url, { headers }).then((response) => response.json());
}

function getCaseById(keycloak, caseDefId, status) {
    let url = `${process.env.REACT_APP_API_URL}/case/?`;
    url = url + (status ? `status=${status}` : '');
    url = url + (caseDefId ? `&caseDefinitionId=${caseDefId}` : '');

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    return fetch(url, { headers })
        .then((response) => response.json())
        .then(mapperToCase);
}

function getRecordTypes(keycloak) {
    const url = `${process.env.REACT_APP_API_URL}/record-type/`;

    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    return fetch(url, { headers }).then((response) => response.json());
}

function mapperToCase(data) {
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
