import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'components/aop-phase-two/services/tcs/tcsApiService'
import { useSession } from 'SessionStoreContext'
import {
  convertFromKBPSD,
  convertToKBPSD,
} from './UnitCapacityComponents/uomConversionUtils'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'

const NetUnitCapacity = ({
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
  const headerMap = generateHeaderNames(AOP_YEAR)

  // State management for this capacity type only
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [selectedDropdown, setSelectedDropdown] = useState('KBPSD')
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [apiMetadata, setApiMetadata] = useState({ headers: [], keys: [] })

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
        )

        // Add 2 dummy records to the API response
        if (response?.results && Array.isArray(response.results)) {
          response.results.push(
            {
              id: 'dummy_1',
              particulates: 'Dummy Unit 1',
              april: 100,
              may: 105,
              june: 110,
              july: 115,
              aug: 120,
              sep: 125,
              oct: 130,
              nov: 135,
              dec: 140,
              jan: 145,
              feb: 150,
              mar: 155,
              remark: 'This is a dummy record for testing',
              insertedDateTime: new Date().toISOString(),
            },
            {
              id: 'dummy_2',
              particulates: 'Dummy Unit 2',
              april: 200,
              may: 210,
              june: 220,
              july: 230,
              aug: 240,
              sep: 250,
              oct: 260,
              nov: 270,
              dec: 280,
              jan: 290,
              feb: 300,
              mar: 310,
              remark: 'This is another dummy record for testing',
              insertedDateTime: new Date().toISOString(),
            },
          )
        }

        let transformedData = []
        if (response?.results && Array.isArray(response.results)) {
          transformedData = response.results.map((item, index) => {
            // Backend data is in KBPSD, create nested structure for each month with both KBPSD and KTPD
            const months = [
              'april',
              'may',
              'june',
              'july',
              'aug',
              'sep',
              'oct',
              'nov',
              'dec',
              'jan',
              'feb',
              'mar',
            ]
            const monthData = {}

            months.forEach((month) => {
              const kbpsdValue = item[month] || 0
              monthData[month] = {
                kbpsd: kbpsdValue,
                ktpd: convertFromKBPSD(kbpsdValue, 'KTPD'),
              }
            })

            return {
              id: item.id || `row_${index}`,
              particulates: item.particulates,
              ...monthData,
              remark: item.remark,
              insertedDateTime: item.insertedDateTime,
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
        console.error(`Error fetching Net Unit Capacity data:`, err)
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
    [keycloak, PLANT_ID, AOP_YEAR, setSnackbarData, setSnackbarOpen],
  )

  // Fetch capacity data when dropdown selection changes
  useEffect(() => {
    if (PLANT_ID && AOP_YEAR && selectedDropdown) {
      // Clear modified cells when UOM changes to reset edit state
      setModifiedCells({})
      fetchUnitCapacityData(selectedDropdown)
    }
  }, [PLANT_ID, AOP_YEAR, selectedDropdown, fetchUnitCapacityData])

  // Column configuration for Unit Capacity with monthly nested KBPSD and KTPD
  const columnConfig = useMemo(() => {
    const config = {
      id: {
        editable: false,
        type: 'text',
        minWidth: 50,
        widthT: 100,
        hidden: true,
      },
      particulates: {
        editable: false,
        type: 'text',
        minWidth: 150,
        widthT: 150,
      },
    }

    // Add monthly columns with KBPSD and KTPD sub-columns
    const months = [
      'april',
      'may',
      'june',
      'july',
      'aug',
      'sep',
      'oct',
      'nov',
      'dec',
      'jan',
      'feb',
      'mar',
    ]
    months.forEach((month) => {
      config[`${month}.kbpsd`] = {
        editable: false,
        type: 'number1',
        minWidth: 80,
        widthT: 100,
        format: valueFormat,
        title: 'KBPSD',
      }
      config[`${month}.ktpd`] = {
        editable: false,
        type: 'number1',
        minWidth: 80,
        widthT: 100,
        format: valueFormat,
        title: 'KTPD',
      }
    })

    config.remark = {
      title: 'Remark',
      editable: false,
      type: 'text',
      minWidth: 200,
      widthT: 250,
    }

    return config
  }, [valueFormat])

  const columns = useMemo(() => {
    const { headers, keys } = apiMetadata

    if (!headers || !keys || headers.length === 0 || !headerMap) {
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
      title: config.title || columnMap[key] || key,
      ...config,
    }))

    // Group monthly columns with KBPSD and KTPD sub-columns
    const months = [
      { key: 'april', headerKey: 4 },
      { key: 'may', headerKey: 5 },
      { key: 'june', headerKey: 6 },
      { key: 'july', headerKey: 7 },
      { key: 'aug', headerKey: 8 },
      { key: 'sep', headerKey: 9 },
      { key: 'oct', headerKey: 10 },
      { key: 'nov', headerKey: 11 },
      { key: 'dec', headerKey: 12 },
      { key: 'jan', headerKey: 1 },
      { key: 'feb', headerKey: 2 },
      { key: 'mar', headerKey: 3 },
    ]

    const otherCols = cols.filter(
      (col) => !months.some((m) => col.field.startsWith(`${m.key}.`)),
    )

    const result = []
    // Position 0: id
    result.push(otherCols.find((col) => col.field === 'id'))
    // Position 1: particulates
    result.push(otherCols.find((col) => col.field === 'particulates'))

    // Position 2: Capacity with monthly columns (Apr to Mar)
    const monthlyColumns = months
      .map((month) => {
        const kbpsdCol = cols.find((col) => col.field === `${month.key}.kbpsd`)
        const ktpdCol = cols.find((col) => col.field === `${month.key}.ktpd`)

        return {
          title: headerMap[month.headerKey] || month.key.toUpperCase(),
          children: [kbpsdCol, ktpdCol].filter(Boolean),
        }
      })
      .filter((col) => col.children.length > 0)

    if (monthlyColumns.length > 0) {
      result.push({
        title: 'Capacity',
        children: monthlyColumns,
      })
    }

    // Position 3: remark and other remaining columns
    const remainingCols = otherCols.filter(
      (col) =>
        col.field !== 'id' &&
        col.field !== 'particulates' &&
        col.field !== 'insertedDateTime',
    )
    result.push(...remainingCols)
    return result
  }, [apiMetadata, columnConfig, headerMap])

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
    addButton: false,
    remarksEditable: false,
    showCalculate: false,
    showExport: true,
    showImport: false,
    saveBtnForRemark: false,
    saveBtn: false,
    showWorkFlowBtns: false,
    showTitle: true,
    showDropdown: false,
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
          selectedDropdownValue={selectedDropdown}
          setSelectedDropdownValue={setSelectedDropdown}
        />
      </Stack>
    </Box>
  )
}

export default NetUnitCapacity
