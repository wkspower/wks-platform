import React, { useState, useEffect } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import ValueFormatterPhaseTwo, {
  customValueFormatterPhaseTwo,
} from '../../common/ValueFormatterPhaseTwo'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { ProductionNormsApiService } from '../../services/coker/productionNormsApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'

const HistoricalMonths = ({ refreshData }) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [dynamicColumns, setDynamicColumns] = useState([])

  const valueFormat = customValueFormatterPhaseTwo(5)

  const selectOptions = [
    { value: 'P-4', label: 'P-4' },
    { value: 'P-5', label: 'P-5' },
    { value: 'NP', label: 'NP' },
    { value: 'NR', label: 'NR' },
  ]

  const baseColumns = [
    {
      field: 'id',
      title: 'Id',
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'TypeDisplayName',
      title: 'Type',
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
    ...dynamicColumns,
    {
      field: 'remarks',
      title: 'Remarks',
      minWidth: 200,
      type: 'text',
      editable: true,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchData = async () => {
    setLoading(true)
    try {
      // Fetch manual entry data from backend API
      const response = await ProductionNormsApiService.getHistoricalMonths(
        keycloak,
        AOP_YEAR,
        PLANT_ID,
      )

      // Extract data from response - API returns { data: [...], code: 200, message: '...' }
      const manualEntryData = response?.data || []

      // Derive month column keys dynamically from the first row of response data
      if (manualEntryData.length > 0) {
        const EXCLUDED_KEYS = [
          'Particulars',
          'id',
          'TypeDisplayName',
          'UOM',
          'auditYear',
          'normParameterFKId',
          'remark',
          'remarks',
          'Remarks',
          'inEdit',
          'productName',
        ]
        const monthKeys = Object.keys(manualEntryData[0]).filter(
          (key) => !EXCLUDED_KEYS.includes(key),
        )
        const generatedMonthCols = monthKeys.map((key) => ({
          field: key,
          title: key,
          minWidth: 120,
          type: 'select',
          options: selectOptions,
          displayMode: 'label',
          editable: true,
          format: valueFormat,
        }))
        setDynamicColumns(generatedMonthCols)
      }

      // Add inEdit flag to each row for edit tracking
      const dataWithEditFlag = manualEntryData.map((row, index) => ({
        ...row,
        productName: row.Particulars,
        remarks: row.Remarks || row.remark,
        id: index + 1,
        idFromApi: row.id || null,
        inEdit: false,
      }))

      setRows(dataWithEditFlag)
      setOriginalRows(dataWithEditFlag)
    } catch (error) {
      console.error('Error fetching manual entry data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error fetching data',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const saveChanges = async () => {
    setLoading(true)

    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const data = modifiedData.filter((row) => row.inEdit)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const monthKeys = dynamicColumns.map((col) => col.field)
    const fieldsToCheck = monthKeys
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'productName',
      'remarks',
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

    // Build payload dynamically using month keys from API response
    const convertedData = data.map((row) => {
      const monthData = {}
      monthKeys.forEach((key) => {
        monthData[key] = row[key]
      })
      return {
        ...monthData,
        id: row.idFromApi || null,
        Remarks: row?.remarks,
      }
    })

    try {
      // Call the API to save manual entry data
      await ProductionNormsApiService.saveHistoricalMonths(
        keycloak,
        AOP_YEAR,
        PLANT_ID,
        convertedData,
      )

      // If we reach here, save was successful
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      setOriginalRows([])
      await fetchData()
    } catch (error) {
      console.error('Error saving manual entry data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving data!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const handleCalculate = async () => {
    setLoading(true)

    try {
      const response =
        await ProductionNormsApiService.calculateHistoricalMonths(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

      if (response?.code === 200) {
        await fetchData()
      }
    } catch (error) {
      console.error('Error calculating steady state consumption:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (refreshData) {
      fetchData()
      // handleCalculate()
    }
  }, [refreshData])

  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showExport: false,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Pigging - NP/P-4/P-5/NR',
    showDropdown: false,
    remarksEditable: true,
    showCalculate: false,
  }

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />

      <AdvanceKendoTable
        columns={baseColumns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
        handleRemarkCellClick={handleRemarkCellClick}
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
        handleCalculate={handleCalculate}
        // groupBy={['normParameterTypeDisplayName']}
        customHeight={70}
        paginationConfig={{
          threshold: 100,
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 100,
        }}
      />
    </Box>
  )
}

export default HistoricalMonths
