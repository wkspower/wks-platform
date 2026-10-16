import Config from 'consts/index'

import { ImportExportApiService } from '../common/importExportApiService'
import { json } from 'services/request'

export const ProductionNormsApiService = {
  // Configuration APIs
  getConfigurationData,
  saveConfigurationData,
  importConfigurationExcel,
  exportConfigurationExcel,

  // Constants APIs
  getConstantsData,
  saveConstantsData,
  importConstantsExcel,
  exportConstantsExcel,

  // Norm Calculation API
  loadButtonNormCalculation,

  //Manual entry
  getManualEntryData,
  saveManualEntryData,
  importManualEntryExcel,
  exportManualEntryExcel,

  // Generic Import/Export helpers
  saveExcelData,
  exportExcelData,

  // PIMS Throughput APIs
  savePIMSThroughputData,
  getPIMSThroughputData,
  importPIMSThroughputExcel,
  exportPIMSThroughputExcel,
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
  const url = `${Config.CaseEngineUrl}/task/vgoht/norms-basis?year=${year}&plantFKId=${plantId}`
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
 * @returns {Promise} Save response
 */
async function saveConfigurationData(keycloak, year, payload, plantId) {
  const url = `${Config.CaseEngineUrl}/task/vgoht/norms-basis?year=${year}&plantFKId=${plantId}`
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
    'vgoht/production-norms/configuration/import',
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
    endpoint: `vgoht/production-norms/configuration/export/${plantId}/${year}`,
    queryParams: {},
    fileName: `VGOHT_Production_Norms_Configuration_${year}.xlsx`,
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
  const url = `${Config.CaseEngineUrl}/task/vgoht/norms-basis/constant?year=${year}&plantFKId=${plantId}&siteId=${siteId}&periodFrom=${periodFrom}&periodTo=${periodTo}`
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
  return saveExcelData(file, keycloak, 'vgoht/norms-basis/constant/import', {
    year: year,
    plantFKId: plantId,
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
async function exportConstantsExcel(keycloak, plantId, year) {
  return exportExcelData(
    keycloak,
    'vgoht/norms-basis/constant/export',
    { year: year, plantFKId: plantId },
    `VGOHT_Production_Norms_Constants_${year}.xlsx`,
  )
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
  const url = `${Config.CaseEngineUrl}/task/vgoht/load-button-norm-calculation?plantId=${plantId}&aopYear=${aopYear}&siteId=${siteId}&periodFrom=${periodFrom}&periodTo=${periodTo}`
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

// ========================|| Manual Entry API ||=====================================//
/**
 * Get Manual Entry data for Production Norms
 * @param {Object} keycloak - Keycloak session
 * @param {string} year - AOP Year
 * @param {string} plantFKId - Plant ID
 * @param {string} type - Configuration type
 * @param {string} version - Version (optional)
 * @returns {Promise} Manual entry data
 */
async function getManualEntry(keycloak, year, plantFKId, type, version = '') {
  const url = `${Config.CaseEngineUrl}/task/production-norms-by-type?year=${year}&plantFKId=${plantFKId}&type=${type}${version ? `&version=${version}` : ''}`
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
 * Save Manual Entry data for Production Norms
 * @param {Object} keycloak - Keycloak session
 * @param {string} year - AOP Year
 * @param {string} plantFKId - Plant ID
 * @param {Array} payload - Data to save (list of ConfigurationDTO)
 * @param {string} version - Version (optional)
 * @returns {Promise} Save response
 */
async function saveManualEntry(
  keycloak,
  year,
  plantFKId,
  payload,
  version = '',
) {
  const url = `${Config.CaseEngineUrl}/task/production-norms?year=${year}&plantFKId=${plantFKId}${version ? `&version=${version}` : ''}`
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
async function exportPIMSThroughputExcel(keycloak, plantId, year) {
  return ImportExportApiService.exportExcelData(keycloak, {
    endpoint: `production-norms/pims-throughput/export/${plantId}/${year}`,
    queryParams: {},
    fileName: `Production_Norms_PIMS_Throughput_${year}.xlsx`,
    method: 'GET',
  })
}
// ========================|| Manual Entry APIs ||=====================================//
/**
 * Get Production Norms Manual Entry data
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Manual Entry data
 */
async function getManualEntryData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/production-norms?year=${year}&plantFKId=${plantId}`
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
 * Save Production Norms Manual Entry data
 * @param {Object} keycloak - Keycloak session
 * @param {string} year - AOP Year
 * @param {string} plantId - Plant ID
 * @param {Array} payload - Data to save
 * @returns {Promise} Save response
 */
async function saveManualEntryData(keycloak, year, plantId, payload) {
  const url = `${Config.CaseEngineUrl}/task/production-norms?year=${year}&plantFKId=${plantId}`
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
 * Import Manual Entry Excel file
 * @param {File} file - Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Import response
 */
async function importManualEntryExcel(file, keycloak, plantId, year) {
  return saveExcelData(file, keycloak, 'manual-entry-import', {
    year: year,
    plantId: plantId,
  })
}

/**
 * Export Manual Entry Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Export response
 */
async function exportManualEntryExcel(keycloak, plantId, year, fileName) {
  return ImportExportApiService.exportExcelData(keycloak, {
    endpoint: 'manual-entry-export',
    queryParams: { year: year, plantId: plantId },
    fileName: fileName || `Naphthasplitter_Production_Norms_Manual_Entry_${year}.xlsx`,
    method: 'POST',
  })
}
