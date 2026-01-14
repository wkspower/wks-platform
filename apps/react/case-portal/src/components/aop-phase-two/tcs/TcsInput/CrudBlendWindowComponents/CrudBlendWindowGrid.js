import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'components/aop-phase-two/services/tcs/tcsApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'

const CrudBlendWindowGrid = ({
  tableKey,
  title,
  PLANT_ID,
  AOP_YEAR,
  SITE_ID,
  tableData,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
  onRefresh,
}) => {
  const keycloak = useSession()
  const valueFormat = ValueFormatterPhaseTwo()

  // State management for this grid
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [apiMetadata, setApiMetadata] = useState({ headers: [], keys: [] })
  const [selectedGroupType, setSelectedGroupType] = useState('')

  // Process table data when it's provided
  useEffect(() => {
    if (tableData && tableData.headers && tableData.keys) {
      const { headers, keys, results } = tableData

      const transformedData = (results || []).map((item, index) => ({
        id: item.id || `row_${index}`,
        ...item,
        inEdit: false,
      }))

      setApiMetadata({ headers, keys })
      setRows(transformedData)
      setOriginalRows(transformedData)

      // Set default group type for CrudeBlendWindow
      if (tableKey === 'CrudeBlendWindow' && results && results.length > 0) {
        const firstType = results[0]?.type
        if (firstType && !selectedGroupType) {
          setSelectedGroupType(firstType)
        }
      }
    } else {
      setRows([])
      setApiMetadata({ headers: [], keys: [] })
    }
  }, [tableData, tableKey])

  // Refresh function for this grid
  const fetchGridData = useCallback(() => {
    if (onRefresh) {
      onRefresh()
    }
  }, [onRefresh])

  // Get unique group types for CrudeBlendWindow
  const groupTypes = useMemo(() => {
    if (tableKey !== 'CrudeBlendWindow') return []
    const types = [...new Set(rows.map((row) => row.type).filter(Boolean))]
    return types
  }, [rows, tableKey])

  // Build columns dynamically from API metadata
  const columns = useMemo(() => {
    const { headers, keys } = apiMetadata

    if (!headers || !keys || headers.length === 0) {
      return []
    }

    // Map keys to their headers
    const columnMap = {}
    headers.forEach((header, index) => {
      columnMap[keys[index]] = header
    })
    // Build columns with editable configuration
    const cols = keys.map((key) => {
      const isRemarkField = key === 'remarks' || key === 'reasons'
      const col = {
        field: key,
        title: columnMap[key] || key,
        editable: [
          'property',
          'type',
          'minValue',
          'maxValue',
          'criticality',
          'remarks',
          'value_345',
          'maxBlendLimit',
          'reasons',
        ].includes(key)
          ? true
          : false,
        type: [
          'minValue',
          'maxValue',
          'criticality',
          'maxBlendLimit',
          'value_345',
        ].includes(key)
          ? 'number1'
          : 'text',
        minWidth: isRemarkField ? 350 : 150,
        widthT: isRemarkField ? 450 : 250,
        hidden: ['id'].includes(key),
        locked: ['stream', 'unit', 'crude'].includes(key),
      }

      // Configure 'type' as dropdown field
      if (key === 'type') {
        col.type = 'select'
        col.options = groupTypes.map((type) => ({ value: type, label: type }))
      }

      // Add min/max validation for maxBlendLimit
      if (key === 'maxBlendLimit') {
        col.minValue = 0
        col.maxValue = 100
      }

      return col
    })

    // Reorder columns: id, type, then rest
    const idCol = cols.find((col) => col.field === 'id')
    const typeCol = cols.find((col) => col.field === 'type')
    const otherCols = cols.filter(
      (col) => col.field !== 'id' && col.field !== 'type',
    )

    return [idCol, typeCol, ...otherCols].filter(Boolean)
  }, [apiMetadata, groupTypes])

  console.log('columns', columns)

  // Handle remark cell click
  const handleRemarkCellClick = useCallback(
    (row) => {
      // Open dialog for both 'remarks' and 'reasons' fields
      const remarkField = columns.find(
        (col) => col.field === 'remarks' || col.field === 'reasons',
      )

      if (remarkField) {
        const fieldName = remarkField.field
        setCurrentRemark(row[fieldName] || '')
        setCurrentRowId(row.id)
        setRemarkDialogOpen(true)
      }
    },
    [columns, tableKey],
  )

  // Reset inEdit flags when modifiedCells is cleared
  useEffect(() => {
    if (Object.keys(modifiedCells).length === 0) {
      setRows((prev) =>
        prev.map((row) => ({
          ...row,
          inEdit: false,
        })),
      )
    }
  }, [modifiedCells])

  // Save changes for this grid
  const saveChanges = useCallback(async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      // Custom validation: If any row data is updated, remarks/reasons must be filled and different from original
      const fieldsToCheck = [
        'minValue',
        'maxValue',
        'criticality',
        'maxBlendLimit',
        'value_345',
      ]
      const remarksFieldName = apiMetadata.keys.includes('remarks')
        ? 'remarks'
        : 'reasons'
      const displayFieldName =
        tableKey === 'CrudeBlendWindow'
          ? 'property'
          : tableKey === 'CrudeSpecificConstraints'
            ? 'crude'
            : 'kbpsd'

      const validationError = validateRowDataWithRemarks(
        data,
        originalRows,
        fieldsToCheck,
        displayFieldName,
        remarksFieldName,
      )

      if (validationError) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationError,
          severity: 'error',
        })
        return
      }

      // Set id to null for new items
      const formattedData = data.map((item) => {
        if (item.isNew) {
          return { ...item, id: null }
        }
        return item
      })

      const payload = {
        tableKey: tableKey,
        data: { results: formattedData },
      }

      const response = await TcsApiService.saveCrudBlendWindowData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        SITE_ID,
        payload,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: `${title} data saved successfully!`,
        severity: 'success',
      })
      setModifiedCells({})
      // Refresh all tables data from parent
      if (onRefresh) {
        onRefresh()
      }
    } catch (error) {
      console.error(`Error saving ${tableKey} data:`, error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Error saving ${title} data!`,
        severity: 'error',
      })
    }
  }, [
    modifiedCells,
    originalRows,
    apiMetadata,
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    SITE_ID,
    title,
    tableKey,
    setSnackbarData,
    setSnackbarOpen,
    onRefresh,
  ])

  // Delete row data
  const deleteRowData = useCallback(
    async (paramsForDelete) => {
      try {
        const { id } = paramsForDelete
        const deleteId = id

        // Check if this is a newly added row (not saved to backend yet)
        const isNewRow = paramsForDelete.isNew

        if (isNewRow) {
          // Just remove from local state
          setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Row removed successfully!',
            severity: 'success',
          })
        } else {
          // Call API to delete from backend
          setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Record deleted successfully!',
            severity: 'success',
          })
          if (onRefresh) {
            onRefresh()
          }
        }
      } catch (error) {
        console.error('Error deleting record:', error)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error deleting record!',
          severity: 'error',
        })
      }
    },
    [onRefresh, setSnackbarData, setSnackbarOpen],
  )

  // Dropdown config for group type selection (only for CrudeBlendWindow)
  const groupTypeDropdownConfig = useMemo(() => {
    if (tableKey !== 'CrudeBlendWindow' || groupTypes.length === 0) return null

    return {
      options: groupTypes.map((type) => ({ id: type, name: type })),
      label: 'Select Type',
      placeholder: 'Select group type',
      valueKey: 'id',
      labelKey: 'name',
    }
  }, [groupTypes, tableKey])

  // Initial field values for new rows
  const initialFieldValues = useMemo(() => {
    if (tableKey === 'CrudeBlendWindow') {
      return {
        type: selectedGroupType || (groupTypes.length > 0 ? groupTypes[0] : ''),
        property: '',
        stream: '',
        unit: '',
      }
    }
    return {}
  }, [tableKey, selectedGroupType, groupTypes])

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    addButton: true,
    deleteButton: true,
    showAction: true,
    remarksEditable: true,
    showCalculate: false,
    showExport: false,
    showImport: false,
    saveBtnForRemark: true,
    saveBtn: true,
    showWorkFlowBtns: false,
    showTitle: true,
    filterable: false,
  }

  return (
    <Box>
      <Stack sx={{ mt: 2 }}>
        <AdvanceKendoTable
          rows={rows}
          setRows={setRows}
          fetchData={fetchGridData}
          title={title}
          handleRemarkCellClick={handleRemarkCellClick}
          columns={columns}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={() => {}}
          saveChanges={saveChanges}
          deleteRowData={deleteRowData}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          permissions={permissions}
          initialFieldValues={initialFieldValues}
          {...(groupTypeDropdownConfig && {
            dropdownConfig: groupTypeDropdownConfig,
            selectedDropdownValue: selectedGroupType,
            setSelectedDropdownValue: setSelectedGroupType,
          })}
          paginationConfig={{
            threshold: 100,
            defaultPageSize: 50,
            pageSizes: [10, 20, 50, 100],
          }}
          {...(tableKey === 'CrudeBlendWindow' && { groupBy: 'type' })}
        />
      </Stack>
    </Box>
  )
}

export default CrudBlendWindowGrid
