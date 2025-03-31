// import { useSelector } from 'react-redux'
import NumericInputOnly from 'utils/NumericInputOnly'
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'
import baseColDefs from '../../../assets/slowdown.json'

const useEnhancedSlowdownColDefs = ({ allProducts, handleRemarkCellClick }) => {
  //   const dataGridStore = useSelector((state) => state.dataGridStore)
  //   const { verticalChange } = dataGridStore
  //   const vertName = verticalChange?.selectedVertical
  //   const lowerVertName = vertName?.toLowerCase() || 'meg'

  // Enhance each column from our base JSON
  const enhancedColDefs = baseColDefs.map((col) => {
    let updatedCol = { ...col }

    // // If a headerMap is provided, override the headerName
    // if (headerMap && headerMap[col.headerName]) {
    //   updatedCol.headerName = headerMap[col.headerName]
    // }

    // Handle the "product" field: add value getter/formatter and a custom edit cell (dropdown)
    if (col.field === 'product') {
      updatedCol.valueGetter = (params) => params.value || ''
      updatedCol.valueFormatter = (params) => {
        const product = allProducts.find((p) => p.id === params.value)
        return product ? product.displayName : ''
      }
      updatedCol.renderEditCell = (params) => {
        const { value, id, api } = params
        const existingValues = new Set(
          [...api.getRowModels().values()]
            .filter((row) => row.id !== id)
            .map((row) => row.product),
        )
        return (
          <select
            value={value || ''}
            onChange={(event) =>
              api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: event.target.value,
              })
            }
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
            {allProducts
              .filter(
                (product) =>
                  product.id === value || !existingValues.has(product.id),
              )
              .map((product) => (
                <option key={product.id} value={product.id}>
                  {product.displayName}
                </option>
              ))}
          </select>
        )
      }
    }

    // Handle date/time fields (maintStartDateTime and maintEndDateTime)
    if (
      col.field === 'maintStartDateTime' ||
      col.field === 'maintEndDateTime'
    ) {
      updatedCol.valueGetter = (params) => {
        const value = params.value
        return value ? new Date(value) : null
      }
      updatedCol.valueFormatter = (params) =>
        params.value ? new Date(params.value).toLocaleString() : ''
    }

    // For "durationInHrs", add a valueGetter to calculate duration based on start/end dates
    if (col.field === 'durationInHrs') {
      updatedCol.renderEditCell = NumericInputOnly
      updatedCol.valueGetter = (params) => {
        const { maintStartDateTime, maintEndDateTime } = params.row
        const start = new Date(maintStartDateTime)
        const end = new Date(maintEndDateTime)
        if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
          const durationMs = end - start
          const hours = Math.floor(durationMs / (1000 * 60 * 60))
          const minutes = Math.floor(
            (durationMs % (1000 * 60 * 60)) / (1000 * 60),
          )
          return `${hours}:${minutes < 10 ? '0' : ''}${minutes}`
        }
        return ''
      }
    }

    // For "rate", use a numeric input editor
    if (col.field === 'rate') {
      updatedCol.renderEditCell = NumericInputOnly
    }

    // For the "remark" field, add a tooltip and click handler to open a remark dialog
    if (col.field === 'remark') {
      updatedCol.renderCell = (params) => {
        const displayText = truncateRemarks(params.value)
        const isEditable = !params.row.Particulars // or any condition for editability
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

  return enhancedColDefs
}

export default useEnhancedSlowdownColDefs
