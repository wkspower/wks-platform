import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
// import DataGridTable from '../ASDataGrid'
// import { GridRowModes } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { DataService } from 'services/DataService'
// import NumericInputOnly from 'utils/NumericInputOnly'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { validateFields } from 'utils/validationUtils'
// import TextField from '@mui/material/TextField'
// import { useDispatch } from 'react-redux'
// import { setIsBlocked } from 'store/reducers/dataGridStore'
import getShutdownConsumptionColDef from 'components/data-tables/CommonHeader/getShutdownConsumptionColDef'
import KendoDataTables from './index'
import { NormalOperationNormsApiService } from 'services/NormalOperationNormsApiService'
import { GradientRounded } from '../../../node_modules/@mui/icons-material/index'

const ShutdownNorms = () => {
  const [gradeId, setGradeId] = useState(null)

  const [modifiedCells, setModifiedCells] = React.useState({})

  const [loading, setLoading] = useState(false)
  const menu = useSelector((state) => state.dataGridStore)
  // const [allProducts, setAllProducts] = useState([])
  const [shutdownMonths, setShutdownMonths] = useState([])
  const { sitePlantChange, yearChanged, oldYear, plantID } = menu

  const isOldYear = oldYear?.oldYear

  const [open1, setOpen1] = useState(false)

  const apiRef = useGridApiRef()
  // const dispatch = useDispatch()
  const [rows, setRows] = useState([])

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const [_plantID, set_PlantID] = useState('')

  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [rowModesModel, setRowModesModel] = useState({}) // Track row edit state

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  useEffect(() => {
    if (plantID?.plantId) {
      set_PlantID(plantID?.plantId)
    }
  }, [plantID])

  // const getProductDisplayName = (id) => {
  //   if (!id) return
  //   const product = allProducts.find((p) => p.id === id)
  //   return product ? product.displayName : ''
  // }

  const keycloak = useSession()

  const saveChanges = React.useCallback(async () => {
    if (lowerVertName == 'meg') {
      try {
        var data = Object.values(modifiedCells)

        if (data.length == 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          setLoading(false)
          return
        }

        const requiredFields = ['remarks']
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

        saveShutDownNormsData(data)
      } catch (error) {
        setLoading(false)
      }
    }
    if (lowerVertName == 'pe' || lowerVertName == 'pp') {
      try {
        var editedData = Object.values(modifiedCells)
        if (editedData.length === 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
          return
        }

        const requiredFields = ['remarks']

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
            setCalculatebtnClicked(false)
            return
          }

          saveShutDownNormsData(editedData)
        } else {
          saveShutDownNormsData(editedData)
        }
      } catch (error) {
        console.log('Error saving changes:', error)
        setLoading(false)
        setCalculatebtnClicked(false)
      }
    }
  }, [apiRef, selectedUnit, calculatebtnClicked, modifiedCells])

  const loadGrades = async () => {
    if (['pe', 'pp'].includes(lowerVertName)) {
      try {
        const response =
          await NormalOperationNormsApiService.getGradesForShutdownNorms(
            keycloak,
          )

        if (response?.code === 200) {
          setGrades(response?.data)
          if (Array.isArray(response?.data) && response?.data?.length === 0) {
            setLoading(false)
          }
        }
      } catch (error) {
        setGrades([])
        console.error('Error fetching grades:', error)
      }
    }
  }

  // 1) Load grades list if vertical requires it
  useEffect(() => {
    loadGrades()
  }, [plantID, yearChanged, keycloak])

  // 2) Fetch main data when gradeId or other deps change
  useEffect(() => {
    const loadData = async () => {
      try {
        if (['pe', 'pp'].includes(lowerVertName)) {
          if (!gradeId) return // wait until user selects grade
          await fetchData(gradeId)
        } else {
          await fetchData()
        }

        if (['pe', 'pp'].includes(lowerVertName)) {
          if (!gradeId) return
          let data
          data = await DataService.getShutdownMonths(keycloak, gradeId)
        } else {
          data = await DataService.getShutdownMonths(keycloak)
        }

        setShutdownMonths(data)
      } catch (error) {
        console.error('Error in loadData:', error)
      }
    }

    loadData()
  }, [
    oldYear,
    yearChanged,
    keycloak,
    selectedUnit,
    plantID,
    gradeId,
    lowerVertName,
  ])

  const isCellEditable = (params) => {
    return params.row.isEditable
  }

  const colDefs = getShutdownConsumptionColDef({ headerMap, shutdownMonths })

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const processRowUpdate = React.useCallback((newRow, oldRow) => {
    const rowId = newRow.id
    const updatedFields = []
    for (const key in newRow) {
      if (
        Object.prototype.hasOwnProperty.call(newRow, key) &&
        newRow[key] !== oldRow[key]
      ) {
        updatedFields.push(key)
      }
    }

    unsavedChangesRef.current.unsavedRows[rowId || 0] = newRow

    if (!unsavedChangesRef.current.rowsBeforeChange[rowId]) {
      unsavedChangesRef.current.rowsBeforeChange[rowId] = oldRow
    }

    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === newRow.id ? { ...newRow, isNew: false } : row,
      ),
    )

    if (updatedFields.length > 0) {
      setModifiedCells((prevModifiedCells) => ({
        ...prevModifiedCells,
        [rowId]: [...(prevModifiedCells[rowId] || []), ...updatedFields],
      }))
    }

    return newRow
  }, [])

  const saveShutDownNormsData = async (newRows) => {
    setLoading(true)
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      const isTPH = selectedUnit == 'TPD'
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const businessData = newRows.map((row) => ({
        april: isTPH && row.april ? row.april * 24 : row.april || null,
        may: isTPH && row.may ? row.may * 24 : row.may || null,
        june: isTPH && row.june ? row.june * 24 : row.june || null,
        july: isTPH && row.july ? row.july * 24 : row.july || null,
        august: isTPH && row.august ? row.august * 24 : row.august || null,
        september:
          isTPH && row.september ? row.september * 24 : row.september || null,
        october: isTPH && row.october ? row.october * 24 : row.october || null,
        november:
          isTPH && row.november ? row.november * 24 : row.november || null,
        december:
          isTPH && row.december ? row.december * 24 : row.december || null,
        january: isTPH && row.january ? row.january * 24 : row.january || null,
        february:
          isTPH && row.february ? row.february * 24 : row.february || null,
        march: isTPH && row.march ? row.march * 24 : row.march || null,
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
        gradeFkId: gradeId || null,
      }))
      if (businessData.length > 0) {
        // console.log(title)

        const response = await DataService.saveShutDownNormsData(
          plantId,
          businessData,
          keycloak,
        )
        // dispatch(setIsBlocked(true))

        setSnackbarOpen(true)
        setSnackbarData({
          message: `Data Saved Successfully!`,
          severity: 'success',
        })
        setModifiedCells({})

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }

        setLoading(false)
        setCalculatebtnClicked(false)

        return response
      } else {
        setSnackbarOpen(true)
        setLoading(false)
        setSnackbarData({
          message: `Data not saved!`,
          severity: 'error',
        })
        setCalculatebtnClicked(false)
      }
    } catch (error) {
      console.error(`Error saving Data`, error)
      setLoading(false)
    } finally {
      fetchData(gradeId)
      setCalculatebtnClicked(false)
      setLoading(false)
    }
  }

  const [calculationObject, setCalculationObject] = useState([])

  const fetchData = async (gradeId) => {
    console.log('gradeID', gradeId)
    try {
      setLoading(true)
      setRows([])

      const verticalsRequiringGrade = ['pe', 'pp']
      if (verticalsRequiringGrade.includes(lowerVertName) && !gradeId) {
        setLoading(false)
        return
      }

      // let gradeId = null
      // if (lowerVertName == 'pe' || lowerVertName == 'pp') {
      //   gradeId = '0E39FD68-D4DC-4FA9-A686-6A265BC35580'
      // }

      const data = await DataService.getShutdownNormsData(keycloak, gradeId)

      if (data?.code != 200) {
        setRows([])
        setLoading(false)
        return
      }

      setCalculationObject(data?.data?.aopCalculation)

      const isTPD = selectedUnit === 'TPD'

      const formattedData = data?.data?.mcuNormsValueDTOList?.map(
        (item, index) => {
          const baseItem = {
            ...item,
            idFromApi: item.id,
            id: index,
            remarks: item?.remarks?.trim() || null,
            originalRemark: item?.remarks?.trim(),
            materialFkId: item?.materialFkId?.toLowerCase(),
            Particulars: item.normParameterTypeDisplayName || 'By Products',
            isEditable: true,
          }

          if (isTPD) {
            const months = [
              'april',
              'may',
              'june',
              'july',
              'august',
              'september',
              'october',
              'november',
              'december',
              'january',
              'february',
              'march',
            ]

            months.forEach((month) => {
              const value = item[month]
              baseItem[month] = value ? (value / 24).toFixed(2) : value || null
            })
          }

          return baseItem
        },
      )

      setRows(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }
  const [grades, setGrades] = useState([])

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  const onProcessRowUpdateError = React.useCallback((error) => {
    console.log(error)
  }, [])

  const handleCalculatePe = async () => {
    setCalculatebtnClicked(true)
    setLoading(true)
    try {
      const year = localStorage.getItem('year')
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var plantId = plantId
      const responce = await DataService.handleCalculateShutdownNorms(
        plantId,
        year,
        keycloak,
      )

      if (responce?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        setLoading(false)

        loadGrades()
        fetchData(gradeId)
      }

      // dispatch(setIsBlocked(true))
      else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
        setLoading(false)
      }
    } catch (error) {
      console.error('Error saving refresh data:', error)
      setLoading(false)
    }
  }

  const handleCalculate = () => {
    handleCalculatePe()
  }

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      showCalculate: false,
      // noColor: true,
      allAction: true,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      units: ['TPH', 'TPD'],
      saveWithRemark: false,
      saveBtn: true,
      showCalculate: lowerVertName == 'meg' ? false : true,
      showCalculateVisibility:
        lowerVertName != 'meg' &&
        Object.keys(calculationObject || {}).length > 0
          ? true
          : false,
      // noColor: true,

      showG: lowerVertName === 'pe' || lowerVertName === 'pp' ? true : false,
      dropdownLabel:
        lowerVertName === 'pe' || lowerVertName === 'pp'
          ? 'Select Grade'
          : 'Select Mode',
      allAction: true,
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Shutdown Consumption (Quantity)`,
    },
    isOldYear,
  )

  const handleGradeChange = (gradeId) => {
    console.log('gradeIdgradeId', gradeId)

    setGradeId(gradeId)
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        isCellEditable={isCellEditable}
        title='Shutdown Norms'
        columns={colDefs}
        setRows={setRows}
        rows={rows}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        processRowUpdate={processRowUpdate}
        handleUnitChange={handleUnitChange}
        onRowModesModelChange={onRowModesModelChange}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        rowModesModel={rowModesModel}
        open1={open1}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        onProcessRowUpdateError={onProcessRowUpdateError}
        // fetchData={fetchData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        handleRemarkCellClick={handleRemarkCellClick}
        handleCalculate={handleCalculate}
        groupBy='Particulars'
        permissions={adjustedPermissions}
        handleGradeChange={handleGradeChange}
        plantID={plantID}
        grades={grades}
      />
    </div>
  )
}

export default ShutdownNorms
