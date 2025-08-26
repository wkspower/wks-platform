import { useGridApiRef } from '@mui/x-data-grid'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import KendoDataTables from './index'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useDispatch } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import { validateFields } from 'utils/validationUtils'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import { styled } from '@mui/material/styles'
import getNormalOpNormColDef from 'components/data-tables/CommonHeader/getNormalOpNormColDef'
import {
  Box,
  Typography,
  Tab,
  Tabs,
} from '../../../node_modules/@mui/material/index'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const NormalOpNormsScreen = () => {
  const [modifiedCells, setModifiedCells] = React.useState({})
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
  const [rowsExpression, setRowsExpression] = useState()
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
  const { sitePlantChange, verticalChange, yearChanged, oldYear, plantID } =
    dataGridStore
  const isOldYear = oldYear?.oldYear

  const [_plantID, set_PlantID] = useState('')

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()
  const dispatch = useDispatch()
  const headerMap = generateHeaderNames(localStorage.getItem('year'))

  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const keycloak = useSession()

  const fetchData = async (gradeId) => {
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
        response = await DataService.getCrackerOperationNormsData(
          keycloak,
          gradeId,
          'Best Achieved',
        )
        response2 = await DataService.getCrackerOperationNormsData(
          keycloak,
          gradeId,
          'Expression',
        )
        response3 = await DataService.getCrackerOperationNormsData(
          keycloak,
          gradeId,
          'Yearly Norms',
        )
      } else {
        response = await DataService.getNormalOperationNormsData(
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
      const response = await DataService.getNormalOperationNormsGrades(keycloak)

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
      const res = await DataService.getIntermediateValues(keycloak)
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
      const res = await DataService.getNormTransactions(keycloak)
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
  }, [oldYear, yearChanged, keycloak, gradeId, plantID])

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

  const colDefs2 = [
    {
      field: 'isChecked',
      type: 'switch',
      widthT: 30,
      filter: false,
    },
    {
      field: 'materialDisplayName',
      title: 'Particulars',
    },

    {
      field: 'uom',
      title: 'UOM / MT',

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

  const handleRemarkCellClick = (row) => {
    if (!row?.isEditable) return
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
      /* empty */
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
        const response = await DataService.saveNormalOperationNormsData(
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
        data = await DataService.handleCalculateNormalOperationNormsPe(
          plantId,
          siteID,
          verticalId,
          year,
          keycloak,
        )
      } else {
        data = await DataService.handleCalculateNormalOperationNorms(
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
      showCalculate: lowerVertName === 'cracker' ? false : true,
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
      showCheckbox: lowerVertName === 'cracker' ? true : false,
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
      await DataService.getNormalOpsNormsExcel(keycloak, gradeId)

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

      const response = await DataService.saveNormalOpsNormsExcel(
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

  const [selectedTab, setSelectedTab] = useState(0)
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
        const response = await DataService.updateCrackerOperationNormsData(
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

  // BACKUP CODE

  //   const handleGlobalCheckboxChange = (
  //   gridName,
  //   id,
  //   materialName,
  //   field,
  //   value,
  //   dataItem,
  //   itemId,
  // ) => {
  //   const uniqueItemId = `${gridName}-${id}` // unique per row per grid

  //   // Helper to update rows in a grid
  //   const updateGridRows = (setRowsFunc, currentGridName) => {
  //     setRowsFunc((prev) =>
  //       prev.map((row) =>
  //         row.id === id && gridName === currentGridName
  //           ? { ...row, [field]: value }
  //           : row.materialName === materialName &&
  //               !(row.id === id && gridName === currentGridName)
  //             ? { ...row, [field]: false }
  //             : row,
  //       ),
  //     )
  //   }

  //   // Update all grids
  //   updateGridRows(setRows, 'main')
  //   updateGridRows(setRowsExpression, 'expression')
  //   updateGridRows(setRowsBestAchivedIndividual, 'best')

  //   // Merge changes into modifiedCells for all rows
  //   setModifiedCells((prev) => ({
  //     ...prev,
  //     [uniqueItemId]: {
  //       ...(prev[uniqueItemId] || {}),
  //       ...dataItem,
  //       [field]: value,
  //     },
  //   }))
  // }

  const handleGlobalCheckboxChange = (
    gridName,
    id,
    materialName,
    field,
    value,
    dataItem,
    itemId,
  ) => {
    const uniqueItemId = `${gridName}-${id}` // clicked row
    const uncheckedRows = [] // store rows that get unchecked automatically

    // Helper to update rows in a grid
    const updateGridRows = (setRowsFunc, currentGridName) => {
      setRowsFunc((prev) =>
        prev.map((row) => {
          if (row.id === id && gridName === currentGridName) {
            return { ...row, [field]: value } // clicked row
          }
          if (
            row.materialName === materialName &&
            !(row.id === id && gridName === currentGridName)
          ) {
            uncheckedRows.push({ row, gridName: currentGridName }) // collect unchecked rows
            return { ...row, [field]: false } // keep your uncheck logic
          }
          return row
        }),
      )
    }

    // Update all grids
    updateGridRows(setRows, 'main')
    updateGridRows(setRowsExpression, 'expression')
    updateGridRows(setRowsBestAchivedIndividual, 'best')

    // Merge clicked row + unchecked rows into modifiedCells
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
              label='Final monthly norms preview'
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
          columns={colDefs2}
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

      {selectedTab === 0 &&
        (lowerVertName === 'cracker' || lowerVertName === 'meg') && (
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
    </div>
  )
}

export default NormalOpNormsScreen
