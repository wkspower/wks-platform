import { json, nop } from './request';
import Config from '../consts';

export const FormService = {
  getAll,
  getByKey,
  getVariableById,
  update,
  remove,
  create,
};

async function create(keycloak, body) {
  const url = `${Config.CaseEngineUrl}/form`;

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(body),
    });
    return nop(keycloak, resp);
  } catch (err) {
    console.log(err);
    return await Promise.reject(err);
  }
}

async function update(keycloak, id, body) {
  const url = `${Config.CaseEngineUrl}/form/${id}`;

  try {
    const resp = await fetch(url, {
      method: 'PATCH',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(body),
    });
    return nop(keycloak, resp);
  } catch (err) {
    console.log(err);
    return await Promise.reject(err);
  }
}

async function remove(keycloak, id) {
  const url = `${Config.CaseEngineUrl}/form/${id}`;

  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
    });
    return nop(keycloak, resp);
  } catch (err) {
    console.log(err);
    return await Promise.reject(err);
  }
}

async function getAll(keycloak) {
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  };

  var url = `${Config.CaseEngineUrl}/form`;

  try {
    const resp = await fetch(url, { headers });
    return json(keycloak, resp);
  } catch (err) {
    console.log(err);
    return await Promise.reject(err);
  }
}

async function getByKey(keycloak, formKey) {
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  };

  var url = `${Config.CaseEngineUrl}/form/${formKey}`;

  try {
    const resp = await fetch(url, { headers });
    return json(keycloak, resp);
  } catch (err) {
    console.log(err);
    return await Promise.reject(err);
  }
}

async function getVariableById(keycloak, processInstanceId) {
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  };

  const body = JSON.stringify({ processInstanceId });

  var url = `${Config.CaseEngineUrl}/variable`;

  try {
    const resp = await fetch(url, { headers, body });
    return json(keycloak, resp);
  } catch (err) {
    console.log(err);
    return await Promise.reject(err);
  }
}
