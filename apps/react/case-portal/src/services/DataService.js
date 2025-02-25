import { json, nop } from './request'
import Config from 'consts/index'

export const DataService = {
  getProductById,
  getYearWiseProduct,
  getAllSites,
  getShutDownPlantData,
  getAllProducts,
  getYearlyData,
  getSlowDownPlantData,
  getTAPlantData,
  saveShutdownData,
  saveSlowdownData,
  saveTurnAroundData,
  createCase,
  getTasksByBusinessKey,
  getProcessInstanceVariables,
  completeTask,
}

async function getProductById(keycloak, id) {
  const url = `${process.env.REACT_APP_API_URL}/task/productList`

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
  const url = `${process.env.REACT_APP_API_URL}/task/saveShutdownData/${plantId}`

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

async function saveSlowdownData(plantId, slowDownDetails, keycloak) {
  const url = `${process.env.REACT_APP_API_URL}/task/saveSlowdownData/${plantId}`

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


async function saveTurnAroundData(plantId, turnAroundDetails, keycloak) {
  const url = `${process.env.REACT_APP_API_URL}/task/saveTurnaroundPlanData/${plantId}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/yearly-data?year=${year}`

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

async function getYearWiseProduct(keycloak, id) {
  var type = 'Business Demand Data'
  var year = '2025'
  const url = `${process.env.REACT_APP_API_URL}/task/getMonthWiseData?type=${type}&year=${year}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/getPlantAndSite`

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
  const url = `${process.env.REACT_APP_API_URL}/task/getAllProducts`

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
  var plantId = ''

  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  plantId = 'B989E3EE-00C8-493C-9CA4-709D340FA5A1'

  const url = `${process.env.REACT_APP_API_URL}/task/getShutDownPlanData?plantId=${plantId}&maintenanceTypeName=${maintenanceTypeName}`

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
  const plantId = 'B989E3EE-00C8-493C-9CA4-709D340FA5A1';
  // const plantId = '7b7e0d7c-2666-43bb-847c-d78e144673de'

  const maintenanceTypeName = 'Slowdown' // Assuming the maintenance type is 'Slowdown'

  // const storedPlant = localStorage.getItem('selectedPlant')
  // if (storedPlant) {
  //   const parsedPlant = JSON.parse(storedPlant)
  //   plantId= (parsedPlant.id)
  // }

  const url = `${process.env.REACT_APP_API_URL}/task/getSlowDownPlanData?plantId=${plantId}&maintenanceTypeName=${maintenanceTypeName}`

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
  const plantId = 'B989E3EE-00C8-493C-9CA4-709D340FA5A1';
  // const plantId = '3E3FDF54-391D-4BAB-A78F-50EBCA9FBEA6'
  const maintenanceTypeName = 'TA_Plan' // Assuming the maintenance type is 'Shutdown'

  // const storedPlant = localStorage.getItem('selectedPlant')
  // if (storedPlant) {
  //   const parsedPlant = JSON.parse(storedPlant)
  //   plantId= (parsedPlant.id)
  // }

  const url = `${process.env.REACT_APP_API_URL}/task/getTurnaroundPlanData?plantId=${plantId}&maintenanceTypeName=${maintenanceTypeName}`

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

async function getMonthWiseData(keycloak) {
  const url = `${process.env.REACT_APP_API_URL}/getMonthWiseData`

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

// New API function: Create a case
async function createCase(keycloak, caseData) {
  // Assuming process.env.REACT_APP_API_URL is set to http://localhost:8081 or similar
  const url = `${process.env.REACT_APP_API_URL}/case`
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
  const url = `${process.env.REACT_APP_API_URL}/task?businessKey=${businessKey}`
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
  const url = `${process.env.REACT_APP_API_URL}/variable?processInstanceId=${processInstanceId}`
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

async function completeTask(keycloak, taskId, payload) {
  const url = `${process.env.REACT_APP_API_URL}/task/${taskId}/complete`
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
