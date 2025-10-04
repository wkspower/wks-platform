import { Backdrop, Box, CircularProgress, TextField, Button } from '@mui/material'
import React, { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import moment from '../../../node_modules/moment/moment.js'
import { ibrGridThree, ibrPlanColumns } from './columnDefs'
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
const DecokingConfig = () => {
  const keycloak = useSession()
  const tabs = ['IBR Plan']
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { yearChanged, oldYear, plantID } = dataGridStore
  const isOldYear = oldYear?.oldYear
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
//my chnage 
  const [modifiedCellsSdTa, setModifiedCellsSdTa] = React.useState({})
  const [ibrScreen2Rows, setIbrScreen2Rows] = useState([])
  const [globalTaStartDate, setGlobalTaStartDate] = useState(null)
  const [globalTaEndDate, setGlobalTaEndDate] = useState(null)
  useEffect(() => {
  if (!globalTaStartDate || !globalTaEndDate || ibrScreen2Rows.length === 0) return

  const updatedRows = ibrScreen2Rows.map(row => ({
    ...row,
    taStartDate: globalTaStartDate,
    taEndDate: globalTaEndDate
  }))
  
  setIbrScreen2Rows(updatedRows)
  
  // Update modified cells for saving
  const newModifiedCells = { ...modifiedCellsSdTa }
  updatedRows.forEach(row => {
    newModifiedCells[row.id] = {
      ...newModifiedCells[row.id],
      ...row,
      taStartDate: globalTaStartDate,
      taEndDate: globalTaEndDate
    }
  })
  setModifiedCellsSdTa(newModifiedCells)
}, [globalTaStartDate, globalTaEndDate])
  const handleRemarkCellClick2 = (dataItem) => {
    setCurrentRemarkSdTa(dataItem.remarks || '')
    setCurrentRowId2(dataItem.id)
    setRemarkDialogOpenSdTa(true)
  }
  const handleRemarkCellClickRunLength = (dataItem) => {
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

  const fetchData = useCallback(
    async (screen = null) => {
      const currentTab = tabs[activeTabIndex]
      setLoading(true)
      try {
        if (currentTab === 'IBR Plan') {
          // Screen 1
          if (!screen || screen === 1) {
            const data1 = await DataService.getIbr(keycloak)
            if (data1?.code === 200) {
              const processedData = data1.data
                .map((item, index) => ({
                  ...item,
                  idFromApi: item.id,
                  id: index,
                  month:
                    item?.month === 'Invalid month'
                      ? 'N/A'
                      : item?.month || 'N/A',
                }))
                .sort((a, b) => b?.isMonthAdd - a?.isMonthAdd)
              setRowsForTab(currentTab, processedData, 1)
            } else {
              setRowsForTab(currentTab, [], 1)
            }
          }
          // Screen 2
          if (!screen || screen === 2) {
            const data2 = await DataService.getIbrSdTa(keycloak)
            const toDateObject = (value) =>
              value ? moment(value, 'MMM D, YYYY').toDate() : null

            if (data2?.code === 200) {
              const processedData = data2.data.map((item, index) => ({
                ...item,
                idFromApi: item.id,
                id: index,
                originalRemark: item?.remarks || '',
                ibrStartDate: toDateObject(item.ibrStartDate),
                ibrEndDate: toDateObject(item.ibrEndDate),
                taStartDate: toDateObject(item.taStartDate),
                taEndDate: toDateObject(item.taEndDate),
                shutDownStartDate: toDateObject(item.shutDownStartDate),
                shutDownEndDate: toDateObject(item.shutDownEndDate),
                actualRunLength: item.actualRunLength || null,
                reduction: item.reduction || null,
              }))

              setRowsForTab(currentTab, processedData, 2)
            } else {
              setRowsForTab(currentTab, [], 2)
            }
          }

          // Screen 3 (sample/static)
          if (!screen || screen === 3) {
            const data3 = await DataService.getIbrScreen3(keycloak)
            const toDateObject = (value) =>
              value ? moment(value, 'YYYY-MM-DD').toDate() : null

            if (data3?.code === 200) {
              setCalculationObject(data3?.data?.aopCalculation)

              const processedData = data3.data?.decokingActivitiesList.map(
                (item, index) => ({
                  ...item,
                  month_: item?.month,
                  idFromApi: item?.id,
                  id: index,
                  remarks: item?.remarks || '',
                  date: toDateObject(item.date),
                }),
              )

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
    [activeTabIndex, keycloak, setRowsForTab],
  )
  useEffect(() => {
    fetchData()
  }, [plantID, oldYear, yearChanged, keycloak, fetchData])

  function validateAllDateOverlaps(rows) {
    const pairs = [['ibrStartDate', 'ibrEndDate', 'IBR']]
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

  const saveChangesSdTa = React.useCallback(async () => {
    try {
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

      const dateFields = ['ibrStartDate', 'ibrEndDate']
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

          // if (
          //   startLimit &&
          //   endLimit &&
          //   (!dateValue || dateValue < startLimit || dateValue > endLimit)
          // ) {
          //   record.isError = true
          //   hasDateError = true
          // }
        }
      }

      // if (hasDateError) {
      //   setRowsForTab('IBR Plan', [...allRows], 2) // update all rows
      //   const formatDate = (date) =>
      //     `${date.getDate().toString().padStart(2, '0')}-${(date.getMonth() + 1)
      //       .toString()
      //       .padStart(2, '0')}-${date.getFullYear()}`
      //   setSnackbarOpen(true)
      //   setSnackbarData({
      //     message: `All dates must be between ${formatDate(startLimit)} and ${formatDate(endLimit)} for selected year.`,
      //     severity: 'error',
      //   })
      //   setLoading(false)
      //   return
      // }
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
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
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
        preCrDays: row?.preCrDays ? Number(row.preCrDays) : null,
        postCrDays: row?.postCrDays ? Number(row.postCrDays) : null,
        remarks: row.remarks || '',
        isCr: row?.isCr ? true : false,
        actualRunLength: row?.actualRunLength ? Number(row.actualRunLength) : null,
        reduction: row?.reduction ? Number(row.reduction) : null,
      }))

      const response = await DataService.postIbr(plantId, payload, keycloak)

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
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var payload = []
      payload = newRow.map((row) => ({
        tenProposed: row?.tenProposed || null,
        elevenProposed: row?.elevenProposed || null,
        twelveProposed: row?.twelveProposed || null,
        thirteenProposed: row?.thirteenProposed || null,
        fourteenProposed: row?.fourteenProposed || null,
        plantId: plantId,
        id: row?.idFromApi || '',
        demo: row?.demo || '',
      }))
      const response = await DataService.saveCrackerRunLength(
        plantId,
        payload,
        keycloak,
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
      showCalculate: true,
      showCalculateVisibility:
        Object.keys(calculationObject || {}).length > 0 ? true : false,
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
      showCalculateVisibility:
        Object.keys(calculationObject || {}).length > 0 ? true : false,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      byDefCollaps: false,
      showTitleNameBusiness: true,
      titleName: 'Furnace Actual and Proposed Runlength',
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
      await DataService.getRunLengthExcel(keycloak)
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
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var response
      response = await DataService.saveRunLengthExcel(rawFile, keycloak)
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
    const year = localStorage.getItem('year')
    const storedPlant = localStorage.getItem('selectedPlant')
    let plantId = ''
    if (storedPlant) {
      const parsedPlant = JSON.parse(storedPlant)
      plantId = parsedPlant.id
    }
    
    const data = await DataService.handleCalculateSdTaActivities(
      plantId,
      year,
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
      const year = localStorage.getItem('year')
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var plantId = plantId
      const data = await DataService.handleCalculateDecokingActivities(
        plantId,
        year,
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
  const rowClass = (row) => (row.isError ? 'row-error' : '')
  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      
      <LocalizationProvider dateAdapter={AdapterMoment}>
  <Box sx={{ display: 'flex', gap: 2, mb: 2, alignItems: 'center' }}>
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Typography
        className='grid-title'
        sx={{ whiteSpace: 'nowrap' }}
      >
        TA Start Date
      </Typography>
      <DatePicker
        id='global-ta-start-date'
        format='dd-MM-yyyy'
        value={globalTaStartDate}
        onChange={(e) => setGlobalTaStartDate(e.value)}
        style={{ height: '80px' }}
        size={'medium'}
      />
    </Box>
    
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Typography
        className='grid-title'
        sx={{ whiteSpace: 'nowrap' }}
      >
        TA End Date
      </Typography>
      <DatePicker
        id='global-ta-end-date'
        format='dd-MM-yyyy'
        value={globalTaEndDate}
        onChange={(e) => setGlobalTaEndDate(e.value)}
        style={{ height: '80px' }}
        size={'medium'}
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
        columns={ibrGridThree}
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
            Summary
          </Typography>
        </CustomAccordionSummary>
        <CustomAccordionDetails>
          <Box sx={{ width: '100%', margin: 0 }}>
            <MaintenanceProcessTable viewOnly={true} />
          </Box>
        </CustomAccordionDetails>
      </CustomAccordion>
    </Box>
  )
}
export default DecokingConfig
