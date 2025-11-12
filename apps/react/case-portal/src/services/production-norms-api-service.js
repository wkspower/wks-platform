import Config from '../consts'
import { json } from './request'
export const ProductionNormsApiService = {
  updateProductNormData,
  handleCalculate,
  getAOPData,
  monthlyProductionC2rC3R,
  MonthwiseProductionExport,
}
async function updateProductNormData(turnAroundDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/monthly-production` // Corrected endpoint
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'PUT', // Ensure it matches @PutMapping
      headers,
      body: JSON.stringify(turnAroundDetails), // Updated variable name for clarity
    })
    if (!resp.ok) {
      throw new Error(
        `Failed to update data: ${resp.status} ${resp.statusText}`,
      )
    }
    return await resp.json() // Ensure proper response handling
  } catch (e) {
    console.error('Error updating turnaround data:', e)
    return Promise.reject(e)
  }
}
async function handleCalculate(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/calculate-monthly-production?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getAOPData(keycloak, type, PLANT_ID, AOP_YEAR) {

  const url = `${Config.CaseEngineUrl}/task/monthly-production?plantId=${PLANT_ID}&year=${AOP_YEAR}&type=${type}`
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
async function monthlyProductionC2rC3R(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/monthly-production-manual-entry?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
export async function MonthwiseProductionExport(keycloak, plantId, year, type) {
  const url = `${Config.CaseEngineUrl}/task/monthly-production-export?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&type=${encodeURIComponent(type)}`
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
    a.download = 'vcm_Month wise Production plan.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Month wise Production plan Excel:', e)
    return Promise.reject(e)
  }
}