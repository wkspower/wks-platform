import Config from '../consts'
import { json } from './request'
export const ShutdownNormsApiService = {
  getShutdownMonths,
  saveShutDownNormsData,
  handleCalculateShutdownNorms,
  getShutdownNormsData,
  shutdownConsumptionHistoryData,
}
async function getShutdownMonths(keycloak, gradeId) {
  var year = localStorage.getItem('year')
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)
  let url
  if (gradeId) {
    url = `${Config.CaseEngineUrl}/task/shutdown-months?plantId=${parsedPlant.id}&maintenanceName=Shutdown&year=${year}&gradeId=${gradeId}`
  } else {
    url = `${Config.CaseEngineUrl}/task/shutdown-months?plantId=${parsedPlant.id}&maintenanceName=Shutdown&year=${year}`
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
async function handleCalculateShutdownNorms(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  //  const url = `${Config.CaseEngineUrl}/task/getCalculatedShutdownNorms?year=${year1}&plantId=${plantId}`
  const url = `${Config.CaseEngineUrl}/task/calculate-shutdown-consumption?year=${year1}&plantId=${plantId}`
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
async function getShutdownNormsData(keycloak, gradeId) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  // let siteID =
  //   JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

  let url
  if (gradeId) {
    url = `${Config.CaseEngineUrl}/task/shutdown-consumption?year=${year}&plantId=${plantId}&gradeId=${gradeId}`
  } else {
    url = `${Config.CaseEngineUrl}/task/shutdown-consumption?year=${year}&plantId=${plantId}`
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
async function shutdownConsumptionHistoryData(keycloak, gradeId) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/shutdown-consumption?year=${year}&plantId=${plantId}`

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
async function saveShutDownNormsData(plantId, turnAroundDetails, keycloak) {
  const year = localStorage.getItem('year')

  let plantId1 = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId1 = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/shutdown-consumption?plantId=${plantId1}`
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
