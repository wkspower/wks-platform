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
