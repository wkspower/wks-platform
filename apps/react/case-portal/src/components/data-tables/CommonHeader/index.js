import { useSelector } from 'react-redux'
import vertical_meg_coldefs_bd from '../../../assets/vertical_meg_coldefs_bd.json'
import vertical_pe_coldefs_bd from '../../../assets/vertical_pe_coldefs_bd.json'
import NumericInputOnly from 'utils/NumericInputOnly'

const getEnhancedColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
}) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

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
        renderEditCell: (params) => {
          const { value, id, api } = params

          const existingValues = new Set(
            [...api.getRowModels().values()]
              .filter((row) => row.id !== id)
              .map((row) => row.normParameterId),
          )

          return (
            <select
              value={value || ''}
              onChange={(event) => {
                api.setEditCellValue({
                  id,
                  field: 'normParameterId',
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
              {allProducts
                .filter((product) => !existingValues.has(product.id))
                .map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.displayName}
                  </option>
                ))}
            </select>
          )
        },
      }
    }
    if (col.field === 'remark') {
      return {
        ...col,
        renderCell: (params) => (
          <div
            style={{
              cursor: 'pointer',
              color: params.value ? 'inherit' : 'gray',
            }}
            onClick={() => handleRemarkCellClick(params.row)}
          >
            {params.value}
          </div>
        ),
      }
    }
    if (headerMap && headerMap[col.headerName]) {
      return {
        ...col,
        renderEditCell: NumericInputOnly,
        headerName: headerMap[col.headerName],
      }
    }
    if (col.field === 'Particulars') {
      return {
        ...col,
        renderCell: (params) => <strong>{params.value}</strong>,
      }
    }
    return col
  })
  return enhancedColDefs
}

export default getEnhancedColDefs
