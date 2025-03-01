import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
import { DataService } from 'services/DataService'
import { useEffect, useState } from 'react'

const productionColumns = [
  {
    field: 'stock',
    headerName: 'Feed Stock',
    width: 150,
    editable: true,
    renderHeader: () => (
      <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
        <div>Feed</div>
        <div>Stock</div>
      </div>
    ),
  },
  { field: 'apr24', headerName: 'Apr-24', width: 100, editable: true },
  { field: 'may24', headerName: 'May-24', width: 100, editable: true },
  { field: 'jun24', headerName: 'Jun-24', width: 100, editable: true },
  { field: 'jul24', headerName: 'Jul-24', width: 100, editable: true },
  { field: 'aug24', headerName: 'Aug-24', width: 100, editable: true },
  { field: 'sep24', headerName: 'Sep-24', width: 100, editable: true },
  { field: 'oct24', headerName: 'Oct-24', width: 100, editable: true },
  { field: 'nov24', headerName: 'Nov-24', width: 100, editable: true },
  { field: 'dec24', headerName: 'Dec-24', width: 100, editable: true },
  { field: 'jan25', headerName: 'Jan-25', width: 100, editable: true },
  { field: 'feb25', headerName: 'Feb-25', width: 100, editable: true },
  { field: 'mar25', headerName: 'Mar-25', width: 100, editable: true },
]

const FeedStockAvailability = () => {
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
      const products = data.map((item) => item.displayName || item.name || item)
      setProductOptions(products)
    } catch (error) {
      console.error('Error fetching product:', error)
    }
  }
  useEffect(() => {
    if (productOptions.length > 0) {
      const rows = productOptions.map((option, index) => ({
        id: index + 1,
        stock: option,
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
      }))
      setProductionData(rows)
    }
  }, [productOptions])

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={productionData}
        title='Feed Stock Availability'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[10, 20, 30]}
        permissions={{
          showAction: true,
          addButton: true,
          deleteButton: true,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default FeedStockAvailability
