import productionColDefs from '../../../assets/feed_stock.json'

const getEnhancedColDefs = ({ headerMap }) => {
  const enhancedColDefs = productionColDefs.map((col) => {
    if (headerMap && headerMap[col.headerName]) {
      return { ...col, headerName: headerMap[col.headerName] }
    }
    return col
  })
  return enhancedColDefs
}

export default getEnhancedColDefs
