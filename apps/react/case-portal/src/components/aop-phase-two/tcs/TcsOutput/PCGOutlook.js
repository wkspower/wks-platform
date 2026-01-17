import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsOutputApiService } from 'components/aop-phase-two/services/tcs/tcsOutputApiService'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'

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

  // Fetch PCG Outlook Data
  const fetchPcgOutlookData = useCallback(async () => {
    if (!SITE_ID || !AOP_YEAR) return
    try {
      setLoading(true)
      let transformedData = []

      const response = await TcsOutputApiService.getPcgOutlookData(
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
          isEditable: false,
        }))
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
  }, [
    keycloak,
    AOP_YEAR,
    SITE_ID,
    currentTab.id,
    setSnackbarData,
    setSnackbarOpen,
  ])

  // Fetch data on mount or when dependencies change
  useEffect(() => {
    if (SITE_ID && AOP_YEAR) {
      fetchPcgOutlookData()
    }
  }, [SITE_ID, AOP_YEAR, fetchPcgOutlookData])

  // Generate header names with month-year format
  const headerMap = useMemo(() => generateHeaderNames(AOP_YEAR), [AOP_YEAR])

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

  const permissions = {
    customHeight: { mainBox: '32vh', otherBox: '100%' },
    textAlignment: 'center',
    allAction: true,
    showExport: true,
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
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        permissions={permissions}
        readonly={true}
      />
    </Box>
  )
}

export default PCGOutlook
