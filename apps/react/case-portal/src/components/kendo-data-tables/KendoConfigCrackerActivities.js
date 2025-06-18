import React, { useState, useCallback, useEffect } from 'react'
import { Box, Tab, Tabs } from '@mui/material'
import KendoDataTables from './index.js'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
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
    // console.log(row)
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
  const ibrPlanColumns = [
    { field: 'furnace', title: 'Furnace', editable: false, width: 200 },
    { field: 'ibrPlan', title: 'IBR Plan', editable: true, width: 200 },
    {
      field: 'aprMarDays',
      title: 'Apr-Mar Days',
      editable: true,
      width: 200,
      type: 'number',
    },
    {
      field: 'startDateIBR',
      title: 'Start Date of IBR',
      editable: true,
      width: 200,
    },
    {
      field: 'endDateIBR',
      title: 'End Date of IBR',
      editable: true,
      width: 200,
    },
    {
      field: 'startDateSD',
      title: 'Start Date of SD',
      editable: true,
      width: 200,
    },
    { field: 'endDateSD', title: 'End Date of SD', editable: true, width: 200 },
    {
      field: 'startDateTA',
      title: 'Start date TA',
      editable: true,
      width: 200,
    },
    { field: 'endDateTA', title: 'End date TA', editable: true, width: 200 },
    { field: 'remarks', title: 'Remarks', editable: true, width: 250 },
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

const [ibrPlanRows, setIbrPlanRows] = useState([])
const [shutdownActivities, setshutdownActivities] = useState([])
const [runningDuration, setRunningDurationRows] = useState([])

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



const keycloak = useSession();

useEffect(() => {
  const currentTab = tabs[activeTabIndex]

  const fetchData = async () => {
    try {
      if (currentTab === 'IBR Plan') {
        const ibrData = await DataService.getIbrPlanData(keycloak);
        setIbrPlanRows(ibrData);
      } else if (currentTab === 'Shutdown Activities') {
        const shutdownData = await DataService.getShutdownActivitiesData(keycloak);
        setshutdownActivities(shutdownData);
      } else if (currentTab === 'Running Duration') {
        const runningData = await DataService.getRunningDurationData(keycloak);
        setRunningDurationRows(runningData);
      }
    } catch (error) {
      console.error('Error fetching decoking config data:', error);
    }
  };

  fetchData();
}, [activeTabIndex, keycloak]);

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
