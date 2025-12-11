import { Box } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import React, { useCallback, useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { getRoleName } from 'services/role-service'
import { useSession } from 'SessionStoreContext'
import useValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import {
  Backdrop,
  CircularProgress,
} from '../../../node_modules/@mui/material/index'
import KendoDataTables from './index'
import { validateFields } from 'utils/validationUtils'
const CrakcerConstants = () => {
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

  const vertName = verticalChange?.selectedVertical
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const lowerVertName = vertName?.toLowerCase()
  const [tabIndex, setTabIndex] = useState(0)
  const [loading, setLoading] = useState(false)
  const [loading1, setLoading1] = useState(false)

  const [productionRowsConstants, setProductionRowsConstants] = useState([])

  const valueFormat = useValueFormatterConsumption()

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

    fetchConstantsData()
  }, [PLANT_ID, AOP_YEAR])

  const colDefsConstants = [
    {
      field: 'DisplayName',
      title: 'Particulars',
      editable: false,
      widthT: 220,
      hidden: false,
    },
    {
      field: 'UOM',
      title: 'UOM',
      editable: false,
      widthT: 80,
    },
    {
      field: 'ConstantValue',
      title: 'Value',
      editable: true,
      type: 'number',
      widthT: 120,
    },

    {
      field: 'remarks',
      title: 'Remark',
      editable: false,
      type: 'string',
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
        AOP_YEAR,
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
      console.error('Error saving data:', error)
      setLoading(false)
    } finally {
      fetchConstantsData()
      setLoading(false)
    }
  }

  const fetchConstantsData = useCallback(async () => {
    setProductionRowsConstants([])
    try {
      const constantsRes =
        await DataService.getCatalystSelectivityDataConstants(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      if (constantsRes?.code !== 200) {
        setProductionRowsConstants([])
        return
      }

      const data = constantsRes?.data
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
      console.error('Error fetching constants data:', error)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  const handleRemarkCellClickConstants = (row) => {
    if (READ_ONLY) return
    setCurrentRemarkConstants(row.remarks || '')
    setCurrentRowIdConstants(row.id)
    setRemarkDialogOpenConstants(true)
  }

  const uploadCrackerConstant = async (rawFile) => {
    setLoading(true)
    setLoading1(true)

    try {
      let response

      response = await DataService.saveConfigurationExcelConstants(
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
        setLoading1(false)

        setModifiedCellsConstants({})
        fetchConstantsData()
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
        link.setAttribute('download', 'Error File - Constants.xlsx')
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
        setLoading1(false)
        fetchConstantsData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
        setLoading(false)
        setLoading1(false)
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
    uploadCrackerConstant(rawFile)
  }
  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response
      response = await DataService.getConfigurationExcelConstants(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        `${VERTICAL_NAME}_Constant`,
      )
    } catch (error) {
      console.error('Error downloading Excel:', error)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(true)
    }
  }

  const adjustedPermissionsConstants = getAdjustedPermissions(
    {
      showAction: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Criteria',
      saveWithRemark: true,
      saveBtn: true,
      showCalculate: false,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      makePagable: false,
    },
    isOldYear,
  )

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCellsConstants)

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
  }, [modifiedCellsConstants])

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading1}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box>
        <KendoDataTables
          modifiedCells={modifiedCellsConstants}
          setModifiedCells={setModifiedCellsConstants}
          columns={colDefsConstants}
          setRows={setProductionRowsConstants}
          rows={productionRowsConstants}
          onAddRow={(newRow) => console.log('New Row Added:', newRow)}
          onDeleteRow={(id) => console.log('Row Deleted:', id)}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          paginationOptions={[100, 200, 300]}
          saveChanges={saveChanges}
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

export default CrakcerConstants
