import Config from 'consts/index'
import { json } from '../../services/request'
import { DataService } from '../DataService'

export const InputApiService = {
  getOperationHoursData,
  saveOperationHours,

  getImportPowerData,
  saveImportPower,

  getAssetPriority,
  saveAssetPriority,

  getAssetCapacity,
  saveAssetCapacity,
}

// ===================== ||Shutdown and Operational hrs APIs || ===================== //
async function getOperationHoursData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/assets/operational-hours/${plantId}/${year}`
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

async function saveOperationHours(keycloak, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/assets/operational-hours/${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify(payload)
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

// ========================|| Import Power APIs ||=====================================//
async function getImportPowerData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/asset-import-mapping/${plantId}/${year}`
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

async function saveImportPower(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/asset-import-mapping/${PLANT_ID}/${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify(payload)
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

// ========================|| Asset Priority APIs ||=====================================//
async function getAssetPriority(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/asset-priority/${plantId}/${year}`
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

async function saveAssetPriority(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/asset-priority/${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify(payload)
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

// ========================|| Asset Capacity APIs ||=====================================//
async function getAssetCapacity(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/asset-capacity/${plantId}/${year}`
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

async function saveAssetCapacity(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/asset-capacity/${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify(payload)
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
