import { DataService } from 'services/DataService'

export const ShutdownConsumptionApiService = {
  getShutdownConsumption,
  saveShutdownConsumption,
  exportShutdownConsumption,
}

// ========================|| Shutdown Consumption APIs ||=====================================//

/**
 * Get Shutdown Consumption Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Shutdown consumption data
 */
async function getShutdownConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.getShutdownConsumption(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error fetching shutdown consumption data:', error)
    return Promise.reject(error)
  }
}

/**
 * Save Shutdown Consumption Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} year - AOP Year
 * @param {Array} payload - Shutdown consumption data to save
 * @returns {Promise} Save response
 */
async function saveShutdownConsumption(keycloak, year, payload) {
  try {
    const response = await DataService.saveShutdownConsumption(
      keycloak,
      year,
      payload,
    )
    return response
  } catch (error) {
    console.error('Error saving shutdown consumption data:', error)
    return Promise.reject(error)
  }
}

/**
 * Export Shutdown Consumption to Excel
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Excel file response
 */
async function exportShutdownConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.exportShutdownConsumption(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error exporting shutdown consumption data:', error)
    return Promise.reject(error)
  }
}
