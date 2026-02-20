import { DataService } from 'services/DataService'

export const SteadyStateConsumptionApiService = {
  getSteadyStateConsumption,
  saveSteadyStateConsumption,
  exportSteadyStateConsumption,
  importSteadyStateConsumption,
  calculateSteadyStateConsumption,
}

// ========================|| Steady State Consumption APIs ||=====================================//

/**
 * Get Steady State Consumption Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Steady state consumption data
 */
async function getSteadyStateConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.get(
      `/api/aop-phase-two/vgoht/steady-state-consumption?plantId=${plantId}&year=${year}`,
      keycloak,
    )
    return response?.data || []
  } catch (error) {
    console.error('Error fetching steady state consumption data:', error)
    throw error
  }
}

/**
 * Save Steady State Consumption Data
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @param {Array} data - Steady state consumption data to save
 * @returns {Promise<Object>} Save response
 */
async function saveSteadyStateConsumption(keycloak, plantId, year, data) {
  try {
    const payload = {
      plantId,
      year,
      data,
    }
    const response = await DataService.post(
      '/api/aop-phase-two/vgoht/steady-state-consumption',
      payload,
      keycloak,
    )
    return response?.data
  } catch (error) {
    console.error('Error saving steady state consumption data:', error)
    throw error
  }
}

/**
 * Export Steady State Consumption to Excel
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @returns {Promise<Blob>} Excel file blob
 */
async function exportSteadyStateConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.get(
      `/api/aop-phase-two/vgoht/steady-state-consumption/export?plantId=${plantId}&year=${year}`,
      keycloak,
      { responseType: 'blob' },
    )
    return response?.data
  } catch (error) {
    console.error('Error exporting steady state consumption data:', error)
    throw error
  }
}

/**
 * Import Steady State Consumption from Excel
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @param {File} file - Excel file to import
 * @returns {Promise<Array>} Imported data
 */
async function importSteadyStateConsumption(keycloak, plantId, year, file) {
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('plantId', plantId)
    formData.append('year', year)

    const response = await DataService.post(
      '/api/aop-phase-two/vgoht/steady-state-consumption/import',
      formData,
      keycloak,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      },
    )
    return response?.data || []
  } catch (error) {
    console.error('Error importing steady state consumption data:', error)
    throw error
  }
}

/**
 * Calculate Steady State Consumption
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @returns {Promise<Array>} Calculated data
 */
async function calculateSteadyStateConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.post(
      '/api/aop-phase-two/vgoht/steady-state-consumption/calculate',
      { plantId, year },
      keycloak,
    )
    return response?.data || []
  } catch (error) {
    console.error('Error calculating steady state consumption:', error)
    throw error
  }
}
