import { Box } from '@mui/material'
import AopTabs from 'components/AopTabs'
import Notification from 'components/Utilities/Notification'
import { verticalEnums } from 'enums/verticalEnums'
// import { usePermissions } from 'hooks/usePermissions'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { PIOImpactApiService } from 'services/Pio-Impact-api-service'
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
import SelectivityData from './SelectivityData'
import { TextArea } from '../../../node_modules/@progress/kendo-react-inputs/index'
import { getRoleName } from 'services/role-service'

const ConfigurationTable = () => {
  const hasExecutedRef = useRef(false)
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak)

  const fetchDataTokenRef = useRef(0)
  const fetchConstantsTokenRef = useRef(0)
  const fetchConstantsManualTokenRef = useRef(0)

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
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

  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  const [loading1, setLoading1] = useState(false)
  const [summaryEdited, setSummaryEdited] = useState(false)
  const [configurationRows, setConfigurationRows] = useState([])
  const [startUpRows, setStartUpRows] = useState([])
  const [otherLossRows, setOtherLossRows] = useState([])
  const [shutdownNormsRows, setShutdownRows] = useState([])
  const [constantsRows, setConstantsRows] = useState([])
  const [productionRows, setProductionRows] = useState([])
  const [elastomerRows, setElastomerRows] = useState([])
  const [productionRowsConstants, setProductionRowsConstants] = useState([])
  const [pioImpactRows, setPioImpactRows] = useState([])
  const [shutdownDataRows, setShutdownDataRows] = useState([])
  const [
    productionRowsConstantsMannualEntry,
    setProductionRowsConstantsMannualEntry,
  ] = useState([])
  const [gradeData, setGradeData] = useState([])
  const [continiousGradeData, setContiniousGradeData] = useState([])
  const [discontiniousGradeData, setDiscontiniousGradeData] = useState([])

  const [reportManualEntry, setReportManualEntry] = useState([])
  const [PIO, setPIO] = useState([])

  const [tabs, setTabs] = useState([])
  const [availableTabs, setAvailableTabs] = useState([])
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
  const [gradeId, setGradeId] = React.useState(null)

  // const { isReadOnly, isReadWrite, isFullAccess, isApproveOnly } =
  //   usePermissions()

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

  const fetchData = async (gradeId = null) => {
    setLoading(true)
    const token = ++fetchDataTokenRef.current

    try {
      setLoading(true)
      var data = []

      const res = await DataService.getCatalystSelectivityData(
        keycloak,
        gradeId,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res?.code != 200) {
        return
      } else {
        data = res?.data
      }

      if (token !== fetchDataTokenRef.current) {
        return
      }

      if (!Array.isArray(data)) {
        return
      }

      const distinctReportTypes = [
        ...new Set(data.map((item) => item.normType).filter(Boolean)),
      ]
      setReportTypes(distinctReportTypes)

      if (
        lowerVertName == verticalEnums.MEG ||
        lowerVertName == verticalEnums.CRACKER ||
        lowerVertName == verticalEnums.ELASTOMER ||
        lowerVertName === 'aromatics'
      ) {
        data = data?.filter(
          (item) =>
            item.normType !== 'Report Manual Entry' &&
            item.normType !== 'PIO Impact' &&
            item.normType !== 'Shutdown',
        )

        const formattedData = data.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remarks,
          srNo: index + 1,
          Particulars: item.normType,
        }))

        setProductionRows(formattedData)
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
        let constantsRows = []
        let configurationRows = []
        let PIORows = []
        let reportManualEntryRows = []

        groups.forEach((normGroup, ConfigTypeName) => {
          let rowsForThisCategory = []
          normGroup.forEach((items, TypeName) => {
            items.forEach((item) => {
              rowsForThisCategory.push({
                ...item,
                idFromApi: item.id,
                originalRemark: item.remarks,
                id: groupId++,
              })
            })
          })
          if (ConfigTypeName == 'Configuration') {
            configurationRows = rowsForThisCategory
          }
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
          } else if (ConfigTypeName == 'Constant') {
            constantsRows = rowsForThisCategory
          } else if (ConfigTypeName == 'Report Manual Entry') {
            reportManualEntryRows = rowsForThisCategory
          } else if (ConfigTypeName == 'PIO Impact') {
            PIORows = rowsForThisCategory
          }
        })

        setShutdownRows(shutdownRows)
        setStartUpRows(startUpRows)
        setOtherLossRows(otherLossRows)
        setContiniousGradeData(continiousGradeRows)
        setDiscontiniousGradeData(discontiniousGradeRows)
        setConstantsRows(constantsRows)
        setConfigurationRows(configurationRows)
        setReportManualEntry(reportManualEntryRows)
        setPIO(PIORows)
      }
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    } finally {
      if (fetchDataTokenRef.current === token) {
        setLoading(false)
      } else {
        // console.info(
        //   'fetchData: not clearing loading because a newer fetch is active',
        // )
      }
    }
  }

  const fetchDataConstants = async () => {
    setLoading(true)
    const token = ++fetchConstantsTokenRef.current
    try {
      var constantsRes = await DataService.getCatalystSelectivityDataConstants(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (token !== fetchConstantsTokenRef.current) {
        return
      }

      if (constantsRes?.code != 200) {
        setProductionRowsConstants([])
        return
      }

      var data = constantsRes?.data

      const formattedData = data.map((item, index) => {
        return {
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.Remarks,
          srNo: index + 1,
          Particulars: item.NormTypeName,
          remarks: item.Remarks,
        }
      })

      setProductionRowsConstants(formattedData)
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      if (fetchConstantsTokenRef.current === token) {
        setLoading(false)
      } else {
        // console.info('fetchDataConstants: newer fetch active, not clearing loading')
      }
    }
  }

  const [reportTypes, setReportTypes] = useState([])

  const fetchDataConstantsMnnualEntry = async () => {
    setLoading(true)

    var constantsRes = []
    const token = ++fetchConstantsManualTokenRef.current
    try {
      constantsRes = await DataService.getCatalystSelectivityData(
        keycloak,
        null,
        PLANT_ID,
        AOP_YEAR,
      )

      if (constantsRes?.code != 200) {
        return
      }

      if (token !== fetchConstantsManualTokenRef.current) {
        return
      }

      const formattedData = constantsRes?.data?.map((item, index) => ({
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

      var dataPioImpact = formattedData?.filter(
        (item) => item?.Particulars == 'PIO Impact',
      )

      var shutdownData = formattedData?.filter(
        (item) => item?.Particulars == 'Shutdown',
      )

      setProductionRowsConstantsMannualEntry(data)
      setPioImpactRows(dataPioImpact)
      setShutdownDataRows(shutdownData)
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      if (fetchConstantsManualTokenRef.current === token) {
        setLoading(false)
      } else {
        // console.info('fetchDataConstantsMnnualEntry: newer fetch active, not clearing loading')
      }
    }
  }
  const fetchGradeData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getPeConfigData(keycloak, PLANT_ID, AOP_YEAR)

      const formattedData = data?.map((item, index) => {
        const converted = {}

        Object.entries(item).forEach(([key, value]) => {
          if (
            key !== 'UOM' &&
            typeof value === 'string' &&
            value.trim() !== '' &&
            !isNaN(value)
          ) {
            converted[key] = value.includes('.')
              ? parseFloat(value)
              : parseInt(value, 10)
          } else {
            converted[key] = value
          }
        })

        return {
          ...converted,
          id: index,
          TypeDisplayName: item?.TypeDisplayName
            ? item?.TypeDisplayName
            : 'Recipe',
        }
      })

      setGradeData(formattedData)
    } catch (error) {
      console.error('Error fetching grade data:', error)
    } finally {
      setLoading(false)
    }
  }

  const getConfigurationTabsMatrix = async () => {
    setLoading(true)
    try {
      var response = await DataService.getConfigurationTabsMatrix(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        SITE_ID,
        VERTICAL_ID,
      )
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

  const carryForwardRecords = async () => {
    try {
      const response = await DataService.carryForwardRecords(
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

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    setTabIndex(0)
    carryForwardRecords()
    getConfigurationExecutionDetails()
  }, [PLANT_ID, AOP_YEAR])

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) {
      return
    }
    getConfigurationExecutionDetails()
    getAopSummary()

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
  }, [configurationExecutionDetails, PLANT_ID])
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
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      setSummary('')
      var res = await DataService.getAopSummary(keycloak, PLANT_ID, AOP_YEAR)
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
    if (!PLANT_ID || !AOP_YEAR) {
      return
    }
    hasExecutedRef.current = false
    getConfigurationExecutionDetails()
  }, [PLANT_ID])

  const getConfigurationExecutionDetails = async () => {
    try {
      const response = await DataService.getConfigurationExecutionDetails(
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
        // setLoading1(false)
      }
    } catch (error) {
      console.error('Error fetching getConfigurationExecutionDetails:', error)
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

  const startDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'StartDate',
  )

  const endDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'EndDate',
  )

  const startDateFromConfig = new Date(startDateConfig?.AttributeValue)
  const endDateDateFromConfig = new Date(endDateConfig?.AttributeValue)

  const handleGradeChange = (gradeId) => {
    setGradeId(gradeId)
  }

  const ConfigurationAccordian = useMemo(() => {
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
                        onChange={(e) => setStartDate(e.value)}
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
                        onChange={(e) => setEndDate(e.value)}
                        style={{ height: '80px' }}
                        size='medium'
                        disabled={READ_ONLY}
                      />
                    </Box>

                    {/* Load Button */}
                    {!isOldYearFlag && (
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
                      alignSelf: 'flex-end', // ?? ensures it's bottom-aligned with the button
                    }}
                  >
                    {`(Last refreshed data on: ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)} for the period from ${formatDateForText(startDateFromConfig)} to ${formatDateForText(endDateDateFromConfig)})`}
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
                // style={{ width: '50%' }}
              />
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

  if (
    (lowerVertName == 'meg' ||
      lowerVertName === 'aromatics' ||
      lowerVertName == 'pvc') &&
    lowerVertName !== 'cracker' &&
    lowerVertName !== 'elastomer'
  ) {
    const isAromatics = lowerVertName === 'aromatics'

    const megTabs = isAromatics
      ? ['Configuration', 'Constants', 'PIO Impact']
      : ['Configuration', 'Constants', 'Report Manual Entry']

    const auditYear = AOP_YEAR
    let displayYear = ''
    if (auditYear) {
      const [start, end] = auditYear.split('-').map(Number)
      displayYear = `(${start - 1}-${(end - 1).toString().slice(-2)})`
    }
    return (
      <div>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading1}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
        {ConfigurationAccordian}
        <Box>
          <AopTabs
            tabIndex={tabIndex}
            setTabIndex={setTabIndex}
            tabs={megTabs.map((tab) =>
              tab === 'Report Manual Entry' ? `${tab} ${displayYear}` : tab,
            )}
          />

          {(() => {
            const currentTab = megTabs[tabIndex]?.toLowerCase()
            const currentTabDisplayName = megTabs[tabIndex]

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
                    reportTypes={reportTypes}
                    currentTabDisplayName={currentTabDisplayName}
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
                    currentTabDisplayName={currentTabDisplayName}
                    reportTypes={reportTypes}
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
                    currentTabDisplayName={currentTabDisplayName}
                    reportTypes={reportTypes}
                  />
                )
              case 'pio impact':
                return (
                  <SelectivityData
                    rows={pioImpactRows}
                    loading={loading}
                    fetchData={fetchDataConstantsMnnualEntry}
                    setRows={setPioImpactRows}
                    configType='pioImpact'
                    groupBy='PIO Impact'
                    summaryEdited={summaryEdited}
                    summary={debouncedSummary}
                    onSummaryEditChange={setSummaryEdited}
                    tabIndex='3'
                    reportTypes={reportTypes}
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )

              case 'shutdown':
                return (
                  <SelectivityData
                    rows={shutdownDataRows}
                    loading={loading}
                    fetchData={fetchDataConstantsMnnualEntry}
                    setRows={setShutdownDataRows}
                    configType='shutdownData'
                    groupBy='Shutdown'
                    summaryEdited={summaryEdited}
                    summary={debouncedSummary}
                    onSummaryEditChange={setSummaryEdited}
                    tabIndex='4'
                    reportTypes={reportTypes}
                    currentTabDisplayName={currentTabDisplayName}
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
        {ConfigurationDialog}
      </div>
    )
  }

  if (lowerVertName === 'cracker') {
    const crackerTabs = ['Configuration', 'Constants']
    const auditYear = AOP_YEAR
    let displayYear = ''
    if (auditYear) {
      const [start, end] = auditYear.split('-').map(Number)
      displayYear = `(${start - 1}-${(end - 1).toString().slice(-2)})`
    }
    return (
      <div>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading1}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
        {ConfigurationAccordian}
        <Box>
          <AopTabs
            tabIndex={tabIndex}
            setTabIndex={setTabIndex}
            tabs={crackerTabs}
          />
          {(() => {
            const currentTab = crackerTabs[tabIndex]?.toLowerCase()
            const currentTabDisplayName = crackerTabs[tabIndex]

            switch (currentTab) {
              case 'configuration':
                return (
                  <SelectivityData
                    rows={productionRows}
                    loading={loading}
                    fetchData={fetchData}
                    setRows={setProductionRows}
                    configType='cracker_configuration'
                    groupBy='Particulars'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                    tabIndex='0'
                    setGradeId={handleGradeChange}
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )
              case 'constants':
                return (
                  <SelectivityData
                    rows={productionRowsConstants}
                    loading={loading}
                    fetchData={fetchDataConstants}
                    setRows={setProductionRowsConstants}
                    configType='cracker_constants'
                    groupBy='Particulars'
                    summaryEdited={summaryEdited}
                    summary={debouncedSummary}
                    onSummaryEditChange={setSummaryEdited}
                    tabIndex='1'
                    currentTabDisplayName={currentTabDisplayName}
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
        {ConfigurationDialog}
      </div>
    )
  }

  if (lowerVertName === 'elastomer') {
    const elastomerTabs = ['Constants', 'Report Manual Entry']
    const auditYear = AOP_YEAR
    let displayYear = ''
    if (auditYear) {
      const [start, end] = auditYear.split('-').map(Number)
      displayYear = `(${start - 1}-${(end - 1).toString().slice(-2)})`
    }
    return (
      <div>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading1}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
        {ConfigurationAccordian}

        <Box>
          {true && (
            <AopTabs
              tabIndex={tabIndex}
              setTabIndex={setTabIndex}
              tabs={elastomerTabs.map((tab) =>
                tab === 'Report Manual Entry' ? `${tab} ${displayYear}` : tab,
              )}
            />
          )}

          {(() => {
            const currentTab = elastomerTabs[tabIndex]?.toLowerCase()
            const currentTabDisplayName = elastomerTabs[tabIndex]
            switch (currentTab) {
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
                    currentTabDisplayName={currentTabDisplayName}
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
                    currentTabDisplayName={currentTabDisplayName}
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
        {ConfigurationDialog}
      </div>
    )
  }

  if (lowerVertName === 'vcm') {
    const elastomerTabs = ['Configuration', 'Constants', 'Report Manual Entry']
    const auditYear = AOP_YEAR
    let displayYear = ''
    if (auditYear) {
      const [start, end] = auditYear.split('-').map(Number)
      displayYear = `(${start - 1}-${(end - 1).toString().slice(-2)})`
    }
    return (
      <div>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading1}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
        {ConfigurationAccordian}
        <Box>
          <AopTabs
            tabIndex={tabIndex}
            setTabIndex={setTabIndex}
            tabs={elastomerTabs.map((tab) =>
              tab === 'Report Manual Entry' ? `${tab} ${displayYear}` : tab,
            )}
          />
          {(() => {
            const currentTab = elastomerTabs[tabIndex]?.toLowerCase()
            const currentTabDisplayName = elastomerTabs[tabIndex]
            switch (currentTab) {
              case 'configuration':
                return (
                  <SelectivityData
                    rows={productionRows}
                    loading={loading}
                    fetchData={fetchData}
                    setRows={setProductionRows}
                    configType='elastomer'
                    groupBy='Particulars'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                    tabIndex='0'
                    currentTabDisplayName={currentTabDisplayName}
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
                    currentTabDisplayName={currentTabDisplayName}
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
                    currentTabDisplayName={currentTabDisplayName}
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
        {ConfigurationDialog}
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
      >
        <AopTabs
          tabIndex={tabIndex}
          setTabIndex={setTabIndex}
          tabs={tabs.map((tabId) => {
            const tabInfo = availableTabs.find(
              (tab) => tab.id.toLowerCase() === tabId.toLowerCase(),
            )
            if (tabInfo) return tabInfo?.displayName || 'loading..'
          })}
        />

        <Box>
          {(() => {
            const currentTabId = tabs[tabIndex]?.toLowerCase()

            const currentTabInfo = availableTabs.find(
              (tab) => tab.id.toLowerCase() === currentTabId,
            )
            const currentTabDisplayName = currentTabInfo?.displayName

            switch (currentTabId) {
              case getTheId('Configuration'):
                return (
                  <SelectivityData
                    rows={configurationRows}
                    loading={loading}
                    fetchData={fetchData}
                    setRows={setConfigurationRows}
                    configType='Configuration'
                    groupBy='TypeDisplayName'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )
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
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )
              case getTheId('Otherlosses'):
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
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )
              case getTheId('ShutdownNorms'):
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
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )
              case getTheId('Constant'):
                return (
                  <SelectivityData
                    rows={constantsRows}
                    loading={loading}
                    setRows={setConstantsRows}
                    fetchData={fetchData}
                    configType='Constant'
                    groupBy='TypeDisplayName'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )
              case getTheId('Receipe'):
                return (
                  <SelectivityData
                    rows={gradeData}
                    loading={loading}
                    fetchData={fetchGradeData}
                    setRows={setGradeData}
                    configType='grades'
                    groupBy='TypeDisplayName'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )
              case getTheId('ContineGradeChange'):
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
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )
              case getTheId('DisContineGradeChange'):
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
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )

              case getTheId('Report Manual Entry'):
                return (
                  <SelectivityData
                    rows={reportManualEntry}
                    loading={loading}
                    setRows={setReportManualEntry}
                    fetchData={fetchData}
                    configType='Report Manual Entry'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                    currentTabDisplayName={currentTabDisplayName}
                  />
                )

              case getTheId('PIO Impact'):
                return (
                  <SelectivityData
                    rows={PIO}
                    loading={loading}
                    setRows={setPIO}
                    fetchData={fetchData}
                    configType='PIO Impact'
                    summary={debouncedSummary}
                    summaryEdited={summaryEdited}
                    onSummaryEditChange={setSummaryEdited}
                    currentTabDisplayName={currentTabDisplayName}
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
