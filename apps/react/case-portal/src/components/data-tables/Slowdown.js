import { DataService } from 'services/DataService'
import {
  Autocomplete,
  TextField,
} from '../../../node_modules/@mui/material/index'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'




const SlowDown = () => {
  const [slowDownData, setSlowDownData] = useState([])
  const [allProducts, setAllProducts] = useState([])

  const keycloak = useSession()

useEffect(() => {
  const fetchData = async () => {
    try {
      const data = await DataService.getSlowDownPlantData(keycloak)
      const formattedData = data.map((item, index) => ({
        ...item,
        // id: item?.maintenanceId, 
        id: index,

      }))
      setSlowDownData(formattedData)
    } catch (error) {
      console.error('Error fetching SlowDown data:', error)
    }
  }


  const getAllProducts = async () => {
    try {
      const data = await DataService.getAllProducts(keycloak);
      console.log('API Response:', data);
         
      // Extract only displayName and id
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
  };
  
  const saveShutdownData = async () => {
    try {
      var plantId = 'B989E3EE-00C8-493C-9CA4-709D340FA5A1';
      // var plantId = '7b7e0d7c-2666-43bb-847c-d78e144673de'
      // var plantId = '7b7e0d7c-2666-43bb-847c-d78e144673de'
      // discription
      // maintStartDateTime
      // maintEndDateTime
      // durationInMins
      // product

      const shutdownDetails = {
        product: "Oxygen",
        discription: "1 Shutdown maintenance",
        durationInMins: 120,
        maintEndDateTime: "2025-02-20T18:00:00Z",
        maintStartDateTime: "2025-02-20T16:00:00Z",
      };
      
      const response = await DataService.saveShutdownData(plantId, shutdownDetails, keycloak);
      console.log("Shutdown data saved successfully:", response);
      return response;
    } catch (error) {
      console.error("Error saving shutdown data:", error);
    }
  };


  
  fetchData()
  // saveShutdownData()
  getAllProducts()
}, [])


const colDefs = [
  {
    field: 'discription',
    headerName: 'Slowdown Desc',
    minWidth: 200,
    editable: true,
    renderHeader: () => (
      <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
        Slowdown Desc
      </div>
    ),
    flex: 3,
  },
  {
    field: 'product',
    headerName: 'Product',
    editable: true,
    minWidth: 225,
    valueGetter: (params) => {
      
      
      const product = allProducts.find((p) => p.id === params);
      return product ? product.displayName : params; 
    },
    renderEditCell: (params) => {
      const { id } = params;
      const isEditable = id > 0;
  
      return (
        <Autocomplete
          options={allProducts}
          getOptionLabel={(option) => option.displayName} 
          value={allProducts.find((product) => product.id === params.value) || null}
          disableClearable
          onChange={(event, newValue) => {
            params.api.setEditCellValue({
              id: params.id,
              field: 'product',
              value: newValue ? newValue.id : '', 
            });
          }}
          onInputChange={(event, newInputValue) => {
            if (event && event.type === 'keydown' && event.key === 'Enter') {
              const selectedProduct = allProducts.find(
                (product) => product.displayName.toLowerCase() === newInputValue.toLowerCase()
              );
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: selectedProduct ? selectedProduct.id : '',
              });
            }
          }}
          renderInput={(params) => (
            <TextField {...params} variant="outlined" size="small" />
          )}
          disabled={!isEditable}
          fullWidth
        />
      );
    },
  },  


  {
    field: "maintStartDateTime",
    headerName: "SD- From",
    type: "dateTime",
    minWidth: 200,
    editable: true,
    valueGetter: (params) => {
      const value = params; 
      const parsedDate = value
        ? dayjs(value, "MMM D, YYYY, h:mm:ss A").toDate()
        : null;
      return parsedDate;
    },
  },
  
  {
    field: "maintEndDateTime",
    headerName: "SD- To",
    type: "dateTime",
    minWidth: 200,
    editable:true,
    valueGetter: (params) => {
      const value = params; 
      const parsedDate = value
        ? dayjs(value, "MMM D, YYYY, h:mm:ss A").toDate()
        : null;
      return parsedDate;
    },
  },


  {
    field: "durationInMins",
    headerName: "Duration (hrs)",
    editable: true,
    type: "number",
    minWidth: 100,
    maxWidth: 150,
    renderCell: (params) => {
      return `${params.value}`;
    },
  },
  
  
  {
    field: "rate",
    headerName: "Rate",
    editable: true,
    type: "number",
    minWidth: 100,
    maxWidth: 150,
  },




  {
    field: "remarks",
    headerName: "Remarks",
    editable: true,
    minWidth: 200,
    maxWidth: 400,
  },
  
]




  

  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={slowDownData}
        title='Slowdown Records'
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

export default SlowDown
