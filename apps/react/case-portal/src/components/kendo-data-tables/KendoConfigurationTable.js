import { Box, Tab, Tabs } from '@mui/material'
import { useEffect, useRef, useState } from 'react'
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

// import CrackerConfig from './KendoConfigCracker'

const ConfigurationTable = () => {
  const keycloak = useSession()

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
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
  const [summary, setSummary] = useState('')
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
        // data = data.sort((a, b) => b.normType.localeCompare(a.normType))

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
        // setTabs([
        //   'StartupLosses',
        //   'OtherLosses',
        //   'ShutdownNorms',
        //   'Receipes',
        //   'ContineGradeChange',
        //   'DisContineGradeChange',
        // ])
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

  // const [startDate, setStartDate] = useState()
  // const [endDate, setEndDate] = useState()

  useEffect(() => {
    getConfigurationTabsMatrix()
    getConfigurationAvailableTabs()
    getAopSummary()
    getConfigurationExecutionDetails()
    if (lowerVertName === 'pe') {
      getConfigurationTabsMatrix()
      getConfigurationAvailableTabs()
      fetchGradeData()
    }

    let year = localStorage.getItem('year')
    const baseYear = parseInt(year.split('-')[0], 10)

    setStartDate(new Date(`${baseYear - 5}-04-01`))
    setEndDate(new Date(`${baseYear}-03-31`))
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

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

  const onLoad = async () => {
    if (startDate && endDate && startDate > endDate) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Please Choose Valid Dates!',
        severity: 'warning',
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

      const startDateObj = configurationExecutionDetails.find(
        (item) => item.Name === 'StartDate',
      )
      const endDateObj = configurationExecutionDetails.find(
        (item) => item.Name === 'EndDate',
      )

      setStartDateObj(startDateObj)
      setEndDateObj(endDateObj)

      var startDate1 = startDateObj?.AttributeValue
      var startDate2 = endDateObj?.AttributeValue

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

      return response
    } catch (error) {
      console.error('Execution Falied!', error)
      setLoading(false)
    } finally {
      setLoading(false)
    }
  }

  const getAopSummary = async () => {
    try {
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

  // const saveSummary = async () => {
  //   setLoading(true)
  //   try {
  //     let plantId = ''
  //     const storedPlant = localStorage.getItem('selectedPlant')
  //     if (storedPlant) {
  //       const parsedPlant = JSON.parse(storedPlant)
  //       plantId = parsedPlant.id
  //     }
  //     let year = localStorage.getItem('year')
  //     const response = await DataService.saveSummaryAOPConsumptionNorm(
  //       plantId,
  //       year,
  //       summary,
  //       keycloak,
  //     )

  //     if (response?.code == 200) {
  //       setSnackbarData({
  //         message: 'Summary Saved Successfully!',
  //         severity: 'success',
  //       })
  //       setLoading(false)
  //       setSnackbarOpen(true)
  //       setIsEdited(false)
  //     } else {
  //       setSnackbarData({
  //         message: 'Summary Saved Failed!',
  //         severity: 'error',
  //       })
  //       setLoading(false)
  //       setSnackbarOpen(true)
  //     }

  //     //

  //     setLoading(false)
  //     return response
  //   } catch (error) {
  //     console.error('Error saving Summary!', error)
  //   } finally {
  //     //
  //     setLoading(false)
  //   }
  // }

  const getConfigurationExecutionDetails = async () => {
    try {
      const data = await DataService.getConfigurationExecutionDetails(keycloak)

      var data1 = data?.data

      setConfigurationExecutionDetails(data1)
    } catch (error) {
      console.error('Error fetching getConfigurationExecutionDetails:', error)
    } finally {
      // handleMenuClose();
    }
  }

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
        summary={summary}
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

    return (
      <div>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading}
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
                    onClick={onLoad}
                    className='btn-load'
                    // disabled={!isLoadEnabled}
                    sx={{ alignSelf: 'flex-end' }}
                  >
                    Load
                  </Button>
                  {configurationExecutionDetails?.[0] ? (
                    <Typography
                      className='summary-title'
                      sx={{ whiteSpace: 'nowrap' }}
                    >
                      {`(Last loaded data on: ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)} for the period from ${formatDateForText(startDate1)} to ${formatDateForText(endDate1)})`}
                    </Typography>
                  ) : (
                    <Typography
                      className='summary-title'
                      sx={{ whiteSpace: 'nowrap' }}
                    >
                      Loading...
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
                    summary={summary}
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
                    summary={summary}
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
                    summary={summary}
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
      </div>
    )
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '5px',
          marginTop: '20px',
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
                label={tabInfo?.displayName || 'N/A'}
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
