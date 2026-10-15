import { useEffect, useState, useCallback, useMemo } from 'react'
import { Box, Tooltip, IconButton } from '@mui/material'
import EditIcon from '@mui/icons-material/Edit'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { InputApiService } from 'components/aop-phase-two/services/cpp/jmd/inputApiService'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { useDebounce } from 'hooks/useDebounce'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'
import DeleteDialog from 'components/aop-phase-two/common/AdvanceKendoTable/components/DeleteDialog'
import AddSourceDialog from './components/AddSourceDialog'

const ImportPowerGrid = ({ setImportData }) => {
  const keycloak = useSession()

  // State management
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
    jmdSelectedPlants,
  } = dataGridStore

  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterPhaseTwo()
  const EXCEL_NAME = generateExcelName(dataGridStore, 'Import_Power')

  // Multi-plant list — same pattern as PowerAssetAvailability
  const PLANT_ID_LIST = useMemo(
    () => jmdSelectedPlants?.map((plant) => plant.id) || [],
    [jmdSelectedPlants],
  )

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [addRowDialogOpen, setAddRowDialogOpen] = useState(false)
  // null = closed, object = row data to edit
  const [editRowData, setEditRowData] = useState(null)

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [rowToDelete, setRowToDelete] = useState(null)

  // Column definitions — field names match CPPImportPowerResponseDTO
  const columns = [
    { field: 'id', title: 'ID', hidden: true },
    {
      field: 'procurementPlant',
      title: 'Plant',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'utilityName',
      title: 'Utility Name',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'utilityCode',
      title: 'Utility Code',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'materialName',
      title: 'Material Name',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
    },
    {
      field: 'materialCode',
      title: 'Material Code',
      width: 120,
      minWidth: 120,
      type: 'text',
      editable: false,
      hidden: false,
    },
    {
      field: 'uom',
      title: 'UOM',
      width: 100,
      minWidth: 100,
      type: 'text',
      editable: false,
    },
    {
      field: 'apr',
      title: headerMap[4],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'jun',
      title: headerMap[6],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'jul',
      title: headerMap[7],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'aug',
      title: headerMap[8],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'sep',
      title: headerMap[9],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'oct',
      title: headerMap[10],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'nov',
      title: headerMap[11],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'dec',
      title: headerMap[12],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'jan',
      title: headerMap[1],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'feb',
      title: headerMap[2],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'mar',
      title: headerMap[3],
      editable: true,
      widthT: 100,
      minWidth: 100,
      align: 'left',
      headerAlign: 'left',
      type: 'number1',
      format: valueFormat,
    },
    {
      field: 'remarks',
      title: 'Remarks',
      width: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  // ── Fetch ──────────────────────────────────────────────────────────────────

  const fetchImportPowerData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await InputApiService.getImportPowerCapacity(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
      )

      let importedPowerPlans = res?.data?.importedPowerPlans

      if (!importedPowerPlans || importedPowerPlans.length === 0) {
        setRows([])
        setImportData([])
        setOriginalRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      importedPowerPlans = importedPowerPlans.map((row) => ({
        ...row,
        remarks: row.remarks || '',
      }))
      setImportData(importedPowerPlans)

      // Calculate total row
      const totalRow = buildTotalRow(importedPowerPlans)
      const finalRows = [...importedPowerPlans, totalRow]

      setRows(finalRows)
      setOriginalRows(finalRows)
    } catch (error) {
      console.error('Error fetching import power data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID_LIST, AOP_YEAR])

  useDebounce(
    () => {
      if (PLANT_ID_LIST?.length && AOP_YEAR) {
        fetchImportPowerData()
        setModifiedCells({})
      }
    },
    1000,
    [PLANT_ID_LIST, AOP_YEAR, fetchImportPowerData],
  )

  // ── Total row helpers ──────────────────────────────────────────────────────

  const monthFields = [
    'apr',
    'may',
    'jun',
    'jul',
    'aug',
    'sep',
    'oct',
    'nov',
    'dec',
    'jan',
    'feb',
    'mar',
  ]

  function buildTotalRow(dataRows) {
    const totals = {}
    monthFields.forEach((f) => {
      totals[f] = dataRows.reduce(
        (sum, row) => sum + (parseFloat(row[f]) || 0),
        0,
      )
    })
    // plantName must be present so groupBy logic can place this row correctly
    const lastRow = dataRows.length > 0 ? dataRows[dataRows.length - 1] : {}
    return {
      id: 'TOTAL_ROW',
      plantName: lastRow.plantName || '', // keep same group bucket as data rows
      procurementPlant: 'Total',
      utilityName: '',
      utilityCode: '',
      materialName: '',
      materialCode: '',
      uom: lastRow.uom || '',
      aopYear: lastRow.aopYear || '',
      ...totals,
      remarks: 'Total Import Power',
      isTotal: true,
      isEditable: false,
    }
  }

  // Custom item change — recalculates totals in real-time when a month cell changes
  const customItemChange = useCallback((event, setRowsFunc) => {
    const { field } = event
    if (!monthFields.includes(field)) return

    setRowsFunc((currentRows) => {
      const dataRows = currentRows.filter((row) => !row.isTotal)
      return [...dataRows, buildTotalRow(dataRows)]
    })
  }, [])

  // ── Edit action cell ───────────────────────────────────────────────────────

  const EditActionCell = ({ dataItem, tdProps }) => {
    // Hide edit button for the Total row
    if (dataItem?.isTotal) {
      return <td {...tdProps} />
    }
    return (
      <td
        {...tdProps}
        style={{
          ...tdProps?.style,
          textAlign: 'center',
          verticalAlign: 'middle',
        }}
      >
        <Box sx={{ display: 'flex', justifyContent: 'center' }}>
          <Tooltip title='Edit Source'>
            <IconButton
              size='small'
              onClick={() => {
                setEditRowData(dataItem)
                setAddRowDialogOpen(true)
              }}
            >
              <EditIcon fontSize='small' />
            </IconButton>
          </Tooltip>

          <Tooltip title='Delete Source'>
            <IconButton
              size='small'
              color='error'
              onClick={() => {
                setRowToDelete(dataItem)
                setDeleteDialogOpen(true)
              }}
            >
              <DeleteOutlineIcon fontSize='small' />
            </IconButton>
          </Tooltip>
        </Box>
      </td>
    )
  }

  // ── Permissions ────────────────────────────────────────────────────────────

  const permissions = {
    showAction: true,
    addButton: true,
    addBtnName: 'Add Source',
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showImport: true,
    showExport: true,
    ExcelName: `Import Power - ${AOP_YEAR}`,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: screenTitle?.title,
  }

  // ── Save ───────────────────────────────────────────────────────────────────

  const saveChanges = async () => {
    setLoading(true)

    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      setLoading(false)
      return
    }

    const data = modifiedData.filter((row) => row.inEdit)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      setLoading(false)
      return
    }

    // Validate remarks
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      monthFields,
      'materialName',
    )
    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({ message: validationError, severity: 'error' })
      setLoading(false)
      return
    }

    // Strip UI-only fields before sending
    const payload = data.map(({ inEdit, isTotal, isEditable, ...rest }) => rest)

    try {
      await InputApiService.saveImportPowerCapacity(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${data.length} row(s)!`,
        severity: 'success',
      })
      fetchImportPowerData()
    } catch (error) {
      console.error('Error saving import power data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  // ── Excel Import ───────────────────────────────────────────────────────────

  const handleExcelUpload = async (file) => {
    if (!file) return
    setLoading(true)
    try {
      const response = await InputApiService.saveImportPowerCapacityExcel(
        file,
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Excel file imported successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchImportPowerData()
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
        link.setAttribute('download', `Error File - Import Power.xlsx`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        await fetchImportPowerData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Upload Failed!', severity: 'error' })
      }
    } catch (error) {
      console.error('Error uploading Excel file:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Failed to import Excel file: ${error.message}`,
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  // ── Excel Export ───────────────────────────────────────────────────────────

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({ message: 'Excel download started!', severity: 'info' })
    try {
      await InputApiService.exportImportPowerCapacityExcel(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        EXCEL_NAME,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Import Power data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  // ── Remark dialog ──────────────────────────────────────────────────────────

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // ── Delete ─────────────────────────────────────────────────────────────────

  const handleConfirmDelete = async () => {
    if (!rowToDelete) return

    setDeleteDialogOpen(false)
    setLoading(true)

    try {
      await InputApiService.deleteSource(
        keycloak,
        rowToDelete.normParameterFkId,
        rowToDelete.importPlantFkId,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Source deleted successfully!',
        severity: 'success',
      })
      fetchImportPowerData()
    } catch (error) {
      console.error('Error deleting source:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to delete source. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
      setRowToDelete(null)
    }
  }

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Import Power'
        permissions={permissions}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        customItemChange={customItemChange}
        groupBy={['plantName']}
        customAddRow={() => {
          setEditRowData(null)
          setAddRowDialogOpen(true)
        }}
        customActionCell={EditActionCell}
      />

      <AddSourceDialog
        open={addRowDialogOpen}
        onClose={() => {
          setAddRowDialogOpen(false)
          setEditRowData(null)
        }}
        onSuccess={fetchImportPowerData}
        editRowData={editRowData}
      />

      <DeleteDialog
        openDeleteDialogeBox={deleteDialogOpen}
        setOpenDeleteDialogeBox={setDeleteDialogOpen}
        deleteTheRecord={handleConfirmDelete}
        message='Are you sure you want to delete this source?'
        confirmButtonText='Delete'
      />
    </Box>
  )
}

export default ImportPowerGrid
