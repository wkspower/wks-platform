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
  const rawTabsStatic = ['Unit Capacity', 'Shutdown', 'Slowdown', 'ZCPP Shutdown', 'PCG Outlook', 'Crude Blend Window']
  const [tabs, setTabs] = useState(rawTabsStatic)
  const [tabIndex, setTabIndex] = useState(0)

  // Data states for each tab
  const [unitCapacityRows, setUnitCapacityRows] = useState([])
  const [shutdownRows, setShutdownRows] = useState([])
  const [slowdownRows, setSlowdownRows] = useState([])
  const [zcppShutdownRows, setZcppShutdownRows] = useState([])
  const [gasifierRows, setGasifierRows] = useState([])
  const [crudeBlendRows, setCrudeBlendRows] = useState([])
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
        ]

      case 'Slowdown':
        return [
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
        ]
      case 'ZCPP Shutdown':
      return [
        {
          title: "ZCPP Shutdown plan January'25 - March'26",
          children: [
            { field: 'jmdCpp', title: 'JMD-CPP', width: 120, editable: true },
            { field: 'ibrDueDate', title: 'IBR Due date', width: 120, editable: true },
            { title: 'GT maintenance', children: [
              { field: 'gtMaintenance', title: 'MI/HGPI/CI/Mods', width: 180, editable: true },
            ]},
            { field: 'noOfDays', title: 'No. of days', width: 100, editable: true },
            { field: 'shutdownDate', title: 'Shutdown date', width: 120, editable: true },
            { field: 'startupDate', title: 'Startup date', width: 120, editable: true },
            { field: 'majorJobs', title: 'Major jobs', width: 180, editable: true },
          ]
        }
      ]
      case 'PCG Outlook':
        return [
          { field: 'srno', title: 'SL.No', width: 60, editable: false },
          { field: 'particular', title: 'Product', width: 150, editable: true },
          { field: 'jan', title: 'Jan-25', width: 70, editable: true },
          { field: 'feb', title: 'Feb-25', width: 70, editable: true },
          { field: 'march', title: 'March-25', width: 70, editable: true },
          { field: 'apr', title: 'Apr-25', width: 70, editable: true },
          { field: 'may', title: 'May-25', width: 70, editable: true },
          { field: 'june', title: 'June-25', width: 70, editable: true },
          { field: 'july', title: 'July-25', width: 70, editable: true },
          { field: 'aug', title: 'Aug-25', width: 70, editable: true },
          { field: 'sep', title: 'Sep-25', width: 70, editable: true },
          { field: 'oct', title: 'Oct-25', width: 70, editable: true },
          { field: 'nov', title: 'Nov-25', width: 70, editable: true },
          { field: 'dec', title: 'Dec-25', width: 70, editable: true },
        ]
      case 'Crude Blend Window':
        return [
          {
            title: 'Crude Blend Window',
            children: [
          { field: 'no', title: 'No', width: 60, editable: false },
          { field: 'property', title: 'Property', width: 180, editable: true },
          { field: 'stream', title: 'Stream', width: 120, editable: true },
          { field: 'unit', title: 'Unit', width: 80, editable: true },
          { field: 'min', title: 'Min', width: 70, editable: true },
          { field: 'max', title: 'Max', width: 70, editable: true },
          { field: 'criticality', title: 'Criticality', width: 90, editable: true },
          { field: 'remarks', title: 'Remarks', width: 400, editable: true },
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
        case 'ZCPP Shutdown':
        return zcppShutdownRows
        case 'PCG Outlook':
          return gasifierRows
        case 'Crude Blend Window':
          return crudeBlendRows
        default:
          return []
      }
    },
    [unitCapacityRows, shutdownRows, slowdownRows, zcppShutdownRows, gasifierRows, crudeBlendRows],
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
      case 'ZCPP Shutdown':
      setZcppShutdownRows(data)
      break
      case 'PCG Outlook':
      setGasifierRows(data)
      break
      case 'Crude Blend Window':
      setCrudeBlendRows(data)
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
        if (!PLANT_NAME) return []
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
    if (PLANT_NAME === 'dta') {
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
              tentativeMonth: '2025-03-25',
              purposeOfSlowdown: 'Heater 1,2,3,4 Pigging',
            },
            {
              id: 2,
              units: 'Coker-1',
              tentativeDuration: 11.0,
              throughputDuringSlowdown: '170 KBPSD',
              tentativeMonth: '2025-08-25',
              purposeOfSlowdown: 'Heater 1,2,3,4 Pigging',
            },
            {
              id: 3,
              units: 'Coker-1',
              tentativeDuration: 6.0,
              throughputDuringSlowdown: '160 KBPSD',
              tentativeMonth: '2025-08-25',
              purposeOfSlowdown: 'Heater 5 Pigging',
            },
            {
              id: 4,
              units: 'Coker-1',
              tentativeDuration: 8.0,
              throughputDuringSlowdown: '206 KBPSD',
              tentativeMonth: '2025-02-25',
              purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
            },
            {
              id: 5,
              units: 'Coker-1',
              tentativeDuration: 8.0,
              throughputDuringSlowdown: '206 KBPSD',
              tentativeMonth: '2025-04-25',
              purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
            },
            {
              id: 6,
              units: 'Coker-1',
              tentativeDuration: 8.0,
              throughputDuringSlowdown: '206 KBPSD',
              tentativeMonth: '2025-06-25',
              purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
            },
            {
              id: 7,
              units: 'Coker-1',
              tentativeDuration: 8.0,
              throughputDuringSlowdown: '206 KBPSD',
              tentativeMonth: '2025-10-25',
              purposeOfSlowdown: 'Heater 5 Online Spalling of all 4 cells',
            },
          ]
        case 'ZCPP Shutdown':
        return [
          {
            id: 1,
            jmdCpp: 'GT-1/HRSG-1',
            ibrDueDate: '2025-01-23',
            gtMaintenance: 'MI',
            noOfDays: 66,
            shutdownDate: '2024-12-24',
            startupDate: '2025-02-28',
            majorJobs: 'HRSG Economiser & Side walls replacement'
          },
          {
            id: 2,
            jmdCpp: 'GT-6/HRSG-6',
            ibrDueDate: '2025-03-07',
            gtMaintenance: 'HGPI',
            noOfDays: 70,
            shutdownDate: '2025-03-01',
            startupDate: '2025-05-10',
            majorJobs: 'Rotor replacement, HRSG side walls, 70 days'
          },
          {
            id: 3,
            jmdCpp: 'GT-13/HRSG-13',
            ibrDueDate: '2025-03-14',
            gtMaintenance: 'CI',
            noOfDays: 11,
            shutdownDate: '2025-03-05',
            startupDate: '2025-03-16',
            majorJobs: 'HRSG side walls replacement'
          },
          {
            id: 4,
            jmdCpp: 'GT-4/HRSG-4',
            ibrDueDate: '2025-05-13',
            gtMaintenance: 'HGPI',
            noOfDays: 30,
            shutdownDate: '2025-03-17',
            startupDate: '2025-04-16',
            majorJobs: 'Turbine rotor replacement'
          }
        ]
        case 'PCG Outlook':
      return [
        {
          id: 1,
          srno: 1,
          particular: 'Gasifier Availability',
          jan: 2.9, feb: 2.8, march: 2.9, apr: 2.8, may: 2.9, june: 2.8,
          july: 2.9, aug: 2.8, sep: 2.9, oct: 2.8, nov: 2.9, dec: 2.8,
        },
        { id: 2, 
          srno: 2,
          particular: 'SinGas Production',
          jan: 6.0, feb: 6.0, march: 6.0, apr: 6.0, may: 6.0, june: 6.0,
          july: 6.0, aug: 6.0, sep: 6.0, oct: 6.0, nov: 6.0, dec: 6.0,
        }
      ]
      case 'Crude Blend Window':
          return [
            {
              id: 1, no: '1.1', property: 'API', stream: 'CDU feed', unit: 'degree', min: 26.0, max: '-', criticality: 2.0, remarks: 'Max acceptable API delta in successive crude blends change is 2 . For 330 KBPSD min API is 26 & for 345 KBPSD min API is 27.5'
            },
            {
              id: 2, no: '1.2', property: 'TAN', stream: 'CDU feed', unit: 'mg KOH/gm', min: 1.3, max: '', criticality: 1.0, remarks: 'Upper TAN to be targeted for 1.2 + 0.1 margin of PIMS error'
            },
            {
              id: 3, no: '1.3', property: 'Sulfur', stream: 'CDU feed', unit: 'Wt%', min: 1.1, max: 2.7, criticality: 1, remarks: '1. Lower limit is based on sulphur/TAN ratio with High TAN (>0.8) crude blend processing and CBA capacity.\n2. Considering AGTL Design for 2.7 WT%S @45.8 KTPD/326 KBPSD crude T\'put. At Higher T\'put of 330/335/340/345 Max S is limited at 2.66/2.63/2.59/2.55 WT%.'
            },
            {
              id: 4, no: '1.4', property: 'K. Visc. @40°C', stream: 'CDU feed', unit: 'cSt', min: '', max: 27, criticality: 2, remarks: 'Max limit:- for Desalter performance. (High viscosity impacts performance adversely).\nDTA RTF crude charge pumps are designed to handle max viscosity 25 cSt crude blend.'
            },
            {
              id: 5, no: '1.5', property: 'Asp to Resin ratio', stream: 'CDU feed', unit: '', min: '', max: 0.35, criticality: 2, remarks: 'While blending the Crudes having high Saturates (>50%), it is proposed to ensure the blend Colloidal Instability Index (CII) not to cross 1.0 and blend Asphaltene to Resin ratio remains less than 0.35.'
            },
            {
              id: 6, no: '1.6', property: 'BS&W', stream: 'CDU feed', unit: 'vol %', min: '', max: 1.0, criticality: 1.0, remarks: 'This parameter is critical to ensure smooth desalter performance & unit reliability.'
            },
            {
              id: 7, no: '1.7', property: 'Salts', stream: 'CDU feed', unit: 'ptb', min: '', max: 70.0, criticality: 1.0, remarks: 'This parameter is critical to ensure smooth desalter performance & unit reliability.'
            }
          ]
        default:
          return []
      }
    } else if (PLANT_NAME === 'sez') {
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
        case 'ZCPP Shutdown':
        return [
          {
            id: 1,
            jmdCpp: 'GT-4/HRSG-4',
            ibrDueDate: '2025-02-07',
            gtMaintenance: 'HGPI',
            noOfDays: 22,
            shutdownDate: '2025-02-01',
            startupDate: '2025-02-23',
            majorJobs: 'IBR'
          },
          {
            id: 2,
            jmdCpp: 'GT-1/HRSG-1',
            ibrDueDate: '2025-03-12',
            gtMaintenance: 'IBR',
            noOfDays: 11,
            shutdownDate: '2025-02-24',
            startupDate: '2025-03-07',
            majorJobs: 'IBR'
          },
          {
            id: 3,
            jmdCpp: 'GT-6/HRSG-6',
            ibrDueDate: '2025-04-13',
            gtMaintenance: 'CI',
            noOfDays: 14,
            shutdownDate: '2025-03-08',
            startupDate: '2025-03-23',
            majorJobs: 'IBR'
          },
          {
            id: 4,
            jmdCpp: 'GT-5/HRSG-5',
            ibrDueDate: '11-May-25',
            gtMaintenance: 'IBR',
            noOfDays: 11,
            shutdownDate: '2025-05-01',
            startupDate: '2025-05-12',
            majorJobs: 'IBR'
          },
          {
            id: 5,
            jmdCpp: 'GT-2/HRSG-2',
            ibrDueDate: '2025-08-20',
            gtMaintenance: 'IBR',
            noOfDays: 11,
            shutdownDate: '2025-06-28',
            startupDate: '2025-07-09',
            majorJobs: 'IBR'
          }
        ]
        case 'PCG Outlook':
      return [
        {
          id: 1,
          srno: 1,
          particular: 'Gasifier Availability',
          jan: 4.3, feb: 4.3, march: 4.3, apr: 4.3, may: 4.3, june: 4.3,
          july: 4.3, aug: 4.3, sep: 4.3, oct: 4.3, nov: 4.3, dec: 4.3,
        },
        { id: 2, 
          srno: 2,
          particular: 'SinGas Production',
          jan: 9.1, feb: 8.9, march: 9.1, apr: 8.9, may: 9.1, june: 8.9,
          july: 9.1, aug: 8.9, sep: 9.1, oct: 8.9, nov: 9.1, dec: 8.9,
        }
      ]
      case 'Crude Blend Window':
          return [
           {
            id: 1, no: '1.1', property: 'API', stream: 'CDU feed', unit: 'degree', min: 24.0, max: '', criticality: 2.0, remarks: 'Max acceptable °API delta in successive crude blends change is 2° API.\nMin and Max limits are applicable for 380 KBPSD\nFor 290 KBPSD design API is 24.'
          },
          {
            id: 2, no: '1.2', property: 'TAN', stream: 'CDU feed', unit: 'mg KOH/gm', min: 1.30, max: '', criticality: 1.0, remarks: 'Upper TAN to be targeted for 1.2 + 0.1 margin of PIMS error'
          },
          {
            id: 3, no: '1.3', property: 'Sulfur', stream: 'CDU feed', unit: 'Wt%', min: 1.1, max: 2.7, criticality: 1, remarks: "Max limit is 2.7wt% 'S' With Acid gas transfer to SEZ PCG @24000 Nm3/hr."
          },
          {
            id: 4, no: '1.4', property: 'K. Visc. @40°C', stream: 'CDU feed', unit: 'cSt', min: '', max: 42, criticality: 2, remarks: 'Max limit:-for Desalter performance.(High viscosity impacts the Desalter performance adversely)'
          }
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
    if (keycloak && PLANT_ID && currentTabDisplay && PLANT_NAME) {
      fetchTcsData(currentTabDisplay)
    }
  }, [
    tabIndex,
    fetchTcsData,
    keycloak,
    PLANT_ID,
    currentTabDisplay,
    yearChanged,
    lowerVertName,
    PLANT_NAME,
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
