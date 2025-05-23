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
    const finalKeys = keys2.length
      ? keys2
      : [
          'norm',
          'particulars',
          'january',
          'february',
          'march',
          'april',
          'may',
          'june',
          'july',
          'august',
          'september',
          'october',
          'november',
          'december',
          'total',
        ]

    return rawCols.map((colDef, idx) => {
      const field = finalKeys[idx]
      const title = String(headers2[idx] || colDef.headerName || field)
      const isTextCol = field === 'norm' || field === 'particulars'

      return {
        field,
        title,
        width: isTextCol ? 200 : 200,
        filterable: true,
        filter: isTextCol ? 'text' : 'numeric',
        format: isTextCol ? undefined : '{0:n3}',
        editable: !isTextCol,
        status: 'inactive',
        editor: !isTextCol
          ? (props) => <NumericInputOnly {...props} />
          : undefined,
      }
    })
  }

  // other report types: assume same styling logic
  return rawCols.map((colDef) => {
    const field = colDef.field
    const headerName = headerMap[colDef.headerName] || colDef.headerName
    const isTextCol = field === 'norm' || field === 'particulars'

    return {
      field,
      title: headerName,
      width:
        type !== 'NormCost'
          ? isTextCol
            ? 200
            : 140 // text columns get twice the flex share
          : undefined,
      // width: isTextCol ? 200 : 200,
      filterable: true,
      filter: isTextCol ? 'text' : 'numeric',
      format: isTextCol ? undefined : '{0:n3}',
      editable: !isTextCol,
      status: 'inactive',
      editor: !isTextCol
        ? (props) => <NumericInputOnly {...props} />
        : undefined,
    }
  })
}
