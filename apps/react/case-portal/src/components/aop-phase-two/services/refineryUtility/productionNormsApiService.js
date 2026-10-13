import Config from 'consts/index'

import { ImportExportApiService } from '../common/importExportApiService'
import { json } from 'services/request'

export const ProductionNormsApiService = {
  // Configuration APIs
  getConfigurationData,
  saveConfigurationData,
  importConfigurationExcel,
  exportConfigurationExcel,
  // PIMS Throughput APIs
  getPIMSThroughputData,
  savePIMSThroughputData,
  importPIMSThroughputExcel,
  exportPIMSThroughputExcel,
  // Norm Calculation API
  loadButtonNormCalculation,

  // Constants APIs
  getConstantsData,
  refinaryConstantData,
  saveConstantsData,
  importConstantsExcel,
  exportConstantsExcel,
  checkIsSummerWinterPlant,
  checkIsTwoColumnPlant: checkIsSummerWinterPlant,

  // Production Demand APIS
  getProductionDemandData,
  saveProductionDemandData,

  // Utility Consumption APIS
  getUtilityConsumptionData,
  saveUtilityConsumptionData,
  importUtilityConsumptionExcel,
  exportUtilityConsumptionExcel,

  // Cat Chem Utility Consumption APIS
  getCatChemUtilityConsumptionData,
  saveCatChemUtilityConsumptionData,
  importCatChemUtilityConsumptionExcel,
  exportCatChemUtilityConsumptionExcel,

  // Treatment Vendor APIS
  getTreatmentVendorData,
  saveTreatmentVendorData,
  deleteTreatmentVendorData,
  importTreatmentVendorExcel,
  exportTreatmentVendorExcel,

  // Commodity Selection APIs
  getCommoditySelectionData,
  saveCommoditySelectionData,

  // Owner Selection APIs
  getPlantOwnerDropdown,
  getSelectedPlantOwner,
  saveSelectedPlantOwner,
  
  // vendor consumption APIs
  getVendorConsumptionData,
  saveVendorConsumptionData,

  // commodity consumption APIs
  getCommodityConsumptionData,
  saveCommodityConsumptionData,

}

// ========================|| Configuration APIs ||=====================================//
/**
 * Get Production Norms Configuration data
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Configuration data
 */
async function getConfigurationData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/norm-basis?plantId=${plantId}&aopYear=${year}`
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

/**
 * Save Production Norms Configuration data
 * @param {Object} keycloak - Keycloak session
 * @param {string} year - AOP Year
 * @param {Array} payload - Data to save
 * @param {string} plantId - Plant ID
 * @param {string} siteId - Site ID
 * @param {string} periodFrom - Period start date
 * @param {string} periodTo - Period end date
 * @returns {Promise} Save response
 */
async function saveConfigurationData(
  keycloak,
  year,
  payload,
  plantId,
  siteId,
  periodFrom,
  periodTo,
) {
  const url = `${Config.CaseEngineUrl}/task/norm-basis?plantId=${plantId}&aopYear=${year}&siteId=${siteId}&periodFrom=${periodFrom}&periodTo=${periodTo}`
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

// ======================= IMPORT AND EXPORT
/**
 * Import Configuration Excel file
 * @param {File} file - Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Import response
 */
async function importConfigurationExcel(file, keycloak, plantId, year) {
  return ImportExportApiService.saveExcelData(
    file,
    keycloak,
    'production-norms/configuration/import',
    plantId,
    year,
  )
}

/**
 * Export Configuration Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Export response
 */
async function exportConfigurationExcel(keycloak, plantId, year) {
  return ImportExportApiService.exportExcelData(keycloak, {
    endpoint: `production-norms/configuration/export/${plantId}/${year}`,
    queryParams: {},
    fileName: `Production_Norms_Configuration_${year}.xlsx`,
    method: 'GET',
  })
}

// ===================== || GENERIC EXCEL IMPORT FUNCTION || ===================== //
/**
 * Generic function to upload Excel file to any Production Norms endpoint
 * @param {File} file - The Excel file to upload
 * @param {Object} keycloak - Keycloak session object
 * @param {string} endpoint - The API endpoint base path (e.g., 'vgoht/norms-basis/constant/import')
 * @param {Object} queryParams - Query parameters (plantFKId, year, etc.) - will be sent as form fields
 * @returns {Promise} API response
 */
async function saveExcelData(file, keycloak, endpoint, queryParams = {}) {
  const url = `${Config.CaseEngineUrl}/task/${endpoint}`

  const formData = new FormData()
  formData.append('file', file)

  // Append all query parameters as form fields
  Object.keys(queryParams).forEach((key) => {
    formData.append(key, queryParams[key])
  })

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

// ===================== || GENERIC EXCEL EXPORT FUNCTION || ===================== //
/**
 * Generic function to export Excel file from backend
 * @param {Object} keycloak - Keycloak session object
 * @param {string} endpoint - The API endpoint base path (e.g., 'vgoht/norms-basis/constant/export')
 * @param {Object} queryParams - Query parameters (plantFKId, year, etc.)
 * @param {string} fileName - Downloaded file name
 * @returns {Promise} Success/error response
 */
async function exportExcelData(keycloak, endpoint, queryParams = {}, fileName) {
  const queryString = new URLSearchParams(queryParams).toString()
  const url = queryString
    ? `${Config.CaseEngineUrl}/task/${endpoint}?${queryString}`
    : `${Config.CaseEngineUrl}/task/${endpoint}`

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })

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

// ========================|| PIMS Throughput APIs ||=====================================//
/**
 * Get PIMS Throughput data
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} PIMS Throughput data
 */
async function getPIMSThroughputData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/pims-throughput?plantId=${plantId}&aopYear=${year}`
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

/**
 * Save PIMS Throughput data
 * @param {Object} keycloak - Keycloak session
 * @param {string} year - AOP Year
 * @param {Array} payload - Data to save
 * @param {string} plantId - Plant ID
 * @param {string} siteId - Site ID
 * @param {string} periodFrom - Period start date
 * @param {string} periodTo - Period end date
 * @returns {Promise} Save response
 */
async function savePIMSThroughputData(
  keycloak,
  year,
  payload,
  plantId,
  siteId,
  periodFrom,
  periodTo,
) {
  const url = `${Config.CaseEngineUrl}/task/pims-throughput?plantId=${plantId}&aopYear=${year}&siteId=${siteId}&periodFrom=${periodFrom}&periodTo=${periodTo}`
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

/**
 * Import PIMS Throughput Excel file
 * @param {File} file - Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Import response
 */
async function importPIMSThroughputExcel(file, keycloak, plantId, year) {
  return ImportExportApiService.saveExcelData(
    file,
    keycloak,
    'production-norms/pims-throughput/import',
    plantId,
    year,
  )
}

/**
 * Export PIMS Throughput Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Export response
 */
async function exportPIMSThroughputExcel(keycloak, plantId, year, fileName) {
  return ImportExportApiService.exportExcelData(keycloak, {
    endpoint: `production-norms/pims-throughput/export/${plantId}/${year}`,
    queryParams: {},
    fileName: fileName,
    method: 'GET',
  })
}

// ========================|| Norm Calculation API ||=====================================//
/**
 * Load Button Norm Calculation
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} aopYear - AOP Year
 * @param {string} siteId - Site ID
 * @param {string} periodFrom - Period start date
 * @param {string} periodTo - Period end date
 * @returns {Promise} Norm calculation response
 */
async function loadButtonNormCalculation(
  keycloak,
  plantId,
  aopYear,
  siteId,
  periodFrom,
  periodTo,
) {
  const url = `${Config.CaseEngineUrl}/task/load-button-norm-calculation?plantId=${plantId}&aopYear=${aopYear}&siteId=${siteId}&periodFrom=${periodFrom}&periodTo=${periodTo}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
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
    const result = await json(keycloak, resp)
    return result || { success: true }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ========================|| Constants APIs ||=====================================//
/**
 * Get Production Norms Constants data
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Constants data
 */
async function getConstantsData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/vgoht/norms-basis/constant?year=${year}&plantFKId=${plantId}`
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

/**
 * Get Refinery Utility Constants data
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Constants data
 */
async function refinaryConstantData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/refinery-utility-constants?year=${year}&plantFKId=${plantId}`
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

/**
 * Check if Refinery Utility Plant requires Summer/Winter columns
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @returns {Promise} Plant column configuration
 */
async function checkIsSummerWinterPlant(keycloak, plantId) {
  const url = `${Config.CaseEngineUrl}/task/refinery-utility/check-is-summer-winter-plant?plantId=${plantId}`
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

/**
 * Save Production Norms Constants data
 * @param {Object} keycloak - Keycloak session
 * @param {string} year - AOP Year
 * @param {string} plantId - Plant ID
 * @param {string} siteId - Site ID
 * @param {string} periodFrom - Period start date
 * @param {string} periodTo - Period end date
 * @param {Array} payload - Data to save
 * @returns {Promise} Save response
 */
async function saveConstantsData(
  keycloak,
  year,
  plantId,
  siteId,
  periodFrom,
  periodTo,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/refinery-utility-constants?year=${year}&plantFKId=${plantId}&siteId=${siteId}&periodFrom=${periodFrom}&periodTo=${periodTo}`
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

/**
 * Import Constants Excel file
 * @param {File} file - Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @param {string} periodFrom - Period start date
 * @param {string} periodTo - Period end date
 * @returns {Promise} Import response
 */
async function importConstantsExcel(
  file,
  keycloak,
  plantId,
  year,
  periodFrom,
  periodTo,
) {
  return saveExcelData(file, keycloak, 'refinery-utility-constants-import', {
    year: year,
    plantId: plantId,
    periodFrom: periodFrom,
    periodTo: periodTo,
  })
}

/**
 * Export Constants Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Export response
 */
async function exportConstantsExcel(keycloak, plantId, year, fileName) {
  return exportExcelData(
    keycloak,
    'refinery-utility-constants-export',
    { year: year, plantId: plantId },
    fileName,
  )
}

async function getProductionDemandData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/vgoht/norms-basis/production-demand?year=${year}&plantFKId=${plantId}`
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

async function saveProductionDemandData(keycloak, PlantId, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/vgoht/norms-basis/production-demand?year=${AOP_YEAR}&plantFKId=${PlantId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
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

async function getUtilityConsumptionData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/utility-consumption?year=${year}&plantFKId=${plantId}`
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

async function saveUtilityConsumptionData(
  keycloak,
  PlantId,
  AOP_YEAR,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/utility-consumption?year=${AOP_YEAR}&plantFKId=${PlantId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
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

async function importUtilityConsumptionExcel(file, keycloak, plantId, year) {
  return saveExcelData(file, keycloak, 'utility-consumption-import', {
    year: year,
    plantId: plantId,
  })
}

async function exportUtilityConsumptionExcel(
  keycloak,
  plantId,
  year,
  fileName,
) {
  return exportExcelData(
    keycloak,
    'utility-consumption-export',
    { year: year, plantId: plantId },
    fileName,
  )
}

async function getCatChemUtilityConsumptionData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/cat-chem?year=${year}&plantId=${plantId}`
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

async function saveCatChemUtilityConsumptionData(
  keycloak,
  PlantId,
  AOP_YEAR,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/cat-chem?year=${AOP_YEAR}&plantId=${PlantId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
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

async function importCatChemUtilityConsumptionExcel(
  file,
  keycloak,
  plantId,
  year,
) {
  return saveExcelData(file, keycloak, 'cat-chem-import', {
    year: year,
    plantId: plantId,
  })
}

async function exportCatChemUtilityConsumptionExcel(
  keycloak,
  plantId,
  year,
  fileName,
) {
  return exportExcelData(
    keycloak,
    'cat-chem-export',
    { year: year, plantId: plantId },
    fileName,
  )
}

async function getTreatmentVendorData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/treatment-vendor?year=${year}&plantFKId=${plantId}`
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

async function saveTreatmentVendorData(keycloak, PlantId, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/treatment-vendor?year=${AOP_YEAR}&plantFKId=${PlantId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
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

async function deleteTreatmentVendorData(keycloak, normParameterFKId, year) {
  const url = `${Config.CaseEngineUrl}/task/treatment-vendor?normParameterFKId=${normParameterFKId}&year=${year}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'DELETE', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function importTreatmentVendorExcel(file, keycloak, plantId, year) {
  return saveExcelData(file, keycloak, 'treatment-vendor-import', {
    year: year,
    plantId: plantId,
  })
}

async function exportTreatmentVendorExcel(keycloak, plantId, year, fileName) {
  return exportExcelData(
    keycloak,
    'treatment-vendor-export',
    { year: year, plantId: plantId },
    fileName,
  )
}
async function getCommoditySelectionData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/commodity-chemicals?year=${year}&plantFKId=${plantId}`
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
async function saveCommoditySelectionData(
  keycloak,
  PlantId,
  AOP_YEAR,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/commodity-chemicals?year=${AOP_YEAR}&plantFKId=${PlantId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
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

// ========================|| Owner Selection APIs ||=====================================//

async function getPlantOwnerDropdown(keycloak, plantId) {
  const url = `${Config.CaseEngineUrl}/task/plant-owner-dropdown?plantFKId=${plantId}`
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

async function getSelectedPlantOwner(keycloak, plantId) {
  const url = `${Config.CaseEngineUrl}/task/selected-plant-owner?plantFKId=${plantId}`
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

async function saveSelectedPlantOwner(keycloak, plantId, payload) {
  const url = `${Config.CaseEngineUrl}/task/selected-plant-owner?plantFKId=${plantId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
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

async function getVendorConsumptionData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/treatment-vendor-consumption?year=${year}&plantFKId=${plantId}`
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
async function saveVendorConsumptionData(
  keycloak,
  PlantId,
  AOP_YEAR,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/refinary-consumption-transactions?year=${AOP_YEAR}&plantFKId=${PlantId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
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

async function getCommodityConsumptionData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/commodity-chemicals-consumption?year=${year}&plantFKId=${plantId}`
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
async function saveCommodityConsumptionData(
  keycloak,
  PlantId,
  AOP_YEAR,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/refinary-consumption-transactions?year=${AOP_YEAR}&plantFKId=${PlantId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
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
