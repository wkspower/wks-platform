import { Backdrop, Box, CircularProgress, Tab, Tabs } from '@mui/material'
import AopTabs from 'components/AopTabs'
import Notification from 'components/Utilities/Notification'
import { verticalEnums } from 'enums/verticalEnums'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import KendoDataTables from './index'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'

const TcsInput = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, oldYear, plantID, yearChanged } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'crude'
  const plantId = JSON.parse(localStorage.getItem('selectedPlant') || '{}')?.id
  const plantName = JSON.parse(
    localStorage.getItem('selectedPlant') || '{}',
  )?.name?.toLowerCase()

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

  // Tab management
  const rawTabsStatic = ['Unit Capacity', 'Shutdown', 'Slowdown']
  const [tabs, setTabs] = useState(rawTabsStatic)
  const [tabIndex, setTabIndex] = useState(0)

  // Data states for each tab
  const [unitCapacityRows, setUnitCapacityRows] = useState([])
  const [shutdownRows, setShutdownRows] = useState([])
  const [slowdownRows, setSlowdownRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})

  const currentTabDisplay = tabs[tabIndex] || 'Unit Capacity'

  // Column definitions for each tab
  const getColumnsForTab = (tabName) => {
    switch (tabName) {
      case 'Unit Capacity':
        return [
          {
            field: 'units',
            title: 'Units',
            widthT: 120,
            locked: true,
            editable: true,
            disable: false,
          },
          {
            title: 'Capacity',
            children: [
              {
                field: 'uom',
                title: 'UOM',
                editable: true,
              },
              {
                field: 'kbpsd',
                title: 'KBPSD',
                editable: true,
              },
              {
                field: 'remarks',
                title: 'Remarks',

                editable: true,
              },
            ],
          },
        ]

      case 'Shutdown':
        return [
          {
            field: 'units',
            title: 'Units',
            width: 150,
            locked: true,
            editable: true,
          },
          {
            title: 'DTA Complex',
            children: [
              {
                title: 'Major Units Shutdown details',
                children: [
                  {
                    field: 'sdTotalDuration',
                    title: 'SD Total duration in days',
                    width: 180,
                    editable: true,
                  },
                  {
                    field: 'tentativeMonth',
                    title: 'Tentative Month',
                    width: 150,
                    editable: true,
                  },
                  {
                    field: 'purposeOfShutdown',
                    title: 'Purpose of Shutdown',
                    width: 200,
                    editable: true,
                  },
                ],
              },
            ],
          },
        ]

      case 'Slowdown':
        return [
          {
            title: 'DTA Complex',
            children: [
              {
                title: 'Major Units Slowdown details',
                children: [
                  {
                    field: 'units',
                    title: 'Units',
                    width: 150,
                    locked: true,
                    editable: true,
                  },
                  {
                    field: 'tentativeDuration',
                    title: 'Tentative Duration in days',
                    width: 200,
                    editable: true,
                  },
                  {
                    field: 'throughputDuringSlowdown',
                    title: 'Throughput during the Slowdown',
                    width: 220,
                    editable: true,
                  },
                  {
                    field: 'tentativeMonth',
                    title: 'Tentative Month',
                    width: 150,
                    editable: true,
                  },
                  {
                    field: 'purposeOfSlowdown',
                    title: 'Purpose of Slowdown',
                    width: 200,
                    editable: true,
                  },
                ],
              },
            ],
          },
        ]

      default:
        return []
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const getRows = useCallback(
    (tabId) => {
      switch (tabId) {
        case 'Unit Capacity':
          return unitCapacityRows
        case 'Shutdown':
          return shutdownRows
        case 'Slowdown':
          return slowdownRows
        default:
          return []
      }
    },
    [unitCapacityRows, shutdownRows, slowdownRows],
  )

  const setRowsForTab = useCallback((tabId, data) => {
    switch (tabId) {
      case 'Unit Capacity':
        setUnitCapacityRows(data)
        break
      case 'Shutdown':
        setShutdownRows(data)
        break
      case 'Slowdown':
        setSlowdownRows(data)
        break
      default:
        console.warn('No state for tab:', tabId)
    }
  }, [])

  // Fetch data for current tab
  const fetchTcsData = useCallback(
    async (currentTabDisplay) => {
      if (!currentTabDisplay) return
      try {
        setLoading(true)
        let transformedData = []

        // Replace this with your actual API call
        // const response = await DataService.getTcsInputData(keycloak, currentTabDisplay)

        // Mock data for demonstration - replace with actual API call
        const mockData = generateMockData(currentTabDisplay)

        transformedData = mockData.map((item, index) => ({
          id: item.id || `row_${index}`,
          ...item,
        }))

        setRowsForTab(currentTabDisplay, transformedData)
      } catch (err) {
        setSnackbarData({
          message: `Failed to load ${currentTabDisplay} data. Please try again.`,
          severity: 'error',
        })
        setSnackbarOpen(true)
        setRowsForTab(currentTabDisplay, [])
      } finally {
        setLoading(false)
      }
    },
    [keycloak, setRowsForTab],
  )

  // Mock data generator - replace with actual API call
  // ...existing code...

  // Mock data generator - replace with actual API call
  const generateMockData = (tabName) => {
    if (plantName === 'dta') {
      switch (tabName) {
        case 'Unit Capacity':
          return [
            {
              id: 1,
              units: 'CDU1',
              uom: 'KBPSD',
              kbpsd: 345.0,
              remarks:
                'Unit capacity considered for min API of 27. L+N: CDU+ 7.4 KTPD max; CDU-2: 6.4 KTPD (Summer: March-Oct) & 7.4 KTPD max in winters (Nov-Feb)',
            },
            {
              id: 2,
              units: 'CDU2',
              uom: 'KBPSD',
              kbpsd: 345.0,
              remarks:
                'PCD: Max 24.2 KTPD VR: Max 14.5 KTPD, however HDT VR to Coker will be 13.6 KTPD max',
            },
            {
              id: 3,
              units: 'DHT1',
              uom: 'KBPSD',
              kbpsd: 80.0,
              remarks: 'Grade wise max. capacity : BS III: 100 kbpsd',
            },
            {
              id: 4,
              units: 'DHT2',
              uom: 'KBPSD',
              kbpsd: 100.0,
              remarks: 'BS - VI: D1: 80 KBPSD, D2: 60 KBPSD',
            },
            {
              id: 5,
              units: 'VGOHT1',
              uom: 'KBPSD',
              kbpsd: 104.5,
              remarks: '',
            },
            {
              id: 6,
              units: 'VGOHT2',
              uom: 'KBPSD',
              kbpsd: 104.5,
              remarks: '',
            },
            {
              id: 7,
              units: 'FCCU',
              uom: 'KBPSD',
              kbpsd: 215,
              remarks: '',
            },

            {
              id: 26,
              units: 'HPIB',
              uom: 'TPD',
              kbpsd: '1. HPIB : 409.5 TPD',
              remarks:
                '2. Butene-1 : 195 TPD at 130% capacity; 3. MTBE: 816 TPD',
            },
          ]
        case 'Shutdown':
          return [
            {
              id: 1,
              units: 'CDU#1',
              sdTotalDuration: 28.0,
              tentativeMonth: '05-Jan-25',
              purposeOfShutdown:
                'M&I Jobs / Vacuum Column Bed Replacement/ Heater tube pigging and replacement, APH replacement',
            },
            {
              id: 2,
              units: 'Sat LPG Merox-331',
              sdTotalDuration: 15.0,
              tentativeMonth: '05-Jan-25',
              purposeOfShutdown:
                'M&I and LPG/Amine OLS Normalization along with CDU',
            },
            {
              id: 3,
              units: 'KMU1',
              sdTotalDuration: 18.0,
              tentativeMonth: '05-Jan-25',
              purposeOfShutdown:
                'with CDU-1 for Reactor charcoal replacement and M&I',
            },
            {
              id: 4,
              units: 'KMU2',
              sdTotalDuration: 18.0,
              tentativeMonth: '20-Jan-25',
              purposeOfShutdown:
                'with CDU-1 for Reactor charcoal replacement and M&I',
            },
            {
              id: 5,
              units: 'CBA-3',
              sdTotalDuration: 31.0,
              tentativeMonth: '05-Jan-25',
              purposeOfShutdown:
                'R01/ R02/R03 catalyst replacement with new catalyst, WHBs tube cleaning & IBR inspection, HP steam line bootleg valve and trap replacement job, pit inspection',
            },
            {
              id: 6,
              units: 'SWS-3 With CDU-1 S/D',
              sdTotalDuration: 12.0,
              tentativeMonth: '05-Jan-25',
              purposeOfShutdown:
                'Feed Excahnger and reboilers tune cleaning and inspection.',
            },
            {
              id: 7,
              units: 'SWS-4 With CDU-1 S/D',
              sdTotalDuration: 12.0,
              tentativeMonth: '17-Jan-25',
              purposeOfShutdown:
                'Feed Excahnger and reboilers tune cleaning and inspection.',
            },
            {
              id: 8,
              units: 'PP Line-C',
              sdTotalDuration: 16.0,
              tentativeMonth: '05-Jan-25',
              purposeOfShutdown:
                'Reactor cleaning, Extruder overhauling (with FCC-2 slowdown)',
            },
            {
              id: 9,
              units: 'PP Line-B',
              sdTotalDuration: 16.0,
              tentativeMonth: '05-Jun-25',
              purposeOfShutdown:
                'Line B Cycle gas compressor overhauling and Reactor inspection & cleaning',
            },
            {
              id: 10,
              units: 'HPIB',
              sdTotalDuration: 27.0,
              tentativeMonth: '01-Nov-25',
              purposeOfShutdown:
                'MTBE secondary Reactor Catalyst replacement and M&I of HPC and other equipment',
            },
          ]
        case 'Slowdown':
          return [
            {
              id: 1,
              units: 'Coker-1',
              tentativeDuration: 11.0,
              throughputDuringSlowdown: '170 KBPSD',
              tentativeMonth: "Mar'25",
              purposeOfSlowdown: 'Heater 1,2,3,4 Pigging',
            },
            {
              id: 2,
              units: 'Coker-1',
              tentativeDuration: 11.0,
              throughputDuringSlowdown: '170 KBPSD',
              tentativeMonth: "Aug'25",
              purposeOfSlowdown: 'Heater 1,2,3,4 Pigging',
            },
            {
              id: 3,
              units: 'Coker-1',
              tentativeDuration: 6.0,
              throughputDuringSlowdown: '160 KBPSD',
              tentativeMonth: "Aug'25",
              purposeOfSlowdown: 'Heater 5 Pigging',
            },
            {
              id: 4,
              units: 'Coker-1',
              tentativeDuration: 8.0,
              throughputDuringSlowdown: '206 KBPSD',
              tentativeMonth: "Feb'25",
              purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
            },
            {
              id: 5,
              units: 'Coker-1',
              tentativeDuration: 8.0,
              throughputDuringSlowdown: '206 KBPSD',
              tentativeMonth: "Apr'25",
              purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
            },
            {
              id: 6,
              units: 'Coker-1',
              tentativeDuration: 8.0,
              throughputDuringSlowdown: '206 KBPSD',
              tentativeMonth: "Jun'25",
              purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
            },
            {
              id: 7,
              units: 'Coker-1',
              tentativeDuration: 8.0,
              throughputDuringSlowdown: '206 KBPSD',
              tentativeMonth: "Oct'25",
              purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
            },
          ]
        default:
          return []
      }
    } else if (plantName === 'sez') {
      switch (tabName) {
        case 'Unit Capacity':
          return [
            {
              id: 1,
              units: 'CDU1',
              uom: 'KBPSD',
              kbpsd: 345.0,
              remarks:
                'Unit capacity considered for min API of 27. L+N: CDU+ 7.4 KTPD max; CDU-2: 6.4 KTPD (Summer: March-Oct) & 7.4 KTPD max in winters (Nov-Feb)',
            },
            {
              id: 2,
              units: 'CDU2',
              uom: 'KBPSD',
              kbpsd: 345.0,
              remarks:
                'PCD: Max 24.2 KTPD VR: Max 14.5 KTPD, however HDT VR to Coker will be 13.6 KTPD max',
            },
            {
              id: 3,
              units: 'DHT1',
              uom: 'KBPSD',
              kbpsd: 80.0,
              remarks: 'Grade wise max. capacity : BS III: 100 kbpsd',
            },
            {
              id: 4,
              units: 'DHT2',
              uom: 'KBPSD',
              kbpsd: 100.0,
              remarks: 'BS - VI: D1: 80 KBPSD, D2: 60 KBPSD',
            },
            {
              id: 5,
              units: 'VGOHT1',
              uom: 'KBPSD',
              kbpsd: 104.5,
              remarks: '',
            },
            {
              id: 6,
              units: 'VGOHT2',
              uom: 'KBPSD',
              kbpsd: 104.5,
              remarks: '',
            },
            {
              id: 7,
              units: 'FCCU',
              uom: 'KBPSD',
              kbpsd: 215,
              remarks: '',
            },
            {
              id: 8,
              units: 'HPIB',
              uom: 'TPD',
              kbpsd: 409.5,
              remarks:
                '1. HPIB : 409.5 TPD; 2. Butene-1 : 195 TPD at 130% capacity; 3. MTBE: 816 TPD',
            },
          ]
        case 'Shutdown':
          return [
            {
              id: 1,
              units: 'CDU-3',
              uom: 'KBPSD',
              kbpsd: 380,
              remarks:
                'High H2S crude processing (More than 5%) leading to high free H2S (More than 15%) in crude column overhead resulting high NGC amp and limiting NGC capacity. - to be captured adequately',
            },
            {
              id: 2,
              units: 'CDU-4',
              uom: 'KBPSD',
              kbpsd: 380,
              remarks:
                'L+N: CDU-3: 10.1 KTPD max; CDU-4: 10.1 KTPD max; RCO: Max 29.1 KTPD; LVGO (including vac diesel) & HVGO recovery: Max 14.2 (CDU#3) 13.1 (CDU#4); VR: Max 16.6 KTPD',
            },
            {
              id: 3,
              units: 'DHDS-1',
              uom: 'KBPSD',
              kbpsd: 150,
              remarks: '',
            },
            {
              id: 4,
              units: 'DHDS-2',
              uom: 'KBPSD',
              kbpsd: 170,
              remarks: '',
            },
            {
              id: 5,
              units: 'VGOHT-3',
              uom: 'KBPSD',
              kbpsd: 125,
              remarks: '',
            },
            {
              id: 6,
              units: 'VGOHT-4',
              uom: 'KBPSD',
              kbpsd: 125,
              remarks: '',
            },
            {
              id: 7,
              units: 'FCCU',
              uom: 'KBPSD',
              kbpsd: 220,
              remarks: 'Note 1: (205K VGO/LSWR + 15K Naphtha)',
            },
            {
              id: 8,
              units: 'FCCU',
              uom: 'KBPSD',
              kbpsd: 215,
              remarks: 'Note 2: 215 KBPSD with VGO/LSWR',
            },
          ]
        case 'Slowdown':
          return [
            {
              id: 1,
              units: 'CDU3',
              tentativeDuration: 120.0,
              throughputDuringSlowdown: 360,
              tentativeMonth: '01-11-2024 to 28-02-2025',
              purposeOfSlowdown:
                'A05 finfan tubes replacement as per CES recommendation during winter season',
            },
            {
              id: 2,
              units: 'CDU3',
              tentativeDuration: 10.0,
              throughputDuringSlowdown: 360,
              tentativeMonth: 'Based on more fouling of Exchangers',
              purposeOfSlowdown:
                "05 no's HE cleaning planned (S10,S12A/B,S12C/D,S11A/B,S514) (RESPECTIVELY @ 1.7 KTPD throughput loss for the 10 days - FOR EACH INDIVIDUAL Exchanger)",
            },
            {
              id: 3,
              units: 'CDU4',
              tentativeDuration: 120.0,
              throughputDuringSlowdown: 360,
              tentativeMonth: '01-11-2024 to 28-02-2025',
              purposeOfSlowdown:
                'A05 finfan tubes replacement as per CES recommendation during winter season',
            },
            {
              id: 4,
              units: 'CDU4',
              tentativeDuration: 10.0,
              throughputDuringSlowdown: 365,
              tentativeMonth: 'Based on more fouling of Exchangers',
              purposeOfSlowdown:
                "06 no's HE cleaning planned (S15,S515,S13,S513, S14,S514) (RESPECTIVELY @ 2.1 KTPD for S15 & S515 Each & 1.7 KTPD throughput loss for S13,S14,S513,S514 - each of them - duration of maintenance for 10 days - FOR EACH INDIVIDUAL Exchanger)",
            },
            {
              id: 5,
              units: 'FCC-2',
              tentativeDuration: 12.5,
              throughputDuringSlowdown: 110,
              tentativeMonth: "Jan-Feb'25",
              purposeOfSlowdown:
                'Z414S151A/B(WGC-2 Inter Stage Cooler) – Leak rectification SPRT Rotor replacement',
            },
            {
              id: 6,
              units: 'Alkylation',
              tentativeDuration: 14.0,
              throughputDuringSlowdown: 7,
              tentativeMonth: "Aug'25",
              purposeOfSlowdown:
                '- Reactor R301/401 planned Scheduled M&I in Aug-Sept-25 and Reactor 601 Scheduled M&I in Oct-25 (Note: Each Rx requires 22 days & Between Each Rx SD start 4-5 days GAP required for effective decontamination )',
            },
            {
              id: 7,
              units: 'Alkylation',
              tentativeDuration: 8.0,
              throughputDuringSlowdown: 7.5,
              tentativeMonth: "Aug'25",
              purposeOfSlowdown: '',
            },
          ]
        default:
          return []
      }
    }
  }

  // ...existing code...

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

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }

      const requiredFields =
        currentTabDisplay === 'Unit Capacity' ? ['units'] : ['units']
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
  }, [modifiedCells, currentTabDisplay])

  const saveTcsData = async (newRows) => {
    setLoading(true)
    try {
      // Transform data for API
      const tcsInputData = newRows.map((row) => ({
        id: row.id,
        ...row,
      }))

      // Replace with actual API call
      // const response = await DataService.saveTcsInput(tcsInputData, keycloak, plantId, currentTabDisplay)

      // Mock response
      const response = { code: 200 }

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchTcsData(currentTabDisplay)
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
    console.log('Excel upload for:', currentTabDisplay, rawFile)
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
      // const response = await DataService.exportTcsInputExcel(keycloak, currentTabDisplay)

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

  // Load data when tab changes
  useEffect(() => {
    if (keycloak && plantId && currentTabDisplay) {
      fetchTcsData(currentTabDisplay)
    }
  }, [
    tabIndex,
    fetchTcsData,
    keycloak,
    plantId,
    currentTabDisplay,
    yearChanged,
  ])

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
            if (newIndex >= 0 && newIndex < tabs.length) {
              setTabIndex(newIndex)
            }
          }}
        >
          {tabs.map((tabName, index) => (
            <Tab
              key={tabName}
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
              label={tabName}
            />
          ))}
        </Tabs>
      </Box>

      {/* Tab Content */}
      <Box>
        {(() => {
          const rows = getRows(currentTabDisplay)
          const setRowsForCurrent = useCallback(
            (newRows) => setRowsForTab(currentTabDisplay, newRows),
            [currentTabDisplay],
          )
          const columns = getColumnsForTab(currentTabDisplay)

          return (
            <Box key={currentTabDisplay}>
              <KendoDataTablesReports
                rows={rows}
                setRows={setRowsForCurrent}
                fetchData={() => fetchTcsData(currentTabDisplay)}
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
                permissions={{
                  customHeight: { mainBox: '32vh', otherBox: '100%' },
                  textAlignment: 'center',
                  remarksEditable: true,
                  showCalculate: false,
                  showExport: true,
                  saveBtnForRemark: true,
                  saveBtn: true,
                  showWorkFlowBtns: true,
                  showTitle: true,
                }}
              />
            </Box>
          )
        })()}
      </Box>
    </Box>
  )
}

export default TcsInput
