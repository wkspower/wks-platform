import Config from 'consts/index'
import { json } from '../../../../services/request'

export const TcsWorkflowApiService = {
  // Workflow Status APIs
  checkSubmitEligibility,
  checkWorkflowStatus,
  triggerWorkflow,
  saveRemark,
  getSubmissionHistory,
}

// ===================== || Workflow Status APIs || ===================== //

/**
 * Check if submit is eligible for the given plant/year/site/vertical
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} aopYear - AOP Year
 * @param {string} siteId - Site ID
 * @param {string} verticalId - Vertical ID
 * @returns {Promise} Response with isEligible flag and message
 */
async function checkSubmitEligibility(
  keycloak,
  plantId,
  aopYear,
  siteId,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/tcs-input/check-submit-eligibility?plantId=${plantId}&year=${aopYear}&siteId=${siteId}&verticalId=${verticalId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

/**
 * Check if workflow is already triggered
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} aopYear - AOP Year
 * @param {string} siteId - Site ID
 * @param {string} verticalId - Vertical ID
 * @returns {Promise} Response with isTriggered flag
 */
async function checkWorkflowStatus(
  keycloak,
  plantId,
  aopYear,
  siteId,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/tcs-input/check-workflow-status?plantId=${plantId}&year=${aopYear}&siteId=${siteId}&verticalId=${verticalId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

/**
 * Trigger workflow with remark
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} aopYear - AOP Year
 * @param {string} siteId - Site ID
 * @param {string} verticalId - Vertical ID
 * @param {string} remark - Remark/comment for workflow
 * @param {Object} payload - Additional payload data
 * @returns {Promise} Response with success flag and message
 */
async function triggerWorkflow(
  keycloak,
  plantId,
  aopYear,
  siteId,
  verticalId,
  remark,
  payload = {},
) {
  const url = `${Config.CaseEngineUrl}/task/tcs-input/trigger-workflow?plantId=${plantId}&year=${aopYear}&siteId=${siteId}&verticalId=${verticalId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    remark,
    ...payload,
  })
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    const result = await json(keycloak, resp)
    return result || { success: true }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

/**
 * Save remark for submission
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} aopYear - AOP Year
 * @param {string} siteId - Site ID
 * @param {string} verticalId - Vertical ID
 * @param {string} remark - Remark/comment
 * @param {Object} payload - Additional payload data
 * @returns {Promise} Response with success flag
 */
async function saveRemark(
  keycloak,
  plantId,
  aopYear,
  siteId,
  verticalId,
  remark,
  payload = {},
) {
  const url = `${Config.CaseEngineUrl}/task/tcs-input/save-remark?plantId=${plantId}&year=${aopYear}&siteId=${siteId}&verticalId=${verticalId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    remark,
    ...payload,
  })
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    const result = await json(keycloak, resp)
    return result || { success: true }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

/**
 * Get submission history
 * @param {Object} keycloak - Keycloak session object
 * @param {string} plantId - Plant ID
 * @param {string} aopYear - AOP Year
 * @param {string} siteId - Site ID
 * @param {string} verticalId - Vertical ID
 * @returns {Promise} Response with history data
 */
async function getSubmissionHistory(
  keycloak,
  plantId,
  aopYear,
  siteId,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/tcs-input/submission-history?plantId=${plantId}&year=${aopYear}&siteId=${siteId}&verticalId=${verticalId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
