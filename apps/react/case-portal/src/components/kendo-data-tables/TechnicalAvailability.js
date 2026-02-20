import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import KendoDataTablesReports from 'components/kendo-data-tables/index-reports'
import { Backdrop, Box, CircularProgress } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import { useSession } from 'SessionStoreContext'
import { DataService } from 'services/DataService'
import { SiteReportDataService } from 'services/SiteReportDataService'
import KendoDataTables from './index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { add } from 'lodash'
import { validateFields } from 'utils/validationUtils'
export default function TechnicalAvailability() {
  const keycloak = useSession()
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
    screenTitle,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id

  const SCREEN_NAME = screenTitle?.title
  const AOP_YEAR = year?.selectedYear
  const thisYear = AOP_YEAR
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [modifiedCells, setModifiedCells] = useState({})
  const [enableSaveAddBtn, setEnableSaveAddBtn] = useState(false)
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

  const headerMap = generateHeaderNames(AOP_YEAR)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const unsavedChangesRef = useRef({ unsavedRows: {}, rowsBeforeChange: {} })
  function getAopShortYears(aopYear) {
    if (!aopYear) return { prev: '', next: '' }
    const match = aopYear.match(/(\d{4})-(\d{2})/)
    if (match) {
      const prev = match[1].slice(-2)
      const next = match[2]
      return { prev, next }
    }
    const year = String(aopYear).slice(-2)
    return { prev: year, next: String(Number(year) + 1).padStart(2, '0') }
  }
  const { prev, next } = getAopShortYears(AOP_YEAR)

  const technicalAvailabilityColumns = [
    {
      field: 'id',
      title: 'ID',
      editable: false,
      hidden: true,
    },
    {
      field: 'sno',
      title: 'S.No',
      widthT: 60,
      editable: false,
      align: 'right',
      format: '{0:0}',
    },
    { field: 'plant', title: 'Plant', widthT: 120, editable: false },
    {
      field: 'fyPrevAOP',
      title: `FY${prev} AOP`,
      editable: true,
      type: 'number',
    },
    {
      field: 'fyPrevActual',
      title: `FY${prev} Actual`,
      editable: true,
      type: 'number',
    },
    {
      field: 'fyCurrAOP',
      title: `FY${next} AOP`,
      editable: true,
      type: 'number',
    },
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 200,
      editable: true,
    },
  ]

  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      const res = await SiteReportDataService.getTechnicalAvailability(
        keycloak,
        SITE_ID,
        AOP_YEAR,
      )

      if (res?.code === 200) {
        const mapped = res?.data?.Data?.map((item, index) => ({
          id: item.id || null,
          sno: index + 1,
          plant: item.plant,
          fyPrevAOP: item.fyPrevAOP,
          fyPrevActual: item.fyPrevActual,
          fyCurrAOP: item.fyCurrAOP,
          remarks: item.remarks,
          siteId: item.siteId,
          aopYear: item.aopYear,
          updatedBy: item.updatedBy,
          updatedDate: item.updatedDate,
          idFromApi: item.id || null,
          isEditable: item?.isEditable,
          originalRemark: item.remarks,
        }))
        setRows(mapped)
      } else {
        setRows([])
      }
    } catch (err) {
      console.error('fetchData error', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, yearChanged, plantID])

  useEffect(() => {
    fetchData()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak])

  const saveChanges = React.useCallback(async () => {
    try {
      setLoading(true)
      const data = Object.values(modifiedCells)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const requiredFields = ['remarks']

      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }

      const payload = data.map((item) => ({
        id: item.id || null,
        plant: item.plant,
        fyPrevAOP: item.fyPrevAOP,
        fyPrevActual: item.fyPrevActual,
        fyCurrAOP: item.fyCurrAOP,
        remarks: item.remarks || 'system generated',
        siteId: SITE_ID,
        aopYear: AOP_YEAR,
        updatedBy: keycloak?.userName || 'system',
        updatedDate: new Date().toISOString(),
      }))

      // 3. Save to API
      const response = await SiteReportDataService.saveTechnicalAvailability(
        keycloak,
        SITE_ID,
        AOP_YEAR,
        payload,
      )

      // 4. Handle API response
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Save failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, keycloak, PLANT_ID, AOP_YEAR, fetchData])

  const deleteRowData = async (paramsForDelete) => {
    setLoading(true)

    try {
      const { idFromApi, id } = paramsForDelete
      const deleteId = id

      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await SiteReportDataService.deleteTechnicalAvailability(
          idFromApi,
          keycloak,
        )
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()
      } else {
        setLoading(false)
      }
    } catch (error) {
      console.error('Error deleting Record!', error)
    }
  }

  const handleExcelUpload = (type) => (rawFile) => {
    uploadPeopleDetails(rawFile, type)
  }

  const handleRemarkCellClick = useCallback((row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }, [])

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
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      allAction: true,
      saveBtn: true,
      showTitleNameBusiness: true,
      titleName: 'Technical Availability',
      adjustedPermissions: true,
      ExcelName: `${lowerVertName}_Technical_Availability_${AOP_YEAR}`,
      //addButton: true,
      //deleteButton: true,
    },
    isOldYear,
  )

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <KendoDataTables
        columns={technicalAvailabilityColumns}
        rows={rows}
        setRows={setRows}
        title='Technical Availability'
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        enableSaveAddBtn={enableSaveAddBtn}
        saveChanges={saveChanges}
        handleRemarkCellClick={handleRemarkCellClick}
        deleteRowData={deleteRowData}
        permissions={adjustedPermissions}
      />
      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}
