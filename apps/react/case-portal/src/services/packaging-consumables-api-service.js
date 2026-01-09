import Config from '../consts'
import { json } from './request'
export const PackagingConsumablesApiService = {//PackagingConsumablesApiService
  getPackagingData,
  savePackagingData,//savePackagingData
}
async function getPackagingData(keycloak, PLANT_ID, AOP_YEAR) {
  
  let url
    url = `${Config.CaseEngineUrl}/task/packaging-consumables?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function savePackagingData(
  PLANT_ID,
  payload,
  keycloak,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/packaging-consumables?year=${AOP_YEAR}&plantFKId=${PLANT_ID}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}