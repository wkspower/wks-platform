import React, { useState, useEffect, useCallback } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'
import { MonthwiseProductionPlanApiService } from '../../services/vgoht/monthwiseProductionPlanApiService'
import { monthwiseProductionPlanResponse } from '../dummyData'

// Dummy data for CDU-1 and CDU-2 plants
const DUMMY_DATA = {
  'AF3D92A2-C119-465A-A3BA-F3ED5FC17386': [ // CDU-1
    { id: 1, displayName: 'Fuel Gas', april: 12.5, may: 12.3, june: 12.7, july: 12.4, aug: 12.6, sept: 12.5, oct: 12.8, nov: 12.6, dec: 12.7, jan: 12.5, feb: 12.4, mar: 12.6 },
    { id: 2, displayName: 'LPG', april: 45.2, may: 44.8, june: 45.5, july: 45.0, aug: 45.3, sept: 45.1, oct: 45.6, nov: 45.2, dec: 45.4, jan: 45.0, feb: 44.9, mar: 45.2 },
    { id: 3, displayName: 'Naphtha', april: 78.6, may: 77.9, june: 79.2, july: 78.5, aug: 78.8, sept: 78.6, oct: 79.0, nov: 78.7, dec: 78.9, jan: 78.5, feb: 78.4, mar: 78.7 },
    { id: 4, displayName: 'LKERO', april: 65.3, may: 64.8, june: 65.7, july: 65.1, aug: 65.4, sept: 65.2, oct: 65.7, nov: 65.3, dec: 65.5, jan: 65.1, feb: 65.0, mar: 65.3 },
    { id: 5, displayName: 'HKERO', april: 54.1, may: 53.6, june: 54.5, july: 53.9, aug: 54.2, sept: 54.0, oct: 54.5, nov: 54.1, dec: 54.3, jan: 53.9, feb: 53.8, mar: 54.1 },
    { id: 6, displayName: 'Diesel', april: 120.8, may: 120.0, june: 121.5, july: 120.5, aug: 121.0, sept: 120.8, oct: 121.5, nov: 121.0, dec: 121.3, jan: 120.5, feb: 120.3, mar: 121.0 },
    { id: 7, displayName: 'Vac diesel', april: 98.7, may: 97.9, june: 99.2, july: 98.5, aug: 98.9, sept: 98.7, oct: 99.3, nov: 98.8, dec: 99.1, jan: 98.5, feb: 98.3, mar: 98.8 },
    { id: 8, displayName: 'HAGO', april: 87.6, may: 86.8, june: 88.1, july: 87.4, aug: 87.8, sept: 87.6, oct: 88.2, nov: 87.7, dec: 88.0, jan: 87.4, feb: 87.2, mar: 87.7 },
    { id: 9, displayName: 'LVGO', april: 76.5, may: 75.8, june: 77.0, july: 76.3, aug: 76.7, sept: 76.5, oct: 77.1, nov: 76.6, dec: 76.9, jan: 76.3, feb: 76.1, mar: 76.6 },
    { id: 10, displayName: 'HVGO', april: 65.4, may: 64.7, june: 65.9, july: 65.2, aug: 65.6, sept: 65.4, oct: 66.0, nov: 65.5, dec: 65.8, jan: 65.2, feb: 65.0, mar: 65.5 },
    { id: 11, displayName: 'Slop Wax', april: 43.2, may: 42.8, june: 43.5, july: 43.0, aug: 43.3, sept: 43.1, oct: 43.6, nov: 43.2, dec: 43.4, jan: 43.0, feb: 42.9, mar: 43.2 },
    { id: 12, displayName: 'Vac Resid', april: 210.5, may: 209.0, june: 211.5, july: 210.0, aug: 210.8, sept: 210.5, oct: 211.5, nov: 211.0, dec: 211.3, jan: 210.0, feb: 209.5, mar: 211.0 },
    { id: 13, displayName: 'HAGO C/O', april: 15.2, may: 15.0, june: 15.3, july: 15.1, aug: 15.2, sept: 15.1, oct: 15.3, nov: 15.2, dec: 15.3, jan: 15.1, feb: 15.0, mar: 15.2 },
    { id: 14, displayName: 'VGO drop to VR', april: 5.7, may: 5.6, june: 5.8, july: 5.7, aug: 5.8, sept: 5.7, oct: 5.8, nov: 5.7, dec: 5.8, jan: 5.7, feb: 5.6, mar: 5.7 },
  ],
  'D3F15808-5E4F-454B-9C3A-B921731EDFBD': [ // CDU-2
    { id: 15, displayName: 'Fuel Gas', april: 10.2, may: 10.0, june: 10.3, july: 10.1, aug: 10.2, sept: 10.1, oct: 10.3, nov: 10.2, dec: 10.3, jan: 10.1, feb: 10.0, mar: 10.2 },
    { id: 16, displayName: 'LPG', april: 38.5, may: 38.2, june: 38.7, july: 38.3, aug: 38.6, sept: 38.4, oct: 38.8, nov: 38.5, dec: 38.7, jan: 38.3, feb: 38.2, mar: 38.5 },
    { id: 17, displayName: 'Naphtha', april: 72.1, may: 71.5, june: 72.6, july: 71.9, aug: 72.3, sept: 72.1, oct: 72.6, nov: 72.2, dec: 72.5, jan: 71.9, feb: 71.7, mar: 72.2 },
    { id: 18, displayName: 'LKERO', april: 58.7, may: 58.2, june: 59.1, july: 58.5, aug: 58.8, sept: 58.6, oct: 59.1, nov: 58.7, dec: 59.0, jan: 58.5, feb: 58.3, mar: 58.7 },
    { id: 19, displayName: 'HKERO', april: 48.5, may: 48.1, june: 48.9, july: 48.4, aug: 48.7, sept: 48.5, oct: 48.9, nov: 48.6, dec: 48.8, jan: 48.4, feb: 48.2, mar: 48.6 },
    { id: 20, displayName: 'Diesel', april: 115.3, may: 114.5, june: 116.0, july: 115.0, aug: 115.5, sept: 115.3, oct: 116.0, nov: 115.5, dec: 115.8, jan: 115.0, feb: 114.8, mar: 115.5 },
    { id: 21, displayName: 'HAGO', april: 81.0, may: 80.3, june: 81.5, july: 80.8, aug: 81.2, sept: 81.0, oct: 81.6, nov: 81.1, dec: 81.4, jan: 80.8, feb: 80.6, mar: 81.1 },
    { id: 22, displayName: 'LVGO', april: 70.9, may: 70.2, june: 71.4, july: 70.7, aug: 71.1, sept: 70.9, oct: 71.5, nov: 71.0, dec: 71.3, jan: 70.7, feb: 70.5, mar: 71.0 },
    { id: 23, displayName: 'HVGO', april: 60.8, may: 60.1, june: 61.3, july: 60.6, aug: 61.0, sept: 60.8, oct: 61.4, nov: 61.0, dec: 61.2, jan: 60.6, feb: 60.4, mar: 61.0 },
    { id: 24, displayName: 'Slop Wax', april: 38.6, may: 38.2, june: 38.9, july: 38.4, aug: 38.7, sept: 38.5, oct: 39.0, nov: 38.6, dec: 38.8, jan: 38.4, feb: 38.3, mar: 38.6 },
    { id: 25, displayName: 'Vac Resid', april: 195.2, may: 193.5, june: 196.5, july: 194.8, aug: 195.6, sept: 195.2, oct: 196.5, nov: 195.7, dec: 196.0, jan: 194.8, feb: 194.2, mar: 195.7 },
    { id: 26, displayName: 'VGO drop to VR', april: 4.8, may: 4.7, june: 4.9, july: 4.8, aug: 4.9, sept: 4.8, oct: 4.9, nov: 4.8, dec: 4.9, jan: 4.8, feb: 4.7, mar: 4.8 },
  ]
};

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
  const [selectedUnit, setSelectedUnit] = useState('MT')
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
      fetchData()
    }
  }, [PLANT_ID, AOP_YEAR, selectedUnit])

  const fetchData = async () => {
    setLoading(true)
    try {
      // const response =
      //   await MonthwiseProductionPlanApiService.getMonthwiseProductionPlan(
      //     keycloak,
      //     PLANT_ID,
      //     AOP_YEAR,
      //   )
      const data = monthwiseProductionPlanResponse.data.aopDTOList.filter(i => i.plantFkId === PLANT_ID);
      setRows(data)
      setOriginalRows(data)
    } catch (error) {
      console.error('Error fetching monthwise production plan data:', error)
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
        setCurrentRowId={() => { }}
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
