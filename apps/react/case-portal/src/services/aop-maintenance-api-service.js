import Config from '../consts'
import { json } from './request'
export const AOPMaintenanceApiService = {
  saveDesignRemarks,
  saveDesignBasis,
  designBasis,
  designRemarks,
  maintenacegetdata,
  maintenaceExportdata,
  savemaintenacegetdata,
  maintenaceImportExceldata,
}
async function saveDesignRemarks(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  maintenancedetails,
) {
  // Only encode plantId and year, leave budgetCategory as-is
  const payload = { summary: maintenancedetails }

  const url = `${Config.CaseEngineUrl}/task/maintenance-design-remarks?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function saveDesignBasis(
  keycloak,
  PLANT_ID,
  AOP_YEAR,
  maintenancedetails,
) {
  // Only encode plantId and year, leave budgetCategory as-is
  const payload = { summary: maintenancedetails }

  const url = `${Config.CaseEngineUrl}/task/maintenance-design-basis?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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

async function designBasis(keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/maintenance-design-basis?plantId=${PLANT_ID}&year=${AOP_YEAR}`

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

async function designRemarks(keycloak, PLANT_ID, AOP_YEAR) {
  // Only encode plantId and year, leave budgetCategory as-is
  const url = `${Config.CaseEngineUrl}/task/maintenance-design-remarks?plantId=${PLANT_ID}&year=${AOP_YEAR}`

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

async function maintenacegetdata(keycloak, budgetCategory) {
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)
  var year = localStorage.getItem('year')

  // Only encode plantId and year, leave budgetCategory as-is
  const url = `${Config.CaseEngineUrl}/task/budget-maintenance?plantId=${encodeURIComponent(parsedPlant.id)}&year=${encodeURIComponent(year)}&budgetCategory=${budgetCategory}`

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

async function savemaintenacegetdata(
  maintenancedetails,
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) {
  // Only encode plantId and year, leave budgetCategory as-is
  const url = `${Config.CaseEngineUrl}/task/budget-maintenance`
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${keycloak.token}`,
  }
  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(maintenancedetails),
    })
    return json(keycloak, resp)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function maintenaceExportdata(
  keycloak,

  PLANT_ID,
  AOP_YEAR,
) {
  let url = `${Config.CaseEngineUrl}/task/budget-maintenance-export-excel?year=${encodeURIComponent(AOP_YEAR)}&plantId=${encodeURIComponent(PLANT_ID)}`

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
    a.download = `Maintenance Budget_Export.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)
  } catch (e) {
    console.error('Error exporting Optimizer Input Excel:', e)
    return Promise.reject(e)
  }
}

async function maintenaceImportExceldata(file, keycloak, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/budget-maintenance-import-excel?plantId=${PLANT_ID}&year=${AOP_YEAR}`
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
    console.error('Error importing Budget Maintenance Excel:', e)
    return await Promise.reject(e)
  }
}
