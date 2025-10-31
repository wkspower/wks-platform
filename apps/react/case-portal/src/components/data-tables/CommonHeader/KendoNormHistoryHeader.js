export default function getKendoNormsHistorianColumns({ headerMap, type, valueFormat }) {
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
    case 'bestachieved':
      rawCols = require('../../../assets/norms_historian_basis_BestAchieved.json')
      break
    case 'expessionbased':
      rawCols = require('../../../assets/norms_historian_basis_ExpressionBased.json')
      break
    case 'currentyear':
      rawCols = require('../../../assets/norms_historian_basis_CurrentYear.json')
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
      format: isTextCol ? undefined : valueFormat || '{0:#.##}',
      widthT: colDef.widthT,
      editable: false,
      align: isTextCol ? 'left' : 'right',
    }
  })
}
