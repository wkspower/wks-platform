import Config from 'consts/index'
import { json } from '../../request'

export const TcsOutputApiService = {
  // Tab Configuration APIs
  getTcsAllTabs,
  getTcsVisibleTabs,

  // TCS Unit Capacity Data APIs
  getTcsUnitCapacityUOM,
  getTcsUnitCapacityData,
  saveUnitCapacityData,

  // TCS Crude Blend Window Data APIs
  getCrudBlendWindowData,
  saveCrudBlendWindowData,

  // TCS Shutdown Data APIs
  getTcsShutdownData,
  saveShutdownData,

  // TCS Slowdown Data APIs
  getTcsSlowdownData,
  saveSlowdownData,

  // TCS ROGC Data APIs
  getTcsRogcData,
  saveRogcData,

  // TCS CPP Units SD Plan Data APIs
  getCPPUnitsSdPlanData,
  saveCPPUnitsSdPlanData,

  // TCS PCG Outlook Data APIs
  getPcgOutlookData,
  savePcgOutlookData,

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

// ===================== || TCS Unit Capacity Data APIs || ===================== //
async function getTcsUnitCapacityUOM(keycloak, plantId, year, capacityType) {
  const url = `${Config.CaseEngineUrl}/task/tcs-unit-capacity/uom?plantId=${plantId}&year=${year}&capacityType=${capacityType}`
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

async function getTcsUnitCapacityData(
  keycloak,
  plantId,
  year,
  capacityType,
  selectedUOM,
) {
  const url = `${Config.CaseEngineUrl}/task/tcs-unit-capacity?plantId=${plantId}&year=${year}&capacityType=${capacityType}&uom=${selectedUOM}`
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

async function saveUnitCapacityData(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  capacityType,
  uom,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/tcs-unit-capacity?plantId=${PLANT_ID}&year=${AOP_YEAR}&capacityType=${capacityType}&uom=${uom}`
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
    const result = await json(keycloak, resp)
    return result || { success: true }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ===================== || TCS Shutdown Data APIs || ===================== //
async function getTcsShutdownData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/tcs-shutdown?plantId=${plantId}&year=${year}`
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

async function saveShutdownData(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/tcs-shutdown?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
    const result = await json(keycloak, resp)
    return result || { success: true }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
// ===================== || TCS Slowdown Data APIs || ===================== //
async function getTcsSlowdownData(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/tcs-slowdown?plantId=${plantId}&year=${year}`
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

async function saveSlowdownData(keycloak, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/tcs-slowdown?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
    const result = await json(keycloak, resp)
    return result || { success: true }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ===================== || TCS Crude Blend Window Data APIs || ===================== //
async function getCrudBlendWindowData(keycloak, plantId, year, siteId) {
  const url = `${Config.CaseEngineUrl}/task/crude-blend-window/${plantId}/${siteId}/${year}`
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

async function saveCrudBlendWindowData(
  keycloak,
  plantId,
  year,
  siteId,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/crude-blend-window/${payload.tableKey}/${year}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = JSON.stringify(payload.data)
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

// ===================== || TCS CPP Units SD Plan Data APIs || ===================== //
async function getCPPUnitsSdPlanData(keycloak, financialYear, siteId) {
  const url = `${Config.CaseEngineUrl}/task/cpp-unit-sd-plan/${financialYear}/${siteId}`
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

async function saveCPPUnitsSdPlanData(keycloak, payload) {
  const url = `${Config.CaseEngineUrl}/task/cpp-unit-sd-plan`
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
    const result = await json(keycloak, resp)
    return result || { success: true }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ===================== || TCS ROGC Data APIs || ===================== //
async function getTcsRogcData(keycloak, siteId, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/furnace/${year}/${siteId}/${plantId}`
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

async function saveRogcData(keycloak, SITE_ID, PLANT_ID, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/furnace/${AOP_YEAR}/${SITE_ID}/${PLANT_ID}`
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
    const result = await json(keycloak, resp)
    return result || { success: true }
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// ===================== || TCS PCG Outlook Data APIs || ===================== //
async function getPcgOutlookData(keycloak, siteId, financialYear) {
  const url = `${Config.CaseEngineUrl}/task/pcg-outlook/${siteId}/${financialYear}`
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

async function savePcgOutlookData(keycloak, siteId, financialYear, payload) {
  const url = `${Config.CaseEngineUrl}/task/pcg-outlook/${siteId}/${financialYear}`
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
