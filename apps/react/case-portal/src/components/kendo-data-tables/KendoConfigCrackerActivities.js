// DecokingConfig.jsx (refactored to mirror CrackerConfig patterns)
import {
  Backdrop,
  Box,
  CircularProgress,
  Tab,
  Tabs,
  Typography,
} from '@mui/material'
import React, { useCallback, useEffect, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import KendoDataTables from './index.js'
import { ibrGridOne, ibrGridThree, ibrPlanColumns } from './columnDefs'

import { useSelector } from 'react-redux'
import moment from '../../../node_modules/moment/moment.js'
import { ibrGridThreeRowsSample, runningDurationRowsSample } from './rowSamples'
import KendoDataTablesAccordian from './index-accordian.js'

const DecokingConfig = () => {
  const keycloak = useSession()
  // const tabs = ['IBR Plan', 'Running Duration']
  const tabs = ['IBR Plan']
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear, plantID } =
    dataGridStore
  const isOldYear = oldYear?.oldYear
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const [activeTabIndex, setActiveTabIndex] = useState(0)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const [remarkDialogOpen2, setRemarkDialogOpen2] = useState(false)
  const [currentRemark2, setCurrentRemark2] = useState('')
  const [currentRowId2, setCurrentRowId2] = useState(null)

  const [remarkDialogOpen3, setRemarkDialogOpen3] = useState(false)
  const [currentRemark3, setCurrentRemark3] = useState('')
  const [currentRowId3, setCurrentRowId3] = useState(null)
  const [calculationObject, setCalculationObject] = useState([])

  const handleRemarkCellClick = (dataItem) => {
    setCurrentRemark(dataItem.remarks || '')
    setCurrentRowId(dataItem.id)
    setRemarkDialogOpen(true)
  }

  const handleRemarkCellClick2 = (dataItem) => {
    setCurrentRemark2(dataItem.remarks || '')
    setCurrentRowId2(dataItem.id)
    setRemarkDialogOpen2(true)
  }

  const handleRemarkCellClick3 = (dataItem) => {
    setCurrentRemark3(dataItem.remarks || '')
    setCurrentRowId3(dataItem.id)
    setRemarkDialogOpen3(true)
  }

  // --- Rows State Per Tab ----------------------------------------------------
  const [ibrScreen1Rows, setIbrScreen1Rows] = useState([])
  const [ibrScreen2Rows, setIbrScreen2Rows] = useState([])
  const [ibrScreen3Rows, setIbrScreen3Rows] = useState([])
  const [runningDurationRows, setRunningDurationRows] = useState([])
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [modifiedCells2, setModifiedCells2] = React.useState({})
  const [modifiedCells3, setModifiedCells3] = React.useState({})

  // --- Get/Set Rows by Tab ---------------------------------------------------
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

  // --- Fetch Data ------------------------------------------------------------
  const fetchDataTest = useCallback(async () => {
    const currentTab = tabs[activeTabIndex]
    setLoading(true)
    try {
      if (currentTab === 'IBR Plan') {
        // screen 1
        const data1 = await DataService.getIbrScreen1(keycloak)
        if (data1?.code === 200) {
          const processedData = data1.data
            .map((item, index) => ({
              ...item,
              idFromApi: item.id,
              id: index,
              month:
                item?.month === 'Invalid month' ? 'N/A' : item?.month || 'N/A',
            }))
            .sort((a, b) => b?.isMonthAdd - a?.isMonthAdd)

          setRowsForTab(currentTab, processedData, 1)
        } else {
          setRowsForTab(currentTab, [], 1)
        }

        const data2 = await DataService.getIbrScreen2(keycloak)

        const toDateObject = (value) =>
          value ? moment(value, 'DD/MM/YYYY').toDate() : null

        if (data2?.code === 200) {
          const processedData = data2.data.map((item, index) => ({
            ...item,
            idFromApi: item.id,
            id: index,
            remarks: item?.remarks || '',

            ibrSD: toDateObject(item.ibrSD),
            ibrED: toDateObject(item.ibrED),
            taSD: toDateObject(item.taSD),
            taED: toDateObject(item.taED),
            sdSD: toDateObject(item.sdSD),
            sdED: toDateObject(item.sdED),
          }))
          setRowsForTab(currentTab, processedData, 2)
        } else {
          setRowsForTab(currentTab, [], 2)
        }

        // setRowsForTab(currentTab, ibrPlanRowsSample, 2)
        // screen 3
        // const data3 = await DataService.getIbrScreen3(keycloak)
        setRowsForTab(currentTab, ibrGridThreeRowsSample, 3)
      } else if (currentTab === 'Running Duration') {
        // const rd = await DataService.getRunningDuration(keycloak)
        setRowsForTab(currentTab, runningDurationRowsSample)
      }
    } catch (err) {
      console.error('Error loading data:', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Failed to load data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [activeTabIndex, keycloak, setRowsForTab])

  const fetchData = useCallback(
    async (screen = null) => {
      const currentTab = tabs[activeTabIndex]
      setLoading(true)
      try {
        if (currentTab === 'IBR Plan') {
          // Screen 1
          if (!screen || screen === 1) {
            const data1 = await DataService.getIbrScreen1(keycloak)
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
            const data2 = await DataService.getIbrScreen2(keycloak)

            const toDateObject = (value) =>
              value ? moment(value, 'DD/MM/YYYY').toDate() : null

            if (data2?.code === 200) {
              const processedData = data2.data.map((item, index) => ({
                ...item,
                idFromApi: item.id,
                id: index,
                remarks: item?.remarks || '',
                ibrSD: toDateObject(item.ibrSD),
                ibrED: toDateObject(item.ibrED),
                taSD: toDateObject(item.taSD),
                taED: toDateObject(item.taED),
                sdSD: toDateObject(item.sdSD),
                sdED: toDateObject(item.sdED),
              }))
              setRowsForTab(currentTab, processedData, 2)
            } else {
              setRowsForTab(currentTab, [], 2)
            }
          }

          // Screen 3 (sample/static)
          if (!screen || screen === 3) {
            const data3 = await DataService.getIbrScreen3(keycloak)

            // const toDateObject = (value) =>
            //   value ? moment(value, 'DD/MM/YYYY').toDate() : null

            if (data3?.code === 200) {
              setCalculationObject(data3?.data?.aopCalculation)

              const processedData = data3.data?.decokingActivitiesList.map(
                (item, index) => ({
                  ...item,
                  month_: item?.month,
                  idFromApi: item?.id,
                  id: index,
                  remarks: item?.remarks || '',
                }),
              )
              setRowsForTab(currentTab, processedData, 3)
            } else {
              setRowsForTab(currentTab, [], 3)
            }

            // setRowsForTab(currentTab, ibrGridThreeRowsSample, 3)
          }
        } else if (currentTab === 'Running Duration') {
          setRowsForTab(currentTab, runningDurationRowsSample)
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

  const saveChanges = React.useCallback(async () => {
    setLoading(true)
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var rawData = Object.values(modifiedCells)
      // const data = rawData.filter((row) => row.inEdit)

      // if (data.length == 0) {
      //   setSnackbarOpen(true)
      //   setSnackbarData({
      //     message: 'No Records to Save!',
      //     severity: 'info',
      //   })
      //   setLoading(false)
      //   return
      // }
      saveCracker(rawData)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCells])

  const saveChanges2 = React.useCallback(async () => {
    // setLoading(true)
    try {
      if (Object.keys(modifiedCells2).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var rawData = Object.values(modifiedCells2)
      const data = rawData.filter((row) => row.inEdit)

      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      // console.log(data)

      saveCracker2(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
    // }, 400)
  }, [modifiedCells2])

  const saveChanges3 = React.useCallback(async () => {
    // setLoading(true)
    try {
      if (Object.keys(modifiedCells3).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var rawData = Object.values(modifiedCells3)
      const data = rawData.filter((row) => row.inEdit)

      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      // console.log(data)

      saveCracker3(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
    // }, 400)
  }, [modifiedCells3])

  const monthMap = {
    January: 1,
    February: 2,
    March: 3,
    April: 4,
    May: 5,
    June: 6,
    July: 7,
    August: 8,
    September: 9,
    October: 10,
    November: 11,
    December: 12,
  }

  const saveCracker = async (newRow) => {
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
        days: row?.attributeValue,
        Month: row?.month,
        remarks: row?.remarks,
        isEditable: row?.isEditable,
        normParameterId: row?.normParameterId,
        aopMonth: monthMap[row?.month] || 0,
        id: row?.idFromApi || null,
      }))

      const response = await DataService.saveCracker(plantId, payload, keycloak)
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })

        setModifiedCells({})
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
      fetchData(1)
      setLoading(false)
    }
  }

  const saveCracker2 = async (newRow) => {
    setLoading(true)
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var payload = []
      const formatIfDate = (value) => {
        if (!value) return ''
        return value instanceof Date
          ? moment(value).format('DD/MM/YYYY')
          : value
      }

      payload = newRow.map((row) => ({
        furnace: row?.furnace || '',
        plantId: row?.plantId || '',

        ibrED: formatIfDate(row?.ibrED),
        ibrEDId: row?.ibrEDId || '',

        ibrSD: formatIfDate(row?.ibrSD),
        ibrSDId: row?.ibrSDId || '',

        sdED: formatIfDate(row?.sdED),
        sdEDId: row?.sdEDId || '',

        sdSD: formatIfDate(row?.sdSD),
        sdSDId: row?.sdSDId || '',

        taED: formatIfDate(row?.taED),
        taEDId: row?.taEDId || '',

        taSD: formatIfDate(row?.taSD),
        taSDId: row?.taSDId || '',
        remarks: row?.remarks || '',
      }))

      const response = await DataService.saveCracker2(
        plantId,
        payload,
        keycloak,
      )
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })

        setModifiedCells2({})
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
      fetchData(2)
      setLoading(false)
    }
  }

  const saveCracker3 = async (newRow) => {
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
        hTenProposed: row?.hTenProposed || '',
        hElevenProposed: row?.hElevenProposed || '',
        hTwelveProposed: row?.hTwelveProposed || '',
        hThirteenProposed: row?.hThirteenProposed || '',
        hFourteenProposed: row?.hFourteenProposed || '',
        plantId: row?.plantId || '',
        id: row?.idFromApi || '',
        demo: row?.demo || '',
      }))

      const response = await DataService.saveCracker3(
        plantId,
        payload,
        keycloak,
      )
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })

        setModifiedCells2({})
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

  const adjustedPermissions1 = getAdjustedPermissions(
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
  const adjustedPermissions2 = getAdjustedPermissions(
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
  const adjustedPermissions3 = getAdjustedPermissions(
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
    },
    isOldYear,
  )

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
        // setModifiedCells({})
        setLoading(false)
        fetchData(3)
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

  const renderIbrPlanTables = () => (
    <>
      <>
        <Box sx={{ mt: 2 }}>
          <KendoDataTablesAccordian
            columns={ibrGridOne}
            rows={getRows('IBR Plan')[1]}
            setRows={(data) => setRowsForTab('IBR Plan', data, 1)}
            fetchData={fetchData}
            handleRemarkCellClick={handleRemarkCellClick}
            remarkDialogOpen={remarkDialogOpen}
            currentRemark={currentRemark}
            setCurrentRemark={setCurrentRemark}
            currentRowId={currentRowId}
            snackbarData={snackbarData}
            snackbarOpen={snackbarOpen}
            setSnackbarOpen={setSnackbarOpen}
            setSnackbarData={setSnackbarData}
            modifiedCells={modifiedCells}
            allMonths={allMonths}
            setModifiedCells={setModifiedCells}
            permissions={adjustedPermissions1}
            saveChanges={saveChanges}
            setRemarkDialogOpen={setRemarkDialogOpen}
            titleName={'IBR Plan'}
          />
        </Box>

        <Box sx={{ mt: 2 }}>
          <KendoDataTablesAccordian
            columns={ibrPlanColumns}
            rows={getRows('IBR Plan')[2]}
            setRows={(data) => setRowsForTab('IBR Plan', data, 2)}
            fetchData={fetchData}
            handleRemarkCellClick={handleRemarkCellClick2}
            remarkDialogOpen={remarkDialogOpen2}
            currentRemark={currentRemark2}
            setCurrentRemark={setCurrentRemark2}
            currentRowId={currentRowId2}
            snackbarData={snackbarData}
            snackbarOpen={snackbarOpen}
            setSnackbarOpen={setSnackbarOpen}
            setSnackbarData={setSnackbarData}
            modifiedCells={modifiedCells2}
            allMonths={allMonths}
            setModifiedCells={setModifiedCells2}
            permissions={adjustedPermissions2}
            saveChanges={saveChanges2}
            setRemarkDialogOpen={setRemarkDialogOpen2}
            titleName={'SD / TA Activities'}
          />
        </Box>

        <Box sx={{ mt: 2 }}>
          <KendoDataTablesAccordian
            columns={ibrGridThree}
            rows={getRows('IBR Plan')[3]}
            setRows={(data) => setRowsForTab('IBR Plan', data, 3)}
            fetchData={fetchData}
            handleRemarkCellClick={handleRemarkCellClick3}
            remarkDialogOpen={remarkDialogOpen3}
            currentRemark={currentRemark3}
            setCurrentRemark={setCurrentRemark3}
            currentRowId={currentRowId3}
            snackbarData={snackbarData}
            snackbarOpen={snackbarOpen}
            setSnackbarOpen={setSnackbarOpen}
            setSnackbarData={setSnackbarData}
            modifiedCells={modifiedCells3}
            allMonths={allMonths}
            setModifiedCells={setModifiedCells3}
            permissions={adjustedPermissions3}
            saveChanges={saveChanges3}
            setRemarkDialogOpen={setRemarkDialogOpen3}
            titleName={'Furnace Run length'}
            handleExcelUpload={handleExcelUpload}
            downloadExcelForConfiguration={downloadExcelForConfiguration}
          />
        </Box>
      </>
    </>
  )

  // const renderRunningDurationTable = () => (
  //   <Box sx={{ mt: 2 }}>
  //     <Typography variant='h6'>Running Duration</Typography>
  //     <KendoDataTables
  //       columns={runningDurationColumns}
  //       rows={runningDurationRows}
  //       setRows={setRunningDurationRows}
  //       fetchData={fetchData}
  //       handleRemarkCellClick={handleRemarkCellClick}
  //       remarkDialogOpen={remarkDialog.open}
  //       setRemarkDialogOpen={(open) => setRemarkDialog((v) => ({ ...v, open }))}
  //       currentRemark={remarkDialog.remark}
  //       setCurrentRemark={(r) => setRemarkDialog((v) => ({ ...v, remark: r }))}
  //       currentRowId={remarkDialog.rowId}
  //       snackbarData={snackbarData}
  //       snackbarOpen={snackbarOpen}
  //       setSnackbarOpen={setSnackbarOpen}
  //       setSnackbarData={setSnackbarData}
  //       modifiedCells={modifiedCells}
  //       setModifiedCells={(m) => {
  //         /* implement setter */
  //       }}
  //     />
  //   </Box>
  // )

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* <Tabs value={activeTabIndex} onChange={(e, i) => setActiveTabIndex(i)}>
        {tabs.map((tab) => (
          <Tab key={tab} label={tab} />
        ))}
      </Tabs>

      {activeTabIndex === 0
        ? renderIbrPlanTables()
        : renderRunningDurationTable()} */}
      {renderIbrPlanTables()}
    </Box>
  )
}

export default DecokingConfig
