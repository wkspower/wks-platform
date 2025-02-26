import { DataService } from 'services/DataService'
import { Autocomplete, TextField } from '@mui/material'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
// Import the catalyst options from the JSON file
// import catalystOptionsData from '../../assets/Catalyst.json'

const SelectivityData = () => {
  const keycloak = useSession()
  const [csData, setCsData] = useState([])
  const [allProducts, setAllProducts] = useState([])

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await DataService.getCatalystSelectivityData(keycloak)
        var formattedData = []
        if (data) {
           formattedData = data?.map((item, index) => ({
            ...item,
            id: index,
          }))
        }
        setCsData(formattedData)
      } catch (error) {
        console.error('Error fetching Turnaround data:', error)
      }
    }

    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }
    getAllProducts()
    fetchData()
  }, [])
  // Use catalyst options from the JSON file
  // const productOptions = catalystOptionsData.catalystOptions

  const productionColumns = [
    { field: 'catalyst', headerName: 'Catalyst', editable: true },
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
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
    
  ]


  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={csData}
        title='Catalyst Selectivity Data'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        permissions={{
          showAction: true,
          addButton: true,
          deleteButton: true,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
        }}
      />
    </div>
  )
}

export default SelectivityData
