import production_coldefs_pe from '../../../assets/kendo_production_coldefs_pe.json' // adjust the path as needed
import production_coldefs_meg from '../../../assets/kendo_production_coldefs_meg.json' // adjust the path as needed
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

  // Function to add year suffix to month columns
  const addYearToMonthColumns = (columns) => {
    const currentDate = new Date()
    const currentMonth = currentDate.getMonth() // 0-11 (Jan=0, Dec=11)
    const currentYear = currentDate.getFullYear()

    // Month mapping for field names to month indices
    const monthFieldMap = {
      january: 0,
      february: 1,
      march: 2,
      april: 3,
      may: 4,
      june: 5,
      july: 6,
      august: 7,
      september: 8,
      october: 9,
      november: 10,
      december: 11,
    }

    return columns.map((col) => {
      const fieldLower = col.field?.toLowerCase()

      // Check if this is a month column
      // if (monthFieldMap.hasOwnProperty(fieldLower)) {
      //   const monthIndex = monthFieldMap[fieldLower]
      //   let year = currentYear

      //   // April (3) to December (11) = current year
      //   // January (0) to March (2) = next year
      //   if (monthIndex >= 0 && monthIndex <= 2) { // Jan, Feb, Mar
      //     year = currentYear + 1
      //   }
      //   // April (3) to December (11) stay as current year

      //   // Get the last 2 digits of the year
      //   const yearSuffix = year.toString().slice(-2)

      //   // Append year suffix to existing title
      //   return {
      //     ...col,
      //     title: `${col.title}-${yearSuffix}`,
      //     // Also update headerName if it exists
      //     headerName: col.headerName ? `${col.headerName}-${yearSuffix}` : undefined
      //   }
      // }

      return col
    })
  }

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const hiddenFields = ['idFromApi', 'aopCaseId', 'isEditable', 'avgTph']

  // Get base column definitions and add years to month columns
  const baseColumns =
    lowerVertName === 'meg' ? production_coldefs_meg : production_coldefs_pe
  const columnsWithYears = addYearToMonthColumns(baseColumns)

  return (
    columnsWithYears
      .map((col) => {
        let updatedCol = { ...col }

        if (headerMap && headerMap[col.headerName]) {
          updatedCol.headerName = headerMap[col.headerName]
        }

        if (col.field === 'normParametersFKId') {
          updatedCol = {
            ...updatedCol,
            valueGetter: (params) => params.row?.normParametersFKId || '',
            valueFormatter: (params) => {
              const product = allProducts.find((p) => p.id === params.value)
              return product ? product.displayName : ''
            },
            filterOperators: [
              {
                label: 'contains',
                value: 'contains',
                getApplyFilterFn: (filterItem) => {
                  if (!filterItem?.value) return
                  return (rowId) => {
                    const filterValue = filterItem.value.toLowerCase()
                    const productName = getProductDisplayName(rowId)
                    return (
                      productName?.toLowerCase().includes(filterValue) ?? true
                    )
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
            ...updatedCol,
            renderEditCell: NumericInputOnly,
            valueFormatter: formatValueToThreeDecimals,
            headerName: headerMap[col.headerName],
            align: 'right',
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
                    width: '100%',
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
      // Filter out the hidden columns
      .filter((col) => !hiddenFields.includes(col.field))
  )
}

export default getEnhancedProductionColDefs
