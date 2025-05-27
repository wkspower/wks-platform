import NumericInputOnly from 'utils/NumericInputOnly'
const getEnhancedNormsHistorianBasis = ({ headerMap, type }) => {
  // console.log('headers2', headers2)

  // const formatValueToThreeDecimals = (params) =>
  //   params ? parseFloat(params).toFixed(3) : ''

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }

  let annual_aop_cost_report
  switch (type) {
    case 'HistorianValues':
      annual_aop_cost_report = require('../../../assets/norms_historian_basis_HistorianValues.json')
      break
    case 'McuAndNormGrid':
      annual_aop_cost_report = require('../../../assets/norms_historian_basis_McuAndNormGrid.json')
      break
    case 'ProductionVolumeData':
      annual_aop_cost_report = require('../../../assets/norms_historian_basis_ProductionVolumeData.json')
      break

    default:
      throw new Error('Invalid type provided')
  }

  return annual_aop_cost_report.map((col) => {
    let updatedCol = { ...col }
    if (headerMap && headerMap[col.headerName]) {
      updatedCol.headerName = headerMap[col.headerName]
    }

    if (
      col.field === 'total' ||
      col.field === 'normValue' ||
      col.field === 'actualQuantity'
    ) {
      return {
        ...updatedCol,
        valueFormatter: formatValueToThreeDecimals,
        flex: 1,
        align: 'right',
      }
    }

    if (headerMap && headerMap[col.headerName]) {
      return {
        ...col,
        renderEditCell: NumericInputOnly,
        valueFormatter: formatValueToThreeDecimals,
        headerName: headerMap[col.headerName],
        flex: 1,
        align: 'right',
      }
    }

    return updatedCol
  })
}

export default getEnhancedNormsHistorianBasis
