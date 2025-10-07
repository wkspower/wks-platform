import React, { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Box,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Tab,
  Tabs,
  Typography,
} from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import getNormalOpNormColDef from 'components/data-tables/CommonHeader/getNormalOpNormColDef'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useDispatch, useSelector } from 'react-redux'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { useSession } from 'SessionStoreContext'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import CrakcerConstants from './CrackerConstants'
import KendoDataTables from './index'
import SelectivityData from './SelectivityData'
import { DataService } from 'services/DataService'

// Constants
const MONTHS = [
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

const mapApiRowToGrid = (list = [], prefix = '') =>
  (list || []).map((item, index) => ({
    ...item,
    idFromApi: item.id,
    id: `${prefix}${index}`,
    originalRemark: item.remark || '',
    remark: item.remark || '',
    Particulars: item.normType || item.normParameterTypeDisplayName,
  }))

const mapGridRowToPayload = (rows = []) =>
  (rows || []).map((row) => {
    const payload = {}
    MONTHS.forEach((m) => {
      payload[m] = row[m] || 0
    })
    payload.remark = row.remark || row.remarks || ''
    payload.isChecked = !!row.isChecked
    payload.id = row.idFromApi || null
    payload.materialFKId = row.materialFKId || row.materialFkId || null
    return payload
  })

const NormalOpNormsScreenCracker = () => {
  // state
  const [modifiedCells, setModifiedCells] = useState({})
  const [modifiedCellsFinalNorms, setModifiedCellsFinalNorms] = useState({})
  const [allRedCell, setAllRedCell] = useState([])

  const dataGridStore = useSelector((s) => s.dataGridStore) || {}
  const { verticalChange, yearChanged, oldYear, plantObject, year } =
    dataGridStore || {}

  const isOldYear = oldYear?.oldYear
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const vertName = verticalChange?.selectedVertical || ''
  const lowerVertName = (vertName || '').toLowerCase()

  const dispatch = useDispatch()
  const keycloak = useSession()
  const headerMap = generateHeaderNames(AOP_YEAR)

  const [loading, setLoading] = useState(false)
  const [grades, setGrades] = useState([])
  const [rows, setRows] = useState([])
  const [rowsExpression, setRowsExpression] = useState([])
  const [rowsBestAchivedIndividual, setRowsBestAchivedIndividual] = useState([])
  const [rowsBestFinalNorms, setRowsBestFinalNorms] = useState([])

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [remarkDialogOpen1, setRemarkDialogOpen1] = useState(false)
  const [remarkDialogOpen2, setRemarkDialogOpen2] = useState(false)
  const [remarkDialogOpen3, setRemarkDialogOpen3] = useState(false)
  const [remarkDialogOpen4, setRemarkDialogOpen4] = useState(false)
  const [remarkDialogOpenFinalNorms, setRemarkDialogOpenFinalNorms] =
    useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRemark1, setCurrentRemark1] = useState('')
  const [currentRemark2, setCurrentRemark2] = useState('')
  const [currentRemark3, setCurrentRemark3] = useState('')
  const [currentRemark4, setCurrentRemark4] = useState('')

  const [currentRemarkFinalNorms, setCurrentRemarkFinalNorms] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [currentRowId1, setCurrentRowId1] = useState(null)
  const [currentRowId2, setCurrentRowId2] = useState(null)
  const [currentRowId3, setCurrentRowId3] = useState(null)
  const [currentRowId4, setCurrentRowId4] = useState(null)
  const [currentRowIdFinalNorms, setCurrentRowIdFinalNorms] = useState(null)

  // default gradeId same as earlier (you used '4F' default)
  const [gradeId, setGradeId] = useState('4F')
  const [gradeDisplayName, setGradeDisplayName] = useState('4F')

  const [calculationObject, setCalculationObject] = useState({})
  const [selectedTab, setSelectedTab] = useState(0)
  const [productionRows, setProductionRows] = useState([])
  const [productionRowsConstants, setProductionRowsConstants] = useState([])

  const apiRef = useGridApiRef()

  // column defs
  const colDefs = useMemo(
    () => getNormalOpNormColDef({ headerMap }),
    [headerMap],
  )

  const colDefsIndividual = useMemo(
    () => [
      { field: 'isChecked', type: 'switch', widthT: 30, filter: false },
      {
        field: 'sapMaterialCode',
        title: 'SAP MAT Code',
        widthT: 120,
        editable: false,
      },
      { field: 'materialDisplayName', title: 'Particulars' },
      { field: 'uom', title: 'UOM', editable: false },
      {
        field: 'april',
        title: 'Value',
        editable: true,
        align: 'right',
        format: '{0:#.###}',
        type: 'number',
      },
      { field: 'remark', title: 'Remarks', widthT: 200, editable: true },
    ],
    [],
  )

  const monthIndexMap = {
    april: 4,
    may: 5,
    june: 6,
    july: 7,
    august: 8,
    september: 9,
    october: 10,
    november: 11,
    december: 12,
    january: 1,
    february: 2,
    march: 3,
  }

  const colDefsFinalNorms = useMemo(
    () => [
      {
        field: 'sapMaterialCode',
        title: 'SAP MAT Code',
        widthT: 120,
        editable: false,
        useMethodColors: true,
      },
      {
        field: 'materialDisplayName',
        title: 'Particulars',
        widthT: 130,
        editable: false,
      },
      { field: 'uom', title: 'UOM', widthT: 60, editable: false },
      ...MONTHS.map((m, i) => ({
        field: m,
        title: headerMap[monthIndexMap[m]] || m,
        editable: true,
        width: 120,
        align: 'right',
        type: 'number',
        format: '{0:#.###}',
      })),
      { field: 'isEditable', title: 'isEditable', hidden: true },
      { field: 'remark', title: 'Remark', widthT: 140, editable: true },
    ],
    [headerMap],
  )

  const colDefsFinalNorms1 = useMemo(
    () => [
      {
        field: 'isChecked',
        type: 'switch',
        widthT: 30,
        filter: false,
      },
      {
        field: 'sapMaterialCode',
        title: 'SAP MAT Code',
        widthT: 120,
        editable: false,
      },
      {
        field: 'materialDisplayName',
        title: 'Particulars',
        widthT: 130,
        editable: false,
      },
      { field: 'uom', title: 'UOM', widthT: 60, editable: false },
      ...MONTHS.map((m, i) => ({
        field: m,
        title: headerMap[monthIndexMap[m]] || m,
        editable: true,
        width: 120,
        align: 'right',
        type: 'number',
        format: '{0:#.###}',
      })),
      { field: 'isEditable', title: 'isEditable', hidden: true },
      { field: 'remarks', title: 'Remark', widthT: 140, editable: true },
    ],
    [headerMap],
  )
  const fetchConfigurationData = useCallback(
    async (gradeId = null) => {
      setProductionRows([])
      setLoading(true)

      try {
        const data = await DataService.getCatalystSelectivityData(
          keycloak,
          gradeId,
        )
        const filteredData = data?.filter(
          (item) => item.normType !== 'Report Manual Entry',
        )
        const formattedData = filteredData.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remarks,
          srNo: index + 1,
          Particulars: item.normType,
        }))
        setProductionRows(formattedData)
      } catch (error) {
        console.error('Error fetching configuration data:', error)
      } finally {
        setLoading(false)
      }
    },
    [keycloak],
  )

  const fetchConstantsData = useCallback(async () => {
    setProductionRowsConstants([])
    try {
      const constantsRes =
        await DataService.getCatalystSelectivityDataConstants(keycloak)
      if (constantsRes?.code !== 200) {
        setProductionRowsConstants([])
        return
      }

      const data = constantsRes?.data
      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: index,
        originalRemark: item.Remarks,
        srNo: index + 1,
        Particulars: item.NormTypeName,
        remarks: item.Remarks,
      }))

      setProductionRowsConstants(formattedData)
    } catch (error) {
      console.error('Error fetching constants data:', error)
    }
  }, [keycloak])
  // permission helper: if old year, getAdjustedPermissions blocks actions
  const getAdjustedPermissions = useCallback((permissions, isOldYearFlag) => {
    if (isOldYearFlag != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYearFlag,
      showCalculate: true,
    }
  }, [])

  // base permission objects (these are not final; we'll apply topGrid toggles)
  const baseModePermissions = useMemo(
    () => ({
      showAction: false,
      allAction: true,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: true,
      // saveBtn/showCalculate will be toggled per-grid depending on which is top
      saveBtn: true,
      showCalculate: true,
      downloadExcelBtnFromUI: gradeDisplayName !== 'Monthly' ? true : false,
      ExcelName: `${lowerVertName}_BestAcheived(Min CC)`,
      showCheckbox: false,
      marginBottom: false,
      showTitleNameBusiness: true,
      dropdownLabel: 'Select Mode',
      showCalculateVisibility: true,
      titleName: 'Best Achieved (Min CC)',
      isHeight: (rows?.length || 0) > 10,
    }),
    [calculationObject, lowerVertName, rows],
  )

  const baseExpressionPermissions = useMemo(
    () => ({
      showAction: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Expression (Norms)',
      saveBtn: true, // will be hidden if not top
      showCalculate: false,
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Expression_(Norms)`,
      showCheckbox: true,
    }),
    [lowerVertName],
  )

  const baseMonthlyPermissions = useMemo(
    () => ({
      showAction: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Best Achieved (Individual)',
      showCheckbox: true,
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Best Achieved (Norms)`,
      saveBtn: true, // visible only if top
      showCalculate: true, // visible only if top and calculation available
      showCalculateVisibility: true,
    }),
    [lowerVertName, calculationObject],
  )

  const baseFinalPermissions = useMemo(
    () => ({
      showAction: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Final (Norms)',
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Final_Norms`,
      saveWithRemark: true,
      saveBtn: true,
      showCalculate: true,
      showCalculateVisibility: true,
    }),
    [lowerVertName],
  )

  // Which grid is top?
  // Mode tab: if Monthly selected -> monthly grid is top; otherwise main is top.
  const isModeTab = selectedTab === 1
  const mainIsTop = isModeTab && gradeDisplayName !== 'Monthly'
  const monthlyIsTop = isModeTab && gradeDisplayName === 'Monthly'
  const finalIsTop = selectedTab === 2

  // derive per-grid permissions: top grid keeps save/calc flags, others have them hidden.
  const mainPermissions = useMemo(() => {
    const base = { ...baseModePermissions }
    base.saveBtn = selectedTab === 3 ? true : mainIsTop && base.saveBtn
    base.showCalculate = base.showCalculate
    return getAdjustedPermissions(base, isOldYear)
  }, [
    baseModePermissions,
    mainIsTop,
    getAdjustedPermissions,
    isOldYear,
    selectedTab,
  ])

  const expressionPermissions = useMemo(() => {
    const base = { ...baseExpressionPermissions }
    const showSave = !mainIsTop && !monthlyIsTop && isModeTab
    base.saveBtn = selectedTab === 3 ? true : showSave && base.saveBtn
    base.showCalculate = showSave && base.showCalculate
    return getAdjustedPermissions(base, isOldYear)
  }, [
    baseExpressionPermissions,
    mainIsTop,
    monthlyIsTop,
    isModeTab,
    getAdjustedPermissions,
    isOldYear,
    selectedTab,
  ])

  const monthlyPermissions = useMemo(() => {
    const base = { ...baseMonthlyPermissions }
    base.saveBtn = selectedTab === 3 ? true : monthlyIsTop && base.saveBtn
    base.showCalculate = base.showCalculate

    return getAdjustedPermissions(base, isOldYear)
  }, [
    baseMonthlyPermissions,
    monthlyIsTop,
    calculationObject,
    getAdjustedPermissions,
    isOldYear,
    selectedTab,
  ])

  const finalPermissions = useMemo(() => {
    const base = { ...baseFinalPermissions }
    base.saveBtn = base.saveBtn
    base.showCalculate = base.showCalculate

    return getAdjustedPermissions(base, isOldYear)
  }, [baseFinalPermissions, finalIsTop, getAdjustedPermissions, isOldYear])

  // --- Data fetchers ---
  const fetchFinalNorms = useCallback(async () => {
    try {
      const response =
        await NormalOperationNormsApiService.getfinalNorms(keycloak)
      if (response?.code !== 200) {
        setRowsBestFinalNorms([])
        return
      }
      const mapped = response?.data?.mcuNormsValueDTOList || []

      // Map the data and ensure Method field is included
      const mappedWithMethod = mapped.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: `${index}`,
        originalRemark: item.remark || '',
        remark: item.remark || '',
        Particulars: item.normType || item.normParameterTypeDisplayName,
        Method: item.Method || item.method,
      }))

      setRowsBestFinalNorms(mappedWithMethod)
    } catch (err) {
      console.error('fetchFinalNorms', err)
    }
  }, [keycloak])

  const fetchModeData = useCallback(
    async (gradeIdParam) => {
      if (!lowerVertName) return
      setLoading(true)
      try {
        if (lowerVertName === 'cracker') {
          const [bestResp, exprResp, yearlyResp, colorResp] = await Promise.all(
            [
              NormalOperationNormsApiService.getModeWiseNormsData(
                keycloak,
                gradeIdParam,
                'Best Achieved',
              ),
              NormalOperationNormsApiService.getModeWiseNormsData(
                keycloak,
                gradeIdParam,
                'Expression',
              ),
              NormalOperationNormsApiService.getModeWiseNormsData(
                keycloak,
                gradeIdParam,
                'Yearly Norms',
              ),
              NormalOperationNormsApiService.BestAchivedColorCodes(
                keycloak,
                PLANT_ID,
                AOP_YEAR,
                gradeIdParam,
              ),
            ],
          )

          setCalculationObject(bestResp?.data?.aopCalculation || {})

          setAllRedCell(
            (colorResp?.data?.data || []).map((obj) => ({
              ...obj,
              normParameterFKId: (obj.NormParameter_FK_Id || '').toUpperCase(),
            })),
          )

          setRows(
            mapApiRowToGrid(bestResp?.data?.mcuNormsValueDTOList, 'main-'),
          )
          setRowsExpression(
            mapApiRowToGrid(
              exprResp?.data?.mcuNormsValueDTOList,
              'expression-',
            ),
          )
          setRowsBestAchivedIndividual(
            mapApiRowToGrid(yearlyResp?.data?.mcuNormsValueDTOList, 'best-'),
          )
        }
      } catch (err) {
        console.error('fetchModeData', err)
      } finally {
        setLoading(false)
      }
    },
    [AOP_YEAR, PLANT_ID, keycloak, lowerVertName],
  )

  const fetchAllData = useCallback(
    async (gId) => {
      setLoading(true)
      try {
        setGrades([
          { name: '4F', displayName: '4F', gradeId: '4F' },
          { name: '5F', displayName: '5F', gradeId: '5F' },
          { name: '4F+D', displayName: '4F+D', gradeId: '4F+D' },
          { name: 'Monthly', displayName: 'Monthly', gradeId: 'Monthly' },
        ])

        const promises = []

        // Load data based on selected tab
        if (selectedTab === 0) {
          promises.push(fetchConfigurationData(gId))
        } else if (selectedTab === 1) {
          promises.push(fetchConstantsData())
        } else if (selectedTab === 3) {
          promises.push(fetchModeData(gId))
        } else if (selectedTab === 4) {
          promises.push(fetchFinalNorms())
        }

        await Promise.all(promises)
      } catch (err) {
        console.error('fetchAllData', err)
      } finally {
        setLoading(false)
      }
    },
    [
      fetchModeData,
      fetchFinalNorms,
      fetchConfigurationData,
      fetchConstantsData,
      selectedTab,
    ],
  )

  useEffect(() => {
    fetchAllData(gradeId)
  }, [
    fetchAllData,
    oldYear,
    yearChanged,
    keycloak,
    gradeId,
    plantObject?.id,
    selectedTab,
  ])

  // remark handlers
  const handleRemarkCellClick = useCallback((row) => {
    if (!row?.isEditable) return
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }, [])

  const handleRemarkCellClick1 = useCallback((row) => {
    if (!row?.isEditable) return
    setCurrentRemark1(row.remark || '')
    setCurrentRowId1(row.id)
    setRemarkDialogOpen1(true)
  }, [])
  const handleRemarkCellClick2 = useCallback((row) => {
    if (!row?.isEditable) return
    setCurrentRemark2(row.remark || '')
    setCurrentRowId2(row.id)
    setRemarkDialogOpen2(true)
  }, [])
  const handleRemarkCellClick3 = useCallback((row) => {
    if (!row?.isEditable) return
    setCurrentRemark3(row.remark || '')
    setCurrentRowId3(row.id)
    setRemarkDialogOpen3(true)
  }, [])
  const handleRemarkCellClick4 = useCallback((row) => {
    if (!row?.isEditable) return
    setCurrentRemark4(row.remark || '')
    setCurrentRowId4(row.id)
    setRemarkDialogOpen4(true)
  }, [])

  const handleRemarkCellClickFinalNorms = useCallback((row) => {
    if (!row?.isEditable) return
    console.log('row', row)

    setCurrentRemarkFinalNorms(row.remark || '')
    setCurrentRowIdFinalNorms(row.id)
    setRemarkDialogOpenFinalNorms(true)
  }, [])

  const isCellEditable = useCallback((params) => !!params.row.isEditable, [])

  // save/calc logic
  const saveRows = useCallback(
    async (rowsToSave, isFinal = false) => {
      if (!rowsToSave || rowsToSave.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      setLoading(true)
      try {
        const payload = mapGridRowToPayload(rowsToSave)
        const response = isFinal
          ? await NormalOperationNormsApiService.updateFinalNormsData(
              keycloak,
              gradeId,
              payload,
            )
          : await NormalOperationNormsApiService.updateModeWiseNormsData(
              keycloak,
              gradeId,
              payload,
            )

        if (response?.code === 200) {
          dispatch(setIsBlocked(false))
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Saved Successfully!',
            severity: 'success',
          })
          if (isFinal) setModifiedCellsFinalNorms({})
          else setModifiedCells({})
          await fetchModeData(gradeId)
        } else {
          setSnackbarOpen(true)
          setSnackbarData({ message: 'Data not saved!', severity: 'error' })
        }
        return response
      } catch (err) {
        console.error('saveRows', err)
        setSnackbarOpen(true)
        setSnackbarData({
          message: err.message || 'Error saving data',
          severity: 'error',
        })
      } finally {
        setLoading(false)
      }
    },
    [dispatch, fetchModeData, gradeId, keycloak],
  )

  const saveChangesCrackerFinalNorms = useCallback(() => {
    const data = Object.values(modifiedCellsFinalNorms)
    return saveRows(data, true)
  }, [modifiedCellsFinalNorms, saveRows])

  const saveChangesUnified = useCallback(async () => {
    if (selectedTab === 4) return saveChangesCrackerFinalNorms()

    // Prepare modified rows for save
    let allModified = Object.values(modifiedCells)

    if (!allModified || allModified.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      return
    }

    // Enforce single checked per materialName across the 2 grids
    const materialGroups = {} // key = materialName, value = array of rows
    allModified.forEach((row) => {
      if (!materialGroups[row.materialName])
        materialGroups[row.materialName] = []
      materialGroups[row.materialName].push(row)
    })

    const updatedRows = []
    Object.values(materialGroups).forEach((rows) => {
      // Find the row that is checked
      const checkedRow = rows.find((r) => r.isChecked)
      rows.forEach((r) => {
        if (r !== checkedRow) r.isChecked = false
        updatedRows.push(r)
      })
    })

    return saveRows(updatedRows, false)
  }, [
    selectedTab,
    gradeDisplayName,
    modifiedCells,
    saveRows,
    saveChangesCrackerFinalNorms,
  ])

  const handleCalculate = useCallback(async () => {
    setLoading(true)
    try {
      const res =
        await NormalOperationNormsApiService.handleCalculateNormalOperationNorms(
          PLANT_ID,
          year,
          keycloak,
        )
      const success = res == 0 || !!res
      setSnackbarOpen(true)
      setSnackbarData({
        message: success
          ? 'Data refreshed successfully!'
          : 'Data Refresh Failed!',
        severity: success ? 'success' : 'error',
      })
      if (success) await fetchModeData(gradeId)
      return res
    } catch (err) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: err.message || 'An error occurred',
        severity: 'error',
      })
      console.error('handleCalculate', err)
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, fetchModeData, gradeId, keycloak, year])

  const handleCalculateFinalNorms = useCallback(async () => {
    setLoading(true)
    try {
      const res = await NormalOperationNormsApiService.calculateFinalNorms(
        PLANT_ID,
        AOP_YEAR,
        keycloak,
      )
      const success = res == 0 || !!res
      setSnackbarOpen(true)
      setSnackbarData({
        message: success
          ? 'Data refreshed successfully!'
          : 'Data Refresh Failed!',
        severity: success ? 'success' : 'error',
      })
      if (success) await fetchModeData(gradeId)
      if (success) await fetchFinalNorms()
      return res
    } catch (err) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: err.message || 'An error occurred',
        severity: 'error',
      })
      console.error('handleCalculateFinalNorms', err)
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, fetchModeData, gradeId, keycloak, AOP_YEAR])

  const handleCalculateUnified = useCallback(async () => {
    if (selectedTab === 4) return handleCalculateFinalNorms()
    return handleCalculate()
  }, [selectedTab, handleCalculate, handleCalculateFinalNorms])

  const handleGlobalCheckboxChange = useCallback(
    (gridName, id, materialName, field, value, dataItem) => {
      // helper to ensure unique key format is consistent
      const normalizeUniqueId = (gName, rowId) => `${gName}-${rowId}`

      // 1) Build updated arrays synchronously from current state
      const newMainRows = (rows || []).map((row) => {
        if (gridName === 'main' && row.id === id)
          return { ...row, [field]: value }
        if (
          row.materialName === materialName &&
          !(gridName === 'main' && row.id === id)
        ) {
          return { ...row, [field]: false }
        }
        return row
      })

      const newExpressionRows = (rowsExpression || []).map((row) => {
        if (gridName === 'expression' && row.id === id)
          return { ...row, [field]: value }
        if (
          row.materialName === materialName &&
          !(gridName === 'expression' && row.id === id)
        ) {
          return { ...row, [field]: false }
        }
        return row
      })

      const newBestRows = (rowsBestAchivedIndividual || []).map((row) => {
        if (gridName === 'best' && row.id === id)
          return { ...row, [field]: value }
        if (
          row.materialName === materialName &&
          !(gridName === 'best' && row.id === id)
        ) {
          return { ...row, [field]: false }
        }
        return row
      })

      // 2) Collect unchecked rows from those new arrays (except the one we just checked)
      const uncheckedRows = []
      ;[
        { arr: newMainRows, gridName: 'main' },
        { arr: newExpressionRows, gridName: 'expression' },
        { arr: newBestRows, gridName: 'best' },
      ].forEach(({ arr, gridName: gName }) => {
        arr.forEach((r) => {
          // If this row belongs to the same material and is unchecked, and it's NOT the row we clicked,
          // then it's one of the rows that was implicitly unchecked
          if (
            r.materialName === materialName &&
            !(gName === gridName && r.id === id) &&
            !r[field]
          ) {
            uncheckedRows.push({ ...r, gridName: gName })
          }
        })
      })

      // 3) Apply the new arrays to state (this updates UI)
      setRows(newMainRows)
      setRowsExpression(newExpressionRows)
      setRowsBestAchivedIndividual(newBestRows)

      // 4) Build modifiedCells update using the full row objects we collected
      setModifiedCells((prev = {}) => {
        const updated = { ...prev }

        // checked row: prefer dataItem (from the grid event) but fall back to existing saved object
        const uniqueItemId = normalizeUniqueId(gridName, id)
        updated[uniqueItemId] = {
          ...(prev[uniqueItemId] || {}),
          ...(dataItem || {}),
          [field]: !!value,
          gridName,
          id,
        }

        // add/update all unchecked rows with their full data
        uncheckedRows.forEach((row) => {
          const rowUniqueId = normalizeUniqueId(row.gridName, row.id)
          updated[rowUniqueId] = {
            ...(prev[rowUniqueId] || {}),
            ...row, // includes months, idFromApi, materialFKId, materialName, etc.
            [field]: false,
          }
        })

        return updated
      })
    },
    // include the states/setters you use
    [
      rows,
      rowsExpression,
      rowsBestAchivedIndividual,
      setRows,
      setRowsExpression,
      setRowsBestAchivedIndividual,
      setModifiedCells,
    ],
  )

  const downloadExcelForConfiguration = useCallback(async () => {
    setSnackbarOpen(true)
    setSnackbarData({ message: 'Excel download started!', severity: 'success' })
    try {
      await NormalOperationNormsApiService.getNormalOpsNormsExcel(
        keycloak,
        gradeId,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (err) {
      console.error('downloadExcelForConfiguration', err)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    }
  }, [gradeId, keycloak])

  // grade handlers
  const handleGradeChange = useCallback((gId, gDisplayName) => {
    setGradeId(gId)
    setGradeDisplayName(gDisplayName)
  }, [])

  const onModeSelect = useCallback(
    (event) => {
      const selectedId = event.target.value
      const sel = grades.find((g) => g.gradeId === selectedId) || {
        displayName: selectedId,
      }
      handleGradeChange(selectedId, sel.displayName)
    },
    [grades, handleGradeChange],
  )

  // tabs
  const handleTabChange = useCallback(
    (_, newValue) => {
      setModifiedCells({})
      setSelectedTab(newValue)
      fetchAllData(gradeId)
    },
    [gradeId, fetchAllData],
  )

  const tabSx = {
    border: '1px solid #ADD8E6',
    borderBottom: '1px solid #ADD8E6',
    fontSize: '0.75rem',
    padding: '9px',
    minHeight: '12px',
  }
  const tabLabels = [
    'Configuration',
    'Constants',
    'Criteria for Best Achieved',
    'Norms Selection',
    'Final monthly norms',
  ]

  // UI render
  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box sx={{ margin: 0, padding: 0 }}>
        <Tabs
          value={selectedTab}
          onChange={handleTabChange}
          sx={{
            borderBottom: '0px solid #ccc',
            '.MuiTabs-indicator': { display: 'none' },
            margin: 0,
            minHeight: '28px',
          }}
        >
          {tabLabels.map((label) => (
            <Tab key={label} label={label} sx={tabSx} />
          ))}
        </Tabs>
      </Box>
      {selectedTab === 0 && (
        <SelectivityData
          rows={productionRows}
          loading={loading}
          fetchData={fetchConfigurationData}
          setRows={setProductionRows}
          configType='cracker_configuration'
          groupBy='Particulars'
          tabIndex='0'
          setGradeId={handleGradeChange}
        />
      )}
      {selectedTab === 1 && (
        <SelectivityData
          rows={productionRowsConstants}
          loading={loading}
          fetchData={fetchConstantsData}
          setRows={setProductionRowsConstants}
          configType='cracker_constants'
          groupBy='Particulars'
          tabIndex='1'
        />
      )}

      {/* Criteria Tab */}
      {selectedTab === 2 && <CrakcerConstants />}

      {/* Norms Selection Tab */}
      {selectedTab === 3 && (
        <>
          {/* EXTERNAL DROPDOWN */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 2 }}>
            <FormControl size='small' sx={{ minWidth: 120 }}>
              <InputLabel id='mode-select-label'>Select Mode</InputLabel>
              <Select
                labelId='mode-select-label'
                value={gradeId || ''}
                label='Mode'
                onChange={onModeSelect}
              >
                {grades.map((g) => (
                  <MenuItem key={g.gradeId} value={g.gradeId}>
                    {g.displayName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <Typography component='div' className='grid-title'>
              <span style={{ color: 'red', fontWeight: 'bold' }}>Red</span> -
              Propane (1Z)&nbsp;&nbsp;
              <span style={{ color: 'green', fontWeight: 'bold' }}>
                Green
              </span>{' '}
              - Propane (2Z)
            </Typography>
          </Box>

          {/* MODE TAB: render TOP grid first depending on Monthly vs non-Monthly */}
          {gradeDisplayName === 'Monthly' ? (
            <>
              {/* Monthly is top when Monthly selected -> monthly gets save/calc */}
              <KendoDataTables
                modifiedCells={modifiedCells}
                setModifiedCells={setModifiedCells}
                title='Normal Operations Norms'
                columns={colDefsIndividual}
                setRows={setRowsBestAchivedIndividual}
                rows={rowsBestAchivedIndividual}
                grades={grades}
                paginationOptions={[100, 200, 300]}
                saveChanges={saveChangesUnified}
                isCellEditable={isCellEditable}
                snackbarData={snackbarData}
                handleCalculate={handleCalculateUnified}
                snackbarOpen={snackbarOpen}
                apiRef={apiRef}
                setSnackbarOpen={setSnackbarOpen}
                setSnackbarData={setSnackbarData}
                remarkDialogOpen={remarkDialogOpen1}
                setRemarkDialogOpen={setRemarkDialogOpen1}
                currentRemark={currentRemark1}
                setCurrentRemark={setCurrentRemark1}
                currentRowId={currentRowId1}
                handleRemarkCellClick={handleRemarkCellClick1}
                permissions={monthlyPermissions}
                groupBy='Particulars'
                downloadExcelForConfiguration={downloadExcelForConfiguration}
                handleGradeChange={handleGradeChange}
                onGlobalCheckboxChange={handleGlobalCheckboxChange}
                plantID={PLANT_ID}
                gridName='best'
              />

              {/* expression below */}
              <KendoDataTables
                modifiedCells={modifiedCells}
                setModifiedCells={setModifiedCells}
                title='Normal Operations Norms'
                columns={
                  gradeDisplayName === 'Monthly' ? colDefsFinalNorms1 : colDefs
                }
                setRows={setRowsExpression}
                rows={rowsExpression}
                grades={grades}
                paginationOptions={[100, 200, 300]}
                saveChanges={saveChangesUnified}
                isCellEditable={isCellEditable}
                snackbarData={snackbarData}
                handleCalculate={handleCalculateUnified}
                snackbarOpen={snackbarOpen}
                apiRef={apiRef}
                setSnackbarOpen={setSnackbarOpen}
                setSnackbarData={setSnackbarData}
                remarkDialogOpen={remarkDialogOpen2}
                setRemarkDialogOpen={setRemarkDialogOpen2}
                currentRemark={currentRemark2}
                setCurrentRemark={setCurrentRemark2}
                currentRowId={currentRowId2}
                handleRemarkCellClick={handleRemarkCellClick2}
                permissions={expressionPermissions}
                groupBy='Particulars'
                downloadExcelForConfiguration={downloadExcelForConfiguration}
                handleGradeChange={handleGradeChange}
                plantID={PLANT_ID}
                onGlobalCheckboxChange={handleGlobalCheckboxChange}
                gridName='expression'
              />
            </>
          ) : (
            <>
              {/* Non-monthly: Main is top -> main gets save/calc */}
              <KendoDataTables
                modifiedCells={modifiedCells}
                setModifiedCells={setModifiedCells}
                columns={colDefs}
                setRows={setRows}
                rows={rows}
                grades={grades}
                paginationOptions={[100, 200, 300]}
                saveChanges={saveChangesUnified}
                isCellEditable={isCellEditable}
                snackbarData={snackbarData}
                handleCalculate={handleCalculateUnified}
                snackbarOpen={snackbarOpen}
                apiRef={apiRef}
                setSnackbarOpen={setSnackbarOpen}
                setSnackbarData={setSnackbarData}
                remarkDialogOpen={remarkDialogOpen3}
                setRemarkDialogOpen={setRemarkDialogOpen3}
                currentRemark={currentRemark3}
                setCurrentRemark={setCurrentRemark3}
                currentRowId={currentRowId3}
                handleRemarkCellClick={handleRemarkCellClick3}
                permissions={mainPermissions}
                allRedCell={allRedCell}
                groupBy='Particulars'
                downloadExcelForConfiguration={downloadExcelForConfiguration}
                handleGradeChange={handleGradeChange}
                onGlobalCheckboxChange={handleGlobalCheckboxChange}
                plantID={PLANT_ID}
                gridName='main'
                showThreeColors={true}
              />

              {/* expression below */}
              <KendoDataTables
                modifiedCells={modifiedCells}
                setModifiedCells={setModifiedCells}
                title='Normal Operations Norms'
                columns={colDefs}
                setRows={setRowsExpression}
                rows={rowsExpression}
                grades={grades}
                paginationOptions={[100, 200, 300]}
                saveChanges={saveChangesUnified}
                isCellEditable={isCellEditable}
                snackbarData={snackbarData}
                handleCalculate={handleCalculateUnified}
                snackbarOpen={snackbarOpen}
                apiRef={apiRef}
                setSnackbarOpen={setSnackbarOpen}
                setSnackbarData={setSnackbarData}
                remarkDialogOpen={remarkDialogOpen4}
                setRemarkDialogOpen={setRemarkDialogOpen4}
                currentRemark={currentRemark4}
                setCurrentRemark={setCurrentRemark4}
                currentRowId={currentRowId4}
                handleRemarkCellClick={handleRemarkCellClick4}
                permissions={expressionPermissions}
                groupBy='Particulars'
                downloadExcelForConfiguration={downloadExcelForConfiguration}
                handleGradeChange={handleGradeChange}
                plantID={PLANT_ID}
                onGlobalCheckboxChange={handleGlobalCheckboxChange}
                gridName='expression'
              />
            </>
          )}
        </>
      )}

      {/* FINAL norms tab: final grid is top */}
      {selectedTab === 4 && (
        <>
          {/* Add color-coded legend for Final norms */}
          <Box
            sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 2, mb: 2 }}
          >
            <Typography component='div' className='grid-title'>
              <span style={{ color: 'red', fontWeight: 'bold' }}>Red</span> -
              Expression &nbsp;&nbsp;
              <span style={{ color: 'green', fontWeight: 'bold' }}>
                Green
              </span>{' '}
              - BestAchieved(MinCC)&nbsp;&nbsp;
              <span style={{ color: 'blue', fontWeight: 'bold' }}>Blue</span> -
              Best Achieved(Indv)
            </Typography>
          </Box>
          <KendoDataTables
            modifiedCells={modifiedCellsFinalNorms}
            setModifiedCells={setModifiedCellsFinalNorms}
            columns={colDefsFinalNorms}
            setRows={setRowsBestFinalNorms}
            rows={rowsBestFinalNorms}
            paginationOptions={[100, 200, 300]}
            saveChanges={saveChangesUnified}
            isCellEditable={isCellEditable}
            snackbarData={snackbarData}
            handleCalculate={handleCalculateUnified}
            snackbarOpen={snackbarOpen}
            apiRef={apiRef}
            setSnackbarOpen={setSnackbarOpen}
            setSnackbarData={setSnackbarData}
            remarkDialogOpen={remarkDialogOpenFinalNorms}
            setRemarkDialogOpen={setRemarkDialogOpenFinalNorms}
            currentRemark={currentRemarkFinalNorms}
            setCurrentRemark={setCurrentRemarkFinalNorms}
            currentRowId={currentRowIdFinalNorms}
            handleRemarkCellClick={handleRemarkCellClickFinalNorms}
            permissions={finalPermissions}
            groupBy='Particulars'
            plantID={PLANT_ID}
          />
        </>
      )}
    </div>
  )
}

export default NormalOpNormsScreenCracker
