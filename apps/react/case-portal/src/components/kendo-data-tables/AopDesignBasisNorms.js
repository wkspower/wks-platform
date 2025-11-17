import { Box } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import {
  Backdrop,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
  Typography,
} from '../../../node_modules/@mui/material/index'
import { DatePicker } from '../../../node_modules/@progress/kendo-react-dateinputs/index'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { getRoleName } from 'services/role-service'

const AopDesignBasisNorms = () => {
  const hasExecutedRef = useRef(false)
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak)

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    sitePlantChange,
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const isOldYear = oldYear?.oldYear
  const isOldYearFlag = oldYear?.oldYear === 1
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const [loading, setLoading] = useState(false)
  const [loading1, setLoading1] = useState(false)
  const [summaryEdited, setSummaryEdited] = useState(false)
  const [summary, setSummary] = useState('')
  const [debouncedSummary, setDebouncedSummary] = useState('')
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSummary(summary)
    }, 300)
    return () => clearTimeout(handler)
  }, [summary])
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [startDate, setStartDate] = useState()
  const [endDate, setEndDate] = useState()
  const [startDateObj, setStartDateObj] = useState([])
  const [endDateObj, setEndDateObj] = useState([])
  const [configurationExecutionDetails, setConfigurationExecutionDetails] =
    useState([])
  const [openConfirmDialog, setOpenConfirmDialog] = useState(false)

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

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return

    getConfigurationExecutionDetailsNorms()
  }, [PLANT_ID, AOP_YEAR])

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) {
      return
    }
    getConfigurationExecutionDetailsNorms()

    setTimeout(() => {
      if (
        lowerVertName != 'cracker' &&
        lowerVertName != 'meg' &&
        lowerVertName != 'elastomer'
      ) {
        getConfigurationTabsMatrix()
        getConfigurationAvailableTabs()
        fetchGradeData()
      }
    }, 500)
  }, [oldYear, yearChanged, keycloak, PLANT_ID])

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
      setStartDate(getDateValue('StartDateNorms'))
      setEndDate(getDateValue('EndDateNorms'))
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

  const onLoadTest = async (startDateObj, endDateObj) => {
    setLoading1(true)
    const plantId = PLANT_ID
    const auditYear = AOP_YEAR
    const today = new Date()
    const endDate = new Date(today.getFullYear(), today.getMonth(), 0)
    const startDate = new Date(today.getFullYear() - 5, today.getMonth(), 1)
    const createPayloadItem = (obj, date) => ({
      apr: date,
      UOM: '',
      auditYear,
      normParameterFKId: obj?.NormParameter_FK_Id,
      remarks: 'Initiated',
      id: obj?.Id || null,
      plantId,
    })
    const payload = [
      createPayloadItem(startDateObj, formatDate(startDate)),
      createPayloadItem(endDateObj, formatDate(endDate)),
    ]
    try {
      const response = await DataService.executeConfigurationNorms(
        payload,
        keycloak,
      )
      if (response?.code === 200) {
        await getConfigurationExecutionDetailsNorms()
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
      setLoading1(false)
    }
  }
  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) {
      return
    }
    hasExecutedRef.current = false
    getConfigurationExecutionDetailsNorms()
  }, [PLANT_ID, AOP_YEAR])

  const getConfigurationExecutionDetailsNorms = async () => {
    try {
      const response = await DataService.getConfigurationExecutionDetailsNorms(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const details = response?.data || []
      if (details.length === 0) {
        console.warn(
          'getConfigurationExecutionDetailsNorms returned an empty array:',
          response,
        )
      }
      const hasNoModifiedOn = details.length && !details[0]?.ModifiedOn
      if (hasNoModifiedOn && !hasExecutedRef.current) {
        const startDateObj = details.find(
          (item) => item.Name === 'StartDateNorms',
        )
        const endDateObj = details.find((item) => item.Name === 'EndDateNorms')
        hasExecutedRef.current = true
        await onLoadTest(startDateObj, endDateObj)
      } else {
        setConfigurationExecutionDetails(details)
        // setLoading1(false)
      }
    } catch (error) {
      console.error(
        'Error fetching getConfigurationExecutionDetailsNorms:',
        error,
      )
    } finally {
      // setLoading1(false)
    }
  }
  const onLoad = async () => {
    if (startDate && endDate && startDate > endDate) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Please Choose Valid Dates!',
        severity: 'warning',
      })
      return
    }

    setLoading1(true)
    setLoading(true)

    const startDateObj = configurationExecutionDetails.find(
      (item) => item.Name === 'StartDateNorms',
    )
    const endDateObj = configurationExecutionDetails.find(
      (item) => item.Name === 'EndDateNorms',
    )
    if (!startDateObj?.Id || !endDateObj?.Id) {
      console.warn(
        'StartDateNorms or EndDateNorms object is missing Id. Aborting execution.',
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Start/End date configuration is incomplete.',
        severity: 'error',
      })
      setLoading(false)
      setLoading1(false)
      return
    }

    try {
      setStartDateObj(startDateObj)
      setEndDateObj(endDateObj)

      const payload = [
        {
          apr: formatDate(startDate),
          UOM: '',
          auditYear: AOP_YEAR,
          normParameterFKId: startDateObj?.NormParameter_FK_Id,
          remarks: 'Initiated',
          id: startDateObj?.Id || null,
          plantId: PLANT_ID,
        },
        {
          apr: formatDate(endDate),
          UOM: '',
          auditYear: AOP_YEAR,
          normParameterFKId: endDateObj?.NormParameter_FK_Id,
          remarks: 'Initiated',
          id: endDateObj?.Id || null,
          plantId: PLANT_ID,
        },
      ]

      const response = await DataService.executeConfigurationNorms(
        payload,
        keycloak,
      )

      if (!response || response?.code !== 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Failed!',
          severity: 'error',
        })
        return response
      }

      // Execution started ? show user and refresh details
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Execution Started Successfully!',
        severity: 'success',
      })
      await getConfigurationExecutionDetailsNorms()

      // Run the three loads in parallel and wait for all to finish
      const [r1, r2, r3] = await Promise.all([
        NormalOperationNormsApiService.load1(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          formatDate(endDate),
          formatDate(startDate),
        ),
        NormalOperationNormsApiService.load2(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          formatDate(endDate),
          formatDate(startDate),
        ),
        NormalOperationNormsApiService.load3(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          formatDate(endDate),
          formatDate(startDate),
        ),
      ])

      // all finished ? check statuses
      const ok1 = r1?.code === 200
      const ok2 = r2?.code === 200
      const ok3 = r3?.code === 200

      if (ok1 && ok2 && ok3) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution completed successfully!',
          severity: 'success',
        })
      } else {
        // build a helpful message about which one(s) failed
        const failed = []
        if (!ok1) failed.push('load1')
        if (!ok2) failed.push('load2')
        if (!ok3) failed.push('load3')
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Some Execution failed: ${failed.join(', ')}.`,
          severity: 'error',
        })
        console.error('Load results:', { r1, r2, r3 })
      }

      return response
    } catch (error) {
      console.error('Execution Failed!', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: error?.message || 'Execution Failed!',
        severity: 'error',
      })
    } finally {
      // stop loaders only after all work (or error handling) is done
      setLoading(false)
      setLoading1(false)
    }
  }

  const startDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'StartDateNorms',
  )

  const endDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'EndDateNorms',
  )

  const startDateFromConfig = new Date(startDateConfig?.AttributeValue)
  const endDateDateFromConfig = new Date(endDateConfig?.AttributeValue)

  const ConfigurationAccordian = useMemo(() => {
    return (
      <Box sx={{ mb: '0px' }}>
        <CustomAccordion defaultExpanded disableGutters>
          <CustomAccordionSummary
            aria-controls='meg-grid-content'
            id='meg-grid-header'
          >
            <Typography className='grid-title'>
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
                        onChange={(e) => setStartDate(e.value)}
                        style={{ height: '80px' }}
                        size={'medium'}
                        disabled={READ_ONLY}
                      />{' '}
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
                        onChange={(e) => setEndDate(e.value)}
                        style={{ height: '80px' }}
                        size={'medium'}
                        disabled={READ_ONLY}
                      />{' '}
                    </Box>
                    {/* Load Button */}
                    {!isOldYearFlag && (
                      <Button
                        variant='contained'
                        // onClick={onLoad}
                        onClick={handleOpenDialog}
                        className='btn-save'
                        disabled={READ_ONLY}
                        sx={{ alignSelf: 'flex-end' }}
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
                      alignSelf: 'flex-end', // ?? ensures it's bottom-aligned with the button
                    }}
                  >
                    {`(Last refreshed data on: ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)} for the period from ${formatDateForText(startDateFromConfig)} to ${formatDateForText(endDateDateFromConfig)})`}
                  </Typography>
                )}
              </Box>
            </Box>
          </CustomAccordionDetails>
        </CustomAccordion>
      </Box>
    )
  }, [startDate, endDate, summary, startDateFromConfig, endDateDateFromConfig])

  const ConfigurationDialog = useMemo(() => {
    return (
      <Dialog
        open={openConfirmDialog}
        onClose={handleCloseDialog}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>{'Load?'}</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
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
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading1}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      {ConfigurationAccordian}
      <Notification
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />
      {ConfigurationDialog}

      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
        }}
      ></div>
    </div>
  )
}
export default AopDesignBasisNorms
