
import Config from 'consts/index'
import { json } from '../../services/request'
import { DataService } from '../DataService'

export const InputApiService = {
  getOperationHoursData,
}

// ===================== ||Data APIs || ===================== //
async function getOperationHoursData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/assets/operational-hours/${plantId}/${year}`;
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function saveTcsInputData(keycloak, PLANT_ID, AOP_YEAR, tabName, payload) {
  const url = `${Config.CaseEngineUrl}/task/save-tcs-input-data`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    plantId: PLANT_ID,
    year: AOP_YEAR,
    tabName: tabName,
    data: payload,
  })
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    const result = await json(keycloak, resp)
    return result || { success: true }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

