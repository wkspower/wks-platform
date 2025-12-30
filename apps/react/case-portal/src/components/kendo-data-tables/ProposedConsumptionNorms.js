import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import { validateFields } from 'utils/validationUtils'

import { Box } from '@mui/material'
import getEnhancedColDefsProposedNorms from 'components/data-tables/CommonHeader/Kendo_Proposed_Norms_Header'
import { ConsumptionNormsApiService } from 'services/consumption-norms-api-service'
import { getRoleName } from 'services/role-service'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import KendoDataTables from './index'

const ProposedConsumptionNorms = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [calculationObject, setCalculationObject] = useState([])
  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)
  const [open1, setOpen1] = useState(false)
  const valueFormat = ValueFormatterConsumption()
  const defaultCustomHeight = { mainBox: '55vh', otherBox: '112%' }

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
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const SCREEN_NAME = screenTitle?.title
  const headerMap = generateHeaderNames(AOP_YEAR)

  const PLANT_NAME_NO_CASE = plantObject?.name?.toUpperCase()
  const SITE_NAME_NO_CASE = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME_NO_CASE = verticalObject?.name?.toUpperCase()

  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}`

  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

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
  const [_plantID, set_PlantID] = useState('')
  const dispatch = useDispatch()
  const [gradeId, setGradeId] = useState(null)
  const [grades, setGrades] = useState([])

  const isPEPP = lowerVertName === 'pe' || lowerVertName === 'pp'
  const isPET = lowerVertName === 'pet'

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveEditedData = async (newRows) => {
    setLoading(true)
    try {
      let plantId = PLANT_ID
      let siteID = SITE_ID
      let verticalId = VERTICAL_ID

      const payload = newRows.map((row) => ({
        id: row?.idFromApi ?? null,

        prevYearBudgetApril: row?.prevYearBudgetApril ?? null,
        prevYearBudgetMay: row?.prevYearBudgetMay ?? null,
        prevYearBudgetJune: row?.prevYearBudgetJune ?? null,
        prevYearBudgetJuly: row?.prevYearBudgetJuly ?? null,
        prevYearBudgetAugust: row?.prevYearBudgetAugust ?? null,
        prevYearBudgetSeptember: row?.prevYearBudgetSeptember ?? null,
        prevYearBudgetOctober: row?.prevYearBudgetOctober ?? null,
        prevYearBudgetNovember: row?.prevYearBudgetNovember ?? null,
        prevYearBudgetDecember: row?.prevYearBudgetDecember ?? null,
        prevYearBudgetJanuary: row?.prevYearBudgetJanuary ?? null,
        prevYearBudgetFebruary: row?.prevYearBudgetFebruary ?? null,
        prevYearBudgetMarch: row?.prevYearBudgetMarch ?? null,

        currYearBudgetApril: row?.currYearBudgetApril ?? null,
        currYearBudgetMay: row?.currYearBudgetMay ?? null,
        currYearBudgetJune: row?.currYearBudgetJune ?? null,
        currYearBudgetJuly: row?.currYearBudgetJuly ?? null,
        currYearBudgetAugust: row?.currYearBudgetAugust ?? null,
        currYearBudgetSeptember: row?.currYearBudgetSeptember ?? null,
        currYearBudgetOctober: row?.currYearBudgetOctober ?? null,
        currYearBudgetNovember: row?.currYearBudgetNovember ?? null,
        currYearBudgetDecember: row?.currYearBudgetDecember ?? null,
        currYearBudgetJanuary: row?.currYearBudgetJanuary ?? null,
        currYearBudgetFebruary: row?.currYearBudgetFebruary ?? null,
        currYearBudgetMarch: row?.currYearBudgetMarch ?? null,

        currYearProposedApril: row?.currYearProposedApril ?? null,
        currYearProposedMay: row?.currYearProposedMay ?? null,
        currYearProposedJune: row?.currYearProposedJune ?? null,
        currYearProposedJuly: row?.currYearProposedJuly ?? null,
        currYearProposedAugust: row?.currYearProposedAugust ?? null,
        currYearProposedSeptember: row?.currYearProposedSeptember ?? null,
        currYearProposedOctober: row?.currYearProposedOctober ?? null,
        currYearProposedNovember: row?.currYearProposedNovember ?? null,
        currYearProposedDecember: row?.currYearProposedDecember ?? null,
        currYearProposedJanuary: row?.currYearProposedJanuary ?? null,
        currYearProposedFebruary: row?.currYearProposedFebruary ?? null,
        currYearProposedMarch: row?.currYearProposedMarch ?? null,

        remarks: row?.remarks ?? null,
      }))

      // console.log('payload', payload)

      const response = await ConsumptionNormsApiService.saveProposedNormsData(
        PLANT_ID,
        AOP_YEAR,
        payload,
        keycloak,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Saved Successfully!',
        severity: 'success',
      })
      //

      setLoading(false)
      setModifiedCells({})

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      fetchData(gradeId)
      dispatch(setIsBlocked(false))

      return response
    } catch (error) {
      console.error('Error saving data!', error)
    } finally {
      //
      setLoading(false)
    }
  }

  const saveChanges = React.useCallback(async () => {
    setLoading(true)
    var editedData = Object.values(modifiedCells)
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

    saveEditedData(editedData)
  }, [apiRef, selectedUnit, modifiedCells, calculatebtnClicked])

  const fetchGradeDropdowns = async () => {
    try {
      const response =
        await ConsumptionNormsApiService.getConsumptionAOPNormsGrades(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

      if (response?.code == 200) {
        setGrades(response?.data)
      }

      fetchData(response?.data[0]?.gradeId)
    } catch (error) {
      setGrades([])
      console.error('Error fetching data:', error)
    }
  }

  const fetchGradeDropdownsAfterCalc = async () => {
    try {
      setGrades([])
      const response =
        await ConsumptionNormsApiService.getConsumptionAOPNormsGrades(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

      if (response?.code == 200) {
        setGrades(response?.data)
      }

      if (response?.data?.length === 0) {
        setGradeId(null)
        await fetchData(null)
        return
      }

      const firstGrade = response?.data[0]
      const firstId =
        firstGrade?.id ?? firstGrade?.gradeId ?? firstGrade?.gradeFkId ?? null

      setGradeId(firstId)

      fetchData(firstId)
    } catch (error) {
      setGrades([])
      console.error('Error fetching Business Demand data:', error)
    }
  }

  const fetchData = async (gradeId) => {
    if (!PLANT_ID || !AOP_YEAR) return
    if ((isPEPP || isPET) && !gradeId) return
    setLoading(true)
    try {
      var response
      setRows([])

      response = await ConsumptionNormsApiService.getProposedNormsData(
        keycloak,
        gradeId,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code != 200) {
        setRows([])
        setLoading(false)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error fetching data. Please try again.',
          severity: 'error',
        })

        return
      }
      setCalculationObject(response?.data?.aopCalculation)

      // const formattedData = response?.data?.aopProposedNormsDTOList?.map((item, index) => {
      const formattedData = response?.data?.aopProposedNormsDTOList?.map((item, index) => {
        return {
          ...item,
          idFromApi: item.id,
          originalRemark: item.remarks?.trim() || null,
          id: index,
          Particulars: item.normParameterTypeDisplayName || 'Type',
          isEditable: true,
        }
      })

      setRows(formattedData)
      setLoading(false)
      setCalculatebtnClicked(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
      setCalculatebtnClicked(false)
    }
  }

  useEffect(() => {
    fetchGradeDropdowns()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak])

  const productionColumns = getEnhancedColDefsProposedNorms({
    headerMap,
    lowerVertName,
    valueFormat,
    AOP_YEAR,
  })

  // console.log('productionColumns', productionColumns)

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }

  const handleCalculate = () => {
    handleCalculateMeg()
  }

  const handleCalculateMeg = async () => {
    try {
      const data =
        await ConsumptionNormsApiService.handleCalculateConsumptionNorms(
          PLANT_ID,
          AOP_YEAR,
          keycloak,
        )

      if (data || data == 0) {
        // dispatch(setIsBlocked(true))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })

        if (
          lowerVertName === 'pe' ||
          lowerVertName === 'pp' ||
          lowerVertName === 'pet'
        ) {
          fetchGradeDropdownsAfterCalc()
        } else {
          fetchData(null)
        }
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

  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response
      if (
        lowerVertName === 'pe' ||
        lowerVertName === 'pp' ||
        lowerVertName === 'pet'
      ) {
        response =
          await ConsumptionNormsApiService.OverallConsumptionPEPPExport(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
            EXCEL_EXPORT_TITLE,
            SCREEN_NAME,
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
      saveWithRemark: true,
      saveBtn: true,
      showCalculate: false,
      allAction: true,
      showCalculateVisibility:
        Object.keys(calculationObject || {}).length > 0 ? true : false,
      showRefresh: false,
      noColor: false,
      customHeight: defaultCustomHeight,
      showG: true,
      marginBottom: true,
      dropdownLabel: 'Select Grade',
      downloadExcelBtnFromUI: true,
      downloadExcelBtn: false,
      ExcelName: `${EXCEL_EXPORT_TITLE}_${SCREEN_NAME}`,
      isHeight: rows?.length > 10,
      showTitleNameBusiness: true,
      titleName: `${SCREEN_NAME}`,
    },
    isOldYear,
  )

  const handleGradeChange = (gradeId) => {
    setGradeId(gradeId)
    fetchData(gradeId)
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <div>
        {
          <Box
            sx={{
              width: '100%',
              padding: '0px ',
              margin: '0px',
              backgroundColor: '#F2F3F8',
              borderRadius: 0,
              borderBottom: 'none',
            }}
          >
            <KendoDataTables
              modifiedCells={modifiedCells}
              setModifiedCells={setModifiedCells}
              columns={productionColumns}
              rows={rows}
              setRows={setRows}
              fetchData={fetchData}
              getRowId={(row) => row.id}
              paginationOptions={[100, 200, 300]}
              saveChanges={saveChanges}
              snackbarData={snackbarData}
              snackbarOpen={snackbarOpen}
              apiRef={apiRef}
              open1={open1}
              setOpen1={setOpen1}
              setSnackbarOpen={setSnackbarOpen}
              setSnackbarData={setSnackbarData}
              handleCalculate={handleCalculate}
              handleRemarkCellClick={handleRemarkCellClick}
              handleUnitChange={handleUnitChange}
              remarkDialogOpen={remarkDialogOpen}
              setRemarkDialogOpen={setRemarkDialogOpen}
              currentRemark={currentRemark}
              setCurrentRemark={setCurrentRemark}
              currentRowId={currentRowId}
              permissions={adjustedPermissions}
              groupBy='Particulars'
              grades={grades}
              handleGradeChange={handleGradeChange}
              calculatebtnClicked={calculatebtnClicked}
              downloadExcelForConfiguration={downloadExcelForConfiguration}
              plantID={PLANT_ID}
            />
          </Box>
        }
      </div>
    </div>
  )
}

export default ProposedConsumptionNorms
