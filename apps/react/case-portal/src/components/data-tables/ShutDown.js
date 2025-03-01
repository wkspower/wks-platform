import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'

const ShutDown = () => {
  const [shutdownData, setShutdownData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  // const [snackbarData, setSnackbarData] = useState({
  //   message: '',
  //   severity: 'info',
  // })
  // const [snackbarOpen, setSnackbarOpen] = useState(false)
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
        console.error('Error fetching Shutdown data:', error)
      }
    }

    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        console.log('API Response:', data)

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

    // const saveShutdownData = async () => {
    //   try {
    //     // var plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D'
    //     // var plantId = '7b7e0d7c-2666-43bb-847c-d78e144673de'
    //     // var plantId = '7b7e0d7c-2666-43bb-847c-d78e144673de'

    //     // discription
    //     // maintStartDateTime
    //     // maintEndDateTime
    //     // durationInMins
    //     // product

    //     var plantId = ''

    //     const storedPlant = localStorage.getItem('selectedPlant')
    //     if (storedPlant) {
    //       const parsedPlant = JSON.parse(storedPlant)
    //       plantId = parsedPlant.id
    //     }

    //     const shutdownDetails = {
    //       product: 'Oxygen',
    //       discription: '1 Shutdown maintenance',
    //       durationInMins: 120,
    //       maintEndDateTime: '2025-02-20T18:00:00Z',
    //       maintStartDateTime: '2025-02-20T16:00:00Z',
    //     }

    //     const response = await DataService.saveShutdownData(
    //       plantId,
    //       shutdownDetails,
    //       keycloak,
    //     )
    //     console.log('Shutdown data saved successfully:', response)
    //     return response
    //   } catch (error) {
    //     console.error('Error saving shutdown data:', error)
    //   }
    // }

    fetchData()
    // saveShutdownData()
    getAllProducts()
  }, [])

  const colDefs = [
    {
      field: 'discription',
      headerName: 'Shutdown Desc',
      minWidth: 325,
      editable: true,
      renderHeader: () => (
        <div style={{ textAlign: 'center', fontWeight: 'normal' }}>
          Shutdown Desc
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
        // console.log('p1', params);
        // console.log('p2', params2);
        return params || ''
      },
      valueFormatter: (params) => {
        console.log('params valueFormatter ', params)
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params, params2) => {
        const { id, value } = params
        // console.log('q1', params);
        // console.log('q2', params2);
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
              border: 'none', // Removes border
              outline: 'none', // Removes focus outline
              background: 'transparent', // Keeps background clean
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
      minWidth: 175,
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
      editable: true,
      minWidth: 175,
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
      valueGetter: (id, params) => {
        console.log(id, params)
        const { maintStartDateTime, maintEndDateTime } = params || {}
        if (!maintStartDateTime || !maintEndDateTime) return ''
        const diff =
          (new Date(maintEndDateTime) - new Date(maintStartDateTime)) /
          (1000 * 60 * 60)
        return diff.toFixed(2)
      },
      editable: true,
      minWidth: 100,
      maxWidth: 150,
    },
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
  ]

  // const saveShutdownData = async (newRow) => {
  //   try {
  //     // var plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D'

  //     const storedPlant = localStorage.getItem('selectedPlant')
  //     if (storedPlant) {
  //       const parsedPlant = JSON.parse(storedPlant)
  //       plantId = parsedPlant.id
  //     }

  //     var plantId = plantId
  //     // plantId = plantId;

  //     const shutdownDetails = {
  //       productId: newRow.product,
  //       discription: newRow.discription,
  //       durationInMins: newRow.durationInMins,
  //       maintEndDateTime: newRow.maintEndDateTime,
  //       maintStartDateTime: newRow.maintStartDateTime,
  //     }

  //     const response = await DataService.saveShutdownData(
  //       plantId,
  //       shutdownDetails,
  //       keycloak,
  //     )
  //     //console.log('Shutdown data saved successfully:', response)
  //     setSnackbarOpen(true)
  //     // setSnackbarMessage("Shutdown data saved successfully !");
  //     setSnackbarData({
  //       message: 'Shutdown data saved successfully!',
  //       severity: 'success',
  //     })
  //     // setSnackbarOpen(true);
  //     // setSnackbarData({ message: "Shutdown data saved successfully!", severity: "success" });
  //     return response
  //   } catch (error) {
  //     console.error('Error saving shutdown data:', error)
  //   }
  // }
  // const updateShutdownData = async (newRow) => {
  //   try {
  //     var maintenanceId = newRow?.maintenanceId

  //     const slowDownDetails = {
  //       productId: newRow.product,
  //       discription: newRow.discription,
  //       durationInMins: newRow.durationInMins,
  //       maintEndDateTime: newRow.maintEndDateTime,
  //       maintStartDateTime: newRow.maintStartDateTime,
  //     }

  //     const response = await DataService.updateShutdownData(
  //       maintenanceId,
  //       slowDownDetails,
  //       keycloak,
  //     )

  //     setSnackbarOpen(true)
  //     // setSnackbarMessage("Shutdown data Updated successfully !");
  //     setSnackbarData({
  //       message: 'Shutdown data Updated successfully!',
  //       severity: 'success',
  //     })
  //     // setSnackbarOpen(true);
  //     // setSnackbarData({ message: "Shutdown data Updated successfully!", severity: "success" });
  //     return response
  //   } catch (error) {
  //     console.error('Error saving Shutdown data:', error)
  //   }
  // }
  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={shutdownData}
        title='Shutdown Plan'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        // saveShutdownData={saveShutdownData}
        // updateShutdownData={updateShutdownData}
        // snackbarOpen={snackbarOpen}
        // snackbarData={snackbarData}
        permissions={{
          showAction: true,
          addButton: true,
          deleteButton: true,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: true,
        }}
      />
    </div>
  )
}

export default ShutDown
