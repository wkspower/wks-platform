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
        
        result[month][field] = value;
      } else {
        // Keep flat properties as-is (e.g., id, generatingPlant, etc.)
        result[key] = value;
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
