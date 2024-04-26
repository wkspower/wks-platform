import { json } from './request'
import Config from '../consts'

export const EmailService = {
  send,
  getAllByBusinessKey,
}

async function send(keycloak, body) {
  const url = `${Config.CaseEngineUrl}/case-email`

  try {
    await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        Authorization: `Bearer ${keycloak.token}`,
      },
      body: JSON.stringify(body),
    })
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getAllByBusinessKey(keycloak, caseInstanceBusinessKey) {
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  var url = `${Config.CaseEngineUrl}/case-email?caseInstanceBusinessKey=${caseInstanceBusinessKey}`

  try {
    const resp = await fetch(url, { headers })
    return json(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
