import { json, nop } from './request'
import Config from '../consts'

export const CaseDefService = {
  create,
  update,
  remove,
  getAll,
}

async function create(keycloak, body) {
  const url = `${Config.CaseEngineUrl}/case-definition`

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(body),
    })
    return nop(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function update(keycloak, id, body) {
  const url = `${Config.CaseEngineUrl}/case-definition/${id}`

  try {
    const resp = await fetch(url, {
      method: 'PUT',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(body),
    })
    return nop(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function remove(keycloak, id) {
  const url = `${Config.CaseEngineUrl}/case-definition/${id}`

  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
    })
    return nop(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getAll(keycloak) {
  if (keycloak.isTokenExpired()) {
    keycloak.logout({ redirectUri: window.location.origin })
  }

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  var url = `${Config.CaseEngineUrl}/case-definition`

  try {
    const resp = await fetch(url, { headers })
    return json(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
