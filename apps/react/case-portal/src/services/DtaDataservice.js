import Config from '../consts'
import { json } from './request'

export const DtaDataService = {
  getLineDetails,
  exportShutdownLineWise,
  ImportShutdownLineWise,
  exportSlowdownLineWise,
  ImportSlowdownLineWise,
}
export async function getLineDetails(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/line-details?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
export async function exportShutdownLineWise(
  keycloak,
  plantId,
  year,
  EXCEL_EXPORT_TITLE,
) {
  const maintenanceTypeName = 'Shutdown'
  const url = `${Config.CaseEngineUrl}/task/shutdown-export-line?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
    a.download = `${EXCEL_EXPORT_TITLE}_Shutdown Activities.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Shutdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function ImportShutdownLineWise(file, keycloak, plantId, year) {
  const maintenanceTypeName = 'Shutdown'
  const url = `${Config.CaseEngineUrl}/task/shutdown-import-line?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
    return await resp.json()
  } catch (e) {
    console.error('Error importing Shutdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function exportSlowdownLineWise(
  keycloak,
  plantId,
  year,
  EXCEL_EXPORT_TITLE,
) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/slowdown-export-line?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
    a.download = `${EXCEL_EXPORT_TITLE}_Slowdown Activities.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Slowdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function ImportSlowdownLineWise(file, keycloak, plantId, year) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/slowdown-import-line?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
    return await resp.json()
  } catch (e) {
    console.error('Error importing Slowdown Excel:', e)
    return Promise.reject(e)
  }
}
