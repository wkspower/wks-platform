import React, { useState, useEffect, useCallback, useMemo } from 'react'
import { Box, Alert } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'

// Phase Two Common Components & Utilities
import AdvanceKendoTable from '../common/AdvanceKendoTable/index'
import { customValueFormatterPhaseTwo } from '../common/ValueFormatterPhaseTwo'
import { generateExcelNameWithoutExt } from '../common/utilities/excelNameUtil'
import Notification from '../common/utilities/Notification'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'

// Phase Two API Services
import { SignOffReportApiService } from '../services/common/signOffReportApiService'
import { HistoricPeriodBasisApiService } from '../services/common/historicPeriodBasisApiService'

/**
 * SignOffReport Component
 * Uses configuration execution details API to get StartDate and EndDate,
 * and dynamically renders a single sign-off report grid with 5 decimal places for numbers.
 */
const SignOffReport = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore

  // Store variables
  const PLANT_ID = plantObject?.id || plantObject?.value || plantObject?.plantId
  const AOP_YEAR = year?.selectedYear || year

  // Local state for single grid
  const [loading, setLoading] = useState(false)
  const [columns, setColumns] = useState([])
  const [rows, setRows] = useState([])
  const [gridTitle, setGridTitle] = useState('Sign Off Report')
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [unitNo, setUnitNo] = useState(null)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Notification state
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  // 5 Decimal place formatter for numeric columns
  const numberFormatFiveDecimals = useMemo(
    () => customValueFormatterPhaseTwo(5),
    [],
  )

  /**
   * Helper function to format Date object or date string to YYYY-MM-DD
   */
  const formatDateForAPI = useCallback((date) => {
    if (!date) return null
    if (typeof date === 'string') {
      const trimmed = date.trim()
      const isoMatch = trimmed.match(/^(\d{4}-\d{2}-\d{2})/)
      if (isoMatch) return isoMatch[1]
      if (/^\d{2}-\d{2}-\d{4}$/.test(trimmed)) {
        const [d, m, y] = trimmed.split('-')
        return `${y}-${m}-${d}`
      }
    }
    const d = new Date(date)
    if (isNaN(d.getTime())) return null
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }, [])

  /**
   * Calculate fallback dates from AOP year (1st April of prior year to 31st March)
   */
  const getDatesFromAopYear = useCallback((aopYear) => {
    if (typeof aopYear === 'string' && aopYear.includes('-')) {
      const startYear = parseInt(aopYear.split('-')[0], 10)
      if (!isNaN(startYear)) {
        return {
          startDate: `${startYear - 1}-04-01`,
          endDate: `${startYear}-03-31`,
        }
      }
    }
    return { startDate: null, endDate: null }
  }, [])

  /**
   * Enriches backend column metadata into AdvanceKendoTable-compliant columns.
   * Ensures numbers are configured to 5 decimal places.
   */
  const enrichColumns = useCallback(
    (backendCols = []) => {
      if (!Array.isArray(backendCols)) return []

      return backendCols
        .filter((col) => {
          if (!col || !col.field) return false
          const fieldUpper = col.field.trim().toUpperCase()
          const titleUpper = col.title?.trim().toUpperCase()
          return (
            fieldUpper !== 'GRID_TYPE' &&
            fieldUpper !== 'ID' &&
            titleUpper !== 'ID' &&
            fieldUpper !== 'UNITNO' &&
            titleUpper !== 'UNITNO'
          )
        })
        .map((col, index) => {
          const isNumber = col.type === 'number'
          const isDate = col.type === 'date'
          const isFirstCol = index === 0
          const formattedTitle = col.title || col.field.replace(/_/g, ' ')
          const isRemarks =
            col.field?.toLowerCase().includes('remarks') ||
            col.title?.toLowerCase().includes('remark')

          const baseConfig = {
            field: col.field,
            title: formattedTitle,
            editable: isRemarks ? true : Boolean(col.editable),
            hidden: col.hidden || false,
            locked: isFirstCol && backendCols.length > 6,
          }

          if (isRemarks) {
            return {
              ...baseConfig,
              field: 'remarks',
              title: 'Remarks',
              type: 'textarea',
              align: 'left',
              headerAlign: 'left',
              widthT: Math.max(180, formattedTitle.length * 11),
              minWidth: 150,
            }
          }

          if (isNumber) {
            return {
              ...baseConfig,
              type: 'number',
              format: numberFormatFiveDecimals,
              align: 'right',
              headerAlign: 'left',
              widthT: Math.max(150, Math.min(formattedTitle.length * 12, 220)),
              minWidth: 130,
            }
          }

          if (isDate) {
            return {
              ...baseConfig,
              type: 'date',
              format: '{0:dd-MM-yyyy}',
              align: 'center',
              headerAlign: 'left',
              widthT: 160,
              minWidth: 140,
            }
          }

          // Text / String columns
          return {
            ...baseConfig,
            type: 'text',
            align: 'left',
            headerAlign: 'left',
            widthT: Math.max(
              isFirstCol ? 260 : 180,
              formattedTitle.length * 11,
            ),
            minWidth: isFirstCol ? 220 : 150,
          }
        })
    },
    [numberFormatFiveDecimals],
  )

  /**
   * Normalizes raw row data from API.
   * Ensures each row has a stable unique ID and numeric columns are parsed as numbers.
   */
  const normalizeRows = useCallback((rawRows = [], cols = []) => {
    if (!Array.isArray(rawRows)) return []

    const numericFields = new Set(
      cols.filter((c) => c.type === 'number').map((c) => c.field),
    )
    const dateFields = new Set(
      cols.filter((c) => c.type === 'date').map((c) => c.field),
    )

    return rawRows.map((row, idx) => {
      const normalizedRow = { ...row }

      // Guarantee unique key for Kendo and preserve database UUID
      const rowId = normalizedRow.Id ?? normalizedRow.id ?? (idx + 1)
      normalizedRow.id = rowId
      normalizedRow.Id = rowId

      // Convert numeric fields to JavaScript numbers so Kendo number formatting applies
      numericFields.forEach((field) => {
        const val = normalizedRow[field]
        if (val !== null && val !== undefined && val !== '') {
          const parsed =
            typeof val === 'number'
              ? val
              : parseFloat(String(val).replace(/,/g, '').trim())
          normalizedRow[field] = !isNaN(parsed) ? parsed : null
        } else {
          normalizedRow[field] = null
        }
      })

      // Convert date strings to Date objects if needed
      dateFields.forEach((field) => {
        const val = normalizedRow[field]
        if (val) {
          const d = new Date(val)
          if (!isNaN(d.getTime())) {
            normalizedRow[field] = d
          }
        }
      })

      // Ensure remarks field has default empty string and exists
      const remarks = normalizedRow?.Remarks ?? normalizedRow?.remarks ?? ''
      normalizedRow.remarks = remarks

      if (normalizedRow?.UnitNo) {
        setUnitNo(String(normalizedRow.UnitNo))
      }
      return normalizedRow
    })
  }, [])

  /**
   * Fetch configuration execution details for StartDate & EndDate,
   * then retrieve the Sign Off Report grid data.
   */
  const fetchSignOffReport = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) {
      setColumns([])
      setRows([])
      return
    }

    setLoading(true)
    try {
      // 1. Use configuration execution API to get StartDate and EndDate
      let periodFrom = null
      let periodTo = null

      try {
        const configResponse =
          await HistoricPeriodBasisApiService.getConfigurationExecutionDetails(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
          )

        const configDetails = Array.isArray(configResponse?.data)
          ? configResponse.data
          : Array.isArray(configResponse?.data?.data)
            ? configResponse.data.data
            : []

        const startDateObj = configDetails.find((d) => d.Name === 'StartDate')
        const endDateObj = configDetails.find((d) => d.Name === 'EndDate')

        const rawStartDate =
          startDateObj?.AttributeValue ||
          startDateObj?.Value ||
          startDateObj?.apr
        const rawEndDate =
          endDateObj?.AttributeValue || endDateObj?.Value || endDateObj?.apr

        periodFrom = formatDateForAPI(rawStartDate)
        periodTo = formatDateForAPI(rawEndDate)
      } catch (configErr) {
        console.warn(
          'Could not fetch configuration execution details, falling back to AOP year calculation:',
          configErr,
        )
      }

      // Fallback to computed dates from AOP year if configuration execution has no records
      if (!periodFrom || !periodTo) {
        const fallbackDates = getDatesFromAopYear(AOP_YEAR)
        if (!periodFrom) periodFrom = fallbackDates.startDate
        if (!periodTo) periodTo = fallbackDates.endDate
      }

      // 2. Fetch Sign Off Report with plantId, year, periodFrom, periodTo
      const response = await SignOffReportApiService.getSignOffReport(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        periodFrom,
        periodTo,
      )

      const statusCode = response?.code || (response?.status ?? 200)
      const responseData = response?.data

      if (statusCode === 200 && responseData) {
        // Extract the single grid (whether returned directly as object or as first element of array)
        const singleGrid = Array.isArray(responseData)
          ? responseData[0]
          : Array.isArray(responseData?.data)
            ? responseData.data[0]
            : responseData

        if (!singleGrid) {
          setColumns([])
          setRows([])
          setSnackbarData({
            message:
              response?.message ||
              'No records returned for the selected criteria.',
            severity: 'info',
          })
          setSnackbarOpen(true)
          return
        }

        const rawCols = Array.isArray(singleGrid.columns)
          ? singleGrid.columns
          : []
        const rawRows = Array.isArray(singleGrid.data) ? singleGrid.data : []

        const enrichedCols = enrichColumns(rawCols)
        const enrichedRows = normalizeRows(rawRows, enrichedCols)
        setColumns(enrichedCols)
        setRows(enrichedRows)
        setOriginalRows(JSON.parse(JSON.stringify(enrichedRows)))
        setModifiedCells({})
      } else {
        setColumns([])
        setRows([])
        setOriginalRows([])
        setModifiedCells({})
        setSnackbarData({
          message:
            response?.message ||
            'Failed to fetch Sign Off Report. Please try again.',
          severity: 'error',
        })
        setSnackbarOpen(true)
      }
    } catch (error) {
      console.error('Error fetching Sign Off Report:', error)
      setColumns([])
      setRows([])
      setOriginalRows([])
      setModifiedCells({})
      setSnackbarData({
        message:
          'Error fetching Sign Off Report data. Please verify your connection.',
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }, [
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    formatDateForAPI,
    getDatesFromAopYear,
    enrichColumns,
    normalizeRows,
  ])

  const handleRemarkCellClick = useCallback((row) => {
    setCurrentRemark(row.remarks ?? '')
    setCurrentRowId(row.id ?? row.Id)
    setRemarkDialogOpen(true)
  }, [])

  /**
   * Helper to retrieve all modified rows (from modifiedCells or row comparisons)
   */
  const getModifiedRows = useCallback(() => {
    const fromModifiedCells = Object.values(modifiedCells || {}).filter(
      (r) => r && r.inEdit !== false,
    )
    if (fromModifiedCells.length > 0) {
      return fromModifiedCells
    }

    // Fallback: compare rows against originalRows
    if (
      Array.isArray(rows) &&
      Array.isArray(originalRows) &&
      originalRows.length > 0
    ) {
      return rows.filter((row) => {
        const orig = originalRows.find(
          (o) => o.id === row.id || (row.Id && o.Id === row.Id),
        )
        if (!orig) return false
        const currentRemark = String(row.Remarks ?? row.remarks ?? '').trim()
        const originalRemark = String(orig.Remarks ?? orig.remarks ?? '').trim()
        return currentRemark !== originalRemark
      })
    }

    return []
  }, [modifiedCells, rows, originalRows])

  /**
   * Save Sign Off Report Remarks with validation
   */
  const saveChanges = useCallback(async () => {
    const candidateRows = getModifiedRows()

    if (!candidateRows || candidateRows.length === 0) {
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setSnackbarOpen(true)
      return
    }

    // Filter only those rows where remark actually changed from originalRows
    const changedRows = candidateRows.filter((row) => {
      const orig = originalRows.find(
        (o) => o.id === row.id || (row.Id && o.Id === row.Id),
      )
      const currentRemark = String(row.remarks ?? '').trim()
      const originalRemark = String(orig?.remarks ?? '').trim()
      return !orig || currentRemark !== originalRemark
    })

    if (changedRows.length === 0) {
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setSnackbarOpen(true)
      return
    }

    // Remark validation: Remarks cannot be empty for any modified row
    const invalidRows = changedRows.filter((row) => {
      const remark = String(row.remarks ?? '').trim()
      return remark === ''
    })

    if (invalidRows.length > 0) {
      const displayNames = invalidRows
        .map(
          (row) =>
            row.Particulars ||
            row.particulars ||
            row.NormParameterName ||
            row.normParameterName ||
            row.Parameter ||
            row.parameter ||
            (columns[0]?.field ? row[columns[0].field] : '') ||
            `Row ${row.id}`,
        )
        .filter(Boolean)
        .join(', ')

      setSnackbarData({
        message: `Remarks is required for: ${displayNames}`,
        severity: 'error',
      })
      setSnackbarOpen(true)
      return
    }

    // Prepare payload with expected keys: id and remarks
    const signOffReportDTOList = changedRows.map((row) => ({
      id: String(row.Id ?? row.id ?? ''),
      remarks: String(row.remarks ?? '').trim(),
    }))

    setLoading(true)
    try {
      const response = await SignOffReportApiService.saveSignOffReportRemarks(
        keycloak,
        AOP_YEAR,
        signOffReportDTOList,
      )

      const statusCode = response?.code || (response?.status ?? 200)

      if (statusCode === 200) {
        setSnackbarData({
          message:
            response?.message || 'Sign off report remarks saved successfully',
          severity: 'success',
        })
        setSnackbarOpen(true)
        setModifiedCells({})
        await fetchSignOffReport()
      } else {
        setSnackbarData({
          message:
            response?.message || 'Failed to save Sign Off Report remarks.',
          severity: 'error',
        })
        setSnackbarOpen(true)
      }
    } catch (error) {
      console.error('Error saving Sign Off Report remarks:', error)
      setSnackbarData({
        message:
          error?.response?.data?.message ||
          error?.message ||
          'Error saving Sign Off Report remarks. Please try again.',
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }, [
    getModifiedRows,
    originalRows,
    columns,
    keycloak,
    AOP_YEAR,
    fetchSignOffReport,
  ])

  // Fetch report whenever plant or year changes
  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchSignOffReport()
    }
  }, [PLANT_ID, AOP_YEAR, fetchSignOffReport])

  // Excel filename generator
  const excelFileName = useMemo(() => {
    return generateExcelNameWithoutExt(dataGridStore, gridTitle)
  }, [dataGridStore, gridTitle])

  const permissions = useMemo(() => {
    return {
      showAction: true,
      allAction: true,
      saveBtn: true,
      showExport: false,
      downloadExcelBtnFromUI: true,
      ExcelName: excelFileName,
      showTitle: true,
      titleName: gridTitle,
      remarksEditable: true,
      showNote: Boolean(unitNo),
      note: unitNo ? `(${unitNo})` : '',
    }
  }, [excelFileName, gridTitle, unitNo])

  return (
    <div>
      {/* Global Loader */}
      <LoaderBackdrop open={loading} />

      {/* Modern Toast Notification */}
      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />

      <AdvanceKendoTable
        title={gridTitle}
        columns={columns}
        rows={rows}
        setRows={setRows}
        loading={loading}
        pagable={false}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        saveChanges={saveChanges}
        permissions={permissions}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
      />
    </div>
  )
}

export default SignOffReport
