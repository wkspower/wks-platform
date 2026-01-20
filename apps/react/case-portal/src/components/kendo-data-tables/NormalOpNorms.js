import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import getNormalOpNormColDef from 'components/data-tables/CommonHeader/getNormalOpNormColDef'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { useSession } from 'SessionStoreContext'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import { validateFields } from 'utils/validationUtils'
import { Box, Typography } from '../../../node_modules/@mui/material/index'
import KendoDataTables from './index'
import NormalOpNormsScreenCracker from './NormalOpNormsCrakcer'
import ValueFormatterConsumption from 'utils/ValueFormatterConsumption'
import { getRoleName } from 'services/role-service'
const NormalOpNormsScreen = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})

  const [allRedCell, setAllRedCell] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const [calculationObject, setCalculationObject] = useState([])
  const [grades, setGrades] = useState([])
  const [open1, setOpen1] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [rowsIntermediateValues, setRowsIntermediateValues] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [loading, setLoading] = useState(false)
  const [gradeId, setGradeId] = useState(null)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    screenTitle,
    year,
  } = dataGridStore
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear

  const [_plantID, set_PlantID] = useState('')

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const SCREEN_NAME = screenTitle?.title

  const PLANT_NAME_NO_CASE = plantObject?.name?.toUpperCase()
  const SITE_NAME_NO_CASE = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME_NO_CASE = verticalObject?.name?.toUpperCase()

  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}`

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const dispatch = useDispatch()
  const headerMap = generateHeaderNames(AOP_YEAR)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const isPEPP = lowerVertName === 'pe' || lowerVertName === 'pp'
  const isPET = lowerVertName === 'pet'

  const keycloak = useSession()
  // const READ_ONLY = getRoleName(keycloak)
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const fetchData = async (gradeId) => {
    if (!PLANT_ID || !AOP_YEAR) return
    if ((isPEPP || isPET) && !gradeId) return
    setLoading(true)
    let response

    try {
      {
        response =
          await NormalOperationNormsApiService.getNormalOperationNormsData(
            keycloak,
            gradeId,
            false,
            PLANT_ID,
            AOP_YEAR,
          )
      }

      setCalculationObject(response?.data?.aopCalculation)

      let mappedData = response?.data?.mcuNormsValueDTOList

      let formattedData

      formattedData = mappedData?.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: `${index}`,
        originalRemark: item.remarks,
        Particulars: item.normParameterTypeDisplayName,
        isEditable: isPEPP ? item.normParameterTypeName === 'CatChem' : true,
      }))

      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching Data:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchGradeDropdowns = async () => {
    try {
      const response =
        await NormalOperationNormsApiService.getNormalOperationNormsGrades(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

      if (response?.code === 200) {
        setGrades(response?.data)
        if (Array.isArray(response?.data) && response?.data?.length === 0) {
          setLoading(false)
        }
      }
    } catch (error) {
      setGrades([])
      console.error('Error fetching data:', error)
    }
  }

  const fetchDataIntermediateValues = async () => {
    try {
      const res = await NormalOperationNormsApiService.getIntermediateValues(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.code == 200) {
        const formattedData = res?.data.map((item, index) => {
          const formattedItem = {
            ...item,
            isEditable: false,
            id: index,
            Particulars: item.NormTypeName,
          }
          return formattedItem
        })
        setRowsIntermediateValues(formattedData)
      }
    } catch (error) {
      console.error('Error fetching data:', error)
    }
  }

  const getNormTransactions = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    try {
      const res = await NormalOperationNormsApiService.getNormTransactions(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      if (res?.code == 200) {
        const normalized = res?.data.map((obj) => ({
          ...obj,
          normParameterFKId: obj.normParameterFKId.toUpperCase(),
        }))
        setAllRedCell(normalized)
        // setAllRedCell([])
      }
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      // handleMenuClose();
    }
  }

  const fetchAllData = async (gradeId) => {
    setLoading(true)
    setRows([])
    setRowsIntermediateValues([])
    setAllRedCell([])
    setGrades([])

    try {
      const promises = [fetchData(gradeId), getNormTransactions()]

      if (lowerVertName === 'meg') {
        promises.push(fetchDataIntermediateValues())
      }
      if (isPEPP || isPET) {
        promises.push(fetchGradeDropdowns())
      }

      await Promise.all(promises)
    } catch (error) {
      console.error('Error during data fetching:', error)
    } finally {
      // setLoading(false)
      // console.log(2)
    }
  }

  useEffect(() => {
    fetchAllData(gradeId)
  }, [oldYear, yearChanged, keycloak, gradeId, PLANT_ID, AOP_YEAR])

  const valueFormat = ValueFormatterConsumption()

  const colDefs = getNormalOpNormColDef({
    headerMap,
    valueFormat,
    lowerVertName,
  })

  const colDefsIntermediateValues = [
    {
      field: 'Particulars',
      title: 'Type',
      width: 110,
      groupable: true,
      editable: false,
      hidden: true,
    },

    {
      field: 'NormParameterFKId',
      title: 'Particulars',
      hidden: true,
    },

    {
      field: 'ProductName',
      title: 'Particulars',
      widthT: 130,
    },
    {
      field: 'UOM',
      title: 'UOM',
      widthT: 60,
      editable: false,
    },
    {
      field: 'Apr',
      title: headerMap[4],
      editable: false,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number',
    },

    {
      field: 'May',
      title: headerMap[5],
      editable: false,
      width: 120,
      align: 'right',
      format: valueFormat,
      type: 'number',
    },
    {
      field: 'Jun',
      title: headerMap[6],
      editable: false,
      type: 'number',
      width: 120,
      align: 'right',
      format: valueFormat,
    },
    {
      field: 'Jul',
      title: headerMap[7],
      editable: false,
      type: 'number',
      width: 120,
      align: 'right',
      format: valueFormat,
    },

    {
      field: 'Aug',
      title: headerMap[8],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: valueFormat,
    },
    {
      field: 'Sep',
      title: headerMap[9],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'Oct',
      title: headerMap[10],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: valueFormat,
    },
    {
      field: 'Nov',
      title: headerMap[11],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'Dec',
      title: headerMap[12],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'Jan',
      title: headerMap[1],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: valueFormat,
    },
    {
      field: 'Feb',
      title: headerMap[2],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: valueFormat,
    },
    {
      field: 'Mar',
      title: headerMap[3],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: valueFormat,
    },
    {
      field: 'idFromApi',
      title: 'idFromApi',
      hidden: true,
    },
    {
      field: 'isEditable',
      title: 'isEditable',
      hidden: true,
    },
  ]

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable || READ_ONLY) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCells)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }

      const requiredFields = ['remarks']
      const validationMessage = validateFields(data, requiredFields)
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        return
      }

      saveNormalOperationNormsData(data)
    } catch (error) {
      console.log(error)
    }
  }, [modifiedCells])

  const saveNormalOperationNormsData = async (newRows) => {
    setLoading(true)
    try {
      const payload = newRows.map((row) => ({
        april: row.april || null,
        may: row.may || null,
        june: row.june || null,
        july: row.july || null,
        august: row.august || null,
        september: row.september || null,
        october: row.october || null,
        november: row.november || null,
        december: row.december || null,
        january: row.january || null,
        february: row.february || null,
        march: row.march || null,
        remark: row.remarks,
        remarks: row.remarks,
        financialYear: AOP_YEAR,
        plantId: PLANT_ID,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
        materialFkId: row.materialFkId || null,
        mcuVersion: row.mcuVersion || null,
        plantFkId: row.plantFkId || null,
        siteFkId: row.siteFkId || null,
        verticalFkId: row.verticalFkId || null,
        unit: row.unit || null,
        normParameterTypeId: row.normParameterTypeId || null,
        gradeId: row.gradeId || gradeId || null,
      }))

      if (payload.length > 0) {
        const response =
          await NormalOperationNormsApiService.saveNormalOperationNormsData(
            PLANT_ID,
            payload,
            keycloak,
            gradeId,
            lowerVertName,
            AOP_YEAR,
          )

        // if (response.status === 200) {
        if (response) {
          dispatch(setIsBlocked(false))
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Saved Successfully!`,
            severity: 'success',
          })

          setModifiedCells({})
          unsavedChangesRef.current = {
            unsavedRows: {},
            rowsBeforeChange: {},
          }

          fetchData(gradeId)
          if (lowerVertName == 'meg') fetchDataIntermediateValues()
          getNormTransactions()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Norms not saved!`,
            severity: 'error',
          })
        }
        return response
      }
    } catch (error) {
      console.error(`Error saving Norms`, error)
    } finally {
      // fetchData()
      // setLoading(false)
    }
  }

  const isCellEditable = (params) => {
    return params.row.isEditable
  }

  const handleCalculate = async () => {
    setRows([])
    setLoading(true)
    try {
      var data = null

      if (isPEPP || isPET) {
        data =
          await NormalOperationNormsApiService.handleCalculateNormalOperationNormsPe(
            PLANT_ID,
            SITE_ID,
            VERTICAL_ID,
            AOP_YEAR,
            keycloak,
          )
      } else {
        data =
          await NormalOperationNormsApiService.handleCalculateNormalOperationNorms(
            PLANT_ID,
            AOP_YEAR,
            keycloak,
          )
      }

      if (data == 0 || data) {
        // dispatch(setIsBlocked(true))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })

        if (isPEPP || isPET) fetchGradeDropdowns()
        fetchData(gradeId)
        if (lowerVertName == 'meg') fetchDataIntermediateValues()
        getNormTransactions()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Refresh Falied!',
          severity: 'error',
        })
      }

      return data
    } catch (error) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: error.message || 'An error occurred',
        severity: 'error',
      })

      console.error('Error!', error)
    }
  }

  const getAdjustedPermissions = (permissions, isOldYear) => {
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
      isOldYear: isOldYear,
      showCalculate: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      allAction: true,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: true,
      saveBtn: true,
      showCalculate: true,
      downloadExcelBtnFromUI: false,
      showCheckbox: false,
      showG: isPEPP || isPET ? true : false,
      marginBottom: isPEPP || isPET ? true : false,
      dropdownLabel: isPEPP || isPET ? 'Select Grade' : 'Select Mode',
      showCalculateVisibility:
        Object.keys(calculationObject || {}).length > 0 ? true : false,
      showTitleNameBusiness: true,
      titleName:
        !isPEPP || !isPET ? SCREEN_NAME : 'Steady State Consumption (Norm)',
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      isHeight: lowerVertName !== 'meg' && rows?.length > 10,
    },
    isOldYear,
  )

  const adjustedPermissionsIV = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      showCalculate: false,
      allAction: true,
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Intermediate Values`,
    },
    isOldYear,
  )

  const handleExcelUpload = (rawFile) => {
    saveExcelFile(rawFile)
  }

  const downloadExcelForConfiguration = async () => {
    setLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      if (isPEPP || isPET) {
        await NormalOperationNormsApiService.getNormalOpsNormsExcelpe(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          EXCEL_EXPORT_TITLE,
          SCREEN_NAME,
        )
      } else {
        await NormalOperationNormsApiService.getNormalOpsNormsExcel(
          keycloak,
          gradeId,
          PLANT_ID,
          AOP_YEAR,
          EXCEL_EXPORT_TITLE,
          SCREEN_NAME,
        )
      }

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error!', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      // optional cleanup or logging
      setLoading(false)
    }
  }

  const saveExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      const response =
        await NormalOperationNormsApiService.saveNormalOpsNormsExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
          gradeId,
        )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchAllData(gradeId)
      } else if (response?.code === 400 && response?.data) {
        // Partial save, error file download
        const byteCharacters = atob(response.data)
        const byteNumbers = new Array(byteCharacters.length)
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i)
        }
        const byteArray = new Uint8Array(byteNumbers)
        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'Error File Steady state Norms.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Save Failed!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  const handleGradeChange = (gradeId, gradeDisplayName) => {
    setGradeId(gradeId)
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {lowerVertName != 'cracker' && (
        <KendoDataTables
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          columns={colDefs}
          setRows={setRows}
          rows={rows}
          grades={grades}
          onAddRow={(newRow) => console.log('New Row Added:', newRow)}
          onDeleteRow={(id) => console.log('Row Deleted:', id)}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          paginationOptions={[100, 200, 300]}
          saveChanges={saveChanges}
          isCellEditable={isCellEditable}
          snackbarData={snackbarData}
          handleCalculate={handleCalculate}
          snackbarOpen={snackbarOpen}
          apiRef={apiRef}
          open1={open1}
          setOpen1={setOpen1}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          unsavedChangesRef={unsavedChangesRef}
          handleRemarkCellClick={handleRemarkCellClick}
          permissions={adjustedPermissions}
          allRedCell={allRedCell}
          groupBy='Particulars'
          handleExcelUpload={handleExcelUpload}
          downloadExcelForConfiguration={downloadExcelForConfiguration}
          handleGradeChange={handleGradeChange}
          plantID={plantID}
          gridName='main'
        />
      )}

      {lowerVertName === 'meg' && (
        <Box sx={{ width: '100%', marginTop: 1 }}>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='grid-content'
              id='grid-header'
            >
              <Typography component='span' className='grid-title'>
                Intermediate Values
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <KendoDataTables
                  title='Intermediate Values'
                  columns={colDefsIntermediateValues}
                  setRows={setRowsIntermediateValues}
                  rows={rowsIntermediateValues}
                  paginationOptions={[100, 200, 300]}
                  permissions={adjustedPermissionsIV}
                  groupBy='NormTypeName'
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </Box>
      )}

      {true && lowerVertName === 'cracker' && <NormalOpNormsScreenCracker />}
    </div>
  )
}

export default NormalOpNormsScreen
