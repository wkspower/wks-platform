import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import AdvanceKendoTable from 'components/phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'services/phase-two-services/TCS/tcsApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/phase-two/common/ValueFormatterPhaseTwo'

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
    } else {
      setRows([])
      setApiMetadata({ headers: [], keys: [] })
    }
  }, [tableData])

  // Refresh function for this grid
  const fetchGridData = useCallback(() => {
    if (onRefresh) {
      onRefresh()
    }
  }, [onRefresh])

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
    return keys.map((key) => {
      const isRemarkField = key === 'remarks' || key === 'reasons'
      const col = {
        field: key,
        title: columnMap[key] || key,
        editable: [
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
        hidden: ['id', 'type'].includes(key),
        locked: ['property', 'stream', 'unit', 'crude'].includes(key),
      }

      // Add min/max validation for maxBlendLimit
      if (key === 'maxBlendLimit') {
        col.minValue = 0
        col.maxValue = 100
      }

      return col
    })
  }, [apiMetadata])

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

      const payload = {
        tableKey: tableKey,
        data: { results: data },
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
    setSnackbarData,
    setSnackbarOpen,
    onRefresh,
  ])

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    addButton: false,
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
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          permissions={permissions}
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
