import { json } from './request'
import Config from '../consts'

export const DataService = {
  getProductById,
  getYearWiseProduct,
  getAllSites,
  getNormTransactions,
  getShutDownPlantData,
  getAllProducts,
  getAllProductsAll,
  getIbrPlanData,
  getShutdownActivitiesData,
  getRunningDurationData,
  getYearlyData,
  getSlowDownPlantData,
  getTAPlantData,
  getBDData,
  getCatalystSelectivityData,
  getCatalystSelectivityDataConstants,
  getCatalystSelectivityDataIV,
  getConfigurationTabsMatrix,
  getConfigurationAvailableTabs,
  getProductionNormsData,
  getConsumptionNormsData,
  getMaintenanceData,
  getTurnaroundReportData,
  getAopSummary,

  getAllCatalyst,

  saveShutdownData,
  saveAnnualWorkFlowData,
  saveRemarkData,
  savePlantProductionData,
  saveMonthwiseProduction,
  saveTurnaroundReport,
  saveTurnaroundReportWhole,
  saveText,
  saveAOPConsumptionNorm,
  saveSummaryAOPConsumptionNorm,
  saveSlowdownData,
  saveTurnAroundData,

  saveCatalystData,

  saveBusinessDemandData,
  saveSpyroInput,
  saveSpyroOutput,
  getSpyroOutputData,
  saveNormalOperationNormsData,
  saveShutDownNormsData,
  saveSlowdownNormsData,
  editAOPMCCalculatedData,

  updateSlowdownData,
  updateShutdownData,
  updateTurnAroundData,
  updateProductNormData,
  updateBusinessDemandDataM,

  createCase,
  getTasksByBusinessKey,
  getProcessInstanceVariables,
  completeTask,

  getAOPData,
  getSpyroInputData,
  getAOPMCCalculatedData,

  deleteSlowdownData,
  deleteShutdownData,
  deleteTurnAroundData,
  deleteBusinessDemandData,
  handleRefresh,
  handleCalculate,
  handleCalculateNormalOpsNorms,
  handleCalculateonsumptionNorms,
  handleCalculateProductionVolData,
  handleCalculateProductionVolData2,
  handleCalculateMonthwiseAndTurnaround,
  handleCalculatePlantProductionData,
  handleCalculateMonthwiseProduction,
  handleCalculatePlantConsumptionData,
  calculateTurnAroundPlanReportData,
  calculateAnnualProductionPlanData,

  calculatePlantContributionReportData,

  handleCalculateMaintenance,
  getNormalOperationNormsData,
  getIntermediateValues,
  getShutdownNormsData,
  getSlowdownNormsData,

  handleCalculateConsumptionNorm1,
  handleCalculateNormalOpsNorms1,
  handleCalculateNormalOperationNorms,

  handleCalculateShutdownNorms,
  handleCalculateSlowdownNorms,
  updatePeConfigData,
  getPeConfigData,
  getAllGrades,
  getHeaderData,

  getShutdownMonths,
  getSlowdownMonths,
  getUsersData,
  getUserRoles,
  // getUserAllData,

  getAopyears,
  updateUserAttributes,
  getUserBySearch,
  getUserScreen,
  getScreenbyPlant,
  getWorkflowData,
  getWorkflowDataProduction,
  getAnnualCostAopReport,
  getProductionVolDataBasis,
  getNormsHistorianBasis,
  getAnnualProductionPlanReportData,
  getPlantProductionSummary,
  getPlantContributionYearWisePlan,
  getMonthwiseRawData,
  postMonthwiseRawData,
  getMonthWiseSummary,
  updateUserPlants,
  getCaseId,
  saveworkflow,
  submitWorkFlow,
  getExcel,
  saveConfigurationExcel,
  saveNormalOpsNormsExcel,
  getConfigurationExcel,
  getNormalOpsNormsExcel,

  executeConfiguration,
  getConfigurationExecutionDetails,
  saveProductionVolDataExcel,
  getProductionVolExcel,

  handleCalculateNormalOperationNormsPe,
}

async function handleRefresh(year, plantId, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/handleRefresh?year=${year}&plantId=${plantId}`

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
      throw new Error(
        `Failed to delete data: ${resp.status} ${resp.statusText}`,
      )
    }
    return await resp.text() // Handle text response from the backend
  } catch (e) {
    console.error('Error deleting slowdown data:', e)
    return Promise.reject(e)
  }
}
async function handleCalculate(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/calculateData?year=${year1}&plantId=${plantId}`
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
async function handleCalculateNormalOpsNorms1(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/getCalculatedNormalOpsNorms?year=${year1}&plantId=${plantId}`
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
async function handleCalculateNormalOperationNorms(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handleCalculateNormalOpsNorms?year=${year1}&plantId=${plantId}`
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
async function handleCalculateShutdownNorms(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  //  const url = `${Config.CaseEngineUrl}/task/getCalculatedShutdownNorms?year=${year1}&plantId=${plantId}`
  const url = `${Config.CaseEngineUrl}/task/getShutdownNormsSPData?year=${year1}&plantId=${plantId}`
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

    const data = await resp.json()
    return data
  } catch (e) {
    console.error('Error fetching calculation data:', e)
    return Promise.reject(e)
  }
}

async function handleCalculateSlowdownNorms(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  //  const url = `${Config.CaseEngineUrl}/task/getCalculatedShutdownNorms?year=${year1}&plantId=${plantId}`
  const url = `${Config.CaseEngineUrl}/task/getSlowdownNormsSPData?year=${year1}&plantId=${plantId}`
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

    const data = await resp.json()
    return data
  } catch (e) {
    console.error('Error fetching calculation data:', e)
    return Promise.reject(e)
  }
}

async function handleCalculateConsumptionNorm1(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/getCalculatedConsumptionNorms?year=${year1}&plantId=${plantId}`
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

async function handleCalculateNormalOpsNorms(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/getShutdownNormsSPData?year=${year1}&plantId=${plantId}`

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
async function handleCalculateonsumptionNorms(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handleCalculateonsumptionNorms?year=${year1}&plantId=${plantId}`

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

async function handleCalculateProductionVolData(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/getAOPMCCalculatedDataSP?year=${year1}&plantId=${plantId}`

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
async function handleCalculateProductionVolData2(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/work-flow?year=${year1}&plantId=${plantId}`

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
async function handleCalculateMonthwiseAndTurnaround(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/monthwise-turnaround?year=${year1}&plantId=${plantId}`

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
async function handleCalculatePlantProductionData(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/plant-production-summary?year=${year1}&plantId=${plantId}`

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

async function handleCalculateMonthwiseProduction(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/monthwise-production-data?year=${year1}&plantId=${plantId}`

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
async function handleCalculatePlantConsumptionData(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/plant-consumption-data?year=${year1}&plantId=${plantId}`

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
async function calculateTurnAroundPlanReportData(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/turnarounf-plan-data?year=${year1}&plantId=${plantId}`

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
async function calculateAnnualProductionPlanData(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/annual-production-data?year=${year1}&plantId=${plantId}`

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

async function calculatePlantContributionReportData(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handle/calculate/plan-contribution-data?year=${year1}&plantId=${plantId}`

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

async function handleCalculateMaintenance(plantId, year, keycloak) {
  const year1 = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/handleCalculateMaintenance?year=${year1}&plantId=${plantId}`

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

async function deleteSlowdownData(maintenanceId, keycloak) {
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/deleteSlowdownData/${maintenanceId}/${plantId}`

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

    return await resp.text() // Handle text response from the backend
  } catch (e) {
    console.error('Error deleting slowdown data:', e)
    return Promise.reject(e)
  }
}
async function deleteShutdownData(maintenanceId, keycloak) {
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/deleteShutdownData/${maintenanceId}/${plantId}`

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

    return await resp.text() // Handle text response from the backend
  } catch (e) {
    console.error('Error deleting slowdown data:', e)
    return Promise.reject(e)
  }
}
async function deleteTurnAroundData(maintenanceId, keycloak) {
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/deleteTurnaroundData/${maintenanceId}?plantId=${plantId}`

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

    return await resp.text() // Handle text response from the backend
  } catch (e) {
    console.error('Error deleting slowdown data:', e)
    return Promise.reject(e)
  }
}
async function deleteBusinessDemandData(maintenanceId, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/deleteBusinessDemandData/${maintenanceId}`

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

    return await resp.text() // Handle text response from the backend
  } catch (e) {
    console.error('Error deleting slowdown data:', e)
    return Promise.reject(e)
  }
}
async function updateBusinessDemandDataM(maintenanceId, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/editBusinessDemandData/${maintenanceId}`

  const headers = {
    Accept: 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'UPDATE',
      headers,
    })

    if (!resp.ok) {
      throw new Error(`Failed to edit data: ${resp.status} ${resp.statusText}`)
    }

    return await resp.text() // Handle text response from the backend
  } catch (e) {
    console.error('Error Editing Business data:', e)
    return Promise.reject(e)
  }
}
async function updatePeConfigData(keycloak, payload) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/updatePeConfigData?year=${year}&plantId=${plantId}`

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

    if (!resp.ok) {
      throw new Error(`Failed to edit data: ${resp.status} ${resp.statusText}`)
    }

    return await resp.text() // Handle text response from the backend
  } catch (e) {
    console.error('Error Editing Config data:', e)
    return Promise.reject(e)
  }
}

async function getExcel(keycloak, payload) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/export-excel?year=${year}&plantId=${plantId}`

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

async function getProductById(keycloak) {
  const url = `${Config.CaseEngineUrl}/task/productList`

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
async function getBDData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/getBusinessDemandData?year=${year}&plantId=${plantId}`
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
async function getPeConfigData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/getPeConfigData?year=${year}&plantId=${plantId}`
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
async function getAllGrades(keycloak) {
  // var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/getAllGrades?plantId=${plantId}`
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
async function getUsersData(keycloak) {
  const url = `${Config.CaseEngineUrl}/task/users`
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
async function getUserRoles(keycloak) {
  const url = `${Config.CaseEngineUrl}/task/users/roles`
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
async function getUserScreen(keycloak, verticalId) {
  const url = `${Config.CaseEngineUrl}/task/screen-mapping?verticalId=${verticalId}`
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
async function getScreenbyPlant(keycloak, verticalId, plantId) {
  const url = `${Config.CaseEngineUrl}/task/user/screen?verticalId=${verticalId}&plantId=${plantId}`
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
async function getWorkflowData(keycloak, plantId) {
  let year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/work-flow?plantId=${plantId}&year=${year}`
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

async function getAnnualCostAopReport(
  keycloak,
  reportType = 'production',
  aopYearFilter1 = 'null',
) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  var url = ''
  if (reportType == 'aopYearFilter') {
    url = `${Config.CaseEngineUrl}/task/report/annual-aop?plantId=${plantId}&year=${year}&reportType=${reportType}&aopYearFilter=null`
  } else {
    url = `${Config.CaseEngineUrl}/task/report/annual-aop?plantId=${plantId}&year=${year}&reportType=${reportType}&aopYearFilter=${aopYearFilter1}`
  }

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
async function getProductionVolDataBasis(keycloak, reportType) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const url = `${Config.CaseEngineUrl}/task/report/production-volume-aop?plantId=${plantId}&year=${year}&reportType=${reportType}`

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

async function getNormsHistorianBasis(keycloak, reportType) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const url = `${Config.CaseEngineUrl}/task/report/norms-Historian-basis?plantId=${plantId}&year=${year}&reportType=${reportType}`

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

async function getAnnualProductionPlanReportData(keycloak, type) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const url = `${Config.CaseEngineUrl}/task/report/plant/production/plan?plantId=${plantId}&year=${year}&reportType=${type}`
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
async function getPlantContributionYearWisePlan(keycloak, type) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const url = `${Config.CaseEngineUrl}/task/report/plant/contribution/year-wise/plan?plantId=${plantId}&year=${year}&reportType=${type}`
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
async function getPlantProductionSummary(keycloak) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/report/production-summary?plantId=${plantId}&year=${year}`
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
async function getMonthwiseRawData(keycloak, reportType) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/report/month-wise/consumption-summary?plantId=${plantId}&year=${year}&reportType=${reportType}`
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

async function postMonthwiseRawData(keycloak,dataList, plantId) {
const year = localStorage.getItem('year')

  const url = `${Config.CaseEngineUrl}/task/report/month-wise/consumption-summary?plantId=${plantId}&year=${year}`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
     // 🔍 Log payload before sending to backend
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

async function getMonthWiseSummary(keycloak) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/report/month-wise/production?plantId=${plantId}&year=${year}&typeOne=${'month'}&typeSecond=${'year'}`
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

async function getWorkflowDataProduction(keycloak, plantId) {
  let year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/production-aop/work-flow?plantId=${plantId}&year=${year}`
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
// async function getUserBySearch(keycloak, searchKey) {
//   const url = `${Config.CaseEngineUrl}/task/users/search?search=${searchKey}`
//   const headers = {
//     Accept: 'application/json',
//     'Content-Type': 'application/json',
//     Authorization: `Bearer ${keycloak.token}`,
//   }

//   try {
//     const resp = await fetch(url, { method: 'GET', headers })
//     return json(keycloak, resp)
//   } catch (e) {
//     console.log(e)
//     return await Promise.reject(e)
//   }
// }
async function getUserBySearch(keycloak, searchKey) {
  const url = `${Config.CaseEngineUrl}/task/users/search?search=${encodeURIComponent(searchKey)}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })

    // Optional: check response directly here
    if (!resp.ok) {
      throw new Error(`API Error: ${resp.status} ${resp.statusText}`)
    }

    return await resp.json()
    // OR return await json(keycloak, resp) if using a wrapper
  } catch (e) {
    console.error('Search user API failed:', e)
    return Promise.reject(e)
  }
}

async function updateUserAttributes(keycloak, userId) {
  const url = `${Config.CaseEngineUrl}/task/users/${userId}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'PUT', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function getMaintenanceData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/getMaintenanceCalculatedData?year=${year}&plantId=${plantId}`
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
async function getNormalOperationNormsData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  // let siteID =
  //   JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

  const url = `${Config.CaseEngineUrl}/task/normalOperationNorms?year=${year}&plantId=${plantId}`
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
async function getIntermediateValues(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  // let siteID =
  //   JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

  // value = '/get/configuration/intermediate-values'

  const url = `${Config.CaseEngineUrl}/task/get/configuration/intermediate-values?year=${year}&plantFKId=${plantId}`
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
async function getShutdownNormsData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  // let siteID =
  //   JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

  const url = `${Config.CaseEngineUrl}/task/shutdownNorms?year=${year}&plantId=${plantId}`
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
async function getSlowdownNormsData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  // let siteID =
  //   JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

  const url = `${Config.CaseEngineUrl}/task/slowdownNorms?year=${year}&plantId=${plantId}`
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

async function getCatalystSelectivityData(keycloak) {
  var plantId = ''

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  // let siteID =
  //   JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

  var year = localStorage.getItem('year')

  //const url = `${process.env.REACT_APP_API_URL}/task/getConfigurationData?year=${year}&plantFKId=${plantId}`
  const url = `${Config.CaseEngineUrl}/task/getConfigurationData?year=${year}&plantFKId=${plantId}`

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
async function getCatalystSelectivityDataConstants(keycloak) {
  var plantId = ''

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  var year = localStorage.getItem('year')

  //const url = `${process.env.REACT_APP_API_URL}/task/getConfigurationData?year=${year}&plantFKId=${plantId}`
  const url = `${Config.CaseEngineUrl}/task/configuration-constants?year=${year}&plantFKId=${plantId}`

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

async function getConfigurationTabsMatrix(keycloak) {
  let plantId = ''
  let siteID =
    JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''
  let verticalId = localStorage.getItem('verticalId')

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/access/matrix?plantId=${plantId}&siteId=${siteID}&verticalId=${verticalId}`

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

async function getAopSummary(keycloak) {
  let plantId = ''

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  let aopYear = localStorage.getItem('year')

  const url = `${Config.CaseEngineUrl}/task/aop-summary?plantId=${plantId}&aopYear=${aopYear}`

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
async function getConfigurationAvailableTabs(keycloak) {
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
async function getCatalystSelectivityDataIV(keycloak) {
  var plantId = ''

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  // let siteID =
  //   JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

  var year = localStorage.getItem('year')

  //const url = `${process.env.REACT_APP_API_URL}/task/getConfigurationData?year=${year}&plantFKId=${plantId}`
  const url = `${Config.CaseEngineUrl}/task/configuration/intermediate-values?year=${year}&plantFKId=${plantId}`

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
async function getProductionNormsData(keycloak) {
  var plantId = ''

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  let siteID =
    JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

  const url = `${Config.CaseEngineUrl}/task/getProductionNormData?year=2024&plantId=${plantId}&siteId=${siteID}`

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
async function getConsumptionNormsData(keycloak) {
  var plantId = ''

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  // let siteID =
  //   JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

  var year = localStorage.getItem('year')

  const url = `${Config.CaseEngineUrl}/task/getAOPConsumptionNorm?year=${year}&plantId=${plantId}`

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

async function saveShutdownData(plantId, shutdownDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/saveShutdownData/${plantId}`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(shutdownDetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function saveAnnualWorkFlowData(keycloak, workFlowData) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const url = `${Config.CaseEngineUrl}/task/annual-aop-data?plantId=${plantId}`

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
async function saveRemarkData(keycloak, workFlowData) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const url = `${Config.CaseEngineUrl}/task/annual-aop-remark?plantId=${plantId}`

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

async function savePlantProductionData(keycloak, dataList) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage?.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/plant-production-data?plantId=${plantId}&year=${year}`

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
async function saveMonthwiseProduction(keycloak, dataList) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage?.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/monthwise-production-data?plantId=${plantId}&year=${year}`

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
async function saveTurnaroundReport(keycloak, dataList) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage?.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/turnaround-data?plantId=${plantId}&year=${year}`

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
async function saveTurnaroundReportWhole(keycloak, dataList) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage?.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/report/turn-around?plantId=${plantId}&year=${year}&reportType=${'previousYear'}`
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
async function saveText(submitedText, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/saveText`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(submitedText),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function saveworkflow(data, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/saveWorkflow`

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

async function getCaseId(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  var siteId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const storedSite = localStorage.getItem('selectedSite')
  if (storedSite) {
    const parsedSite = JSON.parse(storedSite)
    siteId = parsedSite.id
  }

  const verticalId = localStorage.getItem('verticalId')

  const url = `${Config.CaseEngineUrl}/task/getCaseId?plantId=${plantId}&year=${year}&siteId=${siteId}&verticalId=${verticalId}`

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

async function saveAOPConsumptionNorm(plantId, shutdownDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/saveAOPConsumptionNorm`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(shutdownDetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function saveSummaryAOPConsumptionNorm(
  plantId,
  aopYear,
  summary,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/aop-summary?plantId=${plantId}&aopYear=${aopYear}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify({ summary }), // <-- send JSON object with summary key
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function saveSlowdownData(plantId, slowDownDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/saveSlowdownData/${plantId}`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(slowDownDetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function updateSlowdownData(maintenanceId, slowDownDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/editSlowdownData/${maintenanceId}`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'PUT', // Changed from POST to PUT
      headers,
      body: JSON.stringify(slowDownDetails),
    })

    if (!resp.ok) {
      throw new Error(
        `Failed to update data: ${resp.status} ${resp.statusText}`,
      )
    }

    return await resp.json() // Ensure proper response handling
  } catch (e) {
    console.error('Error updating slowdown data:', e)
    return Promise.reject(e)
  }
}
async function updateUserPlants(keycloak, payload) {
  const url = `${Config.CaseEngineUrl}/task/users`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'PUT',
      headers,
      body: JSON.stringify(payload),
    })

    if (!resp.ok) {
      throw new Error(`API Error: ${resp.status} ${resp.statusText}`)
    }

    return await resp.json()
  } catch (e) {
    console.error('Update user plants API failed:', e)
    return Promise.reject(e)
  }
}

async function updateShutdownData(maintenanceId, slowDownDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/editShutdownData/${maintenanceId}`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'PUT', // Changed from POST to PUT
      headers,
      body: JSON.stringify(slowDownDetails),
    })

    if (!resp.ok) {
      throw new Error(
        `Failed to update data: ${resp.status} ${resp.statusText}`,
      )
    }

    return await resp.json() // Ensure proper response handling
  } catch (e) {
    console.error('Error updating shutdown data:', e)
    return Promise.reject(e)
  }
}

async function updateTurnAroundData(
  maintenanceId,
  turnAroundDetails,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/editTurnaroundData/${maintenanceId}` // Corrected endpoint

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'PUT', // Ensure it matches @PutMapping
      headers,
      body: JSON.stringify(turnAroundDetails), // Updated variable name for clarity
    })

    if (!resp.ok) {
      throw new Error(
        `Failed to update data: ${resp.status} ${resp.statusText}`,
      )
    }

    return await resp.json() // Ensure proper response handling
  } catch (e) {
    console.error('Error updating turnaround data:', e)
    return Promise.reject(e)
  }
}

async function updateProductNormData(turnAroundDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/updateAOP` // Corrected endpoint

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'PUT', // Ensure it matches @PutMapping
      headers,
      body: JSON.stringify(turnAroundDetails), // Updated variable name for clarity
    })

    if (!resp.ok) {
      throw new Error(
        `Failed to update data: ${resp.status} ${resp.statusText}`,
      )
    }

    return await resp.json() // Ensure proper response handling
  } catch (e) {
    console.error('Error updating turnaround data:', e)
    return Promise.reject(e)
  }
}

async function saveTurnAroundData(plantId, turnAroundDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/saveTurnaroundPlanData/${plantId}`

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

async function saveCatalystData(plantId, turnAroundDetails, keycloak) {
  var year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/saveConfigurationData?year=${year}&plantFKId=${plantId}`

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

async function saveBusinessDemandData(plantId, turnAroundDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/saveBusinessDemandData`

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
async function saveSpyroInput(payload, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/spyro-input`
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
async function saveSpyroOutput(payload, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/spyro-output`
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
async function getSpyroOutputData(keycloak, mode) {
  const year = localStorage.getItem('year')
  let plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/spyro-output?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&Mode=${encodeURIComponent(mode)}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.error('Failed to fetch spyro-output data', e)
    return Promise.reject(e)
  }
}

async function saveNormalOperationNormsData(
  plantId,
  turnAroundDetails,
  keycloak,
) {
  var year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/normalOperationNorms?year=${year}&plantId=${plantId}`
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
async function saveShutDownNormsData(plantId, turnAroundDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/shutdownNorms`

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
async function saveSlowdownNormsData(plantId, turnAroundDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/slowdownNorms`

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

// Config.CaseEngineUrl

async function editAOPMCCalculatedData(plantId, turnAroundDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/editAOPMCCalculatedData`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'PUT',
      headers,
      body: JSON.stringify(turnAroundDetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

export default saveShutdownData

async function getYearlyData(keycloak, year) {
  const url = `${Config.CaseEngineUrl}/task/yearly-data?year=${year}`

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

async function getYearWiseProduct(keycloak) {
  var type = 'Business Demand Data'
  var year = '2025'
  const url = `${Config.CaseEngineUrl}/task/getMonthWiseData?type=${type}&year=${year}`

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

async function getAllSites(keycloak) {
  const url = `${Config.CaseEngineUrl}/task/getPlantsAndSidesAndVerticals`
  // const url = `${Config.CaseEngineUrl}/task/getPlantAndSite`

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
async function getNormTransactions(keycloak) {
  var year = localStorage.getItem('year')
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)

  const url = `${Config.CaseEngineUrl}/task/norms-transactions?plantId=${parsedPlant?.id}&year=${year}`
  // const url = `${Config.CaseEngineUrl}/task/getPlantAndSite`

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

async function getAopyears(keycloak) {
  // const storedPlant = localStorage.getItem('selectedPlant')
  // const parsedPlant = JSON.parse(storedPlant)
  // const url = `${Config.CaseEngineUrl}/task/getAllProducts?normParameterTypeName=${type}&plantId=${parsedPlant.id}`
  const url = `${Config.CaseEngineUrl}/task/aop-years`

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
async function getAllProducts(keycloak) {
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)
  // const url = `${Config.CaseEngineUrl}/task/getAllProducts?normParameterTypeName=${type}&plantId=${parsedPlant.id}`
  const url = `${Config.CaseEngineUrl}/task/getAllProducts?normParameterTypeName=null&plantId=${parsedPlant.id}`

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

async function getShutdownMonths(keycloak) {
  var year = localStorage.getItem('year')

  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)
  // const url = `${Config.CaseEngineUrl}/task/getAllProducts?normParameterTypeName=${type}&plantId=${parsedPlant.id}`
  // http://localhost:8080/task/shutdown-months?plantId=AACDBE12-C5F6-4B79-9C88-751169815B42&MaintenanceName=Shutdown
  const url = `${Config.CaseEngineUrl}/task/shutdown-months?plantId=${parsedPlant.id}&maintenanceName=Shutdown&year=${year}`

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

async function getSlowdownMonths(keycloak) {
  const storedPlant = localStorage.getItem('selectedPlant')
  var year = localStorage.getItem('year')

  const parsedPlant = JSON.parse(storedPlant)
  const url = `${Config.CaseEngineUrl}/task/slowdown-months?plantId=${parsedPlant.id}&maintenanceName=Slowdown&year=${year}`
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
async function getHeaderData(keycloak, screenName) {
  const verticalId = localStorage.getItem('verticalId')
  const url = `${Config.CaseEngineUrl}/task/getHeaderData?verticalId=${verticalId}&screenName=${screenName}`

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
async function getAllProductsAll(keycloak, type) {
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)
  const url = `${Config.CaseEngineUrl}/task/getAllProducts?normParameterTypeName=${type}&plantId=${parsedPlant.id}`
  // const url = `${Config.CaseEngineUrl}/task/getAllProducts?normParameterTypeName=null&plantId=${parsedPlant.id}`

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

async function getIbrPlanData(keycloak) {
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)

  const url = `${Config.CaseEngineUrl}/task/getIbrPlanData?plantId=${parsedPlant.id}`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const response = await fetch(url, { headers })
    if (!response.ok) throw new Error('Failed to fetch IBR plan data')
    return await response.json()
  } catch (error) {
    console.error('Error in getIbrPlanData:', error)
    throw error
  }
}

async function getShutdownActivitiesData(keycloak) {
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)

  const url = `${Config.CaseEngineUrl}/task/getShutdownActivities?plantId=${parsedPlant.id}`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const response = await fetch(url, { headers })
    if (!response.ok)
      throw new Error('Failed to fetch shutdown activities data')
    return await response.json()
  } catch (error) {
    console.error('Error in getShutdownActivitiesData:', error)
    throw error
  }
}

async function getRunningDurationData(keycloak) {
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)
  const url = `${Config.CaseEngineUrl}/task/getRunningDurationData?plantId=${parsedPlant.id}`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.error(e)
    return await Promise.reject(e)
  }
}


async function getAllCatalyst(keycloak) {
  const url = `${Config.CaseEngineUrl}/task/getAllCatalystAttributes`

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

async function getShutDownPlantData(keycloak) {
  var maintenanceTypeName = 'Shutdown'
  var year = localStorage.getItem('year')
  var plantId = ''

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  // plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D'
  // plantId = plantId

  const url = `${Config.CaseEngineUrl}/task/getShutDownPlanData?plantId=${plantId}&maintenanceTypeName=${maintenanceTypeName}&year=${year}`

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

async function getSlowDownPlantData(keycloak) {
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const maintenanceTypeName = 'Slowdown' // Assuming the maintenance type is 'Slowdown'
  var year = localStorage.getItem('year')

  // const storedPlant = localStorage.getItem('selectedPlant')
  // if (storedPlant) {
  //   const parsedPlant = JSON.parse(storedPlant)
  //   plantId= (parsedPlant.id)
  // }

  const url = `${Config.CaseEngineUrl}/task/getSlowDownPlanData?plantId=${plantId}&maintenanceTypeName=${maintenanceTypeName}&year=${year}`

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

async function getAOPData(keycloak, type) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/getAOP?plantId=${plantId}&year=${year}&type=${type}`
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
async function getSpyroInputData(keycloak, mode) {
  const year = localStorage.getItem('year')
  let plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/spyro-input?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&Mode=${encodeURIComponent(mode)}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.error('Failed to fetch spyro-input data', e)
    return Promise.reject(e)
  }
}

async function getAOPMCCalculatedData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/getAOPMCCalculatedData?plantId=${plantId}&year=${year}`
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

async function getTAPlantData(keycloak) {
  // const plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D';

  var plantId = ''

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  // const plantId = '3E3FDF54-391D-4BAB-A78F-50EBCA9FBEA6'
  const maintenanceTypeName = 'TA_Plan' // Assuming the maintenance type is 'Shutdown'
  var year = localStorage.getItem('year')

  // const storedPlant = localStorage.getItem('selectedPlant')
  // if (storedPlant) {
  //   const parsedPlant = JSON.parse(storedPlant)
  //   plantId= (parsedPlant.id)
  // }

  const url = `${Config.CaseEngineUrl}/task/getTurnaroundPlanData?plantId=${plantId}&maintenanceTypeName=${maintenanceTypeName}&year=${year}`

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

// async function getMonthWiseData(keycloak) {
//   const url = `${Config.CaseEngineUrl}/getMonthWiseData`

//   const headers = {
//     Accept: 'application/json',
//     'Content-Type': 'application/json',
//     Authorization: `Bearer ${keycloak.token}`,
//   }

//   try {
//     const resp = await fetch(url, { method: 'GET', headers })
//     return json(keycloak, resp)
//   } catch (e) {
//     console.log(e)
//     return await Promise.reject(e)
//   }
// }

// New API function: Create a case
async function createCase(keycloak, caseData) {
  // Assuming Config.CaseEngineUrl is set to http://localhost:8081 or similar
  const url = `${Config.CaseEngineUrl}/case`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(caseData),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

// New API function: Get tasks by businessKey
async function getTasksByBusinessKey(keycloak, businessKey) {
  const url = `${Config.CaseEngineUrl}/task?businessKey=${businessKey}`
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
// New API function: Get process instance variables by processInstanceId
async function getProcessInstanceVariables(keycloak, processInstanceId) {
  const url = `${Config.CaseEngineUrl}/variable?processInstanceId=${processInstanceId}`
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

// async function completeTask(keycloak, taskId, payload) {
//   const url = `${Config.CaseEngineUrl}/task/${taskId}/complete`
//   const headers = {
//     Accept: 'application/json',
//     'Content-Type': 'application/json',
//     Authorization: `Bearer ${keycloak.token}`,
//   }
//   try {
//     const resp = await fetch(url, {
//       method: 'POST',
//       headers,
//       body: JSON.stringify(payload),
//     })
//     return json(keycloak, resp)
//   } catch (e) {
//     console.log(e)
//     return await Promise.reject(e)
//   }
// }
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

async function getTurnaroundReportData(keycloak, type) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const url = `${Config.CaseEngineUrl}/task/report/turn-around?plantId=${plantId}&year=${year}&reportType=${type}`
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

async function saveConfigurationExcel(file, keycloak) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/configuration-import-excel?plantId=${plantId}&year=${year}`

  const formData = new FormData()
  formData.append('file', file)

  const headers = {
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: formData,
    })


    if (!resp.ok) {
      throw new Error(`Failed to edit data: ${resp.status} ${resp.statusText}`)
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'ConfigurationResponse.xlsx' // Filename to save
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing Config data:', e)
    return Promise.reject(e)
  }
}
async function saveNormalOpsNormsExcel(file, keycloak) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/norms-import-excel?plantId=${plantId}&year=${year}`

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
async function saveProductionVolDataExcel(file, keycloak) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/production-volume-data/import/excel?plantId=${plantId}&year=${year}`

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

async function getConfigurationExcel(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  const url = `${Config.CaseEngineUrl}/task/configuration-export-excel?year=${year}&plantId=${plantId}`
  //const url = `${Config.CaseEngineUrl}/task/norms-export-excel?year=${year}&plantId=${plantId}`

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
    a.download = 'Configuration.xlsx' // Filename to save
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing Config data:', e)
    return Promise.reject(e)
  }
}
async function getNormalOpsNormsExcel(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  // const url = `${Config.CaseEngineUrl}/task/configuration-export-excel?year=${year}&plantId=${plantId}`
  const url = `${Config.CaseEngineUrl}/task/norms-export-excel?year=${year}&plantId=${plantId}`

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
    a.download = 'Normal Ops Norms.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing Config data:', e)
    return Promise.reject(e)
  }
}
async function getProductionVolExcel(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${Config.CaseEngineUrl}/task/production-volume-data/export/excel?year=${year}&plantId=${plantId}`

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
    a.download = 'Production Vol Data.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing Config data:', e)
    return Promise.reject(e)
  }
}

async function executeConfiguration(executionDetailDtoList, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/configuration-execution`

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(executionDetailDtoList),
    })

    if (!resp.ok) {
      throw new Error(`HTTP error! Status: ${resp.status}`)
    }

    const data = await resp.json()
    return data
  } catch (e) {
    console.error('Error saving configuration execution:', e)
    return Promise.reject(e)
  }
}

async function getConfigurationExecutionDetails(keycloak) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')
  const url = `${Config.CaseEngineUrl}/task/configuration-execution?plantId=${plantId}&year=${year}`
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
