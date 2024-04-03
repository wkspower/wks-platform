import { json } from './request';
import Config from '../consts';

export const EmailService = {
    getAllByBusinessKey
};

async function getAllByBusinessKey(keycloak, caseInstanceBusinessKey) {
    const headers = {
        Authorization: `Bearer ${keycloak.token}`
    };

    var url = `${Config.EmailUrl}/email-to-case/?caseInstanceBusinessKey=${caseInstanceBusinessKey}`;

    try {
        const resp = await fetch(url, { headers });
        return json(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}
