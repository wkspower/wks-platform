import Config from 'consts/index'
import { json } from 'services/request'

export const EfficiencyAndFuelRatioAPIService = {
  getEfficiency,
  saveEfficiency,
  exportEfficiency,
  importEfficiency,
  getFuelRatio,
  saveFuelRatio,
}

// ===================== GENERIC HELPERS ===================== //

function buildHeaders(keycloak) {
  return {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
}

function buildPlantIdsParam(plantIds) {
  const plantIdArray = Array.isArray(plantIds) ? plantIds : [plantIds]
  return plantIdArray.join(',')
}

// ===================== || EFFICIENCY GET || ===================== //
// GET /task/jmd/efficiency?plantIds=...&aopYear=...
async function getEfficiency(keycloak, plantIds, aopYear) {
  const queryParams = buildPlantIdsParam(plantIds)
  const url = `${Config.CaseEngineUrl}/task/jmd/efficiency?plantIds=${queryParams}&aopYear=${aopYear}`
  const headers = buildHeaders(keycloak)
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error fetching efficiency data:', e)
    return await Promise.reject(e)
  }
}

// ===================== || EFFICIENCY SAVE OR UPDATE || ===================== //
// POST /task/jmd/efficiency?plantIds=...&aopYear=...
async function saveEfficiency(keycloak, plantIds, aopYear, payload) {
  const queryParams = buildPlantIdsParam(plantIds)
  const url = `${Config.CaseEngineUrl}/task/jmd/efficiency?plantIds=${queryParams}&aopYear=${aopYear}`
  const headers = buildHeaders(keycloak)
  const body = JSON.stringify(payload)
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
    console.error('Error saving efficiency data:', e)
    return await Promise.reject(e)
  }
}

// ===================== || EFFICIENCY EXPORT || ===================== //
// GET /task/jmd/efficiency/export?plantIds=...&aopYear=...
async function exportEfficiency(keycloak, plantIds, aopYear, fileName) {
  const queryParams = buildPlantIdsParam(plantIds)
  const url = `${Config.CaseEngineUrl}/task/jmd/efficiency/export?plantIds=${queryParams}&aopYear=${aopYear}`
  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(
        `Failed to export Excel: ${resp.status} ${resp.statusText}`,
      )
    }
    const blob = await resp.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName || `Efficiency_${aopYear}.xlsx`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(downloadUrl)
  } catch (e) {
    console.error('Error exporting efficiency data:', e)
    return Promise.reject(e)
  }
}

// ===================== || EFFICIENCY IMPORT || ===================== //
// POST /task/jmd/efficiency/import?plantIds=...&aopYear=...
async function importEfficiency(file, keycloak, plantIds, aopYear) {
  const queryParams = buildPlantIdsParam(plantIds)
  const url = `${Config.CaseEngineUrl}/task/jmd/efficiency/import?plantIds=${queryParams}&aopYear=${aopYear}`
  const formData = new FormData()
  formData.append('file', file)
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: formData,
    })
    if (!resp.ok) {
      throw new Error(
        `Failed to import data: ${resp.status} ${resp.statusText}`,
      )
    }
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error importing efficiency Excel data:', e)
    return Promise.reject(e)
  }
}

// ===================== || FUEL RATIO GET || ===================== //
// GET /task/jmd/fuel-ratio?plantIds=...&aopYear=...
async function getFuelRatio(keycloak, plantIds, aopYear) {
  const queryParams = buildPlantIdsParam(plantIds)
  const url = `${Config.CaseEngineUrl}/task/jmd/fuel-ratio?plantIds=${queryParams}&aopYear=${aopYear}`
  const headers = buildHeaders(keycloak)
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error fetching fuel ratio data:', e)
    return await Promise.reject(e)
  }
}

// ===================== || FUEL RATIO SAVE OR UPDATE || ===================== //
// POST /task/jmd/fuel-ratio?plantIds=...&aopYear=...
async function saveFuelRatio(keycloak, plantIds, aopYear, payload) {
  const queryParams = buildPlantIdsParam(plantIds)
  const url = `${Config.CaseEngineUrl}/task/jmd/fuel-ratio?plantIds=${queryParams}&aopYear=${aopYear}`
  const headers = buildHeaders(keycloak)
  const body = JSON.stringify(payload)
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
    console.error('Error saving fuel ratio data:', e)
    return await Promise.reject(e)
  }
}
