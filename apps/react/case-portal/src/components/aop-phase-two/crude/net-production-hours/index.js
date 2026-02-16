import React, { useState, useEffect } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'
import { NetProductionHoursApiService } from '../../services/vgoht/netProductionHoursApiService'

const NetProductionHours = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
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
      widthT: 300,
      minWidth: 250,
      type: 'text',
      editable: false,
      locked: true,
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
      particulars: 'Total Available Hours for the Month',
      apr: 720.0,
      may: 744.0,
      jun: 720.0,
      jul: 744.0,
      aug: 744.0,
      sep: 720.0,
      oct: 744.0,
      nov: 720.0,
      dec: 744.0,
      jan: 744.0,
      feb: 672.0,
      mar: 744.0,
    },
    {
      id: 2,
      particulars: 'Shutdown Hours',
      apr: 0.0,
      may: 0.0,
      jun: 0.0,
      jul: 0.0,
      aug: 0.0,
      sep: 0.0,
      oct: 0.0,
      nov: 0.0,
      dec: 0.0,
      jan: 0.0,
      feb: 0.0,
      mar: 0.0,
    },
    {
      id: 3,
      particulars: 'Non Shutdown Operating Hours',
      apr: 720.0,
      may: 744.0,
      jun: 720.0,
      jul: 744.0,
      aug: 744.0,
      sep: 720.0,
      oct: 744.0,
      nov: 720.0,
      dec: 744.0,
      jan: 744.0,
      feb: 672.0,
      mar: 744.0,
    },
    {
      id: 4,
      particulars: 'Slowdown Hours',
      apr: 0.0,
      may: 0.0,
      jun: 0.0,
      jul: 0.0,
      aug: 0.0,
      sep: 0.0,
      oct: 0.0,
      nov: 0.0,
      dec: 0.0,
      jan: 0.0,
      feb: 0.0,
      mar: 0.0,
    },
    {
      id: 7,
      particulars: 'Plant Operating Hours',
      apr: 720.0,
      may: 744.0,
      jun: 720.0,
      jul: 744.0,
      aug: 744.0,
      sep: 720.0,
      oct: 744.0,
      nov: 720.0,
      dec: 744.0,
      jan: 744.0,
      feb: 672.0,
      mar: 744.0,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      // fetchData()
      setRows(dummyRows)
      setOriginalRows(dummyRows)
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchData = async () => {
    setLoading(true)
    try {
      const response = await NetProductionHoursApiService.getNetProductionHours(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const data = response || dummyRows
      setRows(data)
      setOriginalRows(data)
    } catch (error) {
      console.error('Error fetching net production hours data:', error)
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
      // Simulate API call - replace with actual API when available
      // await DataService.saveNetProductionHoursData(keycloak, PLANT_ID, AOP_YEAR, modifiedData)

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      setOriginalRows(rows)
    } catch (error) {
      console.error('Error saving net production hours data:', error)
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
      const blob = await NetProductionHoursApiService.exportNetProductionHours(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `Net_Production_Hours_${AOP_YEAR}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting net production hours data:', error)
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
    allAction: true,
    showExport: true,
    ExcelName: `Net_Production_Hours_${AOP_YEAR}`,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Net Production Hours',
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
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
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

export default NetProductionHours
