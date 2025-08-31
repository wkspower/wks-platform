import Config from '../consts'
import { json } from './request'
export const BusinessDemandDataApiService = {
  getBDData,
  saveBusinessDemandData,
  deleteBusinessDemandData,
}
async function getBDData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/business-demand?year=${year}&plantId=${plantId}`
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
async function saveBusinessDemandData(plantId, turnAroundDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/business-demand`
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
async function deleteBusinessDemandData(maintenanceId, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/business-demand/${maintenanceId}`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers,
    })
    if (!resp.ok) {
      throw new Error(
        `Failed to delete data: ${resp.status} ${resp.statusText}`,
      )
    }
    return await resp.text() // Handle text response from the backend
  } catch (e) {
    console.error('Error deleting slowdown data:', e)
    return Promise.reject(e)
  }
}
