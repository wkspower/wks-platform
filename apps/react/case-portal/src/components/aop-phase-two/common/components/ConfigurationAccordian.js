import React, { useMemo, useState, useEffect, useCallback, useRef } from 'react'
import { Box, Button, Typography } from '@mui/material'
import { DatePicker } from '@progress/kendo-react-dateinputs'
import { TextArea } from '@progress/kendo-react-inputs'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/aop-phase-two/common/utilities/Notification'
import {
  validateDateRange,
  buildConfigurationPayload,
} from '../../crude/production-norms-basis/utils/utility'
import ConfigurationDialog from './ConfigurationDialog'
import { HistoricPeriodBasisApiService } from 'components/aop-phase-two/services/common/historicPeriodBasisApiService'
import {
  Backdrop,
  CircularProgress,
} from '../../../../../node_modules/@mui/material/index'

const ConfigurationAccordian = ({
  PLANT_ID,
  AOP_YEAR,
  READ_ONLY,
  isOldYear,
  isSummaryRequired = false,
  yearGap = 1,
}) => {
  const keycloak = useSession()
  const hasExecutedRef = useRef(false)

  // State management
  const [startDate, setStartDate] = useState()
  const [endDate, setEndDate] = useState()
  const [summary, setSummary] = useState('')
  const [lastModifiedBy, setLastModifiedBy] = useState('')
  const [dateEdited, setDateEdited] = useState(false)
  const [summaryEdited, setSummaryEdited] = useState(false)
  const [configurationExecutionDetails, setConfigurationExecutionDetails] =
    useState([])
  const [openConfirmDialog, setOpenConfirmDialog] = useState(false)
  const [loading, setLoading] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  // Helper function to format dates for API
  const formatDate = (date) => {
    if (!date) return ''
    const year = date?.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  // Helper function to format dates for display
  const formatDateForText = (date, time = false) => {
    if (!date) return ''
    const parsedDate = new Date(date)
    if (isNaN(parsedDate)) return 'Invalid Date'
    const day = String(parsedDate.getDate()).padStart(2, '0')
    const month = String(parsedDate.getMonth() + 1).padStart(2, '0')
    const year = parsedDate.getFullYear()
    let formatted = `${day}-${month}-${year}`
    if (time) {
      let hours = parsedDate.getHours()
      const minutes = String(parsedDate.getMinutes()).padStart(2, '0')
      const ampm = hours >= 12 ? 'PM' : 'AM'
      hours = hours % 12
      hours = hours ? hours : 12
      const formattedTime = `${String(hours).padStart(2, '0')}:${minutes} ${ampm}`
      formatted += ` ${formattedTime}`
    }
    return formatted
  }

  // Fetch configuration execution details
  const fetchConfigurationDetails = async () => {
    try {
      const response =
        await HistoricPeriodBasisApiService.getConfigurationExecutionDetails(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      const details = response?.data || []
      if (details.length === 0) {
        console.warn(
          'getConfigurationExecutionDetails returned an empty array:',
          response,
        )
      }
      const hasNoModifiedOn = details.length && !details[0]?.ModifiedOn
      if (hasNoModifiedOn && !hasExecutedRef.current) {
        const startDateObj = details.find((item) => item.Name === 'StartDate')
        const endDateObj = details.find((item) => item.Name === 'EndDate')
        hasExecutedRef.current = true
        await onLoadTest(startDateObj, endDateObj)
      } else {
        setConfigurationExecutionDetails(details)
        // Capture who last modified the data
        if (details[0]?.User) {
          setLastModifiedBy(details[0].User)
        }
      }
    } catch (error) {
      console.error('Error fetching getConfigurationExecutionDetails:', error)
    }
  }

  // Initial load with configurable year period
  const onLoadTest = async (startDateObj, endDateObj) => {
    const today = new Date()
    const endDate = new Date(today.getFullYear(), today.getMonth(), 0)
    const startDate = new Date(
      today.getFullYear() - yearGap,
      today.getMonth(),
      1,
    )

    const createPayloadItem = (obj, date) => ({
      apr: date,
      UOM: '',
      auditYear: AOP_YEAR,
      normParameterFKId: obj?.NormParameter_FK_Id,
      remarks: 'Initiated',
      id: obj?.Id || null,
      plantId: PLANT_ID,
    })

    const payload = [
      createPayloadItem(startDateObj, formatDate(startDate)),
      createPayloadItem(endDateObj, formatDate(endDate)),
    ]

    try {
      setLoading(true)
      const response = await HistoricPeriodBasisApiService.executeConfiguration(
        payload,
        keycloak,
      )
      if (response?.code === 200) {
        await fetchConfigurationDetails()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Failed!',
          severity: 'error',
        })
      }
      await fetchSummary()
      return response
    } catch (error) {
      console.error('Execution Failed!', error)
    } finally {
      setLoading(false)
    }
  }

  // Fetch AOP summary
  const fetchSummary = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      const res = await HistoricPeriodBasisApiService.getAopSummary(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.code === 200) {
        setSummary(res?.data?.summary || '')
      } else {
        setSummary('')
      }
    } catch (error) {
      console.error('Error fetching summary:', error)
    }
  }

  // Compute and set dates based on configuration details
  const computeAndSetDates = useCallback(() => {
    setStartDate('')
    setEndDate('')
    const hasModifiedOn = configurationExecutionDetails[0]?.ModifiedOn
    if (hasModifiedOn) {
      const getDateValue = (name) =>
        new Date(
          configurationExecutionDetails.find(
            (item) => item.Name === name,
          )?.AttributeValue,
        )
      setStartDate(getDateValue('StartDate'))
      setEndDate(getDateValue('EndDate'))
    } else {
      const today = new Date()
      const fallbackEndDate = new Date(today.getFullYear(), today.getMonth(), 0)
      const fallbackStartDate = new Date(
        today.getFullYear() - yearGap,
        today.getMonth(),
        1,
      )
      setStartDate(fallbackStartDate)
      setEndDate(fallbackEndDate)
    }
  }, [configurationExecutionDetails])

  // Dialog handlers
  const handleOpenDialog = () => {
    // Validate summary if required
    if (isSummaryRequired) {
      if (!summary || summary.trim() === '') {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Please add AOP Design Basis.',
          severity: 'error',
        })
        return
      }

      if (!summaryEdited) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Please update AOP Design Basis.',
          severity: 'error',
        })
        return
      }
    }

    setOpenConfirmDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenConfirmDialog(false)
  }

  const handleConfirmLoad = async () => {
    setOpenConfirmDialog(false)
    await onLoad()
  }

  // Load data with user-selected dates
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

    try {
      setLoading(true)

      // Save summary first if required
      if (isSummaryRequired) {
        await saveSummary(summary)
        setSummaryEdited(false)
      }

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
        setLoading(false)
        return
      }

      // Execute configuration
      const response = await HistoricPeriodBasisApiService.executeConfiguration(
        payload,
        keycloak,
      )

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
      setDateEdited(false)
    }
  }

  const saveSummary = async (summary) => {
    try {
      const response = await HistoricPeriodBasisApiService.saveAOPSummary(
        PLANT_ID,
        AOP_YEAR,
        summary,
        keycloak,
      )

      return response
    } catch (error) {
      // console.error('Error saving Summary!', error)
    } finally {
      //
      // setLoading(false)
      fetchSummary()
    }
  }

  // Initialize on mount and when PLANT_ID/AOP_YEAR changes
  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    hasExecutedRef.current = false
    fetchConfigurationDetails()
    fetchSummary()
    setSummaryEdited(false)
    setDateEdited(false)
  }, [PLANT_ID, AOP_YEAR])

  // Compute dates when configuration details change
  useEffect(() => {
    computeAndSetDates()
  }, [computeAndSetDates])

  const startDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'StartDate',
  )

  const endDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'EndDate',
  )

  const startDateFromConfig = new Date(startDateConfig?.AttributeValue)
  const endDateDateFromConfig = new Date(endDateConfig?.AttributeValue)

  const accordian = useMemo(() => {
    return (
      <Box sx={{ mb: '0px' }}>
        <CustomAccordion defaultExpanded disableGutters>
          <CustomAccordionSummary
            aria-controls='meg-grid-content'
            id='meg-grid-header'
          >
            <Typography className='accordian-title'>
              AOP Historical Period Basis
            </Typography>
          </CustomAccordionSummary>
          <CustomAccordionDetails>
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-end',
                mt: 0,
              }}
            >
              <Box
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                  marginTop: '5px',
                }}
              >
                {true && (
                  <Box
                    sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}
                  >
                    {/* Start Date */}
                    <Box
                      sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}
                    >
                      <Typography
                        className='button-title'
                        sx={{ whiteSpace: 'nowrap' }}
                      >
                        Start Date
                      </Typography>
                      <DatePicker
                        id='start-date'
                        format='dd-MM-yyyy'
                        value={startDate}
                        onChange={(e) => {
                          setStartDate(e.value)
                          setDateEdited(true)
                        }}
                        style={{ height: '80px' }}
                        size='medium'
                        disabled={READ_ONLY}
                      />
                    </Box>

                    {/* End Date */}
                    <Box
                      sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}
                    >
                      <Typography
                        className='button-title'
                        sx={{ whiteSpace: 'nowrap' }}
                      >
                        End Date
                      </Typography>
                      <DatePicker
                        id='end-date'
                        format='dd-MM-yyyy'
                        value={endDate}
                        onChange={(e) => {
                          setEndDate(e.value)
                          setDateEdited(true)
                        }}
                        style={{ height: '80px' }}
                        size='medium'
                        disabled={READ_ONLY}
                      />
                    </Box>

                    {/* Load Button */}
                    {!isOldYear && (
                      <Button
                        variant='contained'
                        onClick={handleOpenDialog}
                        className='btn-save'
                        sx={{ alignSelf: 'flex-end' }}
                        disabled={READ_ONLY}
                      >
                        Load
                      </Button>
                    )}
                  </Box>
                )}

                {configurationExecutionDetails[0]?.ModifiedOn && (
                  <Typography
                    className={
                      READ_ONLY ? 'summary-title-disabled' : 'summary-title'
                    }
                    sx={{
                      whiteSpace: 'normal',
                      alignSelf: 'flex-end',
                    }}
                  >
                    {`(Last refreshed data on: ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)}${lastModifiedBy ? ` by ${lastModifiedBy}` : ''} for the period from ${formatDateForText(startDateFromConfig)} to ${formatDateForText(endDateDateFromConfig)})`}
                  </Typography>
                )}
              </Box>
            </Box>

            <Box
              sx={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-start',
                gap: 0,
                mt: 1,
              }}
            >
              <Typography
                className='button-title'
                sx={{ whiteSpace: 'nowrap' }}
              >
                AOP Design Basis
              </Typography>

              <TextArea
                disabled={READ_ONLY}
                value={summary}
                rows={3}
                onChange={(e) => {
                  setSummary(e.target.value)
                  setSummaryEdited(true)
                }}
              />
            </Box>
          </CustomAccordionDetails>
        </CustomAccordion>
      </Box>
    )
  }, [
    startDate,
    endDate,
    summary,
    configurationExecutionDetails,
    isOldYear,
    READ_ONLY,
  ])

  return (
    <>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      {accordian}

      {/* Confirmation Dialog */}
      <ConfigurationDialog
        open={openConfirmDialog}
        onClose={handleCloseDialog}
        onConfirm={handleConfirmLoad}
        startDate={startDate}
        endDate={endDate}
      />

      {/* Notification */}
      <Notification
        open={snackbarOpen}
        onClose={() => setSnackbarOpen(false)}
        message={snackbarData.message}
        severity={snackbarData.severity}
      />
    </>
  )
}

export default ConfigurationAccordian
