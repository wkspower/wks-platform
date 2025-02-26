import { DataService } from 'services/DataService'
import { Autocomplete, TextField } from '@mui/material'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'

const ProductionvolumeData = () => {
  const keycloak = useSession()

  const [productNormData, setProductNormData] = useState([])

  const [slowDownData, setSlowDownData] = useState([])
  const [allProducts, setAllProducts] = useState([])

  useEffect(() => {
      const fetchData = async () => {
        try {
          const data = await DataService.getAOPData(keycloak)
          const formattedData = data.map((item, index) => ({
            ...item,
            id: item.id,
    
          }))
          setProductNormData(formattedData)
        } catch (error) {
          console.error('Error fetching SlowDown data:', error)
        }
      }

      const getAllProducts = async () => {
        try {
          const data = await DataService.getAllProducts(keycloak);
          const productList = data.map((product) => ({
            id: product.id,
            displayName: product.displayName
          }));
      
          setAllProducts(productList);
          
        } catch (error) {
          console.error('Error fetching product:', error);
        } finally {
          // handleMenuClose();
        }
      }

      getAllProducts()
      fetchData()
    }, []
  )

  const productionColumns = [

    { field: 'id', headerName: 'id'},
   
    { field: 'aopType', headerName: 'aopType'},
    { field: 'aopYear', headerName: 'aopYear'},
    { field: 'plantFkId', headerName: 'plantFkId'},
    
    { field: 'normItem', headerName: 'Product', editable: false },
    { field: 'april', headerName: 'Apr-24', editable: true },
    { field: 'may', headerName: 'May-24', editable: true },
    { field: 'june', headerName: 'Jun-24', editable: true },
    { field: 'july', headerName: 'Jul-24', editable: true },
    { field: 'aug', headerName: 'Aug-24', editable: true },
    { field: 'sep', headerName: 'Sep-24', editable: true },
    { field: 'oct', headerName: 'Oct-24', editable: true },
    { field: 'nov', headerName: 'Nov-24', editable: true },
    { field: 'dec', headerName: 'Dec-24', editable: true },
    { field: 'jan', headerName: 'Jan-25', editable: true },
    { field: 'feb', headerName: 'Feb-25', editable: true },
    { field: 'march', headerName: 'Mar-25', editable: true },
    { field: 'aopCaseId', headerName: 'AOP Case ID', editable: false },
    { field: 'aopStatus', headerName: 'Status', editable: false },
    { field: 'aopRemarks', headerName: 'Remarks', minWidth: 150, editable: true },


  ]
  

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={productNormData}
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
