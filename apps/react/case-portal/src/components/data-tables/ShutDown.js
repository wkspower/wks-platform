import { DataService } from 'services/DataService'
import {
  Autocomplete,
  TextField,
} from '../../../node_modules/@mui/material/index'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'




const ShutDown = () => {



  const [shutdownData, setShutdownData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const keycloak = useSession()

useEffect(() => {
  const fetchData = async () => {
    try {
      const data = await DataService.getShutDownPlantData(keycloak)
      const formattedData = data.map((item, index) => ({
        ...item,
        // id: item?.maintenanceId, 
        id: index,

      }))

      setShutdownData(formattedData)

    } catch (error) {
      console.error('Error fetching shutdown data:', error)
    }
  }


  


  const getAllProducts = async () => {
    try {
      const data = await DataService.getAllProducts(keycloak)
      console.log('API Response:', data)
      setAllProducts(data)
    } catch (error) {
      console.error('Error fetching product:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const saveShutdownData = async () => {
    try {
      // var plantId = 'B989E3EE-00C8-493C-9CA4-709D340FA5A1';
      var plantId = '7b7e0d7c-2666-43bb-847c-d78e144673de'
      const shutdownDetails = {
        product: "Mode C/O",
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
  saveShutdownData()
  getAllProducts()
}, [])


const colDefs = [
  {
    field: 'discription',
    headerName: 'Shutdown Desc',
    minWidth: 300,
    editable: true,
    renderHeader: () => (
      <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
        Shutdown Desc
      </div>
    ),
    flex: 3,
  },
  {
    field: 'product',
    headerName: 'Product',
    editable: true,
    minWidth: 200,
    renderEditCell: (params) => {
      const { id } = params
      const isEditable = id > 0 

      return (
        <Autocomplete
          options={allProducts}
          value={params.value || ''}
          disableClearable
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
          disabled={!isEditable}
          fullWidth
        />
      )
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
    editable:true,
    minWidth: 200,
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
      const durationInHours = params.value ? (params.value / 60).toFixed(2) : "0.00";
      return `${durationInHours}`;
    },
  },
  
]


  

  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={shutdownData}
        title='Shutdown Plan Data'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
      />
    </div>
  )
}

export default ShutDown
