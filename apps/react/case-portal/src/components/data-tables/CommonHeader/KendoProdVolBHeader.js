import NumericInputOnly from 'utils/NumericInputOnly'

export default function getKendoProductionColumns({ headerMap, type }) {
  let rawCols
  switch (type) {
    case 'MC':
      rawCols = require('../../../assets/production_volume_data_basis_MC.json')
      break
    case 'MC Yearwise':
      rawCols = require('../../../assets/production_volume_data_basis_MC_Yearwise.json')
      break
    case 'Calculated Data':
      rawCols = require('../../../assets/production_volume_data_basis_Calculated_Data.json')
      break
    case 'RowData':
      rawCols = require('../../../assets/production_volume_data_basis_Row_Data.json')
      break
    default:
      throw new Error(`Unknown type "${type}"`)
  }

  return rawCols.map((colDef) => {
    const field = colDef.field
    const title = headerMap[colDef.headerName] || colDef.headerName
    const isTextCol = field === 'norm' || field === 'particulars'

    return {
      field,
      title,
      width:
        type === 'MC' || type === 'MC Yearwise'
          ? isTextCol
            ? 200
            : 140
          : undefined, // no width on the auto types
      flex:
        type === 'Calculated Data' || type === 'RowData'
          ? isTextCol
            ? 2
            : 1 // text columns get twice the flex share
          : undefined,
      filterable: true,
      filter: isTextCol ? 'text' : 'numeric',
      ...(isTextCol ? {} : { format: '{0:n3}' }),
      editable: !isTextCol,
      editor: !isTextCol
        ? (props) => <NumericInputOnly {...props} />
        : undefined,
      align: isTextCol ? 'left' : 'right',
    }
  })
}
