import Config from '../consts'
import { json } from './request'
export const FunctionalApiService = {
  saveAOPConsumptionNorm,
  getReliabilityPerformance,
  saveReliabilityPerformance,
  getReliabilityRecords,
  testMacro,
  saveReliabilityRecords,
  exportReliabilityExcel,
  importReliabilityPerformanceExcel,
  importReliabilityIncidentExcel,
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
async function exportReliabilityExcel(keycloak, PLANT_ID, AOP_YEAR, excelName) {
 
  let url = ''
  let fileName = ''
  if (excelName === 'Reliability_Performance') {
    url = `${Config.CaseEngineUrl}/task/reliability-performance-export-excel?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`
    fileName = 'Reliability_Performance.xlsx'
  } else if (excelName === 'Major_Reliability_Incidents') {
    url = `${Config.CaseEngineUrl}/task/reliability-records-export-excel?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`
    fileName = 'Major_Reliability_Incidents.xlsx'
  } else {
    throw new Error('Unknown ExcelName for export')
  }
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
    a.download = fileName
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Reliability Performance Excel:', e)
    return Promise.reject(e)
  }
}
async function importReliabilityPerformanceExcel( keycloak, PLANT_ID, AOP_YEAR, rawFile, excelName) {
  const url = `${Config.CaseEngineUrl}/task/reliability-performance-import-excel?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const formData = new FormData()
  formData.append('file', rawFile)

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
    console.error('Error importing Reliability Performance Excel:', e)
    return await Promise.reject(e)
  }
}
async function importReliabilityIncidentExcel( keycloak, PLANT_ID, AOP_YEAR, rawFile, excelName) {
  const url = `${Config.CaseEngineUrl}/task/reliability-records-import-excel?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const formData = new FormData()
  formData.append('file', rawFile)

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
    console.error('Error importing Reliability Performance Excel:', e)
    return await Promise.reject(e)
  }
}