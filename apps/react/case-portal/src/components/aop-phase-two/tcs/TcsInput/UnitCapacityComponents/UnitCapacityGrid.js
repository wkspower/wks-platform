import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'components/aop-phase-two/services/tcs/tcsApiService'
import { useSession } from 'SessionStoreContext'
import { convertFromKBPSD, convertToKBPSD } from './uomConversionUtils'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'

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

  // Custom itemChange handler to auto-convert between KBPSD and KTPD for monthly fields
  const handleCustomItemChange = useCallback((event, setRowsFunc) => {
    const { dataItem, field, value } = event

    // List of month fields that have nested kbpsd/ktpd
    const monthFields = [
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

    // Check if field is a nested month field (e.g., 'april.kbpsd')
    const isMonthField = monthFields.some((month) =>
      field.startsWith(`${month}.`),
    )

    if (!isMonthField) {
      return
    }

    setRowsFunc((prevRows) => {
      return prevRows.map((row) => {
        if (row.id !== dataItem.id) return row

        const updatedRow = { ...row }
        const [monthName, uomType] = field.split('.')

        // Handle conversions based on which field was edited
        if (uomType === 'kbpsd') {
          updatedRow[monthName] = {
            ...updatedRow[monthName],
            kbpsd: value,
            ktpd: convertFromKBPSD(value, 'KTPD'),
          }
        } else if (uomType === 'ktpd') {
          updatedRow[monthName] = {
            ...updatedRow[monthName],
            kbpsd: convertToKBPSD(value, 'KTPD'),
            ktpd: value,
          }
        }

        return updatedRow
      })
    })
  }, [])

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
        editable: true,
        type: 'number1',
        minWidth: 80,
        widthT: 100,
        format: valueFormat,
        title: 'KBPSD',
      }
      config[`${month}.ktpd`] = {
        editable: true,
        type: 'number1',
        minWidth: 80,
        widthT: 100,
        format: valueFormat,
        title: 'KTPD',
      }
    })

    config.remark = {
      title: 'Remark',
      editable: true,
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
      const fieldsToCheck = [
        'april.kbpsd',
        'april.ktpd',
        'may.kbpsd',
        'may.ktpd',
        'june.kbpsd',
        'june.ktpd',
        'july.kbpsd',
        'july.ktpd',
        'aug.kbpsd',
        'aug.ktpd',
        'sep.kbpsd',
        'sep.ktpd',
        'oct.kbpsd',
        'oct.ktpd',
        'nov.kbpsd',
        'nov.ktpd',
        'dec.kbpsd',
        'dec.ktpd',
        'jan.kbpsd',
        'jan.ktpd',
        'feb.kbpsd',
        'feb.ktpd',
        'mar.kbpsd',
        'mar.ktpd',
      ]
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

      // Extract KBPSD values for backend (backend expects flat monthly fields)
      // Set id to null for new items
      const dataInKBPSD = data.map((row) => {
        return {
          id: row.isNew ? null : row.id,
          particulates: row.particulates,
          april: row.april?.kbpsd,
          may: row.may?.kbpsd,
          june: row.june?.kbpsd,
          july: row.july?.kbpsd,
          aug: row.aug?.kbpsd,
          sep: row.sep?.kbpsd,
          oct: row.oct?.kbpsd,
          nov: row.nov?.kbpsd,
          dec: row.dec?.kbpsd,
          jan: row.jan?.kbpsd,
          feb: row.feb?.kbpsd,
          mar: row.mar?.kbpsd,
          remark: row.remark,
          insertedDateTime: row.insertedDateTime,
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
          saveChanges={saveChanges}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          permissions={permissions}
          customItemChange={handleCustomItemChange}
          dropdownConfig={dropdownConfig}
          selectedDropdownValue={selectedDropdown}
          setSelectedDropdownValue={setSelectedDropdown}
        />
      </Stack>
    </Box>
  )
}

export default UnitCapacityGrid
