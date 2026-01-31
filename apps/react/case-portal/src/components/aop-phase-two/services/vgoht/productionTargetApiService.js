import { DataService } from 'services/DataService'

export const ProductionTargetApiService = {
  getDesignCapacity,
  saveDesignCapacity,
  getMaxAchievedCapacity,
  saveMaxAchievedCapacity,
  getProposedOperatingCapacity,
  saveProposedOperatingCapacity,
  getPercentageSummary,
  exportProductionTarget,
  importProductionTarget,
}

// ========================|| Production Target APIs ||=====================================//

/**
 * Get Design Capacity Data
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @returns {Promise} Design capacity data
 */
async function getDesignCapacity(keycloak, plantId, year) {
  try {
    const response = await DataService.get(
      `/api/aop-phase-two/vgoht/production-target/design-capacity?plantId=${plantId}&year=${year}`,
      keycloak,
    )
    return response?.data || []
  } catch (error) {
    console.error('Error fetching design capacity data:', error)
    throw error
  }
}

/**
 * Save Design Capacity Data
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @param {Array} data - Design capacity data to save
 * @returns {Promise<Object>} Save response
 */
async function saveDesignCapacity(keycloak, plantId, year, data) {
  try {
    const payload = {
      plantId,
      year,
      data,
    }
    const response = await DataService.post(
      '/api/aop-phase-two/vgoht/production-target/design-capacity',
      payload,
      keycloak,
    )
    return response?.data
  } catch (error) {
    console.error('Error saving design capacity data:', error)
    throw error
  }
}

/**
 * Get Max Achieved Capacity Data
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @returns {Promise<Array>} Max achieved capacity data
 */
async function getMaxAchievedCapacity(keycloak, plantId, year) {
  try {
    const response = await DataService.get(
      `/api/aop-phase-two/vgoht/production-target/max-achieved-capacity?plantId=${plantId}&year=${year}`,
      keycloak,
    )
    return response?.data || []
  } catch (error) {
    console.error('Error fetching max achieved capacity data:', error)
    throw error
  }
}

/**
 * Save Max Achieved Capacity Data
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @param {Array} data - Max achieved capacity data to save
 * @returns {Promise<Object>} Save response
 */
async function saveMaxAchievedCapacity(keycloak, plantId, year, data) {
  try {
    const payload = {
      plantId,
      year,
      data,
    }
    const response = await DataService.post(
      '/api/aop-phase-two/vgoht/production-target/max-achieved-capacity',
      payload,
      keycloak,
    )
    return response?.data
  } catch (error) {
    console.error('Error saving max achieved capacity data:', error)
    throw error
  }
}

/**
 * Get Proposed Operating Capacity Data
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @returns {Promise<Array>} Proposed operating capacity data
 */
async function getProposedOperatingCapacity(keycloak, plantId, year) {
  try {
    const response = await DataService.get(
      `/api/aop-phase-two/vgoht/production-target/proposed-operating-capacity?plantId=${plantId}&year=${year}`,
      keycloak,
    )
    return response?.data || []
  } catch (error) {
    console.error('Error fetching proposed operating capacity data:', error)
    throw error
  }
}

/**
 * Save Proposed Operating Capacity Data
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @param {Array} data - Proposed operating capacity data to save
 * @returns {Promise<Object>} Save response
 */
async function saveProposedOperatingCapacity(keycloak, plantId, year, data) {
  try {
    const payload = {
      plantId,
      year,
      data,
    }
    const response = await DataService.post(
      '/api/aop-phase-two/vgoht/production-target/proposed-operating-capacity',
      payload,
      keycloak,
    )
    return response?.data
  } catch (error) {
    console.error('Error saving proposed operating capacity data:', error)
    throw error
  }
}

/**
 * Get Percentage Summary Data
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @returns {Promise<Array>} Percentage summary data
 */
async function getPercentageSummary(keycloak, plantId, year) {
  try {
    const response = await DataService.get(
      `/api/aop-phase-two/vgoht/production-target/percentage-summary?plantId=${plantId}&year=${year}`,
      keycloak,
    )
    return response?.data || []
  } catch (error) {
    console.error('Error fetching percentage summary data:', error)
    throw error
  }
}

/**
 * Export Production Target Data to Excel
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @param {String} type - Type of data to export (design-capacity, max-achieved, proposed-operating, percentage-summary)
 * @returns {Promise<Blob>} Excel file blob
 */
async function exportProductionTarget(keycloak, plantId, year, type) {
  try {
    const response = await DataService.get(
      `/api/aop-phase-two/vgoht/production-target/${type}/export?plantId=${plantId}&year=${year}`,
      keycloak,
      { responseType: 'blob' },
    )
    return response?.data
  } catch (error) {
    console.error(`Error exporting ${type} data:`, error)
    throw error
  }
}

/**
 * Import Production Target Data from Excel
 * @param {Object} keycloak - Keycloak session object
 * @param {Number} plantId - Plant ID
 * @param {Number} year - AOP Year
 * @param {File} file - Excel file to import
 * @param {String} type - Type of data to import (proposed-operating)
 * @returns {Promise<Array>} Imported data
 */
async function importProductionTarget(keycloak, plantId, year, file, type) {
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('plantId', plantId)
    formData.append('year', year)

    const response = await DataService.post(
      `/api/aop-phase-two/vgoht/production-target/${type}/import`,
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
    console.error(`Error importing ${type} data:`, error)
    throw error
  }
}
