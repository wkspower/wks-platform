import { DataService } from 'services/DataService'

export const ShutdownActivitiesApiService = {
  // Shutdown Activities APIs
  getShutdownActivities,
  saveShutdownActivities,
  deleteShutdownActivity,
  importShutdownActivities,
  exportShutdownActivities,
}

// ========================|| Shutdown Activities APIs ||=====================================//

/**
 * Get Shutdown Activities data
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Shutdown activities data
 */
async function getShutdownActivities(keycloak, plantId, year) {
  try {
    const response = await DataService.getShutdownActivities(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error fetching shutdown activities:', error)
    return Promise.reject(error)
  }
}

/**
 * Save Shutdown Activities data
 * @param {Object} keycloak - Keycloak session
 * @param {string} year - AOP Year
 * @param {Array} payload - Shutdown activities data
 * @returns {Promise} Save response
 */
async function saveShutdownActivities(keycloak, year, payload) {
  try {
    const response = await DataService.saveShutdownActivities(
      keycloak,
      year,
      payload,
    )
    return response
  } catch (error) {
    console.error('Error saving shutdown activities:', error)
    return Promise.reject(error)
  }
}

/**
 * Delete a Shutdown Activity
 * @param {Object} keycloak - Keycloak session
 * @param {string} id - Activity ID
 * @returns {Promise} Delete response
 */
async function deleteShutdownActivity(keycloak, id) {
  try {
    const response = await DataService.deleteShutdownActivity(keycloak, id)
    return response
  } catch (error) {
    console.error('Error deleting shutdown activity:', error)
    return Promise.reject(error)
  }
}

/**
 * Import Shutdown Activities from Excel
 * @param {File} file - Excel file
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Import response
 */
async function importShutdownActivities(file, keycloak, plantId, year) {
  try {
    const response = await DataService.importShutdownActivities(
      file,
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error importing shutdown activities:', error)
    return Promise.reject(error)
  }
}

/**
 * Export Shutdown Activities to Excel
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Export response
 */
async function exportShutdownActivities(keycloak, plantId, year) {
  try {
    const response = await DataService.exportShutdownActivities(
      keycloak,
      plantId,
      year,
    )
    return response
  } catch (error) {
    console.error('Error exporting shutdown activities:', error)
    return Promise.reject(error)
  }
}
