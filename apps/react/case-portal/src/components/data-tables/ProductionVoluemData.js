import { DataService } from 'services/DataService'
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
        }
      }

      getAllProducts()
      fetchData()
    }, []
  )

  const productionColumns = [
    { field: 'id', headerName: 'ID' },
    { field: 'aopCaseId', headerName: 'Case ID', minWidth: 120, editable: false },
    { field: 'aopType', headerName: 'Type', minWidth: 80 },
    { field: 'aopYear', headerName: 'Year', minWidth: 80 },
    { field: 'plantFkId', headerName: 'Plant ID', minWidth: 80 },
    { field: 'normItem', headerName: 'Product', minWidth: 80, editable: false },
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
    { field: 'aopStatus', headerName: 'Status', minWidth: 75, editable: false },
  ];
  
  

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
