import Config from 'consts/index'
import { json } from '../../../../services/request'

export const TcsApiService = {
  // Tab Configuration APIs
  getTcsAllTabs,
  getTcsVisibleTabs,

  // TCS Unit Capacity Data APIs
  getTcsUnitCapacityUOM,
  getTcsUnitCapacityData,
  getTcsNetCapacityData,
  saveUnitCapacityData,

  // TCS Crude Blend Window Data APIs
  getCrudBlendWindowData,
  saveCrudBlendWindowData,

  // TCS Shutdown Data APIs
  getTcsShutdownData,
  saveShutdownData,
  deleteShutdownData,

  // TCS Slowdown Data APIs
  getTcsSlowdownData,
  saveSlowdownData,
  deleteSlowdownData,

  // TCS ROGC Data APIs
  getTcsRogcData,
  saveRogcData,

  // TCS CPP Units SD Plan Data APIs
  getCPPUnitsSdPlanData,
  saveCPPUnitsSdPlanData,
  deleteCPPUnitsSdPlanData,

  // TCS PCG Outlook Data APIs
  getPcgOutlookData,
  savePcgOutlookData,

  // Excel Import/Export APIs (Legacy)
  downloadTcsExcel,
  uploadTcsExcel,

  // Specific Excel Import/Export APIs
  importShutdownExcel,
  exportShutdownExcel,
  importSlowdownExcel,
  exportSlowdownExcel,
  importRogcExcel,
  exportRogcExcel,
  importPcgOutlookExcel,
  exportPcgOutlookExcel,
  importCrudBlendWindowExcel,
  exportCrudBlendWindowExcel,
  importCPPUnitsSdPlanExcel,
  exportCPPUnitsSdPlanExcel,
  importUnitCapacityExcel,
  exportUnitCapacityExcel,
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

async function getTcsNetCapacityData(keycloak, plantId, year, capacityType) {
  let url = `${Config.CaseEngineUrl}/task/tcs-net-capacity?year=${year}&capacityType=${capacityType}`
  if (plantId) url += `&plantId=${plantId}`

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

async function deleteShutdownData(keycloak, id) {
  const url = `${Config.CaseEngineUrl}/task/tcs-shutdown?id=${id}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers,
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

async function deleteSlowdownData(keycloak, id) {
  const url = `${Config.CaseEngineUrl}/task/tcs-slowdown?id=${id}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers,
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

async function saveCPPUnitsSdPlanData(
  keycloak,
  financialYear,
  siteId,
  payload,
) {
  const url = `${Config.CaseEngineUrl}/task/cpp-unit-sd-plan/${financialYear}/${siteId}`
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

async function deleteCPPUnitsSdPlanData(keycloak, id) {
  const url = `${Config.CaseEngineUrl}/task/cpp-unit-sd-plan/${id}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers,
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

// ===================== || GENERIC EXCEL IMPORT FUNCTION || ===================== //
/**
 * Generic function to upload Excel file to any TCS endpoint
 * @param {File} file - The Excel file to upload
 * @param {Object} keycloak - Keycloak session object
 * @param {string} endpoint - The API endpoint base path (e.g., 'tcs-shutdown')
 * @param {Object} queryParams - Query parameters (plantId, year, etc.)
 * @returns {Promise} API response
 */
async function saveExcelData(file, keycloak, endpoint, queryParams = {}) {
  const queryString = new URLSearchParams(queryParams).toString()
  const url = queryString
    ? `${Config.CaseEngineUrl}/task/${endpoint}-import?${queryString}`
    : `${Config.CaseEngineUrl}/task/${endpoint}-import`

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
    if (!resp.ok) {
      throw new Error(
        `Failed to import data: ${resp.status} ${resp.statusText}`,
      )
    }
    return json(keycloak, resp)
  } catch (e) {
    console.error(`Error importing Excel data to ${endpoint}:`, e)
    return Promise.reject(e)
  }
}

// ===================== || GENERIC EXCEL EXPORT FUNCTION || ===================== //
/**
 * Generic function to export Excel file from backend
 * @param {Object} keycloak - Keycloak session object
 * @param {string} endpoint - The API endpoint base path (e.g., 'tcs-shutdown')
 * @param {Object} queryParams - Query parameters (plantId, year, etc.)
 * @param {string} fileName - Downloaded file name
 * @returns {Promise} Success/error response
 */
async function exportExcelData(keycloak, endpoint, queryParams = {}, fileName) {
  const queryString = new URLSearchParams(queryParams).toString()
  const url = queryString
    ? `${Config.CaseEngineUrl}/task/${endpoint}-export?${queryString}`
    : `${Config.CaseEngineUrl}/task/${endpoint}-export`

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })

    if (!resp.ok) {
      throw new Error(
        `Failed to export Excel: ${resp.status} ${resp.statusText}`,
      )
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob

    // Extract filename from Content-Disposition header if available
    const contentDisposition = resp.headers.get('content-disposition')
    let downloadFileName = fileName
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="?([^";\n]+)"?/i)
      if (filenameMatch && filenameMatch[1]) {
        downloadFileName = filenameMatch[1]
      }
    }

    a.download = downloadFileName
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)

    return { success: true, message: 'Excel exported successfully' }
  } catch (e) {
    console.error(`Error exporting Excel from ${endpoint}:`, e)
    return Promise.reject(e)
  }
}

// ===================== || SPECIFIC EXCEL IMPORT/EXPORT FUNCTIONS || ===================== //

// TCS Shutdown Excel Import
async function importShutdownExcel(keycloak, PLANT_ID, AOP_YEAR, file) {
  return saveExcelData(file, keycloak, 'tcs-shutdown', {
    plantId: PLANT_ID,
    year: AOP_YEAR,
  })
}

// TCS Shutdown Excel Export
async function exportShutdownExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    'tcs-shutdown',
    { plantId: PLANT_ID, year: AOP_YEAR },
    `TCS_Shutdown_${AOP_YEAR}.xlsx`,
  )
}

// TCS Slowdown Excel Import
async function importSlowdownExcel(keycloak, PLANT_ID, AOP_YEAR, file) {
  return saveExcelData(file, keycloak, 'tcs-slowdown', {
    plantId: PLANT_ID,
    year: AOP_YEAR,
  })
}

// TCS Slowdown Excel Export
async function exportSlowdownExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    'tcs-slowdown',
    { plantId: PLANT_ID, year: AOP_YEAR },
    `TCS_Slowdown_${AOP_YEAR}.xlsx`,
  )
}

// TCS ROGC Excel Import
async function importRogcExcel(keycloak, SITE_ID, PLANT_ID, AOP_YEAR, file) {
  return saveExcelData(file, keycloak, 'furnace', {
    year: AOP_YEAR,
    siteId: SITE_ID,
    plantId: PLANT_ID,
  })
}

// TCS ROGC Excel Export
async function exportRogcExcel(keycloak, SITE_ID, PLANT_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    'furnace',
    { year: AOP_YEAR, siteId: SITE_ID, plantId: PLANT_ID },
    `TCS_ROGC_${AOP_YEAR}.xlsx`,
  )
}

// TCS PCG Outlook Excel Import
async function importPcgOutlookExcel(keycloak, SITE_ID, AOP_YEAR, file) {
  return saveExcelData(file, keycloak, 'pcg-outlook', {
    siteId: SITE_ID,
    financialYear: AOP_YEAR,
  })
}

// TCS PCG Outlook Excel Export
async function exportPcgOutlookExcel(keycloak, SITE_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    'pcg-outlook',
    { siteId: SITE_ID, financialYear: AOP_YEAR },
    `TCS_PCG_Outlook_${AOP_YEAR}.xlsx`,
  )
}

// TCS Crud Blend Window Excel Import
async function importCrudBlendWindowExcel(
  keycloak,
  PLANT_ID,
  SITE_ID,
  AOP_YEAR,
  tableKey,
  file,
) {
  return saveExcelData(file, keycloak, 'crude-blend-window', {
    plantId: PLANT_ID,
    siteId: SITE_ID,
    year: AOP_YEAR,
    tableKey: tableKey,
  })
}

// TCS Crud Blend Window Excel Export
async function exportCrudBlendWindowExcel(
  keycloak,
  PLANT_ID,
  SITE_ID,
  AOP_YEAR,
  tableKey,
) {
  return exportExcelData(
    keycloak,
    'crude-blend-window',
    { plantId: PLANT_ID, siteId: SITE_ID, year: AOP_YEAR, tableKey: tableKey },
    `TCS_Crud_Blend_Window_${tableKey}_${AOP_YEAR}.xlsx`,
  )
}

// TCS CPP Units SD Plan Excel Import
async function importCPPUnitsSdPlanExcel(keycloak, SITE_ID, AOP_YEAR, file) {
  return saveExcelData(file, keycloak, 'cpp-unit-sd-plan', {
    financialYear: AOP_YEAR,
    siteId: SITE_ID,
  })
}

// TCS CPP Units SD Plan Excel Export
async function exportCPPUnitsSdPlanExcel(keycloak, SITE_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    'cpp-unit-sd-plan',
    { financialYear: AOP_YEAR, siteId: SITE_ID },
    `TCS_CPP_Units_SD_Plan_${AOP_YEAR}.xlsx`,
  )
}

// TCS Unit Capacity Excel Import
async function importUnitCapacityExcel(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  capacityType,
  uom,
  file,
) {
  return saveExcelData(file, keycloak, 'tcs-unit-capacity', {
    plantId: PLANT_ID,
    year: AOP_YEAR,
    capacityType: capacityType,
    uom: uom,
  })
}

// TCS Unit Capacity Excel Export
async function exportUnitCapacityExcel(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  capacityType,
  uom,
) {
  return exportExcelData(
    keycloak,
    'tcs-unit-capacity',
    {
      plantId: PLANT_ID,
      year: AOP_YEAR,
      capacityType: capacityType,
      uom: uom,
    },
    `TCS_Unit_Capacity_${capacityType}_${AOP_YEAR}.xlsx`,
  )
}

// Legacy functions for backward compatibility (deprecated)
async function uploadTcsExcel(keycloak, PLANT_ID, AOP_YEAR, tabName, file) {
  // Map tabName to appropriate endpoint
  if (tabName.includes('Shutdown')) {
    return importShutdownExcel(keycloak, PLANT_ID, AOP_YEAR, file)
  }
  // Add more mappings as needed
  console.warn('uploadTcsExcel: Unknown tabName, using generic endpoint')
  return saveExcelData(file, keycloak, 'tcs-generic', {
    plantId: PLANT_ID,
    year: AOP_YEAR,
  })
}

async function downloadTcsExcel(keycloak, PLANT_ID, AOP_YEAR, tabName) {
  // Map tabName to appropriate endpoint
  if (tabName.includes('Shutdown')) {
    return exportShutdownExcel(keycloak, PLANT_ID, AOP_YEAR)
  }
  // Add more mappings as needed
  console.warn('downloadTcsExcel: Unknown tabName, using generic endpoint')
  return exportExcelData(
    keycloak,
    'tcs-generic',
    { plantId: PLANT_ID, year: AOP_YEAR },
    `TCS_${tabName}_${AOP_YEAR}.xlsx`,
  )
}
