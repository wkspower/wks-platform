import Config from 'consts'
import { json } from '../../../../services/request'

export const HistoricPeriodBasisApiService = {
  // Configuration Execution APIs
  getConfigurationExecutionDetails,
  executeConfiguration,
  // AOP Summary APIs
  getAopSummary,
  saveAOPSummary,
}

/**
 * Historic Period Basis API Service
 * Handles all API calls related to configuration execution and AOP summary
 * Independent service for Phase 2 - no old code dependencies
 */

/**
 * Get configuration execution details
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} aopYear - AOP Year
 * @returns {Promise} Response with configuration execution details
 */
async function getConfigurationExecutionDetails(keycloak, plantId, aopYear) {
  const url = `${Config.CaseEngineUrl}/task/configuration-execution?plantId=${plantId}&year=${aopYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (error) {
    console.error('Error fetching configuration execution details:', error)
    return await Promise.reject(error)
  }
}

/**
 * Execute configuration with payload
 * @param {Array} payload - Configuration payload
 * @param {Object} keycloak - Keycloak session
 * @returns {Promise} Response from configuration execution
 */
async function executeConfiguration(payload, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/configuration-execution`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
    })

    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }

    return json(keycloak, resp)
  } catch (error) {
    console.error('Error executing configuration:', error)
    return await Promise.reject(error)
  }
}

/**
 * Get AOP summary
 * @param {Object} keycloak - Keycloak session
 * @param {string} plantId - Plant ID
 * @param {string} aopYear - AOP Year
 * @returns {Promise} Response with AOP summary
 */
async function getAopSummary(keycloak, plantId, aopYear) {
  const url = `${Config.CaseEngineUrl}/task/aop-summary?plantId=${plantId}&aopYear=${aopYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (error) {
    console.error('Error fetching AOP summary:', error)
    return await Promise.reject(error)
  }
}

/**
 * Save AOP consumption norm summary
 * @param {string} plantId - Plant ID
 * @param {string} aopYear - AOP Year
 * @param {string} summary - Summary text
 * @param {Object} keycloak - Keycloak session
 * @returns {Promise} Response from save operation
 */
async function saveAOPSummary(plantId, aopYear, summary, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/aop-summary?plantId=${plantId}&aopYear=${aopYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify({ summary }),
    })
    return json(keycloak, resp)
  } catch (error) {
    console.error('Error saving AOP summary:', error)
    return await Promise.reject(error)
  }
}
