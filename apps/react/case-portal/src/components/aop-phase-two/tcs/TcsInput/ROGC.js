import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'components/aop-phase-two/services/tcs/tcsApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import {
  generateCalendarYearHeaders,
  generateHeaderNames,
} from 'components/aop-phase-two/common/utilities/generateHeaders'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { Stack } from '../../../../../node_modules/@mui/material/index'

const ROGC = ({
  PLANT_ID,
  SITE_ID,
  AOP_YEAR,
  currentTab,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
}) => {
  const keycloak = useSession()
  const valueFormat = ValueFormatterPhaseTwo()

  // State management
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Carry forward data from previous year
  const handleCarryForward = useCallback(async () => {
    try {
      console.log('No data found, attempting carry-forward...')

      const carryForwardResponse = await TcsApiService.carryForwardRogc(
        keycloak,
        AOP_YEAR,
        SITE_ID,
        PLANT_ID,
      )

      console.log('Carry-forward response:', carryForwardResponse)

      setSnackbarData({
        message: 'Data carried forward from previous year successfully!',
        severity: 'success',
      })
      setSnackbarOpen(true)

      return true
    } catch (carryForwardErr) {
      console.error('Error during carry-forward:', carryForwardErr)
      return false
    }
  }, [keycloak, AOP_YEAR, SITE_ID, PLANT_ID, setSnackbarData, setSnackbarOpen])

  // Fetch ROGC Data
  const fetchRogcData = useCallback(
    async (skipCarryForward = false) => {
      if (!PLANT_ID || !AOP_YEAR) return
      try {
        setLoading(true)
        let transformedData = []

        // TODO: Replace with actual API call once backend is ready
        // const response = getMockRogcResponse()
        const response = await TcsApiService.getTcsRogcData(
          keycloak,
          SITE_ID,
          PLANT_ID,
          AOP_YEAR,
        )
        console.log('TCS ROGC Response:', response)

        if (
          response?.furnaceData?.length > 0 &&
          response?.furnaceData &&
          Array.isArray(response.furnaceData)
        ) {
          // Calculate days dynamically based on financial year
          const getDaysInMonth = (year, month) => {
            return new Date(year, month, 0).getDate()
          }

          // Extract the start year from AOP_YEAR (e.g., "2025-26" -> 2025)
          const startYear = parseInt(AOP_YEAR?.split('-')[0])
          const endYear = startYear + 1

          // Add days row at the beginning
          const daysRow = {
            id: 'days_row',
            furnace: 'Days',
            apr: getDaysInMonth(startYear, 4),
            may: getDaysInMonth(startYear, 5),
            jun: getDaysInMonth(startYear, 6),
            jul: getDaysInMonth(startYear, 7),
            aug: getDaysInMonth(startYear, 8),
            sep: getDaysInMonth(startYear, 9),
            oct: getDaysInMonth(startYear, 10),
            nov: getDaysInMonth(startYear, 11),
            dec: getDaysInMonth(startYear, 12),
            jan: getDaysInMonth(endYear, 1),
            feb: getDaysInMonth(endYear, 2),
            mar: getDaysInMonth(endYear, 3),
            remarks: '-',
            isEditable: false,
            inEdit: false,
          }
          transformedData = [daysRow]

          // Add furnace data
          const furnaceRows = response.furnaceData.map((item, index) => ({
            id: item.id || `row_${index}`,
            ...item,
            inEdit: false,
          }))
          transformedData.push(...furnaceRows)

          // Add average row
          const averageRow = {
            id: 'average_row',
            furnace: 'Average of Duty_Furnace_Cracking',
            ...response.gCalPerHrData,
            remarks: '-',
            isEditable: false,
            inEdit: false,
          }
          transformedData.push(averageRow)
        } else {
          // If no data and carry forward not skipped, attempt carry forward
          if (!skipCarryForward) {
            const carryForwardSuccess = await handleCarryForward()
            if (carryForwardSuccess) {
              // Refetch data after successful carry forward
              await fetchRogcData(true)
              return
            }
          }
        }

        setRows(transformedData)
        setOriginalRows(transformedData)
      } catch (err) {
        console.error('Error fetching ROGC data:', err)
        setSnackbarData({
          message: `Failed to load ROGC data. Please try again.`,
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
      SITE_ID,
      AOP_YEAR,
      currentTab.id,
      setSnackbarData,
      setSnackbarOpen,
      handleCarryForward,
    ],
  )

  // Fetch data on mount or when dependencies change
  useEffect(() => {
    if (PLANT_ID && AOP_YEAR && SITE_ID) {
      fetchRogcData()
    }
  }, [PLANT_ID, AOP_YEAR, SITE_ID, fetchRogcData])

  // Generate header names with month-year format
  const headerMap = useMemo(
    () =>
      // generateHeaderNames(AOP_YEAR),
      generateCalendarYearHeaders(AOP_YEAR),
    [AOP_YEAR],
  )

  // Column configuration for ROGC - hardcoded like FixedConsumption.js
  const columns = useMemo(() => {
    return [
      { field: 'id', title: 'ID', hidden: true },
      {
        field: 'furnace',
        title: 'Furnace',
        width: 150,
        minWidth: 150,
        type: 'text',
        editable: false,
      },
      {
        field: 'jan',
        title: headerMap[1],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'feb',
        title: headerMap[2],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'mar',
        title: headerMap[3],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'apr',
        title: headerMap[4],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'may',
        title: headerMap[5],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'jun',
        title: headerMap[6],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'jul',
        title: headerMap[7],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'aug',
        title: headerMap[8],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'sep',
        title: headerMap[9],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'oct',
        title: headerMap[10],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'nov',
        title: headerMap[11],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'dec',
        title: headerMap[12],
        editable: true,
        width: 100,
        minWidth: 80,
        type: 'number1',
        format: valueFormat,
      },
      {
        field: 'remarks',
        title: 'Remark',
        editable: true,
        width: 150,
        minWidth: 150,
        type: 'textarea',
      },
    ]
  }, [headerMap])

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    // Prevent remark dialog from opening if row is not editable
    if (!row?.isEditable && row?.isEditable !== undefined) {
      return
    }
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Track when modifiedCells is cleared and reset inEdit flags
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

  // Save changes
  const saveChanges = useCallback(async () => {
    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      console.log('ROGC data to save:', data)

      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        return
      }

      // Custom validation: If any row data is updated, remarks must be filled and different from original
      const fieldsToCheck = [
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
      const validationError = validateRowDataWithRemarks(
        data,
        originalRows,
        fieldsToCheck,
        'furnace',
        'remarks',
      )

      if (validationError) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationError,
          severity: 'error',
        })
        return
      }

      const response = await TcsApiService.saveRogcData(
        keycloak,
        SITE_ID,
        PLANT_ID,
        AOP_YEAR,
        data,
      )
      console.log('Save ROGC response:', response)

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'ROGC data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      fetchRogcData()
    } catch (error) {
      console.error('Error saving ROGC data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving ROGC data!',
        severity: 'error',
      })
    }
  }, [
    modifiedCells,
    originalRows,
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    setSnackbarData,
    setSnackbarOpen,
    fetchRogcData,
  ])

  // Export handler
  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await TcsApiService.exportRogcExcel(keycloak, SITE_ID, PLANT_ID, AOP_YEAR)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting ROGC data:', error)
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
      const response = await TcsApiService.importRogcExcel(
        keycloak,
        SITE_ID,
        PLANT_ID,
        AOP_YEAR,
        file,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        // Refresh data after import
        await fetchRogcData()
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
          link.download = `TCS_ROGC_Errors_${new Date().getTime()}.xlsx`
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
          await fetchRogcData()
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
    ExcelName: `ROGC_${AOP_YEAR}`,
    showImport: true,
    saveBtnForRemark: true,
    saveBtn: true,
    showWorkFlowBtns: false,
    showTitle: true,
    filterable: false,
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <Stack sx={{ mt: 2 }}>
        <AdvanceKendoTable
          rows={rows}
          setRows={setRows}
          fetchData={fetchRogcData}
          configType='tcs_rogc'
          handleRemarkCellClick={handleRemarkCellClick}
          columns={columns}
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
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          permissions={permissions}
        />
      </Stack>
    </Box>
  )
}

export default ROGC
