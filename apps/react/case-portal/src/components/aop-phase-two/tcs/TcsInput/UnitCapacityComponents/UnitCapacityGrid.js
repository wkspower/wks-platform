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

    // Check if field is a nested month field (e.g., 'apr.kbpsd')
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

        let transformedData = []
        if (response?.results && Array.isArray(response.results)) {
          transformedData = response.results.map((item, index) => {
            // Backend data is in KBPSD, create nested structure for each month with both KBPSD and KTPD
            const months = [
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
      { key: 'apr', headerKey: 4 },
      { key: 'may', headerKey: 5 },
      { key: 'jun', headerKey: 6 },
      { key: 'jul', headerKey: 7 },
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
        'apr.kbpsd',
        'apr.ktpd',
        'may.kbpsd',
        'may.ktpd',
        'jun.kbpsd',
        'jun.ktpd',
        'jul.kbpsd',
        'jul.ktpd',
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
          apr: row.apr?.kbpsd,
          may: row.may?.kbpsd,
          jun: row.jun?.kbpsd,
          jul: row.jul?.kbpsd,
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

  // Export handler
  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await TcsApiService.exportUnitCapacityExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        capacityType,
        selectedDropdown,
      )

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Unit Capacity data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  // Import handler
  const handleExcelUpload = async (file) => {
    if (!file) return

    setLoading(true)
    try {
      const response = await TcsApiService.importUnitCapacityExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        capacityType,
        selectedDropdown,
        file,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        // Refresh data after import
        await fetchUnitCapacityData(selectedDropdown)
      } else if (response?.code === 400 && response?.data) {
        // Handle error response with Excel file download
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
          link.download = `TCS_Unit_Capacity_${capacityType}_Errors_${new Date().getTime()}.xlsx`
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
          // Refresh data after import
          await fetchUnitCapacityData(selectedDropdown)
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

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    addButton: false,
    remarksEditable: true,
    showCalculate: false,
    showExport: true,
    ExcelName: `Unit_Capacity_${capacityType}_${AOP_YEAR}`,
    showImport: true,
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
          handleExcelUpload={handleExcelUpload}
          handleExport={handleExport}
        />
      </Stack>
    </Box>
  )
}

export default UnitCapacityGrid
