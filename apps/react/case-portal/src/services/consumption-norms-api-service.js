import Config from '../consts'
import { json } from './request'
export const ConsumptionNormsApiService = {
  saveAOPConsumptionNorm,
  getConsumptionAOPNormsGrades,
  getConsumptionNormsData,
  handleCalculateonsumptionNorms,
  OverallconsumptionppExport,
}
async function saveAOPConsumptionNorm(PLANT_ID, shutdownDetails, keycloak) {
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
async function getConsumptionAOPNormsGrades(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/consumption-aop/grades?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getConsumptionNormsData(keycloak, gradeId, PLANT_ID, AOP_YEAR) {
  const year = AOP_YEAR
  const plantId = PLANT_ID
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

async function handleCalculateonsumptionNorms(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/calculate-overall-consumption?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
export async function OverallconsumptionppExport(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/overall-consumption-export?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}`
  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'GET',
      headers,
    })
    if (!resp.ok) {
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'Overall_consumption.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Shutdown Excel:', e)
    return Promise.reject(e)
  }
}