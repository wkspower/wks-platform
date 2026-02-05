import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsOutputApiService } from 'components/aop-phase-two/services/tcs/tcsOutputApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { ROLES } from '../../utils/roleUtils'

const CrudBlendWindowGrid = ({
  tableKey,
  title,
  AOP_YEAR,
  SITE_ID,
  tableData,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
  onRefresh,
  userRole,
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
        isEditable: false,
      }))
      console.log('transformedData', transformedData)
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

  const permissions = useMemo(
    () => ({
      customHeight: { mainBox: '32vh', otherBox: '100%' },
      textAlignment: 'center',
      allAction: true,
      showExport: true,
      showTitle: true,
      filterable: false,
      approveBtn: userRole === ROLES.EPS_ENGINEER,
    }),
    [userRole],
  )

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
          {...(tableKey === 'CrudeBlendWindow' && { labelField: 'property' })}
          readonly={true}
        />
      </Stack>
    </Box>
  )
}

export default CrudBlendWindowGrid
