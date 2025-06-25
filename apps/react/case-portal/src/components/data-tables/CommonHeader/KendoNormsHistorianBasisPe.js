export default function getKendoNormsHistorianBasisPe({ headerMap, type }) {
  let rawCols
  switch (type) {
    case 'RAW MCU':
      rawCols = require('../../../assets/norms_historian_basis_raw_mcu.json')
      break
    case 'MCU WITHIN RANGE':
      rawCols = require('../../../assets/norms_historian_basis_mcu_within_range.json')
      break
    case 'MCU RANGE':
      rawCols = require('../../../assets/norms_historian_basis_mcu_range.json')
      break
    case 'AVG ANNUAL NORMS':
      rawCols = require('../../../assets/norms_historian_basis_avg_annual_norms.json')
      break
    case 'CONSECUTIVE DAYS':
      rawCols = require('../../../assets/norms_historian_basis_consecutive_days.json')
      break
    case 'MIIS NORMS RAW DATA':
      rawCols = require('../../../assets/norms_historian_basis_miis_norms_raw_data.json')
      break
    case 'BEST ACHIEVED NORMS':
      rawCols = require('../../../assets/norms_historian_basis_best_achieved_norms.json')
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
