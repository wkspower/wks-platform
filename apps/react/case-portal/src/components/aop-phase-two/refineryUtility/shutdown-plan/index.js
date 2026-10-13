import { useState, useEffect, useCallback, useMemo } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { ShutdownPlanApiService } from '../../services/polyester/shutdownPlanApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { generateExcelName } from '../../common/utilities/excelNameUtil'
import { downloadBase64Excel } from '../../common/utilities/downloadBase64Excel'
import { calculateMonthDuration } from 'components/aop-phase-two/common/utilities/durationHelpers'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'

// ─── Constants ────────────────────────────────────────────────────────────────
const MAINTENANCE_TYPE = 'Shutdown'

// ─── Month options (April → March fiscal order) ───────────────────────────────
const MONTH_OPTIONS = [
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December',
  'January',
  'February',
  'March',
]

/** Add IST (+5:30) offset to a Date before sending to API */
function addTimeOffset(dateTime) {
  if (!dateTime) return null
  const date = new Date(dateTime)
  date.setUTCHours(date.getUTCHours() + 5)
  date.setUTCMinutes(date.getUTCMinutes() + 30)
  return date
}

/**
 * Compute duration from start/end dates if not manually set.
 * Returns "HH.MM" string e.g. "10.30".
 */
function findDuration(row) {
  if (row.durationInHrs) return row.durationInHrs
  if (row.maintStartDateTime && row.maintEndDateTime) {
    const start = new Date(row.maintStartDateTime)
    const end = new Date(row.maintEndDateTime)
    if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
      const mins = (end - start) / (1000 * 60)
      const hours = Math.floor(mins / 60)
      const minutes = mins % 60
      return `${hours}.${Math.round(minutes).toString().padStart(2, '0')}`
    }
  }
  return ''
}

/** Format a duration string to "HH.MM" with zero-padding */
function formatDuration(row) {
  const v = findDuration(row)
  if (!v) return null
  const [h = '00', m = '00'] = String(v).split('.')
  return `${h.padStart(2, '0')}.${m.padStart(2, '0')}`
}

/** Parse fiscal year string "2026-27" → { startYear: 2026, endYear: 2027 } */
function parseFiscalYear(yearStr) {
  if (!yearStr || !yearStr.includes('-')) return null
  const parts = yearStr.split('-')
  const startYear = parseInt(parts[0], 10)
  const endYearShort = parseInt(parts[1], 10)
  const endYear = endYearShort < 100 ? 2000 + endYearShort : endYearShort
  if (isNaN(startYear) || isNaN(endYear)) return null
  return {
    startLimit: new Date(`${startYear}-04-01T00:00:00`),
    endLimit: new Date(`${endYear}-03-31T23:59:59`),
  }
}

/** Format a Date to "dd/mm/yyyy" */
function formatDateDDMMYYYY(date) {
  if (!(date instanceof Date) || isNaN(date)) return ''
  const d = date.getDate().toString().padStart(2, '0')
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  return `${d}/${m}/${date.getFullYear()}`
}

// Helper: parse "HH.MM" string ? total minutes (numeric)
const parseDurationToMinutes = (val) => {
  if (!val && val !== 0) return 0
  const [hrsPart, minPart = '0'] = String(val).split('.')
  const hrs = parseInt(hrsPart, 10) || 0
  const mins = parseInt(String(minPart).padEnd(2, '0').slice(0, 2), 10) || 0
  return hrs * 60 + mins
}

// Helper: format total minutes back to "HH.MM" for display
const formatMinutesToDuration = (totalMins) => {
  const hrs = Math.floor(totalMins / 60)
  const mins = totalMins % 60
  return `${hrs}.${mins.toString().padStart(2, '0')}`
}

// Initial field values for new rows added via the "Add Item" button
const initialFieldValues = {
  discription: '',
  maintenanceId: null,
  maintStartDateTime: null,
  maintEndDateTime: null,
  durationInHrs: '',
  rate: '',
  remark: '',
  isEditable: true,
}

/**
 * Date ↔ Duration auto-calculation config.
 * AdvanceKendoTable's built-in applyDateCalculations handles all 5 scenarios:
 *   1. start + end change  → compute durationInHrs
 *   2. duration change + start exists → compute maintEndDateTime
 *   3. duration change + end exists (no start) → compute maintStartDateTime
 *   4. start changes + duration exists (no end) → compute maintEndDateTime
 *   5. end changes + duration exists (no start) → compute maintStartDateTime
 *
 * requiredInHr: true → duration string is in "HH.MM" format (hours + minutes)
 */
const dateCalculationConfig = {
  dateField1: 'maintStartDateTime',
  dateField2: 'maintEndDateTime',
  daysField: 'durationInHrs',
  requiredInHr: true,
}

// ─── Static column definitions ─────────────────────────────────────────────
const ShutDownColumns = [
  {
    field: 'discription',
    title: 'Shutdown Desc',
    editable: true,
    locked: true,
    type: 'discriptionDrpdwn',
    minWidth: 130,
  },
  {
    field: 'maintenanceId',
    title: 'Maintenance ID',
    editable: false,
    hidden: true,
    isVisible: false,
  },
  {
    field: 'maintStartDateTime',
    title: 'SD - From',
    editable: true,
    type: 'dateTime',
    minWidth: 95,
  },
  {
    field: 'maintEndDateTime',
    title: 'SD - To',
    editable: true,
    type: 'dateTime',
    minWidth: 95,
  },
  {
    field: 'durationInHrs',
    title: 'Duration (hrs)',
    editable: true,
    minWidth: 80,
  },
  {
    field: 'rate',
    title: 'Quantity (TPH)',
    editable: true,
    minWidth: 90,
  },
  {
    field: 'remark',
    title: 'Shutdown Basis',
    editable: true,
    minWidth: 140,
  },
]

// ─── Component ────────────────────────────────────────────────────────────────

const ShutdownPlan = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year, screenTitle } =
    dataGridStore

  const PLANT_ID = plantObject?.id
  const PLANT_NAME = plantObject?.name?.toUpperCase()
  const SITE_NAME = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME = verticalObject?.name?.toUpperCase()
  const AOP_YEAR = year?.selectedYear

  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME}_${SITE_NAME}_${PLANT_NAME}`
  const EXCEL_NAME = generateExcelName(dataGridStore, 'Shutdown_Plan')

  const rateTitle = useMemo(() => {
    const siteName = (siteObject?.name || '').toLowerCase()
    const plantName = (plantObject?.name || '').toLowerCase()

    if (siteName === 'c2' && ['c2 asu', 'c2_asu'].includes(plantName)) {
      return 'Quantity (hourly)'
    }
    if (
      ['dta', 'sez'].includes(siteName) &&
      ['air & asu'].includes(plantName)
    ) {
      return 'Quantity (MWh)'
    }
    return 'Quantity (TPH)'
  }, [siteObject?.name, plantObject?.name])

  const columns = useMemo(() => {
    return ShutDownColumns.map((col) => {
      if (col.field === 'rate') {
        return {
          ...col,
          title: rateTitle,
        }
      }
      return col
    })
  }, [rateTitle])

  // ─── State ──────────────────────────────────────────────────────────────────
  const [allDescriptionDrpdwn, setAllDescriptionDrpdwn] = useState([])
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // ─── Fetch ──────────────────────────────────────────────────────────────────

  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setLoading(true)
    setModifiedCells({})
    try {
      const data = await ShutdownPlanApiService.getShutdownPlanWithValue(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      // API returns a plain array (not {code, data})
      const arr = Array.isArray(data) ? data : data?.data || []
      const formatted = arr.map((item, index) => {
        return {
          ...item,
          idFromApi: item?.id,
          id: index,
          originalRemark: item.remark,
          inEdit: false,
          maintStartDateTime: new Date(item?.maintStartDateTime),
          maintEndDateTime: new Date(item?.maintEndDateTime),
          discription: item.discription,
          durationInHrs: item?.durationInHrs || '',
          rate: item?.rate ?? item?.shutdownRate ?? '',
          monthly: item?.monthly || item?.month || '',
          remark:
            item?.remark === 'null' || item?.remark === 'NULL'
              ? ''
              : item?.remark || '',
        }
      })

      setRows(formatted)
      setOriginalRows(formatted)
    } catch (error) {
      console.error('Error fetching shutdown plan data:', error)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, AOP_YEAR, keycloak])

  // ─── Initial load ────────────────────────────────────────────────────────────

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      setRows([])
      fetchData()
    }
  }, [PLANT_ID, AOP_YEAR])

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return

    const getAllDescriptionDrpdwn = async () => {
      try {
        let data = await ShutdownPlanApiService.dropdownValuesShutdownDesc(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

        let descriptionObjList = []
        if (data?.data) {
          descriptionObjList = data.data.map((product) => ({
            id: product.Name,
            name: product.Name,
            displayName: product.DisplayName,
          }))
        } else if (Array.isArray(data)) {
          descriptionObjList = data.map((product) => ({
            id: product.Name || product.name,
            name: product.Name || product.name,
            displayName: product.DisplayName || product.displayName,
          }))
        }
        setAllDescriptionDrpdwn(descriptionObjList)
      } catch (error) {
        console.error('Error fetching dropdown values', error)
      }
    }

    getAllDescriptionDrpdwn()
  }, [AOP_YEAR, keycloak, PLANT_ID])

  // ─── Save ─────────────────────────────────────────────────────────────────────
  function addTimeOffset(dateTime) {
    if (!dateTime) return null
    const date = new Date(dateTime)
    date.setUTCHours(date.getUTCHours() + 5)
    date.setUTCMinutes(date.getUTCMinutes() + 30)
    return date
  }
  const saveChanges = useCallback(async () => {
    const data = Object.values(modifiedCells)

    console.log('dat ', data)
    // 1. No records
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      return
    }

    // 2. Parse fiscal year boundary
    // const fiscalLimits = parseFiscalYear(AOP_YEAR)

    for (const record of data) {
      const expectedDuration = calculateMonthDuration(record.monthly, AOP_YEAR)
      if (!expectedDuration) continue // no valid month  skip
      const recordMins = parseDurationToMinutes(record.durationInHrs)
      const expectedMins = parseDurationToMinutes(expectedDuration)
      if (recordMins > expectedMins) {
        record.isError = true
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Duration hrs for ${record.monthly} should not exceed ${expectedDuration}.`,
          severity: 'error',
        })
        return
      }
    }

    const modifiedById = {}
    for (const record of data) {
      modifiedById[record.id] = record
    }
    const existingRowIds = new Set(rows.map((r) => r.id))
    const mergedExisting = rows.map((row) => modifiedById[row.id] ?? row)
    const newRows = data.filter((record) => !existingRowIds.has(record.id))
    const allRowsMerged = [...mergedExisting, ...newRows]

    // Group by month and sum total minutes
    const monthTotals = {}
    const monthDisplayName = {}
    for (const row of allRowsMerged) {
      const monthKey = (row.monthly || '').toLowerCase()
      if (!monthKey) continue
      monthTotals[monthKey] =
        (monthTotals[monthKey] || 0) + parseDurationToMinutes(row.durationInHrs)
      if (!monthDisplayName[monthKey]) monthDisplayName[monthKey] = row.monthly
    }

    // Validate each month's total against its max
    for (const [monthKey, totalMins] of Object.entries(monthTotals)) {
      const displayMonth = monthDisplayName[monthKey] || monthKey
      const expectedDuration = calculateMonthDuration(displayMonth, AOP_YEAR)
      const expectedMins = parseDurationToMinutes(expectedDuration)
      if (totalMins > expectedMins) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Total shutdown hours for ${displayMonth} (${formatMinutesToDuration(totalMins)} hrs) exceeds the month limit of ${expectedDuration} hrs. Please reduce the entries for ${displayMonth}.`,
          severity: 'error',
        })
        return
      }
    }

    for (const row of allRowsMerged) {
      const start = new Date(row.maintStartDateTime)
      const end = new Date(row.maintEndDateTime)
      //shutdown timeframe for Multiple months
      if (isNaN(start.getTime()) || isNaN(end.getTime())) continue

      const formatDate = (date) =>
        date.toLocaleDateString('en-GB', {
          day: '2-digit',
          month: 'short',
          year: 'numeric',
        })

      const isSameMonth =
        start.getMonth() === end.getMonth() &&
        start.getFullYear() === end.getFullYear()

      if (!isSameMonth) {
        row.isError = true
        setSnackbarOpen(true)
        setSnackbarData({
          message: `The shutdown timeframe for '${row.discription}' spans multiple months (from ${formatDate(start)} to ${formatDate(end)}). Please split it into separate entries for each month.`,
          severity: 'error',
        })
        return
      }
    }
    // 3.6 Overlapping timeframes
    for (let i = 0; i < allRowsMerged.length; i++) {
      const a = allRowsMerged[i]
      const aStart = new Date(a.maintStartDateTime).getTime()
      const aEnd = new Date(a.maintEndDateTime).getTime()

      if (isNaN(aStart) || isNaN(aEnd)) continue

      for (let j = 0; j < allRowsMerged.length; j++) {
        if (i === j) continue
        const b = allRowsMerged[j]
        
        // Only validate overlap if the descriptions are the same
        const descA = String(a.discription || '').trim().toLowerCase()
        const descB = String(b.discription || '').trim().toLowerCase()
        if (descA !== descB) continue

        const bStart = new Date(b.maintStartDateTime).getTime()
        const bEnd = new Date(b.maintEndDateTime).getTime()

        if (isNaN(bStart) || isNaN(bEnd)) continue

        if (aStart < bEnd && bStart < aEnd) {
          a.isError = true
          b.isError = true
          setSnackbarOpen(true)
          setSnackbarData({
            message: `The shutdown timeframe for "${a.discription || 'this record'}" overlaps with another entry for the same shutdown. Please ensure no overlapping timeframes for the same description.`,
            severity: 'error',
          })
          return
        }
      }
    }

    // 4. Required fields: discription and durationInHrs
    const fieldsToCheck = ['discription', 'durationInHrs']
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'discription',
      'remark',
    )
    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      return
    }

    for (const record of data) {
      if (!record.discription || String(record.discription).trim() === '') {
        record.isError = true
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Shutdown Desc is required for all records.',
          severity: 'error',
        })
        return
      }

      if (
        record.rate === undefined ||
        record.rate === null ||
        String(record.rate).trim() === ''
      ) {
        record.isError = true
        setSnackbarOpen(true)
        setSnackbarData({
          message: `${rateTitle} is required for all records.`,
          severity: 'error',
        })
        return
      }

      const isRemarkEmpty =
        !record.remark ||
        String(record.remark).trim() === '' ||
        String(record.remark).trim().toLowerCase() === 'null'
      if (isRemarkEmpty) {
        record.isError = true
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Please update the field: Shutdown Basis',
          severity: 'error',
        })
        return
      }
    }

    // 6. Build payload
    const shutdownDetails = data.map((row) => ({
      discription: row.discription || row.discriptionDrpdwn,
      rate: row.rate,
      shutdownRate: row.rate ? String(row.rate) : null,
      durationInHrs: (() => {
        const v = findDuration(row)
        if (!v) return null
        const [h = '00', m = '00'] = String(v).split('.')
        return `${h.padStart(2, '0')}.${m.padStart(2, '0')}`
      })(),
      maintStartDateTime: addTimeOffset(row.maintStartDateTime) || null,
      maintEndDateTime: addTimeOffset(row.maintEndDateTime) || null,
      month: row.monthly || row.month, // Use month field
      audityear: AOP_YEAR,
      id: row.idFromApi || null,
      remark: row.remark || 'null',
    }))

    setLoading(true)
    try {
      await ShutdownPlanApiService.saveShutdownPlan(
        keycloak,
        PLANT_ID,
        shutdownDetails,
      )
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Saved Successfully!', severity: 'success' })
      await fetchData()
    } catch (error) {
      console.error('Error saving shutdown plan:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, rows, AOP_YEAR, PLANT_ID, keycloak, fetchData, rateTitle])

  // ─── Delete ───────────────────────────────────────────────────────────────────

  const deleteRowData = useCallback(
    async (dataItem) => {
      const { idFromApi, id } = dataItem

      // New row (not yet persisted) — just remove from local state
      if (!idFromApi) {
        setRows((prev) => prev.filter((row) => row.id !== id))
        setModifiedCells((prev) => {
          const next = { ...prev }
          delete next[id]
          return next
        })
        return
      }

      // Persisted row — call DELETE endpoint
      setLoading(true)
      try {
        await ShutdownPlanApiService.deleteShutdownActivity(
          keycloak,
          idFromApi,
          PLANT_ID,
        )
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted Successfully!',
          severity: 'success',
        })
        await fetchData()
      } catch (error) {
        console.error('Error deleting shutdown activity:', error)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error deleting record!',
          severity: 'error',
        })
      } finally {
        setLoading(false)
      }
    },
    [keycloak, PLANT_ID, fetchData],
  )

  // ─── Import ───────────────────────────────────────────────────────────────────

  const handleExcelUpload = useCallback(
    async (file) => {
      if (!file) return
      setLoading(true)
      try {
        const response =
          await ShutdownPlanApiService.importShutdownPlanForNonProduct(
            file,
            keycloak,
            PLANT_ID,
            AOP_YEAR,
          )

        if (response?.code === 200) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: response?.message || 'Uploaded Successfully!',
            severity: 'success',
          })
          setModifiedCells({})
          await fetchData()
        } else if (response?.code === 400 && response?.data) {
          // Partial save — download error Excel
          downloadBase64Excel(
            response.data,
            `Error File - ${MAINTENANCE_TYPE}.xlsx`,
          )
          setSnackbarOpen(true)
          setSnackbarData({
            message:
              response?.message || 'Partial data saved. Error file downloaded.',
            severity: 'warning',
          })
          await fetchData()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: response?.message || 'Upload Failed!',
            severity: 'error',
          })
        }
      } catch (error) {
        console.error('Error importing shutdown plan:', error)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Unexpected error during import.',
          severity: 'error',
        })
      } finally {
        setLoading(false)
      }
    },
    [keycloak, PLANT_ID, AOP_YEAR, fetchData],
  )

  // ─── Delete Selected ───────────────────────────────────────────────────────────────────

  const handleDeleteSelected = async (deleteIds) => {
    if (!deleteIds || deleteIds?.length === 0) return
    setLoading(true)
    try {
      await ShutdownPlanApiService.deleteMultipleShutdown(
        deleteIds,
        keycloak,
        PLANT_ID,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Record Deleted Successfully!',
        severity: 'success',
      })
      await fetchData()
    } catch (error) {
      console.error('Error deleting shutdown activity:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error deleting record!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  // ─── Export ───────────────────────────────────────────────────────────────────

  const handleExport = useCallback(async () => {
    setSnackbarOpen(true)
    setSnackbarData({ message: 'Excel download started!', severity: 'info' })
    try {
      await ShutdownPlanApiService.exportShutdownPlanWithValue(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        EXCEL_EXPORT_TITLE,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Excel downloaded successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting shutdown plan:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }, [keycloak, PLANT_ID, AOP_YEAR, EXCEL_EXPORT_TITLE])

  // ─── Remark dialog ────────────────────────────────────────────────────────────

  const handleRemarkCellClick = useCallback((row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }, [])

  // ─── Permissions ──────────────────────────────────────────────────────────────

  const permissions = {
    allAction: true,
    showAction: true,
    addButton: true, // "Add Item" button — adds a blank row inline
    deleteButton: true, // trash icon on each editable row
    editButton: true,
    saveBtn: true,
    showImport: true, // Excel import button
    showExport: true, // Excel export (download) button
    showTitleNameBusiness: true,
    titleName: screenTitle?.title,
    showTitle: true,
    ExcelName: EXCEL_NAME,
    remarksEditable: true,
    marginBottom: true,
    deleteMultiple: true,
  }

  // ─── Render ───────────────────────────────────────────────────────────────────

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />

      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        allDescriptionDrpdwn={allDescriptionDrpdwn}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={permissions.titleName}
        permissions={permissions}
        // Remark dialog wiring
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        // Actions
        saveChanges={saveChanges}
        deleteRowData={deleteRowData}
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        fetchData={fetchData}
        // New-row defaults
        initialFieldValues={initialFieldValues}
        // 3-field auto-calculation: start ↔ end ↔ duration
        dateCalculationConfig={dateCalculationConfig}
        // Snackbar
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        handleDeleteSelected={handleDeleteSelected}
        customHeight={70}
        screenType='shutdown'
      />
    </Box>
  )
}

export default ShutdownPlan
