import React, { useState, useEffect } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'
import { NetProductionHoursApiService } from '../../services/vgoht/netProductionHoursApiService'

const dummyRows = [
  {
    Name: 'Total Available Hours for the Month',
    April: 720,
    May: 744,
    June: 720,
    July: 744,
    Aug: 744,
    Sep: 720,
    Oct: 744,
    Nov: 720,
    Dec: 744,
    Jan: 744,
    Feb: 672,
    Mar: 744,
  },
  {
    Name: 'Shutdown Hours',
    April: 0,
    May: 0,
    June: 0,
    July: 0,
    Aug: 0,
    Sep: 0,
    Oct: 0,
    Nov: 0,
    Dec: 0,
    Jan: 0,
    Feb: 0,
    Mar: 0,
  },
  {
    Name: 'Non-Shutdown Operating Hours for the Month',
    April: 720,
    May: 744,
    June: 720,
    July: 744,
    Aug: 744,
    Sep: 720,
    Oct: 744,
    Nov: 720,
    Dec: 744,
    Jan: 744,
    Feb: 672,
    Mar: 744,
  },
  {
    Name: 'Slowdown Hours',
    April: 0,
    May: 0,
    June: 0,
    July: 0,
    Aug: 0,
    Sep: 0,
    Oct: 0,
    Nov: 0,
    Dec: 0,
    Jan: 0,
    Feb: 0,
    Mar: 0,
  },
  {
    Name: 'Avg Slow Down Load PVT',
    April: 0,
    May: 0,
    June: 0,
    July: 0,
    Aug: 0,
    Sep: 0,
    Oct: 0,
    Nov: 0,
    Dec: 0,
    Jan: 0,
    Feb: 0,
    Mar: 0,
  },
  {
    Name: 'Slow Down Load Reduction',
    April: 0,
    May: 0,
    June: 0,
    July: 0,
    Aug: 0,
    Sep: 0,
    Oct: 0,
    Nov: 0,
    Dec: 0,
    Jan: 0,
    Feb: 0,
    Mar: 0,
  },
  {
    Name: 'Plant Operating Hours',
    April: 720,
    May: 744,
    June: 720,
    July: 744,
    Aug: 744,
    Sep: 720,
    Oct: 744,
    Nov: 720,
    Dec: 744,
    Jan: 744,
    Feb: 672,
    Mar: 744,
  },
]

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
      field: 'Name',
      title: 'Particulars',
      widthT: 300,
      minWidth: 250,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'April',
      title: headerMap[4],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'May',
      title: headerMap[5],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'June',
      title: headerMap[6],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'July',
      title: headerMap[7],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'Aug',
      title: headerMap[8],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'Sep',
      title: headerMap[9],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'Oct',
      title: headerMap[10],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'Nov',
      title: headerMap[11],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'Dec',
      title: headerMap[12],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'Jan',
      title: headerMap[1],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'Feb',
      title: headerMap[2],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'Mar',
      title: headerMap[3],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      // fetchData()
      setRows(dummyRows)
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
