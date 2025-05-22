// import { useSelector } from 'react-redux'
import { useSelector } from 'react-redux'
import productionColDefs from '../../../assets/production_aop_meg.json' // Adjust path as needed
import productionColDefsPE from '../../../assets/production_aop_pe.json' // Adjust path as needed
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'
import NumericInputOnly from 'utils/NumericInputOnly'

// import Autocomplete from '@mui/material/Autocomplete'
import TextField from '@mui/material/TextField'

const getEnhancedColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
  findSum,
}) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const getProductDisplayName = (id) => {
    if (!id) return
    const product = allProducts.find((p) => p.id === id)
    return product ? product.displayName : ''
  }

  const formatValueToThreeDecimals = (params) =>
    params ? parseFloat(params).toFixed(3) : ''

  const formatValueToTwoDecimals = (params) =>
    params ? parseFloat(params).toFixed(2) : ''

  let cols

  if (lowerVertName == 'pe') {
    cols = productionColDefsPE
  } else {
    cols = productionColDefs
  }

  const enhancedColDefs = cols.map((col) => {
    let updatedCol = { ...col }

    // For the normParametersFKId column, change the header based on vertical:
    if (col.field === 'normParametersFKId') {
      updatedCol = {
        ...updatedCol,
        headerName: 'Particulars',
        valueGetter: (params) => params || '',
        renderCell: (params) => {
          // console.log(params?.row)
          if (params?.row?.id === 'total') {
            return params?.row?.Particulars
          } else {
            const product = allProducts.find(
              (p) => p.id === params?.row?.normParametersFKId,
            )
            return product ? product.displayName : ''
          }
        },
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
                  field: 'normParametersFKId',
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

    // For the remark column, add a custom renderCell.
    if (col.field === 'aopRemarks') {
      updatedCol = {
        ...updatedCol,
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

    // For the "Total" column, use findSum to compute the value.
    if (col.field === 'averageTPH') {
      updatedCol.valueGetter = findSum
    }

    // Optionally, override headerName using headerMap if provided.
    if (headerMap && headerMap[col.headerName] !== undefined) {
      updatedCol = {
        ...updatedCol,
        headerName: headerMap[col.headerName],
        valueFormatter: formatValueToTwoDecimals,
        renderEditCell: NumericInputOnly,
        align: 'right',
      }
    }

    return updatedCol
  })

  return enhancedColDefs
}

export default getEnhancedColDefs
