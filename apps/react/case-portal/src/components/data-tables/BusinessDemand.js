import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
import getEnhancedColDefs from './CommonHeader/index'
const headerMap = generateHeaderNames()

const BusinessDemand = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const [bdData, setBDData] = useState([])
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
  const vertName = verticalChange?.verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
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

  const fetchData = async () => {
    try {
      const data = await DataService.getBDData(keycloak)
      const groupedRows = []
      const groups = new Map()
      let groupId = 0

      data.forEach((item) => {
        const groupKey = item.normParameterTypeDisplayName || 'Group'

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
        const data = await DataService.getAllProducts(
          keycloak,
          lowerVertName === 'meg' ? 'Production' : 'Grade',
        )
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

  const handleRemarkCellClick = (row, newRow) => {
    // console.log(row, newRow)
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const colDefs = getEnhancedColDefs({
    allProducts,
    headerMap,
    handleRemarkCellClick,
  })
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
        // Validate that both normParameterId and remark are not empty
        const invalidRows = data.filter(
          (row) => !row.normParameterId.trim() || !row.remark.trim(),
        )

        if (invalidRows.length > 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please fill required fields: Product and Remark.',
            severity: 'error',
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

  // const handleRowEditStop = (params, event) => {
  //   setRowModesModel({
  //     ...rowModesModel,
  //     [params.id]: { mode: GridRowModes.View, ignoreModifications: false },
  //   })
  // }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  return (
    <div>
      {/* <div>
        {`Plant: ${verticalChange?.verticalChange?.selectedPlant}, Site: ${verticalChange?.verticalChange?.selectedSite}, Vertical: ${verticalChange?.verticalChange?.selectedVertical}`}
      </div> */}
      <ASDataGrid
        setRows={setRows}
        columns={
          colDefs
          // lowerVertName === 'meg'
          //   ? vertical_meg_coldefs_bd
          //   : vertical_pe_coldefs_bd
        }
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
        apiRef={apiRef}
        deleteId={deleteId}
        setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        open1={open1}
        fetchData={fetchData}
        onProcessRowUpdateError={onProcessRowUpdateError}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        unsavedChangesRef={unsavedChangesRef}
        handleRemarkCellClick={handleRemarkCellClick}
        permissions={{
          showAction: true,
          addButton: true,
          deleteButton: true,
          editButton: true,
          showUnit: false,
          saveWithRemark: true,
          saveBtn: true,
          units: ['TPH', 'TPD'],
        }}
      />
    </div>
  )
}

export default BusinessDemand
