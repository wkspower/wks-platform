import { json, nop } from './request';
import Config from '../consts';

export const RecordService = {
  getRecordTypeById,
  getAllRecordTypes,
  createRecordType,
  getRecordById,
  updateRecord,
  createRecord,
  deleteRecord,
};

async function getRecordById(keycloak, id) {
  const url = `${Config.CaseEngineUrl}/record/${id}`;

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  };

  try {
    const resp = await fetch(url, { headers });
    return json(keycloak, resp);
  } catch (e) {
    console.log(e);
    return await Promise.reject(e);
  }
}

async function getAllRecordTypes(keycloak) {
  const url = `${Config.CaseEngineUrl}/record-type`;

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  };

  try {
    const resp = await fetch(url, { headers });
    return json(keycloak, resp);
  } catch (e) {
    console.log(e);
    return await Promise.reject(e);
  }
}

async function getRecordTypeById(keycloak, id) {
  const url = `${Config.CaseEngineUrl}/record-type/${id}`;

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  };

  try {
    const resp = await fetch(url, { headers });
    return json(keycloak, resp);
  } catch (e) {
    console.log(e);
    return await Promise.reject(e);
  }
}

async function createRecordType(keycloak, id, data) {
  const url = `${Config.CaseEngineUrl}/record-type/${id}`;

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(data),
    });
    return json(keycloak, resp);
  } catch (e) {
    console.log(e);
    return await Promise.reject(e);
  }
}

async function createRecord(keycloak, id, data) {
  const url = `${Config.CaseEngineUrl}/record/${id}`;

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(data),
    });
    return nop(keycloak, resp);
  } catch (e) {
    console.log(e);
    return await Promise.reject(e);
  }
}

async function updateRecord(keycloak, id, oid, data) {
  const url = `${Config.CaseEngineUrl}/record/${id}/${oid}`;

  try {
    const resp = await fetch(url, {
      method: 'PATCH',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(data),
    });
    return nop(keycloak, resp);
  } catch (e) {
    console.log(e);
    return await Promise.reject(e);
  }
}

async function deleteRecord(keycloak, id, oid) {
  const url = `${Config.CaseEngineUrl}/record/${id}/${oid}`;

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
  } catch (e) {
    console.log(e);
    return await Promise.reject(e);
  }
}
