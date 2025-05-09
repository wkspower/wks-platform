import { useSelector } from 'react-redux'
import vertical_meg_coldefs_bd from '../../../assets/vertical_meg_coldefs_bd.json'
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
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const getProductDisplayName = (id) => {
    if (!id) return
    const product = allProducts.find((p) => p.id === id)
    return product ? product.displayName : ''
  }

  const enhancedColDefs = (
    lowerVertName === 'meg' ? vertical_meg_coldefs_bd : vertical_pe_coldefs_bd
  ).map((col) => {
    if (col.field === 'normParameterId') {
      return {
        ...col,
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
                label='Value'
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

          const allProductOptions = allProducts.map((product) => ({
            value: product.id,
            label: product.displayName,
          }))

          const existingValues = new Set(
            [...api.getRowModels().values()]
              .filter((row) => row.id !== id)
              .map((row) => row.normParameterId),
          )

          const filteredOptions = allProductOptions.filter(
            (option) =>
              option.value === value || !existingValues.has(option.value),
          )

          return (
            <Autocomplete
              value={
                allProductOptions.find((option) => option.value === value) ||
                (params.row.product &&
                  allProductOptions.find(
                    (opt) => opt.value === params.row.product,
                  )) ||
                null
              }
              options={filteredOptions}
              // forcePopupIcon={false}
              disableClearable
              getOptionLabel={(option) => option?.label || ''}
              onChange={(event, newValue) => {
                api.setEditCellValue({
                  id,
                  field: 'normParameterId',
                  value: newValue?.value || '',
                })
              }}
              renderInput={(params) => (
                <TextField
                  {...params}
                  variant='outlined'
                  size='small'
                  fullWidth
                  style={{ width: '150px' }}
                />
              )}
            />
          )
        },
      }
    }

    if (col.field === 'remark') {
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
                  maxWidth: 140,
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

    if (headerMap && headerMap[col.headerName]) {
      return {
        ...col,
        renderEditCell: NumericInputOnly,
        headerName: headerMap[col.headerName],
        align: 'right',
      }
    }
    if (col.field === 'Particulars') {
      return {
        ...col,
        filterable: false,
        renderCell: (params) => <strong>{params.value}</strong>,
      }
    }
    return col
  })
  return enhancedColDefs
}

export default getEnhancedColDefs
