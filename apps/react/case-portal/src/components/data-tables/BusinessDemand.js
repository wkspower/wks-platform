import { DataService } from 'services/DataService'
import { Autocomplete, TextField } from '@mui/material'
import ASDataGrid from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
const headerMap = generateHeaderNames();

const BusinessDemand = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const [bdData, setBDData] = useState([])
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  useEffect(() => {
  

    const fetchData = async () => {
      try {
        const data = await DataService.getBDData(keycloak)
        

        const formattedData = data.map((item, index) => ({
          ...item,
          id: index,
        }))

        setBDData(formattedData)
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

    // Initial data fetch on mount or when selectedPlant changes
    fetchData()
    getAllProducts()
    console.log('sitePlant--->', sitePlantChange)
    console.log(sitePlantChange, 'changed plant or site')
  }, [sitePlantChange, keycloak])

  const colDefs = [
    {
      field: 'NormParametersId',
      headerName: 'Product',
      editable: true,
      minWidth: 225,
      valueGetter: (params , params2) => {         
        return params || ''; 
      },
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params);
        return product ? product.displayName : '';
      },
      renderEditCell: (params , params2) => {
        const { id, value } = params; 
        return (
          <select
            value={value || ""}
            onChange={(event) => {
              // console.log('event',event);
              
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: event.target.value, 
              });
            }}
            style={{
              width: '100%',
              padding: '5px',
              border: 'none',  
              outline: 'none', 
              background: 'transparent', 
            }}
          >
            {allProducts.map((product) => (
              <option key={product.id} value={product.id}>
                {product.displayName}
              </option>
            ))}
          </select>
        );
      },
    }, 


    { field: 'apr24', headerName: headerMap['apr'], editable: true },
    { field: 'may24', headerName: headerMap['may'], editable: true },
    { field: 'jun24', headerName: headerMap['jun'], editable: true },
    { field: 'jul24', headerName: headerMap['jul'], editable: true },
    { field: 'aug24', headerName: headerMap['aug'], editable: true },
    { field: 'sep24', headerName: headerMap['sep'], editable: true },
    { field: 'oct24', headerName: headerMap['oct'], editable: true },
    { field: 'nov24', headerName: headerMap['nov'], editable: true },
    { field: 'dec24', headerName: headerMap['dec'], editable: true },
    { field: 'jan25', headerName: headerMap['jan'], editable: true },
    { field: 'feb25', headerName: headerMap['feb'], editable: true },
    { field: 'mar25', headerName: headerMap['mar'], editable: true },

    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
    {
      field: 'NormParameterMonthlyTransactionId',
      headerName: 'NormParameterMonthlyTransactionId',
      minWidth: 150,
      editable: false,
    },
  ]

  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={bdData}
        title='Business Demand'
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

export default BusinessDemand
