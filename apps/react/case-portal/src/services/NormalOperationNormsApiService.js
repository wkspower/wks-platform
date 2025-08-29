import Config from '../consts'
import { json } from './request'
export const NormalOperationNormsApiService = {
  getModeWiseNormsData,
  updateModeWiseNormsData,
  getNormalOperationNormsData,
  getNormalOperationNormsGrades,
  getGradesForShutdownNorms,
  getIntermediateValues,
  getNormTransactions,
  saveNormalOperationNormsData,
  saveNormalOpsNormsExcel,
  handleCalculateNormalOperationNormsPe,
  handleCalculateNormalOperationNorms,
  getNormalOpsNormsExcel,
  getFinalNormsData,
  updateFinalNormsData,
  getfinalNorms,
}
async function getfinalNorms(keycloak, gradeId, method) {
  const year = localStorage.getItem('year') || ''
  const storedPlant = localStorage.getItem('selectedPlant')
  const plantId = storedPlant ? JSON.parse(storedPlant)?.id || '' : ''

  const url = `${Config.CaseEngineUrl}/task/final-norms?year=${year}&plantId=${plantId}`

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
async function getModeWiseNormsData(keycloak, gradeId, method) {
  const year = localStorage.getItem('year') || ''
  const storedPlant = localStorage.getItem('selectedPlant')
  const plantId = storedPlant ? JSON.parse(storedPlant)?.id || '' : ''

  const url = `${Config.CaseEngineUrl}/task/mode-wise/norms?year=${year}&plantId=${plantId}&mode=${gradeId}&method=${method}`

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
async function updateModeWiseNormsData(keycloak, gradeId, payload) {
  const baseUrl = `${Config.CaseEngineUrl}/task/mode-wise/norms`
  const year = localStorage.getItem('year') || ''
  const storedPlant = localStorage.getItem('selectedPlant')
  const plantId = storedPlant ? JSON.parse(storedPlant)?.id || '' : ''

  const queryParams = new URLSearchParams({
    year,
    plantId,
  })
  const url = `${baseUrl}?${queryParams.toString()}`

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
    return await Promise.reject(e)
  }
}
async function getNormalOperationNormsData(
  keycloak,
  gradeId,
  isCracker = false,
) {
  const year = localStorage.getItem('year') || ''
  const storedPlant = localStorage.getItem('selectedPlant')
  const plantId = storedPlant ? JSON.parse(storedPlant)?.id || '' : ''
  // Construct URL based on presence of gradeId
  const baseUrl = `${Config.CaseEngineUrl}/task/normalOperationNorms`
  const queryParams = new URLSearchParams({
    year,
    plantId,
  })
  if (gradeId) {
    isCracker
      ? queryParams.append('mode', gradeId)
      : queryParams.append('gradeId', gradeId)
  }
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
async function getNormalOperationNormsGrades(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/normal-operation/norms/grades?year=${year}&plantId=${plantId}`
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
async function getGradesForShutdownNorms(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/unique/grades?year=${year}&plantId=${plantId}`
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

async function getIntermediateValues(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/get/configuration/intermediate-values?year=${year}&plantFKId=${plantId}`
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
async function getNormTransactions(keycloak) {
  var year = localStorage.getItem('year')
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)
  const url = `${Config.CaseEngineUrl}/task/norms-transactions?plantId=${parsedPlant?.id}&year=${year}`
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
async function saveNormalOperationNormsData(
  plantId,
  turnAroundDetails,
  keycloak,
  gradeId,
  lowerVertName,
) {
  const year = localStorage.getItem('year')
  const queryParams = new URLSearchParams({ year, plantId })
  if (lowerVertName === 'pe' || lowerVertName === 'pp') {
    queryParams.append('gradeId', gradeId)
  }
  const url = `${Config.CaseEngineUrl}/task/normalOperationNorms?${queryParams.toString()}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(turnAroundDetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function saveNormalOpsNormsExcel(file, keycloak) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/norms-import-excel?plantId=${plantId}&year=${year}`
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
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function handleCalculateNormalOperationNormsPe(
  plantId,
  siteId,
  verticalId,
  year,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/calculate-normal-ops-norms?plantId=${plantId}&siteId=${siteId}&verticalId=${verticalId}&aopYear=${year}`
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
async function handleCalculateNormalOperationNorms(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handleCalculateNormalOpsNorms?year=${year1}&plantId=${plantId}`
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
async function getNormalOpsNormsExcel(keycloak, gradeId) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  var url = `${Config.CaseEngineUrl}/task/norms-export-excel?year=${year}&plantId=${plantId}`

  if (gradeId) {
    url += `&gradeId=${gradeId}`
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
      throw new Error(`Failed to edit data: ${resp.status} ${resp.statusText}`)
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'Steady State Norms.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing data:', e)
    return Promise.reject(e)
  }
}
async function getFinalNormsData(keycloak, gradeId, method) {
  const year = localStorage.getItem('year') || ''
  const storedPlant = localStorage.getItem('selectedPlant')
  const plantId = storedPlant ? JSON.parse(storedPlant)?.id || '' : ''

  const url = `${Config.CaseEngineUrl}/task/mode-wise/norms?year=${year}&plantId=${plantId}&mode=${gradeId}&method=${method}`

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
async function updateFinalNormsData(keycloak, gradeId, payload) {
  const baseUrl = `${Config.CaseEngineUrl}/task/mode-wise/norms`
  const year = localStorage.getItem('year') || ''
  const storedPlant = localStorage.getItem('selectedPlant')
  const plantId = storedPlant ? JSON.parse(storedPlant)?.id || '' : ''

  const queryParams = new URLSearchParams({
    year,
    plantId,
  })
  const url = `${baseUrl}?${queryParams.toString()}`

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
    return await Promise.reject(e)
  }
}
