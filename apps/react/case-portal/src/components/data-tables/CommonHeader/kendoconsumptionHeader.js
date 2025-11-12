import productionColDefs from '../../../assets/kendo_consumption_aop.json'
import productionColDefsElastomer from '../../../assets/kendo_consumption_aop_elastomer.json'
import productionColDefs1 from '../../../assets/kendo_consumption_aop_cracker.json'
import productionColDefsVcm from '../../../assets/kendo_consumption_aop_vcm.json'
const getEnhancedColDefs = ({ headerMap, lowerVertName, valueFormat }) => {
  let colDefs = productionColDefs

  // console.log('lowerVertName', lowerVertName)

  if (lowerVertName == 'cracker') {
    colDefs = productionColDefs1
  }

  if (lowerVertName == 'elastomer') {
    colDefs = productionColDefsElastomer
  }
  if(lowerVertName == 'vcm'){
    colDefs = productionColDefsVcm // Using VCM columns for VCM as well
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

    if (col.field == 'avgOfAllMonths') {
      col = {
        ...col,
        format: valueFormat || '{0:#.###}',
        editable: false,
        type: 'number',
      }
    }

    return col
  })

  return enhancedColDefs
}

export default getEnhancedColDefs
