import productionColDefs from '../../../assets/maintenance_details.json' // adjust path as needed

const getEnhancedColDefs = ({ headerMap }) => {
  // If needed, you can use Redux to access other state here.
  // For this example, we only use the provided headerMap.
  const enhancedColDefs = productionColDefs.map((col) => {
    // For month columns (the field names for months)
    if (
      [
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
      ].includes(col.field)
    ) {
      // Here col.headerName holds the month number as a string (e.g., "4")
      const key = col.headerName
      return {
        ...col,
        headerName: headerMap[key] || col.headerName,
      }
    }
    return col
  })

  return enhancedColDefs
}

export default getEnhancedColDefs
