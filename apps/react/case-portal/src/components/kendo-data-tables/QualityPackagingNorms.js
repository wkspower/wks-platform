import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Select,
  MenuItem,
  Backdrop,
  Box,
  CircularProgress,
  Typography,
  Button,
  Tab,
  Tabs,
} from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import KendoDataTables from './index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { Grid, TextField } from '../../../node_modules/@mui/material/index'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { TextArea } from '../../../node_modules/@progress/kendo-react-inputs/index'
import { AOPMaintenanceApiService } from 'services/aop-maintenance-api-service'
import { getRoleName } from 'services/role-service'
import { QualityParameterService } from '../../services/QualityParameterService'
import { t } from '../../../node_modules/i18next/index'
import { format } from '../../../node_modules/date-fns/format'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { validateFields } from 'utils/validationUtils'
export default function QualityPackagingNorms() {
  const [rows, setRows] = useState([])
  const [priceDiffRows, setPriceDiffRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)
  const [remarkDialogOpenDiff, setRemarkDialogOpenDiff] = useState(false)
  const [currentRemarkDiff, setCurrentRemarkDiff] = useState('')
  const [currentRowIdDiff, setCurrentRowIdDiff] = useState(null)
  const [enableSaveAddBtnDiff, setEnableSaveAddBtnDiff] = useState(false)
  const [modifiedCellsDiff, setModifiedCellsDiff] = useState({})
  const [modifiedCellsPackaging, setModifiedCellsPackaging] = useState({})
  const [remarkDialogOpenPackaging, setRemarkDialogOpenPackaging] =
    useState(false)
  const [currentRemarkPackaging, setCurrentRemarkPackaging] = useState('')
  const [currentRowIdPackaging, setCurrentRowIdPackaging] = useState(null)
  const [enableSaveAddBtnPackaging, setEnableSaveAddBtnPackaging] =
    useState(false)
  const [modifiedCellsOtherCosts, setModifiedCellsOtherCosts] = useState({})
  const [remarkDialogOpenOtherCosts, setRemarkDialogOpenOtherCosts] =
    useState(false)
  const [currentRemarkOtherCosts, setCurrentRemarkOtherCosts] = useState('')
  const [currentRowIdOtherCosts, setCurrentRowIdOtherCosts] = useState(null)
  const [enableSaveAddBtnOtherCosts, setEnableSaveAddBtnOtherCosts] =
    useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
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
  } = dataGridStore
  const keycloak = useSession()
  const PLANT_ID = plantObject?.id
  const PLANT_NAME = plantObject?.name
  const SITE_NAME = siteObject?.name
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const AOP_YEAR = year?.selectedYear
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const valueFormat = ValueFormatterConsumption()
  //const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const READ_ONLY = false
  const [tabIndex, setTabIndex] = useState(0)
  const defaultTabs = ['Quality', 'Packaging & Consumables']
  const [packagingRows, setPackagingRows] = useState([])
  const [rowsOtherCosts, setRowsOtherCosts] = useState([])
  const [calculationObject, setCalculationObject] = useState([])

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const handleRemarkCellClickDiff = (row) => {
    if (READ_ONLY) return
    setCurrentRemarkDiff(row.remark || '')
    setCurrentRowIdDiff(row.id)
    setRemarkDialogOpenDiff(true)
  }

  const handleRemarkCellClickPackaging = (row) => {
    if (READ_ONLY) return
    setCurrentRemarkPackaging(row.remark || '')
    setCurrentRowIdPackaging(row.id)
    setRemarkDialogOpenPackaging(true)
  }
  const handleRemarkCellClickOtherCosts = (row) => {
    if (READ_ONLY) return
    setCurrentRemarkOtherCosts(row.remark || '')
    setCurrentRowIdOtherCosts(row.id)
    setRemarkDialogOpenOtherCosts(true)
  }

  function getPreviousYear(aopYear) {
    if (!aopYear) return ''
    const [start, end] = aopYear.split('-').map((s) => s.trim())
    const prevStart = parseInt(start, 10) - 1
    const prevEnd = (parseInt(end, 10) === 0 ? 99 : parseInt(end, 10) - 1)
      .toString()
      .padStart(2, '0')
    return `${prevStart}-${prevEnd}`
  }
  const previousYear = getPreviousYear(AOP_YEAR)
  const columns = [
    {
      field: 'id',
      title: 'ID',
      editable: false,
      hidden: true,
    },
    {
      field: 'sno',
      title: 'S.no',
      widthT: 60,
      editable: false,
      format: '{0:n0}',
      type: 'number',
    },
    {
      field: 'normParameterTypeName ',
      title: 'Norm Parameter Type',
      hidden: true,
    },
    {
      field: 'materialId',
      title: 'Material ID',
      hidden: true,
      editable: false,
    },
    {
      field: 'name',
      title: 'Name',
      editable: false,
    },
    {
      field: 'unit',
      title: 'Unit',
      editable: false,
      widthT: 60,
    },

    {
      field: 'budget',
      title: `Budget ${previousYear}`,
      editable: false,
      type: 'numberWithUOMValidation',
      format: valueFormat,
    },

    {
      field: 'actual',
      title: `Actual ${previousYear}`,
      editable: true,
      type: 'numberWithUOMValidation',
      format: valueFormat,
    },

    {
      field: 'proposedNorm',
      title: `Proposed Norm ${AOP_YEAR}`,
      width: 150,
      editable: true,
      editor: 'numeric',
      type: 'numberWithUOMValidation',
      format: valueFormat,
    },
    {
      field: 'remark',
      title: 'Remark',
      editable: true,
    },
  ]

  const fetchQualityParameters = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) {
      setRows([])
      return
    }
    setLoading(true)
    try {
      const res = await QualityParameterService.getQualityParameterData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.code === 200 && Array.isArray(res?.data?.Data)) {
        const mappedRows = res.data.Data.map((item, idx) => ({
          id: item.id || idx + 1,
          sno: idx + 1,
          materialId: item.materialId,
          name: item.displayName,
          unit: item.uom,
          budget: item.prevBudget,
          actual: item.prevActual,
          proposedNorm: item.proposedNorm,
          normParameterTypeName: item.normParameterTypeName,
          isEditable: item.isEditable !== false,
          Particulars: item.normParameterTypeName,
          remark: item.remark,
          originalRemark: item.remark,
        }))
        setRows(mappedRows)
      } else {
        setRows([])
      }
    } catch (err) {
      console.error('fetchQualityParameters error', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  // For Price Differential
  const fetchPriceDifferential = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) {
      setPriceDiffRows([])
      return
    }
    setLoading(true)
    try {
      const res1 = await QualityParameterService.getPriceDifferentialData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const priceDiffSource = Array.isArray(res1?.data)
        ? res1.data
        : Array.isArray(res1?.data?.data)
          ? res1.data.data
          : Array.isArray(res1?.data?.Data)
            ? res1.data.Data
            : []
      const mappedPriceDiffRows = priceDiffSource.map((item, idx) => ({
        id: item.id || idx,
        materialId: item.materialId,
        qualityType: item.displayName,
        percentage: item.percentage,
        normParameterTypeName: item.normParameterTypeName,
        originalRemark: item.remark,
        remark: item.remark,
        Particulars: item.normParameterTypeName,
        unit: '%',
      }))
      setPriceDiffRows(mappedPriceDiffRows)
    } catch (err) {
      console.error('fetchPriceDifferential error', err)
      setPriceDiffRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    if (tabIndex === 0) {
      fetchQualityParameters()
      fetchPriceDifferential()
    }
    // Add other fetches for other tabs if needed
  }, [
    fetchQualityParameters,
    fetchPriceDifferential,
    PLANT_ID,
    VERTICAL_ID,
    tabIndex,
  ])

  const fetchPackagingRows = useCallback(async () => {
    setLoading(true)
    try {
      const res = await QualityParameterService.getPackagingConsumbleData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      setCalculationObject(res?.data?.aopCalculation)
      if (res?.code === 200 && Array.isArray(res?.data?.data)) {
        const mappedRows = res.data.data.map((item, idx) => ({
          id: item.id || idx,
          sno: idx + 1,
          materialId: item.materialId,
          name: item.displayName,
          unit: item.uom,
          packagingPrice: item.packagingPrice,
          budget: item.prevBudget,
          actual: item.prevActual,
          proposedNorm: item.proposedNorm,
          sapMaterialCode: item.sapMaterialCode,
          Particulars: item.normParameterTypeName,
          originalRemark: item.remark,
          remark: item.remark,
        }))
        setPackagingRows(mappedRows)
      } else {
        setPackagingRows([])
      }
    } catch (err) {
      console.error('fetchPackagingRows error', err)
      setPackagingRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  const handleCalculate = async () => {
    try {
      const data = await QualityParameterService.calculatePackagingData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (data || data == 0) {
        // dispatch(setIsBlocked(true))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        fetchPackagingRows()
        fetchOtherCostsRows()
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
  // // Fetch for Other Costs
  const fetchOtherCostsRows = useCallback(async () => {
    setLoading(true)
    try {
      // Replace with your actual API call for other costs data
      const res = await QualityParameterService.getOtherCostData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.code === 200 && Array.isArray(res?.data?.Data)) {
        const mappedRows = res.data.Data.map((item, idx) => ({
          id: item.id || idx,
          sno: idx + 1,
          materialId: item.materialId,
          name: item.displayName,
          unit: item.uom,
          budget: item.prevBudget,
          actual: item.prevActual,
          proposedNorm: item.proposedNorm,
          sapMaterialCode: item.sapMaterialCode,
          normParameterTypeName: item.normTypeName,
          Particulars: item.normTypeName,
          originalRemark: item.remark,
          remark: item.remark,
        }))
        setRowsOtherCosts(mappedRows)
      } else {
        setRowsOtherCosts([])
      }
    } catch (err) {
      console.error('fetchOtherCostsRows error', err)
      setRowsOtherCosts([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    if (tabIndex === 1) {
      fetchPackagingRows()
      fetchOtherCostsRows()
    }
  }, [fetchPackagingRows, tabIndex])

  const priceDiffColumns = [
    {
      field: 'id',
      title: 'ID',
      widthT: 50, // Changed from width
      editable: false,
      hidden: true,
    },
    {
      field: 'materialId',
      title: 'Material ID',
      widthT: 120, // Changed from width
      hidden: true,
      editable: false,
    },
    {
      field: 'normParameterTypeName ',
      title: 'Norm Parameter Type',
      hidden: true,
    },
    {
      field: 'qualityType',
      title: 'Quality Type',
      editable: false,
      widthT: 200,
    },
    {
      field: 'percentage',
      title: 'Value (%)',
      editable: true,
      type: 'numberWithUOMValidation',
      format: valueFormat,
      widthT: 200,
    },
    {
      field: 'unit',
      hidden: true,
    },
    {
      field: 'remark',
      title: 'Remark',
      editable: true,
    },
  ]

  const packagingColumns = [
    {
      field: 'sno',
      title: 'S.no',
      widthT: 70,
      format: '{0:n0}',
      editable: false,
      type: 'number',
    },
    {
      field: 'materialId',
      title: 'Material ID',
      editable: false,
      hidden: true,
    },
    {
      field: 'sapMaterialCode',
      title: 'SAP Material Code',
      editable: false,
    },
    {
      field: 'name',
      title: 'Name of Item',
      editable: false,
    },
    {
      field: 'unit',
      title: 'Unit',
      widthT: 70,
      editable: false,
    },
    {
      field: 'packagingPrice',
      title: 'Packaging Price (Rs)',
      editable: true,
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'budget',
      title: `Budget ${previousYear}`,
      editable: false,
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'actual',
      title: `Actual ${previousYear}`,
      editable: true,
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'proposedNorm',
      title: `Proposed Norm ${AOP_YEAR}`,
      editable: true,
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'remark',
      title: 'Remark',
      editable: true,
    },
  ]

  const columnsOtherCosts = [
    {
      field: 'sno',
      title: 'S.no',
      widthT: 70,
      type: 'number',
      format: '{0:n0}',
    },
    {
      field: 'materialId',
      title: 'Material ID',
      editable: false,
      hidden: true,
    },
    {
      field: 'sapMaterialCode',
      title: 'SAP Material Code',
      editable: false,
    },
    {
      field: 'normParameterTypeName ',
      title: 'Norm Parameter Type',
      hidden: true,
    },
    {
      field: 'name',
      title: 'Name of Item',
      editable: false,
    },
    {
      field: 'unit',
      title: 'Unit',
      widthT: 80,
    },
    {
      field: 'budget',
      title: `Budget ${previousYear}`,
      editable: true,
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'actual',
      title: `Actual ${previousYear}`,
      editable: true,
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'proposedNorm',
      title: `Proposed Cost ${AOP_YEAR}`,
      editable: true,
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'remark',
      title: 'Remark',
      editable: true,
    },
  ]
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
      const requiredFields = ['remark']
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

      // Prepare the data to save
      const qualityParameterDTOList = Object.values(modifiedCells).map(
        (row) => ({
          id: null,
          materialId: row.materialId,
          displayName: row.name,
          uom: row.unit,
          prevBudget: row.budget,
          prevActual: row.actual,
          proposedNorm: row.proposedNorm,
          plantId: PLANT_ID,
          aopYear: AOP_YEAR,
          remark: row.remark || '',
          normParameterTypeName: 'Quality',
        }),
      )

      setLoading(true)
      const res = await QualityParameterService.saveQualityParameterData(
        PLANT_ID,
        AOP_YEAR,
        qualityParameterDTOList,
        keycloak,
      )

      if (res?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({}) // Clear modified cells after successful save
        fetchQualityParameters()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Save Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      console.error('Error while save', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, keycloak, PLANT_ID, AOP_YEAR, fetchQualityParameters])

  const savePriceDiffChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCellsDiff)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      const requiredFields = ['remark']
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

      // Prepare the data to save
      const priceDifferentialDTOList = Object.values(modifiedCellsDiff).map(
        (row) => ({
          id: null,
          displayName: row.qualityType,
          percentage: row.percentage,
          plantId: PLANT_ID,
          aopYear: AOP_YEAR,
          remark: row.remark || 'system gen',
          normParameterTypeName: 'Quality',
          materialId: row.materialId,
        }),
      )

      setLoading(true)
      const res = await QualityParameterService.savePriceDifferentialData(
        PLANT_ID,
        AOP_YEAR,
        priceDifferentialDTOList,
        keycloak,
      )

      if (res?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsDiff({})
        fetchPriceDifferential()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Save Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      console.error('Error while saving price differential', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [modifiedCellsDiff, keycloak, PLANT_ID, AOP_YEAR, fetchPriceDifferential])

  const savePackagingChanges = useCallback(async () => {
    try {
      var data = Object.values(modifiedCellsPackaging)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      const requiredFields = ['remark']
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

      // Prepare the data to save
      const packagingConsumbleDTOList = Object.values(
        modifiedCellsPackaging,
      ).map((row) => ({
        id: null,
        materialId: row.materialId,
        displayName: row.name, // API expects displayName
        uom: row.unit, // API expects uom
        packagingPrice: row.packagingPrice,
        prevBudget: row.budget, // API expects prevBudget
        prevActual: row.actual, // API expects prevActual
        proposedNorm: row.proposedNorm,
        plantId: PLANT_ID,
        aopYear: AOP_YEAR,
        remark: row.remark || '',
        normParameterTypeName: 'packaging And Consumables',
      }))

      setLoading(true)
      // Replace with your actual save API for packaging
      const res = await QualityParameterService.savePackagingConsumbleData(
        PLANT_ID,
        AOP_YEAR,
        packagingConsumbleDTOList,
        keycloak,
      )

      if (res?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsPackaging({})
        fetchPackagingRows()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Save Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      console.error('Error while saving packaging data', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [modifiedCellsPackaging, keycloak, PLANT_ID, AOP_YEAR, fetchPackagingRows])

  const saveOtherCostsChanges = useCallback(async () => {
    try {
      var data = Object.values(modifiedCellsOtherCosts)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      const requiredFields = ['remark']
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
      // Prepare the data to save
      const otherCostDTOList = Object.values(modifiedCellsOtherCosts).map(
        (row) => ({
          id: null,
          materialId: row.materialId,
          displayName: row.name,
          uom: row.unit,
          prevBudget: row.budget,
          prevActual: row.actual,
          proposedNorm: row.proposedNorm,
          plantId: PLANT_ID,
          aopYear: AOP_YEAR,
          remark: row.remark || '',
        }),
      )

      setLoading(true)
      // Replace with your actual save API for other costs
      const res = await QualityParameterService.saveOtherCostData(
        PLANT_ID,
        AOP_YEAR,
        otherCostDTOList,
        keycloak,
      )

      if (res?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsOtherCosts({})
        fetchOtherCostsRows()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Save Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      console.error('Error while saving other costs data', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [modifiedCellsOtherCosts, keycloak, PLANT_ID, AOP_YEAR])

  const handleExcelUpload = (type) => (rawFile) => {
    uploadQualityPackagingNorms(rawFile, type)
  }
  const uploadQualityPackagingNorms = async (rawFile, type) => {
    setLoading(true)
    try {
      let response
      if (type === 'Quality_Parameters') {
        response = await QualityParameterService.QualityParameterExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
        if (response?.code === 200) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: response?.message || 'Uploaded Successfully!',
            severity: 'success',
          })
          setModifiedCells({})
          fetchQualityParameters()
        } else if (response?.code === 400 && response?.data) {
          // Error file handling
          const byteCharacters = atob(response.data)
          const byteNumbers = Array.from(byteCharacters, (char) =>
            char.charCodeAt(0),
          )
          const byteArray = new Uint8Array(byteNumbers)
          const blob = new Blob([byteArray], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.setAttribute('download', `Error File - ${type}.xlsx`)
          document.body.appendChild(link)
          link.click()
          link.remove()
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Partial data saved. Error file downloaded.',
            severity: 'warning',
          })
          fetchQualityParameters()
        }
      } else if (type === 'Price_differential') {
        response = await QualityParameterService.PriceDifferentialExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
        if (response?.code === 200) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: response?.message || 'Uploaded Successfully!',
            severity: 'success',
          })
          setModifiedCellsDiff({})
          fetchPriceDifferential()
        } else if (response?.code === 400 && response?.data) {
          // Error file handling
          const byteCharacters = atob(response.data)
          const byteNumbers = Array.from(byteCharacters, (char) =>
            char.charCodeAt(0),
          )
          const byteArray = new Uint8Array(byteNumbers)
          const blob = new Blob([byteArray], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.setAttribute('download', `Error File - ${type}.xlsx`)
          document.body.appendChild(link)
          link.click()
          link.remove()
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Partial data saved. Error file downloaded.',
            severity: 'warning',
          })
          fetchPriceDifferential()
        }
      } else if (type === 'packaging') {
        response = await QualityParameterService.PackagingConsumbleExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
        if (response?.code === 200) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: response?.message || 'Uploaded Successfully!',
            severity: 'success',
          })
          setModifiedCellsPackaging({})
          fetchPackagingRows()
        } else if (response?.code === 400 && response?.data) {
          // Error file handling
          const byteCharacters = atob(response.data)
          const byteNumbers = Array.from(byteCharacters, (char) =>
            char.charCodeAt(0),
          )
          const byteArray = new Uint8Array(byteNumbers)
          const blob = new Blob([byteArray], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.setAttribute('download', `Error File - ${type}.xlsx`)
          document.body.appendChild(link)
          link.click()
          link.remove()
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Partial data saved. Error file downloaded.',
            severity: 'warning',
          })
          fetchPackagingRows()
        }
      } else if (type === 'othercost') {
        response = await QualityParameterService.OtherCostExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
        if (response?.code === 200) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: response?.message || 'Uploaded Successfully!',
            severity: 'success',
          })
          setModifiedCellsOtherCosts({})
          fetchOtherCostsRows()
        } else if (response?.code === 400 && response?.data) {
          // Error file handling
          const byteCharacters = atob(response.data)
          const byteNumbers = Array.from(byteCharacters, (char) =>
            char.charCodeAt(0),
          )
          const byteArray = new Uint8Array(byteNumbers)
          const blob = new Blob([byteArray], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.setAttribute('download', `Error File - ${type}.xlsx`)
          document.body.appendChild(link)
          link.click()
          link.remove()
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Partial data saved. Error file downloaded.',
            severity: 'warning',
          })
          fetchOtherCostsRows()
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Invalid upload type!',
          severity: 'error',
        })
        return
      }

      if (
        response?.code !== 200 &&
        !(response?.code === 400 && response?.data)
      ) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error uploading excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }
  const downloadExcelForConfiguration = async (type) => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response
      let EXCEL_EXPORT_TITLE = ''

      if (type === 'Quality_Parameters') {
        EXCEL_EXPORT_TITLE = `${vertName}_${SITE_NAME}_${PLANT_NAME}_Quality_Parameters`
        response = await QualityParameterService.QualityParametersExport(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          EXCEL_EXPORT_TITLE,
        )
      } else if (type === 'Price_differential') {
        EXCEL_EXPORT_TITLE = `${vertName}_${SITE_NAME}_${PLANT_NAME}_Price_Differential`
        response = await QualityParameterService.PriceDifferentialExport(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          EXCEL_EXPORT_TITLE,
        )
      } else if (type === 'packaging') {
        EXCEL_EXPORT_TITLE = `${vertName}_${SITE_NAME}_${PLANT_NAME}_Packagings_Consumables`
        response = await QualityParameterService.PackagingConsumbleExport(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          EXCEL_EXPORT_TITLE,
        )
      } else {
        EXCEL_EXPORT_TITLE = `${vertName}_${PLANT_NAME}_Other_Costs`
        response = await QualityParameterService.OtherCostExport(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          EXCEL_EXPORT_TITLE,
        )
      }

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Excel downloaded successfully!',
        severity: 'success',
      })
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
  const getAdjustedPermissionsQuality = (permissions, isOldYear) => {
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
    }
  }

  const adjustedPermissionsQuality = getAdjustedPermissionsQuality(
    {
      allAction: true,
      saveBtn: true,
      showTitleNameBusiness: true,
      titleName: 'Quality Parameters',
      adjustedPermissions: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      ExcelName: `${lowerVertName}_Quality_Parameters`,
      addButton: false,
      deleteButton: false,
      showTitle: true,
    },
    isOldYear,
  )
  const getAdjustedPermissionDifferential = (permissions, isOldYear) => {
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
    }
  }

  const peopleInitiativePermissionDifferential =
    getAdjustedPermissionDifferential(
      {
        saveBtn: true,
        allAction: true,
        showTitleNameBusiness: true,
        titleName: 'Price Differential As Percentage wrt Quality',
        adjustedPermissions: true,
        downloadExcelBtn: true,
        uploadExcelBtn: true,
        ExcelName: `${lowerVertName}_Price_Differential`,
        addButton: false,
        deleteButton: false,
      },
      isOldYear,
    )

  const getAdjustedPermissionsPackaging = (permissions, isOldYear) => {
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
    }
  }

  const adjustedPermissionsPackaging = getAdjustedPermissionsPackaging(
    {
      allAction: true,
      saveBtn: true,
      showTitleNameBusiness: true,
      titleName: 'Packagings & Consumables',
      adjustedPermissions: true,
      //downloadExcelBtnFromUI: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      ExcelName: `${lowerVertName}_Packagings_Consumables`,
      addButton: false,
      deleteButton: false,
      showCalculate: lowerVertName === 'elastomer' ? false : true,
      showCalculateVisibility:
        Object.keys(calculationObject || {}).length > 0 ? true : false,
    },
    isOldYear,
  )
  const getAdjustedPermissionsOtherCost = (permissions, isOldYear) => {
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
    }
  }

  const adjustedPermissionsOtherCost = getAdjustedPermissionsOtherCost(
    {
      allAction: true,
      saveBtn: true,
      showTitleNameBusiness: true,
      titleName: 'Other Costs',
      adjustedPermissions: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      ExcelName: `${lowerVertName}_Other_Costs`,
      addButton: false,
      deleteButton: false,
    },
    isOldYear,
  )

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      {defaultTabs?.length > 1 && (
        <Tabs
          value={tabIndex}
          onChange={(e, newIndex) => setTabIndex(newIndex)}
          variant='scrollable'
          scrollButtons='auto'
          sx={{
            borderBottom: '0px solid #ccc',
            '.MuiTabs-indicator': { display: 'none' },
            margin: '0px 0px 10px 0px',
            minHeight: '28px',
          }}
          textColor='primary'
          indicatorColor='primary'
        >
          {defaultTabs.map((label, idx) => (
            <Tab
              key={idx}
              label={label}
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
            />
          ))}
        </Tabs>
      )}
      {tabIndex === 0 && (
        <Box>
          <KendoDataTables
            columns={columns.filter((col) => !col.hidden)}
            rows={rows}
            setRows={setRows}
            title='Quality Parameters'
            modifiedCells={modifiedCells}
            setModifiedCells={setModifiedCells}
            remarkDialogOpen={remarkDialogOpen}
            setRemarkDialogOpen={setRemarkDialogOpen}
            currentRemark={currentRemark}
            setCurrentRemark={setCurrentRemark}
            currentRowId={currentRowId}
            setCurrentRowId={setCurrentRowId}
            enableSaveAddBtn={enableSaveAddBtn}
            permissions={adjustedPermissionsQuality}
            saveChanges={saveChanges}
            handleRemarkCellClick={handleRemarkCellClick}
            downloadExcelForConfiguration={() =>
              downloadExcelForConfiguration('Quality_Parameters')
            }
            handleExcelUpload={handleExcelUpload('Quality_Parameters')}
            groupBy='Particulars'
          />
          <KendoDataTables
            columns={priceDiffColumns}
            rows={priceDiffRows}
            setRows={setPriceDiffRows}
            title='Price Differential As Percentage wrt Quality'
            modifiedCells={modifiedCellsDiff}
            setModifiedCells={setModifiedCellsDiff}
            permissions={peopleInitiativePermissionDifferential}
            remarkDialogOpen={remarkDialogOpenDiff}
            setRemarkDialogOpen={setRemarkDialogOpenDiff}
            currentRemark={currentRemarkDiff}
            setCurrentRemark={setCurrentRemarkDiff}
            currentRowId={currentRowIdDiff}
            setCurrentRowId={setCurrentRowIdDiff}
            enableSaveAddBtn={enableSaveAddBtnDiff}
            saveChanges={savePriceDiffChanges}
            handleRemarkCellClick={handleRemarkCellClickDiff}
            downloadExcelForConfiguration={() =>
              downloadExcelForConfiguration('Price_differential')
            }
            handleExcelUpload={handleExcelUpload('Price_differential')}
            groupBy='Particulars'
          />
        </Box>
      )}
      {tabIndex === 1 && (
        <Box>
          <KendoDataTables
            columns={packagingColumns}
            rows={packagingRows}
            setRows={setPackagingRows}
            title='Packings & Consumables'
            saveChanges={savePackagingChanges}
            permissions={adjustedPermissionsPackaging}
            handleCalculate={handleCalculate}
            modifiedCells={modifiedCellsPackaging}
            setModifiedCells={setModifiedCellsPackaging}
            remarkDialogOpen={remarkDialogOpenPackaging}
            setRemarkDialogOpen={setRemarkDialogOpenPackaging}
            currentRemark={currentRemarkPackaging}
            setCurrentRemark={setCurrentRemarkPackaging}
            currentRowId={currentRowIdPackaging}
            setCurrentRowId={setCurrentRowIdPackaging}
            handleRemarkCellClick={handleRemarkCellClickPackaging}
            enableSaveAddBtn={enableSaveAddBtnPackaging}
            downloadExcelForConfiguration={() =>
              downloadExcelForConfiguration('packaging')
            }
            handleExcelUpload={handleExcelUpload('packaging')}
            groupBy='Particulars'
          />
          <KendoDataTables
            columns={columnsOtherCosts}
            rows={rowsOtherCosts}
            setRows={setRowsOtherCosts}
            title='Other Costs'
            saveChanges={saveOtherCostsChanges}
            permissions={adjustedPermissionsOtherCost}
            modifiedCells={modifiedCellsOtherCosts}
            setModifiedCells={setModifiedCellsOtherCosts}
            remarkDialogOpen={remarkDialogOpenOtherCosts}
            setRemarkDialogOpen={setRemarkDialogOpenOtherCosts}
            currentRemark={currentRemarkOtherCosts}
            setCurrentRemark={setCurrentRemarkOtherCosts}
            currentRowId={currentRowIdOtherCosts}
            setCurrentRowId={setCurrentRowIdOtherCosts}
            handleRemarkCellClick={handleRemarkCellClickOtherCosts}
            enableSaveAddBtn={enableSaveAddBtnOtherCosts}
            downloadExcelForConfiguration={() =>
              downloadExcelForConfiguration('othercost')
            }
            handleExcelUpload={handleExcelUpload('othercost')}
            groupBy='Particulars'
            // Add other props as needed
          />
        </Box>
      )}
      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
