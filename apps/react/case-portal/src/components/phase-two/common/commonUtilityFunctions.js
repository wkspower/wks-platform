// Helper function to get nested field value using dot notation
const getNestedValue = (obj, path) => {
  return path.split('.').reduce((current, prop) => current?.[prop], obj)
}

// Validation function: Check if any row data is updated, then remarks must be filled and different from original
export const validateRowDataWithRemarks = (
  data,
  originalRows,
  fieldsToCheck = [
    'april',
    'may',
    'june',
    'july',
    'aug',
    'sept',
    'oct',
    'nov',
    'dec',
    'jan',
    'feb',
    'mar',
  ],
  displayFieldName,
  remarksFieldName = 'remarks',
) => {
  const invalidRows = data.filter((row) => {
    // Get original row from originalRows
    const originalRow = originalRows.find((orig) => orig.id === row.id)

    if (!originalRow) return false

    // Check if any data field was updated
    const dataWasUpdated = fieldsToCheck.some((field) => {
      return row[field] !== originalRow[field]
    })

    // If data was updated, check if remarks field is filled and different from original
    if (dataWasUpdated) {
      const currentRemarks = row[remarksFieldName] || ''
      const originalRemarks = originalRow[remarksFieldName] || ''

      const remarksIsEmpty = currentRemarks.trim() === ''
      const remarksNotChanged = currentRemarks.trim() === originalRemarks.trim()

      return remarksIsEmpty || remarksNotChanged
    }

    return false
  })

  if (invalidRows.length > 0) {
    const displayNames = invalidRows
      .map((row) => row[displayFieldName] || `Row ${row.id}`)
      .join(', ')
    return `Remarks is required and must be different from previous for: ${displayNames}`
  }

  return ''
}

// Validation function for nested fields: Check if any nested field is updated, remarks must be filled and different from original
export const validateNestedRowDataWithRemarks = (
  data,
  originalRows,
  fieldsToCheck = [],
  displayFieldName = 'assetName',
  remarksFieldName = 'remarks',
) => {
  const invalidRows = data.filter((row) => {
    // Get original row from originalRows
    const originalRow = originalRows.find((orig) => orig.id === row.id)

    if (!originalRow) return false

    // Check if any data field was updated (supports nested paths like 'april.shutdownHrs')
    const dataWasUpdated = fieldsToCheck.some((field) => {
      return getNestedValue(row, field) !== getNestedValue(originalRow, field)
    })

    // If data was updated, check if remarks field is filled and different from original
    if (dataWasUpdated) {
      const currentRemarks = row[remarksFieldName] || ''
      const originalRemarks = originalRow[remarksFieldName] || ''

      const remarksIsEmpty = currentRemarks.trim() === ''
      const remarksNotChanged = currentRemarks.trim() === originalRemarks.trim()

      return remarksIsEmpty || remarksNotChanged
    }

    return false
  })

  if (invalidRows.length > 0) {
    const displayNames = invalidRows
      .map((row) => row[displayFieldName] || `Row ${row.id}`)
      .join(', ')
    return `Remarks is required and must be different from previous for: ${displayNames}`
  }

  return ''
}

export const isEmptyNullUndefined = (value, options = {}) => {
  const {
    trimStrings = true,
    whitespaceAsEmpty = true,
    treatNaNAsEmpty = false,
  } = options

  // null / undefined
  if (value === undefined || value === null) {
    // Examples:
    // isEmptyNullUndefined(undefined) → true
    // isEmptyNullUndefined(null) → true
    return true
  }

  const t = typeof value

  // Numbers
  if (t === 'number') {
    // Examples:
    // isEmptyNullUndefined(NaN, { treatNaNAsEmpty: true }) → true
    // isEmptyNullUndefined(10) → false
    return treatNaNAsEmpty && Number.isNaN(value)
  }

  // Boolean, BigInt, Function
  if (t === 'boolean' || t === 'bigint' || t === 'function') {
    // Examples:
    // isEmptyNullUndefined(true) → false
    // isEmptyNullUndefined(false) → false
    // isEmptyNullUndefined(5n) → false
    // isEmptyNullUndefined(() => {}) → false
    return false
  }

  // Strings
  if (t === 'string') {
    // Examples:
    // isEmptyNullUndefined("") → true
    // isEmptyNullUndefined("   ") → true (trimStrings & whitespaceAsEmpty on)
    // isEmptyNullUndefined("   ", { whitespaceAsEmpty: false }) → false
    if (trimStrings) {
      const s = value.trim()
      return whitespaceAsEmpty ? s.length === 0 : value.length === 0
    }
    return value.length === 0
  }

  // Dates
  if (value instanceof Date) {
    // Examples:
    // isEmptyNullUndefined(new Date("invalid")) → true
    // isEmptyNullUndefined(new Date()) → false
    return Number.isNaN(value.getTime())
  }

  // Typed arrays
  if (ArrayBuffer.isView(value) && !(value instanceof DataView)) {
    // Examples:
    // isEmptyNullUndefined(new Uint8Array([])) → true
    // isEmptyNullUndefined(new Uint8Array([1])) → false
    return value.length === 0
  }

  // Arrays
  if (Array.isArray(value)) {
    // Examples:
    // isEmptyNullUndefined([]) → true
    // isEmptyNullUndefined([1,2]) → false
    return value.length === 0
  }

  // Map
  if (value instanceof Map) {
    // Examples:
    // isEmptyNullUndefined(new Map()) → true
    // isEmptyNullUndefined(new Map([['a', 1]])) → false
    return value.size === 0
  }

  // Set
  if (value instanceof Set) {
    // Examples:
    // isEmptyNullUndefined(new Set()) → true
    // isEmptyNullUndefined(new Set([1])) → false
    return value.size === 0
  }

  // Plain objects only
  const isPlainObject = (obj) => {
    if (obj === null || typeof obj !== 'object') return false
    const proto = Object.getPrototypeOf(obj)
    return proto === Object.prototype || proto === null
  }

  if (isPlainObject(value)) {
    // Examples:
    // isEmptyNullUndefined({}) → true
    // isEmptyNullUndefined({ a: 1 }) → false
    return Object.keys(value).length === 0
  }

  // Other objects (RegExp, Error, File, etc.)
  // Examples:
  // isEmptyNullUndefined(/abc/) → false
  // isEmptyNullUndefined(new Error()) → false
  return false
}

export const recalcEndDate = (startRaw, durationStr, requiredInHr = true) => {
  if (!startRaw) return null
  const start = new Date(startRaw)
  if (!(start instanceof Date) || isNaN(start)) return null
  
  if (requiredInHr) {
    // parse "HH.MM" for hours
    const [hrsPart, minPart = '0'] = String(durationStr).split('.')
    const hrs = parseInt(hrsPart, 10)
    const mins = parseInt(minPart.padEnd(2, '0').slice(0, 2), 10)
    if (isNaN(hrs) || isNaN(mins) || mins < 0 || mins > 59) return null
    const end = new Date(start.getTime() + (hrs * 60 + mins) * 60000)
    return end
  } else {
    // parse as days (can be decimal like 10.5 days)
    const days = parseFloat(durationStr)
    if (isNaN(days) || days < 0) return null
    const end = new Date(start.getTime() + days * 24 * 60 * 60000)
    return end
  }
}

export const recalcDuration = (startRaw, endRaw, requiredInHr = true) => {
  const start = startRaw ? new Date(startRaw) : null
  const end = endRaw ? new Date(endRaw) : null
  if (
    start instanceof Date &&
    !isNaN(start) &&
    end instanceof Date &&
    !isNaN(end)
  ) {
    const diffMs = end.getTime() - start.getTime()
    if (diffMs < 0) return ''
    
    if (requiredInHr) {
      // Calculate in hours with format "H.MM"
      const totalMins = Math.floor(diffMs / 60000)
      const hrs = Math.floor(totalMins / 60)
      const mins = totalMins % 60
      return `${hrs}.${mins.toString().padStart(2, '0')}`
    } else {
      // Calculate in days (decimal format)
      const totalDays = diffMs / (24 * 60 * 60000)
      return totalDays.toFixed(2)
    }
  }
  return ''
}

export default function valueFormatterByUOM(value, unit = null) {
  // Return original if null, undefined, or empty string
  if (value === null || value === undefined || value === '') {
    return value
  }

  const unitsConfig = {
   oneDigitUnits : [],
   twoDigitUnits : [],
   threeDigitUnits : ['MT'],
   fourDigitUnits : [],
   fiveDigitUnits : ['KG'],
  };
  // Convert to number safely
  const numValue = Number(value)
  if (Number.isNaN(numValue)) {
    return value // Not a valid number; return original
  }
  const u = unit?.toUpperCase()

  if (unitsConfig.oneDigitUnits.includes(u)) {
    return numValue.toFixed(1)
  } else if (unitsConfig.twoDigitUnits.includes(u)) {
    return numValue.toFixed(2)
  } else if (unitsConfig.threeDigitUnits.includes(u)) {
    return numValue.toFixed(3)
  } else if (unitsConfig.fourDigitUnits.includes(u)) {
    return numValue.toFixed(4)
  } else if (unitsConfig.fiveDigitUnits.includes(u)) {
    return numValue.toFixed(5)
  }

  // Default format
  return numValue.toFixed(3)
}

