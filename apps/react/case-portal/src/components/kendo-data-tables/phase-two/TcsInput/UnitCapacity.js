import { Box, Backdrop, CircularProgress } from '@mui/material'
import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'services/phase-two-services/tcsApiService'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import { generateMockData } from './utility'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'

const UnitCapacity = ({
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

  // State management
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

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
        if (v === undefined || v === null || String(v).trim() === '') return true
        const n = Number(String(v).trim())
        return Number.isFinite(n)
      }),
    )
  }

  // State to store API response metadata (headers and keys)
  const [apiMetadata, setApiMetadata] = useState({ headers: [], keys: [] })

  // Fetch Unit Capacity data
  const fetchUnitCapacityData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      let transformedData = []

      const response = await TcsApiService.getTcsUnitCapacityData(keycloak, PLANT_ID, AOP_YEAR)
      console.log('TCS Unit Capacity Response:', response)

      if (response?.results && Array.isArray(response.results)) {
        transformedData = response.results.map((item, index) => ({
          id: item.id || `row_${index}`,
          ...item,
          inEdit: false,
        }))
      }
      
      // Store headers and keys from API response
      if (response?.headers && response?.keys) {
        setApiMetadata({ headers: response.headers, keys: response.keys })
      }
      
      setRows(transformedData)
    } catch (err) {
      console.error('Error fetching Unit Capacity data:', err)
      setSnackbarData({
        message: `Failed to load Unit Capacity data. Please try again.`,
        severity: 'error',
      })
      setSnackbarOpen(true)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR, currentTab.id, setSnackbarData, setSnackbarOpen])

  // Fetch data on mount or when dependencies change
  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchUnitCapacityData()
    }
  }, [PLANT_ID, AOP_YEAR, fetchUnitCapacityData])

  // Column configuration for Unit Capacity - dynamically generated from API response
  const columns = useMemo(() => {
    const { headers, keys } = apiMetadata
    
    if (!headers || !keys || headers.length === 0) {
      return []
    }

    // Map keys to their headers
    const columnMap = {}
    headers.forEach((header, index) => {
      columnMap[keys[index]] = header
    })

    // Build columns with UI structure
    const firstColumn = {
      field: 'particulates',
      title: columnMap['particulates'] || 'Particulars',
      widthT: 120,
      locked: true,
      editable: false,
      disable: false,
      type: 'text',
      minWidth: 50,
    }

    // Group remaining columns under "Capacity"
    const capacityChildren = []
    const capacityKeys = ['uom', 'kbpsd', 'remark']
    
    capacityKeys.forEach((key) => {
      if (columnMap[key]) {
        capacityChildren.push({
          field: key,
          title: columnMap[key],
          editable: true,
          type: 'text',
          minWidth: key === 'remark' ? 100 : 80,
        })
      }
    })

    const columns = [firstColumn]
    
    if (capacityChildren.length > 0) {
      columns.push({
        title: 'Capacity',
        children: capacityChildren,
      })
    }

    return columns
  }, [apiMetadata])

  // Apply numeric formatting to detected numeric fields
  const numericKeys = useMemo(() => getNumericKeysInAllRows(rows), [rows])

  const columnsWithFormatting = useMemo(() => {
    return columns.map((col) => {
      if (col.children) {
        return {
          ...col,
          children: col.children.map((child) => {
            if (numericKeys.includes(child.field)) {
              return {
                ...child,
                type: 'number1',
                format: valueFormat,
              }
            }
            return child
          }),
        }
      }
      if (numericKeys.includes(col.field)) {
        return {
          ...col,
          type: 'number1',
          format: valueFormat,
        }
      }
      return col
    })
  }, [columns, numericKeys, valueFormat])

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Track when modifiedCells is cleared and reset inEdit flags
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

  // Save changes
  const saveChanges = useCallback(async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      console.log('Unit Capacity data to save:', data)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      // TODO: Replace with actual API call
      // const response = await TcsApiService.saveUnitCapacityData(keycloak, data, PLANT_ID, AOP_YEAR)

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unit Capacity data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      fetchUnitCapacityData()
    } catch (error) {
      console.error('Error saving Unit Capacity data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving Unit Capacity data!',
        severity: 'error',
      })
    }
  }, [modifiedCells, keycloak, PLANT_ID, AOP_YEAR, setSnackbarData, setSnackbarOpen, fetchUnitCapacityData])

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
    showWorkFlowBtns: false,
    showTitle: true,
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <AdvanceKendoTable
        rows={rows}
        setRows={setRows}
        fetchData={fetchUnitCapacityData}
        configType='tcs_unit_capacity'
        handleRemarkCellClick={handleRemarkCellClick}
        columns={columnsWithFormatting}
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
    </Box>
  )
}

export default UnitCapacity
