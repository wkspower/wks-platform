import Config from 'consts/index'
import { json } from '../../../../services/request'
export const UtilityPlantApiServiceV2 = {
  //  Fixed Consumption APIs
  getFixedConsumptionData,
  saveFixedConsumptionData,
  saveFixedConsumptionExcel,

  //   Plant requirement APIs
  getPlantRequirementData,
  savePlantRequirementData,
  savePlantRequirementExcel,

  // Import Consumption APIs
  getImportConsumptionData,
  saveImportConsumptionData,

  //Norm Based Utility Budget APIs
  getNormBasedUtilityBudget,
  saveNormsData,
  saveNormsExcel,
  calculateNormsData,

  // Generic Excel Import
  saveExcelData,
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
  const url = `${Config.CaseEngineUrl}/task/${endpoint}?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

// ===================== || SPECIFIC EXCEL IMPORT FUNCTIONS || ===================== //

// Fixed Consumption Excel Import
async function saveFixedConsumptionExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'fixed-consumption/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// Plant Requirement Excel Import
async function savePlantRequirementExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(
    file,
    keycloak,
    'plant-requirement/import',
    PLANT_ID,
    AOP_YEAR,
  )
}

// Norms Excel Import
async function saveNormsExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  return saveExcelData(file, keycloak, 'norms/import', PLANT_ID, AOP_YEAR)
}
