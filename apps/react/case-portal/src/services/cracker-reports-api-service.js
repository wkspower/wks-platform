import Config from '../consts'
import { json } from './request'
export const CrackerReportsApiDataService = {
  spyroInputReport,
  spyroOutputReport,
  finalNormsReport,
  finalNormsProductionReport,
  configurationIntermediateValues,
  findingModel,
  miisData,
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
async function finalNormsReport(
  keycloak,
  reportType,
  periodFrom,
  periodTo,
  mode,
) {
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  let url = `${Config.CaseEngineUrl}/task/final-norms-report?plantId=${plantId}&year=${year}`

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

  let url = `${Config.CaseEngineUrl}/task/final-norms-production-report?plantId=${plantId}&year=${year}`

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
