import React, { useState, useEffect, useCallback } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'
import { OverallAopConsumptionApiService } from '../../services/vgoht/overallAopConsumptionApiService'

const OverallAopConsumption = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const valueFormat = ValueFormatterPhaseTwo()
  const headerMap = generateHeaderNames(AOP_YEAR)

  const columns = [
    {
      field: 'particulars',
      title: 'Particulars',
      widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'uom',
      title: 'UOM',
      widthT: 100,
      minWidth: 80,
      type: 'text',
      editable: false,
    },
    {
      field: 'apr',
      title: headerMap[4],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'jun',
      title: headerMap[6],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'jul',
      title: headerMap[7],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'aug',
      title: headerMap[8],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'sep',
      title: headerMap[9],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'oct',
      title: headerMap[10],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'nov',
      title: headerMap[11],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'dec',
      title: headerMap[12],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'jan',
      title: headerMap[1],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'feb',
      title: headerMap[2],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'mar',
      title: headerMap[3],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
  ]

  const dummyRows = [
    {
      id: 1,
      particulars: 'Fuel Oil',
      uom: 'KL',
      apr: 1250.5,
      may: 1320.8,
      jun: 1180.3,
      jul: 1290.6,
      aug: 1350.2,
      sep: 1220.9,
      oct: 1310.4,
      nov: 1280.7,
      dec: 1340.1,
      jan: 1260.5,
      feb: 1300.8,
      mar: 1270.3,
    },
    {
      id: 2,
      particulars: 'Natural Gas',
      uom: 'MMBTU',
      apr: 8500.0,
      may: 8750.5,
      jun: 8300.2,
      jul: 8600.8,
      aug: 8900.3,
      sep: 8450.6,
      oct: 8700.9,
      nov: 8550.4,
      dec: 8800.7,
      jan: 8650.1,
      feb: 8720.5,
      mar: 8580.2,
    },
    {
      id: 3,
      particulars: 'Electricity',
      uom: 'MWH',
      apr: 4500.0,
      may: 4650.5,
      jun: 4400.2,
      jul: 4550.8,
      aug: 4700.3,
      sep: 4480.6,
      oct: 4620.9,
      nov: 4530.4,
      dec: 4680.7,
      jan: 4590.1,
      feb: 4640.5,
      mar: 4560.2,
    },
    {
      id: 4,
      particulars: 'Steam',
      uom: 'MT',
      apr: 12500.0,
      may: 12850.5,
      jun: 12300.2,
      jul: 12650.8,
      aug: 13000.3,
      sep: 12450.6,
      oct: 12750.9,
      nov: 12600.4,
      dec: 12900.7,
      jan: 12700.1,
      feb: 12820.5,
      mar: 12680.2,
    },
    {
      id: 5,
      particulars: 'Cooling Water',
      uom: 'M3',
      apr: 25000.0,
      may: 26500.5,
      jun: 24500.2,
      jul: 25800.8,
      aug: 27000.3,
      sep: 24800.6,
      oct: 26200.9,
      nov: 25500.4,
      dec: 26800.7,
      jan: 25900.1,
      feb: 26300.5,
      mar: 25600.2,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      // fetchData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchData = async () => {
    setLoading(true)
    try {
      const response =
        await OverallAopConsumptionApiService.getOverallAopConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      const data = response || dummyRows
      setRows(data)
    } catch (error) {
      console.error('Error fetching overall AOP consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error fetching data',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleCalculate = async () => {
    setLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Calculating...',
      severity: 'info',
    })

    try {
      const calculatedData =
        await OverallAopConsumptionApiService.calculateOverallAopConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      setRows(calculatedData)
      setSnackbarData({
        message: 'Calculation completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error calculating overall AOP consumption:', error)
      setSnackbarData({
        message: 'Calculation failed. Please try again.',
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
        await OverallAopConsumptionApiService.exportOverallAopConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `Overall_AOP_Consumption_${AOP_YEAR}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting overall AOP consumption data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  const permissions = {
    showAction: false,
    addButton: false,
    deleteButton: false,
    editButton: false,
    saveBtn: false,
    allAction: false,
    showExport: true,
    showCalculate: true,
    ExcelName: `Overall_AOP_Consumption_${AOP_YEAR}`,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Overall AOP Consumption (Norm/Quantity)',
    showDropdown: false,
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
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
        handleExport={handleExport}
        handleCalculate={handleCalculate}
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

export default OverallAopConsumption
