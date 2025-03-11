import { DataService } from 'services/DataService'
import ASDataGrid from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useGridApiRef } from '@mui/x-data-grid'
const headerMap = generateHeaderNames()

const BusinessDemand = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const [bdData, setBDData] = useState([])
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const fetchData = async () => {
    try {
      const data = await DataService.getBDData(keycloak)

      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: index,
      }))
      setBDData(formattedData)
      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching Turnaround data:', error)
    }
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak)
        const productList = data.map((product) => ({
          id: product.id, // Convert id to lowercase
          // id: product.id.toLowerCase(), // Convert id to lowercase
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
  }, [sitePlantChange, keycloak])

  const colDefs = [
    {
      field: 'normParameterId',
      headerName: 'Product',
      editable: true,
      minWidth: 225,
      valueGetter: (params) => {
        return params || ''
      },
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params) => {
        const { value } = params
        return (
          <select
            value={value || ''}
            onChange={(event) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'normParameterId',
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
            {/* Disabled first option */}
            <option value='' disabled>
              Select
            </option>
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
      field: 'april',
      headerName: headerMap['apr'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'may',
      headerName: headerMap['may'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'june',
      headerName: headerMap['jun'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'july',
      headerName: headerMap['jul'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'aug',
      headerName: headerMap['aug'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'sep',
      headerName: headerMap['sep'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'oct',
      headerName: headerMap['oct'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'nov',
      headerName: headerMap['nov'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'dec',
      headerName: headerMap['dec'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'jan',
      headerName: headerMap['jan'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'feb',
      headerName: headerMap['feb'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'march',
      headerName: headerMap['mar'],
      editable: true,
      type: 'number',
      align: 'left',
      headerAlign: 'left',
    },

    // { field: 'avgTph', headerName: 'AVG TPH', minWidth: 150, editable: true },
    { field: 'remark', headerName: 'Remark', minWidth: 150, editable: true },
    {
      field: 'idFromApi',
      headerName: 'idFromApi',
    },
  ]

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    // Keep track of original values before editing
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
      try {
        var data = Object.values(unsavedChangesRef.current.unsavedRows)
        if (data.length == 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          return
        }
        saveBusinessDemandData(data)
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {}
    }, 1000)
  }, [apiRef])

  const saveBusinessDemandData = async (newRows) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const businessData = newRows.map((row) => ({
        april: row.april || null,
        may: row.may || null,
        june: row.june || null,
        july: row.july || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        jan: row.jan || null,
        feb: row.feb || null,
        march: row.march || null,
        remark: row.remark || null,
        avgTph: row.avgTph || null,
        year: localStorage.getItem('year'),
        plantId: plantId,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
      }))
      if (businessData.length > 0) {
        const response = await DataService.saveBusinessDemandData(
          plantId,
          businessData, // Now sending an array of rows
          keycloak,
        )
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Business Demand data Saved Successfully!',
          severity: 'success',
        })
        // fetchData()
        return response
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Business Demand data not saved!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error saving Business Demand data:', error)
    } finally {
      fetchData()
    }
  }

  const handleRowEditStop = (params, event) => {
    setRowModesModel({
      ...rowModesModel,
      [params.id]: { mode: GridRowModes.View, ignoreModifications: false },
    })
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  return (
    <div>
      <ASDataGrid
        setRows={setRows}
        columns={colDefs}
        rows={rows}
        title='Business Demand'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        onRowEditStop={handleRowEditStop}
        apiRef={apiRef}
        deleteId={deleteId}
        setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        open1={open1}
        // handleDeleteClick={handleDeleteClick}
        fetchData={fetchData}
        onProcessRowUpdateError={onProcessRowUpdateError}
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

export default BusinessDemand
