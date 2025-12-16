
import Config from 'consts/index'
import { json } from '../../services/request'
import { DataService } from '../DataService'

export const TcsApiService = {
  // Tab Configuration APIs
  getTcsAllTabs,
  getTcsVisibleTabs,

  // TCS Input Data APIs
  getTcsInputData,
  saveTcsInputData,

  // Excel Import/Export APIs
  downloadTcsExcel,
  uploadTcsExcel,
}

// ===================== || Tab Configuration APIs || ===================== //
async function getTcsAllTabs(keycloak) {
  const url = `${Config.CaseEngineUrl}/task/configuration-type-data`
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

async function getTcsVisibleTabs(keycloak, VERTICAL_ID, SITE_ID, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/access/matrix?plantId=${PLANT_ID}&siteId=${SITE_ID}&verticalId=${VERTICAL_ID}&type=TCS`
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

// ===================== || TCS Input Data APIs || ===================== //
async function getTcsInputData(keycloak, PLANT_ID, AOP_YEAR, tabName) {
  const url = `${Config.CaseEngineUrl}/task/tcs-input-data?plantId=${PLANT_ID}&year=${AOP_YEAR}&tabName=${encodeURIComponent(tabName)}`
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

async function saveTcsInputData(keycloak, PLANT_ID, AOP_YEAR, tabName, payload) {
  const url = `${Config.CaseEngineUrl}/task/save-tcs-input-data`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify({
    plantId: PLANT_ID,
    year: AOP_YEAR,
    tabName: tabName,
    data: payload,
  })
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
    console.log(e)
    return await Promise.reject(e)
  }
}

// ===================== || Excel Import/Export APIs || ===================== //
async function downloadTcsExcel(keycloak, PLANT_ID, AOP_YEAR, tabName) {
  const url = `${Config.CaseEngineUrl}/task/download-tcs-excel?plantId=${PLANT_ID}&year=${AOP_YEAR}&tabName=${encodeURIComponent(tabName)}`
  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }
    return resp.blob()
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function uploadTcsExcel(keycloak, PLANT_ID, AOP_YEAR, tabName, file) {
  const url = `${Config.CaseEngineUrl}/task/upload-tcs-excel`
  const formData = new FormData()
  formData.append('file', file)
  formData.append('plantId', PLANT_ID)
  formData.append('year', AOP_YEAR)
  formData.append('tabName', tabName)

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: formData,
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
