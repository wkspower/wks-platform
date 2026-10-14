import Config from 'consts/index'
import { json } from 'services/request'

export const SignOffReportApiService = {
  getSignOffReport,
}

// ========================|| Sign Off Report APIs ||=====================================//

/**
 * Get Sign Off Report Data (Dynamic columns and grids from Stored Procedure)
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant UUID
 * @param {string} year - AOP Year (e.g. '2026-27')
 * @param {string} [periodFrom] - Optional Period From date (YYYY-MM-DD)
 * @param {string} [periodTo] - Optional Period To date (YYYY-MM-DD)
 * @returns {Promise} Response object with AOPMessageVM structure (code, message, data: Array<Grid>)
 */
async function getSignOffReport(keycloak, plantId, year, periodFrom, periodTo) {
  const queryParams = new URLSearchParams()

  if (plantId) {
    queryParams.append('plantId', plantId)
  }
  if (year) {
    queryParams.append('year', year)
  }
  if (periodFrom) {
    queryParams.append('periodFrom', periodFrom)
  }
  if (periodTo) {
    queryParams.append('periodTo', periodTo)
  }

  const url = `${Config.CaseEngineUrl}/task/sign-off-report?${queryParams.toString()}`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (error) {
    console.error('Error in SignOffReportApiService.getSignOffReport:', error)
    return await Promise.reject(error)
  }
}
