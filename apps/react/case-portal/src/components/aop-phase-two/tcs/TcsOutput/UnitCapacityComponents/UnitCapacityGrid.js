import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsOutputApiService } from 'components/aop-phase-two/services/tcs/tcsOutputApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { convertFromKBPSD, convertToKBPSD } from './uomConversionUtils'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import ApproveDialog from '../../TcsInput/workflow/ApproveDialog'

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
  const headerMap = generateHeaderNames(AOP_YEAR)

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

  const [openApproveDialogeBox, setOpenApproveDialogeBox] = useState(false)
  // Approve Dialog handlers
  const closeApproveDialogeBox = () => setOpenApproveDialogeBox(false)
  const handleApprove = (plantId, remark) => {
    console.log('Approved plantId:', plantId, 'Remark:', remark)
    // TODO: Call API to approve the plant
    setSnackbarData({
      message: `Plant ${plantId} approved successfully`,
      severity: 'success',
    })
    setSnackbarOpen(true)
    closeApproveDialogeBox()
  }
  const handleReject = (plantId, remark) => {
    console.log('Rejected plantId:', plantId, 'Remark:', remark)
    // TODO: Call API to reject the plant
    setSnackbarData({
      message: `Plant ${plantId} rejected`,
      severity: 'error',
    })
    setSnackbarOpen(true)
    closeApproveDialogeBox()
  }

  // Fetch UOM options for this capacity type
  const fetchUOMOptions = useCallback(async () => {
    if (!VERTICAL_ID || !AOP_YEAR) return
    try {
      setLoadingUOM(true)
      const response = await TcsOutputApiService.getTcsUnitCapacityUOM(
        keycloak,
        VERTICAL_ID,
        AOP_YEAR,
        capacityType,
      )

      const uomOptions = response || response?.data || []
      if (uomOptions.length === 0) return

      setDropdownConfig((prev) => ({
        ...prev,
        options: uomOptions,
      }))

      const defaultUOM = uomOptions[0].id
      setSelectedDropdown(defaultUOM)
    } catch (err) {
      console.error(`Error fetching UOM options (${capacityType}):`, err)
      setSnackbarData({
        message: `Failed to load UOM options. Please try again.`,
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setLoadingUOM(false)
    }
  }, [
    keycloak,
    VERTICAL_ID,
    AOP_YEAR,
    capacityType,
    setSnackbarData,
    setSnackbarOpen,
  ])

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
          // Create a map of particulates to plantId
          const particulatesMap = new Map()
          let plantIdCounter = 1

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

            let plantId = item.plantId

            // If no plantId exists, use particulates field to determine plantId
            if (!plantId && item.particulates) {
              if (!particulatesMap.has(item.particulates)) {
                particulatesMap.set(item.particulates, plantIdCounter++)
              }
              plantId = particulatesMap.get(item.particulates)
            } else if (!plantId) {
              // Fallback if neither plantId nor particulates exists
              plantId = new Date().getTime() + index
            }

            return {
              id: item.id || `row_${index}`,
              particulates: item.particulates,
              ...monthData,
              remark: item.remark,
              insertedDateTime: item.insertedDateTime,
              plantId: plantId,
              plantName: item.plantName || item.particulates,
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
  useEffect(() => {
    if (VERTICAL_ID && AOP_YEAR) {
      fetchUOMOptions()
    }
  }, [VERTICAL_ID, AOP_YEAR, fetchUOMOptions])

  // Fetch capacity data when dropdown selection changes
  useEffect(() => {
    if (SITE_ID && VERTICAL_ID && AOP_YEAR && selectedDropdown) {
      fetchUnitCapacityData(selectedDropdown)
    }
  }, [SITE_ID, VERTICAL_ID, AOP_YEAR, selectedDropdown, fetchUnitCapacityData])

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
    showExport: true,
    showTitle: true,
    showDropdown: false,
    approveBtn: true,
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
          onApproveClick={() => setOpenApproveDialogeBox(true)}
        />
      </Stack>
      {/* Approve Dialog */}
      <ApproveDialog
        open={openApproveDialogeBox}
        onClose={closeApproveDialogeBox}
        onApprove={handleApprove}
        onReject={handleReject}
        entries={rows.map((row) => ({
          id: row.id,
          plantId: row.plantId,
          plantName: row.plantName,
        }))}
      />
    </Box>
  )
}

export default UnitCapacityGrid
