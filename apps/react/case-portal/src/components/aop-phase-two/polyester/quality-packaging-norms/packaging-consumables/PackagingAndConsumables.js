import React, { useCallback, useEffect, useState } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { getRoleName } from 'services/role-service'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { validateFields } from 'utils/validationUtils'
import { QualityPackagingNormsApiService } from '../../../services/polyester/qualityPackagingNormsApiService'
import AdvanceKendoTable from '../../../common/AdvanceKendoTable'

const PackagingAndConsumables = ({ refreshTrigger, triggerRefresh }) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, oldYear, plantObject, siteObject, year, isReleased } =
    dataGridStore

  const PLANT_ID = plantObject?.id
  const PLANT_NAME = plantObject?.name
  const SITE_NAME = siteObject?.name
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const AOP_YEAR = year?.selectedYear
  const IS_OLD_YEAR = oldYear?.oldYear
  const IS_RELEASED = isReleased

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, IS_RELEASED)
  const valueFormat = ValueFormatterConsumption()

  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [calculationObject, setCalculationObject] = useState([])

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  function getPreviousYear(aopYear) {
    if (!aopYear) return ''
    const [start, end] = aopYear.split('-').map((s) => s.trim())
    const prevStart = parseInt(start, 10) - 1
    const prevEnd = (parseInt(end, 10) === 0 ? 99 : parseInt(end, 10) - 1)
      .toString()
      .padStart(2, '0')
    return `${prevStart}-${prevEnd}`
  }
  const previousYear = getPreviousYear(AOP_YEAR)

  const columns = [
    {
      field: 'sno',
      title: 'S.no',
      minWidth: 70,
      format: '{0:n0}',
      editable: false,
      type: 'number',
    },
    {
      field: 'sapMaterialCode',
      title: 'SAP Material Code',
      editable: false,
      minWidth: 100,
    },
    {
      field: 'name',
      title: 'Name of Item',
      editable: false,
      minWidth: 100,
    },
    {
      field: 'unit',
      title: 'Unit',
      minWidth: 70,
      editable: false,
    },
    {
      field: 'packagingPrice',
      title: 'Packaging Price (Rs)',
      editable: true,
      type: 'number',
      format: valueFormat,
      minWidth: 100,
    },
    {
      field: 'budget',
      title: `Budget ${previousYear}`,
      editable: false,
      type: 'number',
      format: valueFormat,
      minWidth: 100,
    },
    {
      field: 'actual',
      title: `Actual ${previousYear}`,
      editable: true,
      type: 'number',
      format: valueFormat,
      minWidth: 100,
    },
    {
      field: 'proposedNorm',
      title: `Proposed Norm ${AOP_YEAR}`,
      editable: true,
      type: 'number',
      format: valueFormat,
      minWidth: 100,
    },
    {
      field: 'remark',
      title: 'Remark',
      type: 'textarea',
      editable: true,
      minWidth: 100,
    },
  ]

  const fetchPackagingRows = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) {
      setRows([])
      return
    }
    setLoading(true)
    try {
      const res = await QualityPackagingNormsApiService.getPackagingConsumables(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      setCalculationObject(res?.data?.aopCalculation)
      if (res?.code === 200 && Array.isArray(res?.data?.data)) {
        const mappedRows = res.data.data.map((item, idx) => ({
          id: item.id || idx,
          sno: idx + 1,
          materialId: item.materialId,
          name: item.displayName,
          unit: item.uom,
          packagingPrice: item.packagingPrice,
          budget: item.prevBudget,
          actual: item.prevActual,
          proposedNorm: item.proposedNorm,
          sapMaterialCode: item.sapMaterialCode,
          Particulars: item.normParameterTypeName,
          originalRemark: item.remark,
          remark: item.remark,
        }))
        setRows(mappedRows)
      } else {
        setRows([])
      }
    } catch (err) {
      console.error('fetchPackagingRows error', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    fetchPackagingRows()
  }, [fetchPackagingRows, refreshTrigger])

  const saveChanges = useCallback(async () => {
    try {
      const data = Object.values(modifiedCells)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }
      const requiredFields = ['remark']
      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

      const packagingConsumbleDTOList = data.map((row) => ({
        id: typeof row.id === 'number' ? null : row.id,
        materialId: row.materialId,
        displayName: row.name,
        uom: row.unit,
        packagingPrice: row.packagingPrice,
        prevBudget: row.budget,
        prevActual: row.actual,
        proposedNorm: row.proposedNorm,
        plantId: PLANT_ID,
        aopYear: AOP_YEAR,
        remark: row.remark || '',
        normParameterTypeName: 'packaging And Consumables',
      }))

      setLoading(true)
      const res =
        await QualityPackagingNormsApiService.savePackagingConsumables(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          packagingConsumbleDTOList,
        )

      if (res?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchPackagingRows()
      } else {
        setLoading(false)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Save Failed!',
          severity: 'error',
        })
      }
    } catch (err) {
      setLoading(false)
      console.error('Error while saving packaging data', err)
      setSnackbarOpen(true)
      setSnackbarData({ message: err.message, severity: 'error' })
    } 
  }, [modifiedCells, keycloak, PLANT_ID, AOP_YEAR, fetchPackagingRows])

  const handleCalculate = async () => {
    setLoading(true)
    try {
      const data = await QualityPackagingNormsApiService.calculatePackagingData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (data || data === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        
        fetchPackagingRows()
      } else {
        setLoading(false)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      setLoading(false)
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })
      console.error('Error calculating packaging data:', error)
    }
  }

  const handleExcelUpload = async (rawFile) => {
    setLoading(true)
    try {
      const response =
        await QualityPackagingNormsApiService.importPackagingConsumables(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          rawFile,
        )
      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchPackagingRows()
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteNumbers = Array.from(byteCharacters, (char) =>
          char.charCodeAt(0),
        )
        const byteArray = new Uint8Array(byteNumbers)
        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `Error File - Packaging.xlsx`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        fetchPackagingRows()
      } else {
        setLoading(false)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      setLoading(false)
      console.error('Error uploading excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    }
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })
    try {
      const EXCEL_EXPORT_TITLE = `${vertName}_${SITE_NAME}_${PLANT_NAME}_Packaging & Consumables`
      await QualityPackagingNormsApiService.exportPackagingConsumables(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        EXCEL_EXPORT_TITLE,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Excel downloaded successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error downloading Excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    }
  }

  const handleRemarkCellClick = useCallback(
    (row) => {
      if (READ_ONLY) return
      setCurrentRemark(row.remark || '')
      setCurrentRowId(row.id)
      setRemarkDialogOpen(true)
    },
    [READ_ONLY],
  )

  
  const permissions = {
    allAction: true,
    saveBtn: true,
    showTitleNameBusiness: true,
    titleName: 'Packaging & Consumables',
    showExport: true,
    showImport: true,
    ExcelName: `${lowerVertName}_Packaging & Consumables`,
    addButton: false,
    deleteButton: false,
    showCalculate: true,
    calculateDisabled: !(
      calculationObject && Object.keys(calculationObject).length > 0
    ),
    showTitle: true,
  }

  return (
    <Box>
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        title='Packaging & Consumables'
        loading={loading}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        permissions={permissions}
        saveChanges={saveChanges}
        handleCalculate={handleCalculate}
        handleRemarkCellClick={handleRemarkCellClick}
        handleExport={handleExport}
        handleExcelUpload={handleExcelUpload}
        groupBy='Particulars'
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        snackbarData={snackbarData}
        setSnackbarData={setSnackbarData}
      />
    </Box>
  )
}

export default PackagingAndConsumables
