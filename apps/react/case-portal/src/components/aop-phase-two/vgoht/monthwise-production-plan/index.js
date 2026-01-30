import React, { useState, useEffect, useCallback } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'
import { MonthwiseProductionPlanApiService } from '../../services/vgoht/monthwiseProductionPlanApiService'

const MonthwiseProductionPlan = () => {
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
  const [selectedUnit, setSelectedUnit] = useState('TPH')
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const valueFormat = ValueFormatterPhaseTwo()
  const headerMap = generateHeaderNames(AOP_YEAR)

  // Dropdown configuration for unit selection
  const dropdownConfig = {
    options: [
      { id: 'TPH', name: 'TPH' },
      { id: 'TPD', name: 'TPD' },
      { id: 'MT', name: 'MT' },
      { id: 'KT', name: 'KT' },
    ],
    label: 'Select Unit',
    placeholder: 'Select',
    valueKey: 'id',
    labelKey: 'name',
  }

  const columns = [
    {
      field: 'displayName',
      title: 'Product Name',
      widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'april',
      title: headerMap[4],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'june',
      title: headerMap[6],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'july',
      title: headerMap[7],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'aug',
      title: headerMap[8],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'sep',
      title: headerMap[9],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'oct',
      title: headerMap[10],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'nov',
      title: headerMap[11],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'dec',
      title: headerMap[12],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'jan',
      title: headerMap[1],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'feb',
      title: headerMap[2],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'march',
      title: headerMap[3],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'averageTPH',
      title: 'Average TPH',
      widthT: 120,
      minWidth: 100,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'aopRemarks',
      title: 'Remark',
      widthT: 150,
      minWidth: 120,
      type: 'textarea',
      editable: true,
    },
  ]

  // Dummy data for aromatics products
  const dummyRows = [
    {
      id: 1,
      displayName: 'Benzene',
      april: 125.5,
      may: 130.2,
      june: 128.8,
      july: 132.1,
      aug: 135.4,
      sep: 127.9,
      oct: 131.5,
      nov: 129.3,
      dec: 133.7,
      jan: 126.8,
      feb: 130.9,
      march: 128.4,
      averageTPH: 130.0,
      aopRemarks: '',
    },
    {
      id: 2,
      displayName: 'Toluene',
      april: 85.3,
      may: 88.7,
      june: 86.2,
      july: 89.5,
      aug: 91.2,
      sep: 87.4,
      oct: 90.1,
      nov: 88.3,
      dec: 92.0,
      jan: 86.9,
      feb: 89.4,
      march: 87.6,
      averageTPH: 88.5,
      aopRemarks: '',
    },
    {
      id: 3,
      displayName: 'Xylene',
      april: 95.8,
      may: 98.2,
      june: 96.5,
      july: 99.3,
      aug: 101.5,
      sep: 97.1,
      oct: 100.2,
      nov: 98.7,
      dec: 102.3,
      jan: 96.3,
      feb: 99.1,
      march: 97.4,
      averageTPH: 98.5,
      aopRemarks: '',
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      // fetchData()
    }
  }, [PLANT_ID, AOP_YEAR, selectedUnit])

  const fetchData = async () => {
    setLoading(true)
    try {
      const response =
        await MonthwiseProductionPlanApiService.getMonthwiseProductionPlan(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      const data = response || dummyRows
      setRows(data)
      setOriginalRows(data)
    } catch (error) {
      console.error('Error fetching monthwise production plan data:', error)
      setRows(dummyRows)
      setOriginalRows(dummyRows)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error fetching data, using dummy data',
        severity: 'warning',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleCalculate = useCallback(async () => {
    setLoading(true)
    try {
      const calculatedData =
        await MonthwiseProductionPlanApiService.calculateMonthwiseProductionPlan(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      setRows(calculatedData)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Data calculated successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error calculating monthwise production plan:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error calculating data',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

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

    try {
      await MonthwiseProductionPlanApiService.saveMonthwiseProductionPlan(
        keycloak,
        AOP_YEAR,
        modifiedData,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      setOriginalRows(rows)
    } catch (error) {
      console.error('Error saving monthwise production plan data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving data!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel export started!',
      severity: 'info',
    })

    try {
      const blob =
        await MonthwiseProductionPlanApiService.exportMonthwiseProductionPlan(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `Monthwise_Production_Plan_${AOP_YEAR}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting monthwise production plan data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.aopRemarks || '')
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
    showExport: true,
    showCalculate: true,
    ExcelName: `Monthwise_Production_Plan_${AOP_YEAR}`,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: `Month wise Production plan (${selectedUnit})`,
    showDropdown: true,
    remarksEditable: true,
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

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
        handleExport={handleExport}
        handleCalculate={handleCalculate}
        dropdownConfig={dropdownConfig}
        selectedDropdownValue={selectedUnit}
        setSelectedDropdownValue={setSelectedUnit}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
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

export default MonthwiseProductionPlan
