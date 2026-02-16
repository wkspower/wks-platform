import React, { useState, useEffect } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import ValueFormatterPhaseTwo from '../../common/ValueFormatterPhaseTwo'
import { convertRowsByUnit, convertDataForSave } from './utils'
import { ProductionTargetApiService } from '../../services/vgoht/productionTargetApiService'

const MaxAchievedCapacityGrid = ({
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
      april: 140.2,
      may: 145.8,
      june: 142.5,
      july: 148.3,
      august: 150.1,
      september: 143.7,
      october: 147.2,
      november: 144.9,
      december: 149.5,
      january: 141.8,
      february: 146.3,
      march: 143.2,
      remarks: '',
    },
    {
      id: 2,
      productName: 'Toluene',
      april: 95.7,
      may: 99.2,
      june: 96.8,
      july: 100.5,
      august: 102.3,
      september: 98.1,
      october: 101.0,
      november: 99.5,
      december: 103.2,
      january: 97.4,
      february: 100.8,
      march: 98.3,
      remarks: '',
    },
    {
      id: 3,
      productName: 'Xylene',
      april: 107.5,
      may: 110.8,
      june: 108.2,
      july: 112.1,
      august: 114.3,
      september: 109.6,
      october: 112.8,
      november: 111.2,
      december: 115.0,
      january: 108.9,
      february: 111.5,
      march: 109.8,
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
      const response = await ProductionTargetApiService.getMaxAchievedCapacity(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const data = response || dummyRows
      const convertedData = convertRowsByUnit(data, selectedUnit)

      setRows(convertedData)
      setOriginalRows(convertedData)
    } catch (error) {
      console.error('Error fetching max achieved capacity data:', error)
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
      await ProductionTargetApiService.saveMaxAchievedCapacity(
        keycloak,
        PLANT_ID,
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
      console.error('Error saving max achieved capacity data:', error)
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
      link.download = `Max_Achieved_Capacity_${AOP_YEAR}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting max achieved capacity data:', error)
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
    ExcelName: `Max_Achieved_Capacity_${AOP_YEAR}`,
    showImport: false,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Max Achieved',
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

export default MaxAchievedCapacityGrid
