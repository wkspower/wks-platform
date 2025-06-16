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
    // console.log('field', field)
    const title = String(headerMap[colDef.headerName] || colDef.headerName)
    const isTextCol = !(colDef.type == 'number')

    return {
      field,
      title,
      filterable: true,
      filter: isTextCol ? 'text' : 'numeric',
      isRightAlligned: isTextCol ? 'text' : 'numeric',
      format: isTextCol ? undefined : '{0:#.###}',
      ...(isTextCol ? {} : { format: '{0:#.###}' }),

      editable: false,
      align: isTextCol ? 'left' : 'right',
    }
  })
}
