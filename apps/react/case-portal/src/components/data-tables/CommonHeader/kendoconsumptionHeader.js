import productionColDefs from '../../../assets/kendo_consumption_aop.json'
import productionColDefs1 from '../../../assets/kendo_consumption_aop_cracker.json'

const getEnhancedColDefs = ({ headerMap, lowerVertName, valueFormat }) => {
  let colDefs = productionColDefs

  if (lowerVertName == 'cracker') {
    colDefs = productionColDefs1
  }
  const enhancedColDefs = colDefs.map((col) => {
    if (headerMap && headerMap[col.headerName] !== undefined) {
      col = {
        ...col,
        title: headerMap[col.headerName],
        type: 'number',
        format: valueFormat || '{0:#.###}',
        editable: false,
        width: 120,
      }
    }

    return col
  })

  return enhancedColDefs
}

export default getEnhancedColDefs
