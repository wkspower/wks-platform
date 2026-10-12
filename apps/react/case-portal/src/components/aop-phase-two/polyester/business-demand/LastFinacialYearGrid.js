import React, { useState, useEffect, useCallback, useRef } from 'react'
import { Box } from '@mui/material'
import { useDispatch, useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { BusinessDemandApiService } from '../../services/polyester/businessDemandApiService'
import { generateExcelNameWithoutExt } from 'components/aop-phase-two/common/utilities/excelNameUtil'
import { convertRows, UNIT_OPTIONS, DEFAULT_UNIT } from './utils'
import { customValueFormatterPhaseTwo } from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import GetFinancialYear from 'components/Utilities/GetFinancialYear'

const LastFinacialYearGrid = () => {
  const dispatch = useDispatch()
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    oldYear,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
    isReleased,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const IS_OLD_YEAR = oldYear?.oldYear
  const IS_RELEASED = isReleased

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, IS_RELEASED)
  const valueFormat = ValueFormatterProduction()
  const customFormat = customValueFormatterPhaseTwo(5)
  const { previousFYFormatted } = GetFinancialYear(AOP_YEAR)
  const headerMap = generateHeaderNames(previousFYFormatted)
  const EXCEL_NAME = generateExcelNameWithoutExt(
    dataGridStore,
    'Last Financial',
  )

  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const [selectedUnit, setSelectedUnit] = useState(DEFAULT_UNIT)

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const monthsConfig = [
    { field: 'april', key: 4, title: 'April' },
    { field: 'may', key: 5, title: 'May' },
    { field: 'june', key: 6, title: 'June' },
    { field: 'july', key: 7, title: 'July' },
    { field: 'aug', key: 8, title: 'August' },
    { field: 'sep', key: 9, title: 'September' },
    { field: 'oct', key: 10, title: 'October' },
    { field: 'nov', key: 11, title: 'November' },
    { field: 'dec', key: 12, title: 'December' },
    { field: 'jan', key: 1, title: 'January' },
    { field: 'feb', key: 2, title: 'February' },
    { field: 'march', key: 3, title: 'March' },
  ]

  const MONTH_FIELDS = monthsConfig.map((m) => m.field)

  const columns = [
    {
      field: 'Particulars',
      title: 'Type',
      editable: false,
      hidden: true,
      minWidth: 100,
    },
    {
      field: 'product',
      title: 'Particulars',
      editable: false,
      minWidth: 200,
    },
    ...monthsConfig.map((m) => ({
      field: m.field,
      title: headerMap[m.key] || m.title,
      editable: false,
      type: 'numberNonGrey',
      format: customFormat,
      minWidth: 110,
    })),
    {
      field: 'total',
      title: 'Total',
      editable: false,
      type: 'number1',
      format: valueFormat,
      minWidth: 110,
    },
  ]

  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      const response = await BusinessDemandApiService.getLastFYNetProduction(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const data = response?.data || []
      if (data && Array.isArray(data)) {
        const MONTH_FIELDS = monthsConfig.map((m) => m.field)
        const formattedData = data.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: index,
          originalRemark: item.remark,
          inEdit: false,
          april: item?.apr,
          june: item?.jun,
          july: item?.jul,
          march: item?.mar,
          Particulars: item.normParameterTypeDisplayName,
          total: MONTH_FIELDS.reduce(
            (sum, f) => sum + (Number(item[f]) || 0),
            0,
          ),
        }))

        // Vertical totals row (column-wise sum) — always built from raw TPM
        const totals = {
          id: '__totals__',
          product: 'Total',
          isFooter: true,
          isEditable: false,
        }
        MONTH_FIELDS.forEach((f) => {
          totals[f] = formattedData.reduce(
            (sum, row) => sum + (Number(row[f]) || 0),
            0,
          )
        })
        totals.total = MONTH_FIELDS.reduce(
          (sum, f) => sum + (totals[f] || 0),
          0,
        )

        const allRows = [...formattedData, totals]

        // Convert to the selected unit once — stored directly in rows state
        // When selectedUnit changes this function re-runs via the useEffect below
        setRows(convertRows(allRows, selectedUnit, AOP_YEAR))
        setModifiedCells({})
      } else {
        setRows([])
      }
    } catch (error) {
      console.error('Error fetching business demand data:', error)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR, selectedUnit])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })
    try {
      await BusinessDemandApiService.exportBusinessDemand(
        keycloak,
        PLANT_ID,
        previousFYFormatted,
        EXCEL_NAME,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Excel downloaded successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting business demand excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    }
  }

  const handleExcelUpload = async (rawFile) => {
    setLoading(true)
    try {
      const response = await BusinessDemandApiService.importBusinessDemand(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        rawFile,
      )
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
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
        link.setAttribute('download', 'Error File - Business Demand.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error importing business demand excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = useCallback(
    (row) => {
      if (READ_ONLY) return
      setCurrentRemark(row.remark || '')
      setCurrentRowId(row.id)
      setRemarkDialogOpen(true)
    },
    [READ_ONLY],
  )

  // Real-time recalculation of row total and footer totals when a month cell is edited
  const customItemChange = useCallback(
    (e, setRowsFn) => {
      const { dataItem, field, value } = e
      if (!MONTH_FIELDS.includes(field)) return

      setRowsFn((prev) => {
        // 1. Recalculate the edited row's horizontal total
        const updated = prev.map((r) => {
          if (r.id === dataItem.id) {
            const newTotal = MONTH_FIELDS.reduce((sum, f) => {
              const v = f === field ? value : r[f]
              return sum + (Number(v) || 0)
            }, 0)
            return { ...r, [field]: value, total: newTotal }
          }
          return r
        })

        // 2. Recalculate the footer totals row
        const dataRows = updated.filter((r) => r.id !== '__totals__')
        const newTotals = { ...updated.find((r) => r.id === '__totals__') }
        MONTH_FIELDS.forEach((f) => {
          newTotals[f] = dataRows.reduce(
            (sum, r) => sum + (Number(r[f]) || 0),
            0,
          )
        })
        newTotals.total = MONTH_FIELDS.reduce(
          (sum, f) => sum + (newTotals[f] || 0),
          0,
        )

        return updated.map((r) => (r.id === '__totals__' ? newTotals : r))
      })
    },
    [MONTH_FIELDS],
  )

  const dropdownConfig = {
    options: UNIT_OPTIONS,
    label: 'Unit',
    placeholder: 'Select Unit',
    valueKey: 'id',
    labelKey: 'name',
  }

  const permissions = {
    showAction: false,
    addButton: false,
    deleteButton: false,
    editButton: false,
    showUnit: false,
    saveBtn: false,
    showCalculate: false,
    allAction: true,
    showDropdown: false,
    downloadExcelBtnFromUI: true,
    showImport: false,
    showTitleNameBusiness: true,
    ExcelName: EXCEL_NAME,
    showTitle: true,
    titleName: `Last Financial ${previousFYFormatted} (TPM)`,
    isTotalFooterActive: true,
  }

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        title={`Last Financial ${previousFYFormatted} (TPM)`}
        loading={loading}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        permissions={permissions}
        handleExport={handleExport}
        handleExcelUpload={handleExcelUpload}
        dropdownConfig={dropdownConfig}
        selectedDropdownValue={selectedUnit}
        setSelectedDropdownValue={setSelectedUnit}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        snackbarData={snackbarData}
        setSnackbarData={setSnackbarData}
        paginationConfig={{
          threshold: 100,
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 100,
        }}
      />
    </Box>
  )
}

export default LastFinacialYearGrid
