import Config from '../consts'
import { json } from './request'
export const MaintenanceDetailsApiService = {
  getCrackerMaintenanceData,
  getCrackerMaintenanceDataNMD,
  getMaintenanceData,
  saveCrackerMaintenance,
  saveCrackerMaintenanceNMD,
  handleCalculateMaintenance,
  handleCalculateMaintenanceCracker,
  CrackerMaintenanceImport,
  CrackerMaintenanceImportNMD,
  CrackerMaintenanceExport,
  CrackerMaintenanceExportNMD,
  deleteShutdownConfig,
  saveShutdownConfig,
  getShutdownConfig,
  getFinishingShutdownConfig,
  getCrackerDownsteamShutdownDMD,
  getSdDaysValues,
  getSlowdownConfig,
  deleteSlowdownConfig,
  saveSlowdownConfig,
  saveFinishingShutdown,
  deleteFinishingShutdownConfig,
}

async function getCrackerMaintenanceData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function getCrackerMaintenanceDataNMD(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-nmd?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function saveCrackerMaintenance(
  PLANT_ID,
  AOP_YEAR,
  decokePlanningDTOList,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/maintenance?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(decokePlanningDTOList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function saveCrackerMaintenanceNMD(
  PLANT_ID,
  AOP_YEAR,
  decokePlanningDTOList,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-nmd?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(decokePlanningDTOList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getMaintenanceData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-details?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function handleCalculateMaintenance(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/handleCalculateMaintenance?year=${AOP_YEAR}&plantId=${PLANT_ID}`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'GET',
      headers,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    const data = await resp.json() // Parse JSON response
    return data
  } catch (e) {
    console.error('Error fetching calculation data:', e)
    return Promise.reject(e)
  }
}
async function handleCalculateMaintenanceCracker(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/calculate-decoke-maintenance?year=${AOP_YEAR}&plantId=${PLANT_ID}`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'GET',
      headers,
    })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    const data = await resp.json()
    return data
  } catch (e) {
    console.error('Error fetching calculation data:', e)
    return Promise.reject(e)
  }
}
async function CrackerMaintenanceImport(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-import?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
    return json(keycloak, resp) // assuming `json()` handles response properly
  } catch (e) {
    console.error('Error importing Optimizer Input Excel:', e)
    return await Promise.reject(e)
  }
}

async function CrackerMaintenanceImportNMD(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-import-nmd?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
    return json(keycloak, resp) // assuming `json()` handles response properly
  } catch (e) {
    console.error('Error importing Optimizer Input Excel:', e)
    return await Promise.reject(e)
  }
}

async function CrackerMaintenanceExport(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_NAME,
) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-export?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'GET',
      headers,
    })

    if (!resp.ok) {
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = `${EXCEL_NAME}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}

async function CrackerMaintenanceExportNMD(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_NAME,
) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-export-nmd?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'GET',
      headers,
    })

    if (!resp.ok) {
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = `${EXCEL_NAME}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}
async function deleteShutdownConfig(id, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-history?id=${encodeURIComponent(id)}`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers,
    })
    if (!resp.ok) {
      throw new Error(
        `Failed to delete data: ${resp.status} ${resp.statusText}`,
      )
    }
    return await resp.json()
  } catch (e) {
    console.error('Error deleting slowdown data:', e)
    return Promise.reject(e)
  }
}
async function saveShutdownConfig(PLANT_ID, AOP_YEAR, dataList, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-history?plantFKId=${encodeURIComponent(PLANT_ID)}&year=${encodeURIComponent(AOP_YEAR)}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(dataList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error in saveAnnualProduction:', e)
    return await Promise.reject(e)
  }
}
async function getShutdownConfig(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-history?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function getCrackerDownsteamShutdownDMD(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-other-plants?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function getSdDaysValues(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/type-of-sd?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function deleteSlowdownConfig(id, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/slowdown-history?id=${encodeURIComponent(id)}`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers,
    })
    if (!resp.ok) {
      throw new Error(
        `Failed to delete data: ${resp.status} ${resp.statusText}`,
      )
    }
    return await resp.json()
  } catch (e) {
    console.error('Error deleting slowdown data:', e)
    return Promise.reject(e)
  }
}
async function saveSlowdownConfig(PLANT_ID, AOP_YEAR, dataList, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/slowdown-history?plantFKId=${encodeURIComponent(PLANT_ID)}&year=${encodeURIComponent(AOP_YEAR)}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(dataList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error in saveAnnualProduction:', e)
    return await Promise.reject(e)
  }
}
async function getSlowdownConfig(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/slowdown-history?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function getFinishingShutdownConfig(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/finishing-shutdown?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function saveFinishingShutdown(SITE_ID, AOP_YEAR, dataList, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/finishing-shutdown?siteId=${encodeURIComponent(SITE_ID)}&year=${encodeURIComponent(AOP_YEAR)}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(dataList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error in saveAnnualProduction:', e)
    return await Promise.reject(e)
  }
}
async function deleteFinishingShutdownConfig(id, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/finishing-shutdown?id=${encodeURIComponent(id)}`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers,
    })
    if (!resp.ok) {
      throw new Error(
        `Failed to delete data: ${resp.status} ${resp.statusText}`,
      )
    }
    return await resp.json()
  } catch (e) {
    console.error('Error deleting finishing shutdown config data:', e)
    return Promise.reject(e)
  }
}
