import React, { useState, useCallback, useEffect } from 'react'
import { Box, Tab, Tabs } from '@mui/material'
import KendoDataTables from './index.js'

const DecokingConfig = () => {
  const tabs = ['IBR Plan', 'Shutdown Activities', 'Running Duration']

  const [activeTabIndex, setActiveTabIndex] = useState(0)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const handleRemarkCellClick = (row) => {
    // if (!row?.isEditable) return

    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const [remarkDialogOpen2, setRemarkDialogOpen2] = useState(false)
  const [currentRemark2, setCurrentRemark2] = useState('')
  const [currentRowId2, setCurrentRowId2] = useState(null)
  const handleRemarkCellClick2 = (row) => {
    // if (!row?.isEditable) return
    console.log(row)
    setCurrentRemark2(row.remarks || '')
    setCurrentRowId2(row.id)
    setRemarkDialogOpen2(true)
  }
  const [data, setData] = useState({
    ibrPlan: [],
    shutdownActivities: [],
    runningDuration: [],
  })

  // IBR Plan data
  const ibrPlanData = [
    {
      id: 1,
      furnace: 'H10',
      ibrPlan: "Jun'25",
      days: 100,
      // remarks: 'Manual Entry with remarks',
      startDateIBR: '',
      endDateIBR: '',
      startDateSD: '',
      endDateSD: '',
      startDateTA: '',
      endDateTA: '',
      remark: 'Start date and end date will be manual entry for IBR',
    },
    {
      id: 2,
      furnace: 'H11',
      ibrPlan: "Jan'26",
      days: 100,
      // remarks: 'Manual Entry with remarks',
      startDateIBR: '',
      endDateIBR: '',
      startDateSD: '',
      endDateSD: '',
      startDateTA: '',
      endDateTA: '',
      remark: 'IBR-1=BBD',
    },
    {
      id: 3,
      furnace: 'H11',
      ibrPlan: "Jan'26",
      days: 100,
      // remarks: 'Manual Entry with remarks',
      startDateIBR: '',
      endDateIBR: '',
      startDateSD: '',
      endDateSD: '',
      startDateTA: '',
      endDateTA: '',
      remark: 'BBD- 2= SAD',
    },
    {
      id: 4,
      furnace: 'H12',
      ibrPlan: "Aug'25",
      days: 100,
      // remarks: 'Manual Entry with remarks',
      startDateIBR: '',
      endDateIBR: '',
      startDateSD: '',
      endDateSD: '',
      startDateTA: '',
      endDateTA: '',
      remark: 'logic to be checked for automating IBR, BBD, SAD entries',
    },
    {
      id: 5,
      furnace: 'H13',
      ibrPlan: "Nov'25",
      days: 60,
      // remarks: 'Manual Entry with remarks',
      startDateIBR: '',
      endDateIBR: '',
      startDateSD: '',
      endDateSD: '',
      startDateTA: '',
      endDateTA: '',
      remark: '',
    },
    {
      id: 6,
      furnace: 'H14',
      ibrPlan: "Jul'25",
      days: 100,
      // remarks: 'Manual Entry with remarks',
      startDateIBR: '',
      endDateIBR: '',
      startDateSD: '',
      endDateSD: '',
      startDateTA: '',
      endDateTA: '',
      remark: '',
    },
    {
      id: 7,
      furnace: 'DEMO',
      ibrPlan: '-',
      days: 0,
      // remarks: '',
      startDateIBR: '',
      endDateIBR: '',
      startDateSD: '',
      endDateSD: '',
      startDateTA: '',
      endDateTA: '',
      remark: '',
    },
  ]

  // Shutdown Activities data
  const shutdownActivitiesData = [
    {
      id: 1,
      month: 'Mar',
      date: '20-Mar-25',
      h10: 38,
      h11: 50,
      h12: 64,
      h13: 17,
      h14: 88,
      demo: 'SD',
      remarks:
        'Run length counter 1st entry will be manual entry, remaining will be as per logic',
    },
    {
      id: 2,
      month: 'Mar',
      date: '21-Mar-25',
      h10: 39,
      h11: 51,
      h12: 65,
      h13: 18,
      h14: 89,
      demo: 'SD',
      remarks:
        'Run length counter 1st entry will be manual entry, remaining will be as per logic',
    },
    {
      id: 3,
      month: 'Mar',
      date: '22-Mar-25',
      h10: 40,
      h11: 52,
      h12: 66,
      h13: 19,
      h14: 90,
      demo: 'SD',
      remarks:
        'Run length counter 1st entry will be manual entry, remaining will be as per logic',
    },

    {
      id: 4,
      month: 'Mar',
      date: '28-Mar-25',
      h10: 46,
      h11: 58,
      h12: 72,
      h13: 25,
      h14: 96,
      demo: 'SD',
      remarks:
        'Run length counter 1st entry will be manual entry, remaining will be as per logic',
    },
  ]

  // Running Duration data
  const runningDurationData = [
    {
      month: 'April',
      ibr: 0,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 5,
      buD: 0,
      fourF: 5,
      fiveF: 25,
      fourFD: 0,
      total: 30,
    },
    {
      month: 'May',
      ibr: 0,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 5,
      buD: 0,
      fiveF: 5,
      fourF: 26,
      fourFD: 0,
      total: 31,
    },
    {
      month: 'June',
      ibr: 8,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 2.5,
      buD: 2,
      fiveF: 12.5,
      fourF: 17.5,
      fourFD: 0,
      total: 30,
    },
    {
      month: 'July',
      ibr: 8,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 5,
      buD: 2,
      fiveF: 15,
      fourF: 16,
      fourFD: 0,
      total: 31,
    },
    {
      month: 'August',
      ibr: 8,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 5,
      buD: 2,
      fiveF: 15,
      fourF: 16,
      fourFD: 0,
      total: 31,
    },
    {
      month: 'September',
      ibr: 0,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 5,
      buD: 0,
      fiveF: 5,
      fourF: 25,
      fourFD: 0,
      total: 30,
    },
    {
      month: 'October',
      ibr: 0,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 1.25,
      buD: 0,
      fiveF: 1.25,
      fourF: 29.75,
      fourFD: 0,
      total: 31,
    },
    {
      month: 'November',
      ibr: 17,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 6.25,
      buD: 2,
      fiveF: 25.25,
      fourF: 4.75,
      fourFD: 0,
      total: 30,
    },
    {
      month: 'December',
      ibr: 0,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 2.5,
      buD: 0,
      fiveF: 2.5,
      fourF: 28.5,
      fourFD: 0,
      total: 31,
    },
    {
      month: 'January',
      ibr: 8,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 2.5,
      buD: 2,
      fiveF: 12.5,
      fourF: 18.5,
      fourFD: 0,
      total: 31,
    },
    {
      month: 'February',
      ibr: 0,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 2.5,
      buD: 0,
      fiveF: 2.5,
      fourF: 25.5,
      fourFD: 0,
      total: 28,
    },
    {
      month: 'March',
      ibr: 0,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 5,
      buD: 0,
      fiveF: 5,
      fourF: 26,
      fourFD: 0,
      total: 31,
    },
    {
      month: 'Total',
      ibr: 49,
      mnt: 0,
      shutdown: 0,
      slowdown: 0,
      sad: 47.5,
      buD: 10,
      fiveF: 106.5,
      fourF: 258.5,
      fourFD: 0,
      total: 365,
    },
  ]

  const ibrPlanColumns = [
    { field: 'id', title: 'ID', editable: false, width: 80 },
    { field: 'furnace', title: 'Furnace', editable: false, width: 120 },
    { field: 'ibrPlan', title: 'IBR Plan', editable: true, width: 120 },
    {
      field: 'days',
      title: 'Days',
      editable: true,
      width: 100,
      type: 'number',
    },
    { field: 'startDateIBR', title: 'Start IBR', editable: true, width: 150 },
    { field: 'endDateIBR', title: 'End IBR', editable: true, width: 150 },
    { field: 'startDateSD', title: 'Start SD', editable: true, width: 150 },
    { field: 'endDateSD', title: 'End SD', editable: true, width: 150 },
    { field: 'startDateTA', title: 'Start TA', editable: true, width: 150 },
    { field: 'endDateTA', title: 'End TA', editable: true, width: 150 },
    { field: 'remark', title: ' Remarks', editable: true, width: 250 },
  ]

  const shutdownColumns = [
    { field: 'month', title: 'Month', editable: false },
    { field: 'date', title: 'Date', editable: false },
    { field: 'h10', title: 'H10', editable: true, type: 'number' },
    { field: 'h11', title: 'H11', editable: true },
    { field: 'h12', title: 'H12', editable: true, type: 'number' },
    { field: 'h13', title: 'H13', editable: true, type: 'number' },
    { field: 'h14', title: 'H14', editable: true, type: 'number' },
    { field: 'demo', title: 'Demo', editable: true },
    { field: 'remarks', title: 'Remarks', editable: true },
  ]

  const runningDurationColumns = [
    { field: 'month', title: 'Month', editable: false },
    { field: 'ibr', title: 'IBR', editable: true, type: 'number' },
    { field: 'mnt', title: 'MNT', editable: true, type: 'number' },
    {
      field: 'shutdown',
      title: 'Shutdown',
      editable: true,
      width: 120,
      type: 'number',
    },
    {
      field: 'slowdown',
      title: 'Slowdown',
      editable: true,
      width: 120,
      type: 'number',
    },
    { field: 'sad', title: 'SAD', editable: true, type: 'number' },
    { field: 'buD', title: 'BUD', editable: true, type: 'number' },
    { field: 'fourF', title: '4F', editable: true, type: 'number' },
    { field: 'fiveF', title: '5F', editable: true, type: 'number' },
    { field: 'fourFD', title: '4FD', editable: true, type: 'number' },
    { field: 'total', title: 'Total', editable: false, type: 'number' },
  ]

  const [ibrPlanRows, setIbrPlanRows] = useState(ibrPlanData)
  const [shutdownActivities, setshutdownActivities] = useState(
    shutdownActivitiesData,
  )
  const [runningDuration, setRunningDurationRows] =
    useState(runningDurationData)

  const fetchData = useCallback((tabName) => {
    setTimeout(() => {
      switch (tabName) {
        case 'IBR Plan':
          setData((prev) => ({ ...prev, ibrPlan: ibrPlanData }))
          break
        case 'Shutdown Activities':
          setData((prev) => ({
            ...prev,
            shutdownActivities: shutdownActivitiesData,
          }))
          break
        case 'Running Duration':
          setData((prev) => ({ ...prev, runningDuration: runningDurationData }))
          break
        default:
          break
      }
    }, 300)
  }, [])

  useEffect(() => {
    const currentTab = tabs[activeTabIndex]
    fetchData(currentTab)
  }, [activeTabIndex, fetchData])

  const renderIBRPlanTable = () => (
    <Box>
      <KendoDataTables
        title='IBR Plan (Screen-3)'
        columns={ibrPlanColumns}
        rows={ibrPlanRows}
        setRows={setIbrPlanRows}
        handleRemarkCellClick={handleRemarkCellClick}
        //NormParameterIdCell={NormParameterIdCell}
        //modifiedCells={modifiedCells}
        //setModifiedCells={setModifiedCells}
        //isCellEditable={isCellEditable}
        paginationOptions={[100, 200, 300]}
        //saveChanges={saveChanges}
        //snackbarData={snackbarData}
        //snackbarOpen={snackbarOpen}
        //apiRef={apiRef}
        //setDeleteId={setDeleteId}
        //setOpen1={setOpen1}
        //setSnackbarOpen={setSnackbarOpen}
        //setSnackbarData={setSnackbarData}
        //deleteId={deleteId}
        //open1={open1}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        //unsavedChangesRef={unsavedChangesRef}
        //permissions={adjustedPermissions}
      />
    </Box>
  )

  const renderShutdownActivitiesTable = () => (
    <Box>
      <KendoDataTables
        title='Shutdown Activities (Screen-4)'
        columns={shutdownColumns}
        rows={shutdownActivities}
        setRows={setshutdownActivities}
        handleRemarkCellClick={handleRemarkCellClick2}
        //NormParameterIdCell={NormParameterIdCell}
        //modifiedCells={modifiedCells}
        //setModifiedCells={setModifiedCells}
        //isCellEditable={isCellEditable}
        paginationOptions={[100, 200, 300]}
        //saveChanges={saveChanges}
        //snackbarData={snackbarData}
        //snackbarOpen={snackbarOpen}
        //apiRef={apiRef}
        //setDeleteId={setDeleteId}
        //setOpen1={setOpen1}
        //setSnackbarOpen={setSnackbarOpen}
        //setSnackbarData={setSnackbarData}
        //deleteId={deleteId}
        //open1={open1}
        remarkDialogOpen={remarkDialogOpen2}
        setRemarkDialogOpen={setRemarkDialogOpen2}
        currentRemark={currentRemark2}
        setCurrentRemark={setCurrentRemark2}
        currentRowId={currentRowId2}
        //unsavedChangesRef={unsavedChangesRef}
        //permissions={adjustedPermissions}
      />
    </Box>
  )

  const renderRunningDurationTable = () => (
    <Box>
      <KendoDataTables
        title='Running Duration (Maintenance Details) - (Screen-5)'
        columns={runningDurationColumns}
        rows={runningDuration}
        setRows={setRunningDurationRows}
        // handleRemarkCellClick={handleRemarkCellClick}
        //NormParameterIdCell={NormParameterIdCell}
        //modifiedCells={modifiedCells}
        //setModifiedCells={setModifiedCells}
        //isCellEditable={isCellEditable}
        paginationOptions={[100, 200, 300]}
        //saveChanges={saveChanges}
        //snackbarData={snackbarData}
        //snackbarOpen={snackbarOpen}
        //apiRef={apiRef}
        //setDeleteId={setDeleteId}
        //setOpen1={setOpen1}
        //setSnackbarOpen={setSnackbarOpen}
        //setSnackbarData={setSnackbarData}
        //deleteId={deleteId}
        //open1={open1}
        //remarkDialogOpen={remarkDialogOpen}
        //setRemarkDialogOpen={setRemarkDialogOpen}
        //currentRemark={currentRemark}
        //setCurrentRemark={setCurrentRemark}
        //currentRowId={currentRowId}
        //unsavedChangesRef={unsavedChangesRef}
        //permissions={adjustedPermissions}
      />
    </Box>
  )

  const renderContent = () => {
    switch (tabs[activeTabIndex]) {
      case 'IBR Plan':
        return renderIBRPlanTable()
      case 'Shutdown Activities':
        return renderShutdownActivitiesTable()
      case 'Running Duration':
        return renderRunningDurationTable()
      default:
        return null
    }
  }

  return (
    <Box>
      <Tabs
        value={activeTabIndex}
        onChange={(e, newIndex) => setActiveTabIndex(newIndex)}
        sx={{
          borderBottom: '0px solid #ccc',
          '.MuiTabs-indicator': { display: 'none' },
        }}
        textColor='primary'
        indicatorColor='primary'
      >
        {tabs.map((tabId) => (
          <Tab
            key={tabId}
            label={tabId}
            sx={{
              border: '1px solid #ADD8E6',
              borderBottom: '1px solid #ADD8E6',
              textTransform: 'capitalize',
            }}
          />
        ))}
      </Tabs>

      {renderContent()}
    </Box>
  )
}

export default DecokingConfig
