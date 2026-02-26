import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress, Stack } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterPhaseTwo from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { InputApiService } from 'components/aop-phase-two/services/cpp/inputApiService'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import DateRangeSelectorWithHistory from 'components/aop-phase-two/common/utilities/DateRangeSelectorWithHistory'
import { customValueFormatterPhaseTwo as customValueFormat } from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'

const HRSGHeatRate = () => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
  const [customModifiedCells, setCustomModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedPlant, setSelectedPlant] = useState('')
  const [dropdownOptions, setDropdownOptions] = useState([])
  const [startDate, setStartDate] = useState(null)
  const [endDate, setEndDate] = useState(null)
  const [dateLoading, setDateLoading] = useState(false)
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

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const columns = [
    {
      field: 'id',
      title: 'Id',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
      hidden: true,
    },
    {
      field: 'equipType',
      title: 'Equipment Type',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 150,
    },
    {
      field: 'cppUtility',
      title: 'CPP Utility',
      width: 150,
      type: 'text',
      editable: false,
      minWidth: 150,
    },
    {
      field: 'hrsgLoad',
      title: 'HRSG Load',
      width: 120,
      type: 'number1',
      format: customValueFormat(1),
      editable: true,
      minWidth: 120,
    },
    {
      field: 'oemHeatRate',
      title: 'OEM HR',
      widthT: 150,
      type: 'numberWithRadio',
      format: customValueFormat(1),
      editable: true,
      numericEditable: true,
      minWidth: 150,
      radioGroupField: 'selectedHeatRate',
      targetField: 'finalHeatRate',
      radioValue: 'OEM',
    },
    {
      field: 'previousYearHeatRate',
      title: 'PREVIOUS YEAR BUDGET HR',
      widthT: 200,
      type: 'numberWithRadio',
      format: customValueFormat(1),
      editable: true,
      numericEditable: false,
      minWidth: 200,
      radioGroupField: 'selectedHeatRate',
      targetField: 'finalHeatRate',
      radioValue: 'PREVIOUS_YEAR',
    },
    {
      field: 'proposedHeatRate',
      title: 'PROPOSED HR',
      subtitle: '(Based On Actual Data)',
      widthT: 200,
      type: 'numberWithRadio',
      format: customValueFormat(1),
      editable: true,
      numericEditable: false,
      minWidth: 200,
      radioGroupField: 'selectedHeatRate',
      targetField: 'finalHeatRate',
      radioValue: 'PROPOSED',
    },
    {
      field: 'finalHeatRate',
      title: 'Final HR',
      widthT: 150,
      type: 'number1',
      format: customValueFormat(1),
      editable: true,
      minWidth: 150,
    },
    {
      field: 'remarks',
      title: 'Remark',
      width: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])

  const formatDate = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  useEffect(() => {
    if (selectedPlant && startDate && endDate) {
      const formattedStartDate = formatDate(startDate)
      const formattedEndDate = formatDate(endDate)
      fetchHeatRateData(selectedPlant, formattedStartDate, formattedEndDate)
    }
  }, [PLANT_ID, AOP_YEAR, selectedPlant, startDate, endDate])

  useEffect(() => {
    getPlantList()
  }, [PLANT_ID, AOP_YEAR])

  const getPlantList = async () => {
    setLoading(true)
    try {
      const res = await InputApiService.getHRSGHeatRateDropdown(
        keycloak,
        PLANT_ID,
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
        setLoading(false)
        return
      }
      setSelectedPlant(convertedData[0]?.id)
      setDropdownOptions(convertedData)
    } catch (error) {
      console.error('Error fetching HRSG dropdown options:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const fetchHeatRateData = async (assetId, startDate, endDate) => {
    setLoading(true)
    try {
      const res = await InputApiService.getHRSGHeatRateData(
        keycloak,
        assetId,
        AOP_YEAR,
        startDate,
        endDate,
      )

      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        setLoading(false)
        return
      }
      let tempRes = res.map((item, index) => {
        const selectedHeatRate = item.selectedHeatRate || 'PROPOSED'

        // Validate if selectedHeatRate matches the actual finalHeatRate value
        const fieldMapping = {
          OEM: 'oemHeatRate',
          PREVIOUS_YEAR: 'previousYearHeatRate',
          PROPOSED: 'proposedHeatRate',
        }

        const selectedField = fieldMapping[selectedHeatRate]
        const selectedValue = selectedField ? item[selectedField] : null
        const finalValue = item.finalHeatRate

        // Check if selected column value matches final heat rate
        const isMatch =
          selectedValue !== null &&
          selectedValue !== undefined &&
          finalValue !== null &&
          finalValue !== undefined &&
          parseFloat(selectedValue) === parseFloat(finalValue)

        return {
          ...item,
          id: item.id || index + 1,
          remarks: item.remarks || '',
          selectedHeatRate: isMatch ? selectedHeatRate : 'OTHER',
        }
      })
      console.log('res', res)
      setRows(tempRes)
      setOriginalRows(tempRes)
    } catch (error) {
      console.error('Error fetching HRSG heat rate data:', error)
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
    ExcelName: `HRSG Heat Rate - ${AOP_YEAR}`,
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
    const fieldsToCheck = ['hrsgLoad', 'oemHeatRate', 'finalHeatRate']
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'hrsgLoad',
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

    try {
      const payload = modifiedData.map((item) => {
        const { inEdit, ...rest } = item
        return rest
      })
      const tempPayload = JSON.stringify(payload)

      const res = await InputApiService.saveHRSGHeatRateData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
      const formattedStartDate = formatDate(startDate)
      const formattedEndDate = formatDate(endDate)
      await fetchHeatRateData(
        selectedPlant,
        formattedStartDate,
        formattedEndDate,
      )
    } catch (error) {
      console.error('Error saving heat rate data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
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
      const response = await InputApiService.saveHRSGHeatRateExcel(
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
        if (startDate && endDate) {
          const formattedStartDate = formatDate(startDate)
          const formattedEndDate = formatDate(endDate)
          await fetchHeatRateData(
            selectedPlant,
            formattedStartDate,
            formattedEndDate,
          )
        }
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
      const formattedStartDate = startDate ? formatDate(startDate) : null
      const formattedEndDate = endDate ? formatDate(endDate) : null

      await InputApiService.exportHRSGHeatRateExcel(
        keycloak,
        selectedPlant,
        AOP_YEAR,
        formattedStartDate,
        formattedEndDate,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting HRSG Heat Rate data:', error)
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
    const itemId = dataItem.id

    // When radio selection changes, update the Final Heat Rate
    if (field === 'selectedHeatRate') {
      // Map radioValue to field name
      const fieldMapping = {
        OEM: 'oemHeatRate',
        PREVIOUS_YEAR: 'previousYearHeatRate',
        PROPOSED: 'proposedHeatRate',
      }

      const selectedField = fieldMapping[value]
      const selectedValue = selectedField ? dataItem[selectedField] : null

      setRows((prev) =>
        prev.map((r) => {
          if (r.id === dataItem.id) {
            return {
              ...r,
              selectedHeatRate: value,
              finalHeatRate: selectedValue,
            }
          }
          return r
        }),
      )

      // Track both fields in modifiedCells
      setModifiedCells((prev) => {
        const currentRow = rows.find((r) => r.id === itemId)
        return {
          ...prev,
          [itemId]: {
            ...(prev[itemId] || currentRow),
            selectedHeatRate: value,
            finalHeatRate: selectedValue,
            inEdit: true,
          },
        }
      })

      setCustomModifiedCells((prev) => ({
        ...prev,
        [itemId]: {
          ...(prev[itemId] || {}),
          selectedHeatRate: value,
          finalHeatRate: selectedValue,
        },
      }))

      return
    }

    // When a source column is edited, update finalHeatRate ONLY if that source is currently selected
    const sourceFieldMapping = {
      oemHeatRate: 'OEM',
      previousYearHeatRate: 'PREVIOUS_YEAR',
      proposedHeatRate: 'PROPOSED',
    }

    if (sourceFieldMapping[field]) {
      const radioValueForThisField = sourceFieldMapping[field]

      setRows((prev) =>
        prev.map((r) => {
          if (r.id === dataItem.id) {
            // Only update finalHeatRate if this source is currently selected
            if (r.selectedHeatRate === radioValueForThisField) {
              return {
                ...r,
                [field]: value,
                finalHeatRate: value,
              }
            }
            // Otherwise just update the source field
            return {
              ...r,
              [field]: value,
            }
          }
          return r
        }),
      )

      // Track changes in modifiedCells and customModifiedCells
      const currentRow = rows.find((r) => r.id === itemId)
      if (currentRow?.selectedHeatRate === radioValueForThisField) {
        // Update both source field and finalHeatRate
        setModifiedCells((prev) => ({
          ...prev,
          [itemId]: {
            ...(prev[itemId] || currentRow),
            [field]: value,
            finalHeatRate: value,
            inEdit: true,
          },
        }))

        setCustomModifiedCells((prev) => ({
          ...prev,
          [itemId]: {
            ...(prev[itemId] || {}),
            [field]: value,
            finalHeatRate: value,
          },
        }))
      } else {
        // Still track the source field change even if not selected (for orange highlighting)
        setModifiedCells((prev) => ({
          ...prev,
          [itemId]: {
            ...(prev[itemId] || currentRow),
            [field]: value,
            inEdit: true,
          },
        }))

        setCustomModifiedCells((prev) => ({
          ...prev,
          [itemId]: {
            ...(prev[itemId] || {}),
            [field]: value,
          },
        }))
      }

      return
    }

    // When Final Heat Rate is manually edited, check if it matches any source column
    if (field === 'finalHeatRate') {
      const sourceFields = [
        {
          radioValue: 'OEM',
          field: 'oemHeatRate',
          value: dataItem.oemHeatRate,
        },
        {
          radioValue: 'PREVIOUS_YEAR',
          field: 'previousYearHeatRate',
          value: dataItem.previousYearHeatRate,
        },
        {
          radioValue: 'PROPOSED',
          field: 'proposedHeatRate',
          value: dataItem.proposedHeatRate,
        },
      ]

      let matchedRadioValue = null

      // Check if the entered value matches any source column value
      for (const source of sourceFields) {
        if (
          source.value !== null &&
          source.value !== undefined &&
          parseFloat(value) === parseFloat(source.value)
        ) {
          matchedRadioValue = source.radioValue
          break
        }
      }

      setRows((prev) =>
        prev.map((r) => {
          if (r.id === dataItem.id) {
            return {
              ...r,
              finalHeatRate: value,
              // Auto-select radio if value matches a source, otherwise set to OTHER
              selectedHeatRate: matchedRadioValue || 'OTHER',
            }
          }
          return r
        }),
      )

      // Track both fields in modifiedCells
      setModifiedCells((prev) => {
        const currentRow = rows.find((r) => r.id === itemId)
        return {
          ...prev,
          [itemId]: {
            ...(prev[itemId] || currentRow),
            finalHeatRate: value,
            selectedHeatRate: matchedRadioValue || 'OTHER',
            inEdit: true,
          },
        }
      })

      setCustomModifiedCells((prev) => ({
        ...prev,
        [itemId]: {
          ...(prev[itemId] || {}),
          finalHeatRate: value,
          selectedHeatRate: matchedRadioValue || 'OTHER',
        },
      }))
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

      <Stack sx={{ mt: 2, mb: 2 }}>
        <DateRangeSelectorWithHistory
          onDateChange={({ startDate, endDate }) => {
            console.log('Dates changed:', startDate, endDate)
            setStartDate(startDate)
            setEndDate(endDate)
          }}
          disabled={false}
          timeRequired={false}
        />
      </Stack>

      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        externalCustomModifiedCells={customModifiedCells}
        externalSetCustomModifiedCells={setCustomModifiedCells}
        title='HRSG Heat Rate'
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
        customItemChange={handleCustomItemChange}
        dropdownConfig={dropdownConfig}
        selectedDropdownValue={selectedPlant}
        setSelectedDropdownValue={setSelectedPlant}
        paginationConfig={{
          threshold: 20,
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 20,
        }}
      />
    </Box>
  )
}

export default HRSGHeatRate
