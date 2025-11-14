import Config from '../consts'
import { json } from './request'
export const BusinessDemandDataApiService = {
  getBDData,
  getBDssData,
  saveBusinessDemandData,
  deleteBusinessDemandData,
  businessDemandImport,
  businessDemandExport,
  aopDesignBasisBluePrint,
  savepropanebusiness,
  ssrsBudgetSummary,
  ssrsMaintenanceSummary,
  ssrsSiteMaintenanceSummary,
  SSRS_NormComparisonReport,
}
async function getBDData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/business-demand?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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
async function ssrsBudgetSummary(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-report-urls?year=${AOP_YEAR}&plantId=${PLANT_ID}&type=plant-budget-summary`

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

async function ssrsMaintenanceSummary(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-report-urls?year=${AOP_YEAR}&plantId=${PLANT_ID}&type=maintenance-summary`
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

async function ssrsSiteMaintenanceSummary(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-report-urls?year=${AOP_YEAR}&plantId=${PLANT_ID}&type=site-maintenance-summary`
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

async function saveBusinessDemandData(payloadData, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/business-demand`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(payloadData),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
async function deleteBusinessDemandData(maintenanceId, keycloak) {
  const url = `${Config.CaseEngineUrl}/task/business-demand/${maintenanceId}`
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

async function businessDemandExport(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/business-demand-export?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`

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
    a.download = `BusinessDemand_${'Export'}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}

async function businessDemandImport(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/business-demand-import?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function aopDesignBasisBluePrint() {
  var url = `${window.location.origin}/files/BluePrintCracker.pdf`

  try {
    const resp = await fetch(url, {
      method: 'GET',
    })

    const blob = await resp.blob()
    const fileURL = window.URL.createObjectURL(blob)
    window.open(fileURL, '_blank')
    return true
  } catch (e) {
    console.error('Error fetching file:', e)
    return Promise.reject(e)
  }
}
async function getBDssData(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/business-demand-manual-entry?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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

async function savepropanebusiness(
  PLANT_ID,
  AOP_YEAR,
  turnAroundDetails,
  keycloak,
) {
  const url = `${Config.CaseEngineUrl}/task/business-demand-manual-entry?year=${AOP_YEAR}&plantId=${PLANT_ID}`
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

async function SSRS_NormComparisonReport(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-report-urls?year=${AOP_YEAR}&plantId=${PLANT_ID}&type=norm-comparison-report`

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
