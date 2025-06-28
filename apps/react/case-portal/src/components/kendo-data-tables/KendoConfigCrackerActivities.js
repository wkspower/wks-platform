// DecokingConfig.jsx (refactored to mirror CrackerConfig patterns)
import React, { useState, useCallback, useEffect, useMemo } from 'react'
import {
  Box,
  Tab,
  Tabs,
  Backdrop,
  CircularProgress,
  Typography,
} from '@mui/material'
import KendoDataTables from './index.js'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

// ─── Column Definitions ─────────────────────────────────────────────────────
import {
  ibrGridOne,
  ibrPlanColumns,
  ibrGridThree,
  runningDurationColumns,
} from './columnDefs'

// ─── Sample Data ─────────────────────────────────────────────────────────────
import {
  ibrGridOneRowsSample,
  ibrPlanRowsSample,
  ibrGridThreeRowsSample,
  runningDurationRowsSample,
} from './rowSamples'

const DecokingConfig = () => {
  const keycloak = useSession()
  const tabs = ['IBR Plan', 'Running Duration']

  // ─── Global UI State ──────────────────────────────────────────────────────
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  // ─── Dynamic Tab Index ────────────────────────────────────────────────────
  const [activeTabIndex, setActiveTabIndex] = useState(0)

  // ─── Remark Dialog (generic) ───────────────────────────────────────────────
  const [remarkDialog, setRemarkDialog] = useState({
    open: false,
    rowId: null,
    remark: '',
  })
  const handleRemarkCellClick = useCallback((row) => {
    setRemarkDialog({
      open: true,
      rowId: row.id,
      remark: row.remarks || row.Remarks || '',
    })
  }, [])

  // ─── Rows State Per Tab ────────────────────────────────────────────────────
  const [ibrScreen1Rows, setIbrScreen1Rows] = useState([])
  const [ibrScreen2Rows, setIbrScreen2Rows] = useState([])
  const [ibrScreen3Rows, setIbrScreen3Rows] = useState([])
  const [runningDurationRows, setRunningDurationRows] = useState([])
  const modifiedCells = useState({})[0] // you can replace with useState if needed

  // ─── Get/Set Rows by Tab ───────────────────────────────────────────────────
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

  // ─── Fetch Data ────────────────────────────────────────────────────────────
  const fetchData = useCallback(async () => {
    const currentTab = tabs[activeTabIndex]
    setLoading(true)
    try {
      if (currentTab === 'IBR Plan') {
        // screen 1
        // const data1 = await DataService.getIbrScreen1(keycloak)
        setRowsForTab(currentTab, ibrGridOneRowsSample, 1)
        // screen 2
        // const data2 = await DataService.getIbrScreen2(keycloak)
        setRowsForTab(currentTab, ibrPlanRowsSample, 2)
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

  useEffect(() => {
    fetchData()
  }, [fetchData])

  // ─── Renderers ─────────────────────────────────────────────────────────────
  const renderIbrPlanTables = () => (
    <>
      {[1, 2, 3].map((screen) => (
        <Box key={screen} sx={{ mt: 2 }}>
          <Typography variant='h6'>IBR Plan (Screen-{screen})</Typography>
          <KendoDataTables
            columns={
              screen === 1
                ? ibrGridOne
                : screen === 2
                  ? ibrPlanColumns
                  : ibrGridThree
            }
            rows={getRows('IBR Plan')[screen]}
            setRows={(data) => setRowsForTab('IBR Plan', data, screen)}
            fetchData={fetchData}
            handleRemarkCellClick={handleRemarkCellClick}
            remarkDialogOpen={remarkDialog.open}
            setRemarkDialogOpen={(open) =>
              setRemarkDialog((v) => ({ ...v, open }))
            }
            currentRemark={remarkDialog.remark}
            setCurrentRemark={(r) =>
              setRemarkDialog((v) => ({ ...v, remark: r }))
            }
            currentRowId={remarkDialog.rowId}
            snackbarData={snackbarData}
            snackbarOpen={snackbarOpen}
            setSnackbarOpen={setSnackbarOpen}
            setSnackbarData={setSnackbarData}
            modifiedCells={modifiedCells}
            setModifiedCells={(m) => {
              /* implement setter */
            }}
          />
        </Box>
      ))}
    </>
  )

  const renderRunningDurationTable = () => (
    <Box sx={{ mt: 2 }}>
      <Typography variant='h6'>Running Duration</Typography>
      <KendoDataTables
        columns={runningDurationColumns}
        rows={runningDurationRows}
        setRows={setRunningDurationRows}
        fetchData={fetchData}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialog.open}
        setRemarkDialogOpen={(open) => setRemarkDialog((v) => ({ ...v, open }))}
        currentRemark={remarkDialog.remark}
        setCurrentRemark={(r) => setRemarkDialog((v) => ({ ...v, remark: r }))}
        currentRowId={remarkDialog.rowId}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCells}
        setModifiedCells={(m) => {
          /* implement setter */
        }}
      />
    </Box>
  )

  return (
    <Box>
      <Backdrop open={loading} sx={{ color: '#fff', zIndex: 999 }}>
        <CircularProgress color='inherit' />
      </Backdrop>

      <Tabs value={activeTabIndex} onChange={(e, i) => setActiveTabIndex(i)}>
        {tabs.map((tab) => (
          <Tab key={tab} label={tab} />
        ))}
      </Tabs>

      {activeTabIndex === 0
        ? renderIbrPlanTables()
        : renderRunningDurationTable()}
    </Box>
  )
}

export default DecokingConfig
