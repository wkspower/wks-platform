import Config from '../consts'
import { json } from './request'

export const OptimizerDataApiService = {
  fetchModes,
}
async function fetchModes(keycloak, PLANT_ID, AOP_YEAR, TYPE) {
  const url = `${Config.CaseEngineUrl}/task/modes?year=${AOP_YEAR}&plantId=${PLANT_ID}&type=${TYPE}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
