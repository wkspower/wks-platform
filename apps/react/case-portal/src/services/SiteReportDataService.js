import Config from '../consts'
import { json } from './request'

export const SiteReportDataService = {
  getSiteTeamDetails,
  saveSiteTeam,
  getEnergyPerformanceDetails,
  saveEnergyPerformance,
  getPerformanceHighlightsSummary,
  savePerformanceHighlightsSummary,
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
