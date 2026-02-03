import Config from 'consts/index'
import { json } from '../../../../services/request'

export const TcsWorkflowApiService = {
  // ============ Common/Shared APIs ============
  getWorkflowVariables,
  checkWorkflowStatus,
  triggerWorkflow,
  getPlantwiseHistory,
  getPlantDataForApproveReject,

  // ============ Plant Manager APIs ============
  saveRemark,
  getPlantManagerSubmissionHistory,

  // ============ EPS Engineer APIs ============
  epsEngineerSingleApproveReject,
  epsEngineerMultipleApproveReject,
  epsEngineerSubmission,
  getEpsEngineerSubmissionHistory,

  // ============ CTS/EPS Head APIs ============
  ctsHeadApproveReject,
  ctsHeadSubmission,
  getCtsHeadSubmissionHistory,
  getCtsHeadApproveRejectAuditTrail,

  // ============ Cluster Head APIs ============
  clusterHeadApproveReject,
  clusterHeadSubmission,
  getClusterHeadSubmissionHistory,
  getClusterHeadApproveRejectAuditTrail,
}

// ========================================================================
// ============ COMMON/SHARED APIs ============
// ========================================================================

async function getWorkflowVariables(keycloak, siteId, aopYear) {
  const url = `${Config.CaseEngineUrl}/task/variables/${siteId}/${aopYear}`
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

async function checkWorkflowStatus(keycloak, siteId, aopYear) {
  const url = `${Config.CaseEngineUrl}/task/process-exists/${siteId}/${aopYear}`
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

async function triggerWorkflow(keycloak, verticalId, siteId, aopYear) {
  const url = `${Config.CaseEngineUrl}/task/start/${verticalId}/${siteId}/${aopYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    // Backend returns plain text, not JSON
    const result = await resp.text()
    return { success: true, message: result }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getPlantwiseHistory(keycloak, plantId, siteId, verticalId) {
  const url = `${Config.CaseEngineUrl}/task/plant-submission-audit-trail-by-tab/${plantId}/${siteId}/${verticalId}`
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
    const data = await json(keycloak, resp)

    return data
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getPlantDataForApproveReject(
  keycloak,
  siteId,
  verticalId,
  year,
) {
  const url = `${Config.CaseEngineUrl}/task/ebs-approve-reject-audit-trail/${siteId}/${verticalId}`
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
    const data = await json(keycloak, resp)

    return data
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ========================================================================
// ============ PLANT MANAGER APIs ============
// ========================================================================

async function saveRemark(
  keycloak,
  plantId,
  plantName,
  siteId,
  verticalId,
  userRole,
  remark,
  aopYear,
) {
  const url = `${Config.CaseEngineUrl}/task/complete-plant-submission-task/${plantName}/${siteId}/${aopYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    plantId,
    plantName,
    siteId,
    verticalId,
    submittedBy: userRole,
    submissionRemark: remark,
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
    // Backend returns plain text, not JSON
    const result = await resp.text()
    return { success: true, message: result }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getPlantManagerSubmissionHistory(
  keycloak,
  plantId,
  siteId,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/plant-submission-audit-trail/${plantId}/${siteId}/${verticalId}`
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
    const data = await json(keycloak, resp)
    return data
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ========================================================================
// ============ EPS ENGINEER APIs ============
// ========================================================================

async function epsEngineerSingleApproveReject(
  keycloak,
  plantId,
  siteId,
  verticalId,
  approvalStatus,
  remark,
  year,
  verifiedBy,
  plantName,
) {
  const url = `${Config.CaseEngineUrl}/task/ebs-approve-reject/${plantName}/${siteId}/${approvalStatus}/${year}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    verifiedRemark: remark,
    verifiedBy,
    plantId,
    plantName,
    siteId,
    verticalId,
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
    // Backend returns plain text, not JSON
    const result = await resp.text()
    return { success: true, message: result }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function epsEngineerMultipleApproveReject(
  keycloak,
  siteId,
  approvalStatus,
  year,
  plantSubmissionList,
) {
  const url = `${Config.CaseEngineUrl}/task/bulk-ebs-approve-reject/${siteId}/${approvalStatus}/${year}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify(plantSubmissionList)
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    // Backend returns plain text, not JSON
    const result = await resp.text()
    return { success: true, message: result }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function epsEngineerSubmission(
  keycloak,
  plantName,
  siteId,
  verticalId,
  financialYear,
  remark,
  submittedBy,
) {
  const url = `${Config.CaseEngineUrl}/task/ebs-submission/${siteId}/${financialYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    siteId: siteId,
    verticalId: verticalId,
    submittedBy: submittedBy,
    submissionRemark: remark,
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
    // Backend returns plain text, not JSON
    const result = await resp.text()
    return { success: true, message: result }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getEpsEngineerSubmissionHistory(keycloak, siteId, verticalId) {
  const url = `${Config.CaseEngineUrl}/task/ebs-submission-audit-trail/${siteId}/${verticalId}`
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
    const data = await json(keycloak, resp)

    return data
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ========================================================================
// ============ CTS/EPS HEAD APIs ============
// ========================================================================

async function ctsHeadApproveReject(
  keycloak,
  siteId,
  approvalStatus,
  financialYear,
  verifiedRemark,
  verifiedBy,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/cts-approve-reject/${siteId}/${approvalStatus}/${financialYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    siteId,
    verticalId,
    verifiedRemark,
    verifiedBy,
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
    // Backend returns plain text, not JSON
    const result = await resp.text()
    return { success: true, message: result }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function ctsHeadSubmission(
  keycloak,
  siteId,
  financialYear,
  submissionRemark,
  submittedBy,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/cts-submission/${siteId}/${financialYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    siteId,
    verticalId,
    submissionRemark,
    submittedBy,
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
    // Backend returns plain text, not JSON
    const result = await resp.text()
    return { success: true, message: result }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getCtsHeadSubmissionHistory(keycloak, siteId, verticalId) {
  const url = `${Config.CaseEngineUrl}/task/cts-submission-audit-trail/${siteId}/${verticalId}`
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
    const data = await json(keycloak, resp)

    return data
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getCtsHeadApproveRejectAuditTrail(keycloak, siteId, verticalId) {
  const url = `${Config.CaseEngineUrl}/task/cts-approve-reject-audit-trail/${siteId}/${verticalId}`
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
    const data = await json(keycloak, resp)

    return data
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ========================================================================
// ============ CLUSTER HEAD APIs ============
// ========================================================================

async function clusterHeadApproveReject(
  keycloak,
  siteId,
  approvalStatus,
  financialYear,
  verifiedRemark,
  verifiedBy,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/cluster-head-approve-reject/${siteId}/${approvalStatus}/${financialYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    siteId,
    verticalId,
    verifiedRemark,
    verifiedBy,
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
    // Backend returns plain text, not JSON
    const result = await resp.text()
    return { success: true, message: result }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function clusterHeadSubmission(
  keycloak,
  siteId,
  financialYear,
  submissionRemark,
  submittedBy,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/cluster-head-submission/${siteId}/${financialYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    siteId,
    verticalId,
    submissionRemark,
    submittedBy,
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
    // Backend returns plain text, not JSON
    const result = await resp.text()
    return { success: true, message: result }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function getClusterHeadSubmissionHistory(keycloak, siteId, verticalId) {
  const url = `${Config.CaseEngineUrl}/task/cluster-head-submission-audit-trail/${siteId}/${verticalId}`
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
    const data = await json(keycloak, resp)

    return data
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getClusterHeadApproveRejectAuditTrail(
  keycloak,
  siteId,
  verticalId,
) {
  const url = `${Config.CaseEngineUrl}/task/cluster-head-approve-reject-audit-trail/${siteId}/${verticalId}`
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
    const data = await json(keycloak, resp)

    return data
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
