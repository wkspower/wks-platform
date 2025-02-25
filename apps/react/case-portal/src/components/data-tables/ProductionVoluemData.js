import { DataService } from 'services/DataService'
import { Autocomplete, TextField } from '@mui/material'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'

const ProductionvolumeData = () => {
  const keycloak = useSession()
  const [productOptions, setProductOptions] = useState([])
  const [productionData, setProductionData] = useState([])

  useEffect(() => {
    getAllProducts()
  }, [])

  const getAllProducts = async () => {
    try {
      const data = await DataService.getAllProducts(keycloak)
      console.log('API Response:', data)
      // Assuming each product object has a "name" property
      const products = data.map((item) => item.displayName || item.name || item)
      setProductOptions(products)
    } catch (error) {
      console.error('Error fetching product:', error)
    }
  }

  // Create row data based on the productOptions list
  useEffect(() => {
    if (productOptions.length > 0) {
      const rows = productOptions.map((option, index) => ({
        id: index + 1,
        product: option,
        apr24: Math.floor(Math.random() * 100),
        may24: Math.floor(Math.random() * 100),
        jun24: Math.floor(Math.random() * 100),
        jul24: Math.floor(Math.random() * 100),
        aug24: Math.floor(Math.random() * 100),
        sep24: Math.floor(Math.random() * 100),
        oct24: Math.floor(Math.random() * 100),
        nov24: Math.floor(Math.random() * 100),
        dec24: Math.floor(Math.random() * 100),
        jan25: Math.floor(Math.random() * 100),
        feb25: Math.floor(Math.random() * 100),
        mar25: Math.floor(Math.random() * 100),
        averageTPH: Math.floor(Math.random() * 100),
        remark: 'Good',
      }))
      setProductionData(rows)
    }
  }, [productOptions])

  const productionColumns = [
    {
      field: 'product',
      headerName: 'Product',
      editable: true,
      filterable: true,
      minWidth: 125,

      renderEditCell: (params) => {
        const isEditable = params.id > productOptions.length
        return (
          <Autocomplete
            options={productOptions}
            value={params.value || ''}
            disableClearable
            disabled={!isEditable}
            onChange={(event, newValue) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: newValue,
              })
            }}
            onInputChange={(event, newInputValue) => {
              if (event && event.type === 'keydown' && event.key === 'Enter') {
                params.api.setEditCellValue({
                  id: params.id,
                  field: 'product',
                  value: newInputValue,
                })
              }
            }}
            renderInput={(params) => (
              <TextField {...params} variant='outlined' size='small' />
            )}
            fullWidth
          />
        )
      },
    },
    { field: 'apr24', headerName: 'Apr-24', editable: true },
    { field: 'may24', headerName: 'May-24', editable: true },
    { field: 'jun24', headerName: 'Jun-24', editable: true },
    { field: 'jul24', headerName: 'Jul-24', editable: true },
    { field: 'aug24', headerName: 'Aug-24', editable: true },
    { field: 'sep24', headerName: 'Sep-24', editable: true },
    { field: 'oct24', headerName: 'Oct-24', editable: true },
    { field: 'nov24', headerName: 'Nov-24', editable: true },
    { field: 'dec24', headerName: 'Dec-24', editable: true },
    { field: 'jan25', headerName: 'Jan-25', editable: true },
    { field: 'feb25', headerName: 'Feb-25', editable: true },
    { field: 'mar25', headerName: 'Mar-25', editable: true },
    {
      field: 'averageTPH',
      headerName: 'Average TPH',
      width: 150,
      editable: false,
      renderHeader: () => (
        <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
          <div>Average</div>
          <div>TPH</div>
        </div>
      ),
    },
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
  ]

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={productionData}
        title='Product Volume Data'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
        }}
      />
    </div>
  )
}

export default ProductionvolumeData
