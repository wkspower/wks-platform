import { json } from './request'
import Config from '../consts'

export const VariableService = {
  getByProcessInstanceId,
}

async function getByProcessInstanceId(keycloak, processInstanceId) {
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }

  var url = `${Config.CaseEngineUrl}/variable?processInstanceId=${processInstanceId}`

  try {
    const resp = await fetch(url, { headers })
    return json(keycloak, resp)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
