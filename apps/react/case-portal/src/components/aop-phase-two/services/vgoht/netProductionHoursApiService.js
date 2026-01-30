import { DataService } from 'services/DataService'

export const NetProductionHoursApiService = {
  getNetProductionHours,
  saveNetProductionHours,
  exportNetProductionHours,
}

// ========================|| Net Production Hours APIs ||=====================================//

/**
 * Get Net Production Hours Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Net production hours data
 */
async function getNetProductionHours(keycloak, plantId, year) {
  try {
    const response = await DataService.getNetProductionHours(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error fetching net production hours data:', error)
    return Promise.reject(error)
  }
}

/**
 * Save Net Production Hours Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} year - AOP Year
 * @param {Array} payload - Net production hours data to save
 * @returns {Promise} Save response
 */
async function saveNetProductionHours(keycloak, year, payload) {
  try {
    const response = await DataService.saveNetProductionHours(
      keycloak,
      year,
      payload,
    )
    return response
  } catch (error) {
    console.error('Error saving net production hours data:', error)
    return Promise.reject(error)
  }
}

/**
 * Export Net Production Hours to Excel
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Excel file response
 */
async function exportNetProductionHours(keycloak, plantId, year) {
  try {
    const response = await DataService.exportNetProductionHours(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error exporting net production hours data:', error)
    return Promise.reject(error)
  }
}
