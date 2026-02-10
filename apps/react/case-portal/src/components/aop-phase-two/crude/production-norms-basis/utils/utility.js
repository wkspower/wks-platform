import { DataService } from 'services/DataService'

/**
 * Format date to YYYY-MM-DD format
 */
export const formatDate = (date) => {
  if (!date) return ''
  const year = date?.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * Format date for display text (DD-MM-YYYY with optional time)
 */
export const formatDateForText = (date, time = false) => {
  if (!date) return ''
  const parsedDate = new Date(date)
  if (isNaN(parsedDate)) return 'Invalid Date'
  const day = String(parsedDate.getDate()).padStart(2, '0')
  const month = String(parsedDate.getMonth() + 1).padStart(2, '0')
  const year = parsedDate.getFullYear()
  let formatted = `${day}-${month}-${year}`
  if (time) {
    let hours = parsedDate.getHours()
    const minutes = String(parsedDate.getMinutes()).padStart(2, '0')
    const ampm = hours >= 12 ? 'PM' : 'AM'
    hours = hours % 12
    hours = hours ? hours : 12
    const formattedTime = `${String(hours).padStart(2, '0')}:${minutes} ${ampm}`
    formatted += ` ${formattedTime}`
  }
  return formatted
}

/**
 * Validate date range
 */
export const validateDateRange = (startDate, endDate) => {
  if (!startDate || !endDate) {
    return { valid: false, message: 'Please select both start and end dates.' }
  }

  const s = new Date(startDate)
  const e = new Date(endDate)

  s.setHours(0, 0, 0, 0)
  e.setHours(0, 0, 0, 0)

  if (s > e) {
    return {
      valid: false,
      message:
        'Please choose valid dates (start date must be before end date).',
    }
  }

  return { valid: true }
}

/**
 * Get AOP Summary
 */
export const getAopSummary = async (keycloak, PLANT_ID, AOP_YEAR) => {
  if (!PLANT_ID || !AOP_YEAR) return null
  try {
    const res = await DataService.getAopSummary(keycloak, PLANT_ID, AOP_YEAR)
    if (res?.code === 200) {
      return res?.data?.summary || ''
    }
    return ''
  } catch (error) {
    console.error('Error fetching AOP summary:', error)
    return ''
  }
}

/**
 * Save AOP Summary
 */
export const saveSummary = async (PLANT_ID, AOP_YEAR, summary, keycloak) => {
  try {
    const response = await DataService.saveSummaryAOPConsumptionNorm(
      PLANT_ID,
      AOP_YEAR,
      summary,
      keycloak,
    )
    return response
  } catch (error) {
    console.error('Error saving summary:', error)
    return null
  }
}

/**
 * Get Configuration Execution Details
 */
export const getConfigurationExecutionDetails = async (
  keycloak,
  PLANT_ID,
  AOP_YEAR,
) => {
  try {
    const response = await DataService.getConfigurationExecutionDetails(
      keycloak,
      PLANT_ID,
      AOP_YEAR,
    )
    const details = response?.data || []
    if (details.length === 0) {
      console.warn(
        'getConfigurationExecutionDetails returned an empty array:',
        response,
      )
    }
    return details
  } catch (error) {
    console.error('Error fetching getConfigurationExecutionDetails:', error)
    return []
  }
}

/**
 * Build payload for configuration execution
 */
export const buildConfigurationPayload = (
  startDate,
  endDate,
  configurationExecutionDetails,
  PLANT_ID,
  AOP_YEAR,
) => {
  const startDateObj = configurationExecutionDetails.find(
    (item) => item.Name === 'StartDate',
  )
  const endDateObj = configurationExecutionDetails.find(
    (item) => item.Name === 'EndDate',
  )

  if (!startDateObj?.Id || !endDateObj?.Id) {
    return null
  }

  return [
    {
      apr: formatDate(startDate),
      UOM: '',
      auditYear: AOP_YEAR,
      normParameterFKId: startDateObj?.NormParameter_FK_Id,
      remarks: 'Initiated',
      id: startDateObj?.Id || null,
      plantId: PLANT_ID,
    },
    {
      apr: formatDate(endDate),
      UOM: '',
      auditYear: AOP_YEAR,
      normParameterFKId: endDateObj?.NormParameter_FK_Id,
      remarks: 'Initiated',
      id: endDateObj?.Id || null,
      plantId: PLANT_ID,
    },
  ]
}

/**
 * Execute Configuration
 */
export const executeConfiguration = async (payload, keycloak) => {
  try {
    const response = await DataService.executeConfiguration(payload, keycloak)
    return response
  } catch (error) {
    console.error('Execution Failed!', error)
    return null
  }
}
