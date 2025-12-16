import { Backdrop, Box, CircularProgress, Tab, Tabs } from '@mui/material'
import AopTabs from 'components/AopTabs'
import Notification from 'components/Utilities/Notification'
import { verticalEnums } from 'enums/verticalEnums'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { TcsApiService } from 'services/phase-two-services/tcsApiService'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import AdvanceKendoTable from '../AdvanceKendoTable/index'
import { getColumnsForTab, generateMockData } from './utility'

const TcsInput = () => {
  const keycloak = useSession()
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

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const PLANT_NAME = plantObject?.name?.toLowerCase()

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'crude'

  const headerMap = generateHeaderNames(AOP_YEAR)
  // State management
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const valueFormat= ValueFormatterProduction()
  // Tab management - store full tab objects
  const [tabObj, setTabObj] = useState([])
  const [tabIndex, setTabIndex] = useState(0)
  const [allTabsList, setAllTabsList] = useState([])
  const [visibleTabIds, setVisibleTabIds] = useState([])

  // Dynamic data storage for all tabs
  const [tabsData, setTabsData] = useState({})
  const [modifiedCells, setModifiedCells] = useState({})

  // Get current tab object (has id, displayName, displaySequence)
  const currentTab = tabObj[tabIndex] || {}
  // Get display name for UI
  const currentTabDisplay = currentTab.displayName || 'Unit Capacity'

  // console.log('tabObj:', tabObj)
  console.log('currentTab:', currentTab)
  // console.log('currentTabDisplay:', currentTabDisplay)

  // Fetch all tabs and visible tab IDs from backend
  useEffect(() => {
    fetchTabsData()
  }, [PLANT_ID, SITE_ID, VERTICAL_ID])

  // Load data when tab changes
  useEffect(() => {
    if (keycloak && PLANT_ID && currentTab.id && PLANT_NAME) {
      fetchTcsData(currentTab.id)
    }
  }, [
    tabIndex,
    keycloak,
    PLANT_ID,
    currentTab,
    PLANT_NAME,
  ])

  const fetchTabsData = async () => {
    try {
      if (!PLANT_ID || !SITE_ID || !VERTICAL_ID) return
      
      // First API: Get list of all tabs
      const allTabsResponse = await TcsApiService.getTcsAllTabs(keycloak)
      const allTabsList = allTabsResponse?.data?.configurationTypeList || []
      setAllTabsList(allTabsList)

      // Second API: Get array of tab IDs to show
      const visibleTabsResponse = await TcsApiService.getTcsVisibleTabs(keycloak, VERTICAL_ID, SITE_ID, PLANT_ID)
      let visibleTabIds = []
      if (visibleTabsResponse?.data) {
        visibleTabIds = typeof visibleTabsResponse.data === 'string' 
          ? JSON.parse(visibleTabsResponse.data) 
          : visibleTabsResponse.data
      }
      setVisibleTabIds(visibleTabIds)

      // Filter tabs to show only visible ones
      if (allTabsList && visibleTabIds && visibleTabIds.length > 0) {
        const visibleTabIdsLower = visibleTabIds.map(id => id.toLowerCase())
        const filteredTabs = allTabsList
          .filter(tab => visibleTabIdsLower.includes(tab.id.toLowerCase()))
          .sort((a, b) => a.displaySequence - b.displaySequence)
        setTabObj(filteredTabs)
      }
    } catch (err) {
      console.error('Error fetching tabs:', err)
      setSnackbarData({
        message: 'Failed to load tabs configuration',
        severity: 'error',
      })
      setSnackbarOpen(true)
    }
  }

  // Simplified: Store data for any tab dynamically
  const setRowsForTab = useCallback((tabId, data) => {
    setTabsData(prev => ({
      ...prev,
      [tabId]: data
    }))
  }, [])

  // Fetch data for current tab
  const fetchTcsData = useCallback(
    async (tabId) => {
      if (!tabId) return
      try {
        setLoading(true)
        let transformedData = []

        // Replace this with your actual API call
        // const response = await DataService.getTcsInputData(keycloak, tabId)

        // Mock data for demonstration - replace with actual API call
        const mockData = generateMockData(tabId)
        if (!PLANT_NAME) return []
        transformedData = mockData.map((item, index) => ({
          id: item.id || `row_${index}`,
          ...item,
        }))

        setRowsForTab(tabId, transformedData)
      } catch (err) {
        setSnackbarData({
          message: `Failed to load data. Please try again.`,
          severity: 'error',
        })
        setSnackbarOpen(true)
        setRowsForTab(tabId, [])
      } finally {
        setLoading(false)
      }
    },
    [keycloak, setRowsForTab],
  )



  // Get columns from utility - columns are now fetched from backend
  const columns = useMemo(() => getColumnsForTab(currentTab.id, headerMap, valueFormat), [currentTab.id, headerMap, valueFormat])

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const getRows = useCallback(
    (tabId) => {
      return tabsData[tabId] || []
    },
    [tabsData],
  )

  // Save changes
  const saveChanges = useCallback(async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      console.log('data',data)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }

      const requiredFields = ['Particulars']
      const validationMessage = validateFields(data, requiredFields)

      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({ message: validationMessage, severity: 'error' })
        setLoading(false)
        return
      }

      await saveTcsData(data)
    } catch (error) {
      console.error('Error saving changes:', error)
    }
  }, [modifiedCells, currentTab.id])

  const saveTcsData = async (newRows) => {
    setLoading(true)
    try {
      // Transform data for API
      const tcsInputData = newRows.map((row) => ({
        id: row.id,
        ...row,
      }))

      // Replace with actual API call
      // const response = await DataService.saveTcsInput(tcsInputData, keycloak, plantId, currentTab.id)

      // Mock response
      const response = { code: 200 }

      if (response.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Changes saved successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchTcsData(currentTab.id)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving TCS data!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error saving TCS Input data!', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving TCS data!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  // Excel upload handler
  const handleExcelUpload = (rawFile) => {
    // Implement excel upload logic
    console.log('Excel upload for:', currentTab.id, rawFile)
  }

  // Excel download handler
  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      // Replace with actual API call
      // const response = await DataService.exportTcsInputExcel(keycloak, currentTab.id)

      // Mock response
      const response = { code: 200 }

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Excel download completed successfully!',
          severity: 'success',
        })
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Failed to download Excel.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error downloading Excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    }
  }

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    addButton: true,
    remarksEditable: true,
    showCalculate: false,
    showExport: true,
    showImport: true,
    saveBtnForRemark: true,
    saveBtn: true,
    showWorkFlowBtns: true,
    showTitle: true,
    tabIndex: tabIndex,
  }
  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Tabs */}
      <Box sx={{ overflowX: 'auto', width: '100%' }}>
        <Tabs
          sx={{
            borderBottom: '0px solid #ccc',
            '.MuiTabs-indicator': { display: 'none' },
            margin: '0px 0px 0px 0px',
            minHeight: '28px',
          }}
          textColor='primary'
          indicatorColor='primary'
          value={tabIndex}
          onChange={(e, newIndex) => {
            if (newIndex >= 0 && newIndex < tabObj.length) {
              setTabIndex(newIndex)
            }
          }}
        >
          {tabObj.map((tab, index) => (
            <Tab
              key={tab.id}
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
              label={tab.displayName || tab.name}
            />
          ))}
        </Tabs>
      </Box>

      {/* Tab Content */}
      <Box>
        {(() => {
          const rows = getRows(currentTab.id)
          const setRowsForCurrent = useCallback(
            (newRows) => setRowsForTab(currentTab.id, newRows),
            [currentTab.id],
          )
          const columns = getColumnsForTab(currentTab.id,headerMap,valueFormat);

          return (
            <Box key={currentTab.id}>
              {/* <KendoDataTablesReportsTcs */}
              <AdvanceKendoTable
                rows={rows}
                setRows={setRowsForCurrent}
                fetchData={() => fetchTcsData(currentTab.id)}
                configType='tcs_input'
                handleRemarkCellClick={handleRemarkCellClick}
                columns={columns}
                remarkDialogOpen={remarkDialogOpen}
                setRemarkDialogOpen={setRemarkDialogOpen}
                currentRemark={currentRemark}
                setCurrentRemark={setCurrentRemark}
                currentRowId={currentRowId}
                saveChanges={saveChanges}
                snackbarData={snackbarData}
                snackbarOpen={snackbarOpen}
                setSnackbarOpen={setSnackbarOpen}
                setSnackbarData={setSnackbarData}
                modifiedCells={modifiedCells}
                setModifiedCells={setModifiedCells}
                handleExcelUpload={handleExcelUpload}
                downloadExcelForConfiguration={downloadExcelForConfiguration}
                permissions={permissions}
              />
            </Box>
          )
        })()}
      </Box>
    </Box>
  )
}

export default TcsInput
