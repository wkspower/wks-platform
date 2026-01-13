import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import AdvanceKendoTable from 'components/phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'services/phase-two-services/TCS/tcsApiService'
import { useSession } from 'SessionStoreContext'
import { convertRowFromKBPSD, convertRowToKBPSD } from './uomConversionUtils'
import ValueFormatterPhaseTwo from 'components/phase-two/common/ValueFormatterPhaseTwo'

const UnitCapacityGrid = ({
  capacityType,
  title,
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
  //   if (!PLANT_ID || !AOP_YEAR) return
  //   try {
  //     setLoadingUOM(true)
  //     const response = await TcsApiService.getTcsUnitCapacityUOM(
  //       keycloak,
  //       PLANT_ID,
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
  //   PLANT_ID,
  //   AOP_YEAR,
  //   capacityType,
  //   setSnackbarData,
  //   setSnackbarOpen,
  // ])

  // Fetch Unit Capacity data for this capacity type
  const fetchUnitCapacityData = useCallback(
    async (selectedUOM) => {
      if (!PLANT_ID || !AOP_YEAR) return
      try {
        setLoading(true)

        const response = await TcsApiService.getTcsUnitCapacityData(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          capacityType,
          selectedUOM,
        )
        // const response = {
        //   headers: [
        //     'Id',
        //     'Particulars',
        //     'UOM',
        //     'Summer',
        //     'Winter',
        //     'Remark',
        //     'InsertedDateTime',
        //   ],
        //   keys: [
        //     'id',
        //     'particulates',
        //     'uom',
        //     'summer',
        //     'winter',
        //     'remark',
        //     'insertedDateTime',
        //   ],
        //   results: [
        //     {
        //       id: '9F1897F2-BEB5-4352-A25D-B473C0219FD4',
        //       particulates: 'CDU-1',
        //       uom: 'KBPSD',
        //       summer: 345.0,
        //       winter: 345.0,
        //       remark:
        //         'Unit capacity considered for min API of 27. L+N: CDU-1: 7.4 KTPD max           CDU-2: 6.4 KTPD (Summer: March-Oct) & 7.4 KTPD max in winters (Nov-Feb). RCO: Max 24.2 KTPD VR: Max 14.5 KTPD, however HOT VR to Coker will be 13.6 KTPD max based on hydraulic limitation',
        //       insertedDateTime: 'Dec 22, 2025, 12:00:00 AM',
        //     },
        //   ],
        // }

        let transformedData = []
        if (response?.results && Array.isArray(response.results)) {
          transformedData = response.results.map((item, index) => {
            // Backend data is in KBPSD, convert to selected UOM for display
            const convertedItem = convertRowFromKBPSD(item, selectedUOM)
            return {
              id: item.id || `row_${index}`,
              ...convertedItem,
              inEdit: false,
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
      PLANT_ID,
      AOP_YEAR,
      capacityType,
      setSnackbarData,
      setSnackbarOpen,
    ],
  )

  // Fetch UOM options on component mount
  // useEffect(() => {
  //   if (PLANT_ID && AOP_YEAR) {
  //     fetchUOMOptions()
  //   }
  // }, [PLANT_ID, AOP_YEAR, fetchUOMOptions])

  // Fetch capacity data when dropdown selection changes
  useEffect(() => {
    if (PLANT_ID && AOP_YEAR && selectedDropdown) {
      // Clear modified cells when UOM changes to reset edit state
      setModifiedCells({})
      fetchUnitCapacityData(selectedDropdown)
    }
  }, [PLANT_ID, AOP_YEAR, selectedDropdown, fetchUnitCapacityData])

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
        (col) =>
          col.field !== 'id' &&
          col.field !== 'particulates' &&
          col.field !== 'insertedDateTime',
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

  // Save changes for this capacity type
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

      if (!selectedDropdown) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Please select a UOM before saving!',
          severity: 'warning',
        })
        return
      }

      // Custom validation: If any row data is updated, remarks must be filled and different from original
      const fieldsToCheck = ['summer', 'winter']
      const validationError = validateRowDataWithRemarks(
        data,
        originalRows,
        fieldsToCheck,
        'particulates',
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

      // Convert data from selected UOM back to KBPSD before sending to backend
      const dataInKBPSD = data.map((row) => {
        const convertedRow = convertRowToKBPSD(row, selectedDropdown)
        return {
          ...row,
          summer: convertedRow.summer,
          winter: convertedRow.winter,
        }
      })

      const response = await TcsApiService.saveUnitCapacityData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        capacityType,
        selectedDropdown,
        dataInKBPSD,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unit Capacity data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
    } catch (error) {
      console.error('Error saving Unit Capacity data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving Unit Capacity data!',
        severity: 'error',
      })
    }
  }, [
    modifiedCells,
    originalRows,
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    capacityType,
    selectedDropdown,
    setSnackbarData,
    setSnackbarOpen,
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
          saveChanges={saveChanges}
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
        />
      </Stack>
    </Box>
  )
}

export default UnitCapacityGrid
