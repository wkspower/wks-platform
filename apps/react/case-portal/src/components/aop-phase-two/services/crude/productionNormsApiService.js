import Config from 'consts/index'

import { ImportExportApiService } from '../common/importExportApiService'
import { json } from 'services/request'

export const ProductionNormsApiService = {
  // Configuration APIs
  getConfigurationData,
  saveConfigurationData,
  importConfigurationExcel,
  exportConfigurationExcel,
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
 * @returns {Promise} Save response
 */
async function saveConfigurationData(keycloak, year, payload) {
  const url = `${Config.CaseEngineUrl}/task/norm-basis`
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
