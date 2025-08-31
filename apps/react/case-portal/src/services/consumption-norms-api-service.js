import Config from '../consts'
import { json } from './request'
export const ConsumptionNormsApiService = {
  saveAOPConsumptionNorm,
  getConsumptionAOPNormsGrades,
  getConsumptionNormsData,
  handleCalculateonsumptionNorms,
}
async function saveAOPConsumptionNorm(plantId, shutdownDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/overall-consumption`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(shutdownDetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function getConsumptionAOPNormsGrades(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/consumption-aop/grades?year=${year}&plantId=${plantId}`
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
async function getConsumptionNormsData(keycloak, gradeId) {
  const year = localStorage.getItem('year') || ''
  const storedPlant = localStorage.getItem('selectedPlant')
  const plantId = storedPlant ? JSON.parse(storedPlant)?.id || '' : ''
  // Construct URL based on presence of gradeId
  const baseUrl = `${Config.CaseEngineUrl}/task/overall-consumption`
  const queryParams = new URLSearchParams({
    plantId,
    year,
  })
  if (gradeId) {
    queryParams.append('gradeId', gradeId)
  }
  const url = `${baseUrl}?${queryParams.toString()}`
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

async function handleCalculateonsumptionNorms(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/calculate-overall-consumption?year=${year1}&plantId=${plantId}`
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
    const data = await resp.json() // Parse JSON response
    return data
  } catch (e) {
    console.error('Error fetching calculation data:', e)
    return Promise.reject(e)
  }
}
