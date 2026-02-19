import React, { useState, useEffect, useCallback } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'
import { OverallAopConsumptionApiService } from '../../services/vgoht/overallAopConsumptionApiService'
import { overAllAOpResponse } from '../dummyData'

const OverallAopConsumptionFCC = () => {
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
      field: 'productName',
      title: 'Particulars',
      widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'normParameterTypeDisplayName',
      title: 'normParameterTypeDisplayName',
      widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
    {
      field: 'UOM',
      title: 'UOM',
      widthT: 100,
      minWidth: 80,
      type: 'text',
      editable: false,
    },
    {
      field: 'april',
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
      field: 'june',
      title: headerMap[6],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'july',
      title: headerMap[7],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'august',
      title: headerMap[8],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'september',
      title: headerMap[9],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'october',
      title: headerMap[10],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'november',
      title: headerMap[11],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'december',
      title: headerMap[12],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'january',
      title: headerMap[1],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'february',
      title: headerMap[2],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: false,
      format: valueFormat,
    },
    {
      field: 'march',
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
      fetchData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchData = async () => {
    setLoading(true)
    try {
      // const response =
      //   await OverallAopConsumptionApiService.getOverallAopConsumption(
      //     keycloak,
      //     PLANT_ID,
      //     AOP_YEAR,
      //   )

      const response = overAllAOpResponse.data?.mcuNormsValueDTOList?.map(
        (item) => {
          return {
            ...item,
            isEditaable: false,
          }
        },
      )
      setRows(response)
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
        customHeight={70}
        groupBy={['normParameterTypeDisplayName']}
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

export default OverallAopConsumptionFCC
