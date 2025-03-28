import { useSelector } from 'react-redux'
import productionColDefs from '../../../assets/consumption_aop.json'
import Tooltip from '@mui/material/Tooltip'
import { truncateRemarks } from 'utils/remarksUtils'
import NumericInputOnly from 'utils/NumericInputOnly'

const getEnhancedColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
}) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const formatValueToThreeDecimals = (params) =>
    params ? parseFloat(params).toFixed(3) : ''

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
                  maxWidth: 200,
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
        valueFormatter: formatValueToThreeDecimals,
      }
    }
    return col
  })
  return enhancedColDefs
}

export default getEnhancedColDefs
