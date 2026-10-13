import { useState, useEffect, useCallback } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import { customValueFormatterPhaseTwo } from '../../common/ValueFormatterPhaseTwo'
import { SteadyStateConsumptionApiService } from '../../services/polyester/steadyStateConsumptionApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'
import { downloadBase64Excel } from '../../common/utilities/downloadBase64Excel'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import ValidationErrorDialog from './ValidationErrorDialog'
import RowBasedKendoTable from 'components/aop-phase-two/common/RowBasedKendoTable/index'


const GradeWiseSteadyStateConsumption = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const EXCEL_NAME = generateExcelName(
    dataGridStore,
    'Grade Wise Overall Steady State Consumption (Norm)',
  )
  const isFilament = verticalObject?.name?.toLowerCase() === 'filament (pfy)'

  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  // Grade dropdown state
  const [grades, setGrades] = useState([])
  const [selectedGradeId, setSelectedGradeId] = useState(null)

  // calculationObject for conditional calculateDisabled
  const [calculationObject, setCalculationObject] = useState([])

  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  // Detailed validation error modal states
  const [validationErrorDialogOpen, setValidationErrorDialogOpen] =
    useState(false)
  const [validationErrors, setValidationErrors] = useState([])

  const valueFormat = customValueFormatterPhaseTwo(5)
  const headerMap = generateHeaderNames(AOP_YEAR)

  const staticInitialColumns = [
    {
      field: 'id',
      title: 'Id',
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
    {
      field: 'sapCode',
      title: 'SAP MAT Code',
      minWidth: 120,
      type: 'text',
      editable: false,
    },
    {
      field: 'productName',
      title: 'Particulars',
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'normParameterTypeDisplayName',
      title: 'Type',
      minWidth: 200,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
    {
      field: 'UOM',
      title: 'UOM / MT',
      minWidth: 120,
      type: 'text',
      editable: false,
    },
    {
      field: 'YTD',
      title: 'Overall AOP WtAvg',
      minWidth: 200,
      type: 'number',
      editable: false,
      format: valueFormat
    },
  ]
  const [columns, setColumns] = useState(staticInitialColumns)
  // ===================== Fetch Grade Dropdown =====================

  const fetchGrades = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      let response = await SteadyStateConsumptionApiService.getGrades(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        const gradeList = response?.data || []
        setGrades(gradeList)
        // Auto-select first grade (index 0) on initial load
        if (gradeList.length > 0 && !selectedGradeId) {
          setSelectedGradeId(gradeList[0].gradeId)
        }
        if (!Array.isArray(gradeList) || gradeList.length === 0) {
          setLoading(false)
        }
      } else {
        setGrades([])
      }
    } catch (error) {
      setGrades([])
      console.error('Error fetching grades:', error)
    }
  }, [PLANT_ID, AOP_YEAR, keycloak])

  // ===================== Fetch Main Data (requires gradeId) =====================

  const fetchData = useCallback(
    async () => {
      if (!PLANT_ID || !AOP_YEAR) return

      setLoading(true)
      setRows([])
      const remarksColumn = {
        field: 'remarks',
        title: 'Remark',
        minWidth: 220,
        type: 'textarea',
        editable: true,
      }
      try {
        let response = await SteadyStateConsumptionApiService.getSteadyStateConsumptionWithColumns(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

        if (response?.data?.columns) {
          const excludedColumns = [
            'Material_FK_Id',
            'Site_FK_Id',
            'Plant_FK_ID',
            'Vertical_FK_Id',
            'Material',
            'FinancialYear',
            'NormParameterDisplayOrder',
            'TypeDisplayName',
            'SAPMaterialCode',
            'UOM',
            'IsEditable',
            'FinancialYear',
            'Remarks',
            'CreatedOn',
            'ModifiedOn',
            'MCUVersion',
            'UpdatedBy',
            'NormParameterTypeId',
            'NormParameterTypeName',
            'NormParameterTypeDisplayName',
            'ProductName',
            'YTDNorms',
            'YTD',
          ]

          const dynamicApiColumns = response.data.columns
            .filter((col) => !excludedColumns.includes(col.field))
            .map((col) => {
              let colDef = {
                ...col,
                minWidth: 150,
                editable: col.field === 'WtAvg (YTD)' ? false : true,
                type: col.type === 'number' ? 'row-based' : 'text',
                allowNegative: true
              }
              if (col.type === 'number') {
                colDef.format = valueFormat
              }
              return colDef
            })

          setColumns([
            ...staticInitialColumns,
            ...dynamicApiColumns,
            remarksColumn,
          ])
        }

        setCalculationObject(response?.data?.aopCalculation || [])

        const mappedData =
          response?.data?.data || response?.data?.mcuNormsValueDTOList || []
        const formattedData = mappedData.map((item, index) => ({
          ...item,
          idFromApi: item.id || item.Material_FK_Id,
          id: `${index}`,
          originalRemark: item.Remarks,
          remarks: item.Remarks,
          productName: item.Particulars || item.ProductName,
          normParameterTypeDisplayName:
            item.NormParameterTypeDisplayName || item.TypeDisplayName,
          isEditable:
            typeof item.IsEditable === 'number'
              ? item.IsEditable === 0
                ? false
                : true
              : item.IsEditable,
          type: 'number1',
          sapCode: item.SAPMaterialCode,
          allowNegative: true,
        }))

        setRows(formattedData)
        setOriginalRows(formattedData)
      } catch (error) {
        console.error('Error fetching steady state consumption data:', error)
        setRows([])
        setOriginalRows([])
        setCalculationObject([])
        setColumns([...staticInitialColumns, remarksColumn])
      } finally {
        setLoading(false)
      }
    },
    [PLANT_ID, AOP_YEAR, keycloak],
  )

  const validateGradeNorms = useCallback(
    async (gradeId) => {
      if (!gradeId || !PLANT_ID || !AOP_YEAR) return
      try {
        const valRes =
          await SteadyStateConsumptionApiService.validateGradeSteadyStateNorms(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
            gradeId,
          )
        if (
          valRes &&
          (valRes.code === 400 || valRes.status === 400 || valRes.code === 200) &&
          Array.isArray(valRes.data) &&
          valRes.data.length > 0
        ) {
          setValidationErrors(valRes.data)
          setValidationErrorDialogOpen(true)
        }
      } catch (err) {
        console.error('Error validating grade steady state norms:', err)
      }
    },
    [PLANT_ID, AOP_YEAR, keycloak],
  )

  // ===================== Initial load & Grade change =====================

  useEffect(() => {
    if (!PLANT_ID || !AOP_YEAR) return
    setRows([])
    setOriginalRows([])
    setGrades([])
    setSelectedGradeId(null)
    Promise.all([fetchData()])
  }, [PLANT_ID, AOP_YEAR, fetchData])

  // Re-fetch data & validate when selectedGradeId changes
  // useEffect(() => {
  //     // fetchData(selectedGradeId)
  //     if (!isFilament) validateGradeNorms(selectedGradeId)
  // }, [isFilament, validateGradeNorms])

  const saveChanges = useCallback(async () => {
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      return
    }

    const fieldsToCheck = columns
      .filter(
        (col) =>
          ![
            'id',
            'productName',
            'normParameterTypeDisplayName',
            'sapCode',
            'UOM',
            'remarks'
          ].includes(col.field)
      )
      .map((col) => col.field)
    const validationError = validateRowDataWithRemarks(
      modifiedData,
      originalRows,
      fieldsToCheck,
      'productName',
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

    setLoading(true)
    try {
      const payload = modifiedData.map((row) => {
        const rowPayload = {
          Site_FK_Id: row.Site_FK_Id || null,
          Plant_FK_ID: row.Plant_FK_ID || PLANT_ID,
          Vertical_FK_Id: row.Vertical_FK_Id || null,
          Material_FK_Id: row.Material_FK_Id || null,
          FinancialYear: row.FinancialYear || AOP_YEAR,
          Remarks: row.remarks || '', // Send exactly 'Remarks' as backend expects
          CreatedOn: row.CreatedOn || null,
          ModifiedOn: row.ModifiedOn || null,
          MCUVersion: row.MCUVersion || null,
          UpdatedBy: row.UpdatedBy || null,
          NormParameterTypeId: row.NormParameterTypeId || null,
          NormParameterTypeName: row.NormParameterTypeName || null,
          NormParameterTypeDisplayName: row.NormParameterTypeDisplayName || null,
          UOM: row.UOM || '',
          IsEditable: row.IsEditable !== undefined ? row.IsEditable : null,
          ProductName: row.ProductName || null,
          SAPMaterialCode: row.SAPMaterialCode || null,
          NormParameterDisplayOrder: row.NormParameterDisplayOrder || null,
          WtAvg: row.WtAvg !== undefined ? row.WtAvg : null,
        }
        
        // Append all dynamic grade values as keys
        fieldsToCheck.forEach((field) => {
          const val = row[field]
          rowPayload[field] = (val === '' || val === null || val === undefined) ? '' : Number(val)
        })
        
        return rowPayload
      })

      const response =
        await SteadyStateConsumptionApiService.saveDynamicSteadyStateConsumption(
          PLANT_ID,
          payload,
          keycloak,
          AOP_YEAR,
        )

      let isWeightedAverageError = false
      let errorMsg = ''
      let errorDataList = []

      if (response && (response.code === 400 || response.status === 400)) {
        isWeightedAverageError = true
        errorMsg = response.message || 'Validation failed.'
        if (response && Array.isArray(response.data)) {
          errorDataList = response.data
        }
      }

      if (isWeightedAverageError && !isFilament) {
        if (errorDataList && errorDataList.length > 0) {
          setValidationErrors(errorDataList)
          setValidationErrorDialogOpen(true)
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: errorMsg || 'Weighted average does not match.',
            severity: 'error',
          })
        }
      } else if (response && !(response instanceof Response)) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Saved Successfully!', severity: 'success' })
        setModifiedCells({})
        await fetchData(selectedGradeId)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Norms not saved!', severity: 'error' })
      }
    } catch (error) {
      console.error('Error saving steady state consumption:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error saving data!', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [
    modifiedCells,
    PLANT_ID,
    AOP_YEAR,
    keycloak,
    selectedGradeId,
    fetchData,
    isFilament
  ])

  // ===================== Calculate (PE uses site + vertical — same as NormalOpNorms handleCalculateNormalOperationNormsPe) =====================

  const handleCalculate = async () => {
    setLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({ message: 'Calculating...', severity: 'info' })
    try {
      let data;
      // if (isFilament) {
        data = await SteadyStateConsumptionApiService.calculateSteadyStateConsumptionPolyester(
          PLANT_ID,
          SITE_ID,
          VERTICAL_ID,
          AOP_YEAR,
          keycloak,
        )
      // } else {
      //   data = await SteadyStateConsumptionApiService.calculateSteadyStateConsumptionPE(
      //     PLANT_ID,
      //     SITE_ID,
      //     VERTICAL_ID,
      //     AOP_YEAR,
      //     keycloak,
      //   )
      // }

      if (data == 0 || data) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })
        // await fetchGrades()
        await fetchData(selectedGradeId)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Calculation failed. Please try again.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error calculating steady state consumption:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'Calculation failed.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  // ===================== Export (PE all-grades export — same as NormalOpNorms getNormalOpsNormsExcelpe) =====================

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({ message: 'Excel export started!', severity: 'info' })
    try {
      await SteadyStateConsumptionApiService.exportSteadyStateConsumptionDynamic(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        EXCEL_NAME,
      )
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting steady state consumption:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  // ===================== Import (with gradeId — same as NormalOpNorms saveNormalOpsNormsExcel) =====================

  const handleImport = async (file) => {
    setLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({ message: 'Importing data...', severity: 'info' })
    try {
      const response =
        await SteadyStateConsumptionApiService.importSteadyStateConsumptionDynamic(
          file,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          // selectedGradeId,
        )

      let isWeightedAverageError = false
      let errorMsg = ''
      let errorDataList = []

      if (
        response &&
        response.code === 400 &&
        (response.message === 'Validation Failed' || Array.isArray(response.data))
      ) {
        isWeightedAverageError = true
        errorMsg = response.message || 'Import validation failed.'
        errorDataList = Array.isArray(response.data) ? response.data : []
      }

      if (isWeightedAverageError) {
        if (errorDataList && errorDataList.length > 0) {
          setValidationErrors(errorDataList)
          setValidationErrorDialogOpen(true)
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: errorMsg || 'Weighted average does not match.',
            severity: 'error',
          })
        }
      } else if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchData(selectedGradeId)
      } else if (response?.code === 400 && response?.data) {
        // Partial save — download error file
        downloadBase64Excel(
          response.data,
          'Error File Grade Wise Steady State Consumption.xlsx',
        )
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        await fetchData(selectedGradeId)
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Import failed. Please try again.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error importing steady state consumption:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Import failed. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable) return
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
    showImport: true,
    showCalculate: true,
    // Disable Calculate if no calculationObject from API (same as NormalOpNorms showCalculateVisibility)
    calculateDisabled:
      !calculationObject || Object.keys(calculationObject).length === 0,
    ExcelName: EXCEL_NAME,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Grade Wise Overall Steady State Consumption (Norm)',
    // Grade dropdown (showG → showDropdown: true for PE)
    showDropdown: false,
    remarksEditable: true,
    marginBottom: true,
    showNote: true,
    note: `(UOM is applicable from Raw Materials onwards)`,
  }

  // Grade dropdown config (same as NormalOpNorms dropdownLabel: 'Grade' for isPEPP)
  const dropdownConfig = {
    options: grades,
    label: 'Grade',
    placeholder: 'Select Grade',
    valueKey: 'gradeId', // grade objects use 'gradeId' not 'id'
    labelKey: 'displayName',
  }

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />

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
        setCurrentRowId={() => { }}
        saveChanges={saveChanges}
        handleExport={handleExport}
        handleExcelUpload={handleImport}
        handleCalculate={handleCalculate}
        dropdownConfig={dropdownConfig}
        selectedDropdownValue={selectedGradeId || ''}
        setSelectedDropdownValue={setSelectedGradeId}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        groupBy={['normParameterTypeDisplayName']}
        customHeight={70}
        paginationConfig={{
          threshold: 100,
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 100,
        }}
      />

      {/* Detailed Validation Error Modal */}
      <ValidationErrorDialog
        open={validationErrorDialogOpen}
        onClose={() => {
          setValidationErrorDialogOpen(false)
          setValidationErrors([])
        }}
        errors={validationErrors}
      />
    </Box>
  )
}

export default GradeWiseSteadyStateConsumption
