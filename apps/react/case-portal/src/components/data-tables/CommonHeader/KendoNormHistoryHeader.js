import NumericInputOnly from 'utils/NumericInputOnly'

export default function getKendoNormsHistorianColumns({ headerMap, type }) {
  // load the right JSON asset
  let rawCols
  switch (type) {
    case 'HistorianValues':
      rawCols = require('../../../assets/norms_historian_basis_HistorianValues.json')
      break
    case 'McuAndNormGrid':
      rawCols = require('../../../assets/norms_historian_basis_McuAndNormGrid.json')
      break
    case 'ProductionVolumeData':
      rawCols = require('../../../assets/norms_historian_basis_ProductionVolumeData.json')
      break
    default:
      throw new Error(`Unknown type "${type}"`)
  }

  //   // formatter to 3 decimals
  //   const formatValueToThreeDecimals = (value) => {
  //     if (value === 0) return '0.000'
  //     if (value == null) return ''
  //     return Number(value).toFixed(3)
  //   }

  return rawCols.map((colDef) => {
    const field = colDef.field
    const title = String(headerMap[colDef.headerName] || colDef.headerName)

    // determine if numeric column
    const isNumeric =
      field === 'total' || field === 'normValue' || field === 'actualQuantity'

    // text columns (all others) get larger min width
    const isText = !isNumeric

    return {
      field,
      title,

      width:
        type !== 'McuAndNormGrid'
          ? isText
            ? 200
            : 140 // text columns get twice the flex share
          : undefined,
      filterable: true,
      filter: isNumeric ? 'numeric' : 'text',
      // numeric formatting
      format: isText ? undefined : '{0:n3}',
      editable: isNumeric,
      editor: isNumeric
        ? (props) => <NumericInputOnly {...props} />
        : undefined,
      align: isNumeric ? 'right' : 'left',
    }
  })
}
