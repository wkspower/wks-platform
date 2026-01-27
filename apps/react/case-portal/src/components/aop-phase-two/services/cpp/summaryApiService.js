import Config from 'consts/index'
import { json } from '../../../../services/request'

export const SummaryApiService = {
  getCppModelLogs,
  getMonthlyExecutionDetails,
  getAssetStatusDetails,
}

// ===================== || CPP Model Logs APIs || ===================== //
async function getCppModelLogs(keycloak) {
  const url = `${Config.CaseEngineUrl}/task/cpp-model-logs`
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

async function getMonthlyExecutionDetails(keycloak, executionId) {
  const url = `${Config.CaseEngineUrl}/task/cpp-model-logs/${executionId}/months`
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

async function getAssetStatusDetails(keycloak, executionId, month) {
  const url = `${Config.CaseEngineUrl}/task/cpp-model-logs/${executionId}/month/${month}`
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
