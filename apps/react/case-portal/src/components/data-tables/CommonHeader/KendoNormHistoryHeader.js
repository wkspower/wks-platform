import NumericInputOnly from 'utils/NumericInputOnly'
import { Tooltip } from '@progress/kendo-react-tooltip'

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

  return rawCols.map((colDef) => {
    const field = colDef.field
    // console.log('field', field)
    const title = String(headerMap[colDef.headerName] || colDef.headerName)
    const isTextCol =
      field === 'material' ||
      field === 'materialName' ||
      field === 'norms' ||
      field === 'particulars' ||
      field === 'srNo' ||
      field === 'normDateTime'

    return {
      field,
      title,

      width:
        type !== 'McuAndNormGrid'
          ? isTextCol
            ? 200
            : 140 // text columns get twice the flex share
          : undefined,
      filterable: true,
      filter: isTextCol ? 'text' : 'numeric',
      format: isTextCol ? undefined : '{0:n3}',
      ...(isTextCol ? {} : { format: '{0:n3}' }),

      editable: !isTextCol,
      editor: !isTextCol
        ? (props) => <NumericInputOnly {...props} />
        : undefined,
      align: isTextCol ? 'left' : 'right',

      cell: (props) => {
        const rawValue = props.dataItem[props.field]
        const isText = isTextCol
        const displayValue = isText
          ? rawValue
          : Number(rawValue)?.toLocaleString(undefined, {
              minimumFractionDigits: 3,
              maximumFractionDigits: 3,
            })

        return (
          <td style={{ textAlign: isText ? 'left' : 'right' }}>
            <Tooltip anchorElement='target' position='top'>
              <span style={{ display: 'inline-block', width: '100%' }}>
                {displayValue}
              </span>
            </Tooltip>
          </td>
        )
      },
    }
  })
}
