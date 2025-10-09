import Config from '../consts'
import { json } from './request'
export const PIOImpactApiService = {
  getPioImpactData,
  savePioImpactData,
  deletePIOImpact,
  exportPIOImpact,
  importPIOImpact,
}
async function savePioImpactData(payload, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/pio-impact?year=${AOP_YEAR}&plantId=${PLANT_ID}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return Promise.reject(e)
  }
}
async function deletePIOImpact(id, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/pio-impact?id=${id}`
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
    // Return proper response object with status code
    return { code: resp.status, message: await resp.text() }
  } catch (e) {
    console.error('Error deleting PIO Impact data:', e)
    return Promise.reject(e)
  }
}
async function getPioImpactData(keycloak, PLANT_ID,AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/pio-impact?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
    return Promise.reject(e)
  }
}
export async function exportPIOImpact(keycloak, PLANT_ID,AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/pio-impact-export?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'PIO_Impact.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting PIO Impact Excel:', e)
    return Promise.reject(e)
  }
}
export async function importPIOImpact(file, keycloak, PLANT_ID,AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/pio-impact-import?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
    console.error('Error importing PIO Impact Excel:', e)
    return Promise.reject(e)
  }
}