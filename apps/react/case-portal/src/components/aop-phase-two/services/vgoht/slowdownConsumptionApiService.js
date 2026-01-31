import { DataService } from 'services/DataService'

export const SlowdownConsumptionApiService = {
  getSlowdownConsumption,
  saveSlowdownConsumption,
  exportSlowdownConsumption,
}

// ========================|| Slowdown Consumption APIs ||=====================================//

/**
 * Get Slowdown Consumption Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Slowdown consumption data
 */
async function getSlowdownConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.getSlowdownConsumption(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error fetching slowdown consumption data:', error)
    return Promise.reject(error)
  }
}

/**
 * Save Slowdown Consumption Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} year - AOP Year
 * @param {Array} payload - Slowdown consumption data to save
 * @returns {Promise} Save response
 */
async function saveSlowdownConsumption(keycloak, year, payload) {
  try {
    const response = await DataService.saveSlowdownConsumption(
      keycloak,
      year,
      payload,
    )
    return response
  } catch (error) {
    console.error('Error saving slowdown consumption data:', error)
    return Promise.reject(error)
  }
}

/**
 * Export Slowdown Consumption to Excel
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Excel file response
 */
async function exportSlowdownConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.exportSlowdownConsumption(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error exporting slowdown consumption data:', error)
    return Promise.reject(error)
  }
}
