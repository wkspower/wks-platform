import Config from 'consts/index'
import { json } from '../../request'

export const InputApiService = {
  getOperationHoursData,
  saveOperationHours,
  saveOperationHoursExcel,
  saveSTGOperationHoursExcel,

  getImportPowerData,
  saveImportPower,
  saveImportPowerExcel,

  getAssetPriority,
  saveAssetPriority,
  saveAssetPriorityExcel,

  getAssetCapacity,
  saveAssetCapacity,
  saveAssetCapacityExcel,

  getPlantList,
  getHeatRateData,
  saveHeatRateData,
  saveHeatRateExcel,

  getSTGHeatRateData,
  saveSTGHeatRateData,
  saveSTGHeatRateExcel,

  getHRSGHeatRateData,
  saveHRSGHeatRateData,
  saveHRSGHeatRateExcel,

  // Generic Excel Import
  saveExcelData,
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

// ========================|| Plant List APIs ||=====================================//
async function getPlantList(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/heat-rate/drop-down/${plantId}`
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

// ========================|| Heat Rate APIs ||=====================================//
async function getHeatRateData(keycloak, assetId) {
  const url = `${Config.CaseEngineUrl}/task/heat-rate/${assetId}`
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

async function saveHeatRateData(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/heat-rate/${AOP_YEAR}`
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

// ========================|| STG Heat Rate APIs ||=====================================//
async function getSTGHeatRateData(keycloak, plantId) {
  const url = `${Config.CaseEngineUrl}/task/stg-extraction-lookup`
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

async function saveSTGHeatRateData(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/stg-extraction-lookup/${AOP_YEAR}`
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

// ========================|| HRSG Heat Rate APIs ||=====================================//
async function getHRSGHeatRateData(keycloak, plantId) {
  const url = `${Config.CaseEngineUrl}/task/hrsg-heat-rate-lookup`
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

async function saveHRSGHeatRateData(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/hrsg-heat-rate-lookup/${AOP_YEAR}`
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

// ===================== || GENERIC EXCEL IMPORT FUNCTION || ===================== //
/**
 * Generic function to upload Excel file to any CPP Input endpoint
 * @param {File} file - The Excel file to upload
 * @param {Object} keycloak - Keycloak session object
 * @param {string} endpoint - The API endpoint path (e.g., 'import-power/import')
 * @param {string} PLANT_ID - Plant ID
 * @param {string} AOP_YEAR - Financial year
 * @returns {Promise} API response
 */
async function saveExcelData(file, keycloak, endpoint, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/${endpoint}?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const formData = new FormData()
  formData.append('file', file)
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: formData,
    })
    if (!resp.ok) {
      throw new Error(
        `Failed to import data: ${resp.status} ${resp.statusText}`,
      )
    }
    return json(keycloak, resp)
  } catch (e) {
    console.error(`Error importing Excel data to ${endpoint}:`, e)
    return Promise.reject(e)
  }
}

// ===================== || SPECIFIC EXCEL IMPORT FUNCTIONS || ===================== //

// Operation Hours Excel Import (Power)
async function saveOperationHoursExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'assets/operational-hours/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// STG Operation Hours Excel Import (Steam)
async function saveSTGOperationHoursExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'assets/stg-operational-hours/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// Import Power Excel Import
async function saveImportPowerExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'asset-import-mapping/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// Asset Priority Excel Import
async function saveAssetPriorityExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'asset-priority/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// Asset Capacity Excel Import
async function saveAssetCapacityExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'asset-capacity/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// Heat Rate Excel Import (GT Heat Rate)
async function saveHeatRateExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(file, keycloak, 'heat-rate/import', PLANT_ID, AOP_YEAR)
}

// STG Heat Rate Excel Import
async function saveSTGHeatRateExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'stg-extraction-lookup/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// HRSG Heat Rate Excel Import
async function saveHRSGHeatRateExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'hrsg-heat-rate-lookup/import',
    PLANT_ID,
    AOP_YEAR,
  )
}
