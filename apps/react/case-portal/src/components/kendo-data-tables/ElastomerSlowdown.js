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

      const yearStr = AOP_YEAR
      let startLimit, endLimit
      if (yearStr) {
        const [startYear, endYear] = yearStr
          .split('-')
          .map((y) => parseInt(y.trim(), 10))
        if (!isNaN(startYear) && !isNaN(endYear)) {
          // Use yyyy-mm-dd format for reliable parsing
          startLimit = new Date(`20${startYear}-04-01T00:00:00`)
          endLimit = new Date(`20${endYear}-03-31T23:59:59`)
        }
      }

      // Helper to format date as dd/mm/yyyy
      // eslint-disable-next-line
      function formatDateDDMMYYYY(date) {
        if (!(date instanceof Date) || isNaN(date)) return ''
        const d = date.getDate().toString().padStart(2, '0')
        const m = (date.getMonth() + 1).toString().padStart(2, '0')
        const y = date.getFullYear()
        return `${d}/${m}/${y}`
      }

      for (const record of data) {
        const startDate =
          record.maintStartDateTime instanceof Date
            ? record.maintStartDateTime
            : new Date(record.maintStartDateTime)
        const endDate =
          record.maintEndDateTime instanceof Date
            ? record.maintEndDateTime
            : new Date(record.maintEndDateTime)

        // Validate date format: dd/mm/yyyy (by parsing and checking)
        if (
          startLimit &&
          endLimit &&
          (!startDate ||
            !endDate ||
            isNaN(startDate) ||
            isNaN(endDate) ||
            startDate < startLimit ||
            startDate > endLimit ||
            endDate < startLimit ||
            endDate > endLimit)
        ) {
          record.isError = true
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Dates must be between ${formatDateDDMMYYYY(startLimit)} and ${formatDateDDMMYYYY(endLimit)} for selected year. `,
            severity: 'error',
          })
          return
        }
      }


      // Select required fields based on vertical
      const requiredFields = ['durationInMins', 'remarks', 'rate']

      // Missing required fields
      for (const record of data) {
        for (const field of requiredFields) {
          const value = record[field]
          if (
            value === null ||
            value === undefined ||
            (typeof value === 'string' && value.trim() === '')
          ) {
            let displayField = field
            if (field === 'productName1') displayField = 'Particulars'
            else if (field === 'monthly') displayField = 'Month'
            record.isError = true
            setRows((prevRows) =>
              prevRows.map((row) => {
                if (row.id === record.id) {
                  return { ...row, isError: true }
                }
                return row
              }),
            )
            setSnackbarOpen(true)
            setSnackbarData({
              message: `Required field "${displayField}" is missing for "${record.durationInMins || 'this record'}".`,
              severity: 'error',
            })
            return
          }
        }
      }

      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        data.forEach((r) => (r.isError = true))
        setRows((prevRows) =>
          prevRows.map((row) =>
            data.some((d) => d.id === row.id) ? { ...row, isError: true } : row,
          ),
        )
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }



      // Date required + Start < End check

      for (const record of data) {
        const startMissing = !record.maintStartDateTime
        const endMissing = !record.maintEndDateTime
        if (startMissing || endMissing) {
          record.isError = true
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Start Date and End Date are required for all records.',
            severity: 'error',
          })
          return
        }
        const startDate =
          record.maintStartDateTime instanceof Date
            ? record.maintStartDateTime
            : new Date(record.maintStartDateTime)
        const endDate =
          record.maintEndDateTime instanceof Date
            ? record.maintEndDateTime
            : new Date(record.maintEndDateTime)
        if (
          startDate &&
          endDate &&
          startDate.getTime() >= endDate.getTime()
        ) {
          record.isError = true
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Start time must be before end time for "${record.duration || 'this record'}".`,
            severity: 'error',
          })
          return
        }
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
  }, [modifiedCells, rows, AOP_YEAR])

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
          titleName: 'Slowdown History Config',
        }}
      />
    </div>
  )
}

export default ElastomerSlowdown
