import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import AdvanceKendoTable from 'components/phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsOutputApiService } from 'services/phase-two-services/TCS/tcsOutputApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/phase-two/common/ValueFormatterPhaseTwo'
import { convertRowFromKBPSD } from './uomConversionUtils'

const UnitCapacityGrid = ({
  capacityType,
  title,
  SITE_ID,
  VERTICAL_ID,
  PLANT_ID,
  AOP_YEAR,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
}) => {
  const keycloak = useSession()
  const valueFormat = ValueFormatterPhaseTwo()

  const defaultDropdownConfig = {
    options: [
      { id: 'KBPSD', name: 'KBPSD' },
      { id: 'KTPD', name: 'KTPD' },
      { id: 'TPD', name: 'TPD' },
    ],
    label: 'Select UOM',
    placeholder: 'Select',
    valueKey: 'id',
    labelKey: 'name',
  }

  // State management for this capacity type only
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [selectedDropdown, setSelectedDropdown] = useState('KBPSD')
  const [dropdownConfig, setDropdownConfig] = useState({
    ...defaultDropdownConfig,
  })
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [loadingUOM, setLoadingUOM] = useState(false)
  const [apiMetadata, setApiMetadata] = useState({ headers: [], keys: [] })

  // Fetch UOM options for this capacity type
  // const fetchUOMOptions = useCallback(async () => {
  //   if (!VERTICAL_ID || !AOP_YEAR) return
  //   try {
  //     setLoadingUOM(true)
  //     const response = await TcsOutputApiService.getTcsUnitCapacityUOM(
  //       keycloak,
  //       VERTICAL_ID,
  //       AOP_YEAR,
  //       capacityType,
  //     )

  //     const uomOptions = response || response?.data || []
  //     if (uomOptions.length === 0) return

  //     setDropdownConfig((prev) => ({
  //       ...prev,
  //       options: uomOptions,
  //     }))

  //     const defaultUOM = uomOptions[0].id
  //     setSelectedDropdown(defaultUOM)
  //   } catch (err) {
  //     console.error(`Error fetching UOM options (${capacityType}):`, err)
  //     setSnackbarData({
  //       message: `Failed to load UOM options. Please try again.`,
  //       severity: 'error',
  //     })
  //     setSnackbarOpen(true)
  //   } finally {
  //     setLoadingUOM(false)
  //   }
  // }, [
  //   keycloak,
  //   VERTICAL_ID,
  //   AOP_YEAR,
  //   capacityType,
  //   setSnackbarData,
  //   setSnackbarOpen,
  // ])

  // Fetch Unit Capacity data for this capacity type
  const fetchUnitCapacityData = useCallback(
    async (selectedUOM) => {
      if (!SITE_ID || !VERTICAL_ID || !AOP_YEAR) return
      try {
        setLoading(true)

        const response = await TcsOutputApiService.getTcsUnitCapacityData(
          keycloak,
          SITE_ID,
          VERTICAL_ID,
          AOP_YEAR,
          capacityType,
        )

        let transformedData = []
        if (response?.results && Array.isArray(response.results)) {
          transformedData = response.results.map((item, index) => {
            // Backend data is in KBPSD, convert to selected UOM for display
            const convertedItem = convertRowFromKBPSD(item, selectedUOM)
            return {
              id: item.id || `row_${index}`,
              ...convertedItem,
              inEdit: false,
              isEditable: false,
            }
          })
        }

        if (response?.headers && response?.keys) {
          setApiMetadata({ headers: response.headers, keys: response.keys })
        }

        setRows(transformedData)
        setOriginalRows(transformedData)
      } catch (err) {
        console.error(
          `Error fetching Unit Capacity data (${capacityType}):`,
          err,
        )
        setSnackbarData({
          message: `Failed to load Unit Capacity data. Please try again.`,
          severity: 'error',
        })
        setSnackbarOpen(true)
        setRows([])
      } finally {
        setLoading(false)
      }
    },
    [
      keycloak,
      SITE_ID,
      VERTICAL_ID,
      AOP_YEAR,
      capacityType,
      setSnackbarData,
      setSnackbarOpen,
    ],
  )

  // Fetch UOM options on component mount
  // useEffect(() => {
  //   if (VERTICAL_ID && AOP_YEAR) {
  //     fetchUOMOptions()
  //   }
  // }, [VERTICAL_ID, AOP_YEAR, fetchUOMOptions])

  // Fetch capacity data when dropdown selection changes
  useEffect(() => {
    if (SITE_ID && VERTICAL_ID && AOP_YEAR && selectedDropdown) {
      fetchUnitCapacityData(selectedDropdown)
    }
  }, [SITE_ID, VERTICAL_ID, AOP_YEAR, selectedDropdown, fetchUnitCapacityData])

  // Column configuration for Unit Capacity
  const columnConfig = {
    id: {
      editable: false,
      type: 'text',
      minWidth: 50,
      widthT: 100,
      hidden: true,
    },
    particulates: { editable: false, type: 'text', minWidth: 50, widthT: 150 },
    summer: {
      editable: true,
      type: 'number1',
      minWidth: 50,
      widthT: 120,
      format: valueFormat,
    },
    winter: {
      editable: true,
      type: 'number1',
      minWidth: 50,
      widthT: 120,
      format: valueFormat,
    },
    remark: { editable: true, type: 'text', minWidth: 100, widthT: 250 },
  }

  const columns = useMemo(() => {
    const { headers, keys } = apiMetadata

    if (!headers || !keys || headers.length === 0) {
      return []
    }

    // Map keys to their headers from backend
    const columnMap = {}
    headers.forEach((header, index) => {
      columnMap[keys[index]] = header
    })

    // Build columns using columnConfig for type/formatting
    const cols = Object.entries(columnConfig).map(([key, config]) => ({
      field: key,
      title: columnMap[key] || key,
      ...config,
    }))

    // Group 'summer' and 'winter' columns under 'Capacity' parent
    const summerCol = cols.find((col) => col.field === 'summer')
    const winterCol = cols.find((col) => col.field === 'winter')
    const otherCols = cols.filter(
      (col) => col.field !== 'summer' && col.field !== 'winter',
    )

    if (summerCol && winterCol) {
      const result = []
      // Position 0: id
      result.push(otherCols.find((col) => col.field === 'id'))
      // Position 1: particulates
      result.push(otherCols.find((col) => col.field === 'particulates'))
      // Position 2: Capacity (with summer and winter)
      result.push({
        title: 'Capacity',
        children: [summerCol, winterCol],
      })
      // Position 3: remark and other remaining columns
      const remainingCols = otherCols.filter(
        (col) => col.field !== 'id' && col.field !== 'particulates',
      )
      result.push(...remainingCols)
      return result
    }

    return cols
  }, [apiMetadata])

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    showExport: true,
    showTitle: true,
    showDropdown: true,
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Stack sx={{ mt: 2 }}>
        <AdvanceKendoTable
          rows={rows}
          setRows={setRows}
          fetchData={() => fetchUnitCapacityData(selectedDropdown)}
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
          dropdownConfig={dropdownConfig}
          selectedDropdownValue={selectedDropdown}
          setSelectedDropdownValue={setSelectedDropdown}
          readonly={true}
        />
      </Stack>
    </Box>
  )
}

export default UnitCapacityGrid
