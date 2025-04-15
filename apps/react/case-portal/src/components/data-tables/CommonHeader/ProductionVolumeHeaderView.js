import production_coldefs_meg_view from '../../../assets/production_coldefs_meg_view.json'
import NumericInputOnly from 'utils/NumericInputOnly'
const getEnhancedProductionColDefsView = ({
  allProducts,
  headerMap,
  findAvg,
}) => {
  const formatValueToThreeDecimals = (params) =>
    params ? parseFloat(params).toFixed(3) : ''

  return production_coldefs_meg_view.map((col) => {
    let updatedCol = { ...col }

    if (headerMap && headerMap[col.headerName]) {
      updatedCol.headerName = headerMap[col.headerName]
    }

    if (col.field === 'normParametersFKId') {
      updatedCol = {
        ...updatedCol,

        valueGetter: (params) => params || '',
        valueFormatter: (params) => {
          const product = allProducts.find((p) => p.id === params)
          return product ? product.displayName : ''
        },
      }
    }

    if (col.field === 'avgTph') {
      updatedCol.valueGetter = findAvg
    }

    if (headerMap && headerMap[col.headerName]) {
      return {
        ...col,
        renderEditCell: NumericInputOnly,
        valueFormatter: formatValueToThreeDecimals,
        headerName: headerMap[col.headerName],
        flex: 1,
      }
    }
    return updatedCol
  })
}

export default getEnhancedProductionColDefsView
