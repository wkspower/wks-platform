import { useMemo, useState, useCallback } from 'react'
import { Box } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo, {
  customValueFormatterPhaseTwo,
} from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { validateNestedRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { IntersiteSteamTransferApiService } from 'components/aop-phase-two/services/cpp/jmd/intersiteSteamTransferApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import { downloadBase64Excel } from 'components/aop-phase-two/common/utilities/downloadBase64Excel'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'
import { useDebounce } from 'hooks/useDebounce'

const IntersiteSteamTransfer = () => {
  const keycloak = useSession()
  // State management
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
    jmdSelectedPlants,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const IS_JMD = siteObject?.name?.toLowerCase() == 'jmd'

  const AOP_YEAR = year?.selectedYear

  const PLANT_ID_LIST = useMemo(
    () =>
      IS_JMD ? jmdSelectedPlants?.map((plant) => plant.id) || [] : [PLANT_ID],
    [PLANT_ID, jmdSelectedPlants],
  )

  const EXCEL_NAME = generateExcelName(
    dataGridStore,
    'Intersite_Steam_Transfer',
  )

  const headerMap = useMemo(() => generateHeaderNames(AOP_YEAR), [AOP_YEAR])
  const valueFormat = ValueFormatterPhaseTwo()
  const valueFormatTwo = customValueFormatterPhaseTwo(2)

  // Fiscal-year month order: Apr → Mar
  const MONTH_TO_INDEX = {
    apr: 4,
    may: 5,
    jun: 6,
    jul: 7,
    aug: 8,
    sep: 9,
    oct: 10,
    nov: 11,
    dec: 12,
    jan: 1,
    feb: 2,
    mar: 3,
  }

  // Month columns with Min/Max sub-columns (Apr → Mar)
  // Field names match the flat DTO fields: minApr, maxApr, minMay, maxMay, ...
  const MONTH_COLUMNS = useMemo(
    () =>
      [
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
      ].map((mon) => ({
        title: headerMap[MONTH_TO_INDEX[mon]],
        children: [
          {
            field: `min${mon.charAt(0).toUpperCase() + mon.slice(1)}`,
            title: 'Min',
            widthT: 100,
            minWidth: 100,
            type: 'number1',
            editable: true,
            format: valueFormatTwo,
          },
          {
            field: `max${mon.charAt(0).toUpperCase() + mon.slice(1)}`,
            title: 'Max',
            widthT: 100,
            minWidth: 100,
            type: 'number1',
            editable: true,
            format: valueFormatTwo,
          },
        ],
      })),
    [headerMap, valueFormatTwo],
  )

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Base (static) columns
  const baseColumns = useMemo(
    () => [
      // CPP Plant Name
      // {
      //   field: 'cppPlantName',
      //   title: 'CPP Plant Name',
      //   widthT: 180,
      //   type: 'text',
      //   editable: false,
      //   locked: true,
      //   minWidth: 180,
      // },

      // Sender Plant Name
      {
        field: 'senderPlantName',
        title: 'Sender Plant Name',
        widthT: 180,
        type: 'text',
        editable: false,
        minWidth: 180,
      },
      // Sender Plant Code
      {
        field: 'senderPlantCode',
        title: 'Sender Plant Code',
        widthT: 160,
        type: 'text',
        editable: false,
        minWidth: 160,
      },
      // Sender Cost Center Name
      // {
      //   field: 'senderCostCenterName',
      //   title: 'Sender Cost Center Name',
      //   widthT: 200,
      //   type: 'text',
      //   editable: false,
      //   minWidth: 200,
      // },
      // // Sender Cost Center Code
      // {
      //   field: 'senderCostCenterCode',
      //   title: 'Sender Cost Center Code',
      //   widthT: 180,
      //   type: 'text',
      //   editable: false,
      //   minWidth: 180,
      // },
      // Receiver Plant Name
      {
        field: 'receiverPlantName',
        title: 'Receiver Plant Name',
        widthT: 180,
        type: 'text',
        editable: false,
        minWidth: 180,
      },
      // Receiver Plant Code
      {
        field: 'receiverPlantCode',
        title: 'Receiver Plant Code',
        widthT: 160,
        type: 'text',
        editable: false,
        minWidth: 160,
      },
      // Receiver Cost Center Name
      // {
      //   field: 'receiverCostCenterName',
      //   title: 'Receiver Cost Center Name',
      //   widthT: 200,
      //   type: 'text',
      //   editable: false,
      //   minWidth: 200,
      // },
      // // Receiver Cost Center Code
      // {
      //   field: 'receiverCostCenterCode',
      //   title: 'Receiver Cost Center Code',
      //   widthT: 180,
      //   type: 'text',
      //   editable: false,
      //   minWidth: 180,
      // },
      // Material
      {
        field: 'normParameterName',
        title: 'Material',
        widthT: 180,
        type: 'text',
        editable: false,
        locked: false,
        minWidth: 180,
      },
      // SAP Code
      {
        field: 'sapMaterialCode',
        title: 'SAP Code',
        widthT: 160,
        type: 'text',
        editable: false,
        locked: false,
        minWidth: 160,
      },
      // UOM
      {
        field: 'uom',
        title: 'UOM',
        widthT: 100,
        type: 'text',
        editable: false,
        minWidth: 100,
      },
    ],
    [],
  )

  // Column definitions for monthly view
  const nestedColumns = useMemo(
    () => [
      ...baseColumns,
      // Monthly columns (Min / Max) ─ Apr → Mar
      ...MONTH_COLUMNS,
      {
        field: 'remarks',
        title: 'Remarks',
        widthT: 200,
        type: 'textarea',
        editable: true,
        minWidth: 200,
      },
    ],
    [baseColumns, MONTH_COLUMNS],
  )

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])

  const fetchIntersiteSteamTransferData = useCallback(async () => {
    setLoading(true)
    try {
      const res =
        await IntersiteSteamTransferApiService.getIntersiteSteamTransfer(
          keycloak,
          AOP_YEAR,
        )

      const listData = res?.data?.list || []
      if (listData.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        setLoading(false)
        return
      }
      let tempRes = listData.map((item, index) => {
        return {
          ...item,
          id: item.id || index + 1,
          remarks: item.remarks || '',
        }
      })

      setRows(tempRes)
      setOriginalRows(tempRes)
    } catch (error) {
      console.error('Error fetching intersite steam transfer data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [keycloak, AOP_YEAR])

  useDebounce(
    () => {
      if (AOP_YEAR) {
        fetchIntersiteSteamTransferData()
        setModifiedCells({})
      }
    },
    1000,
    [AOP_YEAR, fetchIntersiteSteamTransferData],
  )

  // Save handler with API call
  const saveChanges = async () => {
    setLoading(true)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    var rawData = Object.values(modifiedCells)
    const data = rawData.filter((row) => row.inEdit)
    if (data.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    // Validation: if any min/max cell is updated, remarks must be filled
    const fieldsToCheck = [
      'minApr',
      'maxApr',
      'minMay',
      'maxMay',
      'minJun',
      'maxJun',
      'minJul',
      'maxJul',
      'minAug',
      'maxAug',
      'minSep',
      'maxSep',
      'minOct',
      'maxOct',
      'minNov',
      'maxNov',
      'minDec',
      'maxDec',
      'minJan',
      'maxJan',
      'minFeb',
      'maxFeb',
      'minMar',
      'maxMar',
    ]
    const validationError = validateNestedRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'cppPlantName',
    )

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    const payload = modifiedData
    try {
      console.log('payload', payload)

      // Call the API to save changes
      const response =
        await IntersiteSteamTransferApiService.saveIntersiteSteamTransfer(
          keycloak,
          PLANT_ID_LIST,
          payload,
          AOP_YEAR,
        )

      if (response?.code === 200) {
        setModifiedCells({})
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Successfully saved ${modifiedData.length} changes!`,
          severity: 'success',
        })
        // Refresh the data after save
        await fetchIntersiteSteamTransferData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message:
            response?.message || 'Failed to save changes. Please try again.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error saving intersite steam transfer data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = async (file) => {
    if (!file) return

    setLoading(true)
    try {
      const response =
        await IntersiteSteamTransferApiService.saveIntersiteSteamTransferExcel(
          file,
          keycloak,
          PLANT_ID_LIST,
          AOP_YEAR,
        )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        // Refresh data after import
        await fetchIntersiteSteamTransferData()
      } else if (response?.code === 400 && response?.data) {
        // Handle error response with Excel file download (partial save)
        try {
          downloadBase64Excel(
            response.data,
            `Intersite_Steam_Transfer_Errors_${new Date().getTime()}.xlsx`,
          )

          setSnackbarOpen(true)
          setSnackbarData({
            message:
              response?.message || 'Partial data saved. Error file downloaded.',
            severity: 'warning',
          })
          // Refresh data after import
          await fetchIntersiteSteamTransferData()
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

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })
    setLoading(true)

    try {
      await IntersiteSteamTransferApiService.exportIntersiteSteamTransfer(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
        EXCEL_NAME,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting intersite steam transfer data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Permissions for monthly view (editable)
  const monthlyPermissions = useMemo(() => {
    return {
      showAction: true,
      addButton: false,
      deleteButton: false,
      editButton: true,
      saveBtn: true,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Intersite Steam Transfer',
      showImport: true,
      showTitle: true,
      showExport: true,
      ExcelName: EXCEL_NAME,
    }
  }, [EXCEL_NAME])

  console.log('nestedColumns', nestedColumns)
  return (
    <Box>
      <LoaderBackdrop open={!!loading} />

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
        {/* Monthly Grid */}
        <AdvanceKendoTable
          columns={nestedColumns}
          rows={rows}
          setRows={setRows}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          title='Intersite Steam Transfer'
          permissions={monthlyPermissions}
          handleRemarkCellClick={handleRemarkCellClick}
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
          customHeight={50}
        />
      </Box>
    </Box>
  )
}

export default IntersiteSteamTransfer
