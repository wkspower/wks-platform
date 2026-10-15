import { useEffect, useState, useMemo, useCallback } from 'react'
import { Box, IconButton, Tooltip } from '@mui/material'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { FuelAvailabilityAPIService } from 'components/aop-phase-two/services/cpp/jmd/fuelAvailabilityAPIService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import DeleteDialog from 'components/aop-phase-two/common/AdvanceKendoTable/components/DeleteDialog'
import { useDebounce } from 'hooks/useDebounce'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'

// Type used to filter the Fuel Availability rows from the
// CPPFuelAvailabilityTransaction table (shared with NCV).
const DATA_TYPE = 'FUEL_AVAILABILITY'

const FuelAvailability = ({
  allFuels = [],
  allCategories = [],
  categoryOptions = [],
}) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, year, jmdSelectedPlants } = dataGridStore
  const PLANT_ID = plantObject?.id
  const IS_JMD = siteObject?.name?.toLowerCase() == 'jmd'
  const AOP_YEAR = year?.selectedYear
  const EXCEL_NAME = generateExcelName(dataGridStore, 'Fuel_Availability')

  // For JMD plants we send the full list of selected plants; for non-JMD
  // we send only the currently selected plant.
  const PLANT_ID_LIST = useMemo(
    () =>
      IS_JMD
        ? jmdSelectedPlants?.map((plant) => plant.id) ?? []
        : PLANT_ID
          ? [PLANT_ID]
          : [],
    [plantObject, jmdSelectedPlants, siteObject],
  )

  // Plant options for the inline add row's CPP Plant dropdown
  const plantOptions = useMemo(
    () =>
      (IS_JMD ? jmdSelectedPlants : PLANT_ID ? [plantObject] : [])?.map(
        (plant) => ({
          value: plant.id,
          label: plant.name || plant.id,
        }),
      ) || [],
    [IS_JMD, jmdSelectedPlants, plantObject, PLANT_ID],
  )

  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterPhaseTwo()

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [rowToDelete, setRowToDelete] = useState(null)

  // Fuel dropdown options filtered by the selected category for a given row.
  // For rows with no category, show all fuels.
  const getFuelOptionsForRow = useCallback(
    (dataItem) => {
      const selectedCategoryId = dataItem?.categoryId
      if (!selectedCategoryId) return []
      return allFuels
        .filter((f) => f.categoryFkId === selectedCategoryId)
        .map((f) => ({
          value: f.id,
          label: f.fuelDisplayName || f.fuelName || f.id,
        }))
    },
    [allFuels],
  )

  const MONTH_TO_INDEX = {
    apr: 4,
    may: 5,
    jun: 6,
    jul: 7,
    aug: 8,
    sep: 9,
    oct: 10,
    nov: 11,
    dec: 12,
    jan: 1,
    feb: 2,
    mar: 3,
  }

  const MONTH_FIELDS = [
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

  const monthBaseColumnConfig = {
    editable: true,
    widthT: 100,
    minWidth: 100,
    align: 'left',
    headerAlign: 'left',
    type: 'number1',
    format: valueFormat,
  }

  const MONTH_COLUMNS = MONTH_FIELDS.map((mon) => ({
    ...monthBaseColumnConfig,
    field: mon,
    title: headerMap[MONTH_TO_INDEX[mon]],
  }))

  const ActionCell = ({ dataItem, tdProps }) => (
    <td
      {...tdProps}
      style={{
        ...tdProps?.style,
        textAlign: 'center',
        verticalAlign: 'middle',
      }}
    >
      <Box sx={{ display: 'flex', justifyContent: 'center' }}>
        <Tooltip title='Delete Row'>
          <span>
            <IconButton
              size='medium'
              color='error'
              onClick={() => {
                setRowToDelete(dataItem)
                setDeleteDialogOpen(true)
              }}
            >
              <DeleteOutlineIcon fontSize='medium' />
            </IconButton>
          </span>
        </Tooltip>
      </Box>
    </td>
  )

  const columns = [
    {
      field: 'cppPlantName',
      title: 'CPP Plant',
      type: 'select',
      options: plantOptions,
      displayMode: 'label',
      returnFullObject: true,
      editable: true,
      locked: false,
      minWidth: 120,
    },
    {
      field: 'categoryDisplayName',
      title: 'Fuel Category',
      type: 'select',
      options: categoryOptions,
      displayMode: 'label',
      returnFullObject: true,
      editable: true,
      locked: true,
      minWidth: 150,
    },
    {
      field: 'fuelDisplayName',
      title: 'Fuel',
      type: 'select',
      dynamicOptions: true,
      getOptions: getFuelOptionsForRow,
      displayMode: 'label',
      returnFullObject: true,
      editable: true,
      locked: true,
      minWidth: 200,
    },
    {
      field: 'fuelCode',
      title: 'Fuel Code',
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 120,
    },
    {
      field: 'uom',
      title: 'UOM',
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    // {
    //   field: 'fuelDescription',
    //   title: 'Description',
    //   type: 'text',
    //   editable: false,
    //   locked: false,
    //   minWidth: 200,
    // },
    ...MONTH_COLUMNS,
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
    {
      field: 'customActions',
      title: 'Action',
      type: 'customAction',
      minWidth: 100,
      className: 'k-text-center',
      cell: ActionCell,
      locked: true,
      lockPosition: 'right',
    },
  ]

  // Fetch fuel availability transaction data for the grid
  const fetchData = useCallback(async () => {
    if (!PLANT_ID_LIST.length || !AOP_YEAR) return
    setLoading(true)
    try {
      const response = await FuelAvailabilityAPIService.getFuelAvailability(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        DATA_TYPE,
      )
      const data = response?.data || []

      if (!data || data.length === 0) {
        setRows([])
        setOriginalRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      // Transaction rows don't carry the description — resolve it from
      // the fuel master (allFuels) by fuelId.
      const descByFuelId = new Map(
        allFuels.map((f) => [f.id, f.fuelDescription]),
      )
      const rowsWithId = data.map((row, index) => ({
        ...row,
        id: row.id || `row_${index}`,
        remarks: row.remarks || '',
        fuelDescription:
          row.fuelDescription ?? descByFuelId.get(row.fuelId) ?? '',
      }))
      setRows(rowsWithId)
      setOriginalRows(rowsWithId)
    } catch (error) {
      console.error('Error fetching fuel availability data:', error)
      setRows([])
      setOriginalRows([])
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID_LIST, AOP_YEAR, allFuels])

  // fuel missing from the master don't cause repeated re-renders.
  useEffect(() => {
    if (!allFuels.length) return
    const descByFuelId = new Map(allFuels.map((f) => [f.id, f.fuelDescription]))
    setRows((prev) => {
      let changed = false
      const next = prev.map((r) => {
        if (!r.fuelDescription) {
          const desc = descByFuelId.get(r.fuelId)
          if (desc) {
            changed = true
            return { ...r, fuelDescription: desc }
          }
        }
        return r
      })
      return changed ? next : prev
    })
  }, [allFuels, rows])

  useDebounce(
    () => {
      if (PLANT_ID_LIST?.length && AOP_YEAR) {
        fetchData()
      }
    },
    1000,
    [PLANT_ID_LIST, AOP_YEAR],
  )

  useEffect(() => {
    setModifiedCells({})
  }, [PLANT_ID_LIST, AOP_YEAR])

  // Custom item change handler for cascading selects.
  // With returnFullObject: true, value is the full option object { value, label }.
  // When category changes, reset fuel fields. When fuel changes, populate
  // fuelId/fuelDisplayName/categoryId/categoryDisplayName/type/uom from the
  // selected fuel option. When plant changes, populate cppPlantFkId/cppPlantName.
  const handleCustomItemChange = useCallback(
    (e, setRowsState, setModifiedCellsState) => {
      const { dataItem, field, value } = e
      if (!dataItem || !field) return

      // With returnFullObject, value is { value, label } or null/'' when cleared
      const isObject = value && typeof value === 'object'
      const selectedId = isObject ? value.value : value
      const selectedLabel = isObject ? value.label : value

      const updates = { [field]: selectedLabel }

      if (field === 'cppPlantName') {
        updates.cppPlantFkId = selectedId || ''
        updates.cppPlantName = selectedLabel || ''
      } else if (field === 'categoryDisplayName') {
        const cat = allCategories.find((c) => c.id === selectedId)
        updates.categoryId = selectedId || ''
        updates.categoryDisplayName =
          cat?.fuelDisplayName || selectedLabel || ''
        updates.categoryName = cat?.fuelName || ''
        // Reset fuel when category changes
        updates.fuelId = ''
        updates.fuelDisplayName = ''
        updates.fuelDescription = ''
      } else if (field === 'fuelDisplayName') {
        const fuel = allFuels.find((f) => f.id === selectedId)
        updates.fuelId = selectedId || ''
        updates.fuelDisplayName = fuel?.fuelDisplayName || selectedLabel || ''
        updates.fuelName = fuel?.fuelName || ''
        updates.fuelCode = fuel?.fuelCode || ''
        updates.fuelDescription = fuel?.fuelDescription || ''
        // Auto-populate UOM from the selected fuel
        if (fuel?.uom) {
          updates.uom = fuel.uom
        }
        // Auto-populate category from the selected fuel's categoryFkId
        if (fuel?.categoryFkId) {
          updates.categoryId = fuel.categoryFkId
          const cat = allCategories.find((c) => c.id === fuel.categoryFkId)
          updates.categoryDisplayName = cat?.fuelDisplayName || ''
          updates.categoryName = cat?.fuelName || ''
        }
        // Set type for new rows
        if (dataItem.isNew) {
          updates.type = DATA_TYPE
        }
      }

      // Update rows
      setRowsState((prevRows) =>
        prevRows.map((row) =>
          row.id === dataItem.id ? { ...row, ...updates } : row,
        ),
      )

      // Update modifiedCells
      setModifiedCellsState((prev) => {
        const existing = prev[dataItem.id] || {}
        return {
          ...prev,
          [dataItem.id]: {
            ...existing,
            ...updates,
            id: dataItem.id,
            inEdit: true,
          },
        }
      })
    },
    [plantOptions, allCategories, allFuels],
  )

  const permissions = {
    showAction: true,
    addButton: true,
    addBtnName: 'Add Fuel',
    editButton: false,
    saveBtn: true,
    allAction: true,
    showExport: true,
    ExcelName: EXCEL_NAME,
    showImport: true,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Fuel Availability',
  }

  const saveChanges = async () => {
    setLoading(true)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const data = modifiedData.filter((row) => row.inEdit)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    // Validate new rows: must have plant, category, fuel
    for (const row of data) {
      if (row.isNew) {
        if (!row.cppPlantFkId) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please select a CPP Plant for the new row.',
            severity: 'error',
          })
          setLoading(false)
          return
        }
        if (!row.fuelId) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Please select a Fuel for the new row.',
            severity: 'error',
          })
          setLoading(false)
          return
        }
      }
    }

    const fieldsToCheck = MONTH_FIELDS
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'fuelDisplayName',
    )

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    try {
      const payload = modifiedData.map((item) => {
        const { inEdit, isNew, isEditable, fuelDescription, ...rest } = item
        // For new rows, set id to null so backend creates the record
        const sanitized = {
          ...rest,
          id: isNew ? null : rest.id,
          type: DATA_TYPE,
          financialYear: AOP_YEAR,
        }
        // Convert empty strings to null
        Object.keys(sanitized).forEach((key) => {
          if (sanitized[key] === '' || sanitized[key] === undefined) {
            sanitized[key] = null
          }
        })
        return sanitized
      })

      await FuelAvailabilityAPIService.saveFuelAvailability(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
      fetchData()
    } catch (error) {
      console.error('Error saving fuel availability data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = async (file) => {
    if (!file) return
    setLoading(true)
    try {
      const response = await FuelAvailabilityAPIService.importFuelAvailability(
        file,
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchData()
      } else if (response?.code === 400 && response?.data) {
        try {
          const base64Data = response.data
          const binaryString = window.atob(base64Data)
          const bytes = new Uint8Array(binaryString.length)
          for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i)
          }
          const blob = new Blob([bytes], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `Fuel_Availability_Errors_${new Date().getTime()}.xlsx`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message:
              response?.message ||
              'Import failed with errors. Please check the downloaded file.',
            severity: 'error',
          })
          await fetchData()
        } catch (downloadError) {
          console.error('Error downloading error file:', downloadError)
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Import failed but could not download error file.',
            severity: 'error',
          })
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Failed to import Excel file.',
          severity: 'error',
        })
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

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await FuelAvailabilityAPIService.exportFuelAvailability(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        DATA_TYPE,
        EXCEL_NAME,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Fuel Availability data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const deleteRowData = (row) => {
    setRows((prev) => prev.filter((r) => r.id !== row.id))
    setOriginalRows((prev) => prev.filter((r) => r.id !== row.id))
    setModifiedCells((prev) => {
      const next = { ...prev }
      delete next[row.id]
      return next
    })
  }

  const handleConfirmDelete = async () => {
    if (!rowToDelete) return
    setDeleteDialogOpen(false)

    const isExistingRecord =
      typeof rowToDelete.id === 'string' && rowToDelete.id.length === 36

    if (isExistingRecord) {
      try {
        await FuelAvailabilityAPIService.deleteFuelAvailability(
          keycloak,
          rowToDelete.id,
        )
      } catch (error) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Failed to delete record from server.',
          severity: 'error',
        })
        setRowToDelete(null)
        return
      }
    }

    deleteRowData(rowToDelete)
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Record deleted successfully!',
      severity: 'success',
    })
    setRowToDelete(null)
  }

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={permissions.showTitle ? permissions.titleName : ''}
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
        // groupBy={['cppPlantName']}
        initialFieldValues={{
          cppPlantName: '',
          categoryDisplayName: '',
          fuelDisplayName: '',
          fuelCode: '',
          fuelDescription: '',
          type: DATA_TYPE,
          uom: '',
          remarks: '',
        }}
        customItemChange={handleCustomItemChange}
        paginationConfig={{
          threshold: 100,
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 100,
        }}
      />
      <DeleteDialog
        openDeleteDialogeBox={deleteDialogOpen}
        setOpenDeleteDialogeBox={setDeleteDialogOpen}
        deleteTheRecord={handleConfirmDelete}
        message='Are you sure you want to delete this Fuel Availability record?'
        confirmButtonText='Delete'
      />
    </Box>
  )
}

export default FuelAvailability
