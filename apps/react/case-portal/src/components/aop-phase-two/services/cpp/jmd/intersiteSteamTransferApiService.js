import Config from 'consts/index'
import { json } from 'services/request'

export const IntersiteSteamTransferApiService = {
  getIntersiteSteamTransfer,
  saveIntersiteSteamTransfer,
  saveIntersiteSteamTransferExcel,
  exportIntersiteSteamTransfer,
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

// ===================== || INTERSITE STEAM TRANSFER GET || ===================== //
async function getIntersiteSteamTransfer(keycloak, financialYear) {
  const url = `${Config.CaseEngineUrl}/task/jmd/intersite-steam-transfer?financialYear=${financialYear}`
  const headers = buildHeaders(keycloak)
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

// ===================== || INTERSITE STEAM TRANSFER SAVE OR UPDATE || ===================== //
async function saveIntersiteSteamTransfer(
  keycloak,
  plantIds,
  payload,
  AOP_YEAR,
) {
  const queryParams = buildPlantIdsParam(plantIds)
  const url = `${Config.CaseEngineUrl}/task/jmd-saveOrUpdateIntersiteSteamTransfer/${AOP_YEAR}?plantIds=${queryParams}`
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
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ===================== || INTERSITE STEAM TRANSFER EXCEL IMPORT || ===================== //
async function saveIntersiteSteamTransferExcel(
  file,
  keycloak,
  plantIds,
  AOP_YEAR,
) {
  const queryParams = buildPlantIdsParam(plantIds)
  const url = `${Config.CaseEngineUrl}/task/jmd/intersite-steam-transfer/import?plantIds=${queryParams}&financialYear=${AOP_YEAR}`
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
    console.error(`Error importing Intersite Steam Transfer Excel data:`, e)
    return Promise.reject(e)
  }
}

// ===================== || INTERSITE STEAM TRANSFER EXPORT || ===================== //
async function exportIntersiteSteamTransfer(
  keycloak,
  plantIds,
  AOP_YEAR,
  fileName,
) {
  const queryParams = buildPlantIdsParam(plantIds)
  const url = `${Config.CaseEngineUrl}/task/jmd/intersite-steam-transfer/export?plantIds=${queryParams}&financialYear=${AOP_YEAR}`
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
    link.download = fileName || `Intersite_Steam_Transfer_${AOP_YEAR}.xlsx`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(downloadUrl)
  } catch (e) {
    console.error('Error exporting Intersite Steam Transfer data:', e)
    return Promise.reject(e)
  }
}
