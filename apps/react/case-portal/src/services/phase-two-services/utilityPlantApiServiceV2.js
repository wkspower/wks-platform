
import Config from 'consts/index'
import { json } from '../../services/request'
export const UtilityPlantApiServiceV2 = {
  //  Fixed Consumption APIs
  getFixedConsumptionData,
  saveFixedConsumptionData,

  //   Plant requirement APIs
  getPlantRequirementData,
  savePlantRequirementData,

  // Import Consumption APIs
  getImportConsumptionData,
  saveImportConsumptionData,
}

// ===================== || Fixed Consumption APIs || ===================== //
async function getFixedConsumptionData(keycloak, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/consumption/fixed-consumption?plantId=${PLANT_ID}`
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
async function saveFixedConsumptionData(keycloak, PLANT_ID, payload) {
  const url = `${Config.CaseEngineUrl}/task/consumption/update-fixed-consumption`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = payload
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ===================== || Plant Requirement APIs || ===================== //
async function getPlantRequirementData(keycloak, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/consumption/plant-requirement?plantId=${PLANT_ID}`
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
async function savePlantRequirementData(keycloak, PLANT_ID, payload) {
  const url = `${Config.CaseEngineUrl}/consumption/plant-requirement-import?plantId=${PLANT_ID}`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = payload
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ===================== || Import  Consumption APIs || ===================== //
async function getImportConsumptionData(keycloak, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/consumption/import-power?plantId=${PLANT_ID}`
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
async function saveImportConsumptionData(keycloak, PLANT_ID, payload) {
  const url = `${Config.CaseEngineUrl}/task/consumption/update-import-power`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = payload
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

