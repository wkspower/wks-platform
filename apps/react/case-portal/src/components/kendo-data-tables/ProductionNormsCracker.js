import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'

import getEnhancedColDefsC2C3R from 'components/data-tables/CommonHeader/Kendo_ProductionAopHeaderC2C3R'
import { DataService } from 'services/DataService'
import { ProductionNormsApiService } from 'services/production-norms-api-service'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import { validateFields } from 'utils/validationUtils'
import getEnhancedColDefs from '../data-tables/CommonHeader/Kendo_ProductionAopHeader'
import KendoDataTables from './index'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { getRoleName } from 'services/role-service'
const ProductionNormsCracker = ({ permissions }) => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [modifiedCellsC2C3R, setModifiedCellsC2C3R] = React.useState({})
  const [calculationObject, setCalculationObject] = useState([])
  const [
    calculationObjectOtherProduction,
    setCalculationObjectOtherProduction,
  ] = useState([])
  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)
  const apiRef = useGridApiRef()
  const apiRefC2C3R = useGridApiRef()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const [_plantID, set_PlantID] = useState('')

  const valueFormat = ValueFormatterProduction()

  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const PLANT_NAME = plantObject?.name
  const SITE_ID = siteObject?.id
  const SITE_NAME = siteObject?.name
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear

  const PLANT_NAME_UC = plantObject?.name?.toUpperCase()
  const SITE_NAME_UC = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME_UC = verticalObject?.name?.toUpperCase()

  const EXCEL_NAME = `${VERTICAL_NAME_UC}_${SITE_NAME_UC}_${PLANT_NAME_UC}_${AOP_YEAR}_Month_Wise_Production_Plan`
  const EXCEL_NAME_OTHER_PRODUCTION = `${VERTICAL_NAME_UC}_${SITE_NAME_UC}_${PLANT_NAME_UC}_${AOP_YEAR}_Other_Production_Plan`

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const [loading, setLoading] = useState(false)
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const headerMap = generateHeaderNames(AOP_YEAR)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('')
  const [rows, setRows] = useState([])
  const [rowsC2C3R, setRowsC2C3R] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [remarkDialogOpenC2C3R, setRemarkDialogOpenC2C3R] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRemarkC2C3R, setCurrentRemarkC2C3R] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [currentRowIdC2C3R, setCurrentRowIdC2C3R] = useState(null)

  const IS_NMD = SITE_NAME?.toLowerCase() == 'nmd'

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const dispatch = useDispatch()

  const saveChanges = React.useCallback(async () => {
    setTimeout(() => {
      try {
        var editedData = Object.values(modifiedCells)
        const allRows = Array.from(apiRef.current.getRowModels().values())
        const updatedRows = allRows.map(
          (row) => unsavedChangesRef.current.unsavedRows[row.id] || row,
        )
        const rowsToSave = updatedRows.filter((row) => row.id !== 'total')

        if (updatedRows.length === 0) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'No Records to Save!',
            severity: 'info',
          })
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
          return
        }

        if (calculatebtnClicked == false) {
          updateProductNormData(rowsToSave)
        } else {
          updateProductNormData(rowsToSave)
        }
      } catch (error) {
        console.log('Error saving changes:', error)

        setCalculatebtnClicked(false)
      }
    }, 400)
  }, [apiRef, selectedUnit, calculatebtnClicked])

  const saveChangesC2C3R = React.useCallback(async () => {
    try {
      var editedData = Object.values(modifiedCellsC2C3R)

      if (editedData?.length === 0) {
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
        return
      }

      updateProductNormData(editedData)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCellsC2C3R])

  const updateProductNormData = async (rowsToSave) => {
    setLoading(true)

    try {
      const payload = (rowsToSave || []).map((row) => ({
        apr: row.apr ?? null,
        may: row.may ?? null,
        jun: row.jun ?? null,
        jul: row.jul ?? null,
        aug: row.aug ?? null,
        sep: row.sep ?? null,
        oct: row.oct ?? null,
        nov: row.nov ?? null,
        dec: row.dec ?? null,
        jan: row.jan ?? null,
        feb: row.feb ?? null,
        mar: row.mar ?? null,
        UOM: row.UOM ?? '',
        auditYear: AOP_YEAR,
        normParameterFKId: row.normParametersFKId,
        remarks: row.remarks ?? '',
        id: null,
      }))

      let response

      if (!IS_NMD) {
        response = await ProductionNormsApiService.saveOtherProductionNorms(
          PLANT_ID,
          payload,
          keycloak,
          AOP_YEAR,
        )
      } else {
        response = await DataService.saveCatalystData(
          PLANT_ID,
          payload,
          keycloak,
          AOP_YEAR,
        )
      }

      // Adjust response check depending on your API (status, success flag, etc.)
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Saved Successfully!', severity: 'success' })

        // clear modified cells and refresh data
        setModifiedCellsC2C3R({})
        await fetchDataC2C3R()
        await fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Data Save Failed!', severity: 'error' })
      }
    } catch (error) {
      console.error('Error Saving Production AOP:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error Saving Production AOP!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
      setCalculatebtnClicked(false)
    }
  }

  const handleCalculateOtherProduction = async () => {
    // dispatch(setIsBlocked(true))
    setCalculatebtnClicked(true)
    setLoading(true)
    try {
      const data =
        await ProductionNormsApiService.handleCalculateOtherProduction(
          PLANT_ID,
          AOP_YEAR,
          keycloak,
        )
      if (data?.code == 200) {
        fetchDataC2C3R()
        fetchData()

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        setLoading(false)
        return
      }
      return res
    } catch (error) {
      console.error('Error saving refresh data:', error)
      setLoading(false)
    }
  }

  const handleCalculate = async () => {
    // dispatch(setIsBlocked(true))
    setCalculatebtnClicked(true)
    setLoading(true)
    try {
      const data = await ProductionNormsApiService.handleCalculate(
        PLANT_ID,
        AOP_YEAR,
        keycloak,
      )
      if (data?.code == 200) {
        fetchData()

        if (lowerVertName === 'meg') {
          fetchDataByProducts()
        }
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        setLoading(false)
        return
      }
      return res
    } catch (error) {
      console.error('Error saving refresh data:', error)
      setLoading(false)
    }
  }

  const rowDataForCracker = [
    {
      displayName: 'Ethyelene',
      april: 13420,
      may: 12875,
      june: 14210,
      july: 13750,
      aug: 12995,
      sep: 14130,
      oct: 13580,
      nov: 13045,
      dec: 13670,
      jan: 13920,
      feb: 13105,
      march: 13840,
      averageTPH: '',
      isEditable: false,
    },
    {
      displayName: 'Propylene',
      april: 9450,
      may: 10235,
      june: 11090,
      july: 10720.2322332332,
      aug: 11560,
      sep: 10985,
      oct: 11340,
      nov: 10575,
      dec: 11120,
      jan: 11280,
      feb: 10850,
      march: 11430,
      averageTPH: '',
      isEditable: false,
      aopStatus: '',
    },
    {
      displayName: 'E + P',
      april: 950,
      may: 1035,
      june: 1090.3422343241232,
      july: 1720,
      aug: 1560,
      sep: 985,
      oct: 140,
      nov: 575,
      dec: 1120,
      jan: 280,
      feb: 850,
      march: 1430,
      averageTPH: '',
      isEditable: false,
      aopStatus: '',
    },
  ]

  const fetchData = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      const response = await ProductionNormsApiService.getAOPData(
        keycloak,
        'Production',
        PLANT_ID,
        AOP_YEAR,
      )
      setCalculationObject(response?.data?.aopCalculation)
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

      let dataSet = response?.data?.aopDTOList

      var data = dataSet
        ?.map((product) => ({
          ...product,
          normParametersFKId: product.materialFKId,
          originalRemark: product.aopRemarks,
          isEditable: false,
          april: product?.april.toFixed(2) ?? '0.00',
          may: product?.may.toFixed(2) ?? '0.00',
          june: product?.june.toFixed(2) ?? '0.00',
          july: product?.july.toFixed(2) ?? '0.00',
          aug: product?.aug.toFixed(2) ?? '0.00',
          sep: product?.sep.toFixed(2) ?? '0.00',
          oct: product?.oct.toFixed(2) ?? '0.00',
          nov: product?.nov.toFixed(2) ?? '0.00',
          dec: product?.dec.toFixed(2) ?? '0.00',
          jan: product?.jan.toFixed(2) ?? '0.00',
          feb: product?.feb.toFixed(2) ?? '0.00',
          march: product?.march.toFixed(2) ?? '0.00',
          Particulars: product.normParameterDisplayName,
          ...(product.materialFKId !== undefined
            ? { materialFKId: undefined }
            : {}),
        }))
        .map(({ materialFKId, ...rest }) => rest)

      let formattedData = []

      const fiscalYear = AOP_YEAR
      const startYear = parseInt(fiscalYear.split('-')[0], 10)
      const nextYear = startYear + 1

      const isLeap = (year) => new Date(year, 1, 29).getDate() === 29

      if (lowerVertName === 'cracker') {
        formattedData = data.map((item, index) => {
          const TPH = selectedUnit == 'TPH'
          const transformedItem = {
            ...item,
            idFromApi: item.id,
            uom: selectedUnit ? selectedUnit : 'MT/Month',
            normParametersFKId: item?.normParametersFKId?.toLowerCase(),
            id: index,
            ...(TPH && {
              jan: item.jan
                ? item.jan / 24 / 31
                : item.jan.toFixed(2) ?? '0.00',
              feb: item.feb
                ? item.feb / 24 / (isLeap(nextYear) ? 29 : 28)
                : item.feb.toFixed(2) ?? '0.00',
              march: item.march
                ? item.march / 24 / 31
                : item.march.toFixed(2) ?? '0.00',
              april: item.april
                ? item.april / 24 / 30
                : item.april.toFixed(2) ?? '0.00',
              may: item.may
                ? item.may / 24 / 31
                : item.may.toFixed(2) ?? '0.00',
              june: item.june
                ? item.june / 24 / 30
                : item.june.toFixed(2) ?? '0.00',
              july: item.july
                ? item.july / 24 / 31
                : item.july.toFixed(2) ?? '0.00',
              aug: item.aug
                ? item.aug / 24 / 31
                : item.aug.toFixed(2) ?? '0.00',
              sep: item.sep
                ? item.sep / 24 / 30
                : item.sep.toFixed(2) ?? '0.00',
              oct: item.oct
                ? item.oct / 24 / 31
                : item.oct.toFixed(2) ?? '0.00',
              nov: item.nov
                ? item.nov / 24 / 30
                : item.nov.toFixed(2) ?? '0.00',
              dec: item.dec
                ? item.dec / 24 / 31
                : item.dec.toFixed(2) ?? '0.00',
            }),
          }
          const total = [
            transformedItem.april,
            transformedItem.may,
            transformedItem.june,
            transformedItem.july,
            transformedItem.aug,
            transformedItem.sep,
            transformedItem.oct,
            transformedItem.nov,
            transformedItem.dec,
            transformedItem.jan,
            transformedItem.feb,
            transformedItem.march,
          ].reduce((sum, val) => sum + (parseFloat(val) || 0), 0)
          return {
            ...transformedItem,
            averageTPH: total,
          }
        })
      }

      const monthFields = [
        'april',
        'may',
        'june',
        'july',
        'aug',
        'sep',
        'oct',
        'nov',
        'dec',
        'jan',
        'feb',
        'march',
      ]
      const totalsRow = {
        id: formattedData.length,
        displayName: 'Total',
        isEditable: false,
        ...monthFields.reduce((acc, field) => {
          acc[field] = formattedData.reduce(
            (sum, row) => sum + (parseFloat(row[field]) || 0),
            0,
          )
          return acc
        }, {}),
      }
      totalsRow.averageTPH = monthFields.reduce(
        (sum, field) => sum + (parseFloat(totalsRow[field]) || 0),
        0,
      )
      let finalData = []

      if (formattedData.length > 0) {
        if (lowerVertName !== 'meg' && lowerVertName !== 'cracker') {
          finalData = [...formattedData, totalsRow]
        } else {
          finalData = [...formattedData]
        }
      } else {
        finalData = []
      }

      setRows(finalData)

      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchDataC2C3R = async () => {
    try {
      setLoading(true)

      let response

      if (IS_NMD) {
        response = await ProductionNormsApiService.monthlyProductionC2rC3R(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      } else {
        response = await ProductionNormsApiService.monthlyOtherProduction(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      }
      let dataSet

      if (IS_NMD) {
        dataSet = response
      } else {
        dataSet = response?.data?.configurationDTOList
        setCalculationObjectOtherProduction(response?.data?.aopCalculation)
      }

      var data = dataSet
        ?.map((product, index) => ({
          ...product,
          normParametersFKId: product.normParameterFKId,
          originalRemark: product.remarks,
          isEditable: product.isEditable,
          apr: product?.apr,
          may: product?.may,
          jun: product?.jun,
          jul: product?.jul,
          aug: product?.aug,
          sep: product?.sep,
          oct: product?.oct,
          nov: product?.nov,
          dec: product?.dec,
          jan: product?.jan,
          feb: product?.feb,
          mar: product?.mar,
          Particulars: product.productName,
          idFromApi: product.id,
          id: index,
        }))
        .map(({ normParameterFKId, ...rest }) => rest)

      // call calculate only if ALL records do not have id
      // const shouldCalculate =
      //   dataSet?.length > 0 &&
      //   dataSet.every((item) => !item.id && !item.idFromApi)

      // if (shouldCalculate) {
      //   handleCalculateOtherProduction()
      // }

      setRowsC2C3R(data)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
    fetchDataC2C3R()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak, selectedUnit])

  const productionColumns = getEnhancedColDefs({
    headerMap,
    valueFormat,
  })

  const productionColumnsC2C3R = getEnhancedColDefsC2C3R({
    headerMap,
    valueFormat,
  })

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }
  const isCellEditable = (params) => params.row.id !== 'total'

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
      showCalculate: false,
      isOldYear: isOldYear,
      showNote: true,
    }
  }

  const getAdjustedPermissionsC2C3R = (permissions, isOldYear) => {
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
      showCalculate: false,
      isOldYear: isOldYear,
      showNote: true,
    }
  }

  const adjustedPermissions = useMemo(
    () =>
      getAdjustedPermissions(
        {
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: true,
          saveWithRemark: true,
          showCalculate: true,
          allAction: true,
          showNote: true,
          showTitleNameBusiness: true,
          titleName: 'Month wise Production plan',
          showCalculateVisibility:
            calculationObject && Object.keys(calculationObject).length > 0,
          saveBtn: false,
          units: ['MT/Month', 'TPH'],
          customHeight: permissions?.customHeight,
          downloadExcelBtnFromUI: !permissions?.hideExportBtn,
          ExcelName: `${EXCEL_NAME}`,
          unitForExcelToadd: selectedUnit || 'MT/Month',
        },
        isOldYear,
      ),
    [permissions, calculationObject, lowerVertName, selectedUnit, isOldYear],
  )

  const adjustedPermissionsForC2C3R = useMemo(
    () =>
      getAdjustedPermissionsC2C3R(
        {
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: true,
          showCalculate: IS_NMD ? false : true,
          allAction: true,
          showNote: true,
          showTitleNameBusiness: false,
          titleName: '',
          saveBtn: true,
          downloadExcelBtnFromUI: true,
          ExcelName: `${EXCEL_NAME_OTHER_PRODUCTION}`,
        },
        isOldYear,
      ),
    [
      permissions,
      calculationObjectOtherProduction,
      lowerVertName,
      isOldYear,
      IS_NMD,
    ],
  )

  const handleRemarkCellClick = (dataItem) => {
    if (READ_ONLY) return
    setCurrentRemarkC2C3R(dataItem.remarks || '')
    setCurrentRowIdC2C3R(dataItem.id)
    setRemarkDialogOpenC2C3R(true)
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* SHOW THIS GRID TO ALL SITES */}
      <KendoDataTables
        modifiedCells={modifiedCellsC2C3R}
        setModifiedCells={setModifiedCellsC2C3R}
        columns={productionColumnsC2C3R}
        rows={rowsC2C3R}
        setRows={setRowsC2C3R}
        title={'Production AOP'}
        isCellEditable={isCellEditable}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        saveChanges={saveChangesC2C3R}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        apiRef={apiRefC2C3R}
        fetchData={fetchDataC2C3R}
        remarkDialogOpen={remarkDialogOpenC2C3R}
        setRemarkDialogOpen={setRemarkDialogOpenC2C3R}
        currentRemark={currentRemarkC2C3R}
        setCurrentRemark={setCurrentRemarkC2C3R}
        currentRowId={currentRowIdC2C3R}
        permissions={adjustedPermissionsForC2C3R}
        selectedUOM={'UOM'}
        note={''}
        handleRemarkCellClick={handleRemarkCellClick}
        handleCalculate={handleCalculateOtherProduction}
      />

      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        columns={productionColumns}
        rows={rows}
        setRows={setRows}
        title={'Production AOP'}
        isCellEditable={isCellEditable}
        onAddRow={(newRow) => console.log('New Row Added:', newRow)}
        onDeleteRow={(id) => console.log('Row Deleted:', id)}
        onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
        paginationOptions={[100, 200, 300]}
        updateProductNormData={updateProductNormData}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        handleCalculate={handleCalculate}
        apiRef={apiRef}
        fetchData={fetchData}
        handleUnitChange={handleUnitChange}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        permissions={adjustedPermissions}
        selectedUOM={'UOM'}
        note={''}
      />
    </div>
  )
}

export default ProductionNormsCracker
