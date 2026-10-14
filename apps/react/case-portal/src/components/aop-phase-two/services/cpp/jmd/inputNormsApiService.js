import Config from 'consts/index'
import { json } from 'services/request'

export const InputNormsApiService = {
  getNormsData,
  saveNormsData,
  exportNormsData,
  importNormsData,
  calculateNormsData,
}

async function getNormsData(
  keycloak,
  plantIds,
  financialYear,
  startDate,
  endDate,
) {
  const plantIdArray = Array.isArray(plantIds) ? plantIds : [plantIds]
  const queryParams = plantIdArray.map((id) => id).join(',')
  let url = `${Config.CaseEngineUrl}/task/jmd/cpp-norms?plantIds=${queryParams}&financialYear=${financialYear}`
  if (startDate && endDate) {
    url += `&startDate=${startDate}&endDate=${endDate}`
  }
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
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

async function saveNormsData(keycloak, payload, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/jmd/cpp-norms/${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
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

async function exportNormsData(
  keycloak,
  plantIds,
  financialYear,
  startDate,
  endDate,
) {
  const plantIdArray = Array.isArray(plantIds) ? plantIds : [plantIds]
  const queryParams = plantIdArray.map((id) => id).join(',')
  let url = `${Config.CaseEngineUrl}/task/jmd/cpp-norms/export?plantIds=${queryParams}&financialYear=${financialYear}`
  if (startDate && endDate) {
    url += `&startDate=${startDate}&endDate=${endDate}`
  }
  const headers = {
    Accept: 'application/octet-stream',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    const blob = await resp.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `CPP_Norms_${financialYear}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function importNormsData(keycloak, file, plantIds, financialYear) {
  const plantIdArray = Array.isArray(plantIds) ? plantIds : [plantIds]
  const queryParams = plantIdArray.map((id) => id).join(',')
  const url = `${Config.CaseEngineUrl}/task/jmd/cpp-norms/import?plantIds=${queryParams}&financialYear=${financialYear}`
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }
  const formData = new FormData()
  formData.append('file', file)
  try {
    const resp = await fetch(url, { method: 'POST', headers, body: formData })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function calculateNormsData(
  keycloak,
  plantIds,
  financialYear,
  startDate,
  endDate,
) {
  const plantIdArray = Array.isArray(plantIds) ? plantIds : [plantIds]
  const queryParams = plantIdArray.map((id) => id).join(',')
  let url = `${Config.CaseEngineUrl}/task/jmd/cpp-norms/calculate?plantIds=${queryParams}&financialYear=${financialYear}`
  if (startDate && endDate) {
    url += `&startDate=${startDate}&endDate=${endDate}`
  }
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'POST', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
