
import Config from 'consts/index'
import { json } from '../../services/request'
export const UtilityPlantApiServiceV2 = {
  //  Fixed Consumption APIs
  getFixedConsumptionData,
  saveFixedConsumptionData,

  //   Plant requirement APIs
  getPlantRequirementData,
  savePlantRequirementData,

  // Import Consumption APIs
  getImportConsumptionData,
  saveImportConsumptionData,

  //Norm Based Utility Budget APIs
  getNormBasedUtilityBudget,
  saveNormsData,
  calculateNormsData,
}

// ===================== || Fixed Consumption APIs || ===================== //
async function getFixedConsumptionData(keycloak, PLANT_ID,AOP_YEAR) {
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
async function saveFixedConsumptionData(keycloak, PLANT_ID, payload,AOP_YEAR) {
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
async function getPlantRequirementData(keycloak, PLANT_ID,AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/plant-requirement/${PLANT_ID}/${AOP_YEAR}`
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
async function savePlantRequirementData(keycloak, PLANT_ID,AOP_YEAR, payload) {
  const url = `${Config.CaseEngineUrl}/consumption/plant-requirement-import?plantId=${PLANT_ID}&aopYear=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
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
  const body = JSON.stringify(
    {
    financial_year: financial_year,
    save_to_db: true,
    cpp_plant_id: PLANT_ID
}
  )
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