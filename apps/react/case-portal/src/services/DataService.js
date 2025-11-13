import Config from '../consts'
import { json } from './request'

export const DataService = {
  getProductById,
  getYearWiseProduct,
  getAllSites,
  getShutDownPlantData,
  getAllProducts,
  getAllProductsAll,
  getYearlyData,
  getSlowDownPlantData,
  getSlowDownConfigurationData,
  getTAPlantData,
  getCatalystSelectivityData,
  getCatalystSelectivityDataConstants,
  getConfigurationTabsMatrix,
  getConfigurationAvailableTabs,
  getTurnaroundReportData,
  getAopSummary,
  getAllCatalyst,
  saveShutdownData,
  saveAnnualWorkFlowData,
  savePlantProductionData,
  saveMonthwiseProduction,
  saveTurnaroundReport,
  saveTurnaroundReportWhole,
  saveText,
  saveSummaryAOPConsumptionNorm,
  saveSlowdownData,
  saveTurnAroundData,
  saveCatalystData,
  saveSpyroInput,
  saveSpyroOutput,
  getSpyroOutputData,
  saveSlowdownNormsData,
  updateSlowdownData,
  updateShutdownData,
  updateTurnAroundData,
  updateUserPlantsForRevokeAccess,
  createCase,
  getTasksByBusinessKey,
  getProcessInstanceVariables,
  completeTask,
  getSpyroInputData,
  deleteSlowdownData,
  deleteShutdownData,
  deleteTurnAroundData,
  handleRefresh,

  handleCalculateProductionVolData2,
  handleCalculateAnnualAopCostMiisContribution,

  handleCalculatePlantProductionData,
  handleCalculateMonthwiseProduction,
  handleCalculatePlantConsumptionData,
  calculateTurnAroundPlanReportData,
  calculateAnnualProductionPlanData,
  calculatePlantContributionReportData,

  getSlowdownNormsData,

  handleCalculateSlowdownNorms,
  handleCalculateSlowdownNormsPP,
  updatePeConfigData,
  getPeConfigData,
  getAllGrades,

  getSlowdownMonths,
  getUsersData,
  getUserRoles,
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
  saveConfigurationExcelConstants,
  importSpyroOutputExcel,
  importSpyroOutputExcelYield,
  exportSpyroOutputExcel,
  exportSpyroOutputExcelYield,
  importSpyroInputExcel,
  exportSpyroInputExcel,
  getConfigurationExcel,
  executeConfiguration,
  getConfigurationExecutionDetails,
  getConfigurationExcelConstants,
  savePlantContributionData,
  savePlantContributionlastfourData,
  getProductionVolDataBasisPe,
  getBestAchievedNorms,
  getProductionVolDataBasisMode,
  saveSlowdownConfigData,
  deleteTurnArondReportItem,
  getIbr,
  saveAnnualProduction,
  getIbrSdTa,
  getIbrScreen3,
  saveCrackerRunLength,
  getRunLengthExcel,
  saveRunLengthExcel,
  handleCalculateDecokingActivities,
  handleCalculateSdTaActivities,
  getSlowDownPlantDataTab,
  postIbr,
  getSpyroOutputDataYield,
  saveSpyroOutputYield,
  getCrackerNextYearParameters,
  getCrackerNextYearData,
  calculateNormsHistorianValues,

  plantContributionPlanLastFourYears,
  calculatePlantContributionSummaryYearly,
  getRecipeExcel,
  saveRecipeExcel,
  getShutdownRateExcel,
  saveShutdownRateExcel,
  getConfigurationExecutionDetailsNorms,
  executeConfigurationNorms,
  getProductionTargetBasis,
  ImportShutdownDetails,
  ImportShutdownElastomerDetails,
  shutdownDetailsExport,
  shutdownDetailsElastomerExport,
  slowdownDetailsExport,
  slowdownDetailsElastomerExport,
  ImportSlowdownDetails,
  ImportSlowdownElastomerDetails,
  getConfigurationExcelType,

  getProductionReports,
  gradeDetails,
  carryForwardRecords,
  getSpecificConsumption,
  calculatePlantContributionBusinessDemand,
  dropdownValues,
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

async function handleCalculateSlowdownNorms(plantId, year, keycloak) {
  //  const url = `${Config.CaseEngineUrl}/task/getCalculatedShutdownNorms?year=${year1}&plantId=${plantId}`
  const url = `${Config.CaseEngineUrl}/task/getSlowdownNormsSPData?year=${year}&plantId=${plantId}`
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

async function handleCalculateSlowdownNormsPP(plantId, year, keycloak) {
  //  const url = `${Config.CaseEngineUrl}/task/getCalculatedShutdownNorms?year=${year1}&plantId=${plantId}`
  const url = `${Config.CaseEngineUrl}/task/calculate-slowdown-norms?plantId=${plantId}&year=${year}`
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

async function deleteSlowdownData(maintenanceId, keycloak, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/slowdown/${maintenanceId}/${PLANT_ID}`
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
async function deleteShutdownData(maintenanceId, keycloak, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/shutdown/${maintenanceId}/${PLANT_ID}`
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
async function deleteTurnAroundData(maintenanceId, keycloak, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/deleteTurnaroundData/${maintenanceId}?plantId=${PLANT_ID}`
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

async function updatePeConfigData(keycloak, payload, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/updatePeConfigData?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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

async function getPeConfigData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/getPeConfigData?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getAllGrades(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/getAllGrades?plantId=${PLANT_ID}`
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
async function getScreenbyPlant(keycloak, verticalId, plantId, userId) {
  let url = `${Config.CaseEngineUrl}/task/user/screen?verticalId=${verticalId}&plantId=${plantId}`
  if (userId) {
    url += `&userId=${userId}`
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
async function getAnnualCostAopReport(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  reportType = 'production',
  aopYearFilter1 = 'null',
) {
  var url = ''
  if (reportType == 'aopYearFilter') {
    url = `${Config.CaseEngineUrl}/task/report/annual-aop?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${reportType}&aopYearFilter=null`
  } else {
    url = `${Config.CaseEngineUrl}/task/report/annual-aop?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${reportType}&aopYearFilter=${aopYearFilter1}`
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
async function getProductionVolDataBasis(
  keycloak,
  reportType,
  uom,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/report/production-volume-aop?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${reportType}&uom=${uom}`
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
// @GetMapping(value="/report/norms-basis")
// 	public ResponseEntity<AOPMessageVM> getNormBasisReport(@RequestParam String plantId,@RequestParam String year,@RequestParam String type){
// 		AOPMessageVM response	=basisReportService.getNormBasisReport(plantId,year,type);
// 		return ResponseEntity.status(response.getCode()).body(response);
// 	}
async function getProductionVolDataBasisPe(
  keycloak,
  reportType,
  periodFrom,
  periodTo,
  mode,
  PLANT_ID,
  AOP_YEAR,
) {
  let url = `${Config.CaseEngineUrl}/task/data-set-norms-historian?plantId=${PLANT_ID}&year=${AOP_YEAR}&type=${reportType}`

  if (periodFrom !== undefined) {
    url += `&periodFrom=${periodFrom}`
  }

  if (periodTo !== undefined) {
    url += `&periodTo=${periodTo}`
  }

  if (mode !== undefined) {
    url += `&mode=${mode}`
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
    return Promise.reject(e)
  }
}
async function getBestAchievedNorms(keycloak, reportType, PLANT_ID, AOP_YEAR) {
  let url = `${Config.CaseEngineUrl}/task/report/best-achieved?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${reportType}`

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
    return Promise.reject(e)
  }
}
async function getProductionVolDataBasisMode(
  keycloak,
  reportType,
  periodFrom,
  periodTo,
  mode,
  PLANT_ID,
  AOP_YEAR,
) {
  let url = `${Config.CaseEngineUrl}/task/report/norms-basis/mode?plantId=${PLANT_ID}&year=${AOP_YEAR}&type=${reportType}`

  if (periodFrom !== undefined) {
    url += `&periodFrom=${periodFrom}`
  }

  if (periodTo !== undefined) {
    url += `&periodTo=${periodTo}`
  }

  if (mode !== undefined) {
    url += `&mode=${mode}`
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
    return Promise.reject(e)
  }
}

async function getNormsHistorianBasis(
  keycloak,
  reportType,
  uom,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/report/norms-Historian-basis?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${reportType}&uom=${uom}`
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
async function getPlantContributionYearWisePlan(
  keycloak,
  type,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/report/plant/contribution/year-wise/plan?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${type}`
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
async function getMonthWiseSummary(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/report/month-wise/production?plantId=${PLANT_ID}&year=${AOP_YEAR}&typeOne=${'month'}&typeSecond=${'year'}`
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

async function getSlowdownNormsData(keycloak, gradeId, PLANT_ID, AOP_YEAR) {
  let url
  if (gradeId) {
    url = `${Config.CaseEngineUrl}/task/slowdownNorms?year=${AOP_YEAR}&plantId=${PLANT_ID}&gradeId=${gradeId}`
  } else {
    url = `${Config.CaseEngineUrl}/task/slowdownNorms?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getCatalystSelectivityData(
  keycloak,
  GRADE_ID,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/production-norms?year=${AOP_YEAR}&plantFKId=${PLANT_ID}`
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
    return Promise.reject(e)
  }
}

async function getCatalystSelectivityDataConstants(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/configuration-constants?year=${AOP_YEAR}&plantFKId=${PLANT_ID}`
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
async function getConfigurationTabsMatrix(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  SITE_ID,
  VERTICAL_ID,
  type,
) {
  const params = new URLSearchParams({
    plantId: PLANT_ID,
    siteId: SITE_ID,
    verticalId: VERTICAL_ID,
  })
  if (type) params.append('type', type)
  const url = `${Config.CaseEngineUrl}/task/access/matrix?${params.toString()}`
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
async function getAopSummary(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/aop-summary?plantId=${PLANT_ID}&aopYear=${AOP_YEAR}`
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

async function saveShutdownData(plantId, shutdownDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/shutdown/${plantId}`
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
async function savePlantContributionData(
  keycloak,
  dataList,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/report/plant/contribution/year-wise/plan?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function savePlantContributionlastfourData(keycloak, dataList) {
  const url = `${Config.CaseEngineUrl}/task/report-plant-contribution-summary-yearly`
  //report-plant-contribution-summary-yearly
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
async function saveMonthwiseProduction(keycloak, dataList, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/monthwise-production-data?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function saveTurnaroundReport(keycloak, dataList, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/turnaround-data?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function saveTurnaroundReportWhole(
  keycloak,
  dataList,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/report/turn-around?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${'previousYear'}`
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

async function saveSummaryAOPConsumptionNorm(
  PLANT_ID,
  AOP_YEAR,
  summary,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/aop-summary?plantId=${PLANT_ID}&aopYear=${AOP_YEAR}`
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
async function saveSlowdownConfigData(
  plantId,
  year,
  slowDownConfigDetails,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/slowdown-configuration?plantId=${plantId}&year=${year}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(slowDownConfigDetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function saveSlowdownData(plantId, slowDownDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/slowdown/${plantId}`
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
async function updateUserPlantsForRevokeAccess(keycloak, payload, userId) {
  const url = `${Config.CaseEngineUrl}/task/users/revoke-access/${userId}`
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

async function saveTurnAroundData(PLANT_ID, turnAroundDetails, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/saveTurnaroundPlanData/${PLANT_ID}`
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
async function saveCatalystData(
  PLANT_ID,
  turnAroundDetails,
  keycloak,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/production-norms?year=${AOP_YEAR}&plantFKId=${PLANT_ID}`
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

async function saveSpyroInput(payload, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/spyro-input?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function saveSpyroOutput(payload, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/spyro-output?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function getSpyroOutputData(keycloak, mode, type, PLANT_ID, AOP_YEAR) {
  const url =
    `${Config.CaseEngineUrl}/task/spyro-output` +
    `?year=${encodeURIComponent(AOP_YEAR)}` +
    `&plantId=${encodeURIComponent(PLANT_ID)}` +
    `&Mode=${encodeURIComponent(mode)}` +
    `&type=${encodeURIComponent(type)}`

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
  //DELETE ME
  // const url = `${Config.CaseEngineUrl}/task/plant-site-vertical`
  const url = `${Config.CaseEngineUrl}/task/plant-site-vertical`
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
async function getAllProducts(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/norm-parameters?normParameterTypeName=null&plantId=${PLANT_ID}`
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

async function getSlowdownMonths(keycloak, gradeId, PLANT_ID, AOP_YEAR) {
  let url
  // const url = `${Config.CaseEngineUrl}/task/slowdown-months?plantId=${parsedPlant.id}&maintenanceName=Slowdown&year=${year}`

  if (gradeId) {
    url = `${Config.CaseEngineUrl}/task/slowdown-months?plantId=${PLANT_ID}&maintenanceName=Slowdown&year=${AOP_YEAR}&gradeId=${gradeId}`
  } else {
    url = `${Config.CaseEngineUrl}/task/slowdown-months?plantId=${PLANT_ID}&maintenanceName=Slowdown&year=${AOP_YEAR}`
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

async function getAllProductsAll(keycloak, type, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/norm-parameters?normParameterTypeName=${type}&plantId=${PLANT_ID}`
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
async function getShutDownPlantData(keycloak, PLANT_ID, AOP_YEAR) {
  var maintenanceTypeName = 'Shutdown'
  // plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D'
  // plantId = plantId
  const url = `${Config.CaseEngineUrl}/task/shutdown?plantId=${PLANT_ID}&maintenanceTypeName=${maintenanceTypeName}&year=${AOP_YEAR}`
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
async function getSlowDownConfigurationData(keycloak, PLANT_ID, AOP_YEAR) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/slowdown-configuration?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function getSlowDownPlantData(keycloak, PLANT_ID, AOP_YEAR) {
  const maintenanceTypeName = 'Slowdown' // Assuming the maintenance type is 'Slowdown'

  const url = `${Config.CaseEngineUrl}/task/slowdown?plantId=${PLANT_ID}&maintenanceTypeName=${maintenanceTypeName}&year=${AOP_YEAR}`
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

async function getSpyroInputData(keycloak, mode, type, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/spyro-input?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}&Mode=${encodeURIComponent(mode)}&type=${type}`
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

async function getTAPlantData(keycloak, PLANT_ID, AOP_YEAR) {
  const maintenanceTypeName = 'TA_Plan' // Assuming the maintenance type is 'Shutdown'
  const url = `${Config.CaseEngineUrl}/task/getTurnaroundPlanData?plantId=${PLANT_ID}&maintenanceTypeName=${maintenanceTypeName}&year=${AOP_YEAR}`
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
async function getTurnaroundReportData(keycloak, type, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/report/turn-around?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${type}`
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

async function saveConfigurationExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-import-excel?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
      throw new Error(`Failed to edit data: ${resp.status} ${resp.statusText}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error Editing Config data:', e)
    return Promise.reject(e)
  }
}

async function calculateNormsHistorianValues(
  plantId,
  year,
  periodFrom,
  periodTo,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/calculate-norms-historian-values?plantId=${plantId}&aopYear=${year}&periodFrom=${periodFrom}&periodTo=${periodTo}`
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

//----
async function importSpyroOutputExcel(
  file,
  keycloak,
  mode,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/spyro-output-import-excel?plantId=${PLANT_ID}&year=${AOP_YEAR}&mode=${encodeURIComponent(mode)}`
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
    return json(keycloak, resp) // assuming `json()` handles response properly
  } catch (e) {
    console.error('Error importing Optimizer Input Excel:', e)
    return await Promise.reject(e)
  }
}

async function importSpyroOutputExcelYield(
  file,
  keycloak,
  mode,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/yield-import?plantId=${PLANT_ID}&year=${AOP_YEAR}&mode=${encodeURIComponent(mode)}`
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
    return json(keycloak, resp) // assuming `json()` handles response properly
  } catch (e) {
    console.error('Error importing Optimizer Input Excel:', e)
    return await Promise.reject(e)
  }
}

async function exportSpyroOutputExcel(keycloak, mode, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/spyro-output-export-excel?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}&mode=${encodeURIComponent(mode)}`

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
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = `Optimizer_Output_${mode || 'Export'}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}

async function exportSpyroOutputExcelYield(keycloak, mode, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/yield-export?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}&mode=${encodeURIComponent(mode)}`

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
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = `SpyroOutput_${mode || 'Export'}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}
async function importSpyroInputExcel(file, keycloak, mode, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/spyro-input-import-excel?plantId=${PLANT_ID}&year=${AOP_YEAR}&mode=${encodeURIComponent(mode)}`
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
    return json(keycloak, resp) // assuming `json()` handles response properly
  } catch (e) {
    console.error('Error importing Optimizer Input Excel:', e)
    return await Promise.reject(e)
  }
}

async function exportSpyroInputExcel(keycloak, mode, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/spyro-input-export-excel?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}&mode=${encodeURIComponent(mode)}`

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
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = `Optimizer_Input_${mode || 'Export'}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}

//--
async function getConfigurationExcel(keycloak, reportType, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-export-excel?year=${AOP_YEAR}&plantId=${PLANT_ID}`

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  const body = JSON.stringify(reportType)

  try {
    const resp = await fetch(url, {
      method: 'POST', // changed from GET to POST since we’re sending a body
      headers,
      body,
    })

    if (!resp.ok) {
      throw new Error(
        `Failed to export data: ${resp.status} ${resp.statusText}`,
      )
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'Production & Norms Basis.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Config data:', e)
    return Promise.reject(e)
  }
}

async function getConfigurationExcelConstants(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-constants-export-excel?year=${AOP_YEAR}&plantFKId=${PLANT_ID}`

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
    a.download = 'Production & Norms Basis - Constants.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing data:', e)
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
async function executeConfigurationNorms(executionDetailDtoList, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/configuration-execution-norms`
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

async function getConfigurationExecutionDetails(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-execution?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function saveConfigurationExcelConstants(
  file,
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/configuration-constants-import-excel?plantFKId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function deleteTurnArondReportItem(maintenanceId, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/report/turn-around?id=${maintenanceId}`
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
async function getIbr(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/decoking-activities?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=RunningDuration`
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

async function saveAnnualProduction(payload, keycloak) {
  const { plantId, year, reportType, dataList } = payload
  let queryParams = `?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}`
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
async function getIbrSdTa(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/decoking-activities/ibr?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function postIbr(PLANT_ID, data, keycloak, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/decoking-activities/ibr?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function getIbrScreen3(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/decoking-activities?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=RunLength`
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
async function saveCrackerRunLength(PLANT_ID, data, keycloak, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/decoking-activities/run-length?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function getRunLengthExcel(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/run-length-export-excel?year=${AOP_YEAR}&plantId=${PLANT_ID}&reportType=RunLength`
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
    a.download = 'Run-Length.xlsx' // Filename to save
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing Config data:', e)
    return Promise.reject(e)
  }
}
async function saveRunLengthExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/run-length-import-excel?year=${AOP_YEAR}&plantId=${PLANT_ID}&reportType=RunLength`
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
      throw new Error(`Failed to edit data: ${resp.status} ${resp.statusText}`)
    }
    return json(keycloak, resp)
  } catch (e) {
    console.error('Error Editing RunlLength data:', e)
    return Promise.reject(e)
  }
}
//handleCalculateSdTaActivities
async function handleCalculateSdTaActivities(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/calculate/sd-ta-activities?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function handleCalculateDecokingActivities(PLANT_ID, AOP_YEAR, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/calculate/decoking-activities?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function getSlowDownPlantDataTab(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/slowdown-columns?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function getSpyroOutputDataYield(
  keycloak,
  mode,
  type,
  PLANT_ID,
  AOP_YEAR,
) {
  const url =
    `${Config.CaseEngineUrl}/task/spyro-output/yield` +
    `?year=${encodeURIComponent(AOP_YEAR)}` +
    `&plantId=${encodeURIComponent(PLANT_ID)}`

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

async function saveSpyroOutputYield(payload, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/spyro-output/yield?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function getCrackerNextYearParameters(
  keycloak,
  date,
  PLANT_ID,
  AOP_YEAR,
) {
  const url =
    `${Config.CaseEngineUrl}/task/next-year/configuration` +
    `?year=${encodeURIComponent(AOP_YEAR)}` +
    `&plantId=${encodeURIComponent(PLANT_ID)}` +
    `&startDate=${encodeURIComponent(date)}`

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
async function getCrackerNextYearData(keycloak, qParams, PLANT_ID, AOP_YEAR) {
  const NEXT_AOP_YEAR = AOP_YEAR?.replace(
    /(\d{4})-(\d{2})/,
    (_, a, b) => `${+a + 1}-${String((+b + 1) % 100).padStart(2, '0')}`,
  )

  const extraQueryString = Object.entries(qParams)
    .map(
      ([key, value]) =>
        `&${encodeURIComponent(key)}=${encodeURIComponent(value)}`,
    )
    .join('')

  const url =
    `${Config.CaseEngineUrl}/task/next-year/entry` +
    `?year=${encodeURIComponent(NEXT_AOP_YEAR)}` +
    `&plantId=${encodeURIComponent(PLANT_ID)}` +
    extraQueryString

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return json(keycloak, resp)
  } catch (e) {
    console.error('Failed to fetch next-year data', e)
    return Promise.reject(e)
  }
}
async function getRecipeExcel(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/recipe-export?year=${AOP_YEAR}&plantId=${PLANT_ID}`

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
      throw new Error(
        `Failed to export data: ${resp.status} ${resp.statusText}`,
      )
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'Production & Norms Basis Recipe.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting data:', e)
    return Promise.reject(e)
  }
}

async function saveRecipeExcel(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/recipe-import?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
    console.error('Error importing recipe data:', e)
    return Promise.reject(e)
  }
}
async function getShutdownRateExcel(keycloak, type, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-rate-export?year=${AOP_YEAR}&plantId=${PLANT_ID}&type=${type}`

  const EXCEL_NAME = type
    ? `Production & Norms Basis ${type}.xlsx`
    : `Production & Norms Basis.xlsx`

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
      throw new Error(
        `Failed to export data: ${resp.status} ${resp.statusText}`,
      )
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = EXCEL_NAME
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Exporting Data:', e)
    return Promise.reject(e)
  }
}

async function saveShutdownRateExcel(file, keycloak, type, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-rate-import?plantId=${PLANT_ID}&year=${AOP_YEAR}&type=${type}`
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
    console.error('Error importing shutdown rate data:', e)
    return Promise.reject(e)
  }
}
async function plantContributionPlanLastFourYears(
  keycloak,
  type,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/report-plant-contribution-summary-yearly?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${type}`
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
async function calculateLoadPlantContribution(PLANT_ID, AOP_YEAR, keycloak) {
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
async function getConfigurationExecutionDetailsNorms(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/configuration-execution-norms?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function getProductionTargetBasis(keycloak, PLANT_ID, AOP_YEAR) {
  let url = `${Config.CaseEngineUrl}/task/data-set-production-target?plantId=${PLANT_ID}&year=${AOP_YEAR}`

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
    return Promise.reject(e)
  }
}
export async function ImportShutdownDetails(file, keycloak, plantId, year) {
  const maintenanceTypeName = 'Shutdown'
  const url = `${Config.CaseEngineUrl}/task/shutdown-import?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
    return await resp.json()
  } catch (e) {
    console.error('Error importing Shutdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function ImportShutdownElastomerDetails(
  file,
  keycloak,
  plantId,
  year,
) {
  const maintenanceTypeName = 'Shutdown'
  const url = `${Config.CaseEngineUrl}/task/shutdown-import-non-product?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
    return await resp.json()
  } catch (e) {
    console.error('Error importing Shutdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function shutdownDetailsExport(keycloak, plantId, year) {
  const maintenanceTypeName = 'Shutdown'
  const url = `${Config.CaseEngineUrl}/task/shutdown-export?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'shutdown.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Shutdown Excel:', e)
    return Promise.reject(e)
  }
}

export async function shutdownDetailsElastomerExport(keycloak, plantId, year) {
  const maintenanceTypeName = 'Shutdown'
  const url = `${Config.CaseEngineUrl}/task/shutdown-export-non-product?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'shutdown.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Shutdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function ImportSlowdownDetails(file, keycloak, plantId, year) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/slowdown-import?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
    return await resp.json()
  } catch (e) {
    console.error('Error importing Slowdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function ImportSlowdownElastomerDetails(
  file,
  keycloak,
  plantId,
  year,
) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/slowdown-import-non-product?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
    return await resp.json()
  } catch (e) {
    console.error('Error importing Slowdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function slowdownDetailsExport(keycloak, plantId, year) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/slowdown-export?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'slowdown.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Slowdown Excel:', e)
    return Promise.reject(e)
  }
}

export async function slowdownDetailsElastomerExport(keycloak, plantId, year) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/slowdown-export-non-product?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
      throw new Error(`Export failed: ${resp.status} ${resp.statusText}`)
    }
    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = 'slowdown.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Slowdown Excel:', e)
    return Promise.reject(e)
  }
}
async function getConfigurationExcelType(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  reportType,
) {
  const url = `${Config.CaseEngineUrl}/task/configuration-export-excel?year=${AOP_YEAR}&plantId=${PLANT_ID}`
  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  const body = JSON.stringify(reportType)
  const EXCEL_NAME = reportType
    ? `Production & Norms Basis ${reportType}.xlsx `
    : `Production & Norms Basis.xlsx`

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body,
    })

    if (!resp.ok) {
      throw new Error(
        `Failed to export data: ${resp.status} ${resp.statusText}`,
      )
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob
    a.download = EXCEL_NAME
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Config data:', e)
    return Promise.reject(e)
  }
}

async function getProductionReports(keycloak, PLANT_ID, AOP_YEAR, REPORT_TYPE) {
  let url = `${Config.CaseEngineUrl}/task/production-reports?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${REPORT_TYPE}`

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
    return Promise.reject(e)
  }
}

async function gradeDetails(keycloak, AOP_YEAR, PLANT_ID) {
  const url = `${Config.CaseEngineUrl}/task/products?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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

async function carryForwardRecords(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/carry-forward?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
export async function getSpecificConsumption(
  keycloak,
  reportType,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/specific-consumption-norms?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${reportType}`
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

//PTA SHUTDOWN ACTIVITIES DROPDOWNS
//AOP_DEPLOYMENT_PROD
async function dropdownValues(keycloak, PLANT_ID, AOP_YEAR) {
  // const url = `${Config.CaseEngineUrl}/task/description-drpdwn?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const url = `${Config.CaseEngineUrl}/task/description-drpdwn?plantId=${PLANT_ID}`
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
