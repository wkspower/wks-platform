import { useSelector } from 'react-redux'
import vertical_meg_coldefs_bd from '../../../assets/kendo_businessdata_coldefs.json'
import vertical_pe_coldefs_bd from '../../../assets/vertical_pe_coldefs_bd.json'
import NumericInputOnly from 'utils/NumericInputOnly'
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'

import Autocomplete from '@mui/material/Autocomplete'
import TextField from '@mui/material/TextField'

const getEnhancedColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
}) => {
  //   const dataGridStore = useSelector((state) => state.dataGridStore)
  //   const { verticalChange } = dataGridStore
  //   const vertName = verticalChange?.selectedVertical
  //   const lowerVertName = vertName?.toLowerCase() || 'meg'
  //   console.log(allProducts)
  //   const getProductDisplayName = (id) => {
  //     console.log(id)
  //     if (!id) return
  //     const product = allProducts.find((p) => p.id === id)
  //     return product ? product.displayName : ''
  //   }

  const formatValueToThreeDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(3) : ''
  }

  const enhancedColDefs = vertical_meg_coldefs_bd.map((col) => {
    if (col.field === 'remark') {
      return {
        ...col,
        cell: ({ dataItem, field, ...tdProps }) => {
          const text = truncateRemarks(dataItem[field])
          const editable = Boolean(dataItem.isEditable)

          return (
            <td
              {...tdProps}
              style={{
                cursor: editable ? 'pointer' : 'not-allowed',
                color: dataItem[field] ? 'inherit' : 'gray',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
              }}
              onClick={() => {
                console.log(editable)
                editable && handleRemarkCellClick(dataItem)
              }}
            >
              {text || (editable ? 'Click to add remark' : '')}
            </td>
          )
        },
      }
    }

    if (headerMap && headerMap[col.title]) {
      return {
        ...col,
        cell: NumericInputOnly,
        title: headerMap[col.title],
        align: 'right',
        valueFormatter: formatValueToThreeDecimals,
      }
    }
    if (col.field === 'Particulars') {
      return {
        ...col,
        filterable: false,
        cell: (params) => (
          <div
            style={{
              whiteSpace: 'normal',
              wordBreak: 'break-word',
              lineHeight: 1.4,
            }}
          >
            <strong>{params.value}</strong>
          </div>
        ),
      }
    }
    return col
  })
  return enhancedColDefs
}

export default getEnhancedColDefs
