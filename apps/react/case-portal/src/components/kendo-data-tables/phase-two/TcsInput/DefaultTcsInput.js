import { Box, Backdrop, CircularProgress } from '@mui/material'
import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { generateMockData, getColumnsForTab } from './utility'

const DefaultTcsInput = ({
  PLANT_ID,
  AOP_YEAR,
  currentTab,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
  tabDisplayName,
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

  // Fetch mock data based on tab displayName
  const fetchTabData = useCallback(async () => {
    if (!tabDisplayName) return
    try {
      setLoading(true)
      
      // Get mock data from utility
      const mockData = generateMockData(currentTab.id)
      const transformedData = mockData.map((item, index) => ({
        id: item.id || `row_${index}`,
        ...item,
        inEdit: false,
      }))

      console.log(`${tabDisplayName} Mock Data:`, transformedData)
      setRows(transformedData)
    } catch (err) {
      console.error(`Error loading ${tabDisplayName} data:`, err)
      setSnackbarData({
        message: `Failed to load ${tabDisplayName} data.`,
        severity: 'error',
      })
      setSnackbarOpen(true)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [tabDisplayName, currentTab.id, setSnackbarData, setSnackbarOpen])

  // Fetch data on mount or when dependencies change
  useEffect(() => {
    if (tabDisplayName) {
      fetchTabData()
    }
  }, [tabDisplayName, fetchTabData])

  // Column configuration from utility.js
  const columns = useMemo(() => {
    return getColumnsForTab(tabDisplayName, {}, valueFormat)
  }, [tabDisplayName, valueFormat])

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
    setCurrentRemark(row.remark || row.remarks || '')
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
      console.log(`${currentTab.displayName} data to save:`, data)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      // TODO: Replace with actual API call based on tab
      // const response = await TcsApiService.saveTabData(keycloak, data, PLANT_ID, AOP_YEAR)

      setSnackbarOpen(true)
      setSnackbarData({
        message: `${currentTab.displayName} data saved successfully!`,
        severity: 'success',
      })
      setModifiedCells({})
      fetchTabData()
    } catch (error) {
      console.error(`Error saving ${currentTab.displayName} data:`, error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Error saving ${currentTab.displayName} data!`,
        severity: 'error',
      })
    }
  }, [modifiedCells, keycloak, PLANT_ID, AOP_YEAR, currentTab.displayName, setSnackbarData, setSnackbarOpen, fetchTabData])

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
        fetchData={fetchTabData}
        configType={`tcs_${currentTab.displayName.toLowerCase().replace(/\s+/g, '_')}`}
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

export default DefaultTcsInput
