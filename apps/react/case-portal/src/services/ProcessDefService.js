import { json, nop } from './request'
import Config from 'consts/index'

export const ProcessDefService = {
  start,
  find,
  getBPMNXml,
}

async function start(keycloak, procDefKey, businessKey) {
  const url = `${Config.CaseEngineUrl}/process-definition/key/${procDefKey}/start`

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify({ businessKey: businessKey }),
    })
    return json(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function find(keycloak) {
  if (keycloak.isTokenExpired()) {
    keycloak.logout({ redirectUri: window.location.origin })
  }

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  var url = `${Config.CaseEngineUrl}/process-definition`

  try {
    const resp = await fetch(url, { headers })
    return json(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getBPMNXml(keycloak, processDefId) {
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  var url = `${Config.CaseEngineUrl}/process-definition/${processDefId}/xml`

  try {
    const resp = await fetch(url, { headers })
    // This endpoint returns raw BPMN XML, not JSON. Guard the status via nop()
    // (rejects on non-OK/401) then read the body as text. Previously this passed
    // resp.text() (a Promise) to json(), which never worked.
    await nop(keycloak, resp)
    return resp.text()
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
