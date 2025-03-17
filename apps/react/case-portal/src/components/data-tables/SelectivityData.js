import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
// Import the catalyst options from the JSON file
// import catalystOptionsData from '../../assets/Catalyst.json'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import NumericInputOnly from 'utils/NumericInputOnly'

const headerMap = generateHeaderNames()

const SelectivityData = () => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange } = dataGridStore
  const keycloak = useSession()
  // const [csData, setCsData] = useState([])
  // const [allProducts, setAllProducts] = useState([])
  // const [allCatalyst, setAllCatalyst] = useState([])
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)

  const [rows, setRows] = useState()

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    return newRow
  }, [])

  const saveChanges = React.useCallback(async () => {
    setTimeout(() => {
      // console.log(
      //   'Edited Data: ',
      //   Object.values(unsavedChangesRef.current.unsavedRows),
      // )
      try {
        // if (title === 'Business Demand') {
        var data = Object.values(unsavedChangesRef.current.unsavedRows)
        // Validation: Check if there are any rows to save
        if (data.length === 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          return
        }

        // Validate that both normParameterId and remark are not empty
        const invalidRows = data.filter((row) => !row.remark.trim())

        if (invalidRows.length > 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please fill required fields: Remark.',
            severity: 'error',
          })
          return
        }
        saveCatalystData(data)
        // }

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {
        // setIsSaving(false);
      }
    }, 1000)
  }, [apiRef])
  const saveCatalystData = async (newRow) => {
    // console.log('new Row ', newRow)

    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const turnAroundDetails = {
        april: newRow.apr24,
        may: newRow.may24,
        june: newRow.jun24,
        july: newRow.jul24,
        aug: newRow.aug24,
        sep: newRow.sep24,
        oct: newRow.oct24,
        nov: newRow.nov24,
        dec: newRow.dec24,
        jan: newRow.jan25,
        feb: newRow.feb25,
        march: newRow.mar25,
        TPH: '100',
        attributeName: 'Silver Ox',
        normParameterFKId: '',
        catalystAttributeFKId: 'C6352800-C64A-4944-B490-5A60D1BCE285',
        catalystId: '',
        remarks: '123',
        avgTPH: '123',
        year: 2024,
      }

      const response = await DataService.saveCatalystData(
        plantId,
        turnAroundDetails,
        keycloak,
      )
      //console.log('Catalyst data Saved Successfully:', response)
      setSnackbarOpen(true)
      // setSnackbarMessage("Catalyst data Saved Successfully !");
      setSnackbarData({
        message: 'Catalyst data Saved Successfully!',
        severity: 'success',
      })
      // setSnackbarOpen(true);
      // setSnackbarData({ message: "Catalyst data Saved Successfully!", severity: "success" });
      return response
    } catch (error) {
      console.error('Error saving Catalyst data:', error)
    } finally {
      fetchData()
    }
  }
  const handleDeleteClick = async (id, params) => {
    try {
      const maintenanceId =
        id?.maintenanceId ||
        params?.row?.idFromApi ||
        params?.row?.maintenanceId ||
        params?.NormParameterMonthlyTransactionId

      // console.log(maintenanceId, params, id)

      // Ensure UI state updates before the deletion process
      setOpen1(true)
      setDeleteId(id)

      // Perform the delete operation
      return await DataService.deleteBusinessDemandData(maintenanceId, keycloak)
    } catch (error) {
      console.error(`Error deleting Configuration data:`, error)
    } finally {
      fetchData()
    }
  }
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
      // setCsData(formattedData)
      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching Turnaround data:', error)
    }
  }
  useEffect(() => {
    // const getAllProducts = async () => {
    //   try {
    //     // const data = await DataService.getAllProducts(keycloak, 'Consumption')
    //     // const productList = data.map((product) => ({
    //     //   id: product.id,
    //     //   displayName: product.displayName,
    //     // }))
    //     // setAllProducts(productList)
    //   } catch (error) {
    //     console.error('Error fetching product:', error)
    //   } finally {
    //     // handleMenuClose();
    //   }
    // }
    // const getAllCatalyst = async () => {
    //   try {
    //     const data = await DataService.getAllCatalyst(keycloak)

    //     const productList = data.map((product) => {
    //       // console.log('Original ID:', product.id)
    //       return {
    //         id: product.id, // Should not change the case
    //         displayName: product.displayName,
    //       }
    //     })
    //     // console.log('Mapped Product List:', productList)

    //     // setAllCatalyst(productList)
    //   } catch (error) {
    //     console.error('Error fetching product:', error)
    //   } finally {
    //     // handleMenuClose();
    //   }
    // }
    // getAllProducts()
    // getAllCatalyst()
    fetchData()
  }, [sitePlantChange, keycloak])
  // Use catalyst options from the JSON file
  // const productOptions = catalystOptionsData.catalystOptions

  const productionColumns = [
    // {
    //   field: 'catalystId',
    //   headerName: 'Catalyst',
    //   editable: true,
    //   minWidth: 225,
    //   valueGetter: (params , params2) => {
    //     console.log('params ',params);
    //     return params || '';
    //   },
    //   valueFormatter: (params) => {
    //     const product = allCatalyst.find((p) => String(p.id).toUpperCase() === String(params));
    //     return product ? product.displayName : '';
    //   },
    //   renderEditCell: (params , params2) => {
    //     const { id, value } = params;
    //     return (
    //       <select
    //         value={value}
    //         onChange={(event) => {
    //           params.api.setEditCellValue({
    //             id: params.id,
    //             field: 'catalystId',
    //             value: event.target.value,
    //           });
    //         }}
    //         style={{
    //           width: '100%',
    //           padding: '5px',
    //           border: 'none',
    //           outline: 'none',
    //           background: 'transparent',
    //         }}
    //       >
    //         {allCatalyst.map((product) => (
    //           <option key={product.id} value={product.id}>
    //             {product.displayName}
    //           </option>
    //         ))}
    //       </select>
    //     );
    //   },
    // },

    {
      field: 'description',
      headerName: 'Description',
      editable: true,
      minWidth: 250,
    },
    {
      field: 'UOM',
      headerName: 'UOM',
      editable: true,
      align: 'left',
      headerAlign: 'left',
      // valueGetter: convertUnits,
    },
    {
      field: 'april',
      headerName: headerMap[4],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      // valueGetter: convertUnits,
    },

    {
      field: 'may',
      headerName: headerMap[5],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'june',
      headerName: headerMap[6],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'july',
      headerName: headerMap[7],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'aug',
      headerName: headerMap[8],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'sep',
      headerName: headerMap[9],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'oct',
      headerName: headerMap[10],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'nov',
      headerName: headerMap[11],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'dec',
      headerName: headerMap[12],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jan',
      headerName: headerMap[1],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'feb',
      headerName: headerMap[2],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'march',
      headerName: headerMap[3],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'remark',
      headerName: 'Remark',
      minWidth: 150,
      editable: true,
      renderCell: (params) => {
        return (
          <div
            style={{
              cursor: 'pointer',
              color: params.value ? 'inherit' : 'gray',
            }}
            onClick={() => handleRemarkCellClick(params.row)}
          >
            {params.value || 'Click to add remark'}
          </div>
        )
      },
    },
  ]

  return (
    <div>
      <ASDataGrid
        columns={productionColumns}
        rows={rows}
        setRows={setRows}
        title='Configuration'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        setDeleteId={setDeleteId}
        fetchData={fetchData}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        deleteId={deleteId}
        open1={open1}
        handleDeleteClick={handleDeleteClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
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

export default SelectivityData
