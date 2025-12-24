import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { generateMockData, getColumnsForTab } from './utility'
import { Stack } from '../../../../../node_modules/@mui/material/index'

const CrudBlendWindow = ({
  PLANT_ID,
  AOP_YEAR,
  currentTab,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
}) => {
  const keycloak = useSession()
  const valueFormat = ValueFormatterProduction()

  // State management for Table 1 (Crude Blend Window)
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [apiMetadata1, setApiMetadata1] = useState({ headers: [], keys: [] })

  // State management for Table 2 (Crude Specific Constraints)
  const [rows2, setRows2] = useState([])
  const [modifiedCells2, setModifiedCells2] = useState({})
  const [remarkDialogOpen2, setRemarkDialogOpen2] = useState(false)
  const [currentRemark2, setCurrentRemark2] = useState('')
  const [currentRowId2, setCurrentRowId2] = useState(null)
  const [apiMetadata2, setApiMetadata2] = useState({ headers: [], keys: [] })

  // Detect numeric fields from data
  const getNumericKeysInAllRows = (rowsData = []) => {
    if (!Array.isArray(rowsData) || rowsData.length === 0) return []

    const allKeys = Array.from(
      rowsData.reduce((set, row) => {
        if (row && typeof row === 'object') {
          Object.keys(row).forEach((k) => set.add(k))
        }
        return set
      }, new Set()),
    )

    return allKeys.filter((key) =>
      rowsData.every((row) => {
        const v = row?.[key]
        if (v === undefined || v === null || String(v).trim() === '')
          return true
        const n = Number(String(v).trim())
        return Number.isFinite(n)
      }),
    )
  }

  // Dummy data for Table 1 (Crude Blend Window)
  const getDummyTable1Data = () => ({
    headers: ['Property', 'Stream', 'Unit', 'Min', 'Max', 'Criticality', 'Remarks'],
    keys: ['property', 'stream', 'unit', 'min', 'max', 'criticality', 'remarks'],
    result: [
      {
        id: 1,
        property: 'API',
        stream: 'CDU feed',
        unit: 'degree',
        min: 26.0,
        max: '-',
        criticality: 2.0,
        remarks: 'Max acceptable API delta in successive crude blends change is 2',
      },
      {
        id: 2,
        property: 'TAN',
        stream: 'CDU feed',
        unit: 'mg KOH/gm',
        min: 1.3,
        max: '',
        criticality: 1.0,
        remarks: 'Upper TAN to be targeted for 1.2 + 0.1 margin',
      },
      {
        id: 3,
        property: 'Sulfur',
        stream: 'CDU feed',
        unit: 'Wt%',
        min: 1.1,
        max: 2.7,
        criticality: 1,
        remarks: 'Lower limit is based on sulphur/TAN ratio',
      },
    ],
  })

  // Dummy data for Table 2 (Crude Specific Constraints)
  const getDummyTable2Data = () => ({
    headers: ['Crude', 'Max Blend Limit', 'Reasons'],
    keys: ['crude', 'maxBlendLimit', 'reasons'],
    result: [
      {
        id: 1,
        crude: 'Arab Light',
        maxBlendLimit: '45%',
        reasons: 'High sulfur content constraint',
      },
      {
        id: 2,
        crude: 'Brent',
        maxBlendLimit: '35%',
        reasons: 'API gravity compatibility',
      },
      {
        id: 3,
        crude: 'WTI',
        maxBlendLimit: '20%',
        reasons: 'Viscosity and TAN limits',
      },
    ],
  })

  // Fetch Crude Blend Window Data (Table 1)
  const fetchCrudBlendWindowData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setLoading(true)

      // TODO: Replace with actual API call
      // const response = await TcsApiService.getCrudBlendWindowData(keycloak, PLANT_ID, AOP_YEAR)
      const response = getDummyTable1Data()

      const transformedData = response.result.map((item, index) => ({
        id: item.id || `row_${index}`,
        ...item,
        inEdit: false,
      }))

      // Store headers and keys from API response
      if (response?.headers && response?.keys) {
        setApiMetadata1({ headers: response.headers, keys: response.keys })
      }

      console.log('Crude Blend Window Data:', transformedData)
      setRows(transformedData)
    } catch (err) {
      console.error('Error loading Crude Blend Window data:', err)
      setSnackbarData({
        message: 'Failed to load Crude Blend Window data. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, AOP_YEAR, currentTab.id, setSnackbarData, setSnackbarOpen])

  // Fetch Crude Specific Constraints Data (Table 2)
  const fetchCrudeSpecificConstraintsData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      // TODO: Replace with actual API call
      // const response = await TcsApiService.getCrudeSpecificConstraintsData(keycloak, PLANT_ID, AOP_YEAR)
      const response = getDummyTable2Data()

      const transformedData = response.result.map((item, index) => ({
        id: item.id || `row_${index}`,
        ...item,
        inEdit: false,
      }))

      // Store headers and keys from API response
      if (response?.headers && response?.keys) {
        setApiMetadata2({ headers: response.headers, keys: response.keys })
      }

      console.log('Crude Specific Constraints Data:', transformedData)
      setRows2(transformedData)
    } catch (err) {
      console.error('Error loading Crude Specific Constraints data:', err)
      setSnackbarData({
        message: 'Failed to load Crude Specific Constraints data. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
      setRows2([])
    }
  }, [PLANT_ID, AOP_YEAR, currentTab.id, setSnackbarData, setSnackbarOpen])

  // Fetch data on mount or when dependencies change
  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchCrudBlendWindowData()
      fetchCrudeSpecificConstraintsData()
    }
  }, [PLANT_ID, AOP_YEAR, fetchCrudBlendWindowData, fetchCrudeSpecificConstraintsData])

  // Column configuration for Table 1 (Crude Blend Window)
  const columnConfig1 = {
    property: { editable: true, type: 'text', minWidth: 100, widthT: 100 },
    stream: { editable: true, type: 'text', minWidth: 100, widthT: 100 },
    unit: { editable: true, type: 'text', minWidth: 30, widthT: 50 },
    min: { editable: true, type: 'number1', minWidth: 30, widthT: 50 },
    max: { editable: true, type: 'number1', minWidth: 30, widthT: 50 },
    criticality: { editable: true, type: 'number1', minWidth: 30, widthT: 50 },
    remarks: { editable: true, type: 'text', minWidth: 100, widthT: 100 },
  }

  const columns1 = useMemo(() => {
    const { headers, keys } = apiMetadata1

    if (!headers || !keys || headers.length === 0) {
      return []
    }

    // Map keys to their headers from backend
    const columnMap = {}
    headers.forEach((header, index) => {
      columnMap[keys[index]] = header
    })

    // Build columns using columnConfig for type/formatting
    return Object.entries(columnConfig1).map(([key, config]) => ({
      field: key,
      title: columnMap[key] || key,
      ...config,
    }))
  }, [apiMetadata1])


  // Column configuration for Table 2 (Crude Specific Constraints)
  const columnConfig2 = {
    crude: { editable: true, type: 'text', minWidth: 80, widthT: 80 },
    maxBlendLimit: { editable: true, type: 'text', minWidth: 50, widthT: 50 },
    reasons: { editable: true, type: 'text', minWidth: 140, widthT: 140 },
  }

  const columns2 = useMemo(() => {
    const { headers, keys } = apiMetadata2

    if (!headers || !keys || headers.length === 0) {
      return []
    }

    // Map keys to their headers from backend
    const columnMap = {}
    headers.forEach((header, index) => {
      columnMap[keys[index]] = header
    })

    // Build columns using columnConfig for type/formatting
    return Object.entries(columnConfig2).map(([key, config]) => ({
      field: key,
      title: columnMap[key] || key,
      ...config,
    }))
  }, [apiMetadata2])


  // Handle remark cell click for Table 1
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Handle remark cell click for Table 2
  const handleRemarkCellClick2 = (row) => {
    setCurrentRemark2(row.reasons || '')
    setCurrentRowId2(row.id)
    setRemarkDialogOpen2(true)
  }

  // Track when modifiedCells is cleared and reset inEdit flags for Table 1
  useEffect(() => {
    if (Object.keys(modifiedCells).length === 0) {
      setRows((prev) =>
        prev.map((row) => ({
          ...row,
          inEdit: false,
        })),
      )
    }
  }, [modifiedCells])

  // Track when modifiedCells2 is cleared and reset inEdit flags for Table 2
  useEffect(() => {
    if (Object.keys(modifiedCells2).length === 0) {
      setRows2((prev) =>
        prev.map((row) => ({
          ...row,
          inEdit: false,
        })),
      )
    }
  }, [modifiedCells2])

  // Save changes for Table 1
  const saveChanges = useCallback(async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      console.log('Crude Blend Window data to save:', data)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      // TODO: Replace with actual API call
      // const response = await TcsApiService.saveCrudBlendWindowData(keycloak, PLANT_ID, AOP_YEAR, data)

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Crude Blend Window data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      fetchCrudBlendWindowData()
    } catch (error) {
      console.error('Error saving Crude Blend Window data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving Crude Blend Window data!',
        severity: 'error',
      })
    }
  }, [
    modifiedCells,
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    setSnackbarData,
    setSnackbarOpen,
    fetchCrudBlendWindowData,
  ])

  // Save changes for Table 2
  const saveChanges2 = useCallback(async () => {
    try {
      if (Object.keys(modifiedCells2).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      const rawData = Object.values(modifiedCells2)
      const data = rawData.filter((row) => row.inEdit)
      console.log('Crude Specific Constraints data to save:', data)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      // TODO: Replace with actual API call
      // const response = await TcsApiService.saveCrudeSpecificConstraintsData(keycloak, PLANT_ID, AOP_YEAR, data)

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Crude Specific Constraints data saved successfully!',
        severity: 'success',
      })
      setModifiedCells2({})
      fetchCrudeSpecificConstraintsData()
    } catch (error) {
      console.error('Error saving Crude Specific Constraints data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving Crude Specific Constraints data!',
        severity: 'error',
      })
    }
  }, [
    modifiedCells2,
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    setSnackbarData,
    setSnackbarOpen,
    fetchCrudeSpecificConstraintsData,
  ])

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    addButton: false,
    remarksEditable: true,
    showCalculate: false,
    showExport: false,
    showImport: false,
    saveBtnForRemark: true,
    saveBtn: true,
    showWorkFlowBtns: false,
    showTitle: true,
    filterable: false,
  }
  const permissions1 = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    addButton: false,
    remarksEditable: true,
    showCalculate: false,
    showExport: false,
    showImport: false,
    saveBtnForRemark: true,
    saveBtn: false,
    showWorkFlowBtns: false,
    showTitle: true,
    filterable: false,
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Stack>
        <AdvanceKendoTable
          rows={rows}
          setRows={setRows}
          fetchData={fetchCrudBlendWindowData}
          handleRemarkCellClick={handleRemarkCellClick}
          columns={columns1}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={() => {}}
          saveChanges={saveChanges}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          permissions={permissions}
        />
      </Stack>
      <Stack sx={{ mt: 2 }}>
        <AdvanceKendoTable
          rows={rows2}
          setRows={setRows2}
          fetchData={fetchCrudeSpecificConstraintsData}
          handleRemarkCellClick={handleRemarkCellClick2}
          columns={columns2}
          title='Crude Specific Constraints'
          remarkDialogOpen={remarkDialogOpen2}
          setRemarkDialogOpen={setRemarkDialogOpen2}
          currentRemark={currentRemark2}
          setCurrentRemark={setCurrentRemark2}
          currentRowId={currentRowId2}
          setCurrentRowId={() => {}}
          saveChanges={saveChanges2}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          modifiedCells={modifiedCells2}
          setModifiedCells={setModifiedCells2}
          permissions={permissions1}
        />
      </Stack>
    </Box>
  )
}

export default CrudBlendWindow
