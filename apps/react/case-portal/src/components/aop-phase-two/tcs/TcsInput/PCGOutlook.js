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

const PCGOutlook = ({
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
      console.log('No PCG Outlook data found, attempting carry-forward...')

      const carryForwardResponse = await TcsApiService.carryForwardPcgOutlook(
        keycloak,
        AOP_YEAR,
        SITE_ID,
      )

      console.log('Carry-forward response:', carryForwardResponse)

      setSnackbarData({
        message: `PCG Outlook data carried forward from previous year successfully!`,
        severity: 'success',
      })
      setSnackbarOpen(true)

      return true
    } catch (carryForwardErr) {
      console.error(
        'Error during carry-forward for PCG Outlook:',
        carryForwardErr,
      )
      return false
    }
  }, [keycloak, AOP_YEAR, SITE_ID, setSnackbarData, setSnackbarOpen])

  // Fetch PCG Outlook Data
  const fetchPcgOutlookData = useCallback(
    async (skipCarryForward = false) => {
      if (!SITE_ID || !AOP_YEAR) return
      try {
        setLoading(true)
        let transformedData = []

        const response = await TcsApiService.getPcgOutlookData(
          keycloak,
          SITE_ID,
          AOP_YEAR,
        )
        console.log('PCG Outlook Response:', response)

        if (response?.length > 0 && Array.isArray(response)) {
          transformedData = response.map((item, index) => ({
            id: item.id || `row_${index}`,
            ...item,
            remarks: item.remarks || '',
            inEdit: false,
          }))
        }

        // If data is empty and carry-forward not skipped, attempt carry-forward and refetch
        if (transformedData.length === 0 && !skipCarryForward) {
          const carryForwardSuccess = await handleCarryForward()
          if (carryForwardSuccess) {
            // Refetch data after successful carry-forward
            await fetchPcgOutlookData(true)
            return
          }
        }

        setRows(transformedData)
        setOriginalRows(transformedData)
      } catch (err) {
        console.error('Error fetching PCG Outlook data:', err)
        setSnackbarData({
          message: `Failed to load PCG Outlook data. Please try again.`,
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
      AOP_YEAR,
      SITE_ID,
      currentTab.id,
      handleCarryForward,
      setSnackbarData,
      setSnackbarOpen,
    ],
  )

  // Fetch data on mount or when dependencies change
  useEffect(() => {
    if (SITE_ID && AOP_YEAR) {
      fetchPcgOutlookData()
    }
  }, [SITE_ID, AOP_YEAR, fetchPcgOutlookData])

  // Generate header names with month-year format
  const headerMap = useMemo(
    () =>
      // generateHeaderNames(AOP_YEAR)
      generateCalendarYearHeaders(AOP_YEAR),
    [AOP_YEAR],
  )

  // Column configuration for PCG Outlook
  const columns = useMemo(() => {
    return [
      { field: 'id', title: 'ID', hidden: true },
      {
        field: 'product',
        title: 'Product',
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
        width: 250,
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
      console.log('PCG Outlook data to save:', data)

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
        'product',
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

      // Remove id and inEdit fields from payload, keep remarks
      const cleanedData = data.map(({ id, inEdit, ...rest }) => rest)

      const response = await TcsApiService.savePcgOutlookData(
        keycloak,
        SITE_ID,
        AOP_YEAR,
        cleanedData,
      )
      console.log('Save PCG Outlook response:', response)

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'PCG Outlook data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      fetchPcgOutlookData()
    } catch (error) {
      console.error('Error saving PCG Outlook data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving PCG Outlook data!',
        severity: 'error',
      })
    }
  }, [
    modifiedCells,
    originalRows,
    keycloak,
    SITE_ID,
    AOP_YEAR,
    setSnackbarData,
    setSnackbarOpen,
    fetchPcgOutlookData,
  ])

  // Export handler
  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await TcsApiService.exportPcgOutlookExcel(keycloak, SITE_ID, AOP_YEAR)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting PCG Outlook data:', error)
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
      const response = await TcsApiService.importPcgOutlookExcel(
        keycloak,
        SITE_ID,
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
        await fetchPcgOutlookData()
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
          link.download = `TCS_PCG_Outlook_Errors_${new Date().getTime()}.xlsx`
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
          await fetchPcgOutlookData()
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
    ExcelName: `PCG_Outlook_${AOP_YEAR}`,
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
          fetchData={fetchPcgOutlookData}
          configType='tcs_pcg_outlook'
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

export default PCGOutlook
