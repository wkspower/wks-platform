import production_coldefs_pe from '../../../assets/production_coldefs_pe.json' // adjust the path as needed
import production_coldefs_meg from '../../../assets/production_coldefs_meg.json' // adjust the path as needed
import { useSelector } from 'react-redux'
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'
import NumericInputOnly from 'utils/NumericInputOnly'

import TextField from '@mui/material/TextField'

const getEnhancedProductionColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
  findAvg,
}) => {
  const formatValueToThreeDecimals = (params) =>
    params ? parseFloat(params).toFixed(3) : ''

  const getProductDisplayName = (id) => {
    if (!id) return
    const product = allProducts.find((p) => p.id === id)
    return product ? product.displayName : ''
  }

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  return (
    lowerVertName === 'meg' ? production_coldefs_meg : production_coldefs_pe
  ).map((col) => {
    let updatedCol = { ...col }

    // Override headerName using headerMap if available
    if (headerMap && headerMap[col.headerName]) {
      updatedCol.headerName = headerMap[col.headerName]
    }

    // Enhance the Product column with custom functions
    if (col.field === 'normParametersFKId') {
      updatedCol = {
        ...updatedCol,
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

    if (col.field === 'avgTph') {
      updatedCol.valueGetter = findAvg
    }

    if (headerMap && headerMap[col.headerName]) {
      return {
        ...col,
        renderEditCell: NumericInputOnly,
        valueFormatter: formatValueToThreeDecimals,

        headerName: headerMap[col.headerName],
      }
    }

    if (col.field === 'remarks') {
      updatedCol.renderCell = (params) => {
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
                maxWidth: 140,
              }}
              onClick={() => handleRemarkCellClick(params.row)}
            >
              {displayText || (isEditable ? 'Click to add remark' : '')}
            </div>
          </Tooltip>
        )
      }
    }

    return updatedCol
  })
}

export default getEnhancedProductionColDefs
