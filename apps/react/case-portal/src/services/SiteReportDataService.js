import Config from '../consts'
import { json } from './request'

export const SiteReportDataService = {
  getSiteTeamDetails,
  saveSiteTeam,
  getEnergyPerformanceDetails,
  saveEnergyPerformance,
  getPerformanceHighlightsSummary,
  savePerformanceHighlightsSummary,
  getSlowdownPlan,
  saveSlowdownPlan,
  getFixedExpensesData,
  saveFixedExpensesData,
  getCapexData,
  saveCapexData,
  getTechnicalAvailability,
  saveTechnicalAvailability,
}
export async function getSiteTeamDetails(keycloak, SITE_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/site-team-transaction?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return await resp.json()
  } catch (e) {
    console.error('Error fetching Plant Team data:', e)
    return Promise.reject(e)
  }
}
export async function saveSiteTeam(keycloak, SITE_ID, AOP_YEAR, data) {
  const url = `${Config.CaseEngineUrl}/task/site-team-transaction?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    })
    return await resp.json()
  } catch (e) {
    console.error('Error saving Site Team data:', e)
    return Promise.reject(e)
  }
}
export async function getEnergyPerformanceDetails(keycloak, SITE_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/energy-performance?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return await resp.json()
  } catch (e) {
    console.error('Error fetching Energy Performance data:', e)
    return Promise.reject(e)
  }
}
export async function saveEnergyPerformance(keycloak, SITE_ID, AOP_YEAR, data) {
  const url = `${Config.CaseEngineUrl}/task/energy-performance?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    })
    return await resp.json()
  } catch (e) {
    console.error('Error saving Energy Performance data:', e)
    return Promise.reject(e)
  }
}
export async function getPerformanceHighlightsSummary(
  keycloak,
  SITE_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/performance-highlights?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return await resp.json()
  } catch (e) {
    console.error('Error fetching Performance Highlights Summary data:', e)
    return Promise.reject(e)
  }
}

export async function savePerformanceHighlightsSummary(
  keycloak,
  SITE_ID,
  AOP_YEAR,
  data,
) {
  const url = `${Config.CaseEngineUrl}/task/performance-highlights?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    })
    return await resp.json()
  } catch (e) {
    console.error('Error saving Performance Highlights Summary data:', e)
    return Promise.reject(e)
  }
}
export async function getSlowdownPlan(keycloak, SITE_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-slowdown-plan?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return await resp.json()
  } catch (e) {
    console.error('Error fetching Shutdown / Slowdown Plan data:', e)
    return Promise.reject(e)
  }
}
export async function saveSlowdownPlan(keycloak, SITE_ID, AOP_YEAR, data) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-slowdown-plan?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    })
    return await resp.json()
  } catch (e) {
    console.error('Error saving Shutdown / Slowdown Plan data:', e)
    return Promise.reject(e)
  }
}
export async function getFixedExpensesData(keycloak, SITE_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/report-fixed-expenses?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return await resp.json()
  } catch (e) {
    console.error('Error fetching Fixed Expenses data:', e)
    return Promise.reject(e)
  }
}
export async function saveFixedExpensesData(keycloak, SITE_ID, AOP_YEAR, data) {
  const url = `${Config.CaseEngineUrl}/task/report-fixed-expenses?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    })
    return await resp.json()
  } catch (e) {
    console.error('Error saving Fixed Expenses data:', e)
    return Promise.reject(e)
  }
}
export async function getCapexData(keycloak, SITE_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/report-capex-pioplan?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return await resp.json()
  } catch (e) {
    console.error('Error fetching Capex data:', e)
    return Promise.reject(e)
  }
}
export async function saveCapexData(keycloak, SITE_ID, AOP_YEAR, data) {
  const url = `${Config.CaseEngineUrl}/task/report-capex-pioplan?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    })
    return await resp.json()
  } catch (e) {
    console.error('Error saving Capex data:', e)
    return Promise.reject(e)
  }
}
export async function getTechnicalAvailability(keycloak, SITE_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/technical-availability?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return await resp.json()
  } catch (e) {
    console.error('Error fetching Technical Availability data:', e)
    return Promise.reject(e)
  }
}
export async function saveTechnicalAvailability(
  keycloak,
  SITE_ID,
  AOP_YEAR,
  data,
) {
  const url = `${Config.CaseEngineUrl}/task/technical-availability?siteId=${SITE_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    })
    return await resp.json()
  } catch (e) {
    console.error('Error saving Technical Availability data:', e)
    return Promise.reject(e)
  }
}
