import { Box } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import React, { useCallback, useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { getRoleName } from 'services/role-service'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import {
  Backdrop,
  CircularProgress,
} from '../../../node_modules/@mui/material/index'
import KendoDataTables from './index'
import { validateFields } from 'utils/validationUtils'
const CrackerConfiguration = (props) => {
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
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const [reportTypes, setReportTypes] = useState([])
  const vertName = verticalChange?.selectedVertical
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const lowerVertName = vertName?.toLowerCase()
  //const [tabIndex, setTabIndex] = useState(0)

  const PLANT_NAME_NO_CASE = plantObject?.name?.toUpperCase()
  const SITE_NAME_NO_CASE = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME_NO_CASE = verticalObject?.name?.toUpperCase()

  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}`

  const [loading, setLoading] = useState(false)

  const [productionRows, setProductionRows] = useState([])

  const valueFormat = ValueFormatterProduction()

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

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [modifiedCellsConfiguration, setModifiedCellsConfiguration] =
    React.useState({})
  const [open1, setOpen1] = useState(false)
  const [currentRowId, setCurrentRowId] = useState(null)
  const [rowsConstants, setRowsConstants] = useState()
  const headerMap = generateHeaderNames(AOP_YEAR)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const tabIndex = props.tabIndex ?? 0
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
      downloadExcelBtn: false,
      uploadExcelBtn: false,
    }
  }

  const colDefsConfiguration = [
    {
      field: 'Particulars',
      title: 'Type',
      widthT: 200,
      editable: false,
      hidden: true,
    },
    {
      field: 'normParameterFKId',
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      widthT: 140,
      editable: false,
    },
    {
      field: 'UOM',
      title: 'UOM',
      editable: false,
      widthT: 60,
    },
    {
      field: 'apr',
      title: headerMap[4],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'may',
      title: headerMap[5],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'jun',
      title: headerMap[6],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'jul',
      title: headerMap[7],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'aug',
      title: headerMap[8],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'sep',
      title: headerMap[9],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'oct',
      title: headerMap[10],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'nov',
      title: headerMap[11],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'dec',
      title: headerMap[12],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'jan',
      title: headerMap[1],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'feb',
      title: headerMap[2],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'mar',
      title: headerMap[3],
      editable: true,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'negativeNumber',
    },
    {
      field: 'remarks',
      title: 'Remark',
      widthT: 130,
      editable: true,
    },
    {
      field: 'isEditable',
      title: 'isEditable',
      hidden: true,
    },
  ]

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    fetchData()
  }, [PLANT_ID, AOP_YEAR])

  const fetchData = useCallback(
    async (gradeId = null) => {
      setProductionRows([])
      setLoading(true)

      var data = []

      try {
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

        const distinctReportTypes = [
          ...new Set(data.map((item) => item.normType).filter(Boolean)),
        ]

        setReportTypes(distinctReportTypes)

        const filteredData = data?.filter(
          (item) => item.normType !== 'Report Manual Entry',
        )
        const formattedData = filteredData.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remarks,
          srNo: index + 1,
          Particulars: item.normType,
        }))
        setProductionRows(formattedData)
      } catch (error) {
        console.error('Error fetching data:', error)
      } finally {
        setLoading(false)
      }
    },
    [keycloak, PLANT_ID, AOP_YEAR],
  )

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const uploadCrackerConfiguration = async (rawFile) => {
    setLoading(true)

    try {
      let response

      response = await DataService.saveConfigurationExcel(
        rawFile,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })

        setLoading(false)

        setModifiedCellsConfiguration({})
        fetchData()
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteNumbers = Array.from(byteCharacters, (char) =>
          char.charCodeAt(0),
        )
        const byteArray = new Uint8Array(byteNumbers)

        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })

        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'Error File - Configuration.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })

        setLoading(false)
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })

        setLoading(false)
      }

      return response
    } catch (error) {
      console.error('Error uploading xcel:', error)
      setSnackbarOpen(true)

      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = (rawFile) => {
    uploadCrackerConfiguration(rawFile)
  }
  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      var report_t = []

      if (tabIndex == 0) {
        report_t = reportTypes.filter(
          (type) =>
            type !== 'Report Manual Entry' &&
            type !== 'Shutdown' &&
            type !== 'PIO Impact',
        )
      }

      await DataService.getConfigurationExcel(
        keycloak,
        report_t,
        PLANT_ID,
        AOP_YEAR,
        EXCEL_EXPORT_TITLE,
        'Configuration',
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error!', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    }
  }
  const adjustedPermissionsConfiguration = getAdjustedPermissions(
    {
      showAction: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Configuration',
      saveWithRemark: true,
      saveBtn: true,
      showCalculate: false,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      makePagable: false,
    },
    isOldYear,
  )
  const saveCatalystData = async (data) => {
    setLoading(true)
    try {
      const payload = data.map((row) => ({
        apr: row.apr || row.ConstantValue || null,
        may: row.may || null,
        jun: row.jun || null,
        jul: row.jul || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        jan: row.jan || null,
        feb: row.feb || null,
        mar: row.mar || null,
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
        AOP_YEAR,
      )

      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCellsConfiguration({})
        setLoading(false)
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Save Failed!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving data!',
        severity: 'error',
      })
      setLoading(false)
    } finally {
      setLoading(false)
    }
  }
  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCellsConfiguration)

      const requiredFields = ['remarks']
      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }
      saveCatalystData(data)
    } catch (error) {
      // Handle error if necessary
    }
  }, [modifiedCellsConfiguration])

  //  console.log('productionRows', productionRows)
  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box>
        <KendoDataTables
          modifiedCells={modifiedCellsConfiguration}
          setModifiedCells={setModifiedCellsConfiguration}
          columns={colDefsConfiguration}
          setRows={setProductionRows}
          rows={productionRows}
          onAddRow={(newRow) => console.log('New Row Added:', newRow)}
          onDeleteRow={(id) => console.log('Row Deleted:', id)}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          paginationOptions={[100, 200, 300]}
          saveChanges={saveChanges}
          fetchData={fetchData}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          open1={open1}
          setOpen1={setOpen1}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          unsavedChangesRef={unsavedChangesRef}
          handleRemarkCellClick={handleRemarkCellClick}
          permissions={adjustedPermissionsConfiguration}
          groupBy='Particulars'
          plantID={PLANT_ID}
          handleExcelUpload={handleExcelUpload}
          downloadExcelForConfiguration={downloadExcelForConfiguration}
        />
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

export default CrackerConfiguration
