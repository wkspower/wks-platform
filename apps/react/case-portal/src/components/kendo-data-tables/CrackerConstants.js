import { Box } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
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
import KendoDataTables from './index'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import moment from '../../../node_modules/moment/moment'

const CrakcerConstants = () => {
  const hasExecutedRef = useRef(false)
  const keycloak = useSession()
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
  const isOldYear = oldYear?.oldYear
  const isOldYearFlag = oldYear?.oldYear === 1
  const vertName = verticalChange?.selectedVertical

  const lowerVertName = vertName?.toLowerCase()
  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  const [loading1, setLoading1] = useState(false)
  const [summaryEdited, setSummaryEdited] = useState(false)

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

  const [remarkDialogOpenConstants, setRemarkDialogOpenConstants] =
    useState(false)
  const [currentRemarkConstants, setCurrentRemarkConstants] = useState('')
  const [modifiedCellsConstants, setModifiedCellsConstants] = React.useState({})
  const [open1, setOpen1] = useState(false)
  const [currentRowIdConstants, setCurrentRowIdConstants] = useState(null)
  const [rowsConstants, setRowsConstants] = useState()

  const unsavedChangesRefConstants = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      showCalculate: false,
    }
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

  useEffect(() => {
    if (!plantID || !AOP_YEAR) return
    setTabIndex(0)
    getConfigurationExecutionDetails()
    fetchData()
  }, [plantID, AOP_YEAR])

  useEffect(() => {
    if (!plantID || !AOP_YEAR) {
      return
    }
    getConfigurationExecutionDetails()
    getAopSummary()
  }, [oldYear, yearChanged, keycloak, plantID])

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
  }, [configurationExecutionDetails, plantID])
  useEffect(() => {
    computeAndSetDates()
  }, [computeAndSetDates])

  function formatDate(date) {
    if (!date) return ''
    const AOP_YEAR = date?.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${AOP_YEAR}-${month}-${day}`
  }
  function formatDateForText(date, time = false) {
    if (!date) return ''
    const parsedDate = new Date(date)
    if (isNaN(parsedDate)) return 'Invalid Date'
    const day = String(parsedDate.getDate()).padStart(2, '0')
    const month = String(parsedDate.getMonth() + 1).padStart(2, '0')
    const AOP_YEAR = parsedDate.getFullYear()
    let formatted = `${day}-${month}-${AOP_YEAR}`
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

  useEffect(() => {
    if (!plantID || !AOP_YEAR) {
      return
    }
    hasExecutedRef.current = false
    getConfigurationExecutionDetails()
  }, [plantID])

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
        // await onLoadTest(startDateObj, endDateObj)
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
      const response1 = await NormalOperationNormsApiService.load1(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        moment(endDate).format('YYYY-MM-DD'),
        moment(startDate).format('YYYY-MM-DD'),
      )

      const response2 = await NormalOperationNormsApiService.load2(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        moment(endDate).format('YYYY-MM-DD'),
        moment(startDate).format('YYYY-MM-DD'),
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

  const isCellEditable = (params) => {
    return params.row.isEditable
  }

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
                    size={'medium'}
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
                    size={'medium'}
                  />
                </Box>
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
                    {`(Last refreshed data on: ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)} for the period from ${formatDateForText(startDateFromConfig)} to ${formatDateForText(endDateDateFromConfig)})`}
                  </Typography>
                )}
              </Box>
            </Box>
            {/* <TextField
              label='AOP Design Basis'
              multiline
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
              className='aop-design-basis'
            /> */}
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

  const colDefsConstants = [
    {
      field: 'DisplayName',
      title: 'Particulars',
    },

    {
      field: 'UOM',
      title: 'UOM',

      editable: false,
    },

    {
      field: 'ConstantValue',
      title: 'Value',
      editable: true,
      align: 'right',
      format: '{0:#.###}',
      type: 'number',
    },
    {
      field: 'remarks',
      title: 'Remark',
    },
  ]

  const saveCatalystData = async (newRow) => {
    setLoading(true)
    try {
      var payload = []

      payload = newRow.map((row) => ({
        apr: row.apr || row.ConstantValue || null,
        may: row.apr || row.ConstantValue || null,
        jun: row.apr || row.ConstantValue || null,
        jul: row.apr || row.ConstantValue || null,
        aug: row.apr || row.ConstantValue || null,
        sep: row.apr || row.ConstantValue || null,
        oct: row.apr || row.ConstantValue || null,
        nov: row.apr || row.ConstantValue || null,
        dec: row.apr || row.ConstantValue || null,
        jan: row.apr || row.ConstantValue || null,
        feb: row.apr || row.ConstantValue || null,
        mar: row.apr || row.ConstantValue || null,
        UOM: '',
        auditYear: AOP_YEAR,
        normParameterFKId: row.normParameterFKId || row.NormParameter_FK_Id,
        remarks: row.remarks,
        id: row.idFromApi || null,
      }))

      const response = await DataService.saveCatalystData(
        PLANT_ID,
        payload,
        keycloak,
      )
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsConstants({})
        setLoading(false)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving Configuration data:', error)
      setLoading(false)
    } finally {
      fetchData()
      setLoading(false)
    }
  }

  const fetchData = async (gradeId) => {
    setLoading(true)
    let response

    try {
      response = await NormalOperationNormsApiService.getNormsConstants(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      let mappedData = response?.data

      let formattedData = mappedData?.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: `main-${index}`,
        originalRemark: item.Remarks,
        remarks: item.Remarks || '',
        Particulars: item.NormTypeName,
      }))

      setRowsConstants(formattedData)
    } catch (error) {
      console.error('Error fetching Data:', error)
    } finally {
      setLoading(false)
    }
  }

  const saveSummary = async (summary) => {
    try {
      const response = await DataService.saveSummaryAOPConsumptionNorm(
        PLANT_ID,
        AOP_YEAR,
        summary,
        keycloak,
      )

      if (response?.code == 200) {
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setLoading(false)
        setSnackbarOpen(true)
        // setIsEdited(false)
      } else {
        setSnackbarData({
          message: 'Saved Failed!',
          severity: 'error',
        })
        setLoading(false)
        // setSnackbarOpen(true)
      }

      //

      // setLoading(false)
      return response
    } catch (error) {
      console.error('Error saving Summary!', error)
    } finally {
      //
      setLoading(false)
    }
  }

  const saveChangesConstants = React.useCallback(async () => {
    try {
      const data = Object.values(modifiedCellsConstants)

      saveSummary(summary)

      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setModifiedCellsConstants({})

        return
      }
      saveCatalystData(data)
    } catch (error) {
      console.error('Error saving Cracker Data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving Cracker Data!',
        severity: 'error',
      })
    }
  }, [modifiedCellsConstants, summary])

  const handleRemarkCellClickConstants = (row) => {
    setCurrentRemarkConstants(row.remarks || '')
    setCurrentRowIdConstants(row.id)
    setRemarkDialogOpenConstants(true)
  }

  const adjustedPermissionsConstants = getAdjustedPermissions(
    {
      showAction: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Criteria',
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Criteria`,
      saveWithRemark: true,
      saveBtn: true,
      showCalculate: false,
    },
    isOldYear,
  )

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
        <KendoDataTables
          modifiedCells={modifiedCellsConstants}
          setModifiedCells={setModifiedCellsConstants}
          columns={colDefsConstants}
          setRows={setRowsConstants}
          rows={rowsConstants}
          onAddRow={(newRow) => console.log('New Row Added:', newRow)}
          onDeleteRow={(id) => console.log('Row Deleted:', id)}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          paginationOptions={[100, 200, 300]}
          saveChanges={saveChangesConstants}
          isCellEditable={isCellEditable}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          open1={open1}
          setOpen1={setOpen1}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          remarkDialogOpen={remarkDialogOpenConstants}
          setRemarkDialogOpen={setRemarkDialogOpenConstants}
          currentRemark={currentRemarkConstants}
          setCurrentRemark={setCurrentRemarkConstants}
          currentRowId={currentRowIdConstants}
          unsavedChangesRef={unsavedChangesRefConstants}
          handleRemarkCellClick={handleRemarkCellClickConstants}
          permissions={adjustedPermissionsConstants}
          groupBy='Particulars'
          plantID={plantID}
          summaryEdited={summaryEdited}
        />
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

export default CrakcerConstants
