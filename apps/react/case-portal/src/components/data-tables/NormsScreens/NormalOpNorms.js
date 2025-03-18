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
  // const [bdData, setBDData] = useState([])
  const menu = useSelector((state) => state.menu)
  const { sitePlantChange } = menu
  const [open1, setOpen1] = useState(false)
  // const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
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

      // setBDData(groupedRows)
      setRows(groupedRows)
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
    }
  }

  useEffect(() => {
   const storedPlant = localStorage.getItem('selectedPlant')
         const parsedPlant = JSON.parse(storedPlant)
       
       const getAllProducts = async () => {
         try {
           const data = await DataService.getAllProducts(
             plantId= parsedPlant.id,keycloak, null)
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
  }, [sitePlantChange, keycloak])

  const formatValueToThreeDecimals = (params) =>
    params ? parseFloat(params).toFixed(3) : ''

  const colDefs = [
    {
      field: 'Particulars',
      headerName: 'Type',
      minWidth: 140,
      groupable: true,
      renderCell: (params) => <strong>{params.value}</strong>,
    },
    {
      field: 'materialFkId',
      headerName: 'Particular',
      minWidth: 140,
      valueGetter: (params) => params || '',
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params) => {
        const { value, id, api } = params
        return (
          <select
            value={value || ''}
            onChange={(event) => {
              api.setEditCellValue({
                id,
                field: 'materialFkId',
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

    { field: 'unit', headerName: 'Unit', width: 100, editable: true },

    {
      field: 'april',
      headerName: headerMap[4],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'may',
      headerName: headerMap[5],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'june',
      headerName: headerMap[6],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'july',
      headerName: headerMap[7],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },

    {
      field: 'august',
      headerName: headerMap[8],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'september',
      headerName: headerMap[9],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'october',
      headerName: headerMap[10],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'november',
      headerName: headerMap[11],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'december',
      headerName: headerMap[12],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'january',
      headerName: headerMap[1],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'february',
      headerName: headerMap[2],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'march',
      headerName: headerMap[3],
      editable: true,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      valueFormatter: formatValueToThreeDecimals,
    },
    {
      field: 'remarks',
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
    setCurrentRemark(row.remarks || '')
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

        saveNormalOperationNormsData(data)
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
      } catch (error) {
        /* empty */
      }
    }, 1000)
  }, [apiRef])

  const saveNormalOperationNormsData = async (newRows) => {
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
        august: row.august || null,
        september: row.september || null,
        october: row.october || null,
        november: row.november || null,
        december: row.december || null,
        january: row.january || null,
        february: row.february || null,
        march: row.march || null,
        remark: row.remarks,
        remarks: row.remarks,
        financialYear: localStorage.getItem('year'),
        plantId: plantId,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
        materialFkId: row.materialFkId || null,
        mcuVersion: row.mcuVersion || null,
        plantFkId: row.plantFkId || null,
        siteFkId: row.siteFkId || null,
        verticalFkId: row.verticalFkId || null,
        unit: row.unit || null,
        normParameterTypeId: row.normParameterTypeId || null,
      }))
      if (businessData.length > 0) {
        // console.log(title)

        const response = await DataService.saveNormalOperationNormsData(
          plantId,
          businessData,
          keycloak,
        )
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Normal Operations Norms Saved Successfully!`,
          severity: 'success',
        })
        // fetchData()
        return response
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Normal Operations Norms not saved!`,
          severity: 'error',
        })
      }
    } catch (error) {
      console.error(`Error saving Normal Operations Norms`, error)
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
        title='Normal Operations Norms'
        columns={colDefs}
        setRows={setRows}
        rows={rows}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        open1={open1}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        onProcessRowUpdateError={onProcessRowUpdateError}
        fetchData={fetchData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        handleRemarkCellClick={handleRemarkCellClick}
        permissions={{
          showAction: false,
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
