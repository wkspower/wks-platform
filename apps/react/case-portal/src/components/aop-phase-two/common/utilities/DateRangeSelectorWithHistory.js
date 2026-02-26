import { useState, useEffect, useCallback, useRef, useMemo } from 'react'
import { Box, Typography, Button } from '@mui/material'
import { DatePicker, DateTimePicker } from '@progress/kendo-react-dateinputs'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import Notification from './Notification'
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '../../../../../node_modules/@mui/material/index'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'

const DateRangeSelectorWithHistory = ({
  onDateChange,
  disabled = false,
  showLoadButton = true,
  loadButtonText = 'Load',
  startDateLabel = 'Start Date',
  endDateLabel = 'End Date',
  dateFormat = 'dd-MM-yyyy',
  datePickerHeight = '80px',
  datePickerSize = 'medium',
  timeRequired = false,
  containerSx = {},
  loadButtonSx = {},
  loadButtonClassName = 'btn-save',
  loadButtonVariant = 'contained',
  dateLoading = false,
  setDateLoading = () => {},
}) => {
  const hasExecutedRef = useRef(false)
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const [startDate, setStartDate] = useState(null)
  const [endDate, setEndDate] = useState(null)
  const [configurationExecutionDetails, setConfigurationExecutionDetails] =
    useState([])
  const [loading, setLoading] = useState(false)

  const [openConfirmDialog, setOpenConfirmDialog] = useState(false)

  const [startDateObj, setStartDateObj] = useState(null)
  const [endDateObj, setEndDateObj] = useState(null)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'success',
  })
  const PickerComponent = timeRequired ? DateTimePicker : DatePicker
  const pickerFormat = timeRequired ? 'dd-MM-yyyy hh:mm a' : dateFormat

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) {
      return
    }
    hasExecutedRef.current = false
    getConfigurationExecutionDetails()
  }, [PLANT_ID])

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
      onDateChange({ startDate, endDate })
    } else {
      const today = new Date()
      const fallbackEndDate = new Date(today.getFullYear(), today.getMonth(), 0)
      const fallbackStartDate = new Date(
        today.getFullYear() - 5,
        today.getMonth(),
        1,
      )
      setStartDate(fallbackStartDate)
      setEndDate(fallbackEndDate)
    }
  }, [configurationExecutionDetails, PLANT_ID])

  useEffect(() => {
    computeAndSetDates()
  }, [computeAndSetDates])

  function formatDate(date) {
    if (!date) return ''
    const year = date?.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  function formatDateForText(date, time = false) {
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
      hours = hours ? hours : 12 // 0 becomes 12
      const formattedTime = `${String(hours).padStart(2, '0')}:${minutes} ${ampm}`
      formatted += ` ${formattedTime}`
    }
    return formatted
  }

  const getConfigurationExecutionDetails = async () => {
    setDateLoading(true)
    try {
      const response = await InputApiService.getConfigurationExecutionDetails(
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
      }
    } catch (error) {
      console.error('Error fetching getConfigurationExecutionDetails:', error)
    } finally {
      setDateLoading(false)
    }
  }

  const onLoadTest = async (startDateObj, endDateObj) => {
    const today = new Date()
    const endDate = new Date(today.getFullYear(), today.getMonth(), 0)
    const startDate = new Date(today.getFullYear() - 5, today.getMonth(), 1)

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
      setDateLoading(true)
      const response = await InputApiService.executeConfiguration(
        payload,
        keycloak,
      )
      if (response?.code === 200) {
        await getConfigurationExecutionDetails()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Failed!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Execution Failed!', error)
    } finally {
      setLoading(false)
      setDateLoading(false)
    }
  }

  const handleStartDateChange = (value) => {
    setStartDate(value)
  }

  const handleEndDateChange = (value) => {
    setEndDate(value)
  }

  const handleOpenDialog = () => {
    setOpenConfirmDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenConfirmDialog(false)
  }

  const handleConfirmLoad = () => {
    setOpenConfirmDialog(false)
    onLoad()
  }

  const onLoad = async () => {
    console.log('onLoad called with startDate:', startDate, 'endDate:', endDate)

    if (startDate && endDate) {
      const s = new Date(startDate)
      const e = new Date(endDate)

      s.setHours(0, 0, 0, 0)
      e.setHours(0, 0, 0, 0)

      if (s > e) {
        setSnackbarOpen(true)
        setSnackbarData({
          message:
            'Please choose valid dates (start date must be before end date).',
          severity: 'warning',
        })
        return
      }

      const minEnd = new Date(s)
      minEnd.setFullYear(minEnd.getFullYear() + 1)
    }
    const startDateObj = configurationExecutionDetails.find(
      (item) => item.Name === 'StartDate',
    )
    const endDateObj = configurationExecutionDetails.find(
      (item) => item.Name === 'EndDate',
    )
    if (!startDateObj?.Id || !endDateObj?.Id) {
      console.warn(
        'StartDate or EndDate object is missing Id. Aborting execution.',
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Start/End date configuration is incomplete.',
        severity: 'error',
      })
      return
    }
    setLoading(true)
    setDateLoading(true)
    try {
      setStartDateObj(startDateObj)
      setEndDateObj(endDateObj)

      const formattedStartDate = formatDate(startDate)
      const formattedEndDate = formatDate(endDate)

      console.log(
        'Formatted dates - Start:',
        formattedStartDate,
        'End:',
        formattedEndDate,
      )

      const payload = [
        {
          apr: formattedStartDate,
          UOM: '',
          auditYear: AOP_YEAR,
          normParameterFKId: startDateObj?.NormParameter_FK_Id,
          remarks: 'Initiated',
          id: startDateObj?.Id || null,
          plantId: PLANT_ID,
        },
        {
          apr: formattedEndDate,
          UOM: '',
          auditYear: AOP_YEAR,
          normParameterFKId: endDateObj?.NormParameter_FK_Id,
          remarks: 'Initiated',
          id: endDateObj?.Id || null,
          plantId: PLANT_ID,
        },
      ]

      console.log('Payload being sent:', JSON.stringify(payload, null, 2))
      const response = await InputApiService.executeConfiguration(
        payload,
        keycloak,
      )
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Started Successfully!',
          severity: 'success',
        })
        getConfigurationExecutionDetails()
        setLoading(false)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Falied!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Execution Falied!', error)
      setLoading(false)
    } finally {
      setLoading(false)
      setDateLoading(false)
    }
  }

  const startDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'StartDate',
  )

  const endDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'EndDate',
  )

  const startDateFromConfig = new Date(startDateConfig?.AttributeValue)
  const endDateDateFromConfig = new Date(endDateConfig?.AttributeValue)

  const ConfigurationDialog = useMemo(() => {
    return (
      <Dialog
        open={openConfirmDialog}
        onClose={handleCloseDialog}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
        disableScrollLock
      >
        <DialogTitle id='alert-dialog-title'>{'Load?'}</DialogTitle>
        <DialogContent>
          <DialogContentText
            id='alert-dialog-description'
            sx={{ color: 'text.primary' }}
          >
            {`Are you sure you want to load data for the period from ${formatDateForText(startDate)} to ${formatDateForText(endDate)}?`}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleConfirmLoad} autoFocus>
            Load
          </Button>
        </DialogActions>
      </Dialog>
    )
  }, [openConfirmDialog])

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'flex-end',
        gap: 1,
        ...containerSx,
      }}
    >
      {/* Start Date */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
        <Typography className='button-title' sx={{ whiteSpace: 'nowrap' }}>
          {startDateLabel}
        </Typography>
        <PickerComponent
          id='start-date'
          format={pickerFormat}
          value={startDate}
          onChange={(e) => handleStartDateChange(e.value)}
          style={{ height: datePickerHeight }}
          size={datePickerSize}
          disabled={disabled || loading}
          {...(timeRequired && {
            autoFill: true,
            enableMouseWheel: true,
            steps: { hour: 1, minute: 1, second: 0 },
          })}
        />
      </Box>

      {/* End Date */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
        <Typography className='button-title' sx={{ whiteSpace: 'nowrap' }}>
          {endDateLabel}
        </Typography>
        <PickerComponent
          id='end-date'
          format={pickerFormat}
          value={endDate}
          onChange={(e) => handleEndDateChange(e.value)}
          style={{ height: datePickerHeight }}
          size={datePickerSize}
          disabled={disabled || loading}
          {...(timeRequired && {
            autoFill: true,
            enableMouseWheel: true,
            steps: { hour: 1, minute: 1, second: 0 },
          })}
        />
      </Box>

      {/* Load Button */}
      {showLoadButton && (
        <Button
          variant={loadButtonVariant}
          onClick={handleOpenDialog}
          className={loadButtonClassName}
          sx={loadButtonSx}
          disabled={disabled || loading}
        >
          {loading ? 'Loading...' : loadButtonText}
        </Button>
      )}

      {/* Last Refreshed Message */}
      {configurationExecutionDetails[0]?.ModifiedOn && (
        <Typography
          className={disabled ? 'summary-title-disabled' : 'summary-title'}
          sx={{
            whiteSpace: 'normal',
            alignItems: 'center',
          }}
        >
          {`(Last refreshed data on: ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)} for the period from ${formatDateForText(startDateFromConfig)} to ${formatDateForText(endDateDateFromConfig)})`}
        </Typography>
      )}

      {/* snackbar toaster */}
      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />
      {ConfigurationDialog}
    </Box>
  )
}

export default DateRangeSelectorWithHistory
