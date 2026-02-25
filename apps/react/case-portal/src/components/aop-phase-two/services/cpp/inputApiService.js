import Config from 'consts/index'
import { json } from '../../../../services/request'

export const InputApiService = {
  getOperationHoursData,
  saveOperationHours,
  saveOperationHoursExcel,
  saveSTGOperationHoursExcel,
  exportPowerResponseExcel,
  savePowerResponseExcel,
  exportSteamResponseExcel,
  saveSteamResponseExcel,

  getImportPowerData,
  saveImportPower,
  saveImportPowerExcel,
  exportImportPowerExcel,
  getImportPowerCapacity,
  saveImportPowerCapacity,
  saveImportPowerCapacityExcel,
  exportImportPowerCapacityExcel,
  getImportPowerOperationalHours,
  saveImportPowerOperationalHours,
  saveImportPowerOperationalHoursExcel,
  exportImportPowerOperationalHoursExcel,

  getAssetPriority,
  saveAssetPriority,
  saveAssetPriorityExcel,
  exportAssetPriorityExcel,

  getAssetCapacity,
  saveAssetCapacity,
  saveAssetCapacityExcel,
  exportAssetCapacityExcel,

  getPlantList,
  getHeatRateData,
  saveHeatRateData,
  saveHeatRateExcel,
  exportHeatRateExcel,
  getConfigurationExecutionDetails,
  executeConfiguration,

  getSTGHeatRateData,
  saveSTGHeatRateData,
  saveSTGHeatRateExcel,
  exportSTGHeatRateExcel,

  getHRSGHeatRateData,
  saveHRSGHeatRateData,
  saveHRSGHeatRateExcel,
  exportHRSGHeatRateExcel,
  getHRSGHeatRateDropdown,

  getNormBasedUtilityBudget,
  saveNormsData,
  exportCPPNormsExcel,
  saveCPPNormsExcel,

  getFuelAvailabilityData,
  saveFuelAvailabilityData,
  saveFuelAvailabilityExcel,
  exportFuelAvailabilityExcel,

  // Generic Excel Import/Export
  saveExcelData,
  exportExcelData,
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

async function exportCPPNormsExcel(keycloak, PLANT_ID, financialYear) {
  return exportExcelData(keycloak, {
    endpoint: `cpp-norms/export`,
    queryParams: { cppPlantId: PLANT_ID, financialYear },
    fileName: `CPPNorms_${financialYear}.xlsx`,
    method: 'GET',
  })
}

async function saveCPPNormsExcel(file, keycloak, PLANT_ID, financialYear) {
  const url = `${Config.CaseEngineUrl}/task/cpp-norms/import?cppPlantId=${PLANT_ID}&financialYear=${financialYear}`
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

    const responseData = await json(keycloak, resp)

    if (resp.status === 400 || resp.status === 200) {
      return responseData
    }

    if (!resp.ok) {
      throw new Error(
        `Failed to import data: ${resp.status} ${resp.statusText}`,
      )
    }

    return responseData
  } catch (e) {
    console.error(`Error importing CPP Norms Excel:`, e)
    return Promise.reject(e)
  }
}

async function saveOperationHours(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/assets/operational-hours/${PLANT_ID}/${AOP_YEAR}`
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

// Power Response Excel Import
async function savePowerResponseExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'assets/power-response/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// Power Response Excel Export
async function exportPowerResponseExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `assets/power-response/export/${PLANT_ID}/${AOP_YEAR}`,
    queryParams: {},
    fileName: `Power_Generation_${AOP_YEAR}.xlsx`,
    method: 'GET',
  })
}

// Steam Response Excel Import
async function saveSteamResponseExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'assets/steam-response/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// Steam Response Excel Export
async function exportSteamResponseExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `assets/steam-response/export/${PLANT_ID}/${AOP_YEAR}`,
    queryParams: {},
    fileName: `Steam_Generation_${AOP_YEAR}.xlsx`,
    method: 'GET',
  })
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

async function getImportPowerCapacity(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/import-power/capacity/${plantId}/${year}`
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

async function saveImportPowerCapacity(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/import-power/capacity/${PLANT_ID}/${AOP_YEAR}`
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

async function saveImportPowerCapacityExcel(
  file,
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) {
  return saveExcelData(
    file,
    keycloak,
    'import-power/capacity/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

async function exportImportPowerCapacityExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `import-power/capacity/export/${PLANT_ID}/${AOP_YEAR}`,
    fileName: `Import Power Capacity - ${PLANT_ID} - ${AOP_YEAR}.xlsx`,
  })
}

async function getImportPowerOperationalHours(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/import-power/operational-hours/${plantId}/${year}`
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

async function saveImportPowerOperationalHours(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/import-power/operational-hours/${PLANT_ID}/${AOP_YEAR}`
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

async function saveImportPowerOperationalHoursExcel(
  file,
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) {
  return saveExcelData(
    file,
    keycloak,
    'import-power/operational-hours/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

async function exportImportPowerOperationalHoursExcel(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) {
  return exportExcelData(keycloak, {
    endpoint: `import-power/operational-hours/export/${PLANT_ID}/${AOP_YEAR}`,
    fileName: `Import Power Operational Hours - ${PLANT_ID} - ${AOP_YEAR}.xlsx`,
  })
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

async function getHeatRateData(keycloak, assetId, year, startDate, endDate) {
  const url = `${Config.CaseEngineUrl}/task/heat-rate/${assetId}/${year}/${startDate}/${endDate}`

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
async function getHRSGHeatRateData(
  keycloak,
  assetId,
  financialYear,
  startDate,
  endDate,
) {
  let url = `${Config.CaseEngineUrl}/task/hrsg-heat-rate/${assetId}/${financialYear}`

  if (startDate && endDate) {
    url += `/${startDate}/${endDate}`
  }

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
  const url = `${Config.CaseEngineUrl}/task/hrsg-heat-rate/${AOP_YEAR}`
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

async function getHRSGHeatRateDropdown(keycloak, cppId) {
  const url = `${Config.CaseEngineUrl}/task/hrsg-heat-rate/drop-down/${cppId}`
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

//====================|| NORM BASED UTILITY BUDGET APIs ||====================//
async function getNormBasedUtilityBudget(keycloak, PLANT_ID, financialYear) {
  const url = `${Config.CaseEngineUrl}/task/cpp-norms?cppPlantId=${PLANT_ID}&financialYear=${financialYear}`
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

async function saveNormsData(keycloak, payload, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/cpp-norms/${AOP_YEAR}`
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
    return json(keycloak, resp)
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
  const url = `${Config.CaseEngineUrl}/task/${endpoint}/${PLANT_ID}/${AOP_YEAR}`
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

    const responseData = await json(keycloak, resp)

    // Return response data for both success and 400 (partial success with error file)
    // Components will handle error file download based on code and data
    if (resp.status === 400 || resp.status === 200) {
      return responseData
    }

    if (!resp.ok) {
      throw new Error(
        `Failed to import data: ${resp.status} ${resp.statusText}`,
      )
    }

    return responseData
  } catch (e) {
    console.error(`Error importing Excel data to ${endpoint}:`, e)
    return Promise.reject(e)
  }
}

// ===================== || GENERIC EXCEL EXPORT FUNCTION || ===================== //
/**
 * Generic function to export Excel file from backend
 * @param {Object} keycloak - Keycloak session object
 * @param {Object} params - Export parameters
 * @param {string} params.endpoint - The API endpoint path (e.g., 'export-excel')
 * @param {Object} params.queryParams - Query parameters (e.g., { year: '2024', plantId: '123', type: 'Production' })
 * @param {Object|null} params.payload - Optional POST body payload
 * @param {string} params.fileName - Downloaded file name (e.g., 'plant_production_plan.xlsx')
 * @param {string} params.method - HTTP method ('GET' or 'POST'), defaults to 'GET'
 * @returns {Promise} Success/error response
 */
async function exportExcelData(keycloak, params) {
  const {
    endpoint,
    queryParams = {},
    payload = null,
    fileName,
    method = 'GET',
  } = params

  const queryString = new URLSearchParams(queryParams).toString()
  const url = `${Config.CaseEngineUrl}/task/${endpoint}${queryString ? `?${queryString}` : ''}`

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const fetchOptions = {
      method,
      headers,
    }

    if (payload && (method === 'POST' || method === 'PUT')) {
      fetchOptions.body = JSON.stringify(payload)
    }

    const resp = await fetch(url, fetchOptions)

    if (!resp.ok) {
      throw new Error(
        `Failed to export Excel: ${resp.status} ${resp.statusText}`,
      )
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob

    // Extract filename from Content-Disposition header if available
    const contentDisposition = resp.headers.get('content-disposition')
    let downloadFileName = fileName
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="?([^";\n]+)"?/i)
      if (filenameMatch && filenameMatch[1]) {
        downloadFileName = filenameMatch[1]
      }
    }

    a.download = downloadFileName
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)

    return { success: true, message: 'Excel exported successfully' }
  } catch (e) {
    console.error(`Error exporting Excel from ${endpoint}:`, e)
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

// Import Power Excel Export
async function exportImportPowerExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `asset-import-mapping/export/${PLANT_ID}/${AOP_YEAR}`,
    queryParams: {},
    fileName: `plant_import_mapping_${AOP_YEAR}.xlsx`,
    method: 'GET',
  })
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

// Asset Priority Excel Export
async function exportAssetPriorityExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `asset-priority/export/${PLANT_ID}/${AOP_YEAR}`,
    queryParams: {},
    fileName: `asset_priority_${AOP_YEAR}.xlsx`,
    method: 'GET',
  })
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

// Asset Capacity Excel Export
async function exportAssetCapacityExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `asset-capacity/export/${PLANT_ID}/${AOP_YEAR}`,
    queryParams: {},
    fileName: `asset_capacity_${AOP_YEAR}.xlsx`,
    method: 'GET',
  })
}

// Heat Rate Excel Import (GT Heat Rate)
async function saveHeatRateExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/heat-rate/import`
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

    return { success: true }
  } catch (e) {
    console.error(`Error importing Heat Rate Excel:`, e)
    return Promise.reject(e)
  }
}

// Heat Rate Excel Export
async function exportHeatRateExcel(
  keycloak,
  assetId,
  financialYear,
  startDate = null,
  endDate = null,
) {
  // Construct endpoint with optional date range
  let endpoint = `heat-rate/export/${assetId}/${financialYear}`

  // If both dates are provided, add them as path variables
  if (startDate && endDate) {
    endpoint = `heat-rate/export/${assetId}/${financialYear}/${startDate}/${endDate}`
  }

  return exportExcelData(keycloak, {
    endpoint: endpoint,
    queryParams: {},
    fileName: `Heat_Rate_${financialYear}.xlsx`,
    method: 'GET',
  })
}

// STG Heat Rate Excel Import
async function saveSTGHeatRateExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/stg-extraction-lookup/import`
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

    return { success: true }
  } catch (e) {
    console.error(`Error importing STG Heat Rate Excel:`, e)
    return Promise.reject(e)
  }
}

// STG Heat Rate Excel Export
async function exportSTGHeatRateExcel(keycloak) {
  return exportExcelData(keycloak, {
    endpoint: `stg-extraction-lookup/export`,
    queryParams: {},
    fileName: `STG_Extraction_Lookup.xlsx`,
    method: 'GET',
  })
}

// HRSG Heat Rate Excel Import
async function saveHRSGHeatRateExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/hrsg-heat-rate/import`
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
        `Failed to import HRSG heat rate data: ${resp.status} ${resp.statusText}`,
      )
    }

    return { success: true }
  } catch (e) {
    console.error(`Error importing HRSG Heat Rate Excel:`, e)
    return Promise.reject(e)
  }
}

// HRSG Heat Rate Excel Export
async function exportHRSGHeatRateExcel(keycloak, assetId, financialYear, startDate = null, endDate = null) {
  // Construct endpoint with optional date range
  let endpoint = `hrsg-heat-rate/export/${assetId}/${financialYear}`

  // If both dates are provided, add them as path variables
  if (startDate && endDate) {
    endpoint = `hrsg-heat-rate/export/${assetId}/${financialYear}/${startDate}/${endDate}`
  }

  return exportExcelData(keycloak, {
    endpoint: endpoint,
    queryParams: {},
    fileName: `HRSG_Heat_Rate_${financialYear}.xlsx`,
    method: 'GET',
  })
}

// ========================|| Fuel Availability APIs ||=====================================//
async function getFuelAvailabilityData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/fuel-availability/${plantId}/${year}`
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

async function saveFuelAvailabilityData(keycloak, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/fuel-availability/${AOP_YEAR}`
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

async function saveFuelAvailabilityExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'fuel-availability/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

async function exportFuelAvailabilityExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `fuel-availability/export/${PLANT_ID}/${AOP_YEAR}`,
    queryParams: {},
    fileName: `Fuel_Availability_${AOP_YEAR}.xlsx`,
    method: 'GET',
  })
}

//========= Heat Rate Date From to
async function getConfigurationExecutionDetails(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-execution?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function executeConfiguration(executionDetailDtoList, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/configuration-execution`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(executionDetailDtoList),
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    const data = await resp.json()
    return data
  } catch (e) {
    console.error('Error saving configuration execution:', e)
    return Promise.reject(e)
  }
}
