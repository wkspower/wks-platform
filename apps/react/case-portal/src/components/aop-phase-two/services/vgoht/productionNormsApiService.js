import { DataService } from 'services/DataService'

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

  // AOP Summary APIs
  getAopSummary,
  saveSummary,

  // Configuration Execution APIs
  getConfigurationExecutionDetails,
  executeConfiguration,
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
  try {
    const response = await DataService.getProductionNormsConfiguration(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error fetching configuration data:', error)
    return Promise.reject(error)
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
  try {
    const response = await DataService.saveProductionNormsConfiguration(
      keycloak,
      year,
      payload,
    )
    return response
  } catch (error) {
    console.error('Error saving configuration data:', error)
    return Promise.reject(error)
  }
}

/**
 * Import Configuration Excel file
 * @param {File} file - Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Import response
 */
async function importConfigurationExcel(file, keycloak, plantId, year) {
  try {
    const response = await DataService.importProductionNormsConfiguration(
      file,
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error importing configuration Excel:', error)
    return Promise.reject(error)
  }
}

/**
 * Export Configuration Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Export response
 */
async function exportConfigurationExcel(keycloak, plantId, year) {
  try {
    const response = await DataService.exportProductionNormsConfiguration(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error exporting configuration Excel:', error)
    return Promise.reject(error)
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
  try {
    const response = await DataService.getProductionNormsConstants(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error fetching constants data:', error)
    return Promise.reject(error)
  }
}

/**
 * Save Production Norms Constants data
 * @param {Object} keycloak - Keycloak session
 * @param {string} year - AOP Year
 * @param {Array} payload - Data to save
 * @returns {Promise} Save response
 */
async function saveConstantsData(keycloak, year, payload) {
  try {
    const response = await DataService.saveProductionNormsConstants(
      keycloak,
      year,
      payload,
    )
    return response
  } catch (error) {
    console.error('Error saving constants data:', error)
    return Promise.reject(error)
  }
}

/**
 * Import Constants Excel file
 * @param {File} file - Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Import response
 */
async function importConstantsExcel(file, keycloak, plantId, year) {
  try {
    const response = await DataService.importProductionNormsConstants(
      file,
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error importing constants Excel:', error)
    return Promise.reject(error)
  }
}

/**
 * Export Constants Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Export response
 */
async function exportConstantsExcel(keycloak, plantId, year) {
  try {
    const response = await DataService.exportProductionNormsConstants(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error exporting constants Excel:', error)
    return Promise.reject(error)
  }
}

// ========================|| AOP Summary APIs ||=====================================//
/**
 * Get AOP Summary
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Summary text
 */
async function getAopSummary(keycloak, plantId, year) {
  try {
    const response = await DataService.getAopSummary(keycloak, plantId, year)
    if (response?.code === 200) {
      return response?.data?.summary || ''
    }
    return ''
  } catch (error) {
    console.error('Error fetching AOP summary:', error)
    return ''
  }
}

/**
 * Save AOP Summary
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @param {string} summary - Summary text
 * @param {Object} keycloak - Keycloak session
 * @returns {Promise} Save response
 */
async function saveSummary(plantId, year, summary, keycloak) {
  try {
    const response = await DataService.saveSummaryAOPConsumptionNorm(
      plantId,
      year,
      summary,
      keycloak,
    )
    return response
  } catch (error) {
    console.error('Error saving summary:', error)
    return Promise.reject(error)
  }
}

// ========================|| Configuration Execution APIs ||=====================================//
/**
 * Get Configuration Execution Details
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Execution details array
 */
async function getConfigurationExecutionDetails(keycloak, plantId, year) {
  try {
    const response = await DataService.getConfigurationExecutionDetails(
      keycloak,
      plantId,
      year,
    )
    const details = response?.data || []
    if (details.length === 0) {
      console.warn(
        'getConfigurationExecutionDetails returned an empty array:',
        response,
      )
    }
    return details
  } catch (error) {
    console.error('Error fetching configuration execution details:', error)
    return []
  }
}

/**
 * Execute Configuration
 * @param {Array} payload - Configuration payload
 * @param {Object} keycloak - Keycloak session
 * @returns {Promise} Execution response
 */
async function executeConfiguration(payload, keycloak) {
  try {
    const response = await DataService.executeConfiguration(payload, keycloak)
    return response
  } catch (error) {
    console.error('Error executing configuration:', error)
    return Promise.reject(error)
  }
}
