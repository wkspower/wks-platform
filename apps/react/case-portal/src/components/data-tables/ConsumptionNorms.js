import { DataService } from 'services/DataService'
import DataGridTable from './ASDataGrid'
import React, { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useGridApiRef } from '@mui/x-data-grid'
import getEnhancedColDefs from './CommonHeader/consumptionHeader'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'

const NormalOpNormsScreen = () => {
  const keycloak = useSession()
  const [allProducts, setAllProducts] = useState([])
  const headerMap = generateHeaderNames()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const [open1, setOpen1] = useState(false)
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [loading, setLoading] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
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

  const saveEditedData = async (newRows) => {
    setLoading(true)
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      let siteID =
        JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''

      let verticalId = localStorage.getItem('verticalId')

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
        aopRemarks: row.aopRemarks || null,
        aopYear: localStorage.getItem('year'),
        plantFkId: plantId,
        siteFkId: siteID,
        verticalFkId: verticalId,
        materialFkId: row.NormParametersId,
        id: row.idFromApi || null,
        aopCaseId: '2025-26-NormsAOP',
        aopStatus: 'Saved',
      }))
      const response = await DataService.saveAOPConsumptionNorm(
        plantId,
        businessData,
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Consumption AOP Saved Successfully!',
        severity: 'success',
      })
      setLoading(false)
      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      fetchData()
      return response
    } catch (error) {
      console.error('Error saving Consumption AOP!', error)
    } finally {
      setLoading(false)
    }
  }

  const saveChanges = React.useCallback(async () => {
    setLoading(true)

    if (lowerVertName == 'meg') {
      try {
        var data = Object.values(unsavedChangesRef.current.unsavedRows)
        if (data.length == 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          setLoading(false)

          return
        }
        const requiredFields = ['aopRemarks']
        const validationMessage = validateFields(data, requiredFields)
        if (validationMessage) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: validationMessage,
            severity: 'error',
          })
          setLoading(false)
          return
        }

        saveEditedData(data)
      } catch (error) {
        console.log('Error saving changes:', error)
        setLoading(false)
      }
    }

    if (lowerVertName == 'pe') {
      try {
        var editedData = Object.values(unsavedChangesRef.current.unsavedRows)
        var allRows = Array.from(apiRef.current.getRowModels().values())
        allRows = allRows.filter((row) => !row.isGroupHeader)
        const updatedRows = allRows.map(
          (row) => unsavedChangesRef.current.unsavedRows[row.id] || row,
        )

        if (updatedRows.length === 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          setLoading(false)

          return
        }

        const requiredFields = ['aopRemarks']

        const validationMessage = validateFields(editedData, requiredFields)
        if (validationMessage) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: validationMessage,
            severity: 'error',
          })
          setLoading(false)
          return
        }

        if (calculatebtnClicked == false) {
          if (editedData.length === 0) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: 'No Records to Save!',
              severity: 'info',
            })
            setLoading(false)

            setCalculatebtnClicked(false)
            return
          }

          saveEditedData(editedData)
        } else {
          saveEditedData(updatedRows)
        }
      } catch (error) {
        setLoading(false)
        console.log('Error saving changes:', error)
        setCalculatebtnClicked(false)
      }
    }
  }, [apiRef, selectedUnit, calculatebtnClicked])

  const fetchData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getConsumptionNormsData(keycloak)

      // const customOrder = [
      //   'Raw Material',
      //   'By Products',
      //   'Cat Chem',
      //   'Utility Consumption',
      //   'Configuration',
      // ]

      // const data = data1.sort(
      //   (a, b) =>
      //     customOrder.indexOf(a.normParameterTypeDisplayName) -
      //     customOrder.indexOf(b.normParameterTypeDisplayName),
      // )

      const groupedRows = []
      const groups = new Map()
      let groupId = 0

      data.forEach((item) => {
        const groupKey = item.normParameterTypeDisplayName

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
          NormParametersId: item.materialFkId.toLowerCase(),
          id: groupId++,
        }

        groups.get(groupKey).push(formattedItem)
        groupedRows.push(formattedItem)
      })

      // setBDData(groupedRows)
      setRows(groupedRows)
      setLoading(false)
      setCalculatebtnClicked(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
      setCalculatebtnClicked(false)
    }
  }

  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProductsAll(keycloak, 'All')
        const productList = data.map((product) => ({
          id: product.id.toLowerCase(),
          displayName: product.displayName,
          name: product.name,
        }))
        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching products:', error)
      }
    }

    getAllProducts()
    fetchData()
  }, [sitePlantChange, keycloak, selectedUnit, lowerVertName])

  const productionColumns = getEnhancedColDefs({
    allProducts,
    headerMap,
    handleRemarkCellClick,
  })

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  const handleCalculate = () => {
    if (lowerVertName == 'meg') {
      handleCalculateMeg()
    } else {
      handleCalculatePe()
    }
  }

  const handleCalculateMeg = async () => {
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const data = await DataService.handleCalculateonsumptionNorms(
        plantId,
        year,
        keycloak,
      )

      if (data || data == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
      }

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      console.error('Error!', error)
    }
  }

  const handleCalculatePe = async () => {
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      var data = await DataService.handleCalculateConsumptionNorm1(
        plantId,
        year,
        keycloak,
      )

      if (data) {
        const groupedRows = []
        const groups = new Map()
        let groupId = 0

        data.forEach((item) => {
          const groupKey = item.normParameterTypeDisplayName

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
            NormParametersId: item.materialFkId.toLowerCase(),
            id: groupId++,
            aopRemarks: item?.aopRemarks?.trim() || null,
          }

          groups.get(groupKey).push(formattedItem)
          groupedRows.push(formattedItem)
        })

        setRows(groupedRows)
        setLoading(false)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
        setLoading(false)
      }

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      console.error('Error!', error)
      setLoading(false)
    }
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <DataGridTable
        columns={productionColumns}
        rows={rows}
        setRows={setRows}
        getRowId={(row) => row.id}
        title='Consumption AOP'
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
        handleCalculate={handleCalculate}
        handleRemarkCellClick={handleRemarkCellClick}
        fetchData={fetchData}
        onProcessRowUpdateError={onProcessRowUpdateError}
        handleUnitChange={handleUnitChange}
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
          units: ['TPH', 'TPD'],
          saveWithRemark: true,
          saveBtn: true,
          showCalculate: true,
          showRefresh: false,
        }}
      />
    </div>
  )
}

export default NormalOpNormsScreen
