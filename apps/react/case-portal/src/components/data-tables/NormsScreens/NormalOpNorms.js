import Tooltip from '@mui/material/Tooltip'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import NumericInputOnly from 'utils/NumericInputOnly'
import DataGridTable from '../ASDataGrid'
const headerMap = generateHeaderNames()

const NormalOpNormsScreen = () => {
  const [allProducts, setAllProducts] = useState([])
  const [bdData, setBDData] = useState([])

  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
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
  const keycloak = useSession()

  const fetchData = async () => {
    try {
      const data = await DataService.getNormalOperationNormsData(keycloak)
      const groupedRows = []
      const groups = new Map()
      let groupId = 0

      data.forEach((item) => {
        const groupKey = item.normParameterTypeDisplayName || 'By Products'

        // const groupKey = ''
        // if (item.id < 4)
        //   groupKey = item.normParameterTypeDisplayName || 'By Products'
        // if (item.id < 8 && item.id > 4)
        //   groupKey = item.normParameterTypeDisplayName || 'Cat Chem'
        // if (item.id < 12 && item.id > 8)
        //   groupKey = item.normParameterTypeDisplayName || 'Consumption'
        // if (item.id < 16 && item.id > 12)
        //   groupKey = item.normParameterTypeDisplayName || 'Production'
        // if (item.id > 16)
        //   groupKey = item.normParameterTypeDisplayName || 'Grade'

        if (!groups.has(groupKey)) {
          groups.set(groupKey, [])
          groupedRows.push({
            id: groupId++,
            Particulars: groupKey,
            isGroupHeader: true,
          })
        }
        const formattedItem = {
          ...item,
          idFromApi: item.id,
          id: groupId++,
        }

        groups.get(groupKey).push(formattedItem)
        groupedRows.push(formattedItem)
      })

      setBDData(groupedRows)
      setRows(groupedRows)
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
    }
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak, 'Consumption')
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
      field: 'Particulars',
      headerName: 'Type',
      minWidth: 125,
      groupable: true,
      renderCell: (params) => <strong>{params.value}</strong>,
    },

    { field: 'unit', headerName: 'Unit', width: 100, editable: true },
    {
      field: 'april',
      headerName: headerMap[4],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
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
      field: 'august',
      headerName: headerMap[8],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'september',
      headerName: headerMap[9],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'october',
      headerName: headerMap[10],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'november',
      headerName: headerMap[11],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'december',
      headerName: headerMap[12],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'january',
      headerName: headerMap[1],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },
    {
      field: 'february',
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
      renderCell: (params) => (
        <Tooltip title={params.value || ''} arrow>
          <div
            style={{
              cursor: 'pointer',
              color: params.value ? 'inherit' : 'gray',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              maxWidth: 140,
            }}
            onClick={() => handleRemarkCellClick(params.row)}
          >
            {params.value}
          </div>
        </Tooltip>
      ),
    },
    {
      field: 'idFromApi',
      headerName: 'idFromApi',
    },
  ]

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
        // saveBusinessDemandData(data)
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
          businessData,
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

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  return (
    <div>
      <DataGridTable
        columns={colDefs}
        setRows={setRows}
        rows={rows}
        title='Normal Operations Norms'
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        // deleteId={deleteId}
        open1={open1}
        // setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        // handleDeleteClick={handleDeleteClick}
        onProcessRowUpdateError={onProcessRowUpdateError}
        fetchData={fetchData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: true,
          showCalculate: false,
        }}
      />
    </div>
  )
}

export default NormalOpNormsScreen
