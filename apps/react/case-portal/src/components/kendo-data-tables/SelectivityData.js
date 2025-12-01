import { DataService } from 'services/DataService'
import { PIOImpactApiService } from 'services/Pio-Impact-api-service'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import { Box } from '../../../node_modules/@mui/material/index'
import { useGridApiRef } from '../../../node_modules/@mui/x-data-grid/index'
import KendoDataTables from './index'
import KendoDataTablesReciepe from './index-reports-receipe'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import ValueFormatterProductionProductionNormBasis from 'utils/ValueFormatterProduction_ProductionNormBasis'
import { getRoleName } from 'services/role-service'
const SelectivityData = (props) => {
  const [modifiedCells, setModifiedCells] = React.useState({})
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const SCREEN_NAME = screenTitle?.title

  const AOP_YEAR = year?.selectedYear
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak)

  const [loading, setLoading] = useState(false)
  const apiRef = useGridApiRef()
  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const [allGradesReciepes, setAllGradesReciepes] = useState(null)
  const reportTypes = props?.reportTypes
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
  const [gradeId, setGradeId] = useState(null)

  const [grades, setGrades] = useState([
    {
      name: 'Monthly',
      displayName: 'Monthly',
      gradeId: 'Monthly',
    },
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
  ])

  const [start, end] = AOP_YEAR.split('-').map(Number)
  const prevYearFormatted = `${start - 1}-${(start - 1 + 1).toString().slice(-2)}`

  const headerMap = generateHeaderNames(AOP_YEAR)

  const headerMapForPrevYear = generateHeaderNames(prevYearFormatted)

  const [isEdited, setIsEdited] = useState(false)

  const handleGradeChange = (gradeId) => {
    setGradeId(gradeId)
    props.setGradeId(gradeId)
  }

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
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
        //TST VALIDATION SEPERATED
        if (lowerVertName == 'meg') {
          const monthNameMap = {
            jan: 'January',
            feb: 'February',
            mar: 'March',
            apr: 'April',
            may: 'May',
            jun: 'June',
            jul: 'July',
            aug: 'August',
            sep: 'September',
            oct: 'October',
            nov: 'November',
            dec: 'December',
          }

          const monthFields = [
            'jan',
            'feb',
            'mar',
            'apr',
            'may',
            'jun',
            'jul',
            'aug',
            'sep',
            'oct',
            'nov',
            'dec',
          ]

          for (const row of data) {
            if ((row.productName || '').trim().toLowerCase() === 'tst') {
              const failedMonths = []

              for (const month of monthFields) {
                const value = Number(row[month])

                if (isNaN(value) || value < 100 || value > 370) {
                  failedMonths.push(monthNameMap[month])
                }
              }

              if (failedMonths.length > 0) {
                setSnackbarOpen(true)
                setSnackbarData({
                  message: `Invalid values detected for 'TST' in the following months (allowed range: 100 to 370): ${failedMonths.join(', ')}.`,
                  severity: 'error',
                })
                return
              }
            }
          }
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
      const response = await DataService.saveSummaryAOPConsumptionNorm(
        PLANT_ID,
        AOP_YEAR,
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
      var payload = []
      var response

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
          auditYear: AOP_YEAR,
          normParameterFKId: row.normParameterFKId || row.NormParameter_FK_Id,
          remarks: row.remarks,
          id: row.idFromApi || null,
        }))

        response = await DataService.saveCatalystData(
          PLANT_ID,
          payload,
          keycloak,
          AOP_YEAR,
        )
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
          auditYear: AOP_YEAR,
          normParameterFKId: row.normParameterFKId || row.NormParameter_FK_Id,
          remarks: row.remarks,
          id: row.idFromApi || null,
        }))

        response = await DataService.saveCatalystData(
          PLANT_ID,
          payload,
          keycloak,
          AOP_YEAR,
        )
      }

      if (response) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved Successfully!',
          severity: 'success',
        })

        setModifiedCells({})
        setLoading(false)

        saveSummary(props?.summary)
        props?.onSummaryEditChange(false)

        if (props?.configType !== 'grades' && lowerVertName !== 'cracker') {
          props?.fetchData(gradeId)
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Saved Failed!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error saving Configuration data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving data!',
        severity: 'error',
      })
    } finally {
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
        const response = await DataService.updatePeConfigData(
          keycloak,
          payload,
          PLANT_ID,
          AOP_YEAR,
        )
        if (response) {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Saved Successfully!',
            severity: 'success',
          })
          fetchConfigData()
        } else {
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Data Saved failed!',
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
        const data = await DataService.getAllGrades(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
        setAllGradesReciepes(data)
      } catch (error) {
        console.error('Error fetching Grades/Reciepes:', error)
      } finally {
        // handleMenuClose();
      }
    }
    const getConfigurationExecutionDetails = async () => {
      try {
        const data = await DataService.getConfigurationExecutionDetails(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )

        var data1 = data?.data

        setConfigurationExecutionDetails(data1)
      } catch (error) {
        console.error('Error fetching getConfigurationExecutionDetails:', error)
      } finally {
        // handleMenuClose();
      }
    }

    if (
      verticalChange?.selectedVertical === 'PE' ||
      verticalChange?.selectedVertical === 'PP'
    )
      getAllGrades()

    if (props?.configType !== 'grades') {
      // Fix: Check if it's PIO Impact and call without gradeId
      if (
        props?.configType === 'pioImpact' ||
        props?.configType === 'shutdownData'
      ) {
        props?.fetchData()
      } else {
        props?.fetchData(gradeId)
      }
    }

    getConfigurationExecutionDetails()
    //if (props?.configType === 'grades') fetchConfigData()
  }, [
    siteObject,
    oldYear,
    yearChanged,
    keycloak,
    lowerVertName,
    props?.configType,
    gradeId,
    AOP_YEAR,
    PLANT_ID,
  ])

  const fetchConfigData = async () => {
    setLoading(true)
    try {
      var data = await DataService.getPeConfigData(keycloak, PLANT_ID, AOP_YEAR)

      data = data.map((item, index) => ({
        ...item,
        id: index,
        TypeDisplayName: item?.TypeDisplayName
          ? item?.TypeDisplayName
          : 'Particulars',
      }))

      props?.setRows(data)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    } finally {
      setLoading(false)
    }
  }

  const type =
    props?.configType === 'megConstantsMannualEntry' ||
    props?.configType === 'Report Manual Entry'
  const selectedHeaderMap = !type ? headerMap : headerMapForPrevYear

  let FORMATE_VALUE = ''
  if (lowerVertName == 'elastomer') {
    FORMATE_VALUE = ValueFormatterProductionProductionNormBasis()
  } else {
    FORMATE_VALUE = ValueFormatterProduction()
  }

  const productionColumns = getEnhancedAOPColDefs({
    allGradesReciepes,
    allProducts,
    headerMap: selectedHeaderMap,
    handleRemarkCellClick,
    configType: props?.configType,
    FORMATE_VALUE,
  })

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButtons: false,
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
      addButtons: false,
      deleteButton: false,
      editButton: false,
      saveWithRemark: true,
      saveBtn: true,
      downloadExcelBtn: true,
      uploadExcelBtn: true,
      showLoad: true,
      allAction: true,

      showTitleNameBusiness: true,
      titleName:
        props?.currentTabDisplayName === 'Report Manual Entry'
          ? `${props?.currentTabDisplayName} (${prevYearFormatted})`
          : props?.currentTabDisplayName,

      // showG: props?.configType === 'cracker_configuration' ? true : false,
      showG: false,
      dropdownLabel: 'Select Mode',
      // marginTop: props?.configType === 'cracker_configuration' ? true : false,
      marginTop: false,
      isHeight: lowerVertName !== 'meg' && props?.rows?.length > 10,
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
      if (props?.configType === 'grades') {
        await DataService.getRecipeExcel(keycloak, PLANT_ID, AOP_YEAR)

        //NEW BUILD 17 NOV
      } else if (
        props?.configType === 'ShutdownNorms' ||
        props?.configType === 'Constant'
      ) {
        //NEW BUILD 17 NOV
        await DataService.getShutdownRateExcel(
          keycloak,
          props?.configType,
          PLANT_ID,
          AOP_YEAR,
        )
      } else if (props?.tabIndex != 1) {
        if (
          lowerVertName == 'pe' ||
          lowerVertName == 'pp' ||
          lowerVertName == 'pta'
        ) {
          await DataService.getConfigurationExcelType(
            keycloak,
            PLANT_ID,
            AOP_YEAR,
            [props?.configType],
          )
        } else {
          var report_t = []

          if (props?.tabIndex == 0) {
            report_t = reportTypes.filter(
              (type) =>
                type !== 'Report Manual Entry' &&
                type !== 'Shutdown' &&
                type !== 'PIO Impact',
            )
          }
          if (props?.tabIndex == 2) {
            report_t = reportTypes.filter(
              (type) => type == 'Report Manual Entry',
            )
          }
          if (props?.tabIndex == 3) {
            report_t = reportTypes.filter((type) => type == 'PIO Impact')
          }

          if (props?.tabIndex == 4) {
            report_t = reportTypes.filter((type) => type == 'Shutdown')
          }

          await DataService.getConfigurationExcel(
            keycloak,
            report_t,
            PLANT_ID,
            AOP_YEAR,
          )
        }
      } else {
        await DataService.getConfigurationExcelConstants(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
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

  const saveExcelFile = async (rawFile) => {
    setLoading(true)
    try {
      var response
      if (props?.configType === 'grades') {
        response = await DataService.saveRecipeExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      } else if (
        //NEW BUILD 17 NOV
        props?.configType === 'ShutdownNorms' ||
        props?.configType === 'Constant'
      ) {
        // Add shutdown rate specific upload
        response = await DataService.saveShutdownRateExcel(
          rawFile,
          keycloak,
          props?.configType,
          PLANT_ID,
          AOP_YEAR,
        )
      } else if (props?.tabIndex != 1) {
        response = await DataService.saveConfigurationExcel(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      } else {
        response = await DataService.saveConfigurationExcelConstants(
          rawFile,
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        )
      }
      if (response?.code == 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Data Upload Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        setLoading(false)

        if (props?.configType === 'cracker_configuration') {
          props?.fetchData(null)
        }
        if (props?.configType === 'cracker_constants') {
          if (typeof props.fetchData === 'function') {
            props.fetchData()
          }
        }

        if (props?.configType === 'grades') {
          fetchConfigData() // This was missing!
        } else if (
          props?.configType !== 'grades' &&
          lowerVertName !== 'cracker'
        ) {
          props?.fetchData(gradeId)
        }
      } else if (response?.code === 400 && response?.data) {
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
        link.setAttribute('download', 'Error File Configuration.xlsx')
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
          message: 'Data Saved Falied!',
          severity: 'error',
        })
      }
      // if (props?.configType !== 'grades') {
      //   props?.fetchData(gradeId)
      // }

      return response
    } catch (error) {
      console.error('Error saving Data:', error)
      setLoading(false)
    } finally {
      // fetchData()
      setLoading(false)
    }
  }

  const deleteRowData = async (paramsForDelete) => {
    setLoading(true)
    try {
      const { idFromApi, id } = paramsForDelete
      const deleteId = id

      // If the row is not saved to backend, just remove it locally
      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setLoading(false)
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Unsaved row deleted!',
          severity: 'info',
        })
        return
      }

      // If the row is saved, call backend API
      const response = await PIOImpactApiService.deletePIOImpact(
        idFromApi,
        keycloak,
      )
      if (response?.code === 200 || response?.code === 204) {
        props?.setRows((prevRows) =>
          prevRows.filter((row) => row.id !== deleteId),
        )
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'PIO Impact record deleted successfully!',
          severity: 'success',
        })
        if (props?.configType === 'pioImpact' && props?.fetchData) {
          await props.fetchData()
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Failed to delete PIO Impact record!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error deleting PIO Impact record', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error deleting record!',
        severity: 'error',
      })
    } finally {
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
          grades={grades}
          handleRemarkCellClick={handleRemarkCellClick}
          NormParameterIdCell={NormParameterIdCell}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          columns={productionColumns}
          rows={props?.rows}
          setRows={props?.setRows}
          title='Configuration'
          summaryEdited={props?.summaryEdited}
          currentTabDisplayName={props?.currentTabDisplayName}
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
          handleGradeChange={handleGradeChange}
          deleteRowData={deleteRowData}
        />
      </Box>
    </div>
  )
}

export default SelectivityData
