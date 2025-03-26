import { useSelector } from 'react-redux'
import productionColDefs from '../../../assets/consumption_aop.json'
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'

const getEnhancedColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
}) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const enhancedColDefs = productionColDefs.map((col) => {
    // For the product/grade column, override headerName based on vertical:
    if (col.field === 'NormParametersId') {
      return {
        ...col,
        headerName: lowerVertName === 'meg' ? 'Product Norm' : 'Spec',
        valueGetter: (params) => params || '',
        valueFormatter: (params) => {
          const product = allProducts.find((p) => p.id === params)
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
    // For the remark column, add custom renderCell.
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
    // For month columns, override headerName using headerMap if available.
    // Month columns in JSON have headerName as a number.
    const monthFields = [
      'apr24',
      'may24',
      'jun24',
      'jul24',
      'aug24',
      'sep24',
      'oct24',
      'nov24',
      'dec24',
      'jan25',
      'feb25',
      'mar25',
    ]
    if (monthFields.includes(col.field)) {
      const key = col.headerName // This is a number.
      return {
        ...col,
        headerName: headerMap && headerMap[key] ? headerMap[key] : key,
      }
    }
    return col
  })
  return enhancedColDefs
}

export default getEnhancedColDefs
