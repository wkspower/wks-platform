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
import AopTabs from 'components/AopTabs'
import { Box } from '@mui/material'
import { DataService } from 'services/DataService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'

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

const ProductionNorms = ({ permissions }) => {
  // State for tabs
  const [tabIndex, setTabIndex] = useState(0)
  const [tabs, setTabs] = useState([])
  const [lineDetails, setLineDetails] = useState([])
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [calculationObject, setCalculationObject] = useState([])
  const keycloak = useSession()
  const [editResetKey, setEditResetKey] = useState(0)

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

  const PLANT_NAME_UPPERCASE = plantObject?.name
  const SITE_NAME_UPPERCASE = siteObject?.name
  const VERTICAL_NAME_UPPERCASE = verticalObject?.name

  const AOP_YEAR = year?.selectedYear

  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const SITE_NAME = siteObject?.name?.toLowerCase()
  const VERTICAL_NAME = verticalObject?.name?.toLowerCase()
  const IS_PP_DTA = false
  const IS_PP_SEZ = false
  const IS_PP_HMD = false

  // const IS_PP_DTA = lowerVertName === 'pp' && SITE_NAME === 'dta'
  // const IS_PP_SEZ = lowerVertName === 'pp' && SITE_NAME === 'sez'

  const plantName = plantObject?.name?.toLowerCase()
  const SITE_NAME_LOWERCASE = siteObject?.name?.toLowerCase()
  const IS_VCM = verticalObject?.name?.toLowerCase() == 'vcm'
  const IS_AROMATIC_SEZ =
    lowerVertName === 'aromatics' && SITE_NAME_LOWERCASE === 'sez'
  const IS_AROMATIC_SEZ_PX4 =
    lowerVertName === 'aromatics' &&
    SITE_NAME_LOWERCASE === 'sez' &&
    plantName === 'px4'
  const IS_PVC_VMD = lowerVertName === 'pvc' && SITE_NAME_LOWERCASE === 'vmd'
  const IS_PVC_DMD = lowerVertName === 'pvc' && SITE_NAME_LOWERCASE === 'dmd'
  const IS_PVC_HMD = lowerVertName === 'pvc' && SITE_NAME_LOWERCASE === 'hmd'
  const IS_AROMATIC_DTA_PLATFORMER =
    lowerVertName === 'aromatics' &&
    SITE_NAME_LOWERCASE === 'dta' &&
    (plantName === 'platformer' || plantName === 'plat')
  const IS_AROMATIC_HMD =
    lowerVertName === 'aromatics' && SITE_NAME_LOWERCASE === 'hmd'
  const IS_CHEMICAL = lowerVertName === 'chemical'
  const IS_ELASTOMER_JMD =
    lowerVertName === 'elastomer' && SITE_NAME_LOWERCASE === 'jmd'
  const IS_ELASTOMER_HMD =
    lowerVertName === 'elastomer' && SITE_NAME_LOWERCASE === 'hmd'

  const IS_ELASTOMER_JMD_IIR =
    lowerVertName === 'elastomer' &&
    SITE_NAME_LOWERCASE === 'jmd' &&
    plantName == 'iir'
  const IS_CHEMICAL_DMD_CHLOR_ALKALI =
    lowerVertName === 'chemical' &&
    SITE_NAME_LOWERCASE === 'dmd' &&
    plantName === 'chlor alkali'
  const IS_CHEMICAL_VMD_ACRYLONITRILE =
    lowerVertName === 'chemical' &&
    SITE_NAME_LOWERCASE === 'vmd' &&
    plantName === 'acrylonitrile'
  const IS_PTA_PMD_PIA =
    lowerVertName === 'pta' &&
    SITE_NAME_LOWERCASE === 'pmd' &&
    plantName === 'pia'
  const IS_CHEMICAL_NMD =
    lowerVertName === 'chemical' && SITE_NAME_LOWERCASE === 'nmd'

  const [loading, setLoading] = useState(false)
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
    duration: 3000,
  })

  const headerMap = generateHeaderNames(AOP_YEAR)

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('')
  const [selectedUnitIIR, setSelectedUnitIIR] = useState('')
  const [rows, setRows] = useState([])
  const [rowsByProducts, setRowsByProducts] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [rowsGradeWise, setRowsGradeWise] = useState([])
  const [rowsIIR, setRowsIIR] = useState([])
  const [rowsInKT, setRowsInKT] = useState([])
  const [rowsInMT, setRowsInMT] = useState([])
  const [aopCombinedRows, setAOPCombinedRows] = useState([])
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const validateTotalsWithIIRRef = React.useRef(true)
  const dispatch = useDispatch()
  const totalRowConfiguration = [
    { field: 'april', aggregate: 'sum' },
    { field: 'may', aggregate: 'sum' },
    { field: 'june', aggregate: 'sum' },
    { field: 'july', aggregate: 'sum' },
    { field: 'aug', aggregate: 'sum' },
    { field: 'sep', aggregate: 'sum' },
    { field: 'oct', aggregate: 'sum' },
    { field: 'nov', aggregate: 'sum' },
    { field: 'dec', aggregate: 'sum' },
    { field: 'jan', aggregate: 'sum' },
    { field: 'feb', aggregate: 'sum' },
    { field: 'march', aggregate: 'sum' },
    { field: 'averageTPH', aggregate: 'sum' },
  ]

  const validateTotalsWithIIR = ({
    data,
    rowsInKT,
    rowsInMT,
    selectedUnit, // 'KT' or 'MT'
  }) => {
    const iirRows = selectedUnit === 'KT' ? rowsInKT : rowsInMT

    const iirMap = new Map()
    iirRows.forEach((item) => {
      iirMap.set(item.product, Number(item.value))
    })

    const mismatches = []
    const matches = []

    const parseNumber = (val) => {
      if (val === null || val === undefined || val === '') return null
      const num = Number(val)
      return isNaN(num) ? null : num
    }

    const roundTo2 = (num) => {
      return Number(Number(num).toFixed(2))
    }

    data.forEach((row) => {
      const displayName = row.displayName

      const totalValue = parseNumber(row.total)
      const iirValue = iirMap.get(displayName)

      if (iirValue == null || totalValue == null) {
        mismatches.push({
          displayName,
          error:
            iirValue == null
              ? 'Not found in IIR data'
              : 'Invalid or missing total value',
        })
        return
      }

      const roundedTotal = roundTo2(totalValue)
      const roundedIIR = roundTo2(iirValue)

      const isMatch = roundedTotal === roundedIIR

      const result = {
        displayName,
        calculatedTotal: roundedTotal,
        iirValue: roundedIIR,
        match: isMatch,
        difference: roundedIIR - roundedTotal,
        unit: selectedUnit,
      }

      if (isMatch) {
        matches.push(result)
      } else {
        mismatches.push(result)
      }
    })

    return {
      allMatch: mismatches.length === 0,
      matches,
      mismatches,
    }
  }

  const saveChanges = React.useCallback(async () => {
    try {
      // Merge both rows and modifiedCells to capture all changes
      console.log('rows', rows)
      const editedData = Object.values(modifiedCells)
      const allData = rows
        .filter((item) => item.displayName !== 'Total')
        ?.map((row) => {
          const modifiedRow = modifiedCells[row.id]
          return modifiedRow ? { ...row, ...modifiedRow } : row
        })
      const enrichedData = allData.map((row) => ({
        ...row,
        total: row.total ?? findSum('1', row),
      }))
      if (
        !IS_CHEMICAL_VMD_ACRYLONITRILE &&
        !IS_CHEMICAL_NMD &&
        !IS_PTA_PMD_PIA
      ) {
        const result = validateTotalsWithIIR({
          data: enrichedData,
          rowsInKT,
          rowsInMT,
          selectedUnit,
        })

        if (!result.allMatch) {
          const message = result.mismatches
            .map((m) =>
              m.error
                ? `${m.displayName}: ${m.error}`
                : `${m.displayName}: Expected ${m.iirValue.toFixed(2)} ${m.unit}, got ${m.calculatedTotal.toFixed(2)} ${m.unit} (Diff : ${m.difference.toFixed(2)})`,
            )
            .join('\n')

          setSnackbarOpen(true)
          setSnackbarData({
            message: `Total validation failed:\n${message}`,
            severity: 'error',
            duration: 1000 * 30,
            autoHide: false,
          })
          setLoading(false)
          return
        }
      }
      const requiredFields = ['remark']

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
      updateProductNormData(editedData)
    } catch (error) {
      console.error('Error in saveChanges:', error)
    }
  }, [selectedUnit, calculatebtnClicked, modifiedCells])

  const updateProductNormData = async (newRow) => {
    setLoading(true)

    try {
      let plantId = PLANT_ID
      const isKiloTon = selectedUnit === 'KT'

      const productNormData = newRow.map((row) => ({
        aopType: row.aopType || 'production',
        aopCaseId: row.aopCaseId || 'production',
        aopStatus: row.aopStatus || 'production',
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
        remark: row.remark,
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
        if (IS_PVC_DMD) {
          fetchDataAOPCombined()
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
  const fetchDataAnnualproduction = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      var res = await ProductionNormsApiService.getIIRAnnualData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res?.code === 200) {
        const isKiloTon = selectedUnitIIR === 'KT'
        const mapped = res?.data.map((item, index) => ({
          id: item.id || null,
          product: item.product,
          value: isKiloTon && item.value ? item.value / 1000 : item.value, // ? convert here
          Particulars: item.type,
          isEditable: false,
        }))
        const rowsInKT = res?.data.map((item) => ({
          id: item.id || null,
          product: item.product,
          value: item.value ? Number(item.value) / 1000 : item.value, // convert to KT
          Particulars: item.type,
          isEditable: false,
        }))

        const rowsInMT = res?.data.map((item) => ({
          id: item.id || null,
          product: item.product,
          value: item.value ? item.value : item.value, // already MT (assuming)
          Particulars: item.type,
          isEditable: false,
        }))
        setRowsIIR(mapped)
        setRowsInKT(rowsInKT)
        setRowsInMT(rowsInMT)
      } else {
        setRowsIIR([])
      }
    } catch (err) {
      console.error('fetchData error', err)
      setRowsIIR([])
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = (dataItem) => {
    setCurrentRemark(dataItem.aopRemarks || dataItem.remark || '')
    setCurrentRowId(dataItem.id)
    setRemarkDialogOpen(true)
  }
  const convertValue = (value, days = 1) => {
    if (value === null || value === undefined || value === '') return null
    return Number(value) / 1000 / days
  }

  const fetchData = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setRows([])
      setLoading(true)
      const selectedLine = lineDetails[tabIndex]
      const lineId = selectedLine?.id
      let response = ''
      if (IS_PP_DTA || IS_PP_SEZ || IS_PVC_DMD || IS_PP_HMD || IS_PVC_HMD) {
        response = await ProductionNormsApiService.getAOPDataLineWise(
          keycloak,
          'Production',
          PLANT_ID,
          AOP_YEAR,
          lineId,
        )
      } else {
        response = await ProductionNormsApiService.getAOPData(
          keycloak,
          'Production',
          PLANT_ID,
          AOP_YEAR,
        )
      }
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
          originalRemark: product.remark,
          remark: product.remark,
          uomField:product.uom,
          isEditable:
            IS_ELASTOMER_JMD_IIR ||
            IS_CHEMICAL_VMD_ACRYLONITRILE ||
            IS_PTA_PMD_PIA ||
            IS_CHEMICAL_NMD
              ? true
              : false,
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

      const fiscalYear = AOP_YEAR
      const startYear = parseInt(fiscalYear.split('-')[0], 10)
      const nextYear = startYear + 1

      const isLeap = (year) => new Date(year, 1, 29).getDate() === 29

      const daysInMonth = {
        april: 30,
        may: 31,
        june: 30,
        july: 31,
        aug: 31,
        sep: 30,
        oct: 31,
        nov: 30,
        dec: 31,
        jan: 31,
        feb: isLeap(nextYear) ? 29 : 28,
        march: 31,
      }

      if (lowerVertName !== 'cracker') {
        formattedData = data.map((item, index) => {
          const isKiloTon = selectedUnit === 'KT'
          const isKiloTonPerDay = selectedUnit === 'KT/Day'

          const transformedItem = {
            ...item,
            idFromApi: item.id,
            uomField:item.uom,
            uom: selectedUnit ? selectedUnit : 'MT',
            normParametersFKId: item?.normParametersFKId?.toLowerCase(),
            id: index,
            ...monthFields.reduce((acc, month) => {
              const val = item[month]
              const isTrainRow = String(
                item.displayName ||
                  item.Particulars ||
                  item.normParameterDisplayName ||
                  '',
              )
                .toLowerCase()
                .includes('train')
              if (isKiloTon || isKiloTonPerDay) {
                acc[month] = convertValue(
                  val,
                  isKiloTonPerDay && IS_AROMATIC_SEZ_PX4 && !isTrainRow
                    ? daysInMonth[month]
                    : 1,
                )
              } else {
                acc[month] = val
              }
              return acc
            }, {}),
          }
          const total = monthFields.reduce(
            (sum, month) =>
              sum + (parseFloat(Number(transformedItem[month])) || 0),
            0,
          )
          return {
            ...transformedItem,
            averageTPH: total,
            _displayNameLower: String(
              transformedItem.displayName || '',
            ).toLowerCase(),
          }
        })
      }

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
        const trainIndex = formattedData.findIndex((item) => {
          const isTrainRow = String(
            item.displayName ||
              item.Particulars ||
              item.normParameterDisplayName ||
              '',
          )
            .toLowerCase()
            .includes('train')
          return isTrainRow
        })
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
          (lowerVertName !== 'meg' &&
            lowerVertName !== 'cracker' &&
            lowerVertName !== 'elastomer' &&
            lowerVertName !== 'vcm' &&
            lowerVertName !== 'pta' &&
            lowerVertName !== 'chemical' &&
            !IS_AROMATIC_SEZ &&
            !IS_AROMATIC_HMD &&
            !IS_AROMATIC_DTA_PLATFORMER) ||
          IS_ELASTOMER_JMD ||
          IS_CHEMICAL_DMD_CHLOR_ALKALI ||
          IS_ELASTOMER_HMD
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
  const fetchDataAOPCombined = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setAOPCombinedRows([])
      setLoading(true)
      const response = await ProductionNormsApiService.getAOPCombinedData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (response?.code != 200) {
        setAOPCombinedRows([])
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
        ?.map((product) => {
          const transformedItem = {
            ...product,
            normParametersFKId: product.materialFKId,
            originalRemark: product.remark,
            remark: product.remark,
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
          }
          const total = monthFields.reduce(
            (sum, month) =>
              sum + (parseFloat(Number(transformedItem[month])) || 0),
            0,
          )
          return {
            ...transformedItem,
            averageTPH: total,
            _displayNameLower: String(
              transformedItem.Particulars || '',
            ).toLowerCase(),
          }
        })
        .map(({ materialFKId, ...rest }) => rest)
      const totalsRow = {
        id: data?.length,
        displayName: 'Total',
        isEditable: false,
        ...monthFields.reduce((acc, field) => {
          acc[field] = data?.reduce(
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
      setAOPCombinedRows([...data, totalsRow])
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
          originalRemark: product.remark,
          isEditable: false,
          Particulars: product.normParameterDisplayName,
          remark: product.remark,

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

  const initialRender = React.useRef(true)

  useEffect(() => {
    const fetchDataWrapper = async () => {
      // Only fetch data if this is not the initial render or if dependencies have changed
      if (!initialRender.current || PLANT_ID) {
        await fetchData()
        if (lowerVertName === 'meg') {
          await fetchDataByProducts()
        }
      } else {
        initialRender.current = false
      }
    }
    if (
      (IS_PP_DTA || IS_PP_SEZ || IS_PVC_DMD || IS_PP_HMD || IS_PVC_HMD) &&
      lineDetails?.length === 0
    ) {
      return
    }
    fetchDataWrapper()
  }, [PLANT_ID, yearChanged, keycloak, selectedUnit, tabIndex, lineDetails])

  // Fetch line details when component mounts or plantID/year changes
  const fetchLineDetails = async () => {
    if (!PLANT_ID || !AOP_YEAR) return

    try {
      const response = await DataService.getLineDetails(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code != 200) {
        setTabs([])
        return
      }
      if (response && Array.isArray(response?.data)) {
        setLineDetails(response.data)
        // Update tabs based on the response
        const lineTabs = response?.data.map((line) => line.displayName)
        setTabs(lineTabs)
      }
    } catch (err) {
      console.error('Error fetching line details:', err)
      // Fallback to default tabs if API fails
      setTabs([])
    }
  }

  useEffect(() => {
    if (IS_PP_DTA || IS_PP_SEZ || IS_PVC_DMD || IS_PP_HMD || IS_PVC_HMD) {
      fetchLineDetails()
    } else {
      setLineDetails([])
    }
  }, [PLANT_ID, keycloak, yearChanged])

  useEffect(() => {
    if (IS_PVC_DMD) {
      fetchDataAOPCombined()
    }
  }, [PLANT_ID, keycloak, yearChanged])

  const valueFormat_ = ValueFormatterProduction()
  const valueFormat = IS_VCM
    ? '{0:0.000}'
    : IS_AROMATIC_SEZ_PX4
      ? '{0:0.0000}'
      : valueFormat_

  const productionColumns = getEnhancedColDefs({
    headerMap,
    valueFormat,
  })
  const columnIIR = [
    {
      field: 'id',
      title: 'ID',
      editable: false,
      hidden: true,
      isVisible: false,
      minWidth: 100,
    },

    {
      field: 'product',
      title: 'Product',
      widthT: 200,
      editable: false,
      autoAdjust: false,
      minWidth: 100,
    },
    {
      field: 'value',
      title: 'Values',
      editable: false,
      type: 'number',
      widthT: 100,
      format: valueFormat,
      autoAdjust: false,
      minWidth: 100,
    },
    {
      field: 'type',
      title: 'type',
      editable: false,
      hidden: true,
      isVisible: false,
      minWidth: 100,
    },
  ]

  const productionColumnsByProducts = getEnhancedColDefsByProducts({
    headerMap,
    valueFormat,
  })

  const handleUnitChange = (unit) => {
    setSelectedUnit(unit)
  }
  const handleUnitChangeIIR = (unit) => {
    setSelectedUnitIIR(unit)
  }
  const isCellEditable = (params) => params.row.id !== 'total'

  useEffect(() => {
    if (
      validateTotalsWithIIRRef.current &&
      (IS_ELASTOMER_JMD_IIR ||
        IS_CHEMICAL_VMD_ACRYLONITRILE ||
        IS_CHEMICAL_NMD) &&
      rows.length > 0 &&
      (rowsInKT.length > 0 || rowsInMT.length > 0)
    ) {
      const enrichedData = rows
        .filter((row) => row.displayName !== 'Total' && row.id !== 'total')
        .map((row) => ({
          ...row,
          total: row.total ?? findSum('1', row),
        }))

      const result = validateTotalsWithIIR({
        data: enrichedData,
        rowsInKT,
        rowsInMT,
        selectedUnit,
      })

      if (!result.allMatch) {
        const message = result.mismatches
          .map((m) =>
            m.error
              ? `${m.displayName}: ${m.error}`
              : `${m.displayName}: Expected ${m.iirValue.toFixed(2)} ${m.unit || 'MT'}, got ${m.calculatedTotal.toFixed(2)} ${m.unit || 'MT'} (Diff: ${m.difference.toFixed(2)})`,
          )
          .join('\n')

        setSnackbarOpen(true)
        setSnackbarData({
          message: `Total validation failed:\n${message}`,
          severity: 'error',
          duration: 1000 * 15,
          autoHide: false,
        })
      }
      validateTotalsWithIIRRef.current = false
    }
  }, [
    rows,
    rowsInKT,
    rowsInMT,
    selectedUnit,
    IS_ELASTOMER_JMD_IIR,
    IS_CHEMICAL_VMD_ACRYLONITRILE,
    IS_CHEMICAL_NMD,
  ])

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchDataAnnualproduction()
    }
  }, [PLANT_ID, AOP_YEAR, selectedUnitIIR])

  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response
      if (lowerVertName === 'pta') {
        response = await ProductionNormsApiService.MonthwiseProductionExport(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          'Production',
          `${VERTICAL_NAME_UPPERCASE}_${SITE_NAME_UPPERCASE}_${PLANT_NAME_UPPERCASE}_${AOP_YEAR}_Month wise Production`,
        )
      } else if (lowerVertName === 'meg') {
        response =
          await ProductionNormsApiService.MonthwiseProductionExportCombined(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
            `${VERTICAL_NAME_UPPERCASE}_${SITE_NAME_UPPERCASE}_${PLANT_NAME_UPPERCASE}_${AOP_YEAR}_Month wise Production`,
          )
      } else if (IS_PVC_DMD) {
        response =
          await ProductionNormsApiService.MonthwiseProductionExportCombinedLineWise(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
            'Production',
            `${VERTICAL_NAME_UPPERCASE}_${SITE_NAME_UPPERCASE}_${PLANT_NAME_UPPERCASE}_${AOP_YEAR}_Month wise Production`,
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
          showUnit: IS_AROMATIC_DTA_PLATFORMER
            ? false
            : lowerVertName === 'vcm' ||
                lowerVertName === 'pta' ||
                lowerVertName === 'cracker' ||
                lowerVertName === 'chemical'
              ? true
              : permissions?.showUnit ?? true,
          saveWithRemark: permissions?.saveWithRemark ?? true,

          showCalculate: IS_ELASTOMER_JMD
            ? false
            : permissions?.showCalculate ?? true,

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
          saveBtn:
            IS_ELASTOMER_JMD_IIR ||
            IS_CHEMICAL_VMD_ACRYLONITRILE ||
            IS_CHEMICAL_NMD ||
            IS_PTA_PMD_PIA
              ? true
              : permissions?.saveBtn ?? false,
          units:
            lowerVertName === 'cracker'
              ? ['MT/Month', 'TPH']
              : IS_AROMATIC_SEZ
                ? ['MT', 'KT', 'KT/Day']
                : ['MT', 'KT'],
          customHeight: permissions?.customHeight,
          downloadExcelBtnFromUI:
            lowerVertName === 'vcm' ||
            lowerVertName === 'pta' ||
            lowerVertName === 'cracker' ||
            lowerVertName === 'chemical'
              ? true
              : lowerVertName === 'meg' || IS_PVC_DMD
                ? false
                : !permissions?.hideExportBtn,
          downloadExcelBtn:
            lowerVertName === 'meg' || IS_PVC_DMD ? true : false,

          ExcelName: `${VERTICAL_NAME_UPPERCASE}_${SITE_NAME_UPPERCASE}_${PLANT_NAME_UPPERCASE}_Month wise Production plan`,
          unitForExcelToadd:
            lowerVertName === 'cracker'
              ? selectedUnit || 'MT/Month'
              : lowerVertName === 'vcm' ||
                  lowerVertName === 'pta' ||
                  lowerVertName === 'chemical'
                ? selectedUnit || 'MT'
                : null,
        },
        isOldYear,
      ),
    [
      permissions,
      calculationObject,
      lowerVertName,
      selectedUnit,
      isOldYear,
      IS_ELASTOMER_JMD,
    ],
  )
  const adjustedPermissionsPVCDMD = useMemo(
    () =>
      getAdjustedPermissions(
        {
          showAction: permissions?.showAction ?? false,
          addButton: permissions?.addButton ?? false,
          deleteButton: permissions?.deleteButton ?? false,
          editButton: permissions?.editButton ?? false,
          showUnit: false,
          saveWithRemark: permissions?.saveWithRemark ?? false,

          showCalculate: permissions?.showCalculate ?? false,

          allAction: permissions?.allAction ?? true,
          showNote: false,

          showTitleNameBusiness: true,
          titleName: permissions?.title
            ? permissions?.title
            : 'Combined Month-Wise Production Plan',

          showCalculateVisibility: false,
          saveBtn: false,
          customHeight: permissions?.customHeight,
          downloadExcelBtnFromUI: false,
          ExcelName: `${VERTICAL_NAME_UPPERCASE}_${SITE_NAME_UPPERCASE}_${PLANT_NAME_UPPERCASE}_Combined Month-Wise Production Plan`,
        },
        isOldYear,
      ),
    [
      permissions,
      calculationObject,
      lowerVertName,
      selectedUnit,
      isOldYear,
      IS_ELASTOMER_JMD,
    ],
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
        lowerVertName === 'vcm' || lowerVertName === 'meg'
          ? false
          : !permissions?.hideExportBtn,
      ExcelName: `${VERTICAL_NAME_UPPERCASE}_${SITE_NAME_UPPERCASE}_${PLANT_NAME_UPPERCASE}_${AOP_YEAR}_Month wise Production plan (By Products)`,

      customHeight: permissions?.customHeight,
    },
    isOldYear,
  )

  const EXCEL_NAME_JMD_GRID = `${VERTICAL_NAME_UPPERCASE}_${SITE_NAME_UPPERCASE}_${PLANT_NAME_UPPERCASE}_${AOP_YEAR}_IIR Anual Production `

  const adjustedPermissionsIIR = useMemo(
    () =>
      getAdjustedPermissions(
        {
          allAction: true,
          saveBtn: false,
          showTitleNameBusiness: true,
          titleName: `${PLANT_NAME_UPPERCASE} Annual production`,
          ExcelName: EXCEL_NAME_JMD_GRID,
          downloadExcelBtnFromUI: true,
          showUnit: true,
          units: ['MT', 'KT'],
        },
        isOldYear,
      ),
    [lowerVertName, AOP_YEAR, isOldYear, PLANT_NAME_UPPERCASE],
  )
  if (lowerVertName === 'cracker' && !permissions?.hideByProducts) {
    return <ProductionNormsCracker />
  }

  return (
    <div>
      {/* LINE1-LINE6 Tabs - Only for PP VERTICAL | DTA SITE */}
      {(IS_PP_DTA || IS_PP_SEZ || IS_PVC_DMD || IS_PP_HMD || IS_PVC_HMD) && (
        <Box display='flex' alignItems='center' sx={{ mb: 1, mt: 1 }}>
          <AopTabs tabIndex={tabIndex} setTabIndex={setTabIndex} tabs={tabs} />
        </Box>
      )}
      <LoaderBackdrop open={!!loading} />
      {IS_ELASTOMER_JMD && (
        <KendoDataTables
          columns={columnIIR}
          rows={rowsIIR}
          setRows={setRowsIIR}
          fetchData={fetchDataAnnualproduction}
          title='IIR Annual production'
          handleUnitChange={handleUnitChangeIIR}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={setCurrentRowId}
          //saveChanges={saveChanges}
          // deleteRowData={deleteRowData}
          permissions={adjustedPermissionsIIR}
          resetEditSignal={editResetKey}
          setEditResetKey={setEditResetKey}
          groupBy='Particulars'
        />
      )}
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
        handleRemarkCellClick={handleRemarkCellClick}
        unsavedChangesRef={unsavedChangesRef}
        permissions={adjustedPermissions}
        selectedUOM={'UOM'}
        resetEditSignal={editResetKey}
        setEditResetKey={setEditResetKey}
        totalRowConfiguration={totalRowConfiguration}
        // groupBy={IS_ELASTOMER_JMD ? 'Particulars' : null}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
        note={
          !permissions?.hideNoteText &&
          lowerVertName !== 'cracker' &&
          lowerVertName !== 'elastomer' &&
          lowerVertName !== 'aromatics' &&
          lowerVertName !== 'vcm' &&
          lowerVertName !== 'pe' &&
          lowerVertName !== 'pp' &&
          lowerVertName !== 'pta' &&
          lowerVertName !== 'chemical' &&
          lowerVertName !== 'pet' &&
          !IS_PVC_VMD
            ? '* MT per Annum'
            : ''
        }
      />

      {IS_PVC_DMD && (
        <KendoDataTables
          columns={productionColumns}
          rows={aopCombinedRows}
          setRows={setAOPCombinedRows}
          title={'Combined Month-Wise Production Plan'}
          paginationOptions={[100, 200, 300]}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          apiRef={apiRef}
          fetchData={fetchDataAOPCombined}
          permissions={adjustedPermissionsPVCDMD}
          resetEditSignal={editResetKey}
          setEditResetKey={setEditResetKey}
        />
      )}

      {lowerVertName === 'meg' && !permissions?.hideNoteText && (
        <KendoDataTables
          columns={productionColumnsByProducts}
          rows={rowsByProducts}
          setRows={setRowsByProducts}
          title={'By Products'}
          fetchData={fetchDataByProducts}
          permissions={adjustedPermissionsByProducts}
          resetEditSignal={editResetKey}
          setEditResetKey={setEditResetKey}
        />
      )}
    </div>
  )
}

export default ProductionNorms
