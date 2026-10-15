import { useState, useEffect, useCallback, useMemo, useRef } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { generateExcelName } from '../../common/utilities/excelNameUtil'
import { downloadBase64Excel } from '../../common/utilities/downloadBase64Excel'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/pcg/productionNormsApiService'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'

const TargetActualDafThroughput = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year, oldYear, isReleased, screenTitle } = dataGridStore
  const PLANT_ID = plantObject?.id || plantObject?.plantId
  const AOP_YEAR = year?.selectedYear || year
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, isReleased)

  const apiRef = useRef(null)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [open1, setOpen1] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Column definitions
  const columns = useMemo(
    () => [
      {
        field: 'particulars',
        title: 'Particulars',
        type: 'text',
        editable: false,
        widthT: 220,
        minWidth: 200,
      },
      {
        field: 'uom',
        title: 'UOM',
        type: 'text',
        editable: false,
        widthT: 120,
        minWidth: 100,
      },
      {
        field: 'targetValue',
        title: 'Target Value',
        type: 'number',
        format: '{0:0.00}',
        editable: !READ_ONLY,
        widthT: 160,
        minWidth: 140,
      },
      {
        field: 'range',
        title: 'Range (+/-)',
        type: 'number',
        format: '{0:0.00}',
        editable: !READ_ONLY,
        widthT: 160,
        minWidth: 140,
      },
      {
        field: 'remarks',
        title: 'Remarks',
        type: 'text',
        editable: !READ_ONLY,
        widthT: 200,
        minWidth: 160,
      },
    ],
    [READ_ONLY],
  )

  const permissions = useMemo(
    () => ({
      showAction: true,
      addButton: false,
      addBtnName: 'Add Item',
      deleteButton: false,
      editButton: false,
      saveBtn: !READ_ONLY,
      allAction: true,
      showExport: false,
      ExcelName: `Target_Actual_DAF_Throughput_${AOP_YEAR || ''}`,
      showImport: false,
      showTitleNameBusiness: true,
      showTitle: true,
      titleName: 'Target Actual DAF Throughput',
      showCalculate: false,
      calculateDisabled: true,
    }),
    [AOP_YEAR, READ_ONLY],
  )

  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setLoading(true)
    setModifiedCells({})
    try {
      const response =
        await ProductionNormsApiService.getTargetActualDafThroughtFilter(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

      const list = Array.isArray(response)
        ? response
        : response?.data || response?.result || []

      if (Array.isArray(list) && list.length > 0) {
        const formattedRows = list.map((item, index) => {
          const targetVal =
            item?.targetValue !== null && item?.targetValue !== undefined
              ? item.targetValue
              : 0
          const rangeVal =
            item?.range !== null && item?.range !== undefined
              ? item.range
              : 0
          const remarksVal = item?.remarks || item?.remark || ''

          return {
            ...item,
            id: item?.id || index + 1,
            idFromApi: item?.id || null,
            normParameterId: item?.normParameterId || null,
            particulars: item?.displayName || 'DAF Throughput',
            displayName: item?.displayName || 'DAF Throughput',
            uom: item?.uom || 'TPH',
            targetValue: targetVal,
            range: rangeVal,
            remarks: remarksVal,
            remark: remarksVal,
            aopYear: item?.aopYear || AOP_YEAR,
            plantId: item?.plantId || PLANT_ID,
            isEditable: !READ_ONLY,
            inEdit: false,
          }
        })
        setRows(formattedRows)
        setOriginalRows(formattedRows)
      } else {
        const defaultData = [
          {
            id: 1,
            idFromApi: null,
            normParameterId: null,
            particulars: 'DAF Throughput',
            displayName: 'DAF Throughput',
            uom: 'TPH',
            targetValue: 0,
            range: 0,
            remarks: '',
            remark: '',
            aopYear: AOP_YEAR,
            plantId: PLANT_ID,
            isEditable: !READ_ONLY,
            inEdit: false,
          },
        ]
        setRows(defaultData)
        setOriginalRows(defaultData)
      }
    } catch (error) {
      console.error('Error fetching Target Actual DAF Throughput data:', error)
      setRows([])
      setOriginalRows([])
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, AOP_YEAR, keycloak, READ_ONLY])

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchData()
    }
  }, [PLANT_ID, AOP_YEAR, fetchData])

  const saveChanges = useCallback(async () => {
    const data = Object.values(modifiedCells)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No changes to save.',
        severity: 'info',
      })
      return
    }

    const fieldsToCheck = ['targetValue', 'range']
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'particulars',
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

    setLoading(true)
    try {
      const payload = (rows || []).map((row) => {
        const modifiedRow = modifiedCells[row.id] || row
        return {
          normParameterId: modifiedRow.normParameterId || null,
          displayName:
            modifiedRow.displayName ||
            modifiedRow.particulars ||
            'DAF Throughput',
          targetValue:
            modifiedRow.targetValue !== '' &&
              modifiedRow.targetValue !== null &&
              modifiedRow.targetValue !== undefined
              ? Number(modifiedRow.targetValue)
              : 0,
          range:
            modifiedRow.range !== '' &&
              modifiedRow.range !== null &&
              modifiedRow.range !== undefined
              ? Number(modifiedRow.range)
              : 0,
          remarks: modifiedRow.remarks || modifiedRow.remark || '',
          aopYear: modifiedRow.aopYear || AOP_YEAR,
          plantId: modifiedRow.plantId || PLANT_ID,
        }
      })

      const res =
        await ProductionNormsApiService.saveTargetActualDafThroughtFilter(
          keycloak,
          AOP_YEAR,
          payload,
        )

      if (
        res?.code === 200 ||
        res?.message?.toLowerCase().includes('success') ||
        res?.status === 200
      ) {
        setSnackbarData({
          message:
            res?.message || 'Target Actual DAF Throughput saved successfully!',
          severity: 'success',
        })
        setSnackbarOpen(true)
        setModifiedCells({})
        await fetchData()
      } else {
        setSnackbarData({
          message:
            res?.message || 'Failed to save Target Actual DAF Throughput.',
          severity: 'error',
        })
        setSnackbarOpen(true)
      }
    } catch (error) {
      console.error('Error saving Target Actual DAF Throughput:', error)
      setSnackbarData({
        message: error.message || 'Failed to save changes',
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, rows, originalRows, keycloak, AOP_YEAR, PLANT_ID, fetchData])

  const handleRemarkCellClick = useCallback(
    (param1, param2) => {
      if (READ_ONLY) return
      if (typeof param1 === 'object' && param1 !== null) {
        setCurrentRowId(param1?.id)
        setCurrentRemark(param1?.remarks || param1?.remark || '')
      } else {
        setCurrentRowId(param1)
        setCurrentRemark(param2 || '')
      }
      setRemarkDialogOpen(true)
    },
    [READ_ONLY],
  )

  const handleExport = useCallback(() => {
    const excelName = generateExcelName(
      screenTitle || 'Target Actual DAF Throughput',
      AOP_YEAR,
      plantObject?.plantCode,
    )
    downloadBase64Excel(rows, columns, excelName)
  }, [screenTitle, AOP_YEAR, plantObject, rows, columns])

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />
      <Box>
        <AdvanceKendoTable
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          setRows={setRows}
          columns={columns}
          rows={rows}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          apiRef={apiRef}
          open1={open1}
          setOpen1={setOpen1}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          handleRemarkCellClick={handleRemarkCellClick}
          fetchData={fetchData}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={setCurrentRowId}
          permissions={permissions}
          saveChanges={saveChanges}
          title={permissions.showTitle ? permissions.titleName : ''}
          handleExport={handleExport}
          paginationConfig={{
            threshold: 100,
            buttonCount: 5,
            pageSizes: [10, 20, 50, 100],
            defaultPageSize: 100,
          }}
        />
      </Box>
    </Box>
  )
}

export default TargetActualDafThroughput

