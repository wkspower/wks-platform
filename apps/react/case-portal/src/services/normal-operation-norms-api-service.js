import Config from '../consts'
import { json } from './request'
export const NormalOperationNormsApiService = {
  getModeWiseNormsData,
  getModeWiseNormsDataworkflow,
  updateModeWiseNormsData,
  getNormalOperationNormsData,
  getNormalOperationNormsGrades,
  getGradesForShutdownNorms,
  getGradesForSlowdownNorms,
  getIntermediateValues,
  getNormTransactions,
  saveNormalOperationNormsData,
  saveNormalOpsNormsExcel,
  handleCalculateNormalOperationNormsPe,
  handleCalculateNormalOperationNorms,
  getNormalOpsNormsExcel,
  getNormalOpsNormsExcelpe,
  saveShutdownNormsExcel,
  updateFinalNormsData,
  getfinalNorms,
  calculateFinalNorms,
  criteriaForBestAchivedExcelExport,
  CrackerConstantsImport,
  getNormsConstants,
  BestAchivedColorCodes,
  load1,
  load2,
  load3,
  getNormTransactionsForFinalNorms,
  getNormTransactionsForFinalNormsModeWise,
  shutdownnormsppExport,
}

async function BestAchivedColorCodes(keycloak, plantId, year, mode) {
  const url = `${Config.CaseEngineUrl}/task/mode-wise/norms-monthwise-modetype?year=${year}&plantId=${plantId}&mode=${encodeURIComponent(mode)}`
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
async function CrackerConstantsImport(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-constants-import-excel?plantFKId=${PLANT_ID}&year=${AOP_YEAR}`
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
    console.error('Error importing cracker Criteria best achieved:', e)
    return await Promise.reject(e)
  }
}

async function criteriaForBestAchivedExcelExport(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-constants-norms-export-excel?year=${encodeURIComponent(AOP_YEAR)}&plantFKId=${encodeURIComponent(PLANT_ID)}`

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
    a.download = `Criteria_best_achieved_${'Export'}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}

async function getNormsConstants(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-constants-norms?year=${AOP_YEAR}&plantFKId=${PLANT_ID}`
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

async function load1(keycloak, PLANT_ID, AOP_YEAR, endDate, startDate) {
  const url = `${Config.CaseEngineUrl}/task/calculate-best-achieved-individual?year=${AOP_YEAR}&plantId=${PLANT_ID}&periodTo=${endDate}&periodFrom=${startDate}`
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
async function load2(keycloak, PLANT_ID, AOP_YEAR, endDate, startDate) {
  const url = `${Config.CaseEngineUrl}/task/calculate-best-achieved?year=${AOP_YEAR}&plantId=${PLANT_ID}&periodTo=${endDate}&periodFrom=${startDate}`
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
async function load3(keycloak, PLANT_ID, AOP_YEAR, endDate, startDate) {
  // calculate--norms
  const url = `${Config.CaseEngineUrl}/task/calculate-steady-norms?year=${AOP_YEAR}&plantId=${PLANT_ID}&periodTo=${endDate}&periodFrom=${startDate}`
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

async function getfinalNorms(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/final-norms?year=${AOP_YEAR}&plantId=${PLANT_ID}`

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
async function getModeWiseNormsData(
  keycloak,
  gradeId,
  method,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/mode-wise/norms?year=${AOP_YEAR}&plantId=${PLANT_ID}&mode=${gradeId}&method=${method}`

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
async function getModeWiseNormsDataworkflow(
  keycloak,
  gradeId,
  method,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/month-wise-raw-data-by-method?year=${AOP_YEAR}&plantId=${PLANT_ID}&mode=${gradeId}&method=${method}`
  ///month-wise-raw-data-by-method
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
async function updateModeWiseNormsData(
  keycloak,
  gradeId,
  payload,
  PLANT_ID,
  AOP_YEAR,
) {
  const baseUrl = `${Config.CaseEngineUrl}/task/mode-wise/norms`
  const year = AOP_YEAR
  const plantId = PLANT_ID

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
  PLANT_ID,
  AOP_YEAR,
) {
  const year = AOP_YEAR
  const plantId = PLANT_ID
  // Construct URL based on presence of gradeId
  const baseUrl = `${Config.CaseEngineUrl}/task/steady-state-norms`
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
async function getNormalOperationNormsGrades(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/normal-operation/norms/grades?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getGradesForShutdownNorms(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/unique/grades?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getGradesForSlowdownNorms(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/slowdown-norms-grades?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getIntermediateValues(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/intermediate-values?year=${AOP_YEAR}&plantFKId=${PLANT_ID}`
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
async function getNormTransactions(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/norms-transactions?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
  AOP_YEAR,
) {
  const year = AOP_YEAR
  const queryParams = new URLSearchParams({ year, plantId })
  if (lowerVertName === 'pe' || lowerVertName === 'pp') {
    queryParams.append('gradeId', gradeId)
  }
  const url = `${Config.CaseEngineUrl}/task/steady-state-norms?${queryParams.toString()}`
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
async function saveNormalOpsNormsExcel(
  file,
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  GRADE_ID,
) {
  let url = ''
  url = `${Config.CaseEngineUrl}/task/steady-state-norms-import?plantId=${PLANT_ID}&year=${AOP_YEAR}`

  if (GRADE_ID) {
    url += `&gradeId=${GRADE_ID}`
  }

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

async function saveShutdownNormsExcel(
  file,
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  GRADE_ID,
) {
  let url = ''
  url = `${Config.CaseEngineUrl}/task/shutdown-consumption-import?plantId=${PLANT_ID}&year=${AOP_YEAR}`

  if (GRADE_ID) {
    url += `&gradeId=${GRADE_ID}`
  }

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
async function handleCalculateNormalOperationNorms(
  plantId,
  AOP_YEAR,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/calculate-steady-state-norms?year=${AOP_YEAR}&plantId=${plantId}`
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
async function calculateFinalNorms(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/calculate-final-norms?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getNormalOpsNormsExcel(
  keycloak,
  gradeId,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_EXPORT_TITLE,
  SCREEN_NAME,
) {
  var url = `${Config.CaseEngineUrl}/task/steady-state-norms-export?year=${AOP_YEAR}&plantId=${PLANT_ID}`

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
    a.download = `${EXCEL_EXPORT_TITLE}_${SCREEN_NAME}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing data:', e)
    return Promise.reject(e)
  }
}

async function getNormalOpsNormsExcelpe(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_EXPORT_TITLE,
  SCREEN_NAME,
) {
  var url = `${Config.CaseEngineUrl}/task/steady-state-norms-all-grades-export?year=${AOP_YEAR}&plantId=${PLANT_ID}`

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
    a.download = `${EXCEL_EXPORT_TITLE}_${SCREEN_NAME}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing data:', e)
    return Promise.reject(e)
  }
}
async function updateFinalNormsData(
  keycloak,
  gradeId,
  payload,
  PLANT_ID,
  AOP_YEAR,
) {
  const baseUrl = `${Config.CaseEngineUrl}/task/final-norms`
  const year = AOP_YEAR
  const plantId = PLANT_ID
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

async function getNormTransactionsForFinalNormsModeWise(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/norms-transactions-final-norms-mode-wise?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function getNormTransactionsForFinalNorms(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/norms-transactions-final-norms?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
export async function shutdownnormsppExport(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-consumption-export?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}`
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
    a.download = 'shutdown_consumption.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Shutdown Excel:', e)
    return Promise.reject(e)
  }
}
