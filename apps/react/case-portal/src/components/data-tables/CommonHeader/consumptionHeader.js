import { useSelector } from 'react-redux'
import productionColDefs from '../../../assets/consumption_aop.json'
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'
import NumericInputOnly from 'utils/NumericInputOnly'
import TextField from '@mui/material/TextField'

const getEnhancedColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
}) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  // const formatValueToFiveDecimals = (params) =>
  //   params ? parseFloat(params).toFixed(3) : ''

  const formatValueToFiveDecimals = (params) => {
    return params === 0 ? 0 : params ? parseFloat(params).toFixed(5) : ''
  }

  const getProductDisplayName = (id) => {
    if (!id) return
    const product = allProducts.find((p) => p.id === id)
    return product ? product.displayName : ''
  }

  const enhancedColDefs = productionColDefs.map((col) => {
    if (col.field === 'NormParametersId') {
      return {
        ...col,
        headerName: lowerVertName === 'meg' ? 'Particulars' : 'Particulars',
        valueGetter: (params) => params || '',
        valueFormatter: (params) => {
          const product = allProducts.find((p) => p.id === params)
          return product ? product.displayName : ''
        },

        filterOperators: [
          {
            label: 'contains',
            value: 'contains',
            getApplyFilterFn: (filterItem) => {
              if (!filterItem?.value) {
                return
              }
              return (rowId) => {
                const filterValue = filterItem.value.toLowerCase()
                if (filterValue) {
                  const productName = getProductDisplayName(rowId)
                  if (productName) {
                    return productName.toLowerCase().includes(filterValue)
                  }
                }
                return true
              }
            },
            InputComponent: ({ item, applyValue, focusElementRef }) => (
              <TextField
                autoFocus
                inputRef={focusElementRef}
                size='small'
                label='Contains'
                value={item.value || ''}
                onChange={(event) =>
                  applyValue({ ...item, value: event.target.value })
                }
                style={{ marginTop: '8px' }}
              />
            ),
          },
        ],

        renderEditCell: (params) => {
          const { value, id, api } = params
          return (
            <select
              value={value || ''}
              onChange={(event) => {
                api.setEditCellValue({
                  id,
                  field: 'NormParametersId',
                  value: event.target.value,
                })
              }}
              style={{
                width: '100%',
                padding: '5px',
                border: 'none',
                outline: 'none',
                background: 'transparent',
              }}
            >
              <option value='' disabled>
                Select
              </option>
              {allProducts.map((product) => (
                <option key={product.id} value={product.id}>
                  {product.displayName}
                </option>
              ))}
            </select>
          )
        },
      }
    }
    if (col.field === 'aopRemarks') {
      return {
        ...col,
        renderCell: (params) => {
          const displayText = truncateRemarks(params.value)
          const isEditable = !params.row.Particulars

          return (
            <Tooltip title={params.value || ''} arrow>
              <div
                style={{
                  cursor: 'pointer',
                  color: params.value ? 'inherit' : 'gray',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                  width: ' 100%',
                }}
                onClick={() => handleRemarkCellClick(params.row)}
              >
                {displayText || (isEditable ? 'Click to add remark' : '')}
              </div>
            </Tooltip>
          )
        },
      }
    }
    if (headerMap && headerMap[col.headerName] !== undefined) {
      col = {
        ...col,
        headerName: headerMap[col.headerName],
        renderEditCell: NumericInputOnly,
        valueFormatter: formatValueToFiveDecimals,
        align: 'right',
        renderCell: (params) => (
          <Tooltip
            title={params.value != null ? params.value.toString() : ''}
            arrow
          >
            <span>{formatValueToFiveDecimals(params.value)}</span>
          </Tooltip>
        ),
      }
    }
    if (col.field === 'Particulars') {
      return {
        ...col,
        renderCell: (params) => (
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
