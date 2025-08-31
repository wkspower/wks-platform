import Config from '../consts'
import { json } from './request'
export const ProductionNormsApiService = {
  updateProductNormData,
  handleCalculate,
  getAOPData,
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
async function handleCalculate(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/calculate-monthly-production?year=${year1}&plantId=${plantId}`
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
async function getAOPData(keycloak, type) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/monthly-production?plantId=${plantId}&year=${year}&type=${type}`
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
