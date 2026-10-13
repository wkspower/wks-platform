import { Box } from '@mui/material'
import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/refineryUtility/productionNormsApiService'
import { validateFields } from 'utils/validationUtils'
import { getRoleName } from 'services/role-service'
import { useSession } from 'SessionStoreContext'
import Notification from 'components/Utilities/Notification'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'

const VendorConsumption = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year, oldYear, isReleased } =
    dataGridStore

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const IS_OLD_YEAR = oldYear?.oldYear
  const IS_RELEASED = isReleased
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, IS_RELEASED)
  const valueFormat = ValueFormatterPhaseTwo()

  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [isTwoColumnPlant, setIsTwoColumnPlant] = useState(false)
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  useEffect(() => {
    const fetchPlantColumnConfig = async () => {
      if (!PLANT_ID) return
      try {
        const res = await ProductionNormsApiService.checkIsSummerWinterPlant(keycloak, PLANT_ID)
        setIsTwoColumnPlant(Boolean(res?.data?.isSummerWinter ?? res?.data?.isTwoColumn))
      } catch (err) {
        console.error('Error checking plant column config:', err)
        setIsTwoColumnPlant(false)
      }
    }
    if (PLANT_ID) {
      fetchPlantColumnConfig()
    }
  }, [PLANT_ID, keycloak])

  const fetchVendorData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      const response = await ProductionNormsApiService.getVendorConsumptionData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (response?.code === 200) {
        const formattedData = (response?.data || []).map((item, index) => {
          const val =
            item?.april !== undefined && item?.april !== null
              ? item.april
              : item?.value !== undefined && item?.value !== null
              ? item.value
              : ''
          const uomVal = item?.uom || item?.UOM || ''
          const nameVal =
            item?.displayName || item?.name || item?.productName || ''
          const groupName =
            item?.normTypeName ||
            item?.TypeDisplayName ||
            'Treatment Vendor Consumption'

          return {
            ...item,
            idFromApi: item?.normParameterFKId,
            id: index,
            Type: groupName,
            ParticularG: groupName,
            name: item?.name || nameVal,
            displayName: nameVal,
            productName: nameVal,
            uom: uomVal,
            UOM: uomVal,
            april: val,
            value: val,
            remarks: item?.remarks || '',
            originalRemark: item?.remarks || '',
            isEditable: item?.isEditable ?? true,
          }
        })
        setRows(formattedData)
      } else {
        setRows([])
      }
    } catch (error) {
      console.error('Error fetching Treatment Vendor Consumption data:', error)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID, AOP_YEAR])

  useEffect(() => {
    setRows([])
    setModifiedCells({})
    fetchVendorData()
  }, [fetchVendorData, PLANT_ID, AOP_YEAR])

  const colDefs = useMemo(
    () => [
      {
        field: 'ParticularG',
        title: 'Particulars',
        editable: false,
        width: 300,
        minWidth: 250,
        widthT: 300,
        locked: true,
        hidden: true,
      },
      {
        field: 'displayName',
        title: 'Particulars',
        editable: false,
        width: 300,
        minWidth: 250,
        widthT: 300,
      },
      {
        field: 'uom',
        title: 'UOM',
        editable: false,
        width: 100,
        minWidth: 80,
        widthT: 100,
      },
      {
        field: 'april',
        title: 'Value',
        width: 150,
        minWidth: 120,
        widthT: 150,
        type: 'number',
        format: valueFormat,
        editable: true,
      },
      {
        field: 'remarks',
        title: 'Remark',
        editable: true,
        width: 300,
        minWidth: 250,
        widthT: 300,
      },
    ],
    [valueFormat],
  )

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveChanges = useCallback(async () => {
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) return
    const requiredFields = ['remarks']
    const validationData = modifiedData.map((row) => ({
      ...row,
    }))
    const validationMessage = validateFields(validationData, requiredFields)
    if (validationMessage) {
      setSnackbarData({ message: validationMessage, severity: 'error' })
      setSnackbarOpen(true)
      return
    }

    setLoading(true)
    try {
      const payload = modifiedData.map((row) => {
        const numericVal =
          row.april !== undefined && row.april !== '' && row.april !== null
            ? Number(row.april)
            : row.value !== undefined && row.value !== '' && row.value !== null
            ? Number(row.value)
            : null

        return {
          normParameterFKId: row.normParameterFKId || row.idFromApi,
          name: row.name || row.displayName || '',
          displayName: row.displayName || row.name || '',
          uom: row.uom || row.UOM || '',
          normTypeName: row.normTypeName || row.ParticularG || '',
          april: numericVal,
          value: numericVal,
          auditYear: row.auditYear || AOP_YEAR,
          remarks: row.remarks || '',
          displayOrder: row.displayOrder ?? null,
          isEditable: row.isEditable ?? true,
        }
      })

      const response = await ProductionNormsApiService.saveVendorConsumptionData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )
      if (response?.code === 200 || response?.success) {
        setSnackbarData({
          message: response?.message || 'Saved Successfully!',
          severity: 'success',
        })
        setSnackbarOpen(true)
        setModifiedCells({})
        fetchVendorData()
      } else if (response?.code === 400) {
        setSnackbarData({
          message: response?.message || 'Partial Data Updated',
          severity: 'warning',
        })
        setSnackbarOpen(true)
        setModifiedCells({})
        fetchVendorData()
      } else {
        setSnackbarData({
          message: response?.message || 'Save Failed!',
          severity: 'error',
        })
        setSnackbarOpen(true)
      }
    } catch (error) {
      console.error('Error saving Treatment Vendor Consumption data:', error)
      setSnackbarData({
        message: error?.message || 'Error saving data',
        severity: 'error',
      })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, PLANT_ID, AOP_YEAR, keycloak, fetchVendorData])

  const handleExcelUpload = async (file) => {
    setLoading(true)
    try {
      const response =
        await ProductionNormsApiService.importVendorConsumptionExcel(
          file,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      if (response?.code === 200) {
        setSnackbarData({
          message: 'Imported Successfully!',
          severity: 'success',
        })
        setSnackbarOpen(true)
        fetchVendorData()
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
        link.setAttribute('download', 'Error File - Treatment Vendor Consumption.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        fetchVendorData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error importing Treatment Vendor Consumption data:', error)
      setSnackbarData({ message: 'Error importing data', severity: 'error' })
      setSnackbarOpen(true)
    } finally {
      setLoading(false)
    }
  }

  const downloadExcelForConfiguration = async () => {
    try {
      setSnackbarData({ message: 'Export Started!', severity: 'success' })
      setSnackbarOpen(true)
      const excelName = `${verticalObject?.name}_${siteObject?.name}_${plantObject?.name}_Treatment Vendor Consumption`
      await ProductionNormsApiService.exportVendorConsumptionExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        excelName,
      )
      setSnackbarData({ message: 'Export Successful!', severity: 'success' })
      setSnackbarOpen(true)
    } catch (error) {
      console.error('Error exporting Treatment Vendor Consumption data:', error)
      setSnackbarData({ message: 'Error exporting data', severity: 'error' })
      setSnackbarOpen(true)
    }
  }

  const adjustedPermissions = useMemo(() => {
    const basePermissions = {
      showAction: true,
      saveWithRemark: true,
      saveBtn: true,
      allAction: true,
      showTitleNameBusiness: true,
      showExport: false,
      showImport: false,
      showCalculate: false,
      showCalculateVisibility: false,
    }

    if (READ_ONLY || IS_OLD_YEAR) {
      return {
        ...basePermissions,
        showAction: false,
        saveWithRemark: false,
        saveBtn: false,
        allAction: false,
      }
    }

    return basePermissions
  }, [READ_ONLY, IS_OLD_YEAR])

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />
      <AdvanceKendoTable
        rows={rows}
        setRows={setRows}
        columns={colDefs}
        permissions={adjustedPermissions}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Treatment Vendor Consumption'
        saveChanges={saveChanges}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        plantID={PLANT_ID}
        groupBy='ParticularG'
        handleExcelUpload={handleExcelUpload}
        handleExport={downloadExcelForConfiguration}
      />

      <Notification
        open={snackbarOpen}
        message={snackbarData.message}
        severity={snackbarData.severity}
        onClose={() => setSnackbarOpen(false)}
      />
    </Box>
  )
}

export default VendorConsumption

