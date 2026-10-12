import React, {
  useMemo,
  useState,
  useEffect,
  useCallback,
  useRef,
  Fragment,
} from 'react'
import {
  Box,
  Button,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material'
import { CalenderIcon } from 'assets/images/icons'
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
import dataGridStore from 'store/reducers/dataGridStore'
import { getRoleName } from 'services/role-service'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { styled } from '@mui/material/styles'
import SettingsIcon from '@mui/icons-material/Settings'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import InfoIcon from '@mui/icons-material/Info'
import { useSelector } from 'react-redux'

const CompactAccordion = styled(CustomAccordion)({
  mb: 0,
  padding: '10px',
  '&:before': { display: 'none' },
})

const ConfigurationAccordian = ({
  PLANT_ID,
  AOP_YEAR,
  isOldYear,
  isSummaryRequired = false,
  yearGap = 1,
  onDatesChange,
  onLoadNormCalculation = () => {},
  normCalculationLoading = false,
}) => {
  const keycloak = useSession()
  const hasExecutedRef = useRef(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { isReleased, oldYear, plantObject, siteObject, verticalObject } =
    dataGridStore
  const IS_OLD_YEAR = oldYear?.oldYear
  const IS_RELEASED = isReleased
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, IS_RELEASED)
  const VERTICAL_NAME = verticalObject?.name?.toLowerCase() || ''
  const SITE_NAME = siteObject?.name?.toLowerCase() || ''
  const PLANT_NAME = plantObject?.name?.toLowerCase() || ''

  const isThreeDates = useMemo(() => {
    const validConfigs = [
      { vertical: 'hydrotreater', site: 'sez', plant: 'vgoht-4' },
      { vertical: 'hydrotreater', site: 'sez', plant: 'vgoht-3' },
      { vertical: 'hydrotreater', site: 'dta', plant: 'vgoht-1' },
      { vertical: 'hydrotreater', site: 'dta', plant: 'vgoht-2' },
      { vertical: 'hydrotreater', site: 'dta', plant: 'dht1' },
      { vertical: 'hydrotreater', site: 'dta', plant: 'dht2' },
    ]
    return validConfigs.some(
      (config) =>
        config.vertical === VERTICAL_NAME &&
        config.site === SITE_NAME &&
        config.plant === PLANT_NAME,
    )
  }, [VERTICAL_NAME, SITE_NAME, PLANT_NAME])

  //HIDE FOR CT's Plants
  const isHideLoadButton = useMemo(() => {
    const validConfigs = [
      { vertical: 'refinery utility', site: 'dmd' }
      // { vertical: 'refinery utility', site: 'dta', plant: 'ct-4' },
      // { vertical: 'refinery utility', site: 'dta', plant: 'ct-6' },
    ]
    return validConfigs.some(
      (config) =>
        config.vertical === VERTICAL_NAME &&
        config.site === SITE_NAME &&
        (!config.plant || config.plant === PLANT_NAME),
    )
  }, [VERTICAL_NAME, SITE_NAME, PLANT_NAME])

  // State management
  const [startDate, setStartDate] = useState()
  const [endDate, setEndDate] = useState()
  const [startShow, setStartShow] = useState(false)
  const [endShow, setEndShow] = useState(false)
  const [sorStartDate, setSorStartDate] = useState()
  const [sorEndDate, setSorEndDate] = useState()
  const [sorStartShow, setSorStartShow] = useState(false)
  const [sorEndShow, setSorEndShow] = useState(false)
  const [slowDownStartDate, setSlowDownStartDate] = useState()
  const [slowDownEndDate, setSlowDownEndDate] = useState()
  const [slowDownStartShow, setSlowDownStartShow] = useState(false)
  const [slowDownEndShow, setSlowDownEndShow] = useState(false)
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
    const parsedDate = new Date(date)
    if (isNaN(parsedDate.getTime())) return ''
    const year = parsedDate.getFullYear()
    const month = String(parsedDate.getMonth() + 1).padStart(2, '0')
    const day = String(parsedDate.getDate()).padStart(2, '0')
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

  // Calculate Last 1 Year dates from selected AOP_YEAR
  const getDatesFromAopYear = useCallback(
    (aopYear, gap = yearGap) => {
      let startYear = null
      if (typeof aopYear === 'string' && aopYear.includes('-')) {
        startYear = parseInt(aopYear.split('-')[0], 10)
      } else if (aopYear) {
        startYear = parseInt(aopYear, 10)
      }

      if (startYear && !isNaN(startYear)) {
        const calcStartDate = new Date(startYear - (gap || 1), 3, 1) // 1st April of (startYear - gap)
        const calcEndDate = new Date(startYear, 2, 31) // 31st March of startYear
        return { startDate: calcStartDate, endDate: calcEndDate }
      }

      const today = new Date()
      const calcEndDate = new Date(today.getFullYear(), today.getMonth(), 0)
      const calcStartDate = new Date(
        today.getFullYear() - (gap || 1),
        today.getMonth(),
        1,
      )
      return { startDate: calcStartDate, endDate: calcEndDate }
    },
    [yearGap],
  )

  // Initial load with configurable year period
  const onLoadTest = async (startDateObj, endDateObj) => {
    const { startDate: aopStartDate, endDate: aopEndDate } =
      getDatesFromAopYear(AOP_YEAR, yearGap)
    let effectiveStartDate = startDate
    let effectiveEndDate = endDate
    if (isHideLoadButton) {
      effectiveStartDate = aopStartDate
      effectiveEndDate = aopEndDate
    }

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
      createPayloadItem(startDateObj, formatDate(effectiveStartDate)),
      createPayloadItem(endDateObj, formatDate(effectiveEndDate)),
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
      const getDateValue = (name) => {
        const parsedDate = new Date(
          configurationExecutionDetails.find(
            (item) => item.Name === name,
          )?.AttributeValue,
        )
        return isNaN(parsedDate.getTime()) ? null : parsedDate
      }
      setStartDate(getDateValue('StartDate'))
      setEndDate(getDateValue('EndDate'))
      setSorStartDate(getDateValue('SORStartDate'))
      setSorEndDate(getDateValue('SOREndDate'))
      setSlowDownStartDate(getDateValue('SlowDownStartDate'))
      setSlowDownEndDate(getDateValue('SlowDownEndDate'))
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
      setSorStartDate(fallbackStartDate)
      setSorEndDate(fallbackEndDate)
      setSlowDownStartDate(fallbackStartDate)
      setSlowDownEndDate(fallbackEndDate)
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

    const sorConfigExists = configurationExecutionDetails.some(
      (item) =>
        (item.Name === 'SORStartDate' || item.Name === 'SOREndDate') && item.Id,
    )
    if (isThreeDates && sorConfigExists) {
      const sorValidation = validateDateRange(sorStartDate, sorEndDate)
      if (!sorValidation.valid) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: `SOR: ${sorValidation.message}`,
          severity: 'warning',
        })
        return
      }
    }

    try {
      setLoading(true)

      // Save summary first if required
      // if (isSummaryRequired) {
      await saveSummary(summary)
      setSummaryEdited(false)
      // }

      // Build payload
      const payload = buildConfigurationPayload(
        startDate,
        endDate,
        configurationExecutionDetails,
        PLANT_ID,
        AOP_YEAR,
        sorStartDate,
        sorEndDate,
        slowDownStartDate,
        slowDownEndDate
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

        // Trigger norm calculation after successful configuration load
        if (onLoadNormCalculation) {
          onLoadNormCalculation()
        }
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

  const carryForwardRecords = async () => {
    try {
      const response = await HistoricPeriodBasisApiService.carryForwardRecords(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response && response.code === 200) {
        // console.log('Carry forward successful, status 200.')
      } else {
        console.warn(
          `Carry forward request completed but status was not 200: ${response?.status}`,
        )
      }
    } catch (error) {
      console.error('Error fetching getConfigurationExecutionDetails:', error)
    } finally {
      // setLoading1(false)
    }
  }

  // Initialize on mount and when PLANT_ID/AOP_YEAR changes
  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    hasExecutedRef.current = false
    carryForwardRecords()
    fetchConfigurationDetails()
    fetchSummary()
    setSummaryEdited(false)
    setDateEdited(false)
  }, [PLANT_ID, AOP_YEAR])

  // Compute dates when configuration details change
  useEffect(() => {
    computeAndSetDates()
  }, [computeAndSetDates])

  // useEffect(() => {
  //   if (isHideLoadButton && !hasExecutedRef.current && configurationExecutionDetails.length > 0) {
  //     const startDateObj = configurationExecutionDetails.find((item) => item.Name === 'StartDate')
  //     const endDateObj = configurationExecutionDetails.find((item) => item.Name === 'EndDate')
  //     if (startDateObj && endDateObj) {
  //       hasExecutedRef.current = true
  //       onLoadTest(startDateObj, endDateObj)
  //     }
  //   }
  // }, [isHideLoadButton, configurationExecutionDetails])

  // Notify parent component when dates change
  useEffect(() => {
    if (onDatesChange && startDate && endDate) {
      onDatesChange(startDate, endDate, sorStartDate, sorEndDate, slowDownStartDate, slowDownEndDate)
    }
  }, [startDate, endDate, sorStartDate, sorEndDate, slowDownStartDate, slowDownEndDate, onDatesChange])

  const startDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'StartDate',
  )

  const endDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'EndDate',
  )

  const startDateFromConfig = new Date(startDateConfig?.AttributeValue)
  const endDateDateFromConfig = new Date(endDateConfig?.AttributeValue)

  const renderDatePickerPill = (label, value, setValue, show, setShow, id) => {
    return (
      <Box className='date-pill-wrapper'>
        <Box
          component='img'
          src={CalenderIcon}
          className='w16-icon'
          style={{
            cursor: READ_ONLY || isHideLoadButton ? 'not-allowed' : 'pointer',
          }}
          onClick={() => !(READ_ONLY || isHideLoadButton) && setShow((v) => !v)}
        />
        <Box component='span' className='header-dropdown-label'>
          {label}:
        </Box>
        <DatePicker
          id={id}
          format='dd-MM-yyyy'
          value={
            value instanceof Date && !isNaN(value.getTime()) ? value : null
          }
          show={show}
          onClose={() => setShow(false)}
          onChange={(e) => {
            const newValue = e.value
            setValue(
              newValue instanceof Date && isNaN(newValue.getTime())
                ? null
                : newValue,
            )
            setDateEdited(true)
          }}
          disabled={READ_ONLY || isHideLoadButton}
        />
        <IconButton
          style={{
            cursor: READ_ONLY || isHideLoadButton ? 'not-allowed' : 'pointer',
            p: 0,
            width: 0,
            height: 0,
          }}
          onClick={() => !(READ_ONLY || isHideLoadButton) && setShow((v) => !v)}
          size='small'
        >
          <ExpandMoreIcon sx={{ fontSize: '1rem', color: '#606060' }} />
        </IconButton>
      </Box>
    )
  }

  const accordian = useMemo(() => {
    const expandCollapseIconStyle = {
      minHeight: '36px !important',
      px: 0.5,
      bgcolor: '#ffffff',
      flexDirection: 'row-reverse',
      '& .MuiAccordionSummary-content': {
        marginLeft: 1,
        my: '4px !important',
      },
      '& .MuiAccordionSummary-expandIconWrapper': {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: 32,
        height: 32,
        borderRadius: '6px',
        backgroundColor: '#ECEEFF',
        color: '#1e293b',
        cursor: 'pointer',
        padding: '8px',
      },
    }

    return (
      <Box sx={{ mb: 1 }}>
        <CompactAccordion
          defaultExpanded
          disableGutters
          className='k-table-box'
        >
          <CustomAccordionSummary
            expandIcon={<ExpandMoreIcon sx={{ fontSize: '1rem' }} />}
            sx={expandCollapseIconStyle}
          >
            <Stack direction='row' spacing={1} alignItems='center'>
              <SettingsIcon sx={{ color: '#0100cb', fontSize: '1rem' }} />
              <Typography
                sx={{
                  fontWeight: 700,
                  fontSize: '16px',
                  fontFamily: "'Honeywell Sans Web', 'Inter', sans-serif",
                  color: '#303030',
                }}
              >
                AOP Historical Period Basis
              </Typography>
            </Stack>
          </CustomAccordionSummary>

          <CustomAccordionDetails sx={{ p: 0.5, pt: 1 }}>
            <Stack direction='column' spacing={1.5}>
              {/* ROW 1: Date pickers + Load button */}
              <Stack
                direction='row'
                sx={{ columnGap: 1.5, rowGap: 1.5 }}
                alignItems='flex-start'
                flexWrap='wrap'
              >
                <Stack direction='column' spacing={0.5}>
                  {isThreeDates && (
                    <Typography
                      variant='caption'
                      className='aop-design-basis-label'
                      sx={{ fontWeight: 700, color: '#333', fontSize: '14px' }}
                    >
                      Historical Period
                    </Typography>
                  )}
                  
                  {/* Normal Operation */}
                  <Stack direction='row' alignItems='center' spacing={2} style={{ marginLeft: isThreeDates ? '10px' : 0 }}>
                    {isThreeDates && (
                      <Typography variant='caption' className='aop-design-basis-label' sx={{ width: '160px', textAlign: 'left' }}>
                        Normal Operation
                      </Typography>
                    )}
                    <Stack
                      direction='row'
                      sx={{ columnGap: 1, rowGap: 0 }}
                      alignItems='center'
                      flexWrap='wrap'
                    >
                      {renderDatePickerPill(
                      'Start Date',
                      startDate,
                      setStartDate,
                      startShow,
                      setStartShow,
                      'start-date',
                    )}
                    {renderDatePickerPill(
                      'End Date',
                      endDate,
                      setEndDate,
                      endShow,
                      setEndShow,
                      'end-date',
                    )}
                    </Stack>
                  </Stack>

                  {/* Slow down operation */}
                  {isThreeDates && (
                    <Stack direction='row' alignItems='center' spacing={2} style={{ marginLeft: '10px' }}>
                      <Typography variant='caption' className='aop-design-basis-label' sx={{ width: '160px', textAlign: 'left' }}>
                        Slow Down operation
                      </Typography>
                      <Stack
                        direction='row'
                        sx={{ columnGap: 1, rowGap: 0 }}
                        alignItems='center'
                        flexWrap='wrap'
                      >
                        {renderDatePickerPill('Start Date', slowDownStartDate, setSlowDownStartDate, slowDownStartShow, setSlowDownStartShow, 'slow-down-start-date')}
                        {renderDatePickerPill('End Date', slowDownEndDate, setSlowDownEndDate, slowDownEndShow, setSlowDownEndShow, 'slow-down-end-date')}
                      </Stack>
                    </Stack>
                  )}

                  {/* Start of run Operation */}
                  {isThreeDates && (
                      <Stack direction='row' alignItems='center' spacing={2} style={{ marginLeft: isThreeDates ? '10px' : 0 }}>
                        {isThreeDates && (<Typography variant='caption' className='aop-design-basis-label' sx={{ width: '160px', textAlign: 'left' }}>
                          Start of run Operation
                        </Typography>)}
                        <Stack
                          direction='row'
                          sx={{ columnGap: 1, rowGap: 0 }}
                          alignItems='center'
                          flexWrap='wrap'
                        >
                          {renderDatePickerPill('Start Date', sorStartDate, setSorStartDate, sorStartShow, setSorStartShow, 'sor-start-date')}
                          {renderDatePickerPill('End Date', sorEndDate, setSorEndDate, sorEndShow, setSorEndShow, 'sor-end-date')}
                        </Stack>
                      </Stack>
                  )}
                </Stack>

                {/* LOAD BUTTON */}
                {!(isOldYear || isHideLoadButton) && (
                  <Tooltip title='Refresh Data'>
                    <Button
                      variant='outlined'
                      className='btn-load'
                      onClick={handleOpenDialog}
                      disabled={READ_ONLY}
                      sx={{
                        height: 28,
                        px: 1.5,
                        mt: 'auto',
                      }}
                    >
                      Load
                    </Button>
                  </Tooltip>
                )}
              </Stack>

              {/* LAST REFRESHED */}
              {configurationExecutionDetails[0]?.ModifiedOn && (
                <Tooltip
                  title={`Last loaded data : ${formatDateForText(
                    configurationExecutionDetails[0]?.ModifiedOn,
                    true,
                  )}`}
                >
                  <Stack
                    direction='row'
                    spacing={0.5}
                    alignItems='center'
                    className='last-refreshed-container'
                  >
                    <InfoIcon sx={{ fontSize: '0.9rem', color: '#00688C' }} />
                    <Typography className='last-refreshed-text'>
                      {`Last loaded data on ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)} by ${configurationExecutionDetails[0]?.User ?? ''} for period ${formatDateForText(startDate, false)} to ${formatDateForText(endDate, false)}`}
                    </Typography>
                  </Stack>
                </Tooltip>
              )}

              {/* ROW 2: AOP DESIGN BASIS */}
              {!isHideLoadButton && (
                <Box sx={{ width: '100%' }}>
                  <Typography
                    variant='caption'
                    className='aop-design-basis-label'
                  >
                    AOP DESIGN BASIS
                  </Typography>
                  <TextArea
                    className='vertical-resize-textarea'
                    disabled={READ_ONLY}
                    value={summary}
                    rows={2}
                    onChange={(e) => {
                      setSummary(e.target.value)
                      setSummaryEdited(true)
                    }}
                  />
                </Box>
              )}
            </Stack>
          </CustomAccordionDetails>
        </CompactAccordion>
      </Box>
    )
  }, [
    startDate,
    endDate,
    startShow,
    endShow,
    sorStartDate,
    sorEndDate,
    sorStartShow,
    sorEndShow,
    slowDownStartDate,
    slowDownEndDate,
    slowDownStartShow,
    slowDownEndShow,
    isThreeDates,
    summary,
    configurationExecutionDetails,
    isOldYear,
    READ_ONLY,
  ])

  return (
    <>
      <LoaderBackdrop open={!!loading || !!normCalculationLoading} />
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
