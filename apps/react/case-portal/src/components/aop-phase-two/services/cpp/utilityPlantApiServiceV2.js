import Config from 'consts/index'
import { json } from '../../../../services/request'
export const UtilityPlantApiServiceV2 = {
  //  Fixed Consumption APIs
  getFixedConsumptionData,
  saveFixedConsumptionData,
  saveFixedConsumptionExcel,
  exportFixedConsumptionExcel,

  //   Plant requirement APIs
  getPlantRequirementData,
  savePlantRequirementData,
  savePlantRequirementExcel,
  exportPlantRequirementExcel,

  // Import Consumption APIs
  getImportConsumptionData,
  saveImportConsumptionData,

  //Norm Based Utility Budget APIs
  getNormBasedUtilityBudget,
  saveNormsData,
  saveNormsExcel,
  exportNormsExcel,
  calculateNormsData,

  // Generic Excel Import/Export
  saveExcelData,
  exportExcelData,
}

// ===================== || Fixed Consumption APIs || ===================== //
async function getFixedConsumptionData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/fixed-consumption/${PLANT_ID}/${AOP_YEAR}`
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
async function saveFixedConsumptionData(keycloak, PLANT_ID, payload, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/update-fixed-consumption/${AOP_YEAR}`
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

// ===================== || Plant Requirement APIs || ===================== //
async function getPlantRequirementData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/plant-requirement/${AOP_YEAR}`
  // const url = `${Config.CaseEngineUrl}/task/plant-requirement/${PLANT_ID}/${AOP_YEAR}`
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
async function savePlantRequirementData(keycloak, AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/task/plant-requirement/${AOP_YEAR}`
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

// ===================== || Import  Consumption APIs || ===================== //
async function getImportConsumptionData(keycloak, YEAR) {
  const url = `${Config.CaseEngineUrl}/task/asset-import-mapping?financialYear=${YEAR}`
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
async function saveImportConsumptionData(keycloak, PLANT_ID, payload) {
  const url = `${Config.CaseEngineUrl}/task/consumption/update-import-power`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  const body = payload
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

//====================|| NORM BASED UTILITY BUDGET APIs ||====================//
async function getNormBasedUtilityBudget(keycloak, PLANT_ID, financialYear) {
  const url = `${Config.CaseEngineUrl}/task/norm-based-utility-budget?cppPlantId=${PLANT_ID}&financialYear=${financialYear}`
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

async function calculateNormsData(keycloak, PLANT_ID, financialYear) {
  const url = `${Config.CaseEngineUrl}/task/budget/run-full-year`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  let financial_year = financialYear.split('-')[0]
  const body = JSON.stringify({
    financial_year: financial_year,
    save_to_db: true,
    cpp_plant_id: PLANT_ID,
  })
  try {
    const resp = await fetch(url, { method: 'POST', headers, body })
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
  const url = `${Config.CaseEngineUrl}/task/saveOrUpdateNormsMonths/${AOP_YEAR}`
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

// ===================== || GENERIC EXCEL IMPORT FUNCTION || ===================== //
/**
 * Generic function to upload Excel file to any CPP endpoint
 * @param {File} file - The Excel file to upload
 * @param {Object} keycloak - Keycloak session object
 * @param {string} endpoint - The API endpoint path (e.g., 'fixed-consumption/import')
 * @param {string} PLANT_ID - Plant ID
 * @param {string} AOP_YEAR - Financial year
 * @returns {Promise} API response
 */
async function saveExcelData(file, keycloak, endpoint, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/${endpoint}`
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
 * @param {Object} params - Export parameters
 * @param {string} params.endpoint - The API endpoint path
 * @param {Object} params.queryParams - Query parameters
 * @param {Object|null} params.payload - Optional POST body payload
 * @param {string} params.fileName - Downloaded file name
 * @param {string} params.method - HTTP method ('GET' or 'POST'), defaults to 'GET'
 * @returns {Promise} Success/error response
 */
async function exportExcelData(keycloak, params) {
  const {
    endpoint,
    queryParams = {},
    payload = null,
    fileName,
    method = 'GET',
  } = params

  const queryString = new URLSearchParams(queryParams).toString()
  const url = `${Config.CaseEngineUrl}/task/${endpoint}`

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const fetchOptions = {
      method,
      headers,
    }

    if (payload && (method === 'POST' || method === 'PUT')) {
      fetchOptions.body = JSON.stringify(payload)
    }

    const resp = await fetch(url, fetchOptions)

    if (!resp.ok) {
      throw new Error(
        `Failed to export Excel: ${resp.status} ${resp.statusText}`,
      )
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

    return { success: true, message: 'Excel exported successfully' }
  } catch (e) {
    console.error(`Error exporting Excel from ${endpoint}:`, e)
    return Promise.reject(e)
  }
}

// ===================== || SPECIFIC EXCEL IMPORT FUNCTIONS || ===================== //

// Fixed Consumption Excel Import
async function saveFixedConsumptionExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    `fixed-consumption/import/${PLANT_ID}/${AOP_YEAR}`,
    PLANT_ID,
    AOP_YEAR,
  )
}

// Fixed Consumption Excel Export
async function exportFixedConsumptionExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `fixed-consumption/export/${PLANT_ID}/${AOP_YEAR}`,
    queryParams: {},
    fileName: `FixedConsumption_${AOP_YEAR}.xlsx`,
    method: 'GET',
  })
}

// Plant Requirement Excel Import
async function savePlantRequirementExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    `plant-requirement/import/${PLANT_ID}/${AOP_YEAR}`,
    PLANT_ID,
    AOP_YEAR,
  )
}

// Plant Requirement Excel Export
async function exportPlantRequirementExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `plant-requirement/export/${PLANT_ID}/${AOP_YEAR}`,
    queryParams: { plantId: PLANT_ID, year: AOP_YEAR },
    fileName: `plant_requirement_${PLANT_ID}_${AOP_YEAR}.xlsx`,
    method: 'GET',
  })
}

// Norms Excel Import
async function saveNormsExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    `norm-based-utility-budget/import?cppPlantId=${PLANT_ID}&financialYear=${AOP_YEAR}`,
    PLANT_ID,
    AOP_YEAR,
  )
}

// Norms Excel Export
async function exportNormsExcel(keycloak, PLANT_ID, AOP_YEAR) {
  return exportExcelData(keycloak, {
    endpoint: `norm-based-utility-budget/export?cppPlantId=${PLANT_ID}&financialYear=${AOP_YEAR}`,
    queryParams: {},
    fileName: `Norms_${PLANT_ID}_${AOP_YEAR}.xlsx`,
    method: 'GET',
  })
}
