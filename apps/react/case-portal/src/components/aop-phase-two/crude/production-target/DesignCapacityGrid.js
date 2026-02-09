import React, { useState, useEffect } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'
import { convertRowsByUnit, convertDataForSave } from './utils'
import { ProductionTargetApiService } from '../../services/vgoht/productionTargetApiService'

const DesignCapacityGrid = ({
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
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [selectedUnit, setSelectedUnit] = useState('TPH')

  const valueFormat = ValueFormatterPhaseTwo()
  const headerMap = generateHeaderNames(AOP_YEAR)

  const dropdownConfig = {
    options: [
      { id: 'TPH', name: 'TPH' },
      { id: 'TPD', name: 'TPD' },
    ],
    label: 'Select Unit',
    placeholder: 'Select',
    valueKey: 'id',
    labelKey: 'name',
  }

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
      field: 'august',
      title: headerMap[8],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'september',
      title: headerMap[9],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'october',
      title: headerMap[10],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'november',
      title: headerMap[11],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'december',
      title: headerMap[12],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'january',
      title: headerMap[1],
      widthT: 100,
      minWidth: 80,
      type: 'number1',
      editable: true,
      format: valueFormat,
    },
    {
      field: 'february',
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
      field: 'remarks',
      title: 'Remark',
      widthT: 150,
      minWidth: 120,
      type: 'textarea',
      editable: true,
    },
  ]

  const dummyRows = [
    {
      id: 1,
      productName: 'Benzene',
      april: 125.5,
      may: 130.2,
      june: 128.8,
      july: 132.1,
      august: 135.4,
      september: 127.9,
      october: 131.5,
      november: 129.3,
      december: 133.7,
      january: 126.8,
      february: 130.9,
      march: 128.4,
      remarks: '',
    },
    {
      id: 2,
      productName: 'Toluene',
      april: 85.3,
      may: 88.7,
      june: 86.2,
      july: 89.5,
      august: 91.2,
      september: 87.4,
      october: 90.1,
      november: 88.3,
      december: 92.0,
      january: 86.9,
      february: 89.4,
      march: 87.6,
      remarks: '',
    },
    {
      id: 3,
      productName: 'Xylene',
      april: 95.8,
      may: 98.2,
      june: 96.5,
      july: 99.3,
      august: 101.5,
      september: 97.1,
      october: 100.2,
      november: 98.7,
      december: 102.3,
      january: 96.3,
      february: 99.1,
      march: 97.4,
      remarks: '',
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
      const response = await ProductionTargetApiService.getDesignCapacity(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const data = response || dummyRows
      const convertedData = convertRowsByUnit(data, selectedUnit)

      setRows(convertedData)
      setOriginalRows(convertedData)
    } catch (error) {
      console.error('Error fetching design capacity data:', error)
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
      const dataToSave = convertDataForSave(modifiedData, selectedUnit)
      await ProductionTargetApiService.saveDesignCapacity(
        keycloak,
        AOP_YEAR,
        dataToSave,
      )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Data saved successfully!',
        severity: 'success',
      })
      setModifiedCells({})
      setOriginalRows(rows)
    } catch (error) {
      console.error('Error saving design capacity data:', error)
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
      const blob = await ProductionTargetApiService.exportProductionTarget(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `Design_Capacity_${AOP_YEAR}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting design capacity data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
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
    showExport: true,
    ExcelName: `Design_Capacity_${AOP_YEAR}`,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Design Capacity',
    showDropdown: true,
    remarksEditable: true,
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
      handleRemarkCellClick={handleRemarkCellClick}
      remarkDialogOpen={remarkDialogOpen}
      setRemarkDialogOpen={setRemarkDialogOpen}
      currentRemark={currentRemark}
      setCurrentRemark={setCurrentRemark}
      currentRowId={currentRowId}
      setCurrentRowId={() => {}}
      saveChanges={saveChanges}
      handleExport={handleExport}
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
  )
}

export default DesignCapacityGrid
