import { Backdrop, Box, CircularProgress } from '@mui/material'
import React, { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import moment from '../../../node_modules/moment/moment.js'
import { ibrGridThree, ibrPlanColumns } from './columnDefs'
import FurnaceRunLengthGrid from './FurnaceRunLengthGrid.js'
import SDTAActivitiesGrid from './SDTAActivitiesGrid.js'
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
  const [ibrScreen2Rows, setIbrScreen2Rows] = useState([])
  const [ibrScreen3Rows, setIbrScreen3Rows] = useState([])
  const [runningDurationRows, setRunningDurationRows] = useState([])
  const [modifiedCellsSdTa, setModifiedCellsSdTa] = React.useState({})
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
                remarks: item?.remarks || '',
                ibrStartDate: toDateObject(item.ibrStartDate),
                ibrEndDate: toDateObject(item.ibrEndDate),
                taStartDate: toDateObject(item.taStartDate),
                taEndDate: toDateObject(item.taEndDate),
                shutDownStartDate: toDateObject(item.shutDownStartDate),
                shutDownEndDate: toDateObject(item.shutDownEndDate),
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
              const fiscalMonthOrder = [
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
              ]

              const processedData = data3.data?.decokingActivitiesList
                .map((item, index) => ({
                  ...item,
                  month_: item?.month,
                  idFromApi: item?.id,
                  id: index,
                  remarks: item?.remarks || '',
                  date: toDateObject(item.date),
                }))
                .sort((a, b) => {
                  return (
                    fiscalMonthOrder.indexOf(a.month_) -
                    fiscalMonthOrder.indexOf(b.month_)
                  )
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
    [activeTabIndex, keycloak, setRowsForTab],
  )
  useEffect(() => {
    fetchData()
  }, [plantID, oldYear, yearChanged, keycloak, fetchData])

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

      postIbr(rawData)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCellsSdTa])

  const saveChangesRunLength = React.useCallback(async () => {
    try {
      if (Object.keys(modifiedCellsRunLength.updatedRows).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      const rawData = modifiedCellsRunLength.updatedRows || []
      if (rawData.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      saveCrackerRunLength(rawData)
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
        displayName: row?.displayName,
        name: row?.name,
        aopYear: localStorage.getItem('year'),
        plantFkId: row?.plantFkId,
        ibrStartDate: formatIfDate(row?.ibrStartDate) || '',
        ibrEndDate: formatIfDate(row?.ibrEndDate) || '',
        taStartDate: formatIfDate(row?.taStartDate) || '',
        taEndDate: formatIfDate(row?.taEndDate) || '',
        shutDownStartDate: formatIfDate(row?.shutDownStartDate) || '',
        shutDownEndDate: formatIfDate(row?.shutDownEndDate) || '',

        preCrDays: row?.preCrDays ? Number(row.preCrDays) : null,
        postCrDays: row?.postCrDays ? Number(row.postCrDays) : null,

        isCr: row?.isCr ? true : false,
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
        setModifiedCellsSdTa({})
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
      byDefCollaps: true,
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
  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <>
        <>
          {/* <SDTAActivitiesGrid
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
          /> */}
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
        </>
      </>
    </Box>
  )
}
export default DecokingConfig
