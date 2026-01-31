/**
 * Utility functions for Production Target grids
 * Handles unit conversions between TPH and TPD
 */

const MONTH_FIELDS = [
  'april',
  'may',
  'june',
  'july',
  'august',
  'september',
  'october',
  'november',
  'december',
  'january',
  'february',
  'march',
]

/**
 * Convert data from TPH to TPD (multiply by 24)
 * @param {Object} row - Row data object
 * @returns {Object} - Converted row data
 */
export const convertTPHtoTPD = (row) => {
  const converted = { ...row }
  MONTH_FIELDS.forEach((month) => {
    if (row[month] !== null && row[month] !== undefined) {
      converted[month] = (row[month] * 24).toFixed(2)
    }
  })
  return converted
}

/**
 * Convert data from TPD to TPH (divide by 24)
 * @param {Object} row - Row data object
 * @returns {Object} - Converted row data
 */
export const convertTPDtoTPH = (row) => {
  const converted = { ...row }
  MONTH_FIELDS.forEach((month) => {
    if (row[month] !== null && row[month] !== undefined) {
      converted[month] = row[month] / 24
    }
  })
  return converted
}

/**
 * Convert array of rows based on selected unit
 * @param {Array} rows - Array of row data
 * @param {String} selectedUnit - Selected unit ('TPH' or 'TPD')
 * @returns {Array} - Converted array of rows
 */
export const convertRowsByUnit = (rows, selectedUnit) => {
  if (selectedUnit === 'TPD') {
    return rows.map((row) => convertTPHtoTPD(row))
  }
  return rows
}

/**
 * Convert modified data back to TPH for saving (if unit is TPD)
 * @param {Array} modifiedData - Array of modified row data
 * @param {String} selectedUnit - Selected unit ('TPH' or 'TPD')
 * @returns {Array} - Converted array ready for API
 */
export const convertDataForSave = (modifiedData, selectedUnit) => {
  if (selectedUnit === 'TPD') {
    return modifiedData.map((row) => convertTPDtoTPH(row))
  }
  return modifiedData
}
