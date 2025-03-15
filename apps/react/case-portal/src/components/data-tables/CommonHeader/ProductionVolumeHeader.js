import production_coldefs_pe from '../../../assets/production_coldefs_pe.json' // adjust the path as needed
import production_coldefs_meg from '../../../assets/production_coldefs_meg.json' // adjust the path as needed
import { useSelector } from 'react-redux'

const getEnhancedProductionColDefs = ({
  allProducts,
  headerMap,
  handleRemarkCellClick,
  findAvg,
}) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.verticalChange?.selectedVertical
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

    if (col.field === 'remark') {
      updatedCol.renderCell = (params) => (
        <div
          style={{
            cursor: 'pointer',
            color: params.value ? 'inherit' : 'gray',
          }}
          onClick={() => handleRemarkCellClick(params.row)}
        >
          {params.value || 'Click to add remark'}
        </div>
      )
    }

    return updatedCol
  })
}

export default getEnhancedProductionColDefs
