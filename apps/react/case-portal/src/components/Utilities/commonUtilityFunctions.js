// Helper function to get nested field value using dot notation
const getNestedValue = (obj, path) => {
  return path.split('.').reduce((current, prop) => current?.[prop], obj)
}

// Validation function: Check if any row data is updated, then remarks must be filled and different from original
export const validateRowDataWithRemarks = (data, originalRows, fieldsToCheck = ['april', 'may', 'june', 'july', 'aug', 'sept', 'oct', 'nov', 'dec', 'jan', 'feb', 'mar'], displayFieldName) => {
  const invalidRows = data.filter((row) => {
    // Get original row from originalRows
    const originalRow = originalRows.find((orig) => orig.id === row.id)
    
    if (!originalRow) return false
    
    // Check if any data field was updated
    const dataWasUpdated = fieldsToCheck.some((field) => {
      return row[field] !== originalRow[field]
    })

    // If data was updated, check if remarks is filled and different from original
    if (dataWasUpdated) {
      const currentRemarks = row.remarks || ''
      const originalRemarks = originalRow.remarks || ''

      const remarksIsEmpty = currentRemarks.trim() === ''
      const remarksNotChanged = currentRemarks.trim() === originalRemarks.trim()

      return remarksIsEmpty || remarksNotChanged
    }

    return false
  })

  if (invalidRows.length > 0) {
    const displayNames = invalidRows.map((row,) => row[displayFieldName] || `Row ${row.id}`).join(', ')
    return `Remarks is required and must be different from previous for: ${displayNames}`
  }

  return ''
}

// Validation function for nested fields: Check if any nested field is updated, remarks must be filled and different from original
export const validateNestedRowDataWithRemarks = (data, originalRows, fieldsToCheck = [], displayFieldName = 'assetName') => {
  const invalidRows = data.filter((row) => {
    // Get original row from originalRows
    const originalRow = originalRows.find((orig) => orig.id === row.id)
    
    if (!originalRow) return false
    
    // Check if any data field was updated (supports nested paths like 'april.shutdownHrs')
    const dataWasUpdated = fieldsToCheck.some((field) => {
      return getNestedValue(row, field) !== getNestedValue(originalRow, field)
    })

    // If data was updated, check if remarks is filled and different from original
    if (dataWasUpdated) {
      const currentRemarks = row.remarks || ''
      const originalRemarks = originalRow.remarks || ''

      const remarksIsEmpty = currentRemarks.trim() === ''
      const remarksNotChanged = currentRemarks.trim() === originalRemarks.trim()

      return remarksIsEmpty || remarksNotChanged
    }

    return false
  })

  if (invalidRows.length > 0) {
    const displayNames = invalidRows.map((row) => row[displayFieldName] || `Row ${row.id}`).join(', ')
    return `Remarks is required and must be different from previous for: ${displayNames}`
  }

  return ''
}

export function flattenMonthObject(data = []) {
  // If data is not an array, wrap it in an array
  const dataArray = Array.isArray(data) ? data : [data];
  
  // Map each item in the array to a flattened object
  return dataArray.map((item) => {
    const result = {};
    
    Object.entries(item).forEach(([key, value]) => {
      // Check if the value is a nested object (like month data)
      if (typeof value === "object" && value !== null && !Array.isArray(value)) {
        // Flatten nested month objects (e.g., apr: { norms, quantity } -> apr.norms, apr.quantity)
        Object.entries(value).forEach(([subKey, subVal]) => {
          result[`${key}_${subKey}`] = subVal;
        });
      } else {
        // Keep flat properties as-is (e.g., id, generatingPlant, etc.)
        result[key] = value;
      }
    });
    
    return result;
  });
}

export function unflattenMonthObject(data = []) {
  // If data is not an array, wrap it in an array
  const dataArray = Array.isArray(data) ? data : [data];
  
  // Map each flattened item back to nested structure
  return dataArray.map((flatObj) => {
    const result = {};
    
    Object.entries(flatObj).forEach(([key, value]) => {
      // Check if key contains underscore (indicates nested property like 'apr_norms')
      if (key.includes("_")) {
        const [month, field] = key.split("_");
        
        if (!result[month]) {
          result[month] = {};
        }
        
        // Map financialYearMonthId to financialYearMonthFkId within month objects
        if (field === "financialYearMonthId") {
          result[month]["financialYearMonthFkId"] = value;
        } else {
          result[month][field] = value;
        }
      } else {
        // Map normHeaderId to normsHeaderFkId for API payload
        if (key === "normHeaderId") {
          result["normsHeaderFkId"] = value;
        } else if (key === "inEdit") {
          // Skip inEdit field - don't include in payload
        } else {
          // Keep flat properties as-is (e.g., id, generatingPlant, etc.)
          result[key] = value;
        }
      }
    });
    
    return result;
  });
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
