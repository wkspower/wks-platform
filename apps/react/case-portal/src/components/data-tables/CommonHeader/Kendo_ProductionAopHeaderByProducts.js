import productionColDefs from '../../../assets/kendo_production_aop_meg_byproducts.json'

const getEnhancedColDefsByProducts = ({ headerMap, valueFormat }) => {
  let cols

  cols = productionColDefs

  const hasTotal = cols.some((col) => col.field === 'averageTPH')

  if (!hasTotal) {
    cols.push({
      field: 'averageTPH',
      title: 'Total',
    })
  }

  const enhancedColDefs = cols.map((col) => {
    let updatedCol = { ...col }

    if (headerMap && headerMap[col.title] !== undefined) {
      updatedCol.title = headerMap[col.title]
    }
    if (col.type === 'number' && valueFormat) {
      updatedCol.format = valueFormat
    }

    return updatedCol
  })

  return enhancedColDefs
}

export default getEnhancedColDefsByProducts
