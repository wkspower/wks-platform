import { Box, Tab, Tabs } from '@mui/material'
import { useCallback, useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import MuiAccordionDetails from '@mui/material/AccordionDetails'

import { styled } from '@mui/material/styles'

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
import SelectivityData from './SelectivityData'
import { DatePicker } from '../../../node_modules/@progress/kendo-react-dateinputs/index'
import Notification from 'components/Utilities/Notification'

const CustomAccordion = styled((props) => (
  <MuiAccordion disableGutters elevation={0} square {...props} />
))(() => ({
  position: 'unset',
  border: 'none',
  boxShadow: 'none',
  margin: '0px',
  '&:before': {
    display: 'none',
  },
}))

const CustomAccordionSummary = styled((props) => (
  <MuiAccordionSummary expandIcon={<ExpandMoreIcon />} {...props} />
))(() => ({
  backgroundColor: '#fff',
  padding: '0px 12px',
  minHeight: '40px',
  '& .MuiAccordionSummary-content': {
    margin: '8px 0',
  },
}))
const CustomAccordionDetails = styled(MuiAccordionDetails)(() => ({
  padding: '0px 0px 12px',
  backgroundColor: '#F2F3F8',
}))

const ConfigurationTable = () => {
  const keycloak = useSession()

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, yearChanged, oldYear, plantID } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase()
  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  const [loading1, setLoading1] = useState(false)
  const [summaryEdited, setSummaryEdited] = useState(false)

  const [startUpRows, setStartUpRows] = useState([])
  const [otherLossRows, setOtherLossRows] = useState([])
  const [shutdownNormsRows, setShutdownRows] = useState([])
  const [productionRows, setProductionRows] = useState([])
  const [elastomerRows, setElastomerRows] = useState([])
  const [productionRowsConstants, setProductionRowsConstants] = useState([])
  const [
    productionRowsConstantsMannualEntry,
    setProductionRowsConstantsMannualEntry,
  ] = useState([])
  const [gradeData, setGradeData] = useState([])
  const [continiousGradeData, setContiniousGradeData] = useState([])
  const [discontiniousGradeData, setDiscontiniousGradeData] = useState([])
  const [tabs, setTabs] = useState([])
  const [availableTabs, setAvailableTabs] = useState([])
  // const [summary, setSummary] = useState('')

  const [summary, setSummary] = useState('')
  const [debouncedSummary, setDebouncedSummary] = useState('')

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSummary(summary)
    }, 300) // adjust debounce delay as needed

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
  const [isEdited, setIsEdited] = useState(false)

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

  // const [_plantID, set_PlantID] = useState('')
  // useEffect(() => {
  //   if (plantID?.plantId) {
  //     set_PlantID(plantID?.plantId)
  //   }
  // }, [plantID])

  const fetchData = async () => {
    // setRows([])
    setProductionRows([])
    setProductionRowsConstants([])
    setProductionRowsConstantsMannualEntry([])
    setLoading(true)
    try {
      setLoading(true)
      var data = await DataService.getCatalystSelectivityData(keycloak)

      if (lowerVertName == 'meg') {
        data = data?.filter((item) => item.normType !== 'Report Manual Entry')

        const formattedData = data.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remarks,
          srNo: index + 1,
          Particulars: item.normType,
        }))
        setProductionRows(formattedData)
        if (data) {
          setLoading(false)
        }

        // setRows(formattedData)
      } else if (lowerVertName == 'elastomer') {
        const formattedData = data.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remarks,
          srNo: index + 1,
          Particulars: item.normType,
        }))
        setElastomerRows(formattedData)
      } else {
        const groups = new Map()
        data.forEach((item) => {
          const ConfigTypeName = item.ConfigTypeName
          const TypeName = item.TypeDisplayName
          if (!groups.has(ConfigTypeName)) {
            groups.set(ConfigTypeName, new Map())
          }
          const normGroup = groups.get(ConfigTypeName)
          if (!normGroup.has(TypeName)) {
            normGroup.set(TypeName, [])
          }
          normGroup.get(TypeName).push(item)
        })
        let groupId = 0
        let shutdownRows = []
        let startUpRows = []
        let otherLossRows = []
        let continiousGradeRows = []
        let discontiniousGradeRows = []
        groups.forEach((normGroup, ConfigTypeName) => {
          let rowsForThisCategory = []
          normGroup.forEach((items, TypeName) => {
            items.forEach((item) => {
              rowsForThisCategory.push({
                ...item,
                idFromApi: item.id,
                id: groupId++,
              })
            })
          })
          if (ConfigTypeName == 'ShutdownNorms') {
            shutdownRows = rowsForThisCategory
          } else if (ConfigTypeName == 'StartupLosses') {
            startUpRows = rowsForThisCategory
          } else if (ConfigTypeName == 'Otherlosses') {
            otherLossRows = rowsForThisCategory
          } else if (ConfigTypeName == 'ContineGradeChange') {
            continiousGradeRows = rowsForThisCategory
          } else if (ConfigTypeName == 'DisContineGradeChange') {
            discontiniousGradeRows = rowsForThisCategory
          }
        })
        setShutdownRows(shutdownRows)
        setStartUpRows(startUpRows)
        setOtherLossRows(otherLossRows)
        setContiniousGradeData(continiousGradeRows)
        setDiscontiniousGradeData(discontiniousGradeRows)
      }
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  const fetchDataConstants = async () => {
    setProductionRowsConstants([])
    try {
      var constantsRes =
        await DataService.getCatalystSelectivityDataConstants(keycloak)
      if (constantsRes?.code != 200) {
        setProductionRowsConstants([])
        return
      }
      var data = constantsRes?.data
      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: index,
        originalRemark: item.Remarks,
        srNo: index + 1,
        Particulars: item.NormTypeName,
        remarks: item.Remarks,
      }))

      setProductionRowsConstants(formattedData)
    } catch (error) {
      console.error('Error fetching data:', error)
    }
  }

  const fetchDataConstantsMnnualEntry = async () => {
    setProductionRowsConstantsMannualEntry([])
    try {
      var constantsRes = await DataService.getCatalystSelectivityData(keycloak)

      const formattedData = constantsRes.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: index,
        originalRemark: item.remarks,
        srNo: index + 1,
        Particulars: item.normType,
      }))

      var data = formattedData?.filter(
        (item) => item?.Particulars == 'Report Manual Entry',
      )

      setProductionRowsConstantsMannualEntry(data)
    } catch (error) {
      console.error('Error fetching data:', error)
    }
  }

  const fetchGradeData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getPeConfigData(keycloak)
      const formattedData = data.map((item, index) => ({
        ...item,
        id: index,
      }))
      setGradeData(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching grade data:', error)
    } finally {
      setLoading(false)
    }
  }

  const getConfigurationTabsMatrix = async () => {
    setLoading(true)
    try {
      var response = await DataService.getConfigurationTabsMatrix(keycloak)
      if (response?.code == 200) {
        const parsedData = JSON.parse(response?.data)

        setTabs(parsedData)
        setLoading(false)
      } else {
        setTabs([])
        setLoading(false)
      }
    } catch (error) {
      console.error('Error fetching data:', error)
      setTabs([])
      setLoading(false)
    }
  }

  const getConfigurationAvailableTabs = async () => {
    setLoading(true)
    try {
      var response = await DataService.getConfigurationAvailableTabs(keycloak)

      if (response?.code == 200) {
        setAvailableTabs(response?.data?.configurationTypeList)
        setLoading(false)
      } else {
        setAvailableTabs([])
        setLoading(false)
      }
    } catch (error) {
      console.error('Error fetching data:', error)
      setAvailableTabs([])
      setLoading(false)
    }
  }

  useEffect(() => {
    getConfigurationExecutionDetails()
    getAopSummary()
    let vertical = JSON.parse(localStorage.getItem('selectedVertical'))?.name
    let verticalName = vertical.toLowerCase()
    setTimeout(() => {
      if (verticalName != 'cracker' && verticalName != 'meg') {
        getConfigurationTabsMatrix()
        // getConfigurationAvailableTabs()
        getConfigurationAvailableTabs()
        fetchGradeData()
      }
    }, 500)

    // const today = new Date()
    // const endDate = new Date(today.getFullYear(), today.getMonth(), 0)
    // const startDate = new Date(
    //   today.getFullYear() - 5,
    //   today.getMonth() - 1 + 1,
    //   1,
    // )
    // setStartDate(startDate)
    // setEndDate(endDate)
  }, [oldYear, yearChanged, keycloak, plantID])

  const computeAndSetDates = useCallback(() => {
    setStartDate('')
    setEndDate('')
    // if (!configurationExecutionDetails.length) return

    const hasModifiedOn = configurationExecutionDetails[0]?.ModifiedOn

    if (hasModifiedOn) {
      // console.log(
      //   'configurationExecutionDetails',
      //   configurationExecutionDetails,
      // )

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
        today.getFullYear() - 5,
        today.getMonth(),
        1,
      )

      setStartDate(fallbackStartDate)
      setEndDate(fallbackEndDate)
    }
  }, [configurationExecutionDetails, plantID])

  useEffect(() => {
    computeAndSetDates()
  }, [computeAndSetDates])

  const getTheId = (name) => {
    const tab = availableTabs.find((tab) => tab.name === name)
    return tab ? tab.id : null
  }

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

  const getAopSummary = async () => {
    try {
      setSummary('')
      var res = await DataService.getAopSummary(keycloak)

      if (res?.code == 200) {
        setSummary(res?.data?.summary)
      } else {
        setSummary('')
      }
    } catch (error) {
      console.error('Error fetching data:', error)
    }
  }
  const onLoadTest = async (startDateObj, endDateObj) => {
    setLoading1(true)

    const plantId =
      JSON.parse(localStorage.getItem('selectedPlant') || '{}')?.id || ''
    const auditYear = localStorage.getItem('year')

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
      const response = await DataService.executeConfiguration(payload, keycloak)

      if (response?.code === 200) {
        await getConfigurationExecutionDetails()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Failed!',
          severity: 'error',
        })
      }
      getAopSummary()
      return response
    } catch (error) {
      console.error('Execution Failed!', error)
    } finally {
      setLoading(false)
      setLoading1(false)
    }
  }

  useEffect(() => {
    hasExecutedRef.current = false
    getConfigurationExecutionDetails()
  }, [plantID])

  const hasExecutedRef = useRef(false)

  const getConfigurationExecutionDetails = async () => {
    try {
      const response =
        await DataService.getConfigurationExecutionDetails(keycloak)
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
        // setLoading1(false)
      }
    } catch (error) {
      console.error('Error fetching getConfigurationExecutionDetails:', error)
    } finally {
      // setLoading1(false)
    }
  }

  const onLoad = async () => {
    setLoading1(true)
    if (startDate && endDate && startDate > endDate) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Please Choose Valid Dates!',
        severity: 'warning',
      })
      return
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
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      // console.log('startDateObj', startDateObj)
      // console.log('endDateObj', endDateObj)

      setStartDateObj(startDateObj)
      setEndDateObj(endDateObj)

      const payload = [
        {
          apr: formatDate(startDate),
          UOM: '',
          auditYear: localStorage.getItem('year'),
          normParameterFKId: startDateObj?.NormParameter_FK_Id,
          remarks: 'Initiated',
          id: startDateObj?.Id || null,
          plantId: plantId,
        },
        {
          apr: formatDate(endDate),
          UOM: '',
          auditYear: localStorage.getItem('year'),
          normParameterFKId: endDateObj?.NormParameter_FK_Id,
          remarks: 'Initiated',
          id: endDateObj?.Id || null,
          plantId: plantId,
        },
      ]

      const response = await DataService.executeConfiguration(payload, keycloak)
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Started Successfully!',
          severity: 'success',
        })
        // setIsLoadEnabled(false)
        getConfigurationExecutionDetails()
        setLoading(false)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Execution Falied!',
          severity: 'error',
        })
      }
      getAopSummary()
      return response
    } catch (error) {
      console.error('Execution Falied!', error)
      setLoading(false)
    } finally {
      setLoading(false)
      setLoading1(false)
    }
  }

  useEffect(() => {
    if (tabIndex >= tabs.length) {
      setTabIndex(0)
    }
  }, [tabs])

  // const [isEdited, setIsEdited] = useState(false)
  if (lowerVertName == 'elastomer') {
    return (
      <SelectivityData
        rows={elastomerRows}
        loading={loading}
        fetchData={fetchData}
        setRows={setElastomerRows}
        configType='meg'
        groupBy='Particulars'
        summary={debouncedSummary}
      />
    )
  }

  const one = configurationExecutionDetails.find(
    (item) => item.Name === 'StartDate',
  )
  const two = configurationExecutionDetails.find(
    (item) => item.Name === 'EndDate',
  )

  const startDate1 = new Date(one?.AttributeValue)
  const endDate1 = new Date(two?.AttributeValue)

  if (lowerVertName == 'meg' && lowerVertName !== 'cracker') {
    const megTabs = ['Configuration', 'Constants', 'Report Manual Entry']
    const auditYear = localStorage.getItem('year')

    let displayYear = ''
    if (auditYear) {
      const [start, end] = auditYear.split('-').map(Number)
      displayYear = `(${start - 1}-${(end - 1).toString().slice(-2)})`
    }

    // console.log(loading1)

    return (
      <div>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading1}
        >
          <CircularProgress color='inherit' />
        </Backdrop>

        <Box sx={{ mb: '4px' }}>
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
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography
                      className='grid-title'
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
                      size={'large'}
                    />

                    <Typography
                      className='grid-title'
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
                      size={'large'}
                    />
                  </Box>
                  {/* Load Button */}
                  <Button
                    variant='contained'
                    // onClick={onLoad}
                    onClick={handleOpenDialog}
                    className='btn-load'
                    // disabled={!isLoadEnabled}
                    sx={{ alignSelf: 'flex-end' }}
                  >
                    Load
                  </Button>
                  {configurationExecutionDetails[0]?.ModifiedOn && (
                    <Typography
                      className='summary-title'
                      sx={{ whiteSpace: 'normal' }}
                    >
                      {`(Last refreshed data on: ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)} for the period from ${formatDateForText(startDate1)} to ${formatDateForText(endDate1)})`}
                    </Typography>
                  )}
                </Box>
              </Box>

              <TextField
                label='AOP Design Basis'
                multiline
                // minRows={isAccordionExpanded ? 4 : 20}
                minRows={2}
                fullWidth
                margin='normal'
                variant='outlined'
                disabled={isOldYear == 1}
                value={summary}
                onChange={(e) => {
                  setSummary(e.target.value)
                  setSummaryEdited(true)
                }}
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
            value={tabIndex}
            onChange={(e, newIndex) => setTabIndex(newIndex)}
            sx={{
              borderBottom: '0px solid #ccc',
              '.MuiTabs-indicator': { display: 'none' },
              margin: '0px 0px 10px 0px',
            }}
            textColor='primary'
            indicatorColor='primary'
          >
            {megTabs.map((tab) => (
              <Tab
                key={tab}
                label={
                  tab === 'Report Manual Entry' ? `${tab} ${displayYear}` : tab
                }
                sx={{
                  border: '1px solid #ADD8E6',
                  borderBottom: '1px solid #ADD8E6',
                }}
              />
            ))}
          </Tabs>

          {(() => {
            const currentTab = megTabs[tabIndex]?.toLowerCase()
            switch (currentTab) {
              case 'configuration':
                return (
                  <SelectivityData
                    rows={productionRows}
                    loading={loading}
                    fetchData={fetchData}
                    setRows={setProductionRows}
                    configType='meg'
                    groupBy='Particulars'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                    tabIndex='0'
                  />
                )
              case 'constants':
                return (
                  <SelectivityData
                    rows={productionRowsConstants}
                    loading={loading}
                    fetchData={fetchDataConstants}
                    setRows={setProductionRowsConstants}
                    configType='megConstants'
                    groupBy='Particulars'
                    summaryEdited={summaryEdited}
                    summary={debouncedSummary}
                    onSummaryEditChange={setSummaryEdited}
                    tabIndex='1'
                  />
                )
              case 'report manual entry':
                return (
                  <SelectivityData
                    rows={productionRowsConstantsMannualEntry}
                    loading={loading}
                    fetchData={fetchDataConstantsMnnualEntry}
                    setRows={setProductionRowsConstantsMannualEntry}
                    configType='megConstantsMannualEntry'
                    groupBy='Particulars'
                    summaryEdited={summaryEdited}
                    summary={debouncedSummary}
                    onSummaryEditChange={setSummaryEdited}
                    tabIndex='2'
                  />
                )

              default:
                return null
            }
          })()}
        </Box>

        <Notification
          open={snackbarOpen}
          message={snackbarData?.message || ''}
          severity={snackbarData?.severity || 'info'}
          onClose={() => setSnackbarOpen(false)}
        />

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
      </div>
    )
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading1}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box sx={{ mb: '6px' }}>
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
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Typography
                    className='grid-title'
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
                    size={'large'}
                  />

                  <Typography
                    className='grid-title'
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
                    size={'large'}
                  />
                </Box>
                {/* Load Button */}
                <Button
                  variant='contained'
                  // onClick={onLoad}
                  onClick={handleOpenDialog}
                  className='btn-load'
                  // disabled={startDate > endDate}
                  sx={{ alignSelf: 'flex-end' }}
                >
                  Load
                </Button>
                {configurationExecutionDetails[0]?.ModifiedOn && (
                  <Typography
                    className='summary-title'
                    sx={{ whiteSpace: 'normal' }}
                  >
                    {`(Last refreshed data on: ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)} for the period from ${formatDateForText(startDate1)} to ${formatDateForText(endDate1)})`}
                  </Typography>
                )}
              </Box>

              {/* Right Side: Save Button */}
              {/* <Button
                  variant='contained'
                  onClick={saveSummary}
                  className='btn-save'
                  disabled={!isEdited}
                >
                  Save Summary
                </Button> */}
            </Box>

            <TextField
              label='AOP Design Basis'
              multiline
              // minRows={isAccordionExpanded ? 4 : 20}
              minRows={2}
              fullWidth
              margin='normal'
              variant='outlined'
              disabled={isOldYear == 1}
              value={summary}
              onChange={(e) => {
                setSummary(e.target.value)
                setSummaryEdited(true)
              }}
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
        open={snackbarOpen}
        message={snackbarData?.message || ''}
        severity={snackbarData?.severity || 'info'}
        onClose={() => setSnackbarOpen(false)}
      />

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

      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '5px',
          marginTop: '40px',
        }}
      >
        <Tabs
          sx={{
            borderBottom: '0px solid #ccc',
            '.MuiTabs-indicator': { display: 'none' },
            margin: '-35px 0px -8px 0%',
          }}
          textColor='primary'
          indicatorColor='primary'
          value={tabIndex}
          onChange={(e, newIndex) => setTabIndex(newIndex)}
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
                }}
                label={tabInfo?.displayName || 'loading..'}
              />
            )
          })}
        </Tabs>

        <Box>
          {(() => {
            const currentTabId = tabs[tabIndex]?.toLowerCase()
            switch (currentTabId) {
              // case 'ac3c9ad7-82b5-4550-b04d-fed0f1fb4908': // StartupLosses
              case getTheId('StartupLosses'):
                return (
                  <SelectivityData
                    rows={startUpRows}
                    loading={loading}
                    fetchData={fetchData}
                    setRows={setStartUpRows}
                    configType='StartupLosses'
                    groupBy='TypeDisplayName'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                  />
                )
              case getTheId('Otherlosses'): // Otherlosses
                return (
                  <SelectivityData
                    rows={otherLossRows}
                    loading={loading}
                    fetchData={fetchData}
                    setRows={setOtherLossRows}
                    configType='Otherlosses'
                    groupBy='TypeDisplayName'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                  />
                )
              case getTheId('ShutdownNorms'): // ShutdownNorms
                return (
                  <SelectivityData
                    rows={shutdownNormsRows}
                    loading={loading}
                    setRows={setShutdownRows}
                    fetchData={fetchData}
                    configType='ShutdownNorms'
                    groupBy='TypeDisplayName'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                    // groupBy2='ConfigTypeDisplayName'
                  />
                )
              case getTheId('Receipe'): // Receipe - Fixed to use gradeFetchData
                return (
                  <SelectivityData
                    rows={gradeData}
                    loading={loading}
                    fetchData={fetchGradeData}
                    setRows={setGradeData}
                    configType='grades'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                  />
                )
              case getTheId('ContineGradeChange'): // ContineGradeChange
                return (
                  <SelectivityData
                    rows={continiousGradeData}
                    loading={loading}
                    setRows={setContiniousGradeData}
                    fetchData={fetchData}
                    configType='ContineGradeChange'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                  />
                )
              case getTheId('DisContineGradeChange'): // DisContineGradeChange
                return (
                  <SelectivityData
                    rows={discontiniousGradeData}
                    loading={loading}
                    setRows={setDiscontiniousGradeData}
                    fetchData={fetchData}
                    configType='DisContineGradeChange'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                  />
                )
              default:
                return null
            }
          })()}
        </Box>
      </div>
    </div>
  )
}

export default ConfigurationTable
