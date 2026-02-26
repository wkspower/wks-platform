import Config from 'consts/index'
import { json } from 'services/request'

export const ImportExportApiService = {
  saveExcelData,
  exportExcelData,
}

// ===================== || GENERIC EXCEL IMPORT FUNCTION || ===================== //
/**
 * Generic function to upload Excel file to any CPP Input endpoint
 * @param {File} file - The Excel file to upload
 * @param {Object} keycloak - Keycloak session object
 * @param {string} endpoint - The API endpoint path (e.g., 'import-power/import')
 * @param {string} PLANT_ID - Plant ID
 * @param {string} AOP_YEAR - Financial year
 * @returns {Promise} API response
 */
async function saveExcelData(file, keycloak, endpoint, PLANT_ID, AOP_YEAR) {
  const url = `${Config.CaseEngineUrl}/task/${endpoint}/${PLANT_ID}/${AOP_YEAR}`
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

    const responseData = await json(keycloak, resp)

    // Return response data for both success and 400 (partial success with error file)
    // Components will handle error file download based on code and data
    if (resp.status === 400 || resp.status === 200) {
      return responseData
    }

    if (!resp.ok) {
      throw new Error(
        `Failed to import data: ${resp.status} ${resp.statusText}`,
      )
    }

    return responseData
  } catch (e) {
    console.error(`Error importing Excel data to ${endpoint}:`, e)
    return Promise.reject(e)
  }
}

// ===================== || GENERIC EXCEL EXPORT FUNCTION || ===================== //
/**
 * Generic function to export Excel file from backend
 * @param {Object} keycloak - Keycloak session object
 * @param {Object} params - Export parameters
 * @param {string} params.endpoint - The API endpoint path (e.g., 'export-excel')
 * @param {Object} params.queryParams - Query parameters (e.g., { year: '2024', plantId: '123', type: 'Production' })
 * @param {Object|null} params.payload - Optional POST body payload
 * @param {string} params.fileName - Downloaded file name (e.g., 'plant_production_plan.xlsx')
 * @param {string} params.method - HTTP method ('GET' or 'POST'), defaults to 'GET'
 * @returns {Promise} Success/error response
 */
async function exportExcelData(keycloak, params) {
  const {
    endpoint,
    queryParams = {},
    payload = null,
    fileName,
    method = 'GET',
  } = params

  const queryString = new URLSearchParams(queryParams).toString()
  const url = `${Config.CaseEngineUrl}/task/${endpoint}${queryString ? `?${queryString}` : ''}`

  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    Authorization: `Bearer ${keycloak.token}`,
  }

  try {
    const fetchOptions = {
      method,
      headers,
    }

    if (payload && (method === 'POST' || method === 'PUT')) {
      fetchOptions.body = JSON.stringify(payload)
    }

    const resp = await fetch(url, fetchOptions)

    if (!resp.ok) {
      throw new Error(
        `Failed to export Excel: ${resp.status} ${resp.statusText}`,
      )
    }

    const blob = await resp.blob()
    const urlBlob = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = urlBlob

    // Extract filename from Content-Disposition header if available
    const contentDisposition = resp.headers.get('content-disposition')
    let downloadFileName = fileName
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="?([^";\n]+)"?/i)
      if (filenameMatch && filenameMatch[1]) {
        downloadFileName = filenameMatch[1]
      }
    }

    a.download = downloadFileName
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(urlBlob)

    return { success: true, message: 'Excel exported successfully' }
  } catch (e) {
    console.error(`Error exporting Excel from ${endpoint}:`, e)
    return Promise.reject(e)
  }
}
