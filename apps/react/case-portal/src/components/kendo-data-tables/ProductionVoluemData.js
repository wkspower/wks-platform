import React, { useEffect, useMemo, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import getEnhancedProductionColDefs from '../data-tables/CommonHeader/Kendo_ProductionVolumeHeader'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import { Typography } from '../../../node_modules/@mui/material/index'
// import { usePermissions } from 'hooks/usePermissions'
import KendoDataTables from './index'
import { validateFields } from 'utils/validationUtils'
import { ProductionVolumeDataApiService } from 'services/production-volume-data-api-service'
import { DataService } from 'services/DataService'
import {
  getColDefsDesignCapacity,
  getColDefsDesignCapacityPEPP,
  getColDefsDesignCapacityPTA,
  getColDefsDesignCapacityPTADMD,
  getColDefsMaxAchievedCapacity,
  getColDefsMaxAchievedCapacityPEPP,
  getColDefsMaxAchievedCapacityPTA,
  getColDefsNonEditable,
  getColDefsPercentageSummary,
  getColDefsPercentageSummaryPEPP,
} from './Utilities-Kendo/productionTargetColDefs'
import ProductionTarget from './ProductionTarget'
import AromaticsProductionGrids from './AromaticsProductionGrids'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { getRoleName } from 'services/role-service'
const ProductionvolumeData = ({ permissions }) => {
  // const { isReadOnly, isWriteOnly, isReadWrite, isFullAccess, isApproveOnly } =
  //   usePermissions()

  const [modifiedCells, setModifiedCells] = React.useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)
  const [modifiedCellsDesignCapacity, setModifiedCellsDesignCapacity] =
    React.useState({})
  const [enableSaveAddBtnDesignCapacity, setEnableSaveAddBtnDesignCapacity] =
    useState(false)
  const [_plantID, set_PlantID] = useState('')

  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)

  const [calculationObject, setCalculationObject] = useState([])

  const apiRef = useGridApiRef()
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

  const IS_OLD_YEAR = oldYear?.oldYear
  const isOldYear = false

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const PLANT_ID = plantObject?.id
  const VERTICAL_ID = verticalObject?.id
  const SITE_ID = siteObject?.id
  const AOP_YEAR = year?.selectedYear

  const PLANT_NAME = plantObject?.name?.toLowerCase()

  const VERTICAL_NAME = verticalObject?.name?.toLowerCase()

  const PLANT_NAME_NO_CASE = plantObject?.name
  const SITE_NAME_NO_CASE = siteObject?.name
  const VERTICAL_NAME_NO_CASE = verticalObject?.name
  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}`

  const IS_PE_PP =
    verticalObject?.name?.toLowerCase() == 'pe' ||
    verticalObject?.name?.toLowerCase() == 'pp'

  const IS_PTA = verticalObject?.name?.toLowerCase() == 'pta'
  const IS_PTA_DMD = IS_PTA && siteObject?.name?.toLowerCase() == 'dmd'

  const IS_VCM = verticalObject?.name?.toLowerCase() == 'vcm'
  const SITE_NAME = siteObject?.name?.toLowerCase()
  const IS_PET = verticalObject?.name?.toLowerCase() == 'pet'
  const IS_VCM_DMD_VCM = IS_VCM && SITE_NAME == 'dmd' && PLANT_NAME == 'vcm'

  const headerMap = generateHeaderNames(AOP_YEAR)
  const [rows, setRows] = useState()
  const [rowsPercentageSummary, setRowsPercentageSummary] = useState()
  const [rowsFormattedAndNonEditable, setRowsFormattedAndNonEditable] =
    useState()

  const valueFormat_ = ValueFormatterProduction()

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [unitDesignCapacity, setUnitDesignCapacity] = useState('TPH')
  const [unitMaxCapacity, setUnitMaxCapacity] = useState('TPH')
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [loading, setLoading] = useState(false)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [remarkDialogOpenDesignCapacity, setRemarkDialogOpenDesignCapacity] =
    useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRemarkDesignCapacity, setCurrentRemarkDesignCapacity] =
    useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [currentRowIdDesignCapacity, setCurrentRowIdDesignCapacity] =
    useState(null)
  const [startDate, setStartDate] = useState(null)
  const [endDate, setEndDate] = useState(null)
  const dispatch = useDispatch()
  const [rowsDesignCapacity, setRowsDesignCapacity] = useState([])
  const [rowsMaxCapacity, setRowsMaxCapacity] = useState([])
  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const handleRemarkCellClickDesignCapacity = (row) => {
    if (READ_ONLY) return
    setCurrentRemarkDesignCapacity(row.remarks || '')
    setCurrentRowIdDesignCapacity(row.id)
    setRemarkDialogOpenDesignCapacity(true)
  }

  const findAvg = (value, row) => {
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

    const values = months.map((month) => row[month] || 0)
    const sum = values.reduce((acc, val) => acc + val, 0)
    const avg = sum / values.length

    return avg === '0.00' ? null : avg
  }

  const editAOPMCCalculatedData = async (newRows) => {
    setLoading(true)
    try {
      const isTPH = selectedUnit == 'TPD'

      const aopmccCalculatedData = newRows.map((row) => ({
        april: isTPH && row.april ? row.april / 24 : row.april || null,
        may: isTPH && row.may ? row.may / 24 : row.may || null,
        june: isTPH && row.june ? row.june / 24 : row.june || null,
        july: isTPH && row.july ? row.july / 24 : row.july || null,
        august: isTPH && row.august ? row.august / 24 : row.august || null,
        september:
          isTPH && row.september ? row.september / 24 : row.september || null,
        october: isTPH && row.october ? row.october / 24 : row.october || null,
        november:
          isTPH && row.november ? row.november / 24 : row.november || null,
        december:
          isTPH && row.december ? row.december / 24 : row.december || null,
        january: isTPH && row.january ? row.january / 24 : row.january || null,
        february:
          isTPH && row.february ? row.february / 24 : row.february || null,
        march: isTPH && row.march ? row.march / 24 : row.march || null,

        financialYear: AOP_YEAR,
        plantFKId: PLANT_ID,
        siteFKId: SITE_ID,
        materialFKId: row.normParametersFKId,
        verticalFKId: VERTICAL_ID,
        id: row.idFromApi || null,
        avgTPH: findAvg('1', row) || null,
        remark: row.remarks,
        remarks: row.remarks,
      }))

      const response =
        await ProductionVolumeDataApiService.editAOPMCCalculatedData(
          aopmccCalculatedData,
          PLANT_ID,
          AOP_YEAR,
          keycloak,
        )

      if (response) {
        dispatch(setIsBlocked(false))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        // setEdit({})

        const responseForNorms =
          await DataService.calculateNormsHistorianValues(
            PLANT_ID,
            AOP_YEAR,
            startDate,
            endDate,
            keycloak,
          )

        setLoading(false)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Please fill all fields, try again!',
          severity: 'error',
        })
        setLoading(false)
      }
      fetchData()
      return response
    } catch (error) {
      console.error('Error saving Data:', error)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  const editDesignCapacityData = async (newRows) => {
    setLoading(true)
    try {
      const isTPH = unitDesignCapacity === 'TPD'

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

      const designCapacityData = newRows.map((row) => {
        const mapped = { id: row.idFromApi || null }
        months.forEach((month) => {
          mapped[month] =
            isTPH && row[month] ? row[month] / 24 : row[month] || null
        })
        mapped.remarks = row.remarks || row.remark || ''
        mapped.materialFKId = row.normParametersFKId || row.materialFKId || null
        mapped.productName = row.productName || row.materialDisplayName || null
        return mapped
      })

      const response =
        await ProductionVolumeDataApiService.editDesignCapacityData(
          designCapacityData,
          PLANT_ID,
          AOP_YEAR,
          keycloak,
        )

      if (response && response.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsDesignCapacity({})
        setEnableSaveAddBtnDesignCapacity(false)
        fetchDesignCapacityData(unitDesignCapacity)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Please fill all fields, try again!',
          severity: 'error',
        })
      }
      setLoading(false)
      return response
    } catch (error) {
      console.error('Error saving Design Capacity:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving Design Capacity!',
        severity: 'error',
      })
      setLoading(false)
    }
  }
  const saveChangesDesignCapacity = React.useCallback(async () => {
    try {
      const data = Object.values(modifiedCellsDesignCapacity)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
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

      editDesignCapacityData(data)
      setEnableSaveAddBtnDesignCapacity(false)
    } catch (error) {
      console.log('Facing issue at saving data', error)
    }
  }, [modifiedCellsDesignCapacity, unitDesignCapacity])

  //
  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCells)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

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

      const invalidRows = data.filter((row) => {
        if (!row.normParametersFKId || !row.normParametersFKId.trim()) {
          return true
        }

        for (const month of months) {
          const value = row[month]
          if (
            value === 0 ||
            value === null ||
            (typeof value === 'string' && !value.trim())
          ) {
            return true
          }
        }

        const remarkValue = row.remark || row.remarks
        const originalRemarkValue =
          row.originalRemark || row.originalRemarks || ''

        if (
          !remarkValue ||
          (typeof remarkValue === 'string' && !remarkValue.trim()) ||
          remarkValue.trim() === originalRemarkValue.trim()
        ) {
          return true
        }

        return false
      })

      if (invalidRows.length > 0) {
        setSnackbarData({
          message:
            'Please fill all fields in edited row and update the Remark!',
          severity: 'error',
        })
        setSnackbarOpen(true)
        return
      } else {
        editAOPMCCalculatedData(data)
      }
      setEnableSaveAddBtn(false)
    } catch (error) {
      console.log('Facing issue at saving data', error)
    }
  }, [modifiedCells, selectedUnit])

  const fetchData = async (unit = selectedUnit) => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    setModifiedCellsDesignCapacity({})
    setEnableSaveAddBtnDesignCapacity({})
    setModifiedCells({})
    // setEdit({})

    try {
      setLoading(true)
      const response =
        await ProductionVolumeDataApiService.getAOPMCCalculatedData(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      if (response?.code != 200) {
        setRows([])
        setLoading(false)
        return
      }
      setCalculationObject(response?.data?.aopCalculation)
      var formattedData = response?.data?.aopMCCalculatedDataDTOList.map(
        (item, index) => {
          const isTPH = selectedUnit == 'TPD'
          return {
            ...item,
            idFromApi: item?.id || null,
            normParametersFKId: item?.materialFKId.toLowerCase(),
            remarks: item?.remarks?.trim() || null,
            originalRemark: item?.remarks?.trim() || null,

            id: index,

            ...(isTPH && {
              april: item.april ? item.april * 24 : item.april || null,
              may: item.may ? item.may * 24 : item.may || null,
              june: item.june ? item.june * 24 : item.june || null,
              july: item.july ? item.july * 24 : item.july || null,
              august: item.august ? item.august * 24 : item.august || null,
              september: item.september
                ? item.september * 24
                : item.september || null,
              october: item.october ? item.october * 24 : item.october || null,
              november: item.november
                ? item.november * 24
                : item.november || null,
              december: item.december
                ? item.december * 24
                : item.december || null,
              january: item.january ? item.january * 24 : item.january || null,
              february: item.february
                ? item.february * 24
                : item.february || null,
              march: item.march ? item.march * 24 : item.march || null,
            }),
          }
        },
      )

      const formulatedData = normalizeAllRows(formattedData)

      const nonEditableRows = formulatedData.map((item) => ({
        ...item,
        isEditable: false,
      }))
      var formattedDataNONEDITABLE = formattedData.map((item) => ({
        ...item,
        isEditable: false,
      }))

      formattedData = formattedData.map((item) => ({
        ...item,
        remarks: item.remarks ? item.remarks.trim() : '',
      }))
      setRowsPercentageSummary(nonEditableRows)
      setRows(formattedData)
      setRowsFormattedAndNonEditable(formattedDataNONEDITABLE)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  function formatDate(date) {
    if (!date) return ''
    const year = date?.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const fetchConfiguration = async () => {
    try {
      setLoading(true)
      const configData = await DataService.getConfigurationExecutionDetails(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (configData?.code !== 200) return

      const StartDate = configData.data.find(
        (d) => d.Name === 'StartDate',
      )?.AttributeValue
      const EndDate = configData.data.find(
        (d) => d.Name === 'EndDate',
      )?.AttributeValue

      if (!StartDate || !EndDate) {
        const today = new Date()
        const endDate = new Date(today.getFullYear(), today.getMonth(), 0)
        const startDate = new Date(today.getFullYear() - 5, today.getMonth(), 1)

        setStartDate(formatDate(startDate))
        setEndDate(formatDate(endDate))
      } else {
        setStartDate(StartDate)
        setEndDate(EndDate)
      }
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  function normalizeAllRows(grid) {
    const monthKeys = [
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

    return grid?.map((row) => {
      // 1. Find this row?s max month value
      const vals = monthKeys?.map((k) => Number(row[k]))
      const maxVal = Math.max(...vals)

      // 2. Shallow-clone the entire row (carries over id, remarks, all FKs, etc.)
      const newRow = { ...row }

      // 3. Overwrite only the month fields:
      monthKeys.forEach((key) => {
        const orig = Number(row[key] || 0)
        const pct = maxVal ? (orig / maxVal) * 100 : 0
        newRow[key] = Number(pct)
      })

      return newRow
    })
  }

  const valueFormat = IS_VCM ? '{0:0.000}' : valueFormat_

  const colDefs_percentage_summary = IS_PE_PP
    ? getColDefsPercentageSummaryPEPP(headerMap, valueFormat)
    : getColDefsPercentageSummary(headerMap, valueFormat)

  const colDefs_design_capacity =
    IS_PE_PP || IS_PET
      ? getColDefsDesignCapacityPEPP(headerMap, valueFormat)
      : IS_PTA_DMD
        ? getColDefsDesignCapacityPTADMD(headerMap, valueFormat)
        : IS_PTA
          ? getColDefsDesignCapacityPTA(headerMap, valueFormat)
          : getColDefsDesignCapacity(headerMap, valueFormat)

  const colDefs_max_achieved_capacity =
    IS_PE_PP || IS_PET
      ? getColDefsMaxAchievedCapacityPEPP(headerMap, valueFormat)
      : IS_PTA
        ? getColDefsMaxAchievedCapacityPTA(headerMap, valueFormat)
        : getColDefsMaxAchievedCapacity(headerMap, valueFormat)

  const colDefs_non_editable = getColDefsNonEditable(headerMap, valueFormat)

  useEffect(() => {
    setModifiedCellsDesignCapacity({})
    setEnableSaveAddBtnDesignCapacity({})
    setModifiedCells({})

    fetchData()

    fetchConfiguration()
  }, [oldYear, yearChanged, keycloak, selectedUnit, PLANT_ID])

  const colDefs_editable = getEnhancedProductionColDefs({
    headerMap,
    valueFormat,
  })

  const handleUnitChangeDesignCapacity = (unit) => {
    setUnitDesignCapacity(unit)
  }

  const handleUnitChangeMaxCapacity = (unit) => {
    setUnitMaxCapacity(unit)
    setUnitDesignCapacity(unit)
    setSelectedUnit(unit)
  }

  const handleUnitChangeMain = (unit) => {
    setSelectedUnit(unit)
  }

  const handleCalculate = () => {
    if (VERTICAL_NAME == 'meg' || VERTICAL_NAME == 'elastomer') {
      handleCalculateMeg()
    } else {
      // handleCalculatePe()
    }
  }

  const fetchDesignCapacityData = async (unit = unitDesignCapacity) => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    setLoading(true)
    try {
      const response =
        await ProductionVolumeDataApiService.getDesignCapacityData(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      let data = response?.data?.aopMCCalculatedDataDTOList
      if (data && !Array.isArray(data)) {
        data = [data]
      }
      if (response?.code === 200 && data) {
        const isTPD = unit === 'TPD'
        const formatted = data.map((item, index) => ({
          ...item,
          id: index + 1,
          idFromApi: item?.id || null,
          productName: item?.materialDisplayName,
          remarks: item?.remarks?.trim() || null,
          originalRemark: item?.remarks?.trim() || null,
          remark: item.remarks?.trim() || '',
          isEditable: IS_PE_PP || IS_PET || IS_VCM || IS_PTA_DMD ? false : true,

          april:
            isTPD && item.april ? item.april * 24 : item.april || item.april,
          may: isTPD && item.may ? item.may * 24 : item.may || item.may,
          june: isTPD && item.june ? item.june * 24 : item.june || item.june,
          july: isTPD && item.july ? item.july * 24 : item.july || item.july,
          august:
            isTPD && item.august
              ? item.august * 24
              : item.august || item.august,
          september:
            isTPD && item.september
              ? item.september * 24
              : item.september || item.september,
          october:
            isTPD && item.october
              ? item.october * 24
              : item.october || item.october,
          november:
            isTPD && item.november
              ? item.november * 24
              : item.november || item.november,
          december:
            isTPD && item.december
              ? item.december * 24
              : item.december || item.december,
          january:
            isTPD && item.january
              ? item.january * 24
              : item.january || item.january,
          february:
            isTPD && item.february
              ? item.february * 24
              : item.february || item.february,
          march:
            isTPD && item.march ? item.march * 24 : item.march || item.march,
        }))
        setRowsDesignCapacity(formatted)
      } else {
        setRowsDesignCapacity([])
      }
    } catch (error) {
      console.error('Error fetching Design Capacity:', error)
      setRowsDesignCapacity([])
    } finally {
      setLoading(false)
    }
  }
  const fetchMaxCapacityData = async (unit = unitMaxCapacity) => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    setLoading(true)
    try {
      const response =
        await ProductionVolumeDataApiService.getMaxAchievedCapacityData(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      let data = response?.data?.aopMCCalculatedDataDTOList
      if (data && !Array.isArray(data)) {
        data = [data]
      }
      if (response?.code === 200 && data) {
        // Conversion logic
        const isTPD = unit === 'TPD'
        const formatted = data.map((item, index) => ({
          ...item,
          idFromApi: item?.id || null,
          productName: item?.materialDisplayName,
          april: isTPD && item.april ? item.april * 24 : item.april,
          may: isTPD && item.may ? item.may * 24 : item.may,
          june: isTPD && item.june ? item.june * 24 : item.june,
          july: isTPD && item.july ? item.july * 24 : item.july,
          august: isTPD && item.august ? item.august * 24 : item.august,
          september:
            isTPD && item.september ? item.september * 24 : item.september,
          october: isTPD && item.october ? item.october * 24 : item.october,
          november: isTPD && item.november ? item.november * 24 : item.november,
          december: isTPD && item.december ? item.december * 24 : item.december,
          january: isTPD && item.january ? item.january * 24 : item.january,
          february: isTPD && item.february ? item.february * 24 : item.february,
          march: isTPD && item.march ? item.march * 24 : item.march,
          isEditable: false,
        }))
        setRowsMaxCapacity(formatted)
      } else {
        setRowsMaxCapacity([])
      }
    } catch (error) {
      console.error('Error fetching Max Achieved Capacity:', error)
      setRowsMaxCapacity([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchDesignCapacityData(unitDesignCapacity)
  }, [unitDesignCapacity, PLANT_ID, yearChanged, keycloak])

  useEffect(() => {
    fetchMaxCapacityData(unitMaxCapacity)
  }, [unitMaxCapacity, PLANT_ID, yearChanged, keycloak])

  useEffect(() => {
    fetchData()
    fetchConfiguration()
  }, [oldYear, yearChanged, keycloak, selectedUnit, PLANT_ID])

  const handleCalculateMeg = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    try {
      const data =
        await ProductionVolumeDataApiService.handleCalculateProductionVolData(
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
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Failed!',
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

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      allAction: false,
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

  //POINT-1 Current MCU to be rename as Max Achieved capacity.
  const percentageTitle =
    IS_PE_PP || IS_PET
      ? // ? 'Current MCU'
        'Max Achieved Capacity'
      : VERTICAL_NAME === 'cracker'
        ? 'Max Achieved Capacity (Ethylene)'
        : 'Max Achieved Capacity'
  const adjustedPermissionsGrid1 = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? false,
      allAction: permissions?.allAction ?? true,
      addButton: permissions?.addButton ?? false,
      deleteButton: permissions?.deleteButton ?? false,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      showRefreshBtn: permissions?.showRefreshBtn ?? true,
      saveBtn: false,
      units: ['TPH', 'TPD'],
      // downloadExcelBtn: permissions?.hideDownloadExcel ? false : true,
      titleName: percentageTitle,

      showTitleAndInformation:
        VERTICAL_NAME == 'cracker' || VERTICAL_NAME == 'vcm' ? true : false,
      titleAndInformation:
        VERTICAL_NAME == 'cracker'
          ? 'Maximum Ethylene Production achieved in the last 05 years historical data for 05 consecutive days in different furnace mode of operation.'
          : VERTICAL_NAME == 'vcm'
            ? `Maximum ${PLANT_NAME_NO_CASE} production achieved in the last five year historical data derived as average of top 10 percent data points.`
            : '',

      showTitleNameBusiness:
        VERTICAL_NAME !== 'cracker' && VERTICAL_NAME !== 'vcm' ? true : false,

      downloadExcelBtnFromUI: IS_PE_PP ? false : true,
      ExcelName: `${EXCEL_EXPORT_TITLE}_Max Achieved Capacity`,
    },
    isOldYear,
  )

  const adjustedPermissionsGrid2 = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? false,
      allAction: permissions?.allAction ?? true,
      addButton: permissions?.addButton ?? false,
      deleteButton: permissions?.deleteButton ?? false,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? true,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      showRefreshBtn: permissions?.showRefreshBtn ?? true,
      saveBtn: IS_PE_PP || IS_PET || IS_VCM || IS_PTA_DMD ? false : true,
      units: ['TPH', 'TPD'],

      // downloadExcelBtn: permissions?.hideDownloadExcel ? false : true,
      downloadExcelBtnFromUI: IS_PE_PP ? false : true,
      downloadExcelBtn: IS_PE_PP ? true : false,
      uploadExcelBtn: IS_PE_PP ? true : false,
      ExcelName: `${EXCEL_EXPORT_TITLE}_Design Capacity`,

      showTitleAndInformation:
        VERTICAL_NAME == 'cracker' || VERTICAL_NAME == 'vcm' ? true : false,
      titleAndInformation:
        VERTICAL_NAME == 'cracker'
          ? 'Design plant capacity for different furnace mode of operation as per licensor provided data.'
          : VERTICAL_NAME == 'vcm'
            ? 'Design plant capacity as per licensor provided data.'
            : '',

      showTitleNameBusiness:
        VERTICAL_NAME !== 'cracker' && VERTICAL_NAME !== 'vcm' ? true : false,

      titleName:
        VERTICAL_NAME === 'cracker'
          ? 'Design Capacity (Ethylene)'
          : VERTICAL_NAME === 'pp' && SITE_NAME === 'nmd'
            ? 'Design Capacity (MCU from MCU Portal)'
            : 'Design Capacity',
    },
    isOldYear,
  )

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? false,
      allAction: permissions?.allAction ?? true,
      addButton: permissions?.addButton ?? false,
      deleteButton: permissions?.deleteButton ?? false,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      showRefreshBtn: permissions?.showRefreshBtn ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      units: ['TPH', 'TPD'],
      showCalculate: permissions?.hideSummary ? false : VERTICAL_NAME === 'meg',
      showCalculateVisibility:
        VERTICAL_NAME === 'meg' &&
        Object.keys(calculationObject || {}).length > 0
          ? true
          : false,
      downloadExcelBtn: IS_PE_PP ? false : true,
      uploadExcelBtn: IS_PE_PP ? false : true,

      showTitleAndInformation:
        VERTICAL_NAME == 'cracker' || VERTICAL_NAME == 'vcm' ? true : false,

      //TEXT NOTE CHANGED TO 01 YEARS
      titleAndInformation:
        VERTICAL_NAME == 'cracker'
          ? 'Maximum Ethylene Production achieved in the last 01 years historical data for 05 consecutive days in different furnace mode of operation.'
          : VERTICAL_NAME == 'vcm'
            ? 'Steady state production operating capacity which is proposed for the AOP FY.'
            : '',

      showTitleNameBusiness:
        VERTICAL_NAME !== 'cracker' && VERTICAL_NAME !== 'vcm' ? true : false,
      titleName:
        VERTICAL_NAME === 'cracker'
          ? 'Proposed Operating Capacity (Ethylene)'
          : IS_VCM
            ? 'Steady State Operating Capacity'
            : 'Proposed Operating Capacity',
    },
    isOldYear,
  )

  const adjustedPermissionsLast = getAdjustedPermissions(
    {
      allAction: true,
      showTitleAndInformation:
        VERTICAL_NAME == 'cracker' || VERTICAL_NAME == 'vcm' ? true : false,
      titleAndInformation:
        VERTICAL_NAME == 'cracker'
          ? 'Percentage Summary (Ethylene)'
          : VERTICAL_NAME == 'vcm'
            ? `Percentage summary represent a month-wise percentage summary, comparing each months value against the highest ${PLANT_NAME_NO_CASE} production rate over the past 12 months.`
            : '',
      showTitleNameBusiness:
        VERTICAL_NAME !== 'cracker' && VERTICAL_NAME !== 'vcm' ? true : false,
      titleName:
        VERTICAL_NAME === 'cracker'
          ? 'Percentage Summary (Ethylene)'
          : !IS_PE_PP && !IS_PET
            ? 'Percentage Summary'
            : '% Summary of Proposed Operating Capacity',
    },
    isOldYear,
  )

  var colDefs_current_operating_capacity = permissions?.hideSummary
    ? colDefs_non_editable
    : colDefs_editable

  var rows1 = permissions?.hideSummary ? rowsFormattedAndNonEditable : rows

  const handleExcelUpload = (rawFile) => {
    saveExcelFile(rawFile)
  }
  const downloadExcelForConfiguration = async (gridType) => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      if (IS_PE_PP) {
        await ProductionVolumeDataApiService.getProductionVolExcelCommon(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          EXCEL_EXPORT_TITLE,
        )
      } else {
        if (gridType === 'design') {
          await ProductionVolumeDataApiService.getDesignCapacityExcel(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
            EXCEL_EXPORT_TITLE,
          )
        } else if (gridType === 'max') {
          await ProductionVolumeDataApiService.getMaxAchievedCapacityExcel(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
            EXCEL_EXPORT_TITLE,
          )
        } else {
          await ProductionVolumeDataApiService.getProductionVolExcel(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
            EXCEL_EXPORT_TITLE,
          )
        }
      }

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error!', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    }
  }
  const saveExcelFile = async (rawFile) => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return

    setLoading(true)
    try {
      const response =
        await ProductionVolumeDataApiService.saveProductionVolDataExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      if (response?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Upload Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        // setEdit({})

        const responseForNorms =
          await DataService.calculateNormsHistorianValues(
            PLANT_ID,
            AOP_YEAR,
            startDate,
            endDate,
            keycloak,
          )

        setLoading(false)

        // setLoading(false)

        fetchData()
      } else if (response?.code === 400 && response?.data) {
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
        link.setAttribute('download', 'Error File Production Vol Data.xlsx')
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
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  const conditionForFirst = !permissions?.hideSummary
  let max_achieved_capacity = []

  max_achieved_capacity = colDefs_max_achieved_capacity

  if (VERTICAL_NAME?.toLowerCase() == 'elastomer' && conditionForFirst) {
    return <ProductionTarget />
  }
  if (VERTICAL_NAME?.toLowerCase() == 'aromatics' && conditionForFirst) {
    return <AromaticsProductionGrids />
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* DESIGN_CAPACITY */}
      {conditionForFirst && (
        <KendoDataTables
          modifiedCells={modifiedCellsDesignCapacity}
          setModifiedCells={setModifiedCellsDesignCapacity}
          enableSaveAddBtn={enableSaveAddBtnDesignCapacity}
          setRows={setRowsDesignCapacity}
          columns={colDefs_design_capacity}
          rows={rowsDesignCapacity}
          paginationOptions={[100, 200, 300]}
          saveChanges={saveChangesDesignCapacity}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          apiRef={apiRef}
          fetchData={fetchDesignCapacityData}
          handleUnitChange={handleUnitChangeMaxCapacity}
          handleRemarkCellClick={handleRemarkCellClickDesignCapacity}
          experimentalFeatures={{ newEditingApi: true }}
          remarkDialogOpen={remarkDialogOpenDesignCapacity}
          setRemarkDialogOpen={setRemarkDialogOpenDesignCapacity}
          currentRemark={currentRemarkDesignCapacity}
          setCurrentRemark={setCurrentRemarkDesignCapacity}
          currentRowId={currentRowIdDesignCapacity}
          setEnableSaveAddBtn={setEnableSaveAddBtnDesignCapacity}
          permissions={adjustedPermissionsGrid2}
          selectedUnit={unitDesignCapacity}
          setSelectedUnit={setUnitDesignCapacity}
          supressGridHeight={true}
          downloadExcelForConfiguration={() =>
            downloadExcelForConfiguration('design')
          }
          handleExcelUpload={handleExcelUpload}
        />
      )}

      {/* MAX_ACHIEVED_CAPACITY */}
      {conditionForFirst && (
        <KendoDataTables
          setRows={setRowsMaxCapacity}
          columns={max_achieved_capacity}
          rows={rowsMaxCapacity}
          fetchData={fetchMaxCapacityData}
          permissions={adjustedPermissionsGrid1}
          selectedUnit={unitDesignCapacity}
          setSelectedUnit={setUnitDesignCapacity}
          handleUnitChange={handleUnitChangeMaxCapacity}
          supressGridHeight={true}
          downloadExcelForConfiguration={() =>
            downloadExcelForConfiguration('max')
          }
        />
      )}

      {/* CURRENT_OPERATING_CAPACITY */}
      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        enableSaveAddBtn={enableSaveAddBtn}
        setRows={setRows}
        columns={colDefs_current_operating_capacity}
        rows={rows1}
        paginationOptions={[100, 200, 300]}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        apiRef={apiRef}
        fetchData={fetchData}
        handleUnitChange={handleUnitChangeMaxCapacity}
        handleRemarkCellClick={handleRemarkCellClick}
        experimentalFeatures={{ newEditingApi: true }}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        handleCalculate={handleCalculate}
        permissions={adjustedPermissions}
        selectedUnit={unitDesignCapacity}
        setSelectedUnit={setUnitDesignCapacity}
        handleExcelUpload={handleExcelUpload}
        supressGridHeight={true}
        downloadExcelForConfiguration={() =>
          downloadExcelForConfiguration('main')
        }
      />

      {/* PERCENTAGE_SUMMARY */}
      {!permissions?.hideSummary && VERTICAL_NAME !== 'pta' && (
        <>
          <KendoDataTables
            setRows={setRowsPercentageSummary}
            columns={colDefs_percentage_summary}
            rows={rowsPercentageSummary}
            title='Production target Reference'
            fetchData={fetchData}
            permissions={adjustedPermissionsLast}
            supressGridHeight={true}
          />
        </>
      )}
    </div>
  )
}

export default ProductionvolumeData
