import ExpandMoreIcon from '@mui/icons-material/ExpandMore'

import {
  Backdrop,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import { styled } from '@mui/material/styles'
import { DatePicker } from '@progress/kendo-react-dateinputs'
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useSelector } from 'react-redux'

import Notification from 'components/Utilities/Notification'
import { verticalEnums } from 'enums/verticalEnums'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import SelectivityData from './SelectivityData'

// Styled Components (moved outside to prevent recreation)
const CustomAccordion = styled((props) => (
  <MuiAccordion disableGutters elevation={0} square {...props} />
))(() => ({
  position: 'unset',
  border: 'none',
  boxShadow: 'none',
  margin: '0px',
  '&:before': { display: 'none' },
}))

const CustomAccordionSummary = styled((props) => (
  <MuiAccordionSummary expandIcon={<ExpandMoreIcon />} {...props} />
))(() => ({
  backgroundColor: '#fff',
  padding: '0px 0px',
  minHeight: '40px',
  '& .MuiAccordionSummary-content': { margin: '8px 0' },
}))

const CustomAccordionDetails = styled(MuiAccordionDetails)(() => ({
  padding: '0px 0px 0px',
  backgroundColor: '#F2F3F8',
}))

// Constants
const MEG_TABS = ['Configuration', 'Constants', 'Report Manual Entry']
const CRACKER_TABS = ['Configuration', 'Constants']
const DEBOUNCE_DELAY = 300
const DATE_FORMATS = {
  display: 'dd-MM-yyyy',
  api: 'YYYY-MM-DD',
}

// Custom hooks
const useDebounce = (value, delay) => {
  const [debouncedValue, setDebouncedValue] = useState(value)

  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay)
    return () => clearTimeout(handler)
  }, [value, delay])

  return debouncedValue
}

const useLocalStorage = (key, defaultValue = null) => {
  return useMemo(() => {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : defaultValue
    } catch {
      return defaultValue
    }
  }, [key, defaultValue])
}

// Utility functions
const formatDate = (date) => {
  if (!date) return ''
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatDateForText = (date, includeTime = false) => {
  if (!date) return ''
  const parsedDate = new Date(date)
  if (isNaN(parsedDate)) return 'Invalid Date'

  const day = String(parsedDate.getDate()).padStart(2, '0')
  const month = String(parsedDate.getMonth() + 1).padStart(2, '0')
  const year = parsedDate.getFullYear()
  let formatted = `${day}-${month}-${year}`

  if (includeTime) {
    let hours = parsedDate.getHours()
    const minutes = String(parsedDate.getMinutes()).padStart(2, '0')
    const ampm = hours >= 12 ? 'PM' : 'AM'
    hours = hours % 12 || 12
    formatted += ` ${String(hours).padStart(2, '0')}:${minutes} ${ampm}`
  }

  return formatted
}

const createPayloadItem = (obj, date, auditYear, plantId) => ({
  apr: date,
  UOM: '',
  auditYear,
  normParameterFKId: obj?.NormParameter_FK_Id,
  remarks: 'Initiated',
  id: obj?.Id || null,
  plantId,
})

// Main Component
const Configuration = memo(() => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, yearChanged, oldYear, plantID } = dataGridStore

  // Memoized values
  const selectedVertical = useLocalStorage('selectedVertical')
  const selectedPlant = useLocalStorage('selectedPlant')
  const auditYear = useLocalStorage('year', '')

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = useMemo(() => vertName?.toLowerCase(), [vertName])
  const isOldYear = oldYear?.oldYear
  const isOldYearFlag = isOldYear === 1
  const plantId = selectedPlant?.id || ''

  // State management
  const [state, setState] = useState({
    tabIndex: 0,
    loading: false,
    loading1: false,
    summaryEdited: false,
    summary: '',
    snackbarOpen: false,
    snackbarData: { message: '', severity: 'info' },
    startDate: null,
    endDate: null,
    openConfirmDialog: false,
    isEdited: false,
  })

  const [dataState, setDataState] = useState({
    startUpRows: [],
    otherLossRows: [],
    shutdownNormsRows: [],
    productionRows: [],
    elastomerRows: [],
    productionRowsConstants: [],
    productionRowsConstantsMannualEntry: [],
    gradeData: [],
    continiousGradeData: [],
    discontiniousGradeData: [],
    tabs: [],
    availableTabs: [],
    configurationExecutionDetails: [],
    startDateObj: [],
    endDateObj: [],
  })

  const debouncedSummary = useDebounce(state.summary, DEBOUNCE_DELAY)
  const hasExecutedRef = useRef(false)

  // Memoized helpers
  const getTabId = useCallback(
    (name) => {
      const tab = dataState.availableTabs.find((tab) => tab.name === name)
      return tab?.id || null
    },
    [dataState.availableTabs],
  )

  const displayYear = useMemo(() => {
    if (!auditYear) return ''
    const [start, end] = auditYear.split('-').map(Number)
    return `(${start - 1}-${(end - 1).toString().slice(-2)})`
  }, [auditYear])

  // Event handlers
  const updateState = useCallback((updates) => {
    setState((prev) => ({ ...prev, ...updates }))
  }, [])

  const updateDataState = useCallback((updates) => {
    setDataState((prev) => ({ ...prev, ...updates }))
  }, [])

  const showSnackbar = useCallback(
    (message, severity = 'info') => {
      updateState({
        snackbarOpen: true,
        snackbarData: { message, severity },
      })
    },
    [updateState],
  )

  const handleTabChange = useCallback(
    (event, newIndex) => {
      console.log('newIndex', newIndex)
      updateState({ tabIndex: newIndex })
    },
    [updateState],
  )

  const handleSummaryChange = useCallback(
    (event) => {
      updateState({
        summary: event.target.value,
        summaryEdited: true,
      })
    },
    [updateState],
  )

  const handleOpenDialog = useCallback(() => {
    updateState({ openConfirmDialog: true })
  }, [updateState])

  const handleCloseDialog = useCallback(() => {
    updateState({ openConfirmDialog: false })
  }, [updateState])

  // API calls
  const fetchData = useCallback(async () => {
    updateDataState({
      productionRows: [],
      productionRowsConstants: [],
      productionRowsConstantsMannualEntry: [],
    })
    updateState({ loading: true })

    try {
      const data = await DataService.getCatalystSelectivityData(keycloak)

      if (lowerVertName === 'meg') {
        const filteredData = data?.filter(
          (item) => item.normType !== 'Report Manual Entry',
        )
        const formattedData =
          filteredData?.map((item, index) => ({
            ...item,
            idFromApi: item.id,
            id: index,
            originalRemark: item.remarks,
            srNo: index + 1,
            Particulars: item.normType,
          })) || []
        updateDataState({ productionRows: formattedData })
      } else if (lowerVertName === 'elastomer') {
        const formattedData =
          data?.map((item, index) => ({
            ...item,
            idFromApi: item.id,
            id: index,
            originalRemark: item.remarks,
            srNo: index + 1,
            Particulars: item.normType,
          })) || []
        updateDataState({ elastomerRows: formattedData })
      } else {
        // Group data processing
        const groups = new Map()
        data?.forEach((item) => {
          const { ConfigTypeName, TypeDisplayName } = item
          if (!groups.has(ConfigTypeName)) {
            groups.set(ConfigTypeName, new Map())
          }
          const normGroup = groups.get(ConfigTypeName)
          if (!normGroup.has(TypeDisplayName)) {
            normGroup.set(TypeDisplayName, [])
          }
          normGroup.get(TypeDisplayName).push(item)
        })

        let groupId = 0
        const categorizedData = {
          shutdownNormsRows: [],
          startUpRows: [],
          otherLossRows: [],
          continiousGradeData: [],
          discontiniousGradeData: [],
        }

        groups.forEach((normGroup, ConfigTypeName) => {
          const rowsForCategory = []
          normGroup.forEach((items) => {
            items.forEach((item) => {
              rowsForCategory.push({
                ...item,
                idFromApi: item.id,
                id: groupId++,
              })
            })
          })

          const categoryMap = {
            ShutdownNorms: 'shutdownNormsRows',
            StartupLosses: 'startUpRows',
            Otherlosses: 'otherLossRows',
            ContineGradeChange: 'continiousGradeData',
            DisContineGradeChange: 'discontiniousGradeData',
          }

          const categoryKey = categoryMap[ConfigTypeName]
          if (categoryKey) {
            categorizedData[categoryKey] = rowsForCategory
          }
        })

        updateDataState(categorizedData)
      }
    } catch (error) {
      console.error('Error fetching data:', error)
      showSnackbar('Error fetching data', 'error')
    } finally {
      updateState({ loading: false })
    }
  }, [keycloak, lowerVertName, updateDataState, updateState, showSnackbar])

  const fetchDataConstants = useCallback(async () => {
    try {
      const response =
        await DataService.getCatalystSelectivityDataConstants(keycloak)
      if (response?.code !== 200) {
        updateDataState({ productionRowsConstants: [] })
        return
      }

      const formattedData =
        response.data?.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.Remarks,
          srNo: index + 1,
          Particulars: item.NormTypeName,
          remarks: item.Remarks,
        })) || []

      updateDataState({ productionRowsConstants: formattedData })
    } catch (error) {
      console.error('Error fetching constants data:', error)
      showSnackbar('Error fetching constants data', 'error')
    }
  }, [keycloak, updateDataState, showSnackbar])

  const fetchDataConstantsManualEntry = useCallback(async () => {
    try {
      const data = await DataService.getCatalystSelectivityData(keycloak)
      const formattedData =
        data?.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remarks,
          srNo: index + 1,
          Particulars: item.normType,
        })) || []

      const manualEntryData = formattedData.filter(
        (item) => item.Particulars === 'Report Manual Entry',
      )

      updateDataState({ productionRowsConstantsMannualEntry: manualEntryData })
    } catch (error) {
      console.error('Error fetching manual entry data:', error)
      showSnackbar('Error fetching manual entry data', 'error')
    }
  }, [keycloak, updateDataState, showSnackbar])

  const fetchGradeData = useCallback(async () => {
    updateState({ loading: true })
    try {
      const data = await DataService.getPeConfigData(keycloak)
      const formattedData =
        data?.map((item, index) => ({
          ...item,
          id: index,
        })) || []
      updateDataState({ gradeData: formattedData })
    } catch (error) {
      console.error('Error fetching grade data:', error)
      showSnackbar('Error fetching grade data', 'error')
    } finally {
      updateState({ loading: false })
    }
  }, [keycloak, updateDataState, updateState, showSnackbar])

  const getAopSummary = useCallback(async () => {
    try {
      const response = await DataService.getAopSummary(keycloak)
      updateState({
        summary: response?.code === 200 ? response.data?.summary || '' : '',
      })
    } catch (error) {
      console.error('Error fetching AOP summary:', error)
      updateState({ summary: '' })
    }
  }, [keycloak, updateState])

  const getConfigurationTabsMatrix = useCallback(async () => {
    updateState({ loading: true })
    try {
      const response = await DataService.getConfigurationTabsMatrix(keycloak)
      if (response?.code === 200) {
        const parsedData = JSON.parse(response.data)
        updateDataState({ tabs: parsedData })
      } else {
        updateDataState({ tabs: [] })
      }
    } catch (error) {
      console.error('Error fetching tabs matrix:', error)
      updateDataState({ tabs: [] })
    } finally {
      updateState({ loading: false })
    }
  }, [keycloak, updateDataState, updateState])

  const getConfigurationAvailableTabs = useCallback(async () => {
    updateState({ loading: true })
    try {
      const response = await DataService.getConfigurationAvailableTabs(keycloak)
      if (response?.code === 200) {
        updateDataState({
          availableTabs: response.data?.configurationTypeList || [],
        })
      } else {
        updateDataState({ availableTabs: [] })
      }
    } catch (error) {
      console.error('Error fetching available tabs:', error)
      updateDataState({ availableTabs: [] })
    } finally {
      updateState({ loading: false })
    }
  }, [keycloak, updateDataState, updateState])

  const getConfigurationExecutionDetails = useCallback(async () => {
    try {
      const response =
        await DataService.getConfigurationExecutionDetails(keycloak)
      const details = response?.data || []

      if (details.length === 0) {
        console.warn(
          'getConfigurationExecutionDetails returned empty array:',
          response,
        )
        return
      }

      const hasNoModifiedOn = details.length && !details[0]?.ModifiedOn
      if (hasNoModifiedOn && !hasExecutedRef.current) {
        const startDateObj = details.find((item) => item.Name === 'StartDate')
        const endDateObj = details.find((item) => item.Name === 'EndDate')
        hasExecutedRef.current = true
        await onLoadTest(startDateObj, endDateObj)
      } else {
        updateDataState({ configurationExecutionDetails: details })
      }
    } catch (error) {
      console.error('Error fetching execution details:', error)
    }
  }, [keycloak, updateDataState])

  const onLoadTest = useCallback(
    async (startDateObj, endDateObj) => {
      updateState({ loading1: true })

      const today = new Date()
      const endDate = new Date(today.getFullYear(), today.getMonth(), 0)
      const startDate = new Date(today.getFullYear() - 5, today.getMonth(), 1)

      const payload = [
        createPayloadItem(
          startDateObj,
          formatDate(startDate),
          auditYear,
          plantId,
        ),
        createPayloadItem(endDateObj, formatDate(endDate), auditYear, plantId),
      ]

      try {
        const response = await DataService.executeConfiguration(
          payload,
          keycloak,
        )
        if (response?.code === 200) {
          await getConfigurationExecutionDetails()
          showSnackbar('Execution Started Successfully!', 'success')
        } else {
          showSnackbar('Execution Failed!', 'error')
        }
        getAopSummary()
        return response
      } catch (error) {
        console.error('Execution Failed!', error)
        showSnackbar('Execution Failed!', 'error')
      } finally {
        updateState({ loading: false, loading1: false })
      }
    },
    [
      auditYear,
      plantId,
      keycloak,
      getConfigurationExecutionDetails,
      getAopSummary,
      showSnackbar,
      updateState,
    ],
  )

  const onLoad = useCallback(async () => {
    if (state.startDate && state.endDate && state.startDate > state.endDate) {
      showSnackbar('Please Choose Valid Dates!', 'warning')
      return
    }

    const { configurationExecutionDetails } = dataState
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
      showSnackbar('Start/End date configuration is incomplete.', 'error')
      return
    }

    updateState({ loading: true, loading1: true })
    updateDataState({ startDateObj, endDateObj })

    const payload = [
      createPayloadItem(
        startDateObj,
        formatDate(state.startDate),
        auditYear,
        plantId,
      ),
      createPayloadItem(
        endDateObj,
        formatDate(state.endDate),
        auditYear,
        plantId,
      ),
    ]

    try {
      const response = await DataService.executeConfiguration(payload, keycloak)
      if (response) {
        showSnackbar('Execution Started Successfully!', 'success')
        getConfigurationExecutionDetails()
      } else {
        showSnackbar('Execution Failed!', 'error')
      }
      getAopSummary()
      return response
    } catch (error) {
      console.error('Execution Failed!', error)
      showSnackbar('Execution Failed!', 'error')
    } finally {
      updateState({ loading: false, loading1: false })
    }
  }, [
    state.startDate,
    state.endDate,
    dataState.configurationExecutionDetails,
    auditYear,
    plantId,
    keycloak,
    getConfigurationExecutionDetails,
    getAopSummary,
    showSnackbar,
    updateState,
    updateDataState,
  ])

  const handleConfirmLoad = useCallback(() => {
    updateState({ openConfirmDialog: false })
    onLoad()
  }, [onLoad, updateState])

  // Date computation
  const computeAndSetDates = useCallback(() => {
    const { configurationExecutionDetails } = dataState

    updateState({ startDate: null, endDate: null })

    const hasModifiedOn = configurationExecutionDetails[0]?.ModifiedOn
    if (hasModifiedOn) {
      const getDateValue = (name) =>
        new Date(
          configurationExecutionDetails.find(
            (item) => item.Name === name,
          )?.AttributeValue,
        )

      updateState({
        startDate: getDateValue('StartDate'),
        endDate: getDateValue('EndDate'),
      })
    } else {
      const today = new Date()
      const fallbackEndDate = new Date(today.getFullYear(), today.getMonth(), 0)
      const fallbackStartDate = new Date(
        today.getFullYear() - 5,
        today.getMonth(),
        1,
      )

      updateState({
        startDate: fallbackStartDate,
        endDate: fallbackEndDate,
      })
    }
  }, [dataState.configurationExecutionDetails, updateState])

  // Effects
  useEffect(() => {
    computeAndSetDates()
  }, [computeAndSetDates])

  useEffect(() => {
    if (state.tabIndex >= dataState.tabs.length) {
      updateState({ tabIndex: 0 })
    }
  }, [dataState.tabs, state.tabIndex, updateState])

  useEffect(() => {
    hasExecutedRef.current = false
    getConfigurationExecutionDetails()
  }, [plantID, getConfigurationExecutionDetails])

  useEffect(() => {
    getConfigurationExecutionDetails()
    getAopSummary()

    const verticalName = selectedVertical?.name?.toLowerCase()

    if (verticalName && verticalName !== 'cracker' && verticalName !== 'meg') {
      setTimeout(() => {
        getConfigurationTabsMatrix()
        getConfigurationAvailableTabs()
        fetchGradeData()
      }, 500)
    }
  }, [
    oldYear,
    yearChanged,
    keycloak,
    plantID,
    selectedVertical,
    getConfigurationExecutionDetails,
    getAopSummary,
    getConfigurationTabsMatrix,
    getConfigurationAvailableTabs,
    fetchGradeData,
  ])

  // Memoized components

  const DatePickerSection = memo(function DatePickerSection({
    startDate,
    endDate,
    onStartDateChange,
    onEndDateChange,
  }) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <Typography className='grid-title' sx={{ whiteSpace: 'nowrap' }}>
          Start Date
        </Typography>
        <DatePicker
          id='start-date'
          format={DATE_FORMATS.display}
          value={startDate}
          onChange={(e) => onStartDateChange(e.value)}
          style={{ height: '80px' }}
          size='large'
        />
        <Typography className='grid-title' sx={{ whiteSpace: 'nowrap' }}>
          End Date
        </Typography>
        <DatePicker
          id='end-date'
          format={DATE_FORMATS.display}
          value={endDate}
          onChange={(e) => onEndDateChange(e.value)}
          style={{ height: '80px' }}
          size='large'
        />
      </Box>
    )
  })

  DatePickerSection.displayName = 'DatePickerSection'

  const ConfirmDialog = memo(function ConfirmDialog({
    open,
    onClose,
    onConfirm,
    startDate,
    endDate,
  }) {
    return (
      <Dialog
        open={open}
        onClose={onClose}
        aria-labelledby='alert-dialog-title'
        aria-describedby='alert-dialog-description'
      >
        <DialogTitle id='alert-dialog-title'>Load?</DialogTitle>
        <DialogContent>
          <DialogContentText id='alert-dialog-description'>
            {`Are you sure you want to load data for the period from ${formatDateForText(
              startDate,
            )} to ${formatDateForText(endDate)}?`}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button onClick={onConfirm} autoFocus>
            Load
          </Button>
        </DialogActions>
      </Dialog>
    )
  })

  ConfirmDialog.displayName = 'ConfirmDialog'

  // Early returns for specific verticals
  if (lowerVertName === 'elastomer') {
    return (
      <SelectivityData
        rows={dataState.elastomerRows}
        loading={state.loading}
        fetchData={fetchData}
        setRows={(rows) => updateDataState({ elastomerRows: rows })}
        configType='meg'
        groupBy='Particulars'
        summary={debouncedSummary}
      />
    )
  }

  // MEG vertical rendering
  if (lowerVertName === verticalEnums.MEG) {
    const { configurationExecutionDetails } = dataState
    const startDateDetails = configurationExecutionDetails.find(
      (item) => item.Name === 'StartDate',
    )
    const endDateDetails = configurationExecutionDetails.find(
      (item) => item.Name === 'EndDate',
    )
    const displayStartDate = new Date(startDateDetails?.AttributeValue)
    const displayEndDate = new Date(endDateDetails?.AttributeValue)

    const renderTabContent = useCallback(() => {
      const currentTab = MEG_TABS[state.tabIndex]?.toLowerCase()
      console.log('🚀 ~ currentTab:', currentTab, state)
      const baseProps = {
        loading: state.loading,
        configType: 'meg',
        groupBy: 'Particulars',
        summary: debouncedSummary,
        summaryEdited: state.summaryEdited,
        onSummaryEditChange: (edited) => updateState({ summaryEdited: edited }),
      }

      switch (currentTab) {
        case 'configuration':
          return (
            <SelectivityData
              {...baseProps}
              rows={dataState.productionRows}
              fetchData={fetchData}
              setRows={(rows) => updateDataState({ productionRows: rows })}
              tabIndex='0'
            />
          )
        case 'constants':
          return (
            <SelectivityData
              {...baseProps}
              rows={dataState.productionRowsConstants}
              fetchData={fetchDataConstants}
              setRows={(rows) =>
                updateDataState({ productionRowsConstants: rows })
              }
              configType='megConstants'
              tabIndex='1'
            />
          )
        case 'report manual entry':
          return (
            <SelectivityData
              {...baseProps}
              rows={dataState.productionRowsConstantsMannualEntry}
              fetchData={fetchDataConstantsManualEntry}
              setRows={(rows) =>
                updateDataState({ productionRowsConstantsMannualEntry: rows })
              }
              configType='megConstantsMannualEntry'
              tabIndex='2'
            />
          )
        default:
          return null
      }
    }, [state.tabIndex])

    return (
      <div>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={state.loading1}
        >
          <CircularProgress color='inherit' />
        </Backdrop>

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
                    marginTop: '10px',
                  }}
                >
                  <DatePickerSection
                    startDate={state.startDate}
                    endDate={state.endDate}
                    onStartDateChange={(date) =>
                      updateState({ startDate: date })
                    }
                    onEndDateChange={(date) => updateState({ endDate: date })}
                  />

                  {!isOldYearFlag && (
                    <Button
                      variant='contained'
                      onClick={handleOpenDialog}
                      className='btn-load'
                      sx={{ alignSelf: 'flex-end' }}
                    >
                      Load
                    </Button>
                  )}

                  {configurationExecutionDetails[0]?.ModifiedOn && (
                    <Typography
                      className='summary-title'
                      sx={{ whiteSpace: 'normal' }}
                    >
                      {`(Last refreshed data on: ${formatDateForText(
                        configurationExecutionDetails[0]?.ModifiedOn,
                        true,
                      )} for the period from ${formatDateForText(displayStartDate)} to ${formatDateForText(displayEndDate)})`}
                    </Typography>
                  )}
                </Box>
              </Box>

              <TextField
                label='AOP Design Basis'
                multiline
                minRows={2}
                fullWidth
                margin='normal'
                variant='outlined'
                disabled={isOldYear === 1}
                value={state.summary}
                onChange={handleSummaryChange}
                sx={{
                  '& .MuiInputBase-root': {
                    backgroundColor: '#ffffff',
                    borderRadius: '8px',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                    padding: '8px',
                  },
                  '& label': {
                    fontSize: '1rem',
                    color: '#666',
                    lineHeight: '1.2',
                    transform: 'translate(14px, 12px) scale(1)',
                  },
                  '& .MuiInputLabel-shrink': {
                    transform: 'translate(14px, -6px) scale(0.75)',
                  },
                  '& .MuiOutlinedInput-notchedOutline': {
                    borderColor: '#ccc',
                  },
                  '&:hover .MuiOutlinedInput-notchedOutline': {
                    borderColor: '#999',
                  },
                  '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                    borderColor: '#1976d2',
                  },
                  '& .MuiInputBase-input': {
                    resize: 'vertical',
                  },
                }}
              />
            </CustomAccordionDetails>
          </CustomAccordion>
        </Box>

        <Box>
          <Tabs
            value={state.tabIndex}
            onChange={handleTabChange}
            sx={{
              borderBottom: '0px solid #ccc',
              '.MuiTabs-indicator': { display: 'none' },
              margin: '0px 0px 0px 0px',
              minHeight: '32px',
            }}
            textColor='primary'
            indicatorColor='primary'
          >
            {MEG_TABS.map((tab) => (
              <Tab
                key={tab}
                label={
                  tab === 'Report Manual Entry' ? `${tab} ${displayYear}` : tab
                }
                sx={{
                  border: '1px solid #ADD8E6',
                  borderBottom: '1px solid #ADD8E6',
                  padding: '9px',
                  minHeight: '10px',
                }}
              />
            ))}
          </Tabs>
          {renderTabContent()}
        </Box>

        <Notification
          open={state.snackbarOpen}
          message={state.snackbarData?.message || ''}
          severity={state.snackbarData?.severity || 'info'}
          onClose={() => updateState({ snackbarOpen: false })}
        />

        <ConfirmDialog
          open={state.openConfirmDialog}
          onClose={handleCloseDialog}
          onConfirm={handleConfirmLoad}
          startDate={state.startDate}
          endDate={state.endDate}
        />
      </div>
    )
  }
  // CRACKER vertical rendering
  //   if (lowerVertName === verticalEnums.CRACKER) {
  //     const { configurationExecutionDetails } = dataState
  //     const startDateDetails = configurationExecutionDetails.find(
  //       (item) => item.Name === 'StartDate',
  //     )
  //     const endDateDetails = configurationExecutionDetails.find(
  //       (item) => item.Name === 'EndDate',
  //     )
  //     const displayStartDate = new Date(startDateDetails?.AttributeValue)
  //     const displayEndDate = new Date(endDateDetails?.AttributeValue)

  //     const renderTabContent = useCallback(() => {
  //       const currentTab = CRACKER_TABS[state.tabIndex]?.toLowerCase()
  //       const baseProps = {
  //         loading: state.loading,
  //         configType: 'cracker',
  //         groupBy: 'Particulars',
  //         summary: debouncedSummary,
  //         summaryEdited: state.summaryEdited,
  //         onSummaryEditChange: (edited) => updateState({ summaryEdited: edited }),
  //       }

  //       switch (currentTab) {
  //         case 'configuration':
  //           return (
  //             <SelectivityData
  //               {...baseProps}
  //               rows={dataState.productionRows}
  //               fetchData={fetchData}
  //               setRows={(rows) => updateDataState({ productionRows: rows })}
  //               tabIndex='0'
  //             />
  //           )
  //         case 'constants':
  //           return (
  //             <SelectivityData
  //               {...baseProps}
  //               rows={dataState.productionRowsConstants}
  //               fetchData={fetchDataConstants}
  //               setRows={(rows) =>
  //                 updateDataState({ productionRowsConstants: rows })
  //               }
  //               configType='megConstants'
  //               tabIndex='1'
  //             />
  //           )

  //         default:
  //           return null
  //       }
  //     }, [
  //       state.tabIndex,
  //       state.loading,
  //       state.summaryEdited,
  //       debouncedSummary,
  //       dataState.productionRows,
  //       dataState.productionRowsConstants,
  //       dataState.productionRowsConstantsMannualEntry,
  //       fetchData,
  //       fetchDataConstants,
  //       fetchDataConstantsManualEntry,
  //       updateDataState,
  //       updateState,
  //     ])

  //     return (
  //       <div>
  //         <Backdrop
  //           sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
  //           open={state.loading1}
  //         >
  //           <CircularProgress color='inherit' />
  //         </Backdrop>

  //         <Box sx={{ mb: '0px' }}>
  //           <CustomAccordion defaultExpanded disableGutters>
  //             <CustomAccordionSummary
  //               aria-controls='meg-grid-content'
  //               id='meg-grid-header'
  //             >
  //               <Typography className='grid-title'>
  //                 AOP Historical Period Basis
  //               </Typography>
  //             </CustomAccordionSummary>
  //             <CustomAccordionDetails>
  //               <Box
  //                 sx={{
  //                   display: 'flex',
  //                   justifyContent: 'space-between',
  //                   alignItems: 'flex-end',
  //                   mt: 0,
  //                 }}
  //               >
  //                 <Box
  //                   sx={{
  //                     display: 'flex',
  //                     alignItems: 'center',
  //                     gap: 1,
  //                     marginTop: '10px',
  //                   }}
  //                 >
  //                   <DatePickerSection
  //                     startDate={state.startDate}
  //                     endDate={state.endDate}
  //                     onStartDateChange={(date) =>
  //                       updateState({ startDate: date })
  //                     }
  //                     onEndDateChange={(date) => updateState({ endDate: date })}
  //                   />

  //                   {!isOldYearFlag && (
  //                     <Button
  //                       variant='contained'
  //                       onClick={handleOpenDialog}
  //                       className='btn-load'
  //                       sx={{ alignSelf: 'flex-end' }}
  //                     >
  //                       Load
  //                     </Button>
  //                   )}

  //                   {configurationExecutionDetails[0]?.ModifiedOn && (
  //                     <Typography
  //                       className='summary-title'
  //                       sx={{ whiteSpace: 'normal' }}
  //                     >
  //                       {`(Last refreshed data on: ${formatDateForText(
  //                         configurationExecutionDetails[0]?.ModifiedOn,
  //                         true,
  //                       )} for the period from ${formatDateForText(displayStartDate)} to ${formatDateForText(displayEndDate)})`}
  //                     </Typography>
  //                   )}
  //                 </Box>
  //               </Box>

  //               <TextField
  //                 label='AOP Design Basis'
  //                 multiline
  //                 minRows={2}
  //                 fullWidth
  //                 margin='normal'
  //                 variant='outlined'
  //                 disabled={isOldYear === 1}
  //                 value={state.summary}
  //                 onChange={handleSummaryChange}
  //                 sx={{
  //                   '& .MuiInputBase-root': {
  //                     backgroundColor: '#ffffff',
  //                     borderRadius: '8px',
  //                     boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
  //                     padding: '8px',
  //                   },
  //                   '& label': {
  //                     fontSize: '1rem',
  //                     color: '#666',
  //                     lineHeight: '1.2',
  //                     transform: 'translate(14px, 12px) scale(1)',
  //                   },
  //                   '& .MuiInputLabel-shrink': {
  //                     transform: 'translate(14px, -6px) scale(0.75)',
  //                   },
  //                   '& .MuiOutlinedInput-notchedOutline': {
  //                     borderColor: '#ccc',
  //                   },
  //                   '&:hover .MuiOutlinedInput-notchedOutline': {
  //                     borderColor: '#999',
  //                   },
  //                   '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
  //                     borderColor: '#1976d2',
  //                   },
  //                   '& .MuiInputBase-input': {
  //                     resize: 'vertical',
  //                   },
  //                 }}
  //               />
  //             </CustomAccordionDetails>
  //           </CustomAccordion>
  //         </Box>

  //         <Box>
  //           <Tabs
  //             value={state.tabIndex}
  //             onChange={handleTabChange}
  //             sx={{
  //               borderBottom: '0px solid #ccc',
  //               '.MuiTabs-indicator': { display: 'none' },
  //               margin: '0px 0px 0px 0px',
  //               minHeight: '32px',
  //             }}
  //             textColor='primary'
  //             indicatorColor='primary'
  //           >
  //             {CRACKER_TABS.map((tab) => (
  //               <Tab
  //                 key={tab}
  //                 label={
  //                   tab === 'Report Manual Entry' ? `${tab} ${displayYear}` : tab
  //                 }
  //                 sx={{
  //                   border: '1px solid #ADD8E6',
  //                   borderBottom: '1px solid #ADD8E6',
  //                   padding: '9px',
  //                   minHeight: '10px',
  //                 }}
  //               />
  //             ))}
  //           </Tabs>
  //           {renderTabContent()}
  //         </Box>

  //         <Notification
  //           open={state.snackbarOpen}
  //           message={state.snackbarData?.message || ''}
  //           severity={state.snackbarData?.severity || 'info'}
  //           onClose={() => updateState({ snackbarOpen: false })}
  //         />

  //         <ConfirmDialog
  //           open={state.openConfirmDialog}
  //           onClose={handleCloseDialog}
  //           onConfirm={handleConfirmLoad}
  //           startDate={state.startDate}
  //           endDate={state.endDate}
  //         />
  //       </div>
  //     )
  //   }

  // Default rendering for other verticals
  const { configurationExecutionDetails, tabs, availableTabs } = dataState
  const startDateDetails = configurationExecutionDetails.find(
    (item) => item.Name === 'StartDate',
  )
  const endDateDetails = configurationExecutionDetails.find(
    (item) => item.Name === 'EndDate',
  )
  const displayStartDate = new Date(startDateDetails?.AttributeValue)
  const displayEndDate = new Date(endDateDetails?.AttributeValue)

  const renderDefaultTabContent = useCallback(() => {
    const currentTabId = tabs[state.tabIndex]?.toLowerCase()
    const baseProps = {
      loading: state.loading,
      summary: debouncedSummary,
      summaryEdited: state.summaryEdited,
      onSummaryEditChange: (edited) => updateState({ summaryEdited: edited }),
    }

    const tabConfig = {
      [getTabId('StartupLosses')]: {
        rows: dataState.startUpRows,
        setRows: (rows) => updateDataState({ startUpRows: rows }),
        configType: 'StartupLosses',
        groupBy: 'TypeDisplayName',
      },
      [getTabId('Otherlosses')]: {
        rows: dataState.otherLossRows,
        setRows: (rows) => updateDataState({ otherLossRows: rows }),
        configType: 'Otherlosses',
        groupBy: 'TypeDisplayName',
      },
      [getTabId('ShutdownNorms')]: {
        rows: dataState.shutdownNormsRows,
        setRows: (rows) => updateDataState({ shutdownNormsRows: rows }),
        configType: 'ShutdownNorms',
        groupBy: 'TypeDisplayName',
      },
      [getTabId('Receipe')]: {
        rows: dataState.gradeData,
        setRows: (rows) => updateDataState({ gradeData: rows }),
        configType: 'grades',
        fetchData: fetchGradeData,
      },
      [getTabId('ContineGradeChange')]: {
        rows: dataState.continiousGradeData,
        setRows: (rows) => updateDataState({ continiousGradeData: rows }),
        configType: 'ContineGradeChange',
      },
      [getTabId('DisContineGradeChange')]: {
        rows: dataState.discontiniousGradeData,
        setRows: (rows) => updateDataState({ discontiniousGradeData: rows }),
        configType: 'DisContineGradeChange',
      },
    }

    const config = tabConfig[currentTabId]
    if (!config) return null

    return (
      <SelectivityData
        {...baseProps}
        {...config}
        fetchData={config.fetchData || fetchData}
      />
    )
  }, [
    state.tabIndex,
    state.loading,
    state.summaryEdited,
    debouncedSummary,
    tabs,
    dataState,
    getTabId,
    updateState,
    updateDataState,
    fetchData,
    fetchGradeData,
  ])

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={state.loading1}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

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
                  marginTop: '0px',
                }}
              >
                <DatePickerSection
                  startDate={state.startDate}
                  endDate={state.endDate}
                  onStartDateChange={(date) => updateState({ startDate: date })}
                  onEndDateChange={(date) => updateState({ endDate: date })}
                />

                {!isOldYearFlag && (
                  <Button
                    variant='contained'
                    onClick={handleOpenDialog}
                    className='btn-load'
                    sx={{ alignSelf: 'flex-end' }}
                  >
                    Load
                  </Button>
                )}

                {configurationExecutionDetails[0]?.ModifiedOn && (
                  <Typography
                    className='summary-title'
                    sx={{ whiteSpace: 'normal' }}
                  >
                    {`(Last refreshed data on: ${formatDateForText(
                      configurationExecutionDetails[0]?.ModifiedOn,
                      true,
                    )} for the period from ${formatDateForText(displayStartDate)} to ${formatDateForText(displayEndDate)})`}
                  </Typography>
                )}
              </Box>
            </Box>

            <TextField
              label='AOP Design Basis'
              multiline
              minRows={2}
              fullWidth
              margin='normal'
              variant='outlined'
              disabled={isOldYear === 1}
              value={state.summary}
              onChange={handleSummaryChange}
              sx={{
                '& .MuiInputBase-root': {
                  backgroundColor: '#ffffff',
                  borderRadius: '8px',
                  boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                  padding: '8px',
                },
                '& label': {
                  fontSize: '1rem',
                  color: '#666',
                  lineHeight: '1.2',
                  transform: 'translate(14px, 12px) scale(1)',
                },
                '& .MuiInputLabel-shrink': {
                  transform: 'translate(14px, -6px) scale(0.75)',
                },
                '& .MuiOutlinedInput-notchedOutline': {
                  borderColor: '#ccc',
                },
                '&:hover .MuiOutlinedInput-notchedOutline': {
                  borderColor: '#999',
                },
                '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                  borderColor: '#1976d2',
                },
                '& .MuiInputBase-input': {
                  resize: 'vertical',
                },
              }}
            />
          </CustomAccordionDetails>
        </CustomAccordion>
      </Box>

      <Notification
        open={state.snackbarOpen}
        message={state.snackbarData?.message || ''}
        severity={state.snackbarData?.severity || 'info'}
        onClose={() => updateState({ snackbarOpen: false })}
      />

      <ConfirmDialog
        open={state.openConfirmDialog}
        onClose={handleCloseDialog}
        onConfirm={handleConfirmLoad}
        startDate={state.startDate}
        endDate={state.endDate}
      />

      <div style={{ display: 'flex', flexDirection: 'column' }}>
        <Tabs
          sx={{
            borderBottom: '0px solid #ccc',
            '.MuiTabs-indicator': { display: 'none' },
          }}
          textColor='primary'
          indicatorColor='primary'
          value={state.tabIndex}
          onChange={handleTabChange}
        >
          {tabs.map((tabId) => {
            const tabInfo = availableTabs.find(
              (tab) => tab.id.toLowerCase() === tabId.toLowerCase(),
            )
            return (
              <Tab
                key={tabId}
                sx={{
                  border: '1px solid #ADD8E6',
                  borderBottom: '1px solid #ADD8E6',
                  padding: '9px',
                  minHeight: '10px',
                }}
                label={tabInfo?.displayName || 'loading..'}
              />
            )
          })}
        </Tabs>
        <Box>{renderDefaultTabContent()}</Box>
      </div>
    </div>
  )
})

Configuration.displayName = 'Configuration'

export default Configuration
