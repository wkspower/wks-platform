import { json } from './request'

export const DataService = {
  getProductById,
  getYearWiseProduct,
  getAllSites,
  getShutDownPlantData,
  getAllProducts,
  getYearlyData,
  getSlowDownPlantData,
  getTAPlantData,
  getBDData,
  getCatalystSelectivityData,
  getProductionNormsData,
  getConsumptionNormsData,

  getAllCatalyst,

  saveShutdownData,
  saveSlowdownData,
  saveTurnAroundData,

  saveCatalystData,

  saveBusinessDemandData,
  saveNormalOperationNormsData,
  saveShutDownNormsData,
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
  getAOPMCCalculatedData,

  deleteSlowdownData,
  deleteShutdownData,
  deleteTurnAroundData,
  deleteBusinessDemandData,
  handleRefresh,
  handleCalculate,
  getNormalOperationNormsData,
  getShutdownNormsData,
}

async function handleRefresh(year, plantId, keycloak) {
  const url = `${process.env.REACT_APP_API_URL}/task/handleRefresh?year=${year}&plantId=${plantId}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/calculateData?year=${year1}&plantId=${plantId}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/deleteSlowdownData/${maintenanceId}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/deleteShutdownData/${maintenanceId}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/deleteTurnaroundData/${maintenanceId}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/deleteBusinessDemandData/${maintenanceId}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/editBusinessDemandData/${maintenanceId}`

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

async function getProductById(keycloak) {
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
async function getBDData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }
  // var siteId = ''
  // const storedSite = localStorage.getItem('selectedSite')
  // if (storedSite) {
  //   const parsedSite = JSON.parse(storedSite)
  //   // siteId = parsedSite.id
  // }
  const url = `${process.env.REACT_APP_API_URL}/task/getBusinessDemandData?year=${year}&plantId=${plantId}`
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
  // var siteId = ''
  const storedSite = localStorage.getItem('selectedSite')
  if (storedSite) {
    // const parsedSite = JSON.parse(storedSite)
    // siteId = parsedSite.id
  }
  const url = `${process.env.REACT_APP_API_URL}/task/getNormalOperationNormsData?year=${year}&plantId=${plantId}`
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
  // var siteId = ''
  const storedSite = localStorage.getItem('selectedSite')
  if (storedSite) {
    // const parsedSite = JSON.parse(storedSite)
    // siteId = parsedSite.id
  }
  const url = `${process.env.REACT_APP_API_URL}/task/getShutdownNormsData?year=${year}&plantId=${plantId}`
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
  var siteId = 'F0F4E75E-3C44-4FB4-BA7A-2B8227847134'

  // const storedSite = localStorage.getItem('selectedSite')
  // if (storedSite) {
  //   const parsedSite = JSON.parse(storedSite)
  //   siteId = parsedSite.id
  // }
  var year = localStorage.getItem('year')

  const url = `${process.env.REACT_APP_API_URL}/task/getConfigurationData?year=${year}&plantFKId=${plantId}`

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
  var siteId = ''

  const storedSite = localStorage.getItem('selectedSite')
  if (storedSite) {
    const parsedSite = JSON.parse(storedSite)
    siteId = parsedSite.id
  }
  const url = `${process.env.REACT_APP_API_URL}/task/getProductionNormData?year=2024&plantId=${plantId}&siteId=${siteId}`

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
  var siteId = ''

  const storedSite = localStorage.getItem('selectedSite')
  if (storedSite) {
    const parsedSite = JSON.parse(storedSite)
    siteId = parsedSite.id
  }

  const url = `${process.env.REACT_APP_API_URL}/task/getCosnumptionNormData?year=2024&plantId=${plantId}&siteId=${siteId}`

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

async function updateSlowdownData(maintenanceId, slowDownDetails, keycloak) {
  const url = `${process.env.REACT_APP_API_URL}/task/editSlowdownData/${maintenanceId}`

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

async function updateShutdownData(maintenanceId, slowDownDetails, keycloak) {
  const url = `${process.env.REACT_APP_API_URL}/task/editShutdownData/${maintenanceId}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/editTurnaroundData/${maintenanceId}` // Corrected endpoint

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
  const url = `${process.env.REACT_APP_API_URL}/task/updateAOP` // Corrected endpoint

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

async function saveCatalystData(plantId, turnAroundDetails, keycloak) {
  const url = `${process.env.REACT_APP_API_URL}/task/saveConfigurationData?year=2025-26&plantFKId=${plantId}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/saveBusinessDemandData`

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

async function saveNormalOperationNormsData(
  plantId,
  turnAroundDetails,
  keycloak,
) {
  const url = `${process.env.REACT_APP_API_URL}/task/saveNormalOperationNormsData`

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
  const url = `${process.env.REACT_APP_API_URL}/task/saveShutDownNormsData`

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

async function editAOPMCCalculatedData(plantId, turnAroundDetails, keycloak) {
  const url = `${process.env.REACT_APP_API_URL}/task/editAOPMCCalculatedData`

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

async function getYearWiseProduct(keycloak) {
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
  const url = `${process.env.REACT_APP_API_URL}/task/getPlantsAndSidesAndVerticals`
  // const url = `${process.env.REACT_APP_API_URL}/task/getPlantAndSite`

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

async function getAllProducts(keycloak, type) {
  const storedPlant = localStorage.getItem('selectedPlant')
  const parsedPlant = JSON.parse(storedPlant)
  const url = `${process.env.REACT_APP_API_URL}/task/getAllProducts?normParameterTypeName=${type}&plantId=${parsedPlant.id}`

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
  const url = `${process.env.REACT_APP_API_URL}/task/getAllCatalystAttributes`

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

  const url = `${process.env.REACT_APP_API_URL}/task/getShutDownPlanData?plantId=${plantId}&maintenanceTypeName=${maintenanceTypeName}&year=${year}`

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

  const url = `${process.env.REACT_APP_API_URL}/task/getSlowDownPlanData?plantId=${plantId}&maintenanceTypeName=${maintenanceTypeName}&year=${year}`

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

async function getAOPData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${process.env.REACT_APP_API_URL}/task/getAOP?plantId=${plantId}&year=${year}`
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

async function getAOPMCCalculatedData(keycloak) {
  var year = localStorage.getItem('year')
  var plantId = ''
  const storedPlant = localStorage.getItem('selectedPlant')
  if (storedPlant) {
    const parsedPlant = JSON.parse(storedPlant)
    plantId = parsedPlant.id
  }

  const url = `${process.env.REACT_APP_API_URL}/task/getAOPMCCalculatedData?plantId=${plantId}&year=${year}`
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

  const url = `${process.env.REACT_APP_API_URL}/task/getTurnaroundPlanData?plantId=${plantId}&maintenanceTypeName=${maintenanceTypeName}&year=${year}`

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
//   const url = `${process.env.REACT_APP_API_URL}/getMonthWiseData`

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
