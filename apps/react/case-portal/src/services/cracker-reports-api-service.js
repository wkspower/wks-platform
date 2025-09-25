import Config from '../consts'
import { json } from './request'
export const CrackerReportsApiDataService = {
  spyroInputReport,
  spyroOutputReport,
  finalNormsReport,
  finalNormsProductionReport,
  configurationIntermediateValues,
  getRawDataSetvalues,
  getRawutilitymonthly,
  getRawCatcame,
  getRawatcammonthly,
  getRawasteam,
  getRawasfindingteam,
  getConfigurationExecutionDetails,
  findingModel,
  miisData,
  furnaceRawData,
  runLengthDataSet,
}

async function runLengthDataSet(keycloak, reportType, PLANT_ID, AOP_YEAR) {
  let url = `${Config.CaseEngineUrl}/task/run-length-data-set?plantId=${PLANT_ID}&year=${AOP_YEAR}&reportType=${reportType}`

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

async function miisData(keycloak, reportType, periodFrom, periodTo, mode) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/miis-data?plantId=${plantId}&year=${year}`

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
async function findingModel(keycloak, reportType, periodFrom, periodTo, mode) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/finding-model?plantId=${plantId}&year=${year}`

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

async function furnaceRawData(keycloak, reportType) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/report-furnace?plantId=${plantId}&year=${year}&reportType=${reportType}`

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

async function configurationIntermediateValues(keycloak) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/configuration-intermediate-values?plantId=${plantId}&year=${year}`

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

async function getRawDataSetvalues(keycloak, periodFrom, periodTo) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/report-best-achieved-raw?plantId=${plantId}&year=${year}&periodFrom=${periodFrom}&periodTo=${periodTo}`

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
async function getRawCatcame(keycloak, periodFrom, periodTo) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/report-best-achieved-catcam?plantId=${plantId}&year=${year}&periodFrom=${periodFrom}&periodTo=${periodTo}`

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

async function getRawutilitymonthly(keycloak, periodFrom, periodTo) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/report-best-achieved-mis-utility-monthly?plantId=${plantId}&year=${year}&periodFrom=${periodFrom}&periodTo=${periodTo}`

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

async function getRawatcammonthly(keycloak, periodFrom, periodTo) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/report-best-achieved-stg-catcam-monthly?plantId=${plantId}&year=${year}&periodFrom=${periodFrom}&periodTo=${periodTo}`

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
///task/report-best-achieved-raw-steam?
async function getRawasteam(keycloak, periodFrom, periodTo, mode) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  // ✅ Encode mode to handle special characters like '+'
  const encodedMode = encodeURIComponent(mode)

  let url = `${Config.CaseEngineUrl}/task/report-best-achieved-raw-steam?plantId=${plantId}&year=${year}&periodFrom=${periodFrom}&periodTo=${periodTo}&mode=${encodedMode}`

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

async function getRawasfindingteam(keycloak, mode) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const encodedMode = encodeURIComponent(mode)

  let url = `${Config.CaseEngineUrl}/task/report-best-achieved-finding-steam?plantId=${plantId}&year=${year}&mode=${encodedMode}`

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

async function finalNormsReport(
  keycloak,
  reportType,
  periodFrom,
  periodTo,
  mode,
) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/final-norms-report?plantId=${plantId}&year=${year}&reportType=${reportType}`

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
async function finalNormsProductionReport(
  keycloak,
  reportType,
  periodFrom,
  periodTo,
  mode,
) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/final-norms-production-report?plantId=${plantId}&year=${year}&reportType=${reportType}`

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
async function spyroOutputReport(
  keycloak,
  reportType,

  mode,
) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/spyro-output-report?plantId=${plantId}&year=${year}&mode=${mode}`

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

async function spyroInputReport(
  keycloak,
  reportType,

  mode,
) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/spyro-input-report?plantId=${plantId}&year=${year}&mode=${mode}`

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
