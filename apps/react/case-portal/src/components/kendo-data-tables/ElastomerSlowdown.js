import React, { useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import { validateFields } from 'utils/validationUtils'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'

import {
  ShutDownAllColumns,
  SlowdownConfigColumns,
} from 'components/colums/ShutdownColumn'
import { MaintenanceDetailsApiService } from 'services/maintenance-details-api-service'
import { getRoleName } from 'services/role-service'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import KendoDataTables from './index'

const ElastomerSlowdown = ({ permissions }) => {
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

  const AOP_YEAR = year?.selectedYear
  const SCREEN_NAME = screenTitle?.title

  const PLANT_NAME_NO_CASE = plantObject?.name?.toUpperCase()
  const SITE_NAME_NO_CASE = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME_NO_CASE = verticalObject?.name?.toUpperCase()

  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}`

  const FORMATE_DECIMAL = ValueFormatterProduction()
  const vertName = verticalChange?.selectedVertical
  const plantName = plantObject?.name

  const PLANT_NAME_LOWER = plantObject?.name?.toLowerCase()
  const SITE_NAME_LOWER = siteObject?.name?.toLowerCase()

  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const lowerVertName = vertName?.toLowerCase()
  const lowerSiteName = SITE_NAME_LOWER
  const [modifiedCells, setModifiedCells] = React.useState({})
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const SHOW_EXCEL_UPLOAD_BUTTON = true

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const fetchData = async () => {
    if (!PLANT_ID || !AOP_YEAR) return

    setModifiedCells({})
    setLoading(true)

    try {
      const response = await MaintenanceDetailsApiService.getSlowdownConfig(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      const formattedDataShutDown = response?.data?.map((item, index) => ({
        ...item,
        idFromApi: item?.id,
        id: index,
        remark: item.remarks,
        originalRemark: item.remarks,
        description: item.description,
        rate: item.rate,
        maintStartDateTime: new Date(item?.maintStartDateTime),
        maintEndDateTime: new Date(item?.maintEndDateTime),
      }))

      const tableData = formattedDataShutDown || []
      setRows(tableData)
    } catch (error) {
      const status = error.response?.status
      const serverMessage = error.response?.data?.message

      console.error(`Error ${status || 'Unknown'}:`, error)

      if (status === 404) {
        console.warn('No configuration found for this year.')
      } else if (status === 500) {
        alert('Server error. Please contact the administrator.')
      } else {
        alert(
          serverMessage || 'An unexpected error occurred while fetching data.',
        )
      }

      setRows([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    fetchData()
  }, [
    oldYear,
    yearChanged,
    keycloak,
    PLANT_ID,
    lowerVertName,
    lowerSiteName,
    PLANT_NAME_LOWER,
  ])

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
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? true,
      addButton: permissions?.addButton ?? true,
      deleteButton: permissions?.deleteButton ?? true,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      customHeight: permissions?.customHeight,
      allAction: true,
      downloadExcelBtn: true,
      showTitleNameBusiness: true,
      titleName: SCREEN_NAME,
      uploadExcelBtn: SHOW_EXCEL_UPLOAD_BUTTON,
    },
    isOldYear,
  )

  const colDefs = useMemo(() => {
    switch (lowerVertName) {
      default:
        return SlowdownConfigColumns
    }
  }, [lowerVertName, plantName])

  const saveSlowDownConfigurationData = async (rows) => {
    setLoading(true)
    try {
      const response = await MaintenanceDetailsApiService.saveSlowdownConfig(
        PLANT_ID,
        AOP_YEAR,
        rows,
        keycloak,
      )
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})

        unsavedChangesRef.current = {
          unsavedRows: {},
          rowsBeforeChange: {},
        }
        fetchData()
      } else {
        setSnackbarOpen(true)

        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
        setLoading(false)
      }

      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setLoading(false)
    } finally {
      // fetchConfigurationData()
      // setLoading(false)
    }
  }

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCells)

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

      const payload = data.map((row) => ({
        id: row.idFromApi || null,
        rate: row.rate || 0,
        description: row.description || '',
        remarks: row.remarks || '',
        maintStartDateTime: row.maintStartDateTime
          ? new Date(row.maintStartDateTime).toLocaleDateString('en-CA')
          : null,
        maintEndDateTime: row.maintEndDateTime
          ? new Date(row.maintEndDateTime).toLocaleDateString('en-CA')
          : null,
        durationInMins: row.durationInMins || 0,
      }))

      saveSlowDownConfigurationData(payload)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCells])

  const handleRemarkCellClick = (dataItem) => {
    // if (!dataItem?.isEditable) return
    if (READ_ONLY) return
    setCurrentRemark(dataItem.remarks || '')
    setCurrentRowId(dataItem.id)
    setRemarkDialogOpen(true)
  }

  const handleDeleteSlowdownConfig = async (row) => {
    if (!row.idFromApi) {
      setRows((prev) => prev.filter((r) => r.id !== row.id))
      return
    }
    setLoading(true)
    try {
      const response = await MaintenanceDetailsApiService.deleteSlowdownConfig(
        row.idFromApi,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (response && response?.code === 200) {
        setRows((prev) => prev.filter((r) => r.id !== row.id))
        setSnackbarData({
          message: 'Deleted Successfully!',
          severity: 'success',
        })
        setSnackbarOpen(true)
        fetchData()
      } else {
        throw new Error('Unexpected response from server')
      }
    } catch (error) {
      console.error('Delete error:', error)
      setSnackbarData({ message: 'Error deleting record!', severity: 'error' })
      setSnackbarOpen(true)
    }
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        setRows={setRows}
        columns={colDefs}
        rows={rows}
        paginationOptions={[100, 200, 300]}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        apiRef={apiRef}
        deleteId={deleteId}
        setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        open1={open1}
        fetchData={fetchData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        unsavedChangesRef={unsavedChangesRef}
        currentRemark={currentRemark}
        currentRowId={currentRowId}
        setCurrentRemark={setCurrentRemark}
        handleRemarkCellClick={handleRemarkCellClick}
        deleteRowData={handleDeleteSlowdownConfig}
        permissions={{
          addButton: true,
          deleteButton: true,
          saveBtn: true,
          allAction: true,

          downloadExcelBtnFromUI: true,
          ExcelName: `${EXCEL_EXPORT_TITLE}-Slowdown History Config`,
          showTitleNameBusiness: true,
          titleName: 'Configuration1',
        }}
      />
    </div>
  )
}

export default ElastomerSlowdown
