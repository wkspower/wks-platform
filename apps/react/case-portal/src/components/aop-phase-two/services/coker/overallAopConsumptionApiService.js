import { DataService } from 'services/DataService'

export const OverallAopConsumptionApiService = {
  getOverallAopConsumption,
  saveOverallAopConsumption,
  exportOverallAopConsumption,
  calculateOverallAopConsumption,
}

// ========================|| Overall AOP Consumption APIs ||=====================================//

/**
 * API Service for Overall AOP Consumption
 * Handles GET and POST operations for overall AOP consumption data
 */

/**
 * Get Overall AOP Consumption Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Overall AOP consumption data
 */
async function getOverallAopConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.getOverallAopConsumption(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error fetching overall AOP consumption data:', error)
    return Promise.reject(error)
  }
}

/**
 * Save Overall AOP Consumption Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} year - AOP Year
 * @param {Array} payload - Overall AOP consumption data to save
 * @returns {Promise} Save response
 */
async function saveOverallAopConsumption(keycloak, year, payload) {
  try {
    const response = await DataService.saveOverallAopConsumption(
      keycloak,
      year,
      payload,
    )
    return response
  } catch (error) {
    console.error('Error saving overall AOP consumption data:', error)
    return Promise.reject(error)
  }
}

/**
 * Export Overall AOP Consumption to Excel
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Excel file response
 */
async function exportOverallAopConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.exportOverallAopConsumption(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error exporting overall AOP consumption data:', error)
    return Promise.reject(error)
  }
}

/**
 * Calculate Overall AOP Consumption
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Calculated data
 */
async function calculateOverallAopConsumption(keycloak, plantId, year) {
  try {
    const response = await DataService.calculateOverallAopConsumption(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error calculating overall AOP consumption:', error)
    return Promise.reject(error)
  }
}
