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
