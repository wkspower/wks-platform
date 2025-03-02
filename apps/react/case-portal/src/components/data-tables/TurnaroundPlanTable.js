import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import dayjs from 'dayjs'
import { useState, useEffect } from 'react'
import { useSession } from 'SessionStoreContext'

const TurnaroundPlanTable = () => {
  const [TaData, setTaData] = useState([])
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
        const data = await DataService.getTAPlantData(keycloak)
        const formattedData = data.map((item, index) => ({
          // ...item,
          // id: index,

          ...item,
          idFromApi: item.id,
          id: index,
        }))

        setTaData(formattedData)
      } catch (error) {
        console.error('Error fetching Turnaround data:', error)
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
    fetchData()
    getAllProducts()
  }, [])

  const colDefs = [
    {
      field: 'discription',
      headerName: 'Activities',
      minWidth: 300,
      editable: true,
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
      headerName: 'TA- From',
      type: 'dateTime',
      editable: true,
      minWidth: 200,
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
      headerName: 'TA- To',
      type: 'dateTime',
      editable: true,
      minWidth: 200,
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
      // type: 'number',
      minWidth: 100,
      renderCell: (params) => {
        // const durationInHours = params.value ? (params.value / 60).toFixed(2) : "0.00";
        return `${params.value}`
      },
    },
    {
      field: 'period',
      headerName: 'Periods (in months)',
      editable: true,
      minWidth: 120,
    },

    {
      field: 'remark',
      headerName: 'Remarks',
      editable: true,
      minWidth: 200,
    },
  ]
  // const saveTurnAroundData = async (newRow) => {
  //   try {
  //     // var plantId = 'A4212E62-2BAC-4A38-9DAB-2C9066A9DA7D'
  //     var plantId = ''

  //     const storedPlant = localStorage.getItem('selectedPlant')
  //     if (storedPlant) {
  //       const parsedPlant = JSON.parse(storedPlant)
  //       plantId = parsedPlant.id
  //     }

  //     const turnAroundDetails = {
  //       productId: newRow.product,
  //       discription: newRow.discription,
  //       durationInMins: newRow.durationInMins,
  //       maintEndDateTime: newRow.maintEndDateTime,
  //       maintStartDateTime: newRow.maintStartDateTime,
  //       remark: newRow.remark,
  //       // rate: newRow.rate,
  //     }
  //     const response = await DataService.saveTurnAroundData(
  //       plantId,
  //       turnAroundDetails,
  //       keycloak,
  //     )
  //     //console.log('Turnaround Plan data saved successfully:', response)
  //     setSnackbarOpen(true)
  //     // setSnackbarMessage("Turnaround Plan data saved successfully !");
  //     setSnackbarData({
  //       message: 'Turnaround Plan data saved successfully!',
  //       severity: 'success',
  //     })
  //     // setSnackbarOpen(true);
  //     // setSnackbarData({ message: "Turnaround Plan data saved successfully!", severity: "success" });
  //     return response
  //   } catch (error) {
  //     console.error('Error saving Turnaround Plan data:', error)
  //   }
  // }
  // const updateTurnAroundData = async (newRow) => {
  //   try {
  //     var maintenanceId = newRow?.maintenanceId

  //     const turnAroundDetails = {
  //       productId: newRow.product,
  //       discription: newRow.discription,
  //       durationInMins: newRow.durationInMins,
  //       maintEndDateTime: newRow.maintEndDateTime,
  //       maintStartDateTime: newRow.maintStartDateTime,
  //       remark: newRow.remark,
  //     }

  //     const response = await DataService.updateTurnAroundData(
  //       maintenanceId,
  //       turnAroundDetails,
  //       keycloak,
  //     )
  //     //console.log('TurnAround data Updated successfully:', response)
  //     setSnackbarOpen(true)
  //     // setSnackbarMessage("TurnAround data Updated successfully !");
  //     setSnackbarData({
  //       message: 'TurnAround data Updated successfully!',
  //       severity: 'success',
  //     })
  //     // setSnackbarOpen(true);
  //     // setSnackbarData({ message: "TurnAround data Updated successfully!", severity: "success" });
  //     return response
  //   } catch (error) {
  //     console.error('Error Updating TurnAround data:', error)
  //   }
  // }
  return (
    <div>
      <ASDataGrid
        columns={colDefs}
        rows={TaData}
        title='Turnaround Plan'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        // saveTurnAroundData={saveTurnAroundData}
        // updateTurnAroundData={updateTurnAroundData}
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

export default TurnaroundPlanTable
