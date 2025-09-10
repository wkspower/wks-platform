import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import getNormalOpNormColDef from 'components/data-tables/CommonHeader/getNormalOpNormColDef'
import { useDispatch } from 'react-redux'
import { NormalOperationNormsApiService } from 'services/normal-operation-norms-api-service'
import { useSession } from 'SessionStoreContext'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import { validateFields } from 'utils/validationUtils'
import {
  Box,
  Tab,
  Tabs,
  Typography,
} from '../../../node_modules/@mui/material/index'
import KendoDataTables from './index'

const NormalOpNormsScreen = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const [modifiedCellsFinalNorms, setModifiedCellsFinalNorms] = React.useState(
    {},
  )
  const [allProducts, setAllProducts] = useState([])
  const [allRedCell, setAllRedCell] = useState([])
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const [calculationObject, setCalculationObject] = useState([])
  const [grades, setGrades] = useState([])
  const [open1, setOpen1] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const [defaultSelect, setDefaultSelect] = useState()
  const [rowsBestAchivedIndividual, setRowsBestAchivedIndividual] = useState()
  const [rowsBestFinalNorms, setRowsBestFinalNorms] = useState()
  const [rowsExpression, setRowsExpression] = useState()
  const [rowsIntermediateValues, setRowsIntermediateValues] = useState()
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [remarkDialogOpenFinalNorms, setRemarkDialogOpenFinalNorms] =
    useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRemarkFinalNorms, setCurrentRemarkFinalNorms] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [currentRowIdFinalNorms, setCurrentRowIdFinalNorms] = useState(null)
  const [loading, setLoading] = useState(false)
  const [gradeId, setGradeId] = useState(null)
  const { sitePlantChange, verticalChange, yearChanged, oldYear, plantID } =
    dataGridStore
  const isOldYear = oldYear?.oldYear

  const [_plantID, set_PlantID] = useState('')

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const dispatch = useDispatch()
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [selectedTab, setSelectedTab] = useState(0)

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })
  const unsavedChangesRefFinalNorms = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const keycloak = useSession()

  const fetchFinalNorms = async () => {
    setLoading(true)
    let response
    try {
      response = await NormalOperationNormsApiService.getfinalNorms(keycloak)
      if (response?.code !== 200) {
        setRowsBestFinalNorms([])
        return
      }
      let mappedData = response?.data?.mcuNormsValueDTOList

      let formattedData

      formattedData = mappedData?.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: `${index}`,
        remarks: item?.remarks || '',
        originalRemark: item?.remarks || '',
        Particulars: item.normType,
      }))

      setRowsBestFinalNorms(formattedData)
    } catch (error) {
      console.error('Error fetching Data:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchData = async (gradeId) => {
    if (selectedTab == 1) {
      fetchFinalNorms()
      return
    }

    setRows([])
    setRowsExpression([])
    setRowsBestAchivedIndividual([])

    const verticalsRequiringGrade = ['pe', 'pp', 'cracker']
    if (verticalsRequiringGrade.includes(lowerVertName) && !gradeId) return
    setLoading(true)
    let response
    let response2
    let response3
    try {
      if (lowerVertName === 'cracker') {
        response = await NormalOperationNormsApiService.getModeWiseNormsData(
          keycloak,
          gradeId,
          'Best Achieved',
        )
        response2 = await NormalOperationNormsApiService.getModeWiseNormsData(
          keycloak,
          gradeId,
          'Expression',
        )
        response3 = await NormalOperationNormsApiService.getModeWiseNormsData(
          keycloak,
          gradeId,
          'Yearly Norms',
        )
      } else {
        response =
          await NormalOperationNormsApiService.getNormalOperationNormsData(
            keycloak,
            gradeId,
            false,
          )
      }

      setCalculationObject(response?.data?.aopCalculation)

      let mappedData = response?.data?.mcuNormsValueDTOList
      let mappedData2 = response2?.data?.mcuNormsValueDTOList
      let mappedData3 = response3?.data?.mcuNormsValueDTOList

      let formattedData

      if (lowerVertName === 'cracker') {
        formattedData = mappedData?.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: `main-${index}`,
          originalRemark: item.remarks,
          Particulars: item.normType,
        }))
      } else {
        formattedData = mappedData?.map((item, index) => ({
          ...item,
          idFromApi: item.id,
          id: `${index}`,
          originalRemark: item.remarks,
          Particulars: item.normParameterTypeDisplayName,
        }))
      }

      let formattedData2 = mappedData2?.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: `expression-${index}`,
        originalRemark: item.remarks,
        Particulars:
          lowerVertName === 'cracker'
            ? item.normType
            : item.normParameterTypeDisplayName,
      }))

      let formattedData3 = mappedData3?.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: `best-${index}`,
        originalRemark: item.remarks,
        Particulars:
          lowerVertName === 'cracker'
            ? item.normType
            : item.normParameterTypeDisplayName,
      }))

      setRows(formattedData)
      setRowsExpression(formattedData2)
      setRowsBestAchivedIndividual(formattedData3)
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
      const res =
        await NormalOperationNormsApiService.getIntermediateValues(keycloak)
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
    try {
      const res =
        await NormalOperationNormsApiService.getNormTransactions(keycloak)
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

      if (lowerVertName === 'meg' || lowerVertName === 'cracker') {
        promises.push(fetchDataIntermediateValues())
      }
      if (lowerVertName === 'pe' || lowerVertName === 'pp') {
        promises.push(fetchGradeDropdowns())
      }
      if (lowerVertName === 'cracker') {
        setGrades([
          {
            name: '4F',
            displayName: '4F',
            gradeId: '4F',
          },
          {
            name: '5F',
            displayName: '5F',
            gradeId: '5F',
          },
          {
            name: '4F+D',
            displayName: '4F+D',
            gradeId: '4F+D',
          },
          {
            name: 'Monthly',
            displayName: 'Monthly',
            gradeId: 'Monthly',
          },
        ])
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
  }, [oldYear, yearChanged, keycloak, gradeId, plantID, selectedTab])

  const colDefs = getNormalOpNormColDef({
    headerMap,
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
      format: '{0:#.###}',
      type: 'number',
    },

    {
      field: 'May',
      title: headerMap[5],
      editable: false,
      width: 120,
      align: 'right',
      format: '{0:#.###}',
      type: 'number',
    },
    {
      field: 'Jun',
      title: headerMap[6],
      editable: false,
      type: 'number',
      width: 120,
      align: 'right',
      format: '{0:#.###}',
    },
    {
      field: 'Jul',
      title: headerMap[7],
      editable: false,
      type: 'number',
      width: 120,
      align: 'right',
      format: '{0:#.###}',
    },

    {
      field: 'Aug',
      title: headerMap[8],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.###}',
    },
    {
      field: 'Sep',
      title: headerMap[9],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.###}',
    },
    {
      field: 'Oct',
      title: headerMap[10],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.###}',
    },
    {
      field: 'Nov',
      title: headerMap[11],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.###}',
    },
    {
      field: 'Dec',
      title: headerMap[12],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.###}',
    },
    {
      field: 'Jan',
      title: headerMap[1],
      editable: false,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.###}',
    },
    {
      field: 'Feb',
      title: headerMap[2],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.###}',
    },
    {
      field: 'Mar',
      title: headerMap[3],
      editable: false,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.###}',
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

  const colDefsIndividual = [
    {
      field: 'isChecked',
      type: 'switch',
      widthT: 30,
      filter: false,
    },

    {
      field: 'sapMaterialCode',
      title: 'SAP MAT Code',
      widthT: 120,
      editable: false,
    },
    {
      field: 'materialDisplayName',
      title: 'Particulars',
    },

    {
      field: 'uom',
      title: 'UOM',

      editable: false,
    },

    {
      field: 'april',
      title: 'Value',
      editable: true,
      align: 'right',
      format: '{0:#.###}',
      type: 'number',
    },
  ]

  const colDefsFinalNorms = [
    {
      field: 'sapMaterialCode',
      title: 'SAP MAT Code',
      widthT: 120,
      editable: false,
    },
    {
      field: 'materialDisplayName',
      title: 'Particulars',
      widthT: 130,
      editable: false,
    },
    {
      field: 'uom',
      title: 'UOM',
      widthT: 60,
      editable: false,
    },
    {
      field: 'april',
      title: headerMap[4],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:#.##}',
      type: 'number',
    },

    {
      field: 'may',
      title: headerMap[5],
      editable: true,
      width: 120,
      align: 'right',
      format: '{0:#.##}',
      type: 'number',
    },
    {
      field: 'june',
      title: headerMap[6],
      editable: true,
      type: 'number',
      width: 120,
      align: 'right',
      format: '{0:#.##}',
    },
    {
      field: 'july',
      title: headerMap[7],
      editable: true,
      type: 'number',
      width: 120,
      align: 'right',
      format: '{0:#.##}',
    },

    {
      field: 'august',
      title: headerMap[8],
      editable: true,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.##}',
    },
    {
      field: 'september',
      title: headerMap[9],
      editable: true,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.##}',
    },
    {
      field: 'october',
      title: headerMap[10],
      editable: true,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.##}',
    },
    {
      field: 'november',
      title: headerMap[11],
      editable: true,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.##}',
    },
    {
      field: 'december',
      title: headerMap[12],
      editable: true,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.##}',
    },
    {
      field: 'january',
      title: headerMap[1],
      editable: true,
      width: 120,
      align: 'right',
      type: 'number',
      format: '{0:#.##}',
    },
    {
      field: 'february',
      title: headerMap[2],
      editable: true,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.##}',
    },
    {
      field: 'march',
      title: headerMap[3],
      editable: true,
      width: 120,
      type: 'number',
      align: 'right',
      format: '{0:#.##}',
    },

    {
      field: 'isEditable',
      title: 'isEditable',
      hidden: true,
    },

    {
      field: 'remarks',
      title: 'Remark',
      widthT: 140,
      editable: true,
    },
  ]
  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable) return
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  const handleRemarkCellClickFinalNorms = (row) => {
    if (!row?.isEditable) return
    setCurrentRemarkFinalNorms(row.remarks || '')
    setCurrentRowIdFinalNorms(row.id)
    setRemarkDialogOpenFinalNorms(true)
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
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const businessData = newRows.map((row) => ({
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
        financialYear: localStorage.getItem('year'),
        plantId: plantId,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
        materialFkId: row.materialFkId || null,
        mcuVersion: row.mcuVersion || null,
        plantFkId: row.plantFkId || null,
        siteFkId: row.siteFkId || null,
        verticalFkId: row.verticalFkId || null,
        unit: row.unit || null,
        normParameterTypeId: row.normParameterTypeId || null,
      }))

      if (businessData.length > 0) {
        const response =
          await NormalOperationNormsApiService.saveNormalOperationNormsData(
            plantId,
            businessData,
            keycloak,
            gradeId,
            lowerVertName,
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
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var plantId = plantId
      var data = null
      let siteID =
        JSON.parse(localStorage.getItem('selectedSiteId') || '{}')?.id || ''
      let verticalId = localStorage.getItem('verticalId')

      if (lowerVertName == 'pe' || lowerVertName == 'pp') {
        data =
          await NormalOperationNormsApiService.handleCalculateNormalOperationNormsPe(
            plantId,
            siteID,
            verticalId,
            year,
            keycloak,
          )
      } else {
        data =
          await NormalOperationNormsApiService.handleCalculateNormalOperationNorms(
            plantId,
            year,
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

        if (lowerVertName == 'pe' || lowerVertName == 'pp')
          fetchGradeDropdowns()
        fetchData(gradeId)
        if (lowerVertName == 'meg' || lowerVertName == 'cracker')
          fetchDataIntermediateValues()
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
  const handleCalculateFinalNorms = async () => {
    setLoading(true)
    try {
      const storedPlant = localStorage.getItem('selectedPlant')
      const year = localStorage.getItem('year')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var plantId = plantId
      var data = null

      data = await NormalOperationNormsApiService.calculateFinalNorms(
        plantId,
        year,
        keycloak,
      )

      if (data == 0 || data) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data refreshed successfully!',
          severity: 'success',
        })

        fetchData(gradeId)
        if (lowerVertName == 'meg' || lowerVertName == 'cracker') {
          fetchDataIntermediateValues()
        }
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

      downloadExcelBtnFromUI: lowerVertName === 'cracker' ? true : false,
      ExcelName: `${lowerVertName}_BestAcheived(Min CC)`,

      showCheckbox: lowerVertName === 'cracker' ? true : false,
      marginBottom: lowerVertName === 'cracker' ? true : false,
      showG:
        lowerVertName === 'pe' ||
        lowerVertName === 'pp' ||
        lowerVertName === 'cracker'
          ? true
          : false,
      dropdownLabel:
        lowerVertName === 'pe' || lowerVertName === 'pp'
          ? 'Select Grade'
          : 'Select Mode',

      showCalculateVisibility:
        Object.keys(calculationObject || {}).length > 0 ? true : false,

      // showTitleNameBusiness: lowerVertName === 'cracker' ? true : false,
      titleName: 'Best Achieved (Min CC)',

      downloadExcelBtn: lowerVertName === 'cracker' ? false : true,
      uploadExcelBtn: lowerVertName === 'cracker' ? false : true,
      isHeight: lowerVertName !== 'meg' && rows?.length > 10,
    },
    isOldYear,
  )
  const adjustedPermissionsExpression = getAdjustedPermissions(
    {
      showAction: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Expression (Norms)',
      showCalculate: false,
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Expression_(Norms)`,
      showCheckbox: lowerVertName === 'cracker' ? true : false,
    },
    isOldYear,
  )

  const adjustedPermissionsFinalNorms = getAdjustedPermissions(
    {
      showAction: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Final (Norms)',
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Final_Norms`,
      saveWithRemark: true,
      saveBtn: true,
      showCalculate: true,
    },
    isOldYear,
  )

  const adjustedPermissionsBestAchivedIndividual = getAdjustedPermissions(
    {
      showAction: false,
      allAction: true,
      showTitleNameBusiness: true,
      titleName: 'Best Achieved (Individual)',
      showCheckbox: lowerVertName === 'cracker' ? true : false,
      downloadExcelBtnFromUI: true,
      ExcelName: `${lowerVertName}_Best Achieved (Norms)`,
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
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      await NormalOperationNormsApiService.getNormalOpsNormsExcel(
        keycloak,
        gradeId,
      )

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
    }
  }

  const saveExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const response =
        await NormalOperationNormsApiService.saveNormalOpsNormsExcel(
          rawFile,
          keycloak,
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
  const handleGradeChange = (gradeId) => {
    setGradeId(gradeId)
  }

  const handleTabChange = (event, newValue) => {
    setModifiedCells({})
    setSelectedTab(newValue)

    if (newValue === 0) {
      setModifiedCells({})
      fetchAllData(gradeId)
    } else if (newValue === 1) {
      // fetchAllData(gradeId)
    }
  }
  const saveNormalOperationNormsDataCracker = async (newRows) => {
    setLoading(true)
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const payload = newRows.map((row) => ({
        april: row.april || 0,
        may: row.may || 0,
        june: row.june || 0,
        july: row.july || 0,
        august: row.august || 0,
        september: row.september || 0,
        october: row.october || 0,
        november: row.november || 0,
        december: row.december || 0,
        january: row.january || 0,
        february: row.february || 0,
        march: row.march || 0,
        remark: row.remark || row.remarks || '',
        isChecked: row.isChecked || false,
        id: row.idFromApi || row.id || null,
        materialFKId: row.materialFKId || row.materialFkId || null,
      }))

      // console.log('payload', payload)
      setLoading(false)

      if (payload.length > 0) {
        const response =
          await NormalOperationNormsApiService.updateModeWiseNormsData(
            keycloak,
            gradeId,
            payload,
          )

        if (response?.code == 200) {
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
            message: `Data not saved!`,
            severity: 'error',
          })
        }
        return response
      }
    } catch (error) {
      console.error(`Error saving Data`, error)
    } finally {
      // fetchData()
      // setLoading(false)
    }
  }
  const saveNormalOperationFinalNorms = async (newRows) => {
    setLoading(true)
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const payload = newRows.map((row) => ({
        april: row.april || 0,
        may: row.may || 0,
        june: row.june || 0,
        july: row.july || 0,
        august: row.august || 0,
        september: row.september || 0,
        october: row.october || 0,
        november: row.november || 0,
        december: row.december || 0,
        january: row.january || 0,
        february: row.february || 0,
        march: row.march || 0,
        isChecked: row.isChecked || false,
        id: row.idFromApi || row.id || null,
        materialFKId: row.materialFKId || row.materialFkId || null,
        remarks: row.remarks || row.remarks || '',
        remark: row.remarks || row.remarks || '',
      }))

      // console.log('payload', payload)

      if (payload.length > 0) {
        const response =
          await NormalOperationNormsApiService.updateFinalNormsData(
            keycloak,
            gradeId,
            payload,
          )

        if (response?.code == 200) {
          dispatch(setIsBlocked(false))
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Saved Successfully!`,
            severity: 'success',
          })

          setLoading(false)

          setModifiedCellsFinalNorms({})
          unsavedChangesRefFinalNorms.current = {
            unsavedRows: {},
            rowsBeforeChange: {},
          }
          fetchData(gradeId)
          if (lowerVertName == 'meg') fetchDataIntermediateValues()
          getNormTransactions()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: `Data not saved!`,
            severity: 'error',
          })
          setLoading(false)
        }
        return response
      }
    } catch (error) {
      console.error(`Error saving Data`, error)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  const saveChangesCracker = React.useCallback(async () => {
    try {
      const data = Object.values(modifiedCells)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }
      saveNormalOperationNormsDataCracker(data)
    } catch (error) {
      console.error('Error saving Cracker Data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving Cracker Data!',
        severity: 'error',
      })
    }
  }, [modifiedCells])
  const saveChangesCrackerFinalNorms = React.useCallback(async () => {
    try {
      const data = Object.values(modifiedCellsFinalNorms)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        return
      }
      saveNormalOperationFinalNorms(data)
    } catch (error) {
      console.error('Error saving Cracker Data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving Cracker Data!',
        severity: 'error',
      })
    }
  }, [modifiedCellsFinalNorms])

  const handleGlobalCheckboxChange = (
    gridName,
    id,
    materialName,
    field,
    value,
    dataItem,
    itemId,
  ) => {
    unsavedChangesRefFinalNorms
    const uniqueItemId = `${gridName}-${id}`
    const uncheckedRows = []

    const updateGridRows = (setRowsFunc, currentGridName) => {
      setRowsFunc((prev) =>
        prev.map((row) => {
          if (row.id === id && gridName === currentGridName) {
            return { ...row, [field]: value }
          }
          if (
            row.materialName === materialName &&
            !(row.id === id && gridName === currentGridName)
          ) {
            uncheckedRows.push({ row, gridName: currentGridName })
            return { ...row, [field]: false }
          }
          return row
        }),
      )
    }

    updateGridRows(setRows, 'main')
    updateGridRows(setRowsExpression, 'expression')
    updateGridRows(setRowsBestAchivedIndividual, 'best')

    setModifiedCells((prev) => {
      const updated = {
        ...prev,
        [uniqueItemId]: {
          ...(prev[uniqueItemId] || {}),
          ...dataItem,
          [field]: value,
        },
      }

      uncheckedRows.forEach(({ row, gridName }) => {
        const rowUniqueId = `${gridName}-${row.id}`
        updated[rowUniqueId] = {
          ...(prev[rowUniqueId] || {}),
          ...row,
          [field]: false && prevValue === true,
        }
      })

      return updated
    })
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {lowerVertName === 'cracker' && (
        <Box style={{ margin: 0, padding: 0 }}>
          <Tabs
            value={selectedTab}
            onChange={handleTabChange}
            sx={{
              borderBottom: '0px solid #ccc',
              '.MuiTabs-indicator': { display: 'none' },
              margin: '0px 0px 0px 0px',
              minHeight: '28px',
            }}
          >
            <Tab
              label='Mode wise selection'
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
            />

            <Tab
              label='Final monthly norms'
              sx={{
                border: '1px solid #ADD8E6',
                borderBottom: '1px solid #ADD8E6',
                fontSize: '0.75rem',
                padding: '9px',
                minHeight: '12px',
              }}
            />
          </Tabs>
        </Box>
      )}

      {lowerVertName === 'cracker' && selectedTab == 0 && (
        <Typography
          component='div'
          className='grid-title'
          sx={{
            marginBottom: '10px',
          }}
        >
          Best Achieved (Min CC)
        </Typography>
      )}

      {selectedTab === 0 && (
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
          saveChanges={
            lowerVertName === 'cracker' ? saveChangesCracker : saveChanges
          }
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
          onGlobalCheckboxChange={handleGlobalCheckboxChange}
          gridName='main'
        />
      )}

      {selectedTab === 0 && lowerVertName === 'cracker' && (
        <KendoDataTables
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          title='Normal Operations Norms'
          columns={colDefs}
          setRows={setRowsExpression}
          rows={rowsExpression}
          grades={grades}
          onAddRow={(newRow) => console.log('New Row Added:', newRow)}
          onDeleteRow={(id) => console.log('Row Deleted:', id)}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          paginationOptions={[100, 200, 300]}
          saveChanges={
            lowerVertName === 'cracker' ? saveChangesCracker : saveChanges
          }
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
          permissions={adjustedPermissionsExpression}
          allRedCell={allRedCell}
          groupBy='Particulars'
          handleExcelUpload={handleExcelUpload}
          downloadExcelForConfiguration={downloadExcelForConfiguration}
          handleGradeChange={handleGradeChange}
          plantID={plantID}
          onGlobalCheckboxChange={handleGlobalCheckboxChange}
          gridName='expression'
        />
      )}

      {selectedTab === 0 && lowerVertName === 'cracker' && (
        <KendoDataTables
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          title='Normal Operations Norms'
          columns={colDefsIndividual}
          setRows={setRowsBestAchivedIndividual}
          rows={rowsBestAchivedIndividual}
          grades={grades}
          onAddRow={(newRow) => console.log('New Row Added:', newRow)}
          onDeleteRow={(id) => console.log('Row Deleted:', id)}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          paginationOptions={[100, 200, 300]}
          saveChanges={
            lowerVertName === 'cracker' ? saveChangesCracker : saveChanges
          }
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
          permissions={adjustedPermissionsBestAchivedIndividual}
          allRedCell={allRedCell}
          groupBy='Particulars'
          handleExcelUpload={handleExcelUpload}
          downloadExcelForConfiguration={downloadExcelForConfiguration}
          handleGradeChange={handleGradeChange}
          plantID={plantID}
          onGlobalCheckboxChange={handleGlobalCheckboxChange}
          gridName='best'
        />
      )}

      {selectedTab === 0 && lowerVertName === 'meg' && (
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

      {selectedTab === 1 && (
        <KendoDataTables
          modifiedCells={modifiedCellsFinalNorms}
          setModifiedCells={setModifiedCellsFinalNorms}
          columns={colDefsFinalNorms}
          setRows={setRowsBestFinalNorms}
          rows={rowsBestFinalNorms}
          onAddRow={(newRow) => console.log('New Row Added:', newRow)}
          onDeleteRow={(id) => console.log('Row Deleted:', id)}
          onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
          paginationOptions={[100, 200, 300]}
          saveChanges={saveChangesCrackerFinalNorms}
          isCellEditable={isCellEditable}
          snackbarData={snackbarData}
          handleCalculate={handleCalculateFinalNorms}
          snackbarOpen={snackbarOpen}
          apiRef={apiRef}
          open1={open1}
          setOpen1={setOpen1}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          remarkDialogOpen={remarkDialogOpenFinalNorms}
          setRemarkDialogOpen={setRemarkDialogOpenFinalNorms}
          currentRemark={currentRemarkFinalNorms}
          setCurrentRemark={setCurrentRemarkFinalNorms}
          currentRowId={currentRowIdFinalNorms}
          unsavedChangesRef={unsavedChangesRefFinalNorms}
          handleRemarkCellClick={handleRemarkCellClickFinalNorms}
          permissions={adjustedPermissionsFinalNorms}
          groupBy='Particulars'
          plantID={plantID}
        />
      )}
    </div>
  )
}

export default NormalOpNormsScreen
