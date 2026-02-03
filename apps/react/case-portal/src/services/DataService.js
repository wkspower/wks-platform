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
  getSpyroInputData,
  deleteSlowdownData,
  deleteShutdownData,
  deleteTurnAroundData,
  handleRefresh,
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
  getAnnualCostAopReport,
  getProductionVolDataBasis,
  getNormsHistorianBasis,
  //getAnnualProductionPlanReportData,
  getPlantContributionYearWisePlan,
  getMonthWiseSummary,
  updateUserPlants,
  saveworkflow,
  saveConfigurationExcel,
  saveConfigurationExcelConstants,
  importSpyroOutputExcel,
  importSpyroOutputExcelYield,
  importSpyroOutputExcelYieldNONNMD,
  exportSpyroOutputExcel,
  exportSpyroOutputExcelYieldNONNMD,
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
  //saveAnnualProduction,
  getIbrSdTa,
  getIbrSdTaNMD,
  getIbrScreen3,
  getIbrScreen3NMD,
  saveCrackerRunLength,
  saveCrackerRunLengthNMD,
  getRunLengthExcel,
  getRunLengthExcelNMD,
  saveRunLengthExcel,
  saveRunLengthExcelNMD,
  handleCalculateDecokingActivities,
  handleCalculateSdTaActivities,
  getSlowDownPlantDataTab,
  postIbr,
  postIbrNMD,
  getSpyroOutputDataYield,
  getSpyroOutputDataYieldNONNMD,
  saveSpyroOutputYieldNONNMD,
  saveSpyroOutputYield,
  getCrackerNextYearParameters,
  getCrackerNextYearParametersNMD,
  getCrackerNextYearData,
  getCrackerNextYearDataNMD,
  calculateNormsHistorianValues,

  plantContributionPlanLastFourYears,
  getRecipeExcel,
  saveRecipeExcel,
  getShutdownRateExcel,
  saveShutdownRateExcel,
  getConfigurationExecutionDetailsNorms,
  executeConfigurationNorms,
  getProductionTargetBasis,
  ImportShutdownProductWise,
  ImportShutdownNonProduct,
  exportShutdownNonProductWise,
  exportShutdownNonProduct,
  slowdownDetailsExport,
  ExportSlowdownDetailsEOE,
  slowdownDetailsElastomerExport,
  ImportSlowdownDetails,
  ImportSlowdownDetailsEOE,
  ImportSlowdownElastomerDetails,
  getConfigurationExcelType,

  getProductionReports,
  gradeDetails,
  carryForwardRecords,
  getSpecificConsumption,
  getConsumptionNorms,
  dropdownValues,
  slowdownconsumptionExport,
  slowdownconsumptionExportVCM,
  saveSlowdownNormsExcel,
  getRevision,
  updateRevision,
  getDataTeamPlant,
  getPeopleInitiative,
  savePlantTeam,
  savePeopleInitiative,
  deletePlantTeam,
  deletePeopleInitiative,
  PlantTeamExport,
  ExportPeopleInitiative,
  ImportPlantTeamExcel,
  ImportPeopleInitiativeExcel,
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
// async function getAnnualProductionPlanReportData(
//   keycloak,
//   type,
//   PLANT_ID,
//   AOP_YEAR,
// ) {
//   const url = `${Config.CaseEngineUrl}/task/report/plant/production/plan?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${type}`
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
  const url = `${Config.CaseEngineUrl}/task/report/turn-around?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=previousYear`
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
  isCalculationParam,
) {
  var url = `${Config.CaseEngineUrl}/task/production-norms?year=${AOP_YEAR}&plantFKId=${PLANT_ID}`

  if (isCalculationParam) {
    url = `${Config.CaseEngineUrl}/task/production-norms?year=${AOP_YEAR}&plantFKId=${PLANT_ID}&calculation=true`
  }
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

async function saveConfigurationExcel(
  file,
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  isCalculationParam,
) {
  var url = `${Config.CaseEngineUrl}/task/configuration-import-excel?plantId=${PLANT_ID}&year=${AOP_YEAR}`

  if (isCalculationParam) {
    url = `${Config.CaseEngineUrl}/task/configuration-import-excel?plantId=${PLANT_ID}&year=${AOP_YEAR}&calculation=true`
  }
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

async function importSpyroOutputExcelYieldNONNMD(
  file,
  keycloak,
  mode,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/yield-import-dmd?plantId=${PLANT_ID}&year=${AOP_YEAR}&mode=${encodeURIComponent(mode)}`
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

async function exportSpyroOutputExcel(
  keycloak,
  mode,
  PLANT_ID,
  AOP_YEAR,
  ExcelName,
) {
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
    a.download = `${ExcelName}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}

async function exportSpyroOutputExcelYield(
  keycloak,
  mode,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_NAME,
) {
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
    a.download = `${EXCEL_NAME}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}

async function exportSpyroOutputExcelYieldNONNMD(
  keycloak,
  mode,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_NAME,
) {
  const url = `${Config.CaseEngineUrl}/task/yield-export-dmd?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}&mode=${encodeURIComponent(mode)}`

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
    a.download = `${EXCEL_NAME}.xlsx`
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

async function exportSpyroInputExcel(
  keycloak,
  mode,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_NAME,
) {
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
    a.download = `${EXCEL_NAME}.xlsx`
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
async function getConfigurationExcel(
  keycloak,
  reportType,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_EXPORT_TITLE,
  EXCEL_EXPORT_TITLE_II,
) {
  const url = `${Config.CaseEngineUrl}/task/configuration-export-excel?year=${AOP_YEAR}&plantId=${PLANT_ID}`

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  const body = JSON.stringify(reportType)

  const fileName = EXCEL_EXPORT_TITLE_II
    ? `${EXCEL_EXPORT_TITLE}_${EXCEL_EXPORT_TITLE_II}.xlsx`
    : `${EXCEL_EXPORT_TITLE}_Production & Norms Basis.xlsx`

  try {
    const resp = await fetch(url, {
      method: 'POST', // changed from GET to POST since we?re sending a body
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

    a.download = fileName
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Config data:', e)
    return Promise.reject(e)
  }
}

async function getConfigurationExcelConstants(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  FILE_NAME,
) {
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
    a.download = FILE_NAME || 'Production & Norms Basis - Constants.xlsx'
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

// async function saveAnnualProduction(
//   PLANT_ID,
//   AOP_YEAR,
//   reportType,
//   dataList,
//   keycloak,
// ) {
//   let queryParams = `?plantId=${encodeURIComponent(PLANT_ID)}&year=${encodeURIComponent(AOP_YEAR)}`
//   if (reportType) {
//     queryParams += `&reportType=${encodeURIComponent(reportType)}`
//   }
//   const url = `${Config.CaseEngineUrl}/task/report/plant/production/plan${queryParams}`
//   const headers = {
//     Accept: 'application/json',
//     'Content-Type': 'application/json',
//     Authorization: `Bearer ${keycloak.token}`,
//   }
//   try {
//     const resp = await fetch(url, {
//       method: 'POST',
//       headers,
//       body: JSON.stringify(dataList),
//     })
//     return json(keycloak, resp)
//   } catch (e) {
//     console.error('Error in saveAnnualProduction:', e)
//     return await Promise.reject(e)
//   }
// }
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

async function getIbrSdTaNMD(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/decoking-activities/ibr-nmd?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function postIbrNMD(PLANT_ID, data, keycloak, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/decoking-activities/ibr-nmd?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function getIbrScreen3NMD(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/decoking-activities-nmd?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=RunLength`
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

async function saveCrackerRunLengthNMD(PLANT_ID, data, keycloak, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/decoking-activities/run-length-nmd?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
async function getRunLengthExcel(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  RUN_LENGTH_EXCEL_NAME,
) {
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
    a.download = `${RUN_LENGTH_EXCEL_NAME}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error Editing Config data:', e)
    return Promise.reject(e)
  }
}

async function getRunLengthExcelNMD(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  RUN_LENGTH_EXCEL_NAME,
) {
  const url = `${Config.CaseEngineUrl}/task/run-length-export-excel-nmd?year=${AOP_YEAR}&plantId=${PLANT_ID}&reportType=RunLength`
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
    a.download = `${RUN_LENGTH_EXCEL_NAME}.xlsx`
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

async function saveRunLengthExcelNMD(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/run-length-import-excel-nmd?year=${AOP_YEAR}&plantId=${PLANT_ID}&reportType=RunLength`
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

async function getSpyroOutputDataYieldNONNMD(
  keycloak,
  mode,
  type,
  PLANT_ID,
  AOP_YEAR,
) {
  const url =
    `${Config.CaseEngineUrl}/task/spyro-output/yield-dmd` +
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

async function saveSpyroOutputYieldNONNMD(
  payload,
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/spyro-output/yield-dmd?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function getCrackerNextYearParametersNMD(
  keycloak,
  date,
  PLANT_ID,
  AOP_YEAR,
) {
  const url =
    `${Config.CaseEngineUrl}/task/next-year/configuration-nmd` +
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
async function getCrackerNextYearDataNMD(
  keycloak,
  qParams,
  PLANT_ID,
  AOP_YEAR,
) {
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
    `${Config.CaseEngineUrl}/task/next-year/entry-nmd` +
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
async function getRecipeExcel(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_EXPORT_TITLE,
) {
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
    a.download = `${EXCEL_EXPORT_TITLE}_Production & Norms Basis Recipe.xlsx`
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
async function getShutdownRateExcel(
  keycloak,
  type,
  PLANT_ID,
  AOP_YEAR,
  EXCEL_EXPORT_TITLE,
) {
  const url = `${Config.CaseEngineUrl}/task/shutdown-rate-export?year=${AOP_YEAR}&plantId=${PLANT_ID}&type=${type}`

  const EXCEL_NAME = type
    ? `${EXCEL_EXPORT_TITLE}_Production & Norms Basis ${type}.xlsx`
    : `${EXCEL_EXPORT_TITLE}_Production & Norms Basis.xlsx`

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
export async function ImportShutdownProductWise(file, keycloak, plantId, year) {
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
export async function ImportShutdownNonProduct(file, keycloak, plantId, year) {
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
export async function exportShutdownNonProductWise(
  keycloak,
  plantId,
  year,
  EXCEL_EXPORT_TITLE,
) {
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
    a.download = `${EXCEL_EXPORT_TITLE}_Shutdown Activities.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Shutdown Excel:', e)
    return Promise.reject(e)
  }
}

export async function exportShutdownNonProduct(
  keycloak,
  plantId,
  year,
  EXCEL_EXPORT_TITLE,
) {
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
    a.download = `${EXCEL_EXPORT_TITLE}_Shutdown Activities.xlsx`
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

export async function ImportSlowdownDetailsEOE(file, keycloak, plantId, year) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/slowdown-rate-import?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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

export async function slowdownDetailsExport(
  keycloak,
  plantId,
  year,
  EXCEL_EXPORT_TITLE,
) {
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
    a.download = `${EXCEL_EXPORT_TITLE}_Slowdown_Activities.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Slowdown Excel:', e)
    return Promise.reject(e)
  }
}

export async function ExportSlowdownDetailsEOE(
  keycloak,
  plantId,
  year,
  EXCEL_EXPORT_TITLE,
) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/slowdown-rate-export?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}&maintenanceTypeName=${encodeURIComponent(maintenanceTypeName)}`
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
    a.download = `${EXCEL_EXPORT_TITLE}_Slowdown_Activities.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Slowdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function slowdownDetailsElastomerExport(
  keycloak,
  plantId,
  year,
  EXCEL_EXPORT_TITLE,
) {
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
    a.download = `${EXCEL_EXPORT_TITLE}_Slowdown_Activities.xlsx`
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
  EXCEL_EXPORT_TITLE,
) {
  const url = `${Config.CaseEngineUrl}/task/configuration-export-excel?year=${AOP_YEAR}&plantId=${PLANT_ID}`
  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  const body = JSON.stringify(reportType)
  const EXCEL_NAME = reportType
    ? `${EXCEL_EXPORT_TITLE}_Production & Norms Basis ${reportType}.xlsx`
    : `${EXCEL_EXPORT_TITLE}_Production & Norms Basis.xlsx`

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

export async function getConsumptionNorms(
  keycloak,
  reportType,
  PLANT_ID,
  AOP_YEAR,
) {
  const url = `${Config.CaseEngineUrl}/task/grade-wise-consumption-norms?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${reportType}`
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
export async function slowdownconsumptionExport(keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/slowdown-consumption-export?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}`
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
    a.download = 'Slowdown_consumption.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Shutdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function slowdownconsumptionExportVCM(
  keycloak,
  plantId,
  year,
  gradeId,
) {
  const url =
    `${Config.CaseEngineUrl}/task/export-slowdown-consumption?year=${encodeURIComponent(year)}&plantId=${encodeURIComponent(plantId)}` +
    (gradeId ? `&gradeId=${encodeURIComponent(gradeId)}` : '')

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
    a.download = 'Slowdown_consumption.xlsx'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Slowdown Excel:', e)
    return Promise.reject(e)
  }
}
async function saveSlowdownNormsExcel(
  file,
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  GRADE_ID,
) {
  let url = ''
  url = `${Config.CaseEngineUrl}/task/import-slowdown-consumption?plantId=${PLANT_ID}&year=${AOP_YEAR}`

  if (GRADE_ID) {
    url += `&gradeId=${GRADE_ID}`
  }

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
async function getRevision(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-version?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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

async function updateRevision(keycloak, payload, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/configuration-version?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
export async function getDataTeamPlant(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/plant-team?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return await resp.json()
  } catch (e) {
    console.error('Error fetching Plant Team data:', e)
    return Promise.reject(e)
  }
}
export async function getPeopleInitiative(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/people-initiative?plantId=${PLANT_ID}&year=${AOP_YEAR}`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, { method: 'GET', headers })
    return await resp.json()
  } catch (e) {
    console.error('Error fetching Plant Team data:', e)
    return Promise.reject(e)
  }
}
export async function savePlantTeam(keycloak, PLANT_ID, AOP_YEAR, data) {
  const url = `${Config.CaseEngineUrl}/task/plant-team?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
    return await resp.json()
  } catch (e) {
    console.error('Error saving Plant Team data:', e)
    return Promise.reject(e)
  }
}

export async function savePeopleInitiative(keycloak, PLANT_ID, AOP_YEAR, data) {
  const url = `${Config.CaseEngineUrl}/task/people-initiative?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
    return await resp.json()
  } catch (e) {
    console.error('Error saving People Initiative data:', e)
    return Promise.reject(e)
  }
}
async function deletePlantTeam(plantTeamId, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/plant-team?id=${encodeURIComponent(plantTeamId)}`
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
async function deletePeopleInitiative(peopleInitiativeId, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/people-initiative?id=${encodeURIComponent(peopleInitiativeId)}`
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
export async function PlantTeamExport(
  keycloak,
  plantId,
  year,
  EXCEL_EXPORT_TITLE,
) {
  const maintenanceTypeName = 'Slowdown'
  const url = `${Config.CaseEngineUrl}/task/plant-team-export?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}`
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
    a.download = `${EXCEL_EXPORT_TITLE || 'plant_team'}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Slowdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function ExportPeopleInitiative(
  keycloak,
  plantId,
  year,
  EXCEL_EXPORT_TITLE,
) {
  const url = `${Config.CaseEngineUrl}/task/people-initiative-export?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}`
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
    a.download = `${EXCEL_EXPORT_TITLE || 'People_Initiative'}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Slowdown Excel:', e)
    return Promise.reject(e)
  }
}
export async function ImportPlantTeamExcel(file, keycloak, plantId, year) {
  const url = `${Config.CaseEngineUrl}/task/plant-team-import?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}`
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
    console.error('Error importing Plant Team Excel:', e)
    return Promise.reject(e)
  }
}

export async function ImportPeopleInitiativeExcel(
  file,
  keycloak,
  plantId,
  year,
) {
  const url = `${Config.CaseEngineUrl}/task/people-initiative-import?plantId=${encodeURIComponent(plantId)}&year=${encodeURIComponent(year)}`
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
    console.error('Error importing People Initiative Excel:', e)
    return Promise.reject(e)
  }
}
