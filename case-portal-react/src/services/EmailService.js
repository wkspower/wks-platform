import { json } from './request';

export const EmailService = {
    getAllByBusinessKey
};

async function getAllByBusinessKey(keycloak, caseInstanceBusinessKey) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${process.env.REACT_APP_EMAIL_URL}/email/?caseInstanceBusinessKey=${caseInstanceBusinessKey}`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}
