import Config from '../consts'
import { json } from './request'
export const ShutdownNormsApiService = {
  getShutdownMonths,
  saveShutDownNormsData,
  handleCalculateShutdownNorms,
  getShutdownNormsData,
  shutdownConsumptionHistoryData,
}
async function getShutdownMonths(keycloak, gradeId, PLANT_ID, AOP_YEAR) {

  let url
  if (gradeId) {
    url = `${Config.CaseEngineUrl}/task/shutdown-months?plantId=${PLANT_ID}&maintenanceName=Shutdown&year=${AOP_YEAR}&gradeId=${gradeId}`
  } else {
    url = `${Config.CaseEngineUrl}/task/shutdown-months?plantId=${PLANT_ID}&maintenanceName=Shutdown&year=${AOP_YEAR}`
  }
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
async function handleCalculateShutdownNorms(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/calculate-shutdown-consumption?year=${AOP_YEAR}&plantId=${PLANT_ID}`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'GET',
      headers,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    const data = await resp.json()
    return data
  } catch (e) {
    console.error('Error fetching calculation data:', e)
    return Promise.reject(e)
  }
}
async function getShutdownNormsData(keycloak, gradeId, PLANT_ID, AOP_YEAR) {
  
  let url
  if (gradeId) {
    url = `${Config.CaseEngineUrl}/task/shutdown-consumption?year=${AOP_YEAR}&plantId=${PLANT_ID}&gradeId=${gradeId}`
  } else {
    url = `${Config.CaseEngineUrl}/task/shutdown-consumption?year=${AOP_YEAR}&plantId=${PLANT_ID}`
  }

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
async function shutdownConsumptionHistoryData(keycloak, gradeId, PLANT_ID, AOP_YEAR) {

  const url = `${Config.CaseEngineUrl}/task/shutdown-consumption?year=${AOP_YEAR}&plantId=${PLANT_ID}`

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
async function saveShutDownNormsData(
  plantId,
  turnAroundDetails,
  keycloak,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-consumption?plantId=${plantId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(turnAroundDetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
