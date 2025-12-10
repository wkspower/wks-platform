import productionColDefs from '../../../assets/kendo_production_n_norm_basis_price_differential.json'

const getEnhancedColDefsPriceDifferential = ({ headerMap, valueFormat }) => {
  let cols

  cols = productionColDefs

  const enhancedColDefs = cols.map((col) => {
    let updatedCol = { ...col }

    if (col.type === 'number' && valueFormat) {
      updatedCol.format = valueFormat
    }

    return updatedCol
  })

  return enhancedColDefs
}

export default getEnhancedColDefsPriceDifferential
