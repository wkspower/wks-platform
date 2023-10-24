import { nop } from './request';
import Config from 'consts/index';

export const DeploymentService = {
    deploy
};

async function deploy(keycloak, file) {
    const url = `${Config.CaseEngineUrl}/deployment`;

    try {
        const resp = await fetch(url, {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
                Authorization: `Bearer ${keycloak.token}`
            },
            body: file
        });
        return nop(keycloak, resp);
    } catch (err) {
        console.log(err);
        return await Promise.reject(err);
    }
}