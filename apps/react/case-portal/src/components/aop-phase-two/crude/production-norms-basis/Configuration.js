import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import {
  ValueFormatterPhaseTwo,
  customValueFormatterPhaseTwo,
} from 'components/aop-phase-two/common/ValueFormatterPhaseTwo'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import RowBasedKendoTable from '../../common/RowBasedKendoTable/index'
import { configurationAndReportManualEntryResponse } from '../dummyData'
import {
  handleDateDifferenceCalculation,
  handleValueMappingDependency,
  handleLegacyDependencyRule,
  handleAdditionDependency,
} from './utils/dependencyUtils'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/crude/productionNormsApiService'

const Configuration = ({ startDate, endDate, refreshData }) => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
  const [customModifiedCells, setCustomModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year, siteObject } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = customValueFormatterPhaseTwo(3)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [dependencyRules, setDependencyRules] = useState({})

  // Addition dependency configuration for MP Steam fields
  const additionDependencyConfig = {
    sourceFields: [
      '38% Ejector inline for next AOP Cycle',
      'Additional MP Steam in Main Ejector',
    ],
    targetField: 'Additional MP Steam in Ejector, A',
  }

  // Editability rules: Define when dependent fields should become non-editable
  const editabilityRules = {
    '38% Ejector Input': {
      dependentField: '38% Ejector inline for next AOP Cycle',
      nonEditableWhen: ['NO', 'No', 'no'], // Values that make dependent field non-editable
    },
  }

  // Build dependency rules from row data
  // Expects rows to have config property on controller fields
  const buildDependencyRules = (rowsData) => {
    const rules = {}
    rowsData.forEach((row) => {
      if (row.config && row.name) {
        rules[row.name] = {
          dependentProductName: row.config.dependentProductName,
          values: row.config.valueMapping || {},
        }
      }
    })
    return rules
  }

  const columns = [
    {
      field: 'name',
      title: 'Particulars',
      widthT: 250,
      minWidth: 200,
      type: 'text',
      editable: false,
      hidden: false,
    },
    {
      field: 'uom',
      title: 'UOM',
      widthT: 80,
      minWidth: 60,
      type: 'text',
      editable: false,
    },
    {
      field: 'attributeValue',
      title: 'Value',
      editable: true,
      widthT: 100,
      minWidth: 80,
      align: 'left',
      headerAlign: 'left',
      type: 'row-based',
      format: valueFormat,
    },
    {
      field: 'remarks',
      title: 'Remarks',
      widthT: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchConfigurationData()
    }
  }, [PLANT_ID, AOP_YEAR, refreshData])

  const fetchConfigurationData = async () => {
    setLoading(true)
    try {
      const res = await ProductionNormsApiService.getConfigurationData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }

      const formattedData = res?.map((item, index) => {
        // Parse config from JSON string if it exists
        let parsedAttributeValue = null
        if (item.config) {
          try {
            parsedAttributeValue =
              typeof item.config === 'string'
                ? JSON.parse(item.config)
                : item.config
          } catch (e) {
            console.error('Error parsing config:', e)
            parsedAttributeValue = null
          }
        }

        const mappingKeys = parsedAttributeValue?.valueMapping
          ? Object.keys(parsedAttributeValue.valueMapping)
          : []

        // Preserve existing type (date, dropdown, etc.) or infer from dependencies
        // Default to 'number' if no type is specified
        const type = item.type || (mappingKeys.length ? 'dropdown' : undefined)

        // Format date values to YYYY-MM-DD string format
        let formattedAttributeValue = item.attributeValue
        if ((type === 'date' || type === 'datetime') && item.attributeValue) {
          try {
            const dateObj = new Date(item.attributeValue)
            if (!isNaN(dateObj.getTime())) {
              const year = dateObj.getFullYear()
              const month = String(dateObj.getMonth() + 1).padStart(2, '0')
              const day = String(dateObj.getDate()).padStart(2, '0')
              formattedAttributeValue = `${year}-${month}-${day}`
            }
          } catch (e) {
            console.error('Error formatting date:', e)
          }
        }

        return {
          ...item,
          config: parsedAttributeValue,
          type,
          options: item.options?.length ? item.options : mappingKeys,
          remarks: item.remarks || '',
          id: item?.id || index + 1,
          attributeValue: formattedAttributeValue,
          isEditable: item.isEditable,
        }
      })

      // Apply editability rules on initial load
      const updatedFormattedData = formattedData.map((row) => {
        // Check if this row is a dependent field in any editability rule
        for (const [sourceFieldName, rule] of Object.entries(
          editabilityRules,
        )) {
          if (rule.dependentField === row.name) {
            // Find the source field
            const sourceField = formattedData.find(
              (r) => r.name === sourceFieldName,
            )
            if (sourceField && sourceField.attributeValue) {
              // Check if source value makes this field non-editable
              const shouldBeNonEditable = rule.nonEditableWhen.includes(
                sourceField.attributeValue,
              )
              if (shouldBeNonEditable) {
                return {
                  ...row,
                  isEditable: false,
                }
              }
            }
          }
        }
        return row
      })

      setRows(updatedFormattedData)
      setOriginalRows(updatedFormattedData)

      // Build dependency rules from the data
      const rules = buildDependencyRules(formattedData)
      setDependencyRules(rules)
    } catch (error) {
      console.error('Error fetching configuration data:', error)
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
    // showExport: true,
    downloadExcelBtnFromUI: true,
    ExcelName: `Production_Norms_Configuration_${AOP_YEAR}`,
    showImport: true,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Configuration',
  }

  const formatDateForAPI = (date) => {
    if (!date) return ''
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const saveChanges = async () => {
    setLoading(true)

    // Validate required parameters
    if (!startDate || !endDate) {
      setSnackbarOpen(true)
      setSnackbarData({
        message:
          'Period dates are required. Please ensure dates are loaded from AOP Period Basis.',
        severity: 'error',
      })
      setLoading(false)
      return
    }

    if (!SITE_ID) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Site ID is required.',
        severity: 'error',
      })
      setLoading(false)
      return
    }

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

    const data = modifiedData.filter((row) => row.inEdit)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const fieldsToCheck = ['attributeValue']
    const validationError = validateRowDataWithRemarks(
      data.filter((item) => item.isEditable == true),
      originalRows,
      fieldsToCheck,
      'name',
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

    // Transform payload to stringify config field for backend
    const payload = modifiedData.map((item) => {
      const { config, ...rest } = item
      return {
        ...rest,
        // Stringify config if it exists and is an object
        config:
          config && typeof config === 'object'
            ? JSON.stringify(config)
            : config,
      }
    })

    try {
      const periodFrom = formatDateForAPI(startDate)
      const periodTo = formatDateForAPI(endDate)

      const response = await ProductionNormsApiService.saveConfigurationData(
        keycloak,
        AOP_YEAR,
        payload,
        PLANT_ID,
        SITE_ID,
        periodFrom,
        periodTo,
      )

      setModifiedCells({})

      if (response?.code === 422) {
        // Show success notification first
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Successfully saved ${modifiedData.length} changes!`,
          severity: 'success',
        })

        // Then show validation error after a delay
        setTimeout(() => {
          setSnackbarOpen(true)
          setSnackbarData({
            message: response.message || 'Validation error occurred.',
            severity: 'error',
            autoHide: false,
          })
        }, 1000)
      } else {
        // Code 200 - show only success notification
        setSnackbarOpen(true)
        setSnackbarData({
          message: `Successfully saved ${modifiedData.length} changes!`,
          severity: 'success',
        })
      }

      await fetchConfigurationData()
    } catch (error) {
      console.error('Error saving configuration data:', error)
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
      const response = await ProductionNormsApiService.importConfigurationExcel(
        file,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        await fetchConfigurationData()
      } else if (response?.code === 400 && response?.data) {
        try {
          const base64Data = response.data
          const binaryString = window.atob(base64Data)
          const bytes = new Uint8Array(binaryString.length)
          for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i)
          }
          const blob = new Blob([bytes], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `Configuration_Errors_${new Date().getTime()}.xlsx`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message:
              response?.message ||
              'Import failed with errors. Please check the downloaded file.',
            severity: 'error',
          })
          await fetchConfigurationData()
        } catch (downloadError) {
          console.error('Error downloading error file:', downloadError)
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Import failed but could not download error file.',
            severity: 'error',
          })
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Failed to import Excel file.',
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
      await ProductionNormsApiService.exportConfigurationExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Configuration data:', error)
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

  const handleCustomItemChange = (e, setRowsCallback) => {
    const { dataItem, field, value } = e

    if (field !== 'attributeValue') return

    const currentProductName = dataItem.name
    let dependentFieldsToCheck = []

    // Check if this field is part of addition dependency (MP Steam fields)
    if (additionDependencyConfig.sourceFields.includes(currentProductName)) {
      handleAdditionDependency({
        currentFieldName: currentProductName,
        value,
        dependencyConfig: additionDependencyConfig,
        rows,
        setRowsCallback,
        setModifiedCells,
        setCustomModifiedCells,
      })
    }

    // Check if this field has a dependency configuration
    if (dataItem.config) {
      const { calculationType, valueMapping, dependentProductName } =
        dataItem.config

      // Handle date difference calculation
      if (calculationType === 'dateDifference') {
        handleDateDifferenceCalculation({
          value,
          dependencyConfig: dataItem.config,
          rows,
          setRowsCallback,
          setModifiedCells,
          setCustomModifiedCells,
        })
        return
      }

      // Handle value mapping (dropdown dependencies)
      if (valueMapping && dependentProductName) {
        const dependentValue = valueMapping[value]
        if (dependentValue !== undefined) {
          handleValueMappingDependency({
            value,
            dependencyConfig: dataItem.config,
            rows,
            setRowsCallback,
            setModifiedCells,
            setCustomModifiedCells,
            sourceFieldName: currentProductName,
            editabilityRules,
          })

          // Track dependent field for cascading check
          dependentFieldsToCheck.push({
            fieldName: dependentProductName,
            fieldValue: dependentValue,
          })
        }

        // Process cascading dependencies after state updates
        if (dependentFieldsToCheck.length > 0) {
          setTimeout(() => {
            setRowsCallback((currentRows) => {
              dependentFieldsToCheck.forEach(({ fieldName, fieldValue }) => {
                // Check if dependent field is part of addition dependency
                if (additionDependencyConfig.sourceFields.includes(fieldName)) {
                  handleAdditionDependency({
                    currentFieldName: fieldName,
                    value: fieldValue,
                    dependencyConfig: additionDependencyConfig,
                    rows: currentRows,
                    setRowsCallback,
                    setModifiedCells,
                    setCustomModifiedCells,
                  })
                }
              })
              return currentRows
            })
          }, 0)
        }
        return
      }
    }

    // Legacy support: Check old dependencyRules format
    const dependencyRule = dependencyRules[currentProductName]
    if (dependencyRule) {
      handleLegacyDependencyRule({
        value,
        dependencyRule,
        rows,
        setRowsCallback,
        setModifiedCells,
        setCustomModifiedCells,
        sourceFieldName: currentProductName,
      })
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
      <RowBasedKendoTable
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
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        customItemChange={handleCustomItemChange}
        externalCustomModifiedCells={customModifiedCells}
        externalSetCustomModifiedCells={setCustomModifiedCells}
        groupBy={['normParameterType']}
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

export default Configuration
