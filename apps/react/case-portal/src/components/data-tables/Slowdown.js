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
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
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
        const data = await DataService.getAllProducts(keycloak)
        // console.log('API Response:', data);

        // Extract only displayName and id
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

    const saveShutdownData = async () => {
      try {
        // var plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D';
        var plantId = ''

        const storedPlant = localStorage.getItem('selectedPlant')
        if (storedPlant) {
          const parsedPlant = JSON.parse(storedPlant)
          plantId = parsedPlant.id
        }

        const shutdownDetails = {
          product: 'Oxygen',
          discription: '1 Shutdown maintenance',
          durationInMins: 120,
          maintEndDateTime: '2025-02-20T18:00:00Z',
          maintStartDateTime: '2025-02-20T16:00:00Z',
        }

        const response = await DataService.saveShutdownData(
          plantId,
          shutdownDetails,
          keycloak,
        )
        console.log('Shutdown data saved successfully:', response)
        return response
      } catch (error) {
        console.error('Error saving shutdown data:', error)
      }
    }

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
      field: 'maintenanceId',
      headerName: 'maintenanceId',
      editable: false,
      hide: true,
    },

    {
      field: 'product',
      headerName: 'Product',
      editable: true,
      minWidth: 225,
      valueGetter: (params, params2) => {
        return params || ''
      },
      valueFormatter: (params) => {
        console.log('params valueFormatter ', params)
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params, params2) => {
        const { id, value } = params
        return (
          <select
            value={value || allProducts[0]?.id}
            onChange={(event) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
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
            {allProducts.map((product) => (
              <option key={product.id} value={product.id}>
                {product.displayName}
              </option>
            ))}
          </select>
        )
      },
    },

    {
      field: 'maintStartDateTime',
      headerName: 'SD- From',
      type: 'dateTime',
      minWidth: 200,
      editable: true,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'MMM D, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'maintEndDateTime',
      headerName: 'SD- To',
      type: 'dateTime',
      minWidth: 200,
      editable: true,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'MMM D, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'durationInMins',
      headerName: 'Duration (hrs)',
      editable: true,
      // type: "number",
      minWidth: 100,
      maxWidth: 150,
      renderCell: (params) => {
        return `${params.value}`
      },
    },

    {
      field: 'rate',
      headerName: 'Rate',
      editable: true,
      type: 'number',
      minWidth: 100,
      maxWidth: 150,
    },

    {
      field: 'remarks',
      headerName: 'Remarks',
      editable: true,
      minWidth: 200,
      maxWidth: 400,
    },
  ]
  // const saveSlowDownData = async (newRow) => {
  //   try {
  //     var plantId = ''
  //     const storedPlant = localStorage.getItem('selectedPlant')
  //     if (storedPlant) {
  //       const parsedPlant = JSON.parse(storedPlant)
  //       plantId = parsedPlant.id
  //     }
  //     const slowDownDetails = {
  //       productId: newRow.product,
  //       discription: newRow.discription,
  //       durationInMins: newRow.durationInMins,
  //       maintEndDateTime: newRow.maintEndDateTime,
  //       maintStartDateTime: newRow.maintStartDateTime,
  //       remark: newRow.remarks,
  //       rate: newRow.rate,
  //     }
  //     const response = await DataService.saveSlowdownData(
  //       plantId,
  //       slowDownDetails,
  //       keycloak,
  //     )
  //     //console.log('Slowdown data saved successfully:', response)
  //     setSnackbarOpen(true)
  //     // setSnackbarMessage("Slowdown data saved successfully !");
  //     setSnackbarData({
  //       message: 'Slowdown data saved successfully!',
  //       severity: 'success',
  //     })
  //     // setSnackbarOpen(true);
  //     // setSnackbarData({ message: "Slowdown data saved successfully!", severity: "success" });
  //     return response
  //   } catch (error) {
  //     console.error('Error saving Slowdown data:', error)
  //   }
  // }
  // const updateSlowdownData = async (newRow) => {
  //   try {
  //     var maintenanceId = newRow?.maintenanceId

  //     const slowDownDetails = {
  //       productId: newRow.product,
  //       discription: newRow.discription,
  //       durationInMins: newRow.durationInMins,
  //       maintEndDateTime: newRow.maintEndDateTime,
  //       maintStartDateTime: newRow.maintStartDateTime,
  //       remark: newRow.remarks,
  //       rate: newRow.rate,
  //     }

  //     const response = await DataService.updateSlowdownData(
  //       maintenanceId,
  //       slowDownDetails,
  //       keycloak,
  //     )
  //     //console.log('Slowdown data Updated successfully:', response)
  //     setSnackbarOpen(true)
  //     // setSnackbarMessage("Slowdown data Updated successfully !");
  //     setSnackbarData({
  //       message: 'Slowdown data Updated successfully!',
  //       severity: 'success',
  //     })
  //     // setSnackbarOpen(true);
  //     // setSnackbarData({ message: "Slowdown data Updated successfully!", severity: "success" });
  //     return response
  //   } catch (error) {
  //     console.error('Error saving Slowdown data:', error)
  //   }
  // }
  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={slowDownData}
        title='Slowdown Plan'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        // saveSlowDownData={saveSlowDownData}
        // updateSlowdownData={updateSlowdownData}
        // snackbarOpen={snackbarOpen}
        // snackbarData={snackbarData}
        permissions={{
          showAction: true,
          addButton: true,
          deleteButton: true,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
        }}
      />
    </div>
  )
}

export default SlowDown
