import { DataService } from 'services/DataService'

import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import {
  Box,
  Button,
  TextField,
  Typography,
} from '../../../node_modules/@mui/material/index'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import KendoDataTables from './index'
import KendoDataTablesReports from './index-reports'
import KendoDataTablesReciepe from './index-reports-receipe'

const SelectivityData = (props) => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [allGradesReciepes, setAllGradesReciepes] = useState(null)
  const [configurationExecutionDetails, setConfigurationExecutionDetails] =
    useState(null)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [allProducts, setAllProducts] = useState([])
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [isEdited, setIsEdited] = useState(false)

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const saveChanges = React.useCallback(async () => {
    try {
      var data = Object.values(modifiedCells)
      if (data.length === 0) {
        //here it is emtpy
        saveSummary(props?.summary)
        props?.onSummaryEditChange(false)
        return
      }

      if (props?.configType !== 'grades') {
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
        saveCatalystData(data)
      } else {
        handleUpdate(data)
      }
    } catch (error) {
      // Handle error if necessary
    }
  }, [modifiedCells, props.summary, props.onSummaryEditChange])

  const saveSummary = async (summary) => {
    try {
      let plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      let year = localStorage.getItem('year')
      const response = await DataService.saveSummaryAOPConsumptionNorm(
        plantId,
        year,
        summary,
        keycloak,
      )

      if (response?.code == 200) {
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })
        setLoading(false)
        setSnackbarOpen(true)
        // setIsEdited(false)
      } else {
        setSnackbarData({
          message: 'Saved Failed!',
          severity: 'error',
        })
        setLoading(false)
        // setSnackbarOpen(true)
      }

      //

      // setLoading(false)
      return response
    } catch (error) {
      console.error('Error saving Summary!', error)
    } finally {
      //
      setLoading(false)
    }
  }

  const saveCatalystData = async (newRow) => {
    setLoading(true)
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      var payload = []

      if (props?.configType == 'megConstants') {
        payload = newRow.map((row) => ({
          apr: row.apr || row.ConstantValue || null,
          may: row.apr || row.ConstantValue || null,
          jun: row.apr || row.ConstantValue || null,
          jul: row.apr || row.ConstantValue || null,
          aug: row.apr || row.ConstantValue || null,
          sep: row.apr || row.ConstantValue || null,
          oct: row.apr || row.ConstantValue || null,
          nov: row.apr || row.ConstantValue || null,
          dec: row.apr || row.ConstantValue || null,
          jan: row.apr || row.ConstantValue || null,
          feb: row.apr || row.ConstantValue || null,
          mar: row.apr || row.ConstantValue || null,
          UOM: '',
          auditYear: localStorage.getItem('year'),
          normParameterFKId: row.normParameterFKId || row.NormParameter_FK_Id,
          remarks: row.remarks,
          id: row.idFromApi || null,
        }))
      } else {
        payload = newRow.map((row) => ({
          apr: row.apr || row.ConstantValue || null,
          may: row.may || null,
          jun: row.jun || null,
          jul: row.jul || null,
          aug: row.aug || null,
          sep: row.sep || null,
          oct: row.oct || null,
          nov: row.nov || null,
          dec: row.dec || null,
          jan: row.jan || null,
          feb: row.feb || null,
          mar: row.mar || null,
          UOM: '',
          auditYear: localStorage.getItem('year'),
          normParameterFKId: row.normParameterFKId || row.NormParameter_FK_Id,
          remarks: row.remarks,
          id: row.idFromApi || null,
        }))
      }

      const response = await DataService.saveCatalystData(
        plantId,
        payload,
        keycloak,
      )
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Configuration data Saved Successfully!',
          severity: 'success',
        })

        setModifiedCells({})
        setLoading(false)

        saveSummary(props?.summary)
        props?.onSummaryEditChange(false)

        if (props?.configType !== 'grades' && lowerVertName !== 'cracker') {
          props?.fetchData()
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving Configuration data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  const handleUpdate = async (updatedRows) => {
    setLoading(true)
    try {
      const payload = updatedRows.map((row) => ({
        recId: row.Reciepe_FK_ID.toString(),
        grades: Object.entries(row)
          .filter(([key]) => /^[0-9A-Fa-f-]{36}$/.test(key))
          .reduce((acc, [key, value]) => {
            acc[key] = Number(value)
            return acc
          }, {}),
      }))

      if (payload.length > 0) {
        const response = await DataService.updatePeConfigData(keycloak, payload)
        if (response) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Configuration Data Saved Successfully!',
            severity: 'success',
          })
          fetchConfigData()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Configuration Data Saved failed!',
            severity: 'error',
          })
        }

        return response
      }
    } catch (error) {
      console.error('Error updating data:', error)
    } finally {
      setLoading(false)
    }
  }

  const isCellEditable = (params) => {
    if (lowerVertName != 'meg') {
      return !(
        params.row.Particulars ||
        params.row.isGroupHeader ||
        params.row.isSubGroupHeader
      )
    } else {
      return params.row.isEditable
    }
  }

  // const isCellEditable = (params) => {
  // }

  useEffect(() => {
    const getAllGrades = async () => {
      try {
        const data = await DataService.getAllGrades(keycloak)
        setAllGradesReciepes(data)
      } catch (error) {
        console.error('Error fetching Grades/Reciepes:', error)
      } finally {
        // handleMenuClose();
      }
    }
    const getConfigurationExecutionDetails = async () => {
      try {
        const data =
          await DataService.getConfigurationExecutionDetails(keycloak)

        var data1 = data?.data

        setConfigurationExecutionDetails(data1)
      } catch (error) {
        console.error('Error fetching getConfigurationExecutionDetails:', error)
      } finally {
        // handleMenuClose();
      }
    }

    if (verticalChange?.selectedVertical === 'PE') getAllGrades()

    if (props?.configType !== 'grades' && lowerVertName !== 'cracker') {
      props?.fetchData()
    }

    getConfigurationExecutionDetails()
    //if (props?.configType === 'grades') fetchConfigData()
  }, [
    sitePlantChange,
    oldYear,
    yearChanged,
    keycloak,
    lowerVertName,
    props?.configType,
  ])

  const fetchConfigData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getPeConfigData(keycloak)

      data = data.map((item, index) => ({
        ...item,
        id: index,
      }))

      props?.setRows(data)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    } finally {
      setLoading(false)
    }
  }

  const productionColumns = getEnhancedAOPColDefs({
    allGradesReciepes,
    allProducts,
    headerMap,
    handleRemarkCellClick,
    configType: props?.configType,
    // columnConfig,
  })

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
      downloadExcelBtn: false,
      uploadExcelBtn: false,
      isOldYear: isOldYear,
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: true,
      saveBtn: true,
      downloadExcelBtn: lowerVertName == 'meg' ? true : false,
      uploadExcelBtn: lowerVertName == 'meg' ? true : false,
      showLoad: lowerVertName == 'meg' ? true : false,
      allAction: true,
    },
    isOldYear,
  )
  const NormParameterIdCell = (props) => {
    const productId = props.dataItem.normParameterFKId
    const product = allProducts.find((p) => p.id === productId)
    const displayName = product?.displayName || ''
    return <td>{displayName ? displayName : props?.dataItem?.particulars}</td>
  }

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
      if (props?.tabIndex != 1) {
        await DataService.getConfigurationExcel(keycloak)
      } else {
        await DataService.getConfigurationExcelConstants(keycloak)
      }

      // If no error is thrown, the request was successful
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
  const handleLoad = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Execution Started !',
      severity: 'success',
    })

    try {
      await DataService.getConfigurationExcel(keycloak)
    } catch (error) {
      console.error('Error!', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to Execute SP.',
        severity: 'error',
      })
    } finally {
      // optional cleanup or logging
    }
  }

  // useEffect(() => {
  //   getAopSummary()
  // }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const saveExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      var plantId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }
      var response
      if (props?.tabIndex != 1) {
        response = await DataService.saveConfigurationExcel(rawFile, keycloak)
      } else {
        response = await DataService.saveConfigurationExcelConstants(
          rawFile,
          keycloak,
        )
      }
      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Configuration data Upload Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        setLoading(false)

        if (props?.configType !== 'grades' && lowerVertName !== 'cracker') {
          props?.fetchData()
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving Configuration data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  if (props?.configType == 'grades') {
    return (
      <div>
        <Box>
          <Backdrop
            sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
            open={!!loading}
          >
            <CircularProgress color='inherit' />
          </Backdrop>
          <KendoDataTablesReciepe
            handleRemarkCellClick={handleRemarkCellClick}
            NormParameterIdCell={NormParameterIdCell}
            modifiedCells={modifiedCells}
            setModifiedCells={setModifiedCells}
            columns={productionColumns}
            rows={props?.rows}
            setRows={props?.setRows}
            title='Configuration'
            summaryEdited={props?.summaryEdited}
            // isCellEditable={isCellEditable}
            // paginationOptions={[100, 200, 300]}
            saveChanges={saveChanges}
            snackbarData={snackbarData}
            snackbarOpen={snackbarOpen}
            apiRef={apiRef}
            setDeleteId={setDeleteId}
            setOpen1={setOpen1}
            setSnackbarOpen={setSnackbarOpen}
            setSnackbarData={setSnackbarData}
            deleteId={deleteId}
            open1={open1}
            remarkDialogOpen={remarkDialogOpen}
            setRemarkDialogOpen={setRemarkDialogOpen}
            currentRemark={currentRemark}
            setCurrentRemark={setCurrentRemark}
            currentRowId={currentRowId}
            permissions={adjustedPermissions}
            groupBy={props?.groupBy}
            handleExcelUpload={handleExcelUpload}
            downloadExcelForConfiguration={downloadExcelForConfiguration}
          />
        </Box>
      </div>
    )
  }

  return (
    <div>
      <Box>
        <Backdrop
          sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
          open={!!loading}
        >
          <CircularProgress color='inherit' />
        </Backdrop>
        <KendoDataTables
          handleRemarkCellClick={handleRemarkCellClick}
          NormParameterIdCell={NormParameterIdCell}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          columns={productionColumns}
          rows={props?.rows}
          setRows={props?.setRows}
          title='Configuration'
          summaryEdited={props?.summaryEdited}
          // isCellEditable={isCellEditable}
          // paginationOptions={[100, 200, 300]}
          saveChanges={saveChanges}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          apiRef={apiRef}
          setDeleteId={setDeleteId}
          setOpen1={setOpen1}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          deleteId={deleteId}
          open1={open1}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          permissions={adjustedPermissions}
          groupBy={props?.groupBy}
          handleExcelUpload={handleExcelUpload}
          downloadExcelForConfiguration={downloadExcelForConfiguration}
        />
      </Box>
    </div>
  )
}

export default SelectivityData
