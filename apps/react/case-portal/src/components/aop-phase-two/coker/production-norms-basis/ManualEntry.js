import React, { useState, useEffect } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo, {
  customValueFormatterPhaseTwo,
} from '../../common/ValueFormatterPhaseTwo'
import { validateRowDataWithRemarks } from '../../common/commonUtilityFunctions'
import { ProductionNormsApiService } from '../../services/coker/productionNormsApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'

const ManualEntry = ({ refreshData }) => {
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

  const valueFormat = customValueFormatterPhaseTwo(5)
  const headerMap = generateHeaderNames(AOP_YEAR)

  const selectOptions = [
    { value: '4.0', label: 'P-4' },
    { value: '5.0', label: 'P-5' },
    { value: '1.0', label: 'NP' },
  ]

  const monthColumns = [
    { field: 'apr', headerIndex: 4 },
    { field: 'may', headerIndex: 5 },
    { field: 'jun', headerIndex: 6 },
    { field: 'jul', headerIndex: 7 },
    { field: 'aug', headerIndex: 8 },
    { field: 'sep', headerIndex: 9 },
    { field: 'oct', headerIndex: 10 },
    { field: 'nov', headerIndex: 11 },
    { field: 'dec', headerIndex: 12 },
    { field: 'jan', headerIndex: 1 },
    { field: 'feb', headerIndex: 2 },
    { field: 'mar', headerIndex: 3 },
  ]

  const columns = [
    {
      field: 'id',
      title: 'Id',
      // widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
    {
      field: 'productName',
      title: 'Particulars',
      // widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'TypeDisplayName',
      title: 'Type',
      // widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
    // {
    //   field: 'UOM',
    //   title: 'UOM',
    //   // widthT: 100,
    //   minWidth: 100,
    //   type: 'text',
    //   editable: false,
    // },
    ...monthColumns.map((month) => ({
      field: month.field,
      title: headerMap[month.headerIndex],
      // widthT: 100,
      minWidth: 120,
      type: 'select',
      options: selectOptions,
      displayMode: 'label',
      editable: true,
      format: valueFormat,
    })),
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
      const response = await ProductionNormsApiService.getManualEntry(
        keycloak,
        AOP_YEAR,
        PLANT_ID,
        'Manual Entry', // type parameter
      )

      // Extract data from response - API returns { data: [...], code: 200, message: '...' }
      const manualEntryData =
        // dummyManualEntryObject
        response?.data || []

      // Add inEdit flag to each row for edit tracking
      const dataWithEditFlag = manualEntryData.map((row) => ({
        ...row,
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

  useEffect(() => {
    if (refreshData) {
      fetchData()
    }
  }, [refreshData])

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
    // const validationError = validateRowDataWithRemarks(
    //   data,
    //   originalRows,
    //   fieldsToCheck,
    //   'productName',
    // )

    // if (validationError) {
    //   setSnackbarOpen(true)
    //   setSnackbarData({
    //     message: validationError,
    //     severity: 'error',
    //   })
    //   setLoading(false)
    //   return
    // }
    console.log('data', data)

    // Convert dropdown values and send only required fields
    const convertedData = data.map((row) => ({
      apr: row.apr,
      may: row.may,
      jun: row.jun,
      jul: row.jul,
      aug: row.aug,
      sep: row.sep,
      oct: row.oct,
      nov: row.nov,
      dec: row.dec,
      jan: row.jan,
      feb: row.feb,
      mar: row.mar,
      UOM: row.UOM || '',
      auditYear: row.auditYear || '',
      normParameterFKId: row.normParameterFKId || '',
      id: row.id || null,
      remarks: `Updated on-${new Date().toLocaleString()}`,
    }))

    console.log('convertedData', convertedData)
    try {
      // Call the API to save manual entry data
      await ProductionNormsApiService.saveManualEntry(
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
    titleName: 'Pigging/Non-Pigging-Next AOP',
    showDropdown: false,
    remarksEditable: true,
  }

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />

      <AdvanceKendoTable
        columns={columns}
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

export default ManualEntry
