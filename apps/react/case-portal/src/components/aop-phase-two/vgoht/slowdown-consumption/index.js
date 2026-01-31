import React, { useState, useEffect } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'
import { SlowdownConsumptionApiService } from '../../services/vgoht/slowdownConsumptionApiService'

const SlowdownConsumption = () => {
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
      field: 'uomMt',
      title: 'UOM / MT',
      widthT: 120,
      minWidth: 100,
      type: 'text',
      editable: false,
    },
    {
      field: 'apr',
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
      field: 'jun',
      title: headerMap[6],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'jul',
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
      field: 'mar',
      title: headerMap[3],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'remark',
      title: 'Remark',
      widthT: 200,
      minWidth: 150,
      type: 'textarea',
      editable: true,
    },
  ]

  const dummyRows = [
    {
      id: 1,
      particulars: 'Fuel Oil',
      uomMt: 'KL',
      apr: 850.5,
      may: 920.8,
      jun: 780.3,
      jul: 890.6,
      aug: 950.2,
      sep: 820.9,
      oct: 910.4,
      nov: 880.7,
      dec: 940.1,
      jan: 860.5,
      feb: 900.8,
      mar: 870.3,
      remark: 'Slowdown period consumption',
    },
    {
      id: 2,
      particulars: 'Natural Gas',
      uomMt: 'MMBTU',
      apr: 5500.0,
      may: 5750.5,
      jun: 5300.2,
      jul: 5600.8,
      aug: 5900.3,
      sep: 5450.6,
      oct: 5700.9,
      nov: 5550.4,
      dec: 5800.7,
      jan: 5650.1,
      feb: 5720.5,
      mar: 5580.2,
      remark: '',
    },
    {
      id: 3,
      particulars: 'Electricity',
      uomMt: 'MWH',
      apr: 3200.0,
      may: 3350.5,
      jun: 3100.2,
      jul: 3250.8,
      aug: 3400.3,
      sep: 3180.6,
      oct: 3320.9,
      nov: 3230.4,
      dec: 3380.7,
      jan: 3290.1,
      feb: 3340.5,
      mar: 3260.2,
      remark: 'Reduced load during slowdown',
    },
    {
      id: 4,
      particulars: 'Steam',
      uomMt: 'MT',
      apr: 8500.0,
      may: 8850.5,
      jun: 8300.2,
      jul: 8650.8,
      aug: 9000.3,
      sep: 8450.6,
      oct: 8750.9,
      nov: 8600.4,
      dec: 8900.7,
      jan: 8700.1,
      feb: 8820.5,
      mar: 8680.2,
      remark: '',
    },
    {
      id: 5,
      particulars: 'Cooling Water',
      uomMt: 'M3',
      apr: 18000.0,
      may: 19500.5,
      jun: 17500.2,
      jul: 18800.8,
      aug: 20000.3,
      sep: 17800.6,
      oct: 19200.9,
      nov: 18500.4,
      dec: 19800.7,
      jan: 18900.1,
      feb: 19300.5,
      mar: 18600.2,
      remark: 'Slowdown cooling requirements',
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
        await SlowdownConsumptionApiService.getSlowdownConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      const data = response || dummyRows
      setRows(data)
      setOriginalRows(data)
    } catch (error) {
      console.error('Error fetching slowdown consumption data:', error)
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

    try {
      await SlowdownConsumptionApiService.saveSlowdownConsumption(
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
      console.error('Error saving slowdown consumption data:', error)
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
        await SlowdownConsumptionApiService.exportSlowdownConsumption(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `Slowdown_Consumption_${AOP_YEAR}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting slowdown consumption data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remark || '')
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
    ExcelName: `Slowdown_Consumption_${AOP_YEAR}`,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Slowdown Consumption (Norm/Quantity)',
    showDropdown: false,
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

export default SlowdownConsumption
