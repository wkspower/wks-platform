import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getShutdownConsumptionColDef from 'components/data-tables/CommonHeader/getShutdownConsumptionColDef'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'

import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { validateFields } from 'utils/validationUtils'
import KendoDataTables from './index'
import { ShutdownNormsApiService } from 'services/shutdown-norms-api-service'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { getRoleName } from 'services/role-service'

const ShutdownNorms = () => {
  const [gradeId, setGradeId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [loading, setLoading] = useState(false)
  const menu = useSelector((state) => state.dataGridStore)
  const [shutdownMonths, setShutdownMonths] = useState([])
  const { yearChanged, oldYear, plantID } = menu
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const [open1, setOpen1] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [_plantID, set_PlantID] = useState('')
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [rowModesModel, setRowModesModel] = useState({})
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    screenTitle,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [calculationObject, setCalculationObject] = useState([])
  const [grades, setGrades] = useState([])

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YAER = year?.selectedYear

  const PLANT_NAME = plantObject?.name
  const SITE_NAME = siteObject?.name
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const SCREEN_NAME = screenTitle?.title
  const headerMap = generateHeaderNames(AOP_YEAR)
  const IS_PE_PP_VERTICAL = ['pe', 'pp'].includes(lowerVertName)
  const textNote = IS_PE_PP_VERTICAL
    ? '*Adding shutdown consumption to all grades will replace any existing individual grade consumption entries.'
    : '*Quantities are per day basis'
  const textNoteWhileSaving =
    'Warning : Adding shutdown consumption to all grades will replace any existing individual grade consumption entries.'

  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const saveChanges = React.useCallback(async () => {
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
  }, [apiRef, selectedUnit, calculatebtnClicked, modifiedCells])

  // 1) Load grades list if vertical requires it
  useEffect(() => {
    const loadGrades = async () => {
      if (IS_PE_PP_VERTICAL) {
        try {
          const response =
            await NormalOperationNormsApiService.getGradesForShutdownNorms(
              keycloak,
              PLANT_ID,
              AOP_YEAR,
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
  }, [PLANT_ID, yearChanged, keycloak])

  // 2) Fetch main data when gradeId or other deps change

  useEffect(() => {
    const loadData = async () => {
      if (!PLANT_ID || !AOP_YEAR) return
      try {
        if (IS_PE_PP_VERTICAL) {
          if (!gradeId) return
          await fetchData(gradeId)
        } else {
          await fetchData()
        }
        let data

        {
          data = await ShutdownNormsApiService.getShutdownMonths(
            keycloak,
            null,
            PLANT_ID,
            AOP_YEAR,
          )
        }
        setShutdownMonths(data)

        // if (lowerVertName == 'cracker') {
        //   setShutdownMonths([1])
        // }
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
    AOP_YEAR,
  ])

  const isCellEditable = (params) => {
    return params.row.isEditable
  }
  const valueFormat = ValueFormatterConsumption()
  const colDefs = getShutdownConsumptionColDef({
    headerMap,
    shutdownMonths,
    valueFormat,
  })

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable || READ_ONLY) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveShutDownNormsData = async (rows) => {
    setLoading(true)
    try {
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
        financialYear: AOP_YEAR,
        plantId: PLANT_ID,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
        materialFkId: row.materialFkId || null,
        mcuVersion: row.mcuVersion || null,
        plantFkId: PLANT_ID,
        siteFkId: SITE_ID,
        verticalFkId: VERTICAL_ID,
        unit: row.unit || null,
        normParameterTypeId: row.normParameterTypeId || null,
        gradeFkId: gradeId || null,
      }))
      if (payload.length > 0) {
        const response = await ShutdownNormsApiService.saveShutDownNormsData(
          PLANT_ID,
          payload,
          keycloak,
          AOP_YEAR,
        )
        // dispatch(setIsBlocked(true))

        setSnackbarOpen(true)
        setSnackbarData({
          message: `Saved Successfully!`,
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
    if (!PLANT_ID || !AOP_YEAR) return

    try {
      setLoading(true)
      setRows([])

      const verticalsRequiringGrade = ['pe', 'pp']
      if (verticalsRequiringGrade.includes(lowerVertName) && !gradeId) {
        setLoading(false)
        return
      }
      let data = []

      if (lowerVertName != 'cracker') {
        data = await ShutdownNormsApiService.getShutdownNormsData(
          keycloak,
          gradeId,
          PLANT_ID,
          AOP_YAER,
        )
      } else {
        data = await ShutdownNormsApiService.shutdownConsumptionHistoryData(
          keycloak,
          gradeId,
          PLANT_ID,
          AOP_YAER,
        )
      }

      if (data?.code != 200) {
        setRows([])
        setLoading(false)
        return
      }

      setCalculationObject(data?.data?.aopCalculation)

      let formattedData = []

      const isElastomer = ['elastomer'].includes(lowerVertName)

      if (lowerVertName != 'cracker') {
        formattedData = data?.data?.mcuNormsValueDTOList?.map((item, index) => {
          const baseItem = {
            ...item,
            idFromApi: item.id,
            id: index,
            remarks: item?.remarks?.trim() || null,
            originalRemark: item?.remarks?.trim(),
            materialFkId: item?.materialFkId?.toLowerCase(),
            Particulars: item.normParameterTypeDisplayName || 'Particulars',
            isEditable: isElastomer ? item?.isEditable : true,
          }

          return baseItem
        })
      } else {
        // For cracker, use mcuNormsValueDTOList if present, else fallback to data.data
        const crackerArray = Array.isArray(data?.data?.mcuNormsValueDTOList)
          ? data.data.mcuNormsValueDTOList
          : Array.isArray(data?.data)
            ? data.data
            : []
        formattedData = crackerArray.map((item, index) => {
          const baseItem = {
            ...item,
            idFromApi: item.id,
            id: index,
            materialFkId: item?.materialFkId?.toLowerCase(),
            Particulars: item.normParameterTypeDisplayName || 'Type',
            isEditable: item?.isEditable || true,
            originalRemark: item.remarks,
          }
          return baseItem
        })
      }

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

  // --- handleCalculateData (call loadGradesAfterCalculation after success) ---
  const handleCalculateData = async () => {
    setRows([])
    setGrades([])
    setGradeId(null)
    setShutdownMonths([])

    setCalculatebtnClicked(true)
    setLoading(true)
    try {
      const response =
        await ShutdownNormsApiService.handleCalculateShutdownNorms(
          PLANT_ID,
          AOP_YEAR,
          keycloak,
        )

      if (response?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })

        // load grades and pick the 0th index
        await loadGradesAfterCalculation()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error saving refresh data:', error)
    } finally {
      setLoading(false)
      setCalculatebtnClicked(false)
    }
  }

  // --- loadGradesAfterCalculation (always pick the first returned grade) ---
  const loadGradesAfterCalculation = async () => {
    if (['pe', 'pp'].includes(lowerVertName)) {
      try {
        const response =
          await NormalOperationNormsApiService.getGradesForShutdownNorms(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
          )

        if (response?.code === 200) {
          const fetchedGrades = response?.data || []
          setGrades(fetchedGrades)

          if (fetchedGrades.length === 0) {
            // no grades — clear selection and fetch blank data
            setGradeId(null)
            await fetchData(null)
            return
          }

          // pick the 0th index (use the correct id field from your grade object)
          const firstGrade = fetchedGrades[0]
          const firstId =
            firstGrade?.id ??
            firstGrade?.gradeId ??
            firstGrade?.gradeFkId ??
            null

          setGradeId(firstId)
          await fetchData(firstId)
        } else {
          setGrades([])
          setGradeId(null)
          await fetchData(null)
        }
      } catch (error) {
        console.error('Error fetching grades:', error)
        setGrades([])
        setGradeId(null)
        await fetchData(null)
      }
    } else {
      // non PE/PP flow
      await fetchData(null)
      if (!PLANT_ID || !AOP_YEAR) return
      try {
        const months = await ShutdownNormsApiService.getShutdownMonths(
          keycloak,
          null,
          PLANT_ID,
          AOP_YEAR,
        )
        setShutdownMonths(months)
      } catch (err) {
        console.error('Error fetching shutdown months:', err)
      }
    }
  }

  const handleCalculate = () => {
    handleCalculateData()
  }

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }
  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response
      if (IS_PE_PP_VERTICAL) {
        response = await NormalOperationNormsApiService.shutdownnormsppExport(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      }
    } catch (error) {
      console.error('Error downloading Excel:', error)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(true)
    }
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

      showNote: lowerVertName === 'meg' || IS_PE_PP_VERTICAL ? true : false,
      showNoteWhileSaving: IS_PE_PP_VERTICAL ? true : false,

      saveBtn: true,
      showCalculate:
        lowerVertName == 'meg' ||
        lowerVertName == 'elastomer' ||
        lowerVertName == 'aromatics' ||
        lowerVertName == 'pta' ||
        lowerVertName == 'vcm' ||
        IS_PE_PP_VERTICAL
          ? false
          : true,
      showCalculateVisibility:
        lowerVertName != 'meg' &&
        lowerVertName != 'pta' &&
        Object.keys(calculationObject || {}).length > 0
          ? true
          : false,

      showG: IS_PE_PP_VERTICAL ? true : false,
      marginBottom: IS_PE_PP_VERTICAL ? true : false,
      dropdownLabel: 'Select Grade',
      allAction: true,
      downloadExcelBtnFromUI: IS_PE_PP_VERTICAL ? false : true,
      downloadExcelBtn: IS_PE_PP_VERTICAL ? true : false,
      showTitleNameBusiness: true,

      titleName:
        lowerVertName === 'elastomer' ||
        lowerVertName === 'pta' ||
        lowerVertName === 'vcm'
          ? `Shutdown Consumption (Norms/Quantity)`
          : SCREEN_NAME,
      ExcelName: `${VERTICAL_NAME}-${SCREEN_NAME}`,
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
        downloadExcelForConfiguration={downloadExcelForConfiguration}
        calculatebtnClicked={calculatebtnClicked}
        plantID={plantID}
        grades={grades}
        note={textNote}
        noteOnSaveDialogeBox={textNoteWhileSaving}
      />
    </div>
  )
}

export default ShutdownNorms
