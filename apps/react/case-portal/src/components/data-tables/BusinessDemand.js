import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import ASDataGrid from './ASDataGrid'
import getEnhancedColDefs from './CommonHeader/index'
const headerMap = generateHeaderNames()
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'

const BusinessDemand = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  // const [bdData, setBDData] = useState([])
  //test
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
  const [loading, setLoading] = useState(false)
  // States for the Remark Dialog
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const fetchData = async () => {
    setLoading(true)
    try {
      const data = await DataService.getBDData(keycloak)
      const groupedRows = []
      const groups = new Map()
      let groupId = 0

      // console.log('lowerVertName', lowerVertName)

      data.forEach((item) => {
        const formattedItem = {
          ...item,
          idFromApi: item.id,
          id: groupId++,
        }

        if (lowerVertName !== 'pe') {
          const groupKey = item.normParameterTypeDisplayName

          if (!groups.has(groupKey)) {
            groups.set(groupKey, [])
            groupedRows.push({
              id: groupId++,
              Particulars: groupKey,
              isGroupHeader: true,
            })
          }

          groups.get(groupKey).push(formattedItem)
        }

        groupedRows.push(formattedItem)
      })

      setRows(groupedRows)
      setLoading(false) // Hide loading
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setLoading(false) // Hide loading
    }
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(
          keycloak,
          // lowerVertName === 'meg' ? 'Production' : 'Grade',
          null,
        )
        const allowedIds = [
          '4D8E17F6-D6CB-407E-8C9C-4BEDBC422C57',
          '00DC05B1-9607-470E-A159-62497E0123E2',
          'A061E050-0281-421F-81C1-B136CE2ED3F3',
          '92E0AF06-9535-4B93-8998-E56A71354393',
        ]

        const productList = data
          .filter((product) => allowedIds.includes(product.id))
          .map((product) => ({
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
  }, [sitePlantChange, keycloak, verticalChange, lowerVertName])

  const handleRemarkCellClick = (row) => {
    // console.log(row, newRow)
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const isCellEditable = (params) => {
    return !params.row.Particulars
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

      const requiredFields = ['normParameterId', 'remark']

      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }
      saveBusinessDemandData(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [apiRef])

  const saveBusinessDemandData = async (newRows) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      let siteId = ''
      const storedSite = localStorage.getItem('selectedSite')
      if (storedSite) {
        const parsedSite = JSON.parse(storedSite)
        siteId = parsedSite.id
      }

      let verticalId = localStorage.getItem('verticalId')
      // const storedVertical = localStorage.getItem('selectedSite')
      // if (storedVertical) {
      //   const parsedVertical = JSON.parse(storedVertical)
      //   verticalId = parsedVertical.id
      // }

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
        siteFKId: siteId,
        verticalFKId: verticalId,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
      }))

      // if (businessData.length > 0) {
      const response = await DataService.saveBusinessDemandData(
        plantId,
        businessData,
        keycloak,
      )

      // console.log(response)

      // if (response.status == 200) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Business Demand data Saved Successfully!',
        severity: 'success',
      })
      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      fetchData()
      return response
    } catch (error) {
      console.error('Error saving Business Demand data!', error)
    } finally {
      // fetchData()
    }
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const deleteRowData = async (paramsForDelete) => {
    try {
      const { idFromApi, id } = paramsForDelete.row
      const deleteId = id

      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await DataService.deleteBusinessDemandData(idFromApi, keycloak)
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()
      }
    } catch (error) {
      console.error('Error deleting Record!', error)
    }
  }

  return (
    <div>
      {/* <div>
        {`Plant: ${verticalChange?.verticalChange?.selectedPlant}, Site: ${verticalChange?.verticalChange?.selectedSite}, Vertical: ${verticalChange?.verticalChange?.selectedVertical}`}
      </div> */}

      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <ASDataGrid
        setRows={setRows}
        columns={
          colDefs
          // lowerVertName === 'meg'
          //   ? vertical_meg_coldefs_bd
          //   : vertical_pe_coldefs_bd
        }
        rows={rows}
        isCellEditable={isCellEditable}
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
        deleteRowData={deleteRowData}
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
