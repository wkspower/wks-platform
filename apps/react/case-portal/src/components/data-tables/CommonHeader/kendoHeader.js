// src/utils/getKendoColumns.js
import NumericInputOnly from 'utils/NumericInputOnly'

export default function getKendoColumns({
  headerMap,
  type,
  headers2 = [],
  keys2 = [],
}) {
  let rawCols
  // const ThreeDecimalCell = (props) => {
  //   const raw = props.dataItem[props.field]
  //   const value = raw === 0 ? '0.000' : raw ? parseFloat(raw).toFixed(3) : ''

  //   return <td>{value}</td>
  // }

  switch (type) {
    case 'Production':
      rawCols = require('../../../assets/annual_aop_cost_report_production.json')
      break
    case 'Price':
      rawCols = require('../../../assets/annual_aop_cost_report_price.json')
      break
    case 'Norm':
      rawCols = require('../../../assets/annual_aop_cost_report_norm.json')
      break
    case 'Quantity':
      rawCols = require('../../../assets/annual_aop_cost_report_quantity.json')
      break
    case 'NormCost':
      rawCols = require('../../../assets/annual_aop_cost_report_norm_cost.json')
      break
    default:
      throw new Error(`Unknown type "${type}"`)
  }

  if (type === 'Price') {
    return keys2.map((field, idx) => {
      const title = headers2[idx] || field
      const isTextCol = field === 'norm' || field === 'particulars'

      return {
        field,
        title,
        filterable: true,
        filter: isTextCol ? 'text' : 'numeric',
        isRightAlligned: isTextCol ? 'text' : 'numeric',
        format: isTextCol ? undefined : '{0:n3}',
        ...(isTextCol ? {} : { format: '{0:n3}' }),

        editable: false,
        align: isTextCol ? 'left' : 'right',
      }
    })
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
      format: isTextCol ? undefined : '{0:n3}',
      ...(isTextCol ? {} : { format: '{0:n3}' }),

      editable: false,
      align: isTextCol ? 'left' : 'right',
    }
  })
}
