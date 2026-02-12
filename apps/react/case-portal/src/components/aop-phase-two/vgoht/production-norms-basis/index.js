import React, { useState, useEffect, useCallback, useRef } from 'react'
import {
  Backdrop,
  Box,
  Button,
  ButtonGroup,
  CircularProgress,
  Stack,
} from '../../../../../node_modules/@mui/material/index'
import TabSection from 'components/aop-phase-two/common/utilities/Tabs'
import ConfigurationAccordian from './components/ConfigurationAccordian'
import ConfigurationDialog from './components/ConfigurationDialog'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import {
  validateDateRange,
  buildConfigurationPayload,
  executeConfiguration,
  getAopSummary,
  saveSummary,
  getConfigurationExecutionDetails,
} from './utils/utility'
import Notification from 'components/aop-phase-two/common/utilities/Notification'
import Configuration from './Configuration'
import Constants from './Constants'
import ReportManualEntry from './ReportManualEntry'
import TabAccessApiService from 'components/aop-phase-two/services/common/tabAccessApiService'
import RevConfirmDialog from './components/RevConfirmDialog'

const ProductionNormsBasis = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { oldYear, plantObject, siteObject, verticalObject, year } =
    dataGridStore

  const hasExecutedRef = useRef(false)

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const isOldYear = false

  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  const [loading1, setLoading1] = useState(false)
  const [dateEdited, setDateEdited] = useState(false)
  const [summaryEdited, setSummaryEdited] = useState(false)
  const [summary, setSummary] = useState('')
  const [startDate, setStartDate] = useState()
  const [endDate, setEndDate] = useState()
  const [configurationExecutionDetails, setConfigurationExecutionDetails] =
    useState([])
  const [openConfirmDialogRev, setOpenConfirmDialogRev] = useState(false)
  const [selectedRevNum, setSelectedRevNum] = useState(null)
  const [revision, setRevision] = useState('1')
  const [revisionDetails, setRevisionDetails] = useState(null)
  const [revisionUpdated, setRevisionUpdated] = useState(false)
  const [openConfirmDialog, setOpenConfirmDialog] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [tabs, setTabs] = useState([])
  const [availableTabs, setAvailableTabs] = useState([])

  const handleOpenDialog = () => {
    setOpenConfirmDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenConfirmDialog(false)
  }

  const handleConfirmLoad = async () => {
    setOpenConfirmDialog(false)
    await onLoad()
  }

  const fetchConfigurationDetails = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    const details = await getConfigurationExecutionDetails(
      keycloak,
      PLANT_ID,
      AOP_YEAR,
    )
    setConfigurationExecutionDetails(details)
  }, [keycloak, PLANT_ID, AOP_YEAR])

  const fetchSummary = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    const summaryData = await getAopSummary(keycloak, PLANT_ID, AOP_YEAR)
    setSummary(summaryData || '')
  }, [keycloak, PLANT_ID, AOP_YEAR])

  const getConfigurationTabsMatrix = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR || !SITE_ID || !VERTICAL_ID) return
    setLoading(true)
    try {
      const response = await TabAccessApiService.getConfigurationTabsMatrix(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        SITE_ID,
        VERTICAL_ID,
      )
      if (response?.code === 200) {
        const parsedData = JSON.parse(response?.data)
        setTabs(parsedData)
      } else {
        setTabs([])
      }
    } catch (error) {
      console.error('Error fetching configuration tabs matrix:', error)
      setTabs([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR, SITE_ID, VERTICAL_ID])

  const getConfigurationAvailableTabs = useCallback(async () => {
    setLoading(true)
    try {
      const response =
        await TabAccessApiService.getConfigurationAvailableTabs(keycloak)
      if (response?.code === 200) {
        setAvailableTabs(response?.data?.configurationTypeList)
      } else {
        setAvailableTabs([])
      }
    } catch (error) {
      console.error('Error fetching configuration available tabs:', error)
      setAvailableTabs([])
    } finally {
      setLoading(false)
    }
  }, [keycloak])

  const onLoad = async () => {
    // Validate dates
    const validation = validateDateRange(startDate, endDate)
    if (!validation.valid) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validation.message,
        severity: 'warning',
      })
      return
    }

    setLoading1(true)
    setLoading(true)

    try {
      // Build payload
      const payload = buildConfigurationPayload(
        startDate,
        endDate,
        configurationExecutionDetails,
        PLANT_ID,
        AOP_YEAR,
      )

      if (!payload) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Start/End date configuration is incomplete.',
          severity: 'error',
        })
        return
      }

      // Execute configuration
      const response = await executeConfiguration(payload, keycloak)

      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Started Successfully!',
          severity: 'success',
        })
        await fetchConfigurationDetails()
        await fetchSummary()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Execution Failed!', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Execution Failed!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
      setLoading1(false)
      setDateEdited(false)
    }
  }

  const handleOpenDialogRev = (num) => {
    setSelectedRevNum(num)
    setOpenConfirmDialogRev(true)
  }

  const handleCloseDialogRev = () => {
    setOpenConfirmDialogRev(false)
    setSelectedRevNum(null)
  }

  const handleConfirmLoadRev = () => {
    setOpenConfirmDialogRev(false)
    handleRevisionChange(selectedRevNum)
  }

  const handleRevisionChange = async (num) => {
    setRevision(num)
    console.log('revisionDetails:', revisionDetails)
    // if (!revisionDetails || revisionDetails.length === 0) {
    //   console.log('Returning early - revisionDetails is null or empty')
    //   return
    // }
    const payload = revisionDetails ? { ...revisionDetails } : {}
    payload.attributeValueVersion = num
    payload.attributeValue = num
    await updateRevision([payload])
  }

  const updateRevision = async (Payload) => {
    try {
      // var response = await DataService.updateRevision(
      //   keycloak,
      //   Payload,
      //   PLANT_ID,
      //   AOP_YEAR,
      // )
      // fetchData()
      setRevisionUpdated(true)
      console.log('Payload', Payload)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Revision updated successfully!',
        severity: 'success',
        duration: 3000,
      })
    } catch (error) {
      console.error('Error updating data:', error)
    }
  }

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    setTabIndex(0)
    fetchConfigurationDetails()
    fetchSummary()
    getConfigurationTabsMatrix()
    getConfigurationAvailableTabs()
    setSummaryEdited(false)
    setDateEdited(false)
  }, [
    PLANT_ID,
    AOP_YEAR,
    fetchConfigurationDetails,
    fetchSummary,
    getConfigurationTabsMatrix,
    getConfigurationAvailableTabs,
  ])

  const [start, end] = AOP_YEAR ? AOP_YEAR.split('-').map(Number) : [0, 0]
  const prevYearFormatted = `${start - 1}-${(start - 1 + 1).toString().slice(-2)}`

  // Hardcoded tabs (commented - now using API)
  // const tablist = [
  //   'Configuration',
  //   'Constants',
  //   `Report Manual Entry (${prevYearFormatted})`,
  // ]

  // Helper function to get tab display name by matching the UUID from tabs array
  const getTabName = (tabId) => {
    if (!tabId || !availableTabs.length) return null
    const tab = availableTabs.find(
      (t) => t.id.toLowerCase() === tabId.toLowerCase(),
    )
    return tab ? tab.displayName : null
  }

  // Dynamic tab list from API (filtered to exclude 'Report Manual Entry')
  const tablist = tabs
    .map((tabId) => {
      if (!tabId || !availableTabs.length) return ''
      const tabInfo = availableTabs.find(
        (tab) => tab.id.toLowerCase() === tabId.toLowerCase(),
      )

      if (tabInfo) {
        const originalName = tabInfo.displayName
        // Filter out Report Manual Entry
        if (originalName === 'Report Manual Entry') {
          return null
        }
        return originalName
      }
      return tabId
    })
    .filter((tab) => tab !== null)

  const renderTab = () => {
    if (!tabs.length || !availableTabs.length) {
      return null
    }

    const currentTabId = tabs[tabIndex]
    if (!currentTabId) return null

    const currentTabName = getTabName(currentTabId)

    switch (currentTabName) {
      case 'Configuration':
        return (
          <Configuration
            revisionUpdated={revisionUpdated}
            setRevisionUpdated={setRevisionUpdated}
          />
        )
      case 'Constants':
        return (
          <Constants
            revisionUpdated={revisionUpdated}
            setRevisionUpdated={setRevisionUpdated}
          />
        )
      case 'Report Manual Entry':
        return <ReportManualEntry />
      default:
        return null
    }
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading1}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Stack sx={{ mt: 1, mb: 1 }}>
        <ConfigurationAccordian
          startDate={startDate}
          endDate={endDate}
          summary={summary}
          configurationExecutionDetails={configurationExecutionDetails}
          isOldYear={isOldYear}
          READ_ONLY={READ_ONLY}
          setStartDate={setStartDate}
          setEndDate={setEndDate}
          setSummary={setSummary}
          setDateEdited={setDateEdited}
          setSummaryEdited={setSummaryEdited}
          handleOpenDialog={handleOpenDialog}
        />
      </Stack>

      {tabs.length > 0 && availableTabs.length > 0 && (
        <Stack
          direction='row'
          justifyContent='space-between'
          alignItems='center'
        >
          <TabSection
            tabIndex={tabIndex}
            setTabIndex={setTabIndex}
            tabs={tablist}
          />

          <Box>
            <ButtonGroup aria-label='revision group'>
              {['1', '2', '3'].map((num) => {
                const selected = revision === num

                return (
                  <Button
                    key={num}
                    onClick={() => handleOpenDialogRev(num)}
                    variant={selected ? 'contained' : 'outlined'}
                    size='small'
                    sx={{
                      textTransform: 'none',
                      fontSize: '0.75rem',
                      padding: '5px 10px',
                      minWidth: '36px',
                      mr: 0.5,
                      ...(selected && {
                        bgcolor: '#0100cb',
                        color: '#fff',
                        borderColor: '#0100cb',
                        fontWeight: 'bold',
                      }),
                      ...(!selected && {
                        borderColor: '#000000ff',
                        color: '#000000ff',
                        fontWeight: 'bold',
                      }),
                    }}
                  >
                    {`Rev ${num}`}
                  </Button>
                )
              })}
            </ButtonGroup>
          </Box>
        </Stack>
      )}

      {/* Tab Content */}
      <Box sx={{ mt: 2 }}>{renderTab()}</Box>

      {/* Dialog sections */}
      <ConfigurationDialog
        open={openConfirmDialog}
        onClose={handleCloseDialog}
        onConfirm={handleConfirmLoad}
        startDate={startDate}
        endDate={endDate}
      />

      <Notification
        open={snackbarOpen}
        onClose={() => setSnackbarOpen(false)}
        message={snackbarData.message}
        severity={snackbarData.severity}
        duration={snackbarData.duration}
      />

      <RevConfirmDialog
        openConfirmDialogRev={openConfirmDialogRev}
        handleCloseDialogRev={handleCloseDialogRev}
        handleConfirmLoadRev={handleConfirmLoadRev}
      />
    </div>
  )
}

export default ProductionNormsBasis
