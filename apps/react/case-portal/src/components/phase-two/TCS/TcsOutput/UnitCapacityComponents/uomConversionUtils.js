/**
 * UOM Conversion Utilities for Unit Capacity
 *
 * Backend always stores data in KBPSD (Kilo Barrels Per Stream Day)
 * UI supports: KBPSD, KTPD (Kilo Tons Per Day), TPD (Tons Per Day)
 *
 * Conversion factors (approximate - adjust based on your business logic):
 * - 1 KBPSD ≈ 0.136 KTPD (assuming specific gravity ~0.85)
 * - 1 KTPD = 1000 TPD
 * - 1 KBPSD ≈ 136 TPD
 */

// Conversion factors
const CONVERSION_FACTORS = {
  // From KBPSD to other units
  KBPSD_TO_KTPD: 0.136, // Adjust based on your specific gravity
  KBPSD_TO_TPD: 136, // Adjust based on your specific gravity

  // From KTPD to other units
  KTPD_TO_KBPSD: 1 / 0.136,
  KTPD_TO_TPD: 1000,

  // From TPD to other units
  TPD_TO_KBPSD: 1 / 136,
  TPD_TO_KTPD: 1 / 1000,
}

/**
 * Convert value from KBPSD (backend format) to selected UOM (display format)
 * @param {number} value - Value in KBPSD
 * @param {string} targetUOM - Target UOM (KBPSD, KTPD, TPD)
 * @returns {number|null} Converted value
 */
export const convertFromKBPSD = (value, targetUOM) => {
  if (value === null || value === undefined || value === '') return null

  const numValue = Number(value)
  if (isNaN(numValue)) return null

  const uom = targetUOM?.toUpperCase()

  switch (uom) {
    case 'KBPSD':
      return numValue
    case 'KTPD':
      return numValue * CONVERSION_FACTORS.KBPSD_TO_KTPD
    case 'TPD':
      return numValue * CONVERSION_FACTORS.KBPSD_TO_TPD
    default:
      console.warn(`Unknown UOM: ${targetUOM}, returning original value`)
      return numValue
  }
}

/**
 * Convert value from selected UOM (display format) to KBPSD (backend format)
 * @param {number} value - Value in selected UOM
 * @param {string} sourceUOM - Source UOM (KBPSD, KTPD, TPD)
 * @returns {number|null} Converted value in KBPSD
 */
export const convertToKBPSD = (value, sourceUOM) => {
  if (value === null || value === undefined || value === '') return null

  const numValue = Number(value)
  if (isNaN(numValue)) return null

  const uom = sourceUOM?.toUpperCase()

  switch (uom) {
    case 'KBPSD':
      return numValue
    case 'KTPD':
      return numValue * CONVERSION_FACTORS.KTPD_TO_KBPSD
    case 'TPD':
      return numValue * CONVERSION_FACTORS.TPD_TO_KBPSD
    default:
      console.warn(`Unknown UOM: ${sourceUOM}, returning original value`)
      return numValue
  }
}

/**
 * Convert row data from KBPSD to selected UOM
 * @param {object} row - Row data with summer/winter values in KBPSD
 * @param {string} targetUOM - Target UOM
 * @returns {object} Row with converted values
 */
export const convertRowFromKBPSD = (row, targetUOM) => {
  if (!row) return row

  return {
    ...row,
    summer: convertFromKBPSD(row.summer, targetUOM),
    winter: convertFromKBPSD(row.winter, targetUOM),
  }
}

/**
 * Convert row data from selected UOM to KBPSD
 * @param {object} row - Row data with summer/winter values in selected UOM
 * @param {string} sourceUOM - Source UOM
 * @returns {object} Row with values converted to KBPSD
 */
export const convertRowToKBPSD = (row, sourceUOM) => {
  if (!row) return row

  return {
    ...row,
    summer: convertToKBPSD(row.summer, sourceUOM),
    winter: convertToKBPSD(row.winter, sourceUOM),
  }
}

/**
 * Convert multiple rows from KBPSD to selected UOM
 * @param {array} rows - Array of row data
 * @param {string} targetUOM - Target UOM
 * @returns {array} Rows with converted values
 */
export const convertRowsFromKBPSD = (rows, targetUOM) => {
  if (!Array.isArray(rows)) return rows
  return rows.map((row) => convertRowFromKBPSD(row, targetUOM))
}

/**
 * Convert multiple rows from selected UOM to KBPSD
 * @param {array} rows - Array of row data
 * @param {string} sourceUOM - Source UOM
 * @returns {array} Rows with values converted to KBPSD
 */
export const convertRowsToKBPSD = (rows, sourceUOM) => {
  if (!Array.isArray(rows)) return rows
  return rows.map((row) => convertRowToKBPSD(row, sourceUOM))
}
