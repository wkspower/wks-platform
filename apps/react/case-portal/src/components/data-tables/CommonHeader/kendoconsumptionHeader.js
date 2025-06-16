import productionColDefs from '../../../assets/kendo_consumption_aop.json'

const getEnhancedColDefs = ({ headerMap }) => {
  const enhancedColDefs = productionColDefs.map((col) => {
    if (headerMap && headerMap[col.headerName] !== undefined) {
      col = {
        ...col,
        title: headerMap[col.headerName],
        type: 'number',
        format: '{0:#.#####}',
        editable: false,
        width: 120,
      }
    }

    return col
  })

  return enhancedColDefs
}

export default getEnhancedColDefs
