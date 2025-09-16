import Config from '../consts'
import { json } from './request'
export const FunctionalApiService = {
  saveAOPConsumptionNorm,
  getReliabilityPerformance,
  saveReliabilityPerformance,
  getReliabilityRecords,
  testMacro,
  saveReliabilityRecords,
}
async function saveAOPConsumptionNorm(plantId, shutdownDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/overall-consumption`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(shutdownDetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getReliabilityPerformance(keycloak, plantId, year, type) {
  const baseUrl = `${Config.CaseEngineUrl}/task/reliability-performance`
  const queryParams = new URLSearchParams({
    plantId,
    year,
    type,
  })

  const url = `${baseUrl}?${queryParams.toString()}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function testMacro(keycloak, value, plantId, year) {
  const baseUrl = `${Config.CaseEngineUrl}/task/macro`
  const queryParams = new URLSearchParams({
    value,
    plantId,
    year,
  })

  const url = `${baseUrl}?${queryParams.toString()}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getReliabilityRecords(keycloak, plantId, year, type) {
  const baseUrl = `${Config.CaseEngineUrl}/task/reliability-records`
  const queryParams = new URLSearchParams({
    plantId,
    year,
    type,
  })

  const url = `${baseUrl}?${queryParams.toString()}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function saveReliabilityPerformance(payloadData, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/reliability-performance`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payloadData),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function saveReliabilityRecords(payloadData, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/reliability-records`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payloadData),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
