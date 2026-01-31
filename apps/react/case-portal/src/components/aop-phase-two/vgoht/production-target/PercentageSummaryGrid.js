import React, { useState, useEffect } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'

const PercentageSummaryGrid = ({
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
  loading,
  setLoading,
}) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})

  const valueFormat = ValueFormatterPhaseTwo()
  const headerMap = generateHeaderNames(AOP_YEAR)

  const columns = [
    {
      field: 'productName',
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

  // Percentage summary - calculated from Proposed Operating Capacity vs Max Achieved Capacity
  const dummyRows = [
    {
      id: 1,
      productName: 'Benzene',
      april: 94.73,
      may: 94.31,
      june: 94.88,
      july: 94.27,
      august: 94.67,
      september: 94.89,
      october: 95.31,
      november: 95.24,
      december: 94.92,
      january: 94.85,
      february: 94.81,
      march: 95.02,
    },
    {
      id: 2,
      productName: 'Toluene',
      april: 94.56,
      may: 94.76,
      june: 94.83,
      july: 94.82,
      august: 95.01,
      september: 94.9,
      october: 95.05,
      november: 94.97,
      december: 95.06,
      january: 94.87,
      february: 95.04,
      march: 94.91,
    },
    {
      id: 3,
      productName: 'Xylene',
      april: 94.7,
      may: 94.95,
      june: 95.2,
      july: 95.27,
      august: 95.45,
      september: 95.35,
      october: 95.39,
      november: 95.32,
      december: 95.65,
      january: 95.23,
      february: 95.34,
      march: 95.45,
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
      // Simulate API call - replace with actual API when available
      // This grid shows percentage of Proposed Operating Capacity vs Max Achieved Capacity
      // const response = await DataService.getPercentageSummaryData(keycloak, PLANT_ID, AOP_YEAR)

      setRows(dummyRows)
      setOriginalRows(dummyRows)
    } catch (error) {
      console.error('Error fetching percentage summary data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error fetching data',
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
      // Simulate export - replace with actual API when available
      // await DataService.exportPercentageSummaryData(keycloak, PLANT_ID, AOP_YEAR)

      setTimeout(() => {
        setSnackbarData({
          message: 'Excel download completed successfully!',
          severity: 'success',
        })
      }, 1000)
    } catch (error) {
      console.error('Error exporting percentage summary data:', error)
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
    ExcelName: `Percentage_Summary_${AOP_YEAR}`,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Percentage Summary',
    showDropdown: false,
  }

  return (
    <AdvanceKendoTable
      columns={columns}
      rows={rows}
      setRows={setRows}
      modifiedCells={modifiedCells}
      setModifiedCells={setModifiedCells}
      title={permissions.showTitle ? permissions.titleName : ''}
      permissions={permissions}
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
  )
}

export default PercentageSummaryGrid
