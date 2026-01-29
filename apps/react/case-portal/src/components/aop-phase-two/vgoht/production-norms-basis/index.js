import React, { useState, useEffect, useCallback, useRef } from 'react'
import {
  Backdrop,
  Box,
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

const ProductionNormsBasis = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { oldYear, plantObject, year } = dataGridStore

  const hasExecutedRef = useRef(false)

  const PLANT_ID = plantObject?.id
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
  const [openConfirmDialog, setOpenConfirmDialog] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

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

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    setTabIndex(0)
    fetchConfigurationDetails()
    fetchSummary()
    setSummaryEdited(false)
    setDateEdited(false)
  }, [PLANT_ID, AOP_YEAR, fetchConfigurationDetails, fetchSummary])

  const tablist = ['Configuration', 'Constants']

  const renderTab = () => {
    switch (tabIndex) {
      case 0:
        return <Configuration />
      case 1:
        return <Constants />
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

      <Box>
        <TabSection
          tabIndex={tabIndex}
          setTabIndex={setTabIndex}
          tabs={tablist}
        />
      </Box>

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
        setOpen={setSnackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
      />
    </div>
  )
}

export default ProductionNormsBasis
