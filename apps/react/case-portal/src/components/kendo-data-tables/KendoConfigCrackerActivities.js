import {
  Backdrop,
  Box,
  CircularProgress,
  TextField,
  Button,
} from '@mui/material'
import React, { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import moment from '../../../node_modules/moment/moment.js'
// import { ibrGridThree, ibrPlanColumns } from './columnDefs'
import { ibrGridThree } from './columnDefs'
import FurnaceRunLengthGrid from './FurnaceRunLengthGrid.js'
import SDTAActivitiesGrid from './SDTAActivitiesGrid.js'
import { validateFields } from 'utils/validationUtils.js'
import { Height } from '../../../node_modules/@mui/icons-material/index.js'
import MaintenanceProcessTable from './processTable.js'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian.js'
import { Typography } from '../../../node_modules/@mui/material/index.js'
import { DatePicker } from '../../../node_modules/@progress/kendo-react-dateinputs/index'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment'
import { getRoleName } from 'services/role-service.js'
const DecokingConfig = () => {
  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)

  const tabs = ['IBR Plan']
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
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const SCREEN_NAME = screenTitle?.title
  const siteName = siteObject?.name?.toLowerCase()
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [activeTabIndex, setActiveTabIndex] = useState(0)
  const [remarkDialogOpenSdTa, setRemarkDialogOpenSdTa] = useState(false)
  const [currentRemarkSdTa, setCurrentRemarkSdTa] = useState('')
  const [currentRowIdSdTa, setCurrentRowId2] = useState(null)
  const [remarkDialogOpenRunLength, setRemarkDialogOpenRunLength] =
    useState(false)
  const [currentRemarkRunLength, setCurrentRemarkRunLength] = useState('')
  const [currentRowIdRunLength, setCurrentRowId3] = useState(null)
  const [calculationObject, setCalculationObject] = useState([])
  const [dateError, setDateError] = useState(false)
  //my chnage
  const [modifiedCellsSdTa, setModifiedCellsSdTa] = React.useState({})
  const [ibrScreen2Rows, setIbrScreen2Rows] = useState([])
  const [globalTaStartDate, setGlobalTaStartDate] = useState(null)
  const [globalTaEndDate, setGlobalTaEndDate] = useState(null)

  const [ibrPlanColumns, serIbrPlanColumns] = useState([])
  const [runLengthColumns, setRunLengthColumns] = useState([])
  useEffect(() => {
    if (!globalTaStartDate || !globalTaEndDate || ibrScreen2Rows.length === 0)
      return

    const updatedRows = ibrScreen2Rows.map((row) => ({
      ...row,
      taStartDate: globalTaStartDate,
      taEndDate: globalTaEndDate,
    }))

    setIbrScreen2Rows(updatedRows)

    // Update modified cells for saving
    const newModifiedCells = { ...modifiedCellsSdTa }
    updatedRows.forEach((row) => {
      newModifiedCells[row.id] = {
        ...newModifiedCells[row.id],
        ...row,
        taStartDate: globalTaStartDate,
        taEndDate: globalTaEndDate,
      }
    })
    setModifiedCellsSdTa(newModifiedCells)
  }, [globalTaStartDate, globalTaEndDate])

  const handleRemarkCellClick2 = (dataItem) => {
    if (READ_ONLY) return
    setCurrentRemarkSdTa(dataItem.remarks || '')
    setCurrentRowId2(dataItem.id)
    setRemarkDialogOpenSdTa(true)
  }
  const handleRemarkCellClickRunLength = (dataItem) => {
    if (READ_ONLY) return
    setCurrentRemarkRunLength(dataItem.remarks || '')
    setCurrentRowId3(dataItem.id)
    setRemarkDialogOpenRunLength(true)
  }
  const [ibrScreen1Rows, setIbrScreen1Rows] = useState([])

  const [ibrScreen3Rows, setIbrScreen3Rows] = useState([])
  const [runningDurationRows, setRunningDurationRows] = useState([])
  const [modifiedCellsRunLength, setModifiedCellsRunLength] = React.useState({})
  const allMonths = [
    'N/A',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
    'January',
    'February',
    'March',
  ].map((month) => ({ value: month, displayName: month }))

  const getRows = useCallback(
    (tab) => {
      if (tab === 'IBR Plan') {
        return { 1: ibrScreen1Rows, 2: ibrScreen2Rows, 3: ibrScreen3Rows }
      }
      if (tab === 'Running Duration') {
        return runningDurationRows
      }
      return []
    },
    [ibrScreen1Rows, ibrScreen2Rows, ibrScreen3Rows, runningDurationRows],
  )
  const setRowsForTab = useCallback((tab, data, screen = 1) => {
    if (tab === 'IBR Plan') {
      if (screen === 1) setIbrScreen1Rows(data)
      if (screen === 2) setIbrScreen2Rows(data)
      if (screen === 3) setIbrScreen3Rows(data)
    } else if (tab === 'Running Duration') {
      setRunningDurationRows(data)
    }
  }, [])

  function calcPreCoilReplacementRunLength(actualRunLength, reduction) {
    if (
      actualRunLength === null ||
      actualRunLength === undefined ||
      reduction === null ||
      reduction === undefined
    )
      return null
    const val =
      Number(actualRunLength) -
      (Number(actualRunLength) * Number(reduction)) / 100
    return isNaN(val) ? null : Math.ceil(val) // <-- round up to nearest integer
  }

  const fetchData = useCallback(
    async (screen = null) => {
      if (!PLANT_ID || !AOP_YEAR) return
      const currentTab = tabs[activeTabIndex]
      setLoading(true)
      try {
        if (currentTab === 'IBR Plan') {
          // Screen 1

          //THIS SCREEN IS HIDDEN

          // if (!screen || screen === 1) {
          //   const data1 = await DataService.getIbr(keycloak, PLANT_ID, AOP_YEAR)
          //   if (data1?.code === 200) {
          //     const processedData = data1.data
          //       .map((item, index) => ({
          //         ...item,
          //         idFromApi: item.id,
          //         id: index,
          //         month:
          //           item?.month === 'Invalid month'
          //             ? 'N/A'
          //             : item?.month || 'N/A',
          //       }))
          //       .sort((a, b) => b?.isMonthAdd - a?.isMonthAdd)
          //     setRowsForTab(currentTab, processedData, 1)
          //   } else {
          //     setRowsForTab(currentTab, [], 1)
          //   }
          // }

          // Screen 2
          if (!screen || screen === 2) {
            const data2 = await DataService.getIbrSdTa(
              keycloak,
              PLANT_ID,
              AOP_YEAR,
            )
            const toDateObject = (value) =>
              value ? moment(value, 'MMM D, YYYY').toDate() : null

            if (data2?.code === 200) {
              const dateColumns =
                data2?.data?.columns
                  ?.filter((col) => col.type === 'date')
                  ?.map((col) => col.field) || []

              const processedData1 = data2.data?.data.map((item, index) => ({
                ...item,
                idFromApi: item.id,
                id: index,
                // originalRemark: item?.remarks || '',
                // ibrStartDate: toDateObject(item.ibrStartDate),
                // ibrEndDate: toDateObject(item.ibrEndDate),
                // taStartDate: toDateObject(item.taStartDate),
                // taEndDate: toDateObject(item.taEndDate),
                // shutDownStartDate: toDateObject(item.shutDownStartDate),
                // shutDownEndDate: toDateObject(item.shutDownEndDate),
                // actualRunLength: item.actualRunLength || null,
                // reduction: item.reduction || null,
                // preCrDays: calcPreCoilReplacementRunLength(
                //   item.actualRunLength,
                //   item.reduction,
                // ),
              }))

              const processedData = data2.data?.data.map((item, index) => {
                const converted = {}

                dateColumns.forEach((field) => {
                  converted[field] = toDateObject(item[field])
                })

                return {
                  ...item,
                  ...converted,
                  idFromApi: item.Id,
                  id: index,
                }
              })

              serIbrPlanColumns(
                data2?.data?.columns?.map((col) => ({
                  ...col,
                  editable: true,
                })),
              )

              setRowsForTab(currentTab, processedData, 2)

              if (processedData && processedData.length > 0) {
                const taStart = processedData[0]?.taStartDate
                const taEnd = processedData[0]?.taEndDate

                setGlobalTaStartDate(taStart ? toDateObject(taStart) : null)
                setGlobalTaEndDate(taEnd ? toDateObject(taEnd) : null)
              }
            } else {
              setRowsForTab(currentTab, [], 2)
            }
          }

          // Screen 3 (sample/static)
          if (!screen || screen === 3) {
            const data3 = await DataService.getIbrScreen3(
              keycloak,
              PLANT_ID,
              AOP_YEAR,
            )
            const toDateObject = (value) =>
              value ? moment(value, 'YYYY-MM-DD').toDate() : null

            if (data3?.code === 200) {
              setCalculationObject(data3?.data?.aopCalculation)
              const dynamiccolumnDeckoking = (data3?.data?.columns || []).map((col) => ({
                ...col,
                editable: !['Id', 'Date', 'Month', 'AOPYear', 'Plant_FK_Id'].includes(col.field),
              }))
              setRunLengthColumns(dynamiccolumnDeckoking)

              // Use correct date format from API
              const toDateObject = (value) =>
                value ? moment(value, 'MMM D, YYYY').toDate() : null

              const processedData = (data3.data?.data || []).map((item, index) => {
                const row = { id: index }
                dynamiccolumnDeckoking.forEach(col => {
                  // Convert date fields if needed
                  if (col.type === 'date') {
                    row[col.field] = toDateObject(item[col.field])
                  } else {
                    row[col.field] = item[col.field]
                  }
                })
                row.idFromApi = item.Id
                return row
              })

              setRowsForTab(currentTab, processedData, 3)
            } else {
              setRowsForTab(currentTab, [], 3)
            }
            // setRowsForTab(currentTab, ibrGridThreeRowsSample, 3)
          }
        } else if (currentTab === 'Running Duration') {
          // setRowsForTab(currentTab, runningDurationRowsSample)
        }
      } catch (err) {
        console.error('Error loading data:', err)
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Failed to load data', severity: 'error' })
      } finally {
        setLoading(false)
      }
    },
    [activeTabIndex, keycloak, setRowsForTab, AOP_YEAR, PLANT_ID],
  )

  useEffect(() => {
    fetchData()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak, fetchData])

  function validateAllDateOverlaps(rows) {
    const pairs = [['IBR_SD', 'IBR_ED', 'IBR']]
    rows.forEach((row) => {
      row.isError = false
    })

    let overlapMessage = ''
    let foundOverlap = false

    // Compare every range in every row with every other range (including different types)
    for (let i = 0; i < rows.length; i++) {
      for (const [startA, endA, labelA] of pairs) {
        const aStart = rows[i][startA]
          ? new Date(rows[i][startA]).getTime()
          : null
        const aEnd = rows[i][endA] ? new Date(rows[i][endA]).getTime() : null
        if (!aStart || !aEnd) continue
        for (let j = 0; j < rows.length; j++) {
          for (const [startB, endB, labelB] of pairs) {
            const bStart = rows[j][startB]
              ? new Date(rows[j][startB]).getTime()
              : null
            const bEnd = rows[j][endB]
              ? new Date(rows[j][endB]).getTime()
              : null
            if (!bStart || !bEnd) continue
            // Skip same row and same activity
            if (i === j && labelA === labelB) continue
            if (aStart <= bEnd && bStart <= aEnd) {
              rows[i].isError = true
              rows[j].isError = true
              foundOverlap = true
              overlapMessage = `Furnace ${rows[i].displayName} ${labelA} overlaps Furnace ${rows[j].displayName} ${labelB}.`
              break
            }
          }
          if (foundOverlap) break
        }
        if (foundOverlap) break
      }
      if (foundOverlap) break
    }

    // Also check within a single row (column-wise)
    if (!foundOverlap) {
      for (let i = 0; i < rows.length; i++) {
        const ranges = pairs
          .map(([start, end, label]) => {
            const s = rows[i][start] ? new Date(rows[i][start]).getTime() : null
            const e = rows[i][end] ? new Date(rows[i][end]).getTime() : null
            return s && e ? { start: s, end: e, label } : null
          })
          .filter(Boolean)

        for (let m = 0; m < ranges.length; m++) {
          for (let n = m + 1; n < ranges.length; n++) {
            if (
              ranges[m].start <= ranges[n].end &&
              ranges[n].start <= ranges[m].end
            ) {
              rows[i].isError = true
              foundOverlap = true
              overlapMessage = `Furnace ${rows[i].displayName} ${ranges[m].label} overlaps Furnace ${rows[i].displayName} ${ranges[n].label}.`
              break
            }
          }
          if (foundOverlap) break
        }
        if (foundOverlap) break
      }
    }

    return foundOverlap
      ? { overlap: true, message: overlapMessage }
      : { overlap: false }
  }

  function rangesOverlap(startA, endA, startB, endB) {
    if (!startA || !endA || !startB || !endB) return false
    return startA <= endB && startB <= endA
  }

  // Check if TA dates overlap with IBR or Maintenance dates in any row
  function checkTaDateOverlapWithRows(taStart, taEnd, rows) {
    for (const row of rows) {
      const ibrStart = row.IBR_SD ? new Date(row.IBR_SD) : null
      const ibrEnd = row.IBR_ED ? new Date(row.IBR_ED) : null
      // const maintStart = row.shutDownStartDate ? new Date(row.shutDownStartDate) : null
      // const maintEnd = row.shutDownEndDate ? new Date(row.shutDownEndDate) : null

      if (rangesOverlap(taStart, taEnd, ibrStart, ibrEnd)) {
        return `TA dates overlap with IBR dates for Furnace ${row.displayName || row.id + 1}.`
      }
    }
    return null
  }

  const saveChangesSdTa = async () => {
    if (globalTaStartDate && globalTaEndDate) {
      saveChangesSdTa1()
    } else saveChangesSdTa2()
  }

  const saveChangesSdTa2 = React.useCallback(async () => {
    try {
      const { startLimit, endLimit } = getAopYearLimits()

      setDateError(false)

      var rawData = Object.values(modifiedCellsSdTa)

      const dateFields = ['IBR_SD', 'IBR_ED']
      const allRows = [...ibrScreen2Rows] // get all rows, not just modified
      let hasDateError = false

      for (const record of allRows) {
        record.isError = false // reset previous errors
        for (const field of dateFields) {
          let dateValue = record[field]
          if (typeof dateValue === 'string') {
            // Only accept DD-MM-YYYY format
            const ddmmyyyyRegex = /^(\d{2})-(\d{2})-(\d{4})$/
            const match = dateValue.match(ddmmyyyyRegex)
            if (match) {
              // month is 1-based in JS Date
              const day = match[1],
                month = match[2],
                year = match[3]
              dateValue = new Date(`${year}-${month}-${day}T00:00:00`)
            } else {
              // Invalid format, mark as error
              dateValue = null
            }
          }
        }
      }

      const requiredFields = ['idFromApi']

      var rawData1 = getRows('IBR Plan')[2]
      // Overlap validation
      const result = validateAllDateOverlaps(rawData1)
      if (result.overlap) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: result.message,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      const validationMessage = validateFields(rawData, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      postIbr2(rawData)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCellsSdTa])

  const saveChangesSdTa1 = React.useCallback(async () => {
    try {
      const { startLimit, endLimit } = getAopYearLimits()
      // Validate TA dates only on Save
      if (
        !globalTaStartDate ||
        !globalTaEndDate ||
        (startLimit && globalTaStartDate < startLimit) ||
        (endLimit && globalTaStartDate > endLimit) ||
        (startLimit && globalTaEndDate < startLimit) ||
        (endLimit && globalTaEndDate > endLimit)
      ) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: `TA dates must be between ${formatDateDDMMYYYY(startLimit)} 
        and ${formatDateDDMMYYYY(endLimit)} for selected year.`,
          severity: 'error',
        })
        setDateError(true)
        setLoading(false)
        return
      }
      if (globalTaStartDate > globalTaEndDate) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Start date must be before or equal to End date.',
          severity: 'error',
        })
        setDateError(true)
        setLoading(false)
        return
      }
      setDateError(false)
      const taOverlapMsg = checkTaDateOverlapWithRows(
        globalTaStartDate,
        globalTaEndDate,
        ibrScreen2Rows,
      )
      if (taOverlapMsg) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: taOverlapMsg,
          severity: 'error',
        })
        setLoading(false)
        return
      }
      if (Object.keys(modifiedCellsSdTa).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      var rawData = Object.values(modifiedCellsSdTa)

      const dateFields = ['IBR_SD', 'IBR_ED']
      const allRows = [...ibrScreen2Rows] // get all rows, not just modified
      let hasDateError = false

      for (const record of allRows) {
        record.isError = false // reset previous errors
        for (const field of dateFields) {
          let dateValue = record[field]
          if (typeof dateValue === 'string') {
            // Only accept DD-MM-YYYY format
            const ddmmyyyyRegex = /^(\d{2})-(\d{2})-(\d{4})$/
            const match = dateValue.match(ddmmyyyyRegex)
            if (match) {
              // month is 1-based in JS Date
              const day = match[1],
                month = match[2],
                year = match[3]
              dateValue = new Date(`${year}-${month}-${day}T00:00:00`)
            } else {
              // Invalid format, mark as error
              dateValue = null
            }
          }
        }
      }

      const requiredFields = ['idFromApi']

      var rawData1 = getRows('IBR Plan')[2]
      // Overlap validation
      const result = validateAllDateOverlaps(rawData1)
      if (result.overlap) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: result.message,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      const validationMessage = validateFields(rawData, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }
      postIbr(rawData)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCellsSdTa])

  const saveChangesRunLength = React.useCallback(async () => {
    try {
      saveCrackerRunLength(Object.values(modifiedCellsRunLength))
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCellsRunLength])

  const postIbr = async (newRow) => {
    setLoading(true)
    try {
      const formatIfDate = (value) => {
        if (!value) return ''
        const parsed = moment.utc(
          value,
          ['MMM D, YYYY', 'MMM D, YYYY, h:mm:ss A'],
          true,
        )
        return parsed.isValid()
          ? new Date(parsed.add(1, 'day').format('YYYY-MM-DD'))
          : value
      }

      const payload = newRow.map((row) => ({
        id: row?.idFromApi || null,
        ibrStartDate: formatIfDate(row?.ibrStartDate) || null,
        ibrEndDate: formatIfDate(row?.ibrEndDate) || null,
        taStartDate: formatIfDate(row?.taStartDate) || null,
        taEndDate: formatIfDate(row?.taEndDate) || null,
        shutDownStartDate: formatIfDate(row?.shutDownStartDate) || null,
        shutDownEndDate: formatIfDate(row?.shutDownEndDate) || null,
        preCrDays:
          row?.actualRunLength != null && row?.reduction != null
            ? Math.ceil(
                Number(row.actualRunLength) -
                  (Number(row.actualRunLength) * Number(row.reduction)) / 100,
              )
            : null,
        postCrDays: row?.postCrDays ? Number(row.postCrDays) : null,
        remarks: row.remarks || '',
        isCr: row?.isCr ? true : false,
        actualRunLength: row?.actualRunLength
          ? Number(row.actualRunLength)
          : null,
        reduction: row?.reduction ? Number(row.reduction) : null,
      }))

      const response = await DataService.postIbr(
        PLANT_ID,
        payload,
        keycloak,
        AOP_YEAR,
      )

      if (response?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsSdTa({})
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
    } finally {
      fetchData(2)
      fetchData(3)
      setLoading(false)
    }
  }
  const postIbr2 = async (newRow) => {
    setLoading(true)
    try {
      const formatIfDate = (value) => {
        if (!value) return ''
        const parsed = moment.utc(
          value,
          ['MMM D, YYYY', 'MMM D, YYYY, h:mm:ss A'],
          true,
        )
        return parsed.isValid()
          ? new Date(parsed.add(1, 'day').format('YYYY-MM-DD'))
          : value
      }

      // const payload = newRow.map((row) => ({
      //   id: row?.idFromApi || null,
      //   ibrStartDate: formatIfDate(row?.ibrStartDate) || null,
      //   ibrEndDate: formatIfDate(row?.ibrEndDate) || null,
      //   taStartDate: null,
      //   taEndDate: null,
      //   shutDownStartDate: formatIfDate(row?.shutDownStartDate) || null,
      //   shutDownEndDate: formatIfDate(row?.shutDownEndDate) || null,
      //   preCrDays:
      //     row?.actualRunLength != null && row?.reduction != null
      //       ? Math.ceil(
      //           Number(row.actualRunLength) -
      //             (Number(row.actualRunLength) * Number(row.reduction)) / 100,
      //         )
      //       : null,
      //   postCrDays: row?.postCrDays ? Number(row.postCrDays) : null,
      //   remarks: row.remarks || '',
      //   isCr: row?.isCr ? true : false,
      //   actualRunLength: row?.actualRunLength
      //     ? Number(row.actualRunLength)
      //     : null,
      //   reduction: row?.reduction ? Number(row.reduction) : null,
      // }))

      const response = await DataService.postIbr(
        PLANT_ID,
        newRow,
        keycloak,
        AOP_YEAR,
      )

      if (response?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsSdTa({})
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
    } finally {
      fetchData(2)
      fetchData(3)
      setLoading(false)
    }
  }

  const saveCrackerRunLength = async (newRow) => {
    setLoading(true)
    try {
      const apiFields = runLengthColumns.map(col => col.field)

    // Build payload using only those fields
    const payload = newRow.map((row) => {
      const obj = {}
      apiFields.forEach((field) => {
        let value = row[field]
        // Format date fields if needed
        const colDef = runLengthColumns.find(col => col.field === field)
        if (colDef?.type === 'date' && value instanceof Date) {
          value = `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, '0')}-${String(value.getDate()).padStart(2, '0')}`
        }
        obj[field] = value ?? null
      })
      return obj
    })
      const response = await DataService.saveCrackerRunLength(
        PLANT_ID,
        payload,
        keycloak,
        AOP_YEAR,
      )
      if (response?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsRunLength({})
        setLoading(false)
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
      fetchData(3)
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
      showAccordian: true,
    }
  }
  const adjustedPermissionsSdTa = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: true,
      saveBtn: true,
      allAction: true,
      showTitleName: true,
      showAccordian: true,
      showCalculate: false,
    },
    isOldYear,
  )
  const adjustedPermissionsRunLength = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: true,
      saveBtn: true,
      allAction: true,
      showTitleName: true,
      showAccordian: true,
      showCalculate: true,
      // showCalculateVisibility:
      //   Object.keys(calculationObject || {}).length > 0 ? true : false,

      //BUTTON SHOULD BE DISABLED FOR NOW , LATER WE NEED TO CHANGE THE LOGIC
      showCalculateVisibility: false,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      byDefCollaps: false,
      showTitleNameBusiness: true,
      titleName: '',
    },
    isOldYear,
  )

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
      await DataService.getRunLengthExcel(keycloak, PLANT_ID, AOP_YEAR)
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
    } finally {
      // optional cleanup or logging
    }
  }

  const saveExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      var response
      response = await DataService.saveRunLengthExcel(
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
        setLoading(false)
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
        link.setAttribute('download', 'Error File Run Length.xlsx')
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
      fetchData(3)
      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }
  const handleCalculateSdTa = async () => {
    setLoading(true)
    try {
      const data = await DataService.handleCalculateSdTaActivities(
        PLANT_ID,
        AOP_YEAR,
        keycloak,
      )

      if (data?.code == 200) {
        fetchData(2) // Refresh screen 2 data
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'SDTA data refreshed successfully!',
          severity: 'success',
        })
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Failed to refresh SDTA data!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error calculating SDTA data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error occurred while refreshing data!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }
  const handleCalculate = async () => {
    setLoading(true)
    try {
      const data = await DataService.handleCalculateDecokingActivities(
        PLANT_ID,
        AOP_YEAR,
        keycloak,
      )
      if (data?.code == 200) {
        fetchData(3)
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
  function getAopYearLimits() {
    const yearStr = AOP_YEAR // e.g. "2025-26"
    let startLimit, endLimit
    if (yearStr) {
      const [startYear, endYear] = yearStr
        .split('-')
        .map((y) => parseInt(y.trim(), 10))
      if (!isNaN(startYear) && !isNaN(endYear)) {
        startLimit = new Date(`${startYear}-04-01T00:00:00`)
        // If endYear is 2 digits, prefix with 20
        const endYearFull = endYear < 100 ? 2000 + endYear : endYear
        endLimit = new Date(`${endYearFull}-03-31T23:59:59`)
      }
    }
    return { startLimit, endLimit }
  }

  // ...existing code...

  // Validation for TA dates

  function formatDateDDMMYYYY(date) {
    if (!(date instanceof Date) || isNaN(date)) return ''
    const d = date.getDate().toString().padStart(2, '0')
    const m = (date.getMonth() + 1).toString().padStart(2, '0')
    const y = date.getFullYear()
    return `${d}/${m}/${y}`
  }

  const rowClass = (row) => (row.isError ? 'row-error' : '')
  const filteredIbrGridThree =
    siteName === 'dmd'
      ? ibrGridThree.filter((col) => col.field !== 'demo')
      : ibrGridThree

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <LocalizationProvider dateAdapter={AdapterMoment}>
        <Box sx={{ display: 'flex', gap: 1, mb: 0, alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography className='grid-title' sx={{ whiteSpace: 'nowrap' }}>
              TA Start Date
            </Typography>
            <DatePicker
              id='global-ta-start-date'
              format='dd-MM-yyyy'
              value={globalTaStartDate}
              onChange={(e) => setGlobalTaStartDate(e.value)}
              style={{ height: '80px' }}
              size={'small'}
              disabled={READ_ONLY}
            />
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography className='grid-title' sx={{ whiteSpace: 'nowrap' }}>
              TA End Date
            </Typography>
            <DatePicker
              id='global-ta-end-date'
              format='dd-MM-yyyy'
              value={globalTaEndDate}
              onChange={(e) => setGlobalTaEndDate(e.value)}
              style={{ height: '80px' }}
              size={'small'}
              disabled={READ_ONLY}
            />
          </Box>
        </Box>
      </LocalizationProvider>

      <SDTAActivitiesGrid
        columns={ibrPlanColumns}
        rows={getRows('IBR Plan')[2]}
        setRows={(data) => setRowsForTab('IBR Plan', data, 2)}
        fetchData={fetchData}
        handleRemarkCellClick={handleRemarkCellClick2}
        remarkDialogOpen={remarkDialogOpenSdTa}
        currentRemark={currentRemarkSdTa}
        setCurrentRemark={setCurrentRemarkSdTa}
        currentRowId={currentRowIdSdTa}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCellsSdTa}
        allMonths={allMonths}
        setModifiedCells={setModifiedCellsSdTa}
        permissions={adjustedPermissionsSdTa}
        saveChanges={saveChangesSdTa}
        setRemarkDialogOpen={setRemarkDialogOpenSdTa}
        rowClass={rowClass}
        handleCalculate={handleCalculateSdTa}
      />

      <FurnaceRunLengthGrid
        columns={runLengthColumns}
        rows={getRows('IBR Plan')[3]}
        setRows={(data) => setRowsForTab('IBR Plan', data, 3)}
        fetchData={fetchData}
        handleRemarkCellClick={handleRemarkCellClickRunLength}
        remarkDialogOpen={remarkDialogOpenRunLength}
        currentRemark={currentRemarkRunLength}
        setCurrentRemark={setCurrentRemarkRunLength}
        currentRowId={currentRowIdRunLength}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCellsRunLength}
        allMonths={allMonths}
        setModifiedCells={setModifiedCellsRunLength}
        permissions={adjustedPermissionsRunLength}
        saveChanges={saveChangesRunLength}
        setRemarkDialogOpen={setRemarkDialogOpenRunLength}
        handleExcelUpload={handleExcelUpload}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
        handleCalculate={handleCalculate}
      />

      <CustomAccordion defaultExpanded disableGutters>
        <CustomAccordionSummary
          aria-controls='meg-grid-content'
          id='meg-grid-header'
        >
          <Typography component='span' className='grid-title'>
            Maintenance Details
          </Typography>
        </CustomAccordionSummary>
        <CustomAccordionDetails>
          <Box sx={{ width: '100%', margin: 0 }}>
            <MaintenanceProcessTable />
          </Box>
        </CustomAccordionDetails>
      </CustomAccordion>
    </Box>
  )
}
export default DecokingConfig
