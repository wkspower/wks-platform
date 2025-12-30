import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { TcsApiService } from 'services/phase-two-services/tcsApiService'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'

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
  const valueFormat = ValueFormatterProduction()

  // State management
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)


  // Fetch ROGC Data
  const fetchRogcData = useCallback(async () => {
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

      if (response?.furnaceData && Array.isArray(response.furnaceData)) {
        transformedData = response.furnaceData.map((item, index) => ({
          id: item.id || `row_${index}`,
          ...item,
          inEdit: false,
        }))

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
      }

      setRows(transformedData)
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
  }, [
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    currentTab.id,
    setSnackbarData,
    setSnackbarOpen,
  ])

  // Fetch data on mount or when dependencies change
  useEffect(() => {
    if (PLANT_ID && AOP_YEAR && SITE_ID) {
      fetchRogcData()
    }
  }, [PLANT_ID, AOP_YEAR, SITE_ID, fetchRogcData])

  // Generate header names with month-year format
  const headerMap = useMemo(() => generateHeaderNames(AOP_YEAR), [AOP_YEAR])

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
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    setSnackbarData,
    setSnackbarOpen,
    fetchRogcData,
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
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        permissions={permissions}
      />
    </Box>
  )
}

export default ROGC
