import Config from '../consts'
import { json } from './request'
export const QualityParameterService = {
  getQualityParameterData,
  saveQualityParameterData,
  QualityParametersExport,
  QualityParameterExcel,
  getPriceDifferentialData,
  savePriceDifferentialData,
  PriceDifferentialExport,
  PriceDifferentialExcel,
  getPackagingConsumbleData,
  savePackagingConsumbleData,
  PackagingConsumbleExport,
  PackagingConsumbleExcel,
  getOtherCostData,
  saveOtherCostData,
  OtherCostExport,
}
export async function getQualityParameterData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/quality-transaction?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function saveQualityParameterData(
  PLANT_ID,
  AOP_YEAR,
  qualityParameterDTOList,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/quality-transaction?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(qualityParameterDTOList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function QualityParametersExport(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_EXPORT_TITLE,
) {
  const url = `${Config.CaseEngineUrl}/task/quality-transaction-export?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`

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
    a.download = `${EXCEL_EXPORT_TITLE}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}
export async function QualityParameterExcel(file, keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/quality-transaction-import?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}`
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
    console.error('Error importing Plant Team Excel:', e)
    return Promise.reject(e)
  }
}
export async function getPriceDifferentialData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/price-differential-transaction?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function savePriceDifferentialData(
  PLANT_ID,
  AOP_YEAR,
  priceDifferentialDTOList,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/price-differential-transaction?plantFKId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(priceDifferentialDTOList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function PriceDifferentialExport(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_EXPORT_TITLE,
) {
  const url = `${Config.CaseEngineUrl}/task/price-differential-transaction-export?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`

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
    a.download = `${EXCEL_EXPORT_TITLE}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}
export async function PriceDifferentialExcel(file, keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/price-differential-transaction-import?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}`
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
    console.error('Error importing Plant Team Excel:', e)
    return Promise.reject(e)
  }
}
export async function getPackagingConsumbleData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/packaging-consumables-transaction?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function savePackagingConsumbleData(
  PLANT_ID,
  AOP_YEAR,
  packagingConsumbleDTOList,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/packaging-consumables-transaction?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(packagingConsumbleDTOList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function PackagingConsumbleExport(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_EXPORT_TITLE,
) {
  const url = `${Config.CaseEngineUrl}/task/packaging-consumble-transaction-export?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`

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
    a.download = `${EXCEL_EXPORT_TITLE}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}
export async function PackagingConsumbleExcel(file, keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/packaging-consumables-transaction-import?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}`
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
    console.error('Error importing Plant Team Excel:', e)
    return Promise.reject(e)
  }
}
export async function getOtherCostData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/other-costs-transaction?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function saveOtherCostData(
  PLANT_ID,
  AOP_YEAR,
  otherCostDTOList,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/other-costs-transaction?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(otherCostDTOList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function OtherCostExport(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_EXPORT_TITLE,
) {
  const url = `${Config.CaseEngineUrl}/task/other-cost-transaction-export?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`

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
    a.download = `${EXCEL_EXPORT_TITLE}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}
