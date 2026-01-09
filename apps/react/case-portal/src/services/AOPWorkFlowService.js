import Config from '../consts'
import { json } from './request'
//AOPWorkFlowService

export const AOPWorkFlowService = {
 getExcel,
 getWorkflowData,
 getCaseId,
 submitWorkFlow,
 completeTask,
 saveAnnualWorkFlowData,
 getWorkflowDataProduction,
 handleCalculateAnnualAopCostMiisContribution,
 handleCalculateProductionVolData2,
 handleCalculatePlantProductionData,
 handleCalculateMonthwiseProduction,
 calculateTurnAroundPlanReportData,
 calculateAnnualProductionPlanData,
 handleCalculatePlantConsumptionData,
 calculatePlantContributionReportData,
 calculatePlantContributionSummaryYearly,
 calculatePlantContributionBusinessDemand,
 calculateGradeSpecificConsumptionNorm,
 getPlantProductionSummary,
 savePlantProductionData,
 getMonthwiseRawData,
 postMonthwiseRawData,
 getAnnualProductionPlanReportData,
 saveAnnualProduction,
 deleteAnnualProduction,

}
async function getExcel(keycloak, payload, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/export-excel?year=${AOP_YEAR}&plantId=${PLANT_ID}&type=Production`
  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
    })
    if (!resp.ok) {
      throw new Error(`Failed to edit data: ${resp.status} ${resp.statusText}`)
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'plant_production_plan.xlsx' // Filename to save
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing Config data:', e)
    return Promise.reject(e)
  }
}
async function getWorkflowData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/work-flow?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function getCaseId(keycloak, PLANT_ID, AOP_YEAR, SITE_ID, VERTICAL_ID) {
  const url = `${Config.CaseEngineUrl}/task/getCaseId?plantId=${PLANT_ID}&year=${AOP_YEAR}&siteId=${SITE_ID}&verticalId=${VERTICAL_ID}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'GET',
      headers,
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function submitWorkFlow(data, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/submitWorkflow`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function completeTask(keycloak, payload) {
  const url = `${Config.CaseEngineUrl}/task/completetask`
  // 1. Ensure token is fresh before every request
  await keycloak.updateToken(30)
  // 2. Execute the POST
  const resp = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${keycloak.token}`,
    },
    body: JSON.stringify(payload),
  })
  // 3. 204 = success (no JSON body)
  if (resp.status === 204) return true
  // 4. Any other non-OK status ? read text & throw
  if (!resp.ok) {
    const text = await resp.text().catch(() => '')
    throw new Error(`Server error ${resp.status}: ${text}`.trim())
  }
  // 5. If 2xx with a body, parse it
  const data = await resp.json()
  return Boolean(data)
}
async function saveAnnualWorkFlowData(keycloak, workFlowData, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/annual-aop-data?plantId=${PLANT_ID}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(workFlowData),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function getWorkflowDataProduction(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/production-aop/work-flow?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function handleCalculateAnnualAopCostMiisContribution(
  PLANT_ID,
  AOP_YEAR,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/miis-contribution?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function handleCalculateProductionVolData2(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/work-flow?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function handleCalculatePlantProductionData(
  PLANT_ID,
  AOP_YEAR,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/plant-production-summary?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function handleCalculateMonthwiseProduction(
  PLANT_ID,
  AOP_YEAR,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/monthwise-production-data?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function calculateTurnAroundPlanReportData(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/turnaround-plan-data?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function calculateAnnualProductionPlanData(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/annual-production-data?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function handleCalculatePlantConsumptionData(
  PLANT_ID,
  AOP_YEAR,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/plant-consumption-data?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function calculatePlantContributionReportData(
  PLANT_ID,
  AOP_YEAR,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/plan-contribution-data?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function calculatePlantContributionSummaryYearly(
  PLANT_ID,
  AOP_YEAR,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/calculate-plant-contribution-summary-yearly?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function calculatePlantContributionBusinessDemand(
  PLANT_ID,
  AOP_YEAR,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/load-plant-contribution?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function calculateGradeSpecificConsumptionNorm(
  PLANT_ID,
  AOP_YEAR,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/load-grade-wise-consumption-norms?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getPlantProductionSummary(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/report/production-summary?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function savePlantProductionData(keycloak, dataList, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/plant-production-data?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(dataList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function getMonthwiseRawData(keycloak, reportType, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/report/month-wise/consumption-summary?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${reportType}`
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
async function postMonthwiseRawData(keycloak, dataList, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/report/month-wise/consumption-summary?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    // ?? Log payload before sending to backend
    console.log('Sending payload:', dataList)
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(dataList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error posting data:', e)
    return await Promise.reject(e)
  }
}
async function getAnnualProductionPlanReportData(
  keycloak,
  type,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/report/plant/production/plan?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${type}`
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
async function saveAnnualProduction(
  PLANT_ID,
  AOP_YEAR,
  reportType,
  dataList,
  keycloak,
) {
  let queryParams = `?plantId=${encodeURIComponent(PLANT_ID)}&year=${encodeURIComponent(AOP_YEAR)}`
  if (reportType) {
    queryParams += `&reportType=${encodeURIComponent(reportType)}`
  }
  const url = `${Config.CaseEngineUrl}/task/report/plant/production/plan${queryParams}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(dataList),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error in saveAnnualProduction:', e)
    return await Promise.reject(e)
  }
}
async function deleteAnnualProduction(id, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/report/plant/production/plan?id=${encodeURIComponent(id)}`
  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'DELETE',
      headers,
    })
    if (!resp.ok) {
      throw new Error(
        `Failed to delete data: ${resp.status} ${resp.statusText}`,
      )
    }
    return await resp.json()
  } catch (e) {
    console.error('Error deleting slowdown data:', e)
    return Promise.reject(e)
  }
}