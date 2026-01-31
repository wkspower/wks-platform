import { DataService } from 'services/DataService'

export const MonthwiseProductionPlanApiService = {
  getMonthwiseProductionPlan,
  saveMonthwiseProductionPlan,
  exportMonthwiseProductionPlan,
  calculateMonthwiseProductionPlan,
}

// ========================|| Monthwise Production Plan APIs ||=====================================//

/**
 * Get Monthwise Production Plan Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Production plan data
 */
async function getMonthwiseProductionPlan(keycloak, plantId, year) {
  try {
    const response = await DataService.getMonthwiseProductionPlan(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error fetching monthwise production plan data:', error)
    return Promise.reject(error)
  }
}

/**
 * Save Monthwise Production Plan Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} year - AOP Year
 * @param {Array} payload - Production plan data to save
 * @returns {Promise} Save response
 */
async function saveMonthwiseProductionPlan(keycloak, year, payload) {
  try {
    const response = await DataService.saveMonthwiseProductionPlan(
      keycloak,
      year,
      payload,
    )
    return response
  } catch (error) {
    console.error('Error saving monthwise production plan data:', error)
    return Promise.reject(error)
  }
}

/**
 * Export Monthwise Production Plan to Excel
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Excel file response
 */
async function exportMonthwiseProductionPlan(keycloak, plantId, year) {
  try {
    const response = await DataService.exportMonthwiseProductionPlan(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error exporting monthwise production plan data:', error)
    return Promise.reject(error)
  }
}

/**
 * Calculate Monthwise Production Plan
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Calculated data
 */
async function calculateMonthwiseProductionPlan(keycloak, plantId, year) {
  try {
    const response = await DataService.calculateMonthwiseProductionPlan(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error calculating monthwise production plan:', error)
    return Promise.reject(error)
  }
}
