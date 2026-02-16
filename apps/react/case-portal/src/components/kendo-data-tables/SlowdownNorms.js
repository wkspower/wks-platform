import { useGridApiRef } from '@mui/x-data-grid'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
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
import getSlowdownNormsColDef from 'components/data-tables/CommonHeader/getSlowdownNormsColDef'
import { verticalEnums } from 'enums/verticalEnums'
import KendoDataTables from './index'
import SlowdownNormForMeg from './SlowdownNormForMeg'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { ShutdownNormsApiService } from 'services/shutdown-norms-api-service'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { getRoleName } from 'services/role-service'
import { SlowdownNormForMegServices } from 'services/SlowdownNormForMegServices'
const SlowdownNorms = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [loading, setLoading] = useState(false)
  const menu = useSelector((state) => state.dataGridStore)
  const [allProducts, setAllProducts] = useState([])
  const [grades, setGrades] = useState([])

  const [slowdownMonths, setSlowdownMonths] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const IS_OLD_YEAR = oldYear?.oldYear
  const isOldYear = false

  const PLANT_NAME_NO_CASE = plantObject?.name?.toUpperCase()
  const SITE_NAME_NO_CASE = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME_NO_CASE = verticalObject?.name?.toUpperCase()

  const SITE_NAME_LOWERCASE = siteObject?.name?.toLowerCase()
  const PLANT_NAME_LOWERCASE = plantObject?.name?.toLowerCase()
  const VERTICAL_NAME_LOWERCASE = verticalObject?.name?.toLowerCase()

  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}`
  const IS_PE_PP_VERTICAL_NMD_LLDPE =
    ['pe'].includes(VERTICAL_NAME_LOWERCASE) &&
    ['nmd'].includes(SITE_NAME_LOWERCASE) &&
    ['lldpe1', 'lldpe2'].includes(PLANT_NAME_LOWERCASE)

  const [open1, setOpen1] = useState(false)
  // const [deleteId, setDeleteId] = useState(null)
  const apiRef = useGridApiRef()
  // const dispatch = useDispatch()
  const [rows, setRows] = useState([])
  // const [productNormData, setProductNormData] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const headerMap = generateHeaderNames(AOP_YEAR)
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [rowModesModel, setRowModesModel] = useState({}) // Track row edit state
  const vertName = verticalChange?.selectedVertical
  const SCREEN_NAME = screenTitle?.title
  const lowerVertName = vertName?.toLowerCase()
  const plantName = plantObject?.name.toLowerCase()
  const siteName = siteObject?.name.toLowerCase()
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [gradeId, setGradeId] = useState(null)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const IS_PE_PP = lowerVertName === 'pe' || lowerVertName === 'pp'
  const IS_EDC_PLANT = lowerVertName === 'vcm' && plantName === 'edc'
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

      saveSlowdownNormsData(data)
    } catch (error) {
      setLoading(false)
    }
  }, [apiRef, selectedUnit, calculatebtnClicked, modifiedCells])

  useEffect(() => {
    const loadData = async () => {
      if (!PLANT_ID || !AOP_YEAR) return
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
          data = await DataService.getSlowdownMonths(
            keycloak,
            gradeId,
            PLANT_ID,
            AOP_YEAR,
          )
        } else {
          data = await DataService.getSlowdownMonths(
            keycloak,
            null,
            PLANT_ID,
            AOP_YEAR,
          )
        }
        setSlowdownMonths(data)
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

  useEffect(() => {
    const getSlowdownMonths = async () => {
      if (!PLANT_ID || !AOP_YEAR) return
      try {
        const data = await DataService.getSlowdownMonths(
          keycloak,
          null,
          PLANT_ID,
          AOP_YEAR,
        )
        if (data) setSlowdownMonths(data)
      } catch (error) {
        console.error('Error fetching months:', error)
      } finally {
        // handleMenuClose();
      }
    }
    if (lowerVertName !== verticalEnums.MEG) {
      fetchData(gradeId)
      getSlowdownMonths()
    }
  }, [oldYear, yearChanged, keycloak, selectedUnit, PLANT_ID])

  const isCellEditable = (params) => {
    return params.row.isEditable
  }

  // const months = slowdownMonths
  const valueFormat = ValueFormatterConsumption()

  const colDefs = getSlowdownNormsColDef({
    headerMap,
    slowdownMonths,
    valueFormat,
  })

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable || READ_ONLY) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveSlowdownNormsData = async (newRows) => {
    setLoading(true)
    try {
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
        financialYear: AOP_YEAR,
        plantId: PLANT_ID,
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

        const response = await DataService.saveSlowdownNormsData(
          PLANT_ID,
          businessData,
          keycloak,
        )
        // dispatch(setIsBlocked(true))

        setSnackbarOpen(true)
        setSnackbarData({
          message: `Shutdown Norms Saved Successfully!`,
          severity: 'success',
        })
        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
        setModifiedCells({})

        setLoading(false)
        setCalculatebtnClicked(false)

        fetchData(gradeId)
        return response
      } else {
        setSnackbarOpen(true)
        setLoading(false)
        setSnackbarData({
          message: `Shutdown Norms not saved!`,
          severity: 'error',
        })
        setCalculatebtnClicked(false)
      }
    } catch (error) {
      console.error(`Error saving Shutdown Norms`, error)
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

      const isPEorPP = ['pe', 'pp'].includes(lowerVertName)
      const isElastomer = ['elastomer'].includes(lowerVertName)

      if (isPEorPP && !gradeId) {
        setLoading(false)
        return
      }

      // Fetch data from API
      const data = await DataService.getSlowdownNormsData(
        keycloak,
        gradeId,
        PLANT_ID,
        AOP_YEAR,
      )

      setCalculationObject(data?.data?.aopCalculation)

      const formattedData = data?.data?.slowdownNormsValueDTO?.map(
        (item, index) => {
          const baseItem = {
            ...item,
            idFromApi: item.id,
            id: index,
            remarks: item?.remarks?.trim() || null,
            originalRemark: item?.remarks?.trim(),
            materialFkId: item?.materialFkId?.toLowerCase(),
            Particulars: item.normParameterTypeDisplayName || 'By Products',
            isEditable: isPEorPP
              ? false
              : isElastomer
                ? item?.isEditable
                : true,
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

  const handleCalculate = () => {
    handleCalculateData()
  }

  const loadGradesAfterCalculation = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    if (['pe', 'pp'].includes(lowerVertName)) {
      try {
        const response =
          await NormalOperationNormsApiService.getGradesForSlowdownNorms(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
          )

        if (response?.code === 200) {
          const fetchedGrades = response?.data || []
          setGrades(fetchedGrades)

          if (fetchedGrades.length === 0) {
            setGradeId(null)
            await fetchData(null)
            return
          }

          const firstGrade = fetchedGrades[0]
          const firstId = firstGrade?.id ?? firstGrade?.gradeId ?? null

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
      try {
        const months = await DataService.getSlowdownMonths(
          keycloak,
          null,
          PLANT_ID,
          AOP_YEAR,
        )
        setSlowdownMonths(months)
      } catch (err) {
        console.error('Error fetching shutdown months:', err)
      }
    }
  }

  const handleCalculateData = async () => {
    setRows([])
    setGrades([])
    setGradeId(null)
    setSlowdownMonths([])

    setCalculatebtnClicked(true)
    setLoading(true)
    try {
      var response = []
      if (lowerVertName == 'pp') {
        response = await DataService.handleCalculateSlowdownNormsPP(
          PLANT_ID,
          AOP_YEAR,
          keycloak,
        )
      } else if (lowerVertName == 'vcm') {
        response =
          await SlowdownNormForMegServices.getSlowdownNormsCalculateForMeg({
            keycloak,
            PLANT_ID,
            year: AOP_YEAR,
          })
      } else {
        response = await DataService.handleCalculateSlowdownNorms(
          PLANT_ID,
          AOP_YEAR,
          keycloak,
        )
      }
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

  const onRowModesModelChange = (newRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const handleExcelUpload = (rawFile) => {
    saveExcelFile(rawFile)
  }

  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response

      if (lowerVertName === 'vcm') {
        // Use slowdownconsumptionExportVCM for VCM
        response = await DataService.slowdownconsumptionExportVCM(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          gradeId,
        )
      } else if (lowerVertName === 'pp' || lowerVertName === 'pe') {
        // Use slowdownconsumptionExport for PE/PP
        response = await DataService.slowdownconsumptionExport(
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

  const saveExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      let response

      if (lowerVertName === 'vcm') {
        // Use saveShutdownNormsExcelNonGrade for VCM
        response = await DataService.saveSlowdownNormsExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          gradeId,
        )
      }

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData(gradeId)
      } else if (response?.code === 400 && response?.data) {
        // Partial save, error file download
        const byteCharacters = atob(response.data)
        const byteNumbers = new Array(byteCharacters.length)
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i)
        }
        const byteArray = new Uint8Array(byteNumbers)
        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'Error File Slowdown_Consumption.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Save Failed!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setLoading(false)
    } finally {
      fetchData(gradeId)
      setLoading(false)
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
      // noColor: true,
    }
  }

  const [calculationObject, setCalculationObject] = useState([])

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      units: ['TPH', 'TPD'],
      saveWithRemark: false,
      saveBtn: IS_PE_PP ? false : true,
      showCalculate:
        lowerVertName == 'meg' ||
        lowerVertName == 'elastomer' ||
        lowerVertName == 'aromatics' ||
        lowerVertName == 'pta' ||
        IS_PE_PP ||
        IS_EDC_PLANT
          ? false
          : true,

      allAction: true,
      dropdownLabel: 'Select Grade',
      downloadExcelBtnFromUI:
        IS_PE_PP || lowerVertName === 'vcm' ? false : true,
      uploadExcelBtn: lowerVertName === 'vcm' ? true : false,
      downloadExcelBtn: IS_PE_PP || lowerVertName === 'vcm' ? true : false,
      showG: IS_PE_PP ? true : false,
      marginBottom: IS_PE_PP ? true : false,

      ExcelName: `${EXCEL_EXPORT_TITLE}-Slowdown Consumption`,
      showCalculateVisibility:
        Object.keys(calculationObject || {}).length > 0 ? true : false,

      showTitleNameBusiness: true,
      titleName:
        lowerVertName === 'elastomer'
          ? `Slowdown Consumption (Norms/Quantity)`
          : IS_PE_PP_VERTICAL_NMD_LLDPE
            ? 'Total Loss'
            : `${SCREEN_NAME}`,
    },
    isOldYear,
  )

  // 1) Load grades list if vertical requires it
  useEffect(() => {
    const loadGrades = async () => {
      if (IS_PE_PP) {
        try {
          const response =
            await NormalOperationNormsApiService.getGradesForSlowdownNorms(
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
  }, [PLANT_ID, yearChanged, keycloak, AOP_YEAR])

  const handleGradeChange = (gradeId) => {
    setGradeId(gradeId)
  }

  if (lowerVertName === 'pp' && siteName === 'nmd' && plantName === 'pp') {
    return null // Or: return <div>Screen hidden for this configuration.</div>
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      {lowerVertName === 'meg' ? (
        <SlowdownNormForMeg />
      ) : (
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
          unsavedChangesRef={unsavedChangesRef}
          handleRemarkCellClick={handleRemarkCellClick}
          handleCalculate={handleCalculate}
          permissions={adjustedPermissions}
          groupBy='Particulars'
          plantID={plantID}
          grades={grades}
          calculatebtnClicked={calculatebtnClicked}
          handleGradeChange={handleGradeChange}
          handleExcelUpload={handleExcelUpload}
          downloadExcelForConfiguration={downloadExcelForConfiguration}
        />
      )}
    </div>
  )
}

export default SlowdownNorms
