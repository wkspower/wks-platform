import { DataService } from 'services/DataService'

export const SlowdownActivitiesApiService = {
  // Slowdown Activities APIs
  getSlowdownActivities,
  saveSlowdownActivities,
  deleteSlowdownActivity,
  importSlowdownActivities,
  exportSlowdownActivities,
}

// ========================|| Slowdown Activities APIs ||=====================================//

/**
 * Get Slowdown Activities data
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Slowdown activities data
 */
async function getSlowdownActivities(keycloak, plantId, year) {
  try {
    const response = await DataService.getSlowdownActivities(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error fetching slowdown activities:', error)
    return Promise.reject(error)
  }
}

/**
 * Save Slowdown Activities data
 * @param {Object} keycloak - Keycloak session
 * @param {string} year - AOP Year
 * @param {Array} payload - Slowdown activities data
 * @returns {Promise} Save response
 */
async function saveSlowdownActivities(keycloak, year, payload) {
  try {
    const response = await DataService.saveSlowdownActivities(
      keycloak,
      year,
      payload,
    )
    return response
  } catch (error) {
    console.error('Error saving slowdown activities:', error)
    return Promise.reject(error)
  }
}

/**
 * Delete a Slowdown Activity
 * @param {Object} keycloak - Keycloak session
 * @param {string} id - Activity ID
 * @returns {Promise} Delete response
 */
async function deleteSlowdownActivity(keycloak, id) {
  try {
    const response = await DataService.deleteSlowdownActivity(keycloak, id)
    return response
  } catch (error) {
    console.error('Error deleting slowdown activity:', error)
    return Promise.reject(error)
  }
}

/**
 * Import Slowdown Activities from Excel
 * @param {File} file - Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Import response
 */
async function importSlowdownActivities(file, keycloak, plantId, year) {
  try {
    const response = await DataService.importSlowdownActivities(
      file,
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error importing slowdown activities:', error)
    return Promise.reject(error)
  }
}

/**
 * Export Slowdown Activities to Excel
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Export response
 */
async function exportSlowdownActivities(keycloak, plantId, year) {
  try {
    const response = await DataService.exportSlowdownActivities(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error exporting slowdown activities:', error)
    return Promise.reject(error)
  }
}
