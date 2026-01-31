import Config from 'consts/index'
import { json } from '../../../../services/request'

export const TabAccessApiService = {
  // Tab Configuration APIs
  getConfigurationTabsMatrix,
  getConfigurationAvailableTabs,
}

// ===================== || Tab Configuration APIs || ===================== //

/**
 * Get Configuration Tabs Matrix
 * Returns which tabs should be displayed based on plant/site/vertical configuration
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} year - AOP Year
 * @param {string} siteId - Site ID
 * @param {string} verticalId - Vertical ID
 * @returns {Promise} Configuration tabs matrix data
 */
async function getConfigurationTabsMatrix(
  keycloak,
  plantId,
  year,
  siteId,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/access/matrix`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  const params = new URLSearchParams({
    plantId: plantId,
    auditYear: year,
    siteId: siteId,
    verticalId: verticalId,
  })

  try {
    const resp = await fetch(`${url}?${params}`, {
      method: 'GET',
      headers: headers,
    })

    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }

    return json(keycloak, resp)
  } catch (error) {
    console.error('Error fetching configuration tabs matrix:', error)
    return await Promise.reject(error)
  }
}

/**
 * Get Configuration Available Tabs
 * Returns all possible configuration tabs with their metadata (id, displayName, etc.)
 * @param {Object} keycloak - Keycloak session
 * @returns {Promise} Available configuration types list
 */
async function getConfigurationAvailableTabs(keycloak) {
  const url = `${Config.CaseEngineUrl}/task/configuration-type-data`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'GET',
      headers: headers,
    })

    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }

    return json(keycloak, resp)
  } catch (error) {
    console.error('Error fetching configuration available tabs:', error)
    return await Promise.reject(error)
  }
}

export default TabAccessApiService
