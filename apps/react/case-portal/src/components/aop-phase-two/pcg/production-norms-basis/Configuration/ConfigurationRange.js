import { useEffect, useState } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import RowBasedKendoTable from 'components/aop-phase-two/common/RowBasedKendoTable/index'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/pcg/productionNormsApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'

const COLUMNS = [
  { field: 'displayName', title: 'Data Filters', widthT: 250, minWidth: 200, type: 'text', editable: false },
  { field: 'uom', title: 'UOM', widthT: 80, minWidth: 60, type: 'text', editable: false },
  { field: 'targetValue', title: 'Target Value', editable: true, widthT: 120, minWidth: 100, type: 'row-based' },
  { field: 'range', title: 'Range', editable: true, widthT: 120, minWidth: 100, type: 'row-based' },
  { field: 'selection', title: 'Selection', editable: true, widthT: 80, minWidth: 80, type: 'checkbox' },
  { field: 'remarks', title: 'Remarks', widthT: 250, type: 'textarea', editable: true, minWidth: 250, showPlaceholder: false },
  { field: 'dependantAttributeId', title: 'Applicable Utilities & Cat-Chems', widthT: 250, minWidth: 200, type: 'text', editable: false },
]

const FILTER_CONFIG = [
  { id: 1, displayName: 'Sulphur Production', uom: '', targetValue: 'PIMS Sulphur Production (auto fetch from PIMS throughput tab)', range: '+/- 5%', selection: false, remarks: '', utilities: 'Fuel, HP Stam, LP Steam, Oxygen, Power, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: ['targetValue'], type: 'formula-text' },
  { id: 2, displayName: 'AGR C003 Inline/Bypass', uom: '', targetValue: 'Bypass', range: '+/-5%', selection: false, remarks: '', utilities: 'Fuel, HP Stam, LP Steam, Oxygen, Power, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: ['range'], type: 'dropdown', options: ['Inline', 'Bypass'] },
  { id: 3, displayName: 'AGR C003 Inline (G330PI400340 A/B/C)', uom: 'kg/cm2g', targetValue: '1.2', range: '', selection: false, remarks: 'Less than target value', utilities: 'Fuel, HP Stam, LP Steam, Oxygen, Power, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: ['range'], type: 'number1', hideCheckbox: true },
  { id: 4, displayName: 'AGR C003 Inline ( G330FIC400305)', uom: '%', targetValue: '10', range: '', selection: false, remarks: 'more than target value', utilities: 'Fuel, HP Stam, LP Steam, Oxygen, Power, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: ['range'], type: 'number1', hideCheckbox: true },
  { id: 5, displayName: 'AGR C003 Bypass (G330PI400340 A/B/C)', uom: 'kg/cm2g', targetValue: '1.6', range: '', selection: false, remarks: 'more than target value', utilities: 'Fuel, HP Stam, LP Steam, Oxygen, Power, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: ['range'], type: 'number1', hideCheckbox: true },
  { id: 6, displayName: 'AGR C003 Bypass (G330FIC400305)', uom: '%', targetValue: '2', range: '', selection: false, remarks: 'Less than target value', utilities: 'Fuel, HP Stam, LP Steam, Oxygen, Power, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: ['range'], type: 'number1', hideCheckbox: true },
  { id: 7, displayName: 'J1 Acid Gas Flow', uom: 'Nm3/Hr', targetValue: 'Manual input', range: '+/- 500', selection: false, remarks: '', utilities: 'Fuel, HP Stam, LP Steam, Oxygen, Power, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: [], type: 'formula-text' },
  { id: 8, displayName: 'Average J3 Acid Gas H2S Concentration', uom: '%', targetValue: 'Manual input', range: '+/- 2%', selection: false, remarks: '', utilities: 'Fuel, HP Stam, LP Steam, Oxygen, Power, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: [], type: 'formula-text' },
  { id: 9, displayName: 'Incinerator Temperature (G360TIC000157)', uom: 'Deg C', targetValue: '740', range: '', selection: false, remarks: 'more than target value', utilities: 'Fuel, HP Stam, LP Steam, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: ['range'], type: 'number1' },
  { id: 10, displayName: 'Incinerator Temperature (G361TIC000157)', uom: 'Deg C', targetValue: '740', range: '', selection: false, remarks: 'more than target value', utilities: 'Fuel, HP Stam, LP Steam, Return Steam Condensate, BFW', isEditable: true, nonEditableFields: ['range'], type: 'number1' },
  { id: 11, displayName: 'Average O2 Enrichment', uom: '%', targetValue: 'Manual input', range: '+/- 2%', selection: false, remarks: '', utilities: 'Oxygen, Power', isEditable: true, nonEditableFields: [], type: 'formula-text' },
  { id: 12, displayName: 'SWS-1 Sour Water Processing Rate', uom: 'M3/Hr', targetValue: '200', range: '', selection: false, remarks: 'More than target value', utilities: 'LP Steam & Return Steam Condensate', isEditable: true, nonEditableFields: ['range'], type: 'number1' },
  { id: 13, displayName: 'SRU-1 Sulphur production (GSR1PR001)', uom: 'TPD', targetValue: '50', range: '', selection: false, remarks: 'More than target value', utilities: 'LLP N2, LP N2, Instrument Air, DM Water, Utility Water, Plant Air, caustic, CHEM Ammonia, Chem Maxtreat 3223 SJ', isEditable: true, nonEditableFields: ['range'], type: 'number1' },
  { id: 14, displayName: 'SRU-2 Sulphur production (GSR2PR001)', uom: 'TPD', targetValue: '50', range: '', selection: false, remarks: 'More than target value', utilities: 'LLP N2, LP N2, Instrument Air, DM Water, Utility Water, Plant Air, caustic, CHEM Ammonia, Chem Maxtreat 3223 SJ', isEditable: true, nonEditableFields: ['range'], type: 'number1' }
]

const PERMISSIONS = {
  showAction: true, addButton: false, deleteButton: false, editButton: true, saveBtn: true,
  allAction: true, downloadExcelBtnFromUI: true, ExcelName: 'Production_Norms_Configuration',
  showImport: false, showTitleNameBusiness: true, showTitle: true, titleName: 'Configuration',
}

const parseConfig = (config) => {
  if (!config) return null
  try { return typeof config === 'string' ? JSON.parse(config) : config }
  catch { return null }
}

const formatDate = (val) => {
  if (!val) return val
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const Configuration = ({ startDate, endDate, refreshData }) => {
  const keycloak = useSession()
  const { plantObject, year } = useSelector((state) => state.dataGridStore)
  
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [customModifiedCells, setCustomModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const showSnackbar = (message, severity = 'info', autoHide = true) => {
    setSnackbarOpen(true)
    setSnackbarData({ message, severity, autoHide })
  }

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) fetchConfigurationData()
  }, [PLANT_ID, AOP_YEAR, refreshData])

  const fetchConfigurationData = async () => {
    setLoading(true)
    try {
      const apiRes = await ProductionNormsApiService.getConfigurationData(keycloak, PLANT_ID, AOP_YEAR)
      
      const res = apiRes?.length ? apiRes.map((apiItem, index) => {
        const configItem = FILTER_CONFIG.find(d => [apiItem.displayName, apiItem.productName].includes(d.displayName)) || {}
        
        return {
          ...configItem, ...apiItem,
          isEditable: apiItem.isEditable ?? configItem.isEditable ?? true,
          nonEditableFields: configItem.nonEditableFields || [],
          type: configItem.type || 'number1',
          dataType: configItem.type || 'number1',
          options: configItem.options || []
        }
      }) : FILTER_CONFIG

      if (!res.length) {
        showSnackbar('No data found')
        setLoading(false) 
        setRows([])
        setOriginalRows([])
        return 
      }

      let formattedData = res.map((item, index) => {
        const config = parseConfig(item.config)
        const mappingKeys = config?.valueMapping ? Object.keys(config.valueMapping) : []
        const type = item.dataType || (mappingKeys.length ? 'dropdown' : undefined)
        
        return {
          ...item,
          config, type,
          options: item.options?.length ? item.options : mappingKeys,
          remarks: item.remarks || '',
          id: item.id || index + 1,
          attributeValue: ['date', 'datetime'].includes(type) ? formatDate(item.attributeValue) : item.attributeValue,
          allowNegative: item.allowNegative,
          selection: item.selection?.toLowerCase() === 'true' ? true : false
        }
      })

      const inlineBypassRow = formattedData.find(row => row.displayName === 'AGR C003 Inline/Bypass')
      if (inlineBypassRow) {
        const value = inlineBypassRow.targetValue
        formattedData = formattedData.map(row => {
          if (row.displayName !== 'AGR C003 Inline/Bypass') {
            const isInline = row.displayName?.includes('AGR C003 Inline')
            const isBypass = row.displayName?.includes('AGR C003 Bypass')
            if (isInline || isBypass) {
              return { ...row, isEditable: value === (isInline ? 'Inline' : 'Bypass') }
            }
          }
          return row
        })
      }

      setRows(formattedData)
      setOriginalRows(formattedData)
      setLoading(false) 
    } catch (error) {
      console.error('Error fetching data:', error)
      showSnackbar('Error fetching data', 'error')
      setLoading(false) 
    }
  }

  const saveChanges = async () => {
    const modifiedData = Object.values(modifiedCells)
    console.log("modifiedData", modifiedData)
    const dataToSave = modifiedData.filter((row) => row.inEdit)

    if (!dataToSave.length) return showSnackbar('No Records to Save!')

    const validationError = validateRowDataWithRemarks(
      dataToSave.filter((item) => item.isEditable),
      originalRows,
      ['targetValue', 'range', 'selection'],
      'displayName'
    )

    if (validationError) return showSnackbar(validationError, 'error')

    setLoading(true)
    
    const payload = modifiedData.map((rest) => ({
      ...rest,
    }))

    try {
      const response = await ProductionNormsApiService.saveConfigurationData(keycloak, AOP_YEAR, payload, PLANT_ID)
      setModifiedCells({})
      
      showSnackbar(`Successfully saved ${modifiedData.length} changes!`, 'success')
      if (response?.code === 422) {
        setTimeout(() => showSnackbar(response.message || 'Validation error occurred.', 'error', false), 1000)
      }
      await fetchConfigurationData()
    } catch (error) {
      console.error('Error saving data:', error)
      showSnackbar('Failed to save changes. Please try again.', 'error')
      setLoading(false) 
    }
  }

  const handleExcelUpload = async (file) => {
    if (!file) return
    setLoading(true)
    try {
      const response = await ProductionNormsApiService.importConfigurationExcel(file, keycloak, PLANT_ID, AOP_YEAR)
      
      if (response?.code === 200) {
        showSnackbar(response.message || 'Excel file imported successfully!', 'success')
        await fetchConfigurationData()
      } else if (response?.code === 400 && response?.data) {
        downloadErrorExcel(response.data)
        showSnackbar(response.message || 'Import failed with errors. Please check the downloaded file.', 'error')
        await fetchConfigurationData()
      } else {
        showSnackbar(response?.message || 'Failed to import Excel file.', 'error')
      }
    } catch (error) {
      showSnackbar(`Failed to import Excel file: ${error.message}`, 'error')
    } finally {
      setLoading(false) 
    }
  }

  const downloadErrorExcel = (base64Data) => {
    try {
      const binaryString = window.atob(base64Data)
      const bytes = new Uint8Array(binaryString.length)
      for (let i = 0; i < binaryString.length; i++) bytes[i] = binaryString.charCodeAt(i)
      
      const blob = new Blob([bytes], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `Configuration_Errors_${Date.now()}.xlsx`
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch (e) {
      console.error('Error downloading error file:', e)
    }
  }

  const handleExport = async () => {
    showSnackbar('Excel download started!')
    try {
      await ProductionNormsApiService.exportConfigurationExcel(keycloak, PLANT_ID, AOP_YEAR)
      showSnackbar('Excel download completed successfully!', 'success')
    } catch (error) {
      showSnackbar('Excel download failed. Please try again.', 'error')
    }
  }

  const handleCustomItemChange = ({ dataItem, field, value }, setRowsCallback) => {
    if (!['targetValue', 'range'].includes(field)) return

    if (dataItem.displayName === 'AGR C003 Inline/Bypass' && field === 'targetValue') {
      setRowsCallback((currentRows) => currentRows.map((row) => {
        if (row.displayName !== 'AGR C003 Inline/Bypass') {
          const isInline = row.displayName?.includes('AGR C003 Inline')
          const isBypass = row.displayName?.includes('AGR C003 Bypass')
          if (isInline || isBypass) {
            return { ...row, isEditable: value === (isInline ? 'Inline' : 'Bypass') }
          }
        }
        return row
      }))
    }
  }

  return (
    <Box>
      <LoaderBackdrop open={loading} />
      <RowBasedKendoTable
        columns={COLUMNS}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={PERMISSIONS.showTitle ? PERMISSIONS.titleName : ''}
        permissions={{ ...PERMISSIONS, ExcelName: `${PERMISSIONS.ExcelName}_${AOP_YEAR}` }}
        handleRemarkCellClick={(row) => {setCurrentRemark(row.remarks || ''); setCurrentRowId(row.id); setRemarkDialogOpen(true)}}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        saveChanges={saveChanges}
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        customItemChange={handleCustomItemChange}
        externalCustomModifiedCells={customModifiedCells}
        externalSetCustomModifiedCells={setCustomModifiedCells}
        paginationConfig={{ threshold: 100, buttonCount: 5, pageSizes: [10, 20, 50, 100], defaultPageSize: 100 }}
      />
    </Box>
  )
}

export default Configuration
