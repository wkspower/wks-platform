// import { useSelector } from 'react-redux'
import productionColDefs from '../../../assets/production_aop.json' // Adjust path as needed
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'

const getEnhancedColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
  findSum,
}) => {
  // const dataGridStore = useSelector((state) => state.dataGridStore)
  // const { verticalChange } = dataGridStore
  // const vertName = verticalChange?.selectedVertical
  // const lowerVertName = vertName?.toLowerCase() || 'meg'

  const enhancedColDefs = productionColDefs.map((col) => {
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
          // console.log(params)
          const product = allProducts.find((p) => p.id === params)
          // if (product?.displayName?.length > 0)
          return product ? product.displayName : ''
        },
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

    // For the "Total" column, use findSum to compute the value.
    if (col.field === 'averageTPH') {
      updatedCol.valueGetter = findSum
    }

    // Optionally, override headerName using headerMap if provided.
    if (headerMap && headerMap[col.headerName] !== undefined) {
      updatedCol = {
        ...updatedCol,
        headerName: headerMap[col.headerName],
      }
    }

    return updatedCol
  })

  return enhancedColDefs
}

export default getEnhancedColDefs
