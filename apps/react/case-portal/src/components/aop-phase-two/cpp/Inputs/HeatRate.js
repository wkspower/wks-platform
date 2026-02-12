import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'
import STGHeatRate from './STGHeatRate'
import HRSGHeatRate from './HRSGHeatRate'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'

const HeatRate = () => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const valueFormat = ValueFormatterPhaseTwo()

  const columns = [
    {
      field: 'equipType',
      title: 'Equipment Type',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    {
      field: 'cppUtility',
      title: 'CPP Utility',
      width: 120,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    {
      field: 'gtLoad',
      title: 'GT Load',
      width: 100,
      type: 'number1',
      format: valueFormat,
      editable: true,
      minWidth: 80,
    },
    {
      field: 'heatRate',
      title: 'OEM HR',
      width: 150,
      type: 'numberWithRadio',
      format: valueFormat,
      editable: true,
      minWidth: 150,
      radioGroupField: 'selectedHeatRateSource',
      targetField: 'finalHeatRate',
    },
    {
      field: 'lastYearHeatRate',
      title: 'PREVIOUS YEAR HR',
      width: 150,
      type: 'numberWithRadio',
      format: valueFormat,
      editable: true,
      minWidth: 150,
      radioGroupField: 'selectedHeatRateSource',
      targetField: 'finalHeatRate',
    },
    {
      field: 'derivedHeatRate',
      title: 'ACTUAL PROPOSED HR',
      width: 150,
      type: 'numberWithRadio',
      format: valueFormat,
      editable: true,
      minWidth: 150,
      radioGroupField: 'selectedHeatRateSource',
      targetField: 'finalHeatRate',
    },
    {
      field: 'finalHeatRate',
      title: 'Final Heat Rate',
      width: 150,
      type: 'number1',
      format: valueFormat,
      editable: true,
      minWidth: 150,
    },
    {
      field: 'freeSteamFactor',
      title: 'Free Steam Factor',
      width: 130,
      type: 'number1',
      format: valueFormat,
      editable: true,
      minWidth: 100,
    },
    {
      field: 'remarks',
      title: 'Remark',
      widthT: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [selectedPlant, setSelectedPlant] = useState('')
  const [dropdownOptions, setDropdownOptions] = useState([])
  useEffect(() => {
    if (selectedPlant) {
      fetchHeatRateData(selectedPlant)
    }
  }, [PLANT_ID, AOP_YEAR, selectedPlant])

  useEffect(() => {
    getPlantList()
  }, [PLANT_ID, AOP_YEAR])

  const getPlantList = async () => {
    setLoading(true)
    try {
      const res = await InputApiService.getPlantList(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      // Convert to required format
      const convertedData = res?.map((item) => ({
        id: item[0],
        name: item[1],
      }))

      if (convertedData?.length === 0) {
        setDropdownOptions([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      setSelectedPlant(convertedData[0]?.id)
      setDropdownOptions(convertedData)
    } catch (error) {
      console.error('Error fetching plant list:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const fetchHeatRateData = async (assetId) => {
    setLoading(true)
    try {
      const res = await InputApiService.getHeatRateData(keycloak, assetId)

      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      let tempRes = res?.map((item, index) => {
        // Compute selectedHeatRateSource based on finalHeatRate match
        const finalHR = item.finalHeatRate || item.derivedHeatRate || ''
        let selectedSource = ''

        // Check if finalHeatRate matches any source column
        const sourceFields = [
          { field: 'heatRate', value: item.heatRate },
          { field: 'lastYearHeatRate', value: item.lastYearHeatRate },
          { field: 'derivedHeatRate', value: item.derivedHeatRate || '200' },
        ]

        for (const source of sourceFields) {
          if (
            source.value !== null &&
            source.value !== undefined &&
            parseFloat(finalHR) === parseFloat(source.value)
          ) {
            selectedSource = source.field
            break
          }
        }

        return {
          ...item,
          id: item.id || index + 1,
          remarks: item.remarks || '',
          derivedHeatRate: item.derivedHeatRate || '200',
          lastYearHeatRate: item.lastYearHeatRate || '300',
          finalHeatRate: finalHR,
          selectedHeatRateSource: selectedSource, // Computed, not from API
        }
      })
      setRows(tempRes)
      setOriginalRows(tempRes)
    } catch (error) {
      console.error('Error fetching heat rate data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showTitleNameBusiness: true,
    titleName: screenTitle?.title,
    showImport: true,
    showExport: true,
    ExcelName: `GT Heat Rate - ${AOP_YEAR}`,
    showTitle: true,
    showDropdown: true,
  }

  const dropdownConfig = {
    options: dropdownOptions,
    label: 'Select Plant',
    placeholder: 'Select Plant',
    valueKey: 'id',
    labelKey: 'name',
  }

  const saveChanges = async () => {
    setLoading(true)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    var rawData = Object.values(modifiedCells)
    const data = rawData.filter((row) => row.inEdit)
    if (data.length == 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    // Custom validation: If any row data is updated, remarks must be filled and different from original
    const fieldsToCheck = [
      'gtLoad',
      'heatRate',
      'freeSteamFactor',
      'finalHeatRate',
    ]
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'equipType',
    )

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }
    console.log('modifiedData', modifiedData)
    try {
      const payload = modifiedData.map((item) => {
        const { inEdit, selectedHeatRateSource, ...rest } = item
        return rest
      })
      const tempPayload = JSON.stringify(payload)
      console.log('Payload being sent:', tempPayload)

      const res = await InputApiService.saveHeatRateData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )

      console.log('Save response:', res)
      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
    } catch (error) {
      console.error('Error saving heat rate data:', error)
      console.error('Error message:', error?.message)
      console.error('Error status:', error?.status)
      console.error('Error response:', error?.response)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Failed to save changes. Error: ${error?.message || 'Unknown error'}`,
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }
  const handleExcelUpload = async (file) => {
    if (!file) return

    setLoading(true)
    try {
      const response = await InputApiService.saveHeatRateExcel(
        file,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.success) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Excel file imported successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchHeatRateData(selectedPlant)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error uploading Excel file:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Failed to import Excel file: ${error.message}`,
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await InputApiService.exportHeatRateExcel(keycloak, selectedPlant)
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Heat Rate data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Custom itemChange handler for radio selection with bidirectional sync
  const handleCustomItemChange = (e, setRows) => {
    const { dataItem, field, value } = e

    // When radio selection changes, update the Final Heat Rate
    if (field === 'selectedHeatRateSource') {
      const selectedValue = dataItem[value]

      setRows((prev) =>
        prev.map((r) => {
          if (r.id === dataItem.id) {
            return {
              ...r,
              selectedHeatRateSource: value,
              finalHeatRate: selectedValue,
            }
          }
          return r
        }),
      )
    }

    // When Final Heat Rate is manually edited, check if it matches any source column
    if (field === 'finalHeatRate') {
      const sourceFields = ['heatRate', 'lastYearHeatRate', 'derivedHeatRate']
      let matchedField = null

      // Check if the entered value matches any source column value
      for (const sourceField of sourceFields) {
        const sourceValue = dataItem[sourceField]
        // Compare as numbers to handle string/number type differences
        if (
          sourceValue !== null &&
          sourceValue !== undefined &&
          parseFloat(value) === parseFloat(sourceValue)
        ) {
          matchedField = sourceField
          break
        }
      }

      setRows((prev) =>
        prev.map((r) => {
          if (r.id === dataItem.id) {
            return {
              ...r,
              finalHeatRate: value,
              // Auto-select radio if value matches a source, otherwise clear selection
              selectedHeatRateSource: matchedField || '',
            }
          }
          return r
        }),
      )
    }
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='GT Heat Rate'
        permissions={permissions}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        dropdownConfig={dropdownConfig}
        selectedDropdownValue={selectedPlant}
        setSelectedDropdownValue={setSelectedPlant}
        customItemChange={handleCustomItemChange}
        paginationConfig={{
          threshold: 20, // Show pagination if > 50 rows
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 20,
        }}
      />

      <Stack sx={{ mt: 2 }}>
        <STGHeatRate />
      </Stack>
      <Stack sx={{ mt: 2 }}>
        <HRSGHeatRate />
      </Stack>
    </Box>
  )
}

export default HeatRate
