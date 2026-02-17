import Config from 'consts/index'
import { json } from '../../../../services/request'

export const TcsOutputApiService = {
  // Tab Configuration APIs
  getTcsAllTabs,
  getTcsVisibleTabs,

  // TCS Unit Capacity Data APIs
  getTcsUnitCapacityUOM,
  getTcsUnitCapacityData,
  getTcsNetUnitCapacityData,
  getTcsSiteNetCapacity,

  // TCS Crude Blend Window Data APIs
  getCrudBlendWindowData,

  // TCS Shutdown Data APIs
  getTcsShutdownData,

  // TCS Slowdown Data APIs
  getTcsSlowdownData,

  // TCS ROGC Data APIs
  getTcsRogcData,

  // TCS CPP Units SD Plan Data APIs
  getCPPUnitsSdPlanData,

  // TCS PCG Outlook Data APIs
  getPcgOutlookData,

  // Excel Import/Export APIs (Legacy)
  downloadTcsExcel,
  uploadTcsExcel,

  // Specific Excel Export APIs
  exportShutdownExcel,
  exportSlowdownExcel,
  exportRogcExcel,
  exportPcgOutlookExcel,
  exportCrudBlendWindowExcel,
  exportCPPUnitsSdPlanExcel,
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
async function getTcsUnitCapacityUOM(keycloak, verticalId, year, capacityType) {
  const url = `${Config.CaseEngineUrl}/task/tcs-unit-capacity/uom?verticalId=${verticalId}&year=${year}&capacityType=${capacityType}`
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
  siteId,
  verticalId,
  year,
  capacityType,
) {
  const url = `${Config.CaseEngineUrl}/task/tcs-unit-capacity?siteId=${siteId}&verticalId=${verticalId}&year=${year}&capacityType=${capacityType}`
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

async function getTcsNetUnitCapacityData(keycloak, siteId, verticalId, year) {
  const url = `${Config.CaseEngineUrl}/task/tcs-net-unit-capacity?siteId=${siteId}&verticalId=${verticalId}&year=${year}`
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

async function getTcsSiteNetCapacity(
  keycloak,
  siteId,
  verticalId,
  year,
  capacityType,
) {
  const url = `${Config.CaseEngineUrl}/task/site-capacity?siteId=${siteId}&verticalId=${verticalId}&year=${year}&capacityType=${capacityType}`
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

// ===================== || TCS Shutdown Data APIs || ===================== //
async function getTcsShutdownData(keycloak, siteId, verticalId, year) {
  const url = `${Config.CaseEngineUrl}/task/tcs-shutdown?siteId=${siteId}&verticalId=${verticalId}&year=${year}`
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

// ===================== || TCS Slowdown Data APIs || ===================== //
async function getTcsSlowdownData(keycloak, siteId, verticalId, year) {
  const url = `${Config.CaseEngineUrl}/task/tcs-slowdown?siteId=${siteId}&verticalId=${verticalId}&year=${year}`
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

// ===================== || TCS Crude Blend Window Data APIs || ===================== //
async function getCrudBlendWindowData(keycloak, year, siteId) {
  const url = `${Config.CaseEngineUrl}/task/crude-blend-window/${siteId}/${year}`
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

// ===================== || GENERIC EXCEL EXPORT FUNCTION || ===================== //
/**
 * Generic function to export Excel file from backend
 * @param {Object} keycloak - Keycloak session object
 * @param {string} endpoint - The API endpoint base path (e.g., 'tcs-shutdown')
 * @param {Object} queryParams - Query parameters (siteId, year, etc.) - NO plantId for TCS Output
 * @param {string} fileName - Downloaded file name
 * @returns {Promise} Success/error response
 */
async function exportExcelData(keycloak, endpoint, queryParams = {}, fileName) {
  const queryString = new URLSearchParams(queryParams).toString()
  const url = queryString
    ? `${Config.CaseEngineUrl}/task/${endpoint}?${queryString}`
    : `${Config.CaseEngineUrl}/task/${endpoint}`

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

// ===================== || SPECIFIC EXCEL EXPORT FUNCTIONS || ===================== //

// TCS Shutdown Excel Export
async function exportShutdownExcel(keycloak, SITE_ID, VERTICAL_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    'tcs-shutdown/export',
    { siteId: SITE_ID, verticalId: VERTICAL_ID, year: AOP_YEAR },
    `TCS_Shutdown_${AOP_YEAR}.xlsx`,
  )
}

// TCS Slowdown Excel Export
async function exportSlowdownExcel(keycloak, SITE_ID, VERTICAL_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    'tcs-slowdown/export',
    { siteId: SITE_ID, verticalId: VERTICAL_ID, year: AOP_YEAR },
    `TCS_Slowdown_${AOP_YEAR}.xlsx`,
  )
}

// TCS ROGC Excel Export
async function exportRogcExcel(keycloak, SITE_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    'furnace/export',
    { financialYear: AOP_YEAR, siteId: SITE_ID },
    `TCS_ROGC_${AOP_YEAR}.xlsx`,
  )
}

// TCS PCG Outlook Excel Export
async function exportPcgOutlookExcel(keycloak, SITE_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    'pcg-outlook/export',
    { siteId: SITE_ID, financialYear: AOP_YEAR },
    `TCS_PCG_Outlook_${AOP_YEAR}.xlsx`,
  )
}

// TCS Crud Blend Window Excel Export
async function exportCrudBlendWindowExcel(
  keycloak,
  SITE_ID,
  AOP_YEAR,
  tableKey,
) {
  return exportExcelData(
    keycloak,
    'crude-blend-window/export',
    {
      siteId: SITE_ID,
      financialYear: AOP_YEAR,
      table: tableKey,
    },
    `TCS_Crud_Blend_Window_${tableKey}_${AOP_YEAR}.xlsx`,
  )
}

// TCS CPP Units SD Plan Excel Export
async function exportCPPUnitsSdPlanExcel(keycloak, SITE_ID, AOP_YEAR) {
  return exportExcelData(
    keycloak,
    `cpp-unit-sd-plan/export/${AOP_YEAR}/${SITE_ID}`,
    {},
    `TCS_CPP_Units_SD_Plan_${AOP_YEAR}.xlsx`,
  )
}

// TCS Unit Capacity Excel Export
async function exportUnitCapacityExcel(
  keycloak,
  SITE_ID,
  VERTICAL_ID,
  AOP_YEAR,
  capacityType,
) {
  return exportExcelData(
    keycloak,
    'tcs-unit-capacity/export',
    {
      siteId: SITE_ID,
      verticalId: VERTICAL_ID,
      year: AOP_YEAR,
      capacityType: capacityType,
    },
    `TCS_Unit_Capacity_${capacityType}_${AOP_YEAR}.xlsx`,
  )
}

// ===================== || Excel Import/Export APIs (Legacy) || ===================== //
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
