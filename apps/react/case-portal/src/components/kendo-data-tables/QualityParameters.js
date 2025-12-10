import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedColDefsQualityParameter from 'components/data-tables/CommonHeader/Kendo_QulityParameterHeader'
import React, { useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { ProductionNormsApiService } from 'services/production-norms-api-service'
import { getRoleName } from 'services/role-service'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import { validateFields } from 'utils/validationUtils'
import KendoDataTables from './index'
import getEnhancedColDefsPriceDifferential from 'components/data-tables/CommonHeader/Kendo_PriceDifferentialHeader'

const QualityParameters = ({ permissions }) => {
  const [calculationObject, setCalculationObject] = useState([])
  const keycloak = useSession()
  const apiRef = useGridApiRef()
  const apiReQualityParameter = useGridApiRef()
  const apiRefPriceDifferential = useGridApiRef()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const [_plantID, set_PlantID] = useState('')
  const valueFormat = ValueFormatterProduction()
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const PLANT_NAME = plantObject?.name
  const SITE_ID = siteObject?.id
  const SITE_NAME = siteObject?.name
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const [loading, setLoading] = useState(false)
  const [calculatebtnClicked, setCalculatebtnClicked] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })

  const headerMap = generateHeaderNames(AOP_YEAR)

  const [modifiedCellsQualityParameter, setModifiedCellsQualityParameter] =
    React.useState({})
  const [modifiedCellsPriceDifferential, setModifiedCellsPriceDifferential] =
    React.useState({})

  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [selectedUnit, setSelectedUnit] = useState('')
  const [rowsQualityParameter, setRowsQualityParameter] = useState([])
  const [rowsPriceDifferential, setRowsPriceDifferential] = useState([])
  const [
    remarkDialogOpenQualityParameter,
    setRemarkDialogOpenQualityParameter,
  ] = useState(false)

  const [
    remarkDialogOpenPriceDifferential,
    setRemarkDialogOpenPriceDifferential,
  ] = useState(false)

  const [currentRemarkQualityParameter, setCurrentRemarkQualityParameter] =
    useState('')
  const [currentRowIdQualityParameter, setCurrentRowIdQualityParameter] =
    useState(null)

  const [currentRemarkPriceDifferential, setCurrentRemarkPriceDifferential] =
    useState('')
  const [currentRowIdPriceDifferential, setCurrentRowIdPriceDifferential] =
    useState(null)

  const saveChangesQualityParameter = React.useCallback(async () => {
    try {
      var editedData = Object.values(modifiedCellsQualityParameter)

      if (editedData?.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const requiredFields = ['remarks']

      const validationMessage = validateFields(editedData, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

      updateData(editedData)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCellsQualityParameter])

  const saveChangesPriceDifferential = React.useCallback(async () => {
    try {
      var editedData = Object.values(modifiedCellsPriceDifferential)

      if (editedData?.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const requiredFields = ['remarks']

      const validationMessage = validateFields(editedData, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

      updateDataPriceDifferential(editedData)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
  }, [modifiedCellsPriceDifferential])

  const updateData = async (rowsToSave) => {
    setLoading(true)

    try {
      const PAYLOAD = (rowsToSave || []).map((row) => ({
        apr: row.apr ?? null,
        may: row.may ?? null,
        auditYear: AOP_YEAR,
        normParameterFKId: row.normParametersFKId,
        remarks: row.remarks ?? '',
        id: null,
      }))

      const response = await ProductionNormsApiService.saveQualityParameters(
        PLANT_ID,
        PAYLOAD,
        keycloak,
        AOP_YEAR,
      )

      // Adjust response check depending on your API (status, success flag, etc.)
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Saved Successfully!', severity: 'success' })

        // clear modified cells and refresh data
        setModifiedCellsQualityParameter({})
        await fetchDataQualityParameter()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Data Save Failed!', severity: 'error' })
      }
    } catch (error) {
      console.error('Error Saving data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error Saving data!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
      setCalculatebtnClicked(false)
    }
  }

  const updateDataPriceDifferential = async (rowsToSave) => {
    setLoading(true)

    try {
      const PAYLOAD = (rowsToSave || []).map((row) => ({
        apr: row.apr ?? null,
        auditYear: AOP_YEAR,
        normParameterFKId: row.normParametersFKId,
        remarks: row.remarks ?? '',
        id: null,
      }))

      const response = await ProductionNormsApiService.savePriceDifferential(
        PLANT_ID,
        PAYLOAD,
        keycloak,
        AOP_YEAR,
      )

      // Adjust response check depending on your API (status, success flag, etc.)
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Saved Successfully!', severity: 'success' })

        // clear modified cells and refresh data
        setModifiedCellsPriceDifferential({})
        await fetchDataPriceDifferential()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Data Save Failed!', severity: 'error' })
      }
    } catch (error) {
      console.error('Error Saving data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error Saving data!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
      setCalculatebtnClicked(false)
    }
  }

  const fetchDataQualityParameter = async () => {
    try {
      setLoading(true)
      const response = await ProductionNormsApiService.getQualityParameters(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code != 200) {
        setRowsQualityParameter([])
        setLoading(false)
        return
      }

      let dataSet = response?.data?.configurationDTOList

      var data = dataSet
        ?.map((product, index) => ({
          ...product,
          normParametersFKId: product.normParameterFKId,
          originalRemark: product.remarks,
          isEditable: product.isEditable,
          apr: product?.apr,
          Particulars: product.productName,
          idFromApi: product.id,
          id: index,
        }))
        .map(({ normParameterFKId, ...rest }) => rest)

      setRowsQualityParameter(data)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      setLoading(false)
    }
  }
  const fetchDataPriceDifferential = async () => {
    try {
      setLoading(true)
      const response = await ProductionNormsApiService.getPriceDifferential(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code != 200) {
        setRowsPriceDifferential([])
        setLoading(false)
        return
      }

      let dataSet = response?.data?.configurationDTOList

      var data = dataSet
        ?.map((product, index) => ({
          ...product,
          normParametersFKId: product.normParameterFKId,
          originalRemark: product.remarks,
          isEditable: product.isEditable,
          apr: product?.apr,
          Particulars: product.productName,
          idFromApi: product.id,
          id: index,
        }))
        .map(({ normParameterFKId, ...rest }) => rest)

      setRowsPriceDifferential(data)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchDataQualityParameter()
    fetchDataPriceDifferential()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak, selectedUnit])

  const productionColumnsQualityParameter = getEnhancedColDefsQualityParameter({
    headerMap,
    valueFormat,
  })

  const productionColumnsPriceDifferential =
    getEnhancedColDefsPriceDifferential({
      headerMap,
      valueFormat,
    })

  const getAdjustedPermissionsQualityParameter = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      showCalculate: false,
      isOldYear: isOldYear,
      showNote: true,
    }
  }

  const adjustedPermissionsForQualityParameter = useMemo(
    () =>
      getAdjustedPermissionsQualityParameter(
        {
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: true,
          showCalculate: false,
          allAction: true,
          showNote: true,
          showTitleNameBusiness: true,
          titleName: 'Quality Parameters',
          saveBtn: true,
          downloadExcelBtnFromUI: true,
          ExcelName: `${vertName}_Quality Parameters`,
        },
        isOldYear,
      ),
    [permissions, calculationObject, vertName, isOldYear],
  )

  const getAdjustedPermissionsPriceDifferential = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      showCalculate: false,
      isOldYear: isOldYear,
      showNote: true,
    }
  }

  const adjustedPermissionsForPriceDifferential = useMemo(
    () =>
      getAdjustedPermissionsPriceDifferential(
        {
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: true,
          showCalculate: false,
          allAction: true,
          showNote: true,
          showTitleNameBusiness: true,
          titleName: 'Price Differential',
          saveBtn: true,
          downloadExcelBtnFromUI: true,
          ExcelName: `${vertName}_Price Differential`,
        },
        isOldYear,
      ),
    [permissions, calculationObject, vertName, isOldYear],
  )

  const handleRemarkCellClick = (dataItem) => {
    if (READ_ONLY) return
    setCurrentRemarkQualityParameter(dataItem.remarks || '')
    setCurrentRowIdQualityParameter(dataItem.id)
    setRemarkDialogOpenQualityParameter(true)
  }
  const handleRemarkCellClickPriceDifferential = (dataItem) => {
    if (READ_ONLY) return
    setCurrentRemarkPriceDifferential(dataItem.remarks || '')
    setCurrentRowIdPriceDifferential(dataItem.id)
    setRemarkDialogOpenPriceDifferential(true)
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      {
        <KendoDataTables
          modifiedCells={modifiedCellsQualityParameter}
          setModifiedCells={setModifiedCellsQualityParameter}
          columns={productionColumnsQualityParameter}
          rows={rowsQualityParameter}
          setRows={setRowsQualityParameter}
          title={'Quality Parameters'}
          onAddRow={(newRow) => console.log('New Row Added:', newRow)}
          onDeleteRow={(id) => console.log('Row Deleted:', id)}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          paginationOptions={[100, 200, 300]}
          saveChanges={saveChangesQualityParameter}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          apiRef={apiReQualityParameter}
          fetchData={fetchDataQualityParameter}
          remarkDialogOpen={remarkDialogOpenQualityParameter}
          setRemarkDialogOpen={setRemarkDialogOpenQualityParameter}
          currentRemark={currentRemarkQualityParameter}
          setCurrentRemark={setCurrentRemarkQualityParameter}
          currentRowId={currentRowIdQualityParameter}
          permissions={adjustedPermissionsForQualityParameter}
          handleRemarkCellClick={handleRemarkCellClick}
        />
      }

      {
        <KendoDataTables
          modifiedCells={modifiedCellsPriceDifferential}
          setModifiedCells={setModifiedCellsPriceDifferential}
          columns={productionColumnsPriceDifferential}
          rows={rowsPriceDifferential}
          setRows={setRowsPriceDifferential}
          title={'Price Differential'}
          onAddRow={(newRow) => console.log('New Row Added:', newRow)}
          onDeleteRow={(id) => console.log('Row Deleted:', id)}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          paginationOptions={[100, 200, 300]}
          saveChanges={saveChangesPriceDifferential}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          apiRef={apiRefPriceDifferential}
          fetchData={fetchDataPriceDifferential}
          remarkDialogOpen={remarkDialogOpenPriceDifferential}
          setRemarkDialogOpen={setRemarkDialogOpenPriceDifferential}
          currentRemark={currentRemarkPriceDifferential}
          setCurrentRemark={setCurrentRemarkPriceDifferential}
          currentRowId={currentRowIdPriceDifferential}
          permissions={adjustedPermissionsForPriceDifferential}
          handleRemarkCellClick={handleRemarkCellClickPriceDifferential}
        />
      }
    </div>
  )
}

export default QualityParameters
