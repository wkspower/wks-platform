import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedColDefsByProducts from 'components/data-tables/CommonHeader/Kendo_ProductionAopHeaderByProducts'
import React, { useEffect, useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'

import { ProductionNormsApiService } from 'services/production-norms-api-service'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import { validateFields } from 'utils/validationUtils'
import getEnhancedColDefs from '../data-tables/CommonHeader/Kendo_ProductionAopHeader'
import KendoDataTables from './index'
import ProductionNormsCracker from './ProductionNormsCracker'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
const ProductionNorms = ({ permissions }) => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [calculationObject, setCalculationObject] = useState([])
  const keycloak = useSession()

  const apiRef = useGridApiRef()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const [_plantID, set_PlantID] = useState('')

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
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
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
  const [rowsByProducts, setRowsByProducts] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
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
          //Consition changed if permissions?.saveBtn --> SET TO FALSE
          //UNCOMMENT THE CODE IF permissions?.saveBtn --> SET TO TRUE
          // if (editedData.length === 0) {
          //   setSnackbarOpen(true)
          //   setSnackbarData({
          //     message: 'No Records to Save!',
          //     severity: 'info',
          //   })
          //   setCalculatebtnClicked(false)
          //   return
          // }
          // updateProductNormData(editedData)
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

  const updateProductNormData = async (newRow) => {
    setLoading(true)

    try {
      let plantId = PLANT_ID
      const isKiloTon = selectedUnit != ('MT' || 'MT/Month')

      const productNormData = newRow.map((row) => ({
        aopType: row.aopType || 'production',
        aopCaseId: row.aopCaseId || null,
        aopStatus: row.aopStatus || null,
        aopYear: AOP_YEAR,
        plantFKId: plantId,
        materialFKId: row.normParametersFKId,
        siteFKId: SITE_ID,
        verticalFKId: VERTICAL_ID,
        april:
          row.april === 0
            ? 0
            : isKiloTon && row.april
              ? row.april * 1000
              : row.april || null,
        may:
          row.may === 0
            ? 0
            : isKiloTon && row.may
              ? row.may * 1000
              : row.may || null,
        june:
          row.june === 0
            ? 0
            : isKiloTon && row.june
              ? row.june * 1000
              : row.june || null,
        july:
          row.july === 0
            ? 0
            : isKiloTon && row.july
              ? row.july * 1000
              : row.july || null,
        aug:
          row.aug === 0
            ? 0
            : isKiloTon && row.aug
              ? row.aug * 1000
              : row.aug || null,
        sep:
          row.sep === 0
            ? 0
            : isKiloTon && row.sep
              ? row.sep * 1000
              : row.sep || null,
        oct:
          row.oct === 0
            ? 0
            : isKiloTon && row.oct
              ? row.oct * 1000
              : row.oct || null,
        nov:
          row.nov === 0
            ? 0
            : isKiloTon && row.nov
              ? row.nov * 1000
              : row.nov || null,
        dec:
          row.dec === 0
            ? 0
            : isKiloTon && row.dec
              ? row.dec * 1000
              : row.dec || null,
        jan:
          row.jan === 0
            ? 0
            : isKiloTon && row.jan
              ? row.jan * 1000
              : row.jan || null,
        feb:
          row.feb === 0
            ? 0
            : isKiloTon && row.feb
              ? row.feb * 1000
              : row.feb || null,
        march:
          row.march === 0
            ? 0
            : isKiloTon && row.march
              ? row.march * 1000
              : row.march || null,

        // avgTPH: findAvg('1', row) || null,
        avgTPH: findSum('1', row) || null,
        aopRemarks: row.aopRemarks,
        id: row.idFromApi || null,
      }))

      const response = await ProductionNormsApiService.updateProductNormData(
        productNormData,
        keycloak,
      )

      if (response) {
        dispatch(setIsBlocked(false))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully !',
          severity: 'success',
        })

        setCalculatebtnClicked(false)
        setLoading(false)
        setModifiedCells({})

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }

        setCalculatebtnClicked(false)
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
        setLoading(false)

        setCalculatebtnClicked(false)
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
      displayName: 'Train',
      april: 1,
      may: 1,
      june: 2,
      july: 1,
      aug: 2,
      sep: 1,
      oct: 2,
      nov: 1,
      dec: 2,
      jan: 1,
      feb: 2,
      march: 1,
      averageTPH: '',
      isEditable: false,
      aopStatus: '',
    },
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
      setRows([])
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
      // if (lowerVertName === 'cracker') {
      //   dataSet = rowDataForCracker
      // }

      var data = dataSet
        ?.map((product) => ({
          ...product,
          normParametersFKId: product.materialFKId,
          originalRemark: product.aopRemarks,
          isEditable: false,
          april: product?.april,
          may: product?.may,
          june: product?.june,
          july: product?.july,
          aug: product?.aug,
          sep: product?.sep,
          oct: product?.oct,
          nov: product?.nov,
          dec: product?.dec,
          jan: product?.jan,
          feb: product?.feb,
          march: product?.march,
          Particulars: product.normParameterDisplayName,
          ...(product.materialFKId !== undefined
            ? { materialFKId: undefined }
            : {}),
        }))
        .map(({ materialFKId, ...rest }) => rest)

      let formattedData = []

      if (lowerVertName !== 'cracker') {
        formattedData = data.map((item, index) => {
          const isKiloTon = selectedUnit == 'KT'
          const transformedItem = {
            ...item,
            idFromApi: item.id,
            uom: selectedUnit ? selectedUnit : 'MT',
            normParametersFKId: item?.normParametersFKId?.toLowerCase(),
            id: index,
            ...(isKiloTon && {
              jan: item.jan ? item.jan / 1000 : item.jan,
              feb: item.feb ? item.feb / 1000 : item.feb,
              march: item.march ? item.march / 1000 : item.march,
              april: item.april ? item.april / 1000 : item.april,
              may: item.may ? item.may / 1000 : item.may,
              june: item.june ? item.june / 1000 : item.june,
              july: item.july ? item.july / 1000 : item.july,
              aug: item.aug ? item.aug / 1000 : item.aug,
              sep: item.sep ? item.sep / 1000 : item.sep,
              oct: item.oct ? item.oct / 1000 : item.oct,
              nov: item.nov ? item.nov / 1000 : item.nov,
              dec: item.dec ? item.dec / 1000 : item.dec,
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
            _displayNameLower: String(
              transformedItem.displayName || '',
            ).toLowerCase(),
          }
        })
      }

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
              jan: item.jan ? item.jan / 24 / 31 : item.jan,
              feb: item.feb
                ? item.feb / 24 / (isLeap(nextYear) ? 29 : 28)
                : item.feb,
              march: item.march ? item.march / 24 / 31 : item.march,
              april: item.april ? item.april / 24 / 30 : item.april,
              may: item.may ? item.may / 24 / 31 : item.may,
              june: item.june ? item.june / 24 / 30 : item.june,
              july: item.july ? item.july / 24 / 31 : item.july,
              aug: item.aug ? item.aug / 24 / 31 : item.aug,
              sep: item.sep ? item.sep / 24 / 30 : item.sep,
              oct: item.oct ? item.oct / 24 / 31 : item.oct,
              nov: item.nov ? item.nov / 24 / 30 : item.nov,
              dec: item.dec ? item.dec / 24 / 31 : item.dec,
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
            _displayNameLower: String(
              transformedItem.displayName || '',
            ).toLowerCase(),
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

      const mapTrainNumberToLabel = (val) => {
        const TOL = 0.0001
        if (val === null || val === undefined || val === '') return val

        const parsed = parseFloat(String(val).trim())
        if (Number.isNaN(parsed)) return val

        const candidates = [parsed]
        if (Math.abs(parsed) < 0.1) candidates.push(parsed * 1000)

        for (const num of candidates) {
          const rounded = Math.round(num)
          if (Math.abs(num - rounded) <= TOL) {
            if (rounded === 1) return 'Single'
            if (rounded === 2) return 'Two'
            if (rounded === 3) return 'Three'
          }
        }

        return val
      }

      if (
        lowerVertName === 'aromatics' &&
        Array.isArray(formattedData) &&
        formattedData.length
      ) {
        const trainIndex = formattedData.findIndex(
          (r) =>
            String(r._displayNameLower || r.displayName || '').toLowerCase() ===
            'train',
        )
        if (trainIndex !== -1) {
          monthFields.forEach((m) => {
            const original = formattedData[trainIndex][m]
            if (
              original !== undefined &&
              original !== null &&
              original !== ''
            ) {
              formattedData[trainIndex][m] = mapTrainNumberToLabel(original)
            }
          })
          formattedData[trainIndex].averageTPH =
            formattedData[trainIndex].averageTPH || ''
        }
      }

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

      const trainRow = formattedData.find(
        (r) =>
          String(r._displayNameLower || r.displayName || '').toLowerCase() ===
          'train',
      )

      if (lowerVertName === 'aromatics' && trainRow) {
        trainRow.averageTPH = '-'
      }

      let finalData = []

      if (formattedData.length > 0) {
        if (
          lowerVertName !== 'meg' &&
          lowerVertName !== 'cracker' &&
          lowerVertName !== 'elastomer' &&
          lowerVertName !== 'vcm'
        ) {
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

  const fetchDataByProducts = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setRowsByProducts([])
      setLoading(true)

      const response = await ProductionNormsApiService.getAOPData(
        keycloak,
        'ByProducts',
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code != 200) {
        setRowsByProducts([])
        setLoading(false)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error fetching data. Please try again.',
          severity: 'error',
        })
        return
      }

      var data = response?.data?.aopDTOList
        ?.map((product) => ({
          ...product,
          normParametersFKId: product.materialFKId,
          originalRemark: product.aopRemarks,
          isEditable: false,
          Particulars: product.normParameterDisplayName,

          ...(product.materialFKId !== undefined
            ? { materialFKId: undefined }
            : {}),
        }))
        .map(({ materialFKId, ...rest }) => rest)

      const formattedData = data.map((item, index) => {
        const transformedItem = {
          ...item,
          idFromApi: item.id,
          normParametersFKId: item?.normParametersFKId?.toLowerCase(),
          id: index,
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

      const finalData = [...formattedData]

      if (lowerVertName == 'meg') {
        setRowsByProducts(finalData)
      }

      setLoading(false)
    } catch (error) {
      console.error('Error fetching Production AOP data:', error)
    } finally {
      setLoading(false)
    }
  }

  const findSum = (value, row) => {
    const months = [
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

    const values = months.map((month) => Number(row[month]) || 0)
    const sum = values.reduce((acc, val) => acc + val, 0)

    const total = sum.toFixed(2)
    return total === '0.00' ? null : total
  }

  useEffect(() => {
    fetchData()
    if (lowerVertName === 'meg') {
      fetchDataByProducts()
    }
  }, [PLANT_ID, oldYear, yearChanged, keycloak, selectedUnit])

  const valueFormat = ValueFormatterProduction()

  const productionColumns = getEnhancedColDefs({
    headerMap,
    valueFormat,
  })

  const productionColumnsByProducts = getEnhancedColDefsByProducts({
    headerMap,
    valueFormat,
  })

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }
  const isCellEditable = (params) => params.row.id !== 'total'

  // const downloadExcelForConfiguration = async () => {
  //     setSnackbarOpen(true)
  //     setSnackbarData({
  //       message: 'Excel download started!',
  //       severity: 'success',
  //     })

  //     try {
  //       let response
  //       if ( lowerVertName === 'pta') {
  //         response = await ProductionNormsApiService.MonthwiseProductionExport(
  //           keycloak,
  //           PLANT_ID,
  //           AOP_YEAR,
  //           'Production',
  //         )
  //       }
  //     } catch (error) {
  //       console.error('Error downloading Excel:', error)
  //       setSnackbarData({
  //         message: 'Failed to download Excel.',
  //         severity: 'error',
  //       })
  //     } finally {
  //       setSnackbarOpen(true)
  //     }
  //   }

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
      downloadExcelBtn: false,
    }
  }

  const adjustedPermissions = useMemo(
    () =>
      getAdjustedPermissions(
        {
          showAction: permissions?.showAction ?? false,
          addButton: permissions?.addButton ?? false,
          deleteButton: permissions?.deleteButton ?? false,
          editButton: permissions?.editButton ?? false,
          showUnit:
            lowerVertName === 'vcm' ||
            lowerVertName === 'pta' ||
            lowerVertName === 'cracker'
              ? true
              : permissions?.showUnit ?? true,
          saveWithRemark: permissions?.saveWithRemark ?? true,
          showCalculate: permissions?.showCalculate ?? true,
          allAction: permissions?.allAction ?? true,
          showNote: true,

          showTitleNameBusiness: true,
          titleName: permissions?.title
            ? permissions?.title
            : 'Month wise Production plan',

          showCalculateVisibility:
            calculationObject && Object.keys(calculationObject).length > 0
              ? permissions?.showCalculate ?? true
              : false,
          saveBtn: permissions?.saveBtn ?? false,
          units:
            lowerVertName === 'cracker' ? ['MT/Month', 'TPH'] : ['MT', 'KT'],
          customHeight: permissions?.customHeight,
          downloadExcelBtnFromUI:
            lowerVertName === 'vcm' ||
            lowerVertName === 'pta' ||
            lowerVertName === 'cracker'
              ? true
              : !permissions?.hideExportBtn,
          // downloadExcelBtn: lowerVertName === 'pta'
          // ? true
          // : false,
          ExcelName: `${lowerVertName}_Month wise Production plan`,
          unitForExcelToadd:
            lowerVertName === 'cracker'
              ? selectedUnit || 'MT/Month'
              : lowerVertName === 'vcm' || lowerVertName === 'pta'
                ? selectedUnit || 'MT'
                : null,
        },
        isOldYear,
      ),
    [permissions, calculationObject, lowerVertName, selectedUnit, isOldYear],
  )

  const adjustedPermissionsByProducts = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? false,
      addButton: permissions?.addButton ?? false,
      deleteButton: permissions?.deleteButton ?? false,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? false,
      showCalculate: permissions?.showCalculate ?? false,
      allAction: permissions?.allAction ?? true,
      showCalculateVisibility:
        calculationObject && Object.keys(calculationObject).length > 0
          ? permissions?.showCalculate ?? true
          : false,
      saveBtn: permissions?.saveBtn ?? false,
      units: lowerVertName == 'cracker' ? ['MT/Month', 'TPH'] : ['MT', 'KT'],
      downloadExcelBtnFromUI:
        lowerVertName === 'vcm' ? false : !permissions?.hideExportBtn,
      ExcelName: `${lowerVertName}_Production Target`,
      customHeight: permissions?.customHeight,
    },
    isOldYear,
  )

  if (lowerVertName === 'cracker' && !permissions?.hideByProducts) {
    return <ProductionNormsCracker />
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
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        unsavedChangesRef={unsavedChangesRef}
        permissions={adjustedPermissions}
        selectedUOM={'UOM'}
        // downloadExcelForConfiguration={downloadExcelForConfiguration}
        note={
          !permissions?.hideNoteText &&
          lowerVertName !== 'cracker' &&
          lowerVertName !== 'elastomer' &&
          lowerVertName !== 'aromatics' &&
          lowerVertName !== 'vcm' &&
          lowerVertName !== 'pe' &&
          lowerVertName !== 'pp' &&
          lowerVertName !== 'pta'
            ? '* MT per Annum'
            : ''
        }
      />

      {lowerVertName === 'meg' && !permissions?.hideNoteText && (
        <KendoDataTables
          columns={productionColumnsByProducts}
          rows={rowsByProducts}
          setRows={setRowsByProducts}
          title={'By Products'}
          fetchData={fetchDataByProducts}
          permissions={adjustedPermissionsByProducts}
        />
      )}
    </div>
  )
}

export default ProductionNorms
