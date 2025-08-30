import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getShutdownConsumptionColDef from 'components/data-tables/CommonHeader/getShutdownConsumptionColDef'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { NormalOperationNormsApiService } from 'services/NormalOperationNormsApiService'
import { validateFields } from 'utils/validationUtils'
import KendoDataTables from './index'

const ShutdownNorms = () => {
  const [gradeId, setGradeId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [loading, setLoading] = useState(false)
  const menu = useSelector((state) => state.dataGridStore)
  const [shutdownMonths, setShutdownMonths] = useState([])
  const { yearChanged, oldYear, plantID } = menu
  const isOldYear = oldYear?.oldYear
  const [open1, setOpen1] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [_plantID, set_PlantID] = useState('')
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [rowModesModel, setRowModesModel] = useState({})
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [calculationObject, setCalculationObject] = useState([])
  const [grades, setGrades] = useState([])

  useEffect(() => {
    if (plantID?.plantId) {
      set_PlantID(plantID?.plantId)
    }
  }, [plantID])

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

  // 1) Load grades list if vertical requires it
  useEffect(() => {
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
          setGradeId(null)
          console.error('Error fetching grades:', error)
        }
      }
    }
    loadGrades()
  }, [plantID, yearChanged, keycloak])

  // 2) Fetch main data when gradeId or other deps change

  useEffect(() => {
    const loadData = async () => {
      try {
        if (['pe', 'pp'].includes(lowerVertName)) {
          if (!gradeId) return
          await fetchData(gradeId)
        } else {
          await fetchData()
        }
        let data
        if (['pe', 'pp'].includes(lowerVertName)) {
          if (!gradeId) return
          data = await DataService.getShutdownMonths(keycloak, gradeId)
        } else {
          data = await DataService.getShutdownMonths(keycloak, null)
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

  const saveShutDownNormsData = async (rows) => {
    setLoading(true)
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const payload = rows.map((row) => ({
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
        gradeFkId: gradeId || null,
      }))
      if (payload.length > 0) {
        const response = await DataService.saveShutDownNormsData(
          plantId,
          payload,
          keycloak,
        )
        // dispatch(setIsBlocked(true))

        setSnackbarOpen(true)
        setSnackbarData({
          message: `Data Saved Successfully!`,
          severity: 'success',
        })
        setModifiedCells({})
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

  const fetchData = async (gradeId) => {
    try {
      setLoading(true)
      setRows([])

      const verticalsRequiringGrade = ['pe', 'pp']
      if (verticalsRequiringGrade.includes(lowerVertName) && !gradeId) {
        setLoading(false)
        return
      }

      const data = await DataService.getShutdownNormsData(keycloak, gradeId)

      if (data?.code != 200) {
        setRows([])
        setLoading(false)
        return
      }

      setCalculationObject(data?.data?.aopCalculation)

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

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  const loadGradesAfterCalculation = async () => {
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
            return
          }
        }

        setGradeId(gradeId)
        fetchData(gradeId)
      } catch (error) {
        setGrades([])
        console.error('Error fetching grades:', error)
      }
    } else {
      fetchData(null)
      data = await DataService.getShutdownMonths(keycloak, null)
      setShutdownMonths(data)
    }
  }

  const handleCalculateData = async () => {
    setRows([])
    setGrades([])
    setGradeId(null)
    setShutdownMonths([])

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
      const response = await DataService.handleCalculateShutdownNorms(
        plantId,
        year,
        keycloak,
      )

      if (response?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        setLoading(false)

        loadGradesAfterCalculation()
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
    handleCalculateData()
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
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
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
