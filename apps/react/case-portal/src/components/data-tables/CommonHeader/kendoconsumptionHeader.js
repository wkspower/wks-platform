import productionColDefs from '../../../assets/kendo_consumption_aop.json'
import productionColDefsElastomer from '../../../assets/kendo_consumption_aop_elastomer.json'
import productionColDefs1 from '../../../assets/kendo_consumption_aop_cracker.json'
import productionColDefsVcm from '../../../assets/kendo_consumption_aop_vcm.json'
import productionColDefsVcmDmd from '../../../assets/kendo_consumption_aop_vcmdmd.json'
const getEnhancedColDefs = ({
  headerMap,
  lowerVertName,
  lowerSiteName,
  lowerPlantName,
  valueFormat,
}) => {
  let colDefs = productionColDefs

  // console.log('lowerVertName', lowerVertName)

  if (
    lowerVertName === 'vcm' &&
    lowerSiteName === 'dmd' &&
    lowerPlantName === 'vcm'
  ) {
    colDefs = productionColDefsVcmDmd
  } else if (lowerVertName === 'cracker') {
    colDefs = productionColDefs1
  } else if (lowerVertName === 'elastomer') {
    colDefs = productionColDefsElastomer
  } else if (lowerVertName === 'vcm') {
    colDefs = productionColDefsVcm
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
    if (col.field === 'wtAverage') {
      return {
        ...col,
        type: 'number',
        format: valueFormat || '{0:#.###}',
      }
    }

    return col
  })

  return enhancedColDefs
}

export default getEnhancedColDefs
