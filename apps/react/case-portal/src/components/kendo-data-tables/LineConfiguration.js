import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useGridApiRef } from '@mui/x-data-grid'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { ExclusionDateApiDataService } from 'services/exclusion-date-api-service'
import { LineConfigurationApiDataService } from 'services/line-configuration-api-service'
import { getRoleName } from 'services/role-service'
import { useSession } from 'SessionStoreContext'
import KendoDataTables from './index'

const LineConfiguration = ({
  permissions,
  revision,
  loadBtnClicked,
  summary,
  summaryEdited,
  setSummaryEdited,
}) => {
  const [modifiedCells, setModifiedCells] = useState({})
  const dataGridStore = useSelector((state) => state.dataGridStore)

  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantObject,
    siteObject,
    year,
    screenTitle,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const PLANT_NAME_NO_CASE = plantObject?.name?.toUpperCase()
  const SITE_NAME_NO_CASE = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME_NO_CASE = verticalChange?.selectedVertical?.toUpperCase()
  const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}`
  const AOP_YEAR = year?.selectedYear
  const [open1, setOpen1] = useState(false)
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [startDate, setStartDate] = useState(null)
  const [endDate, setEndDate] = useState(null)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const keycloak = useSession()
  const IS_OLD_YEAR = false
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  let FORMATE_VALUE = '{0:0.000}'

  const [allGradesRecipes, setAllGradesRecipes] = useState([])
  const [loadingGrades, setLoadingGrades] = useState(false)
  const [gradesError, setGradesError] = useState(null)

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const productionColumns = getEnhancedAOPColDefs({
    handleRemarkCellClick,
    configType: 'lines',
    FORMATE_VALUE,
    allGradesRecipes,
  })

  const getAllGrades = async () => {
    setLoadingGrades(true)
    setGradesError(null)

    try {
      const response = await LineConfigurationApiDataService.getAllLines(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (!response || typeof response !== 'object') {
        throw new Error('Invalid API response')
      }

      const { code, message, data } = response

      if (code !== 200) {
        throw new Error(message || 'API returned non-200 code')
      }

      const mappedData = Array.isArray(data) ? data : []

      setAllGradesRecipes(mappedData)
    } catch (error) {
      console.error(' Error fetching data:', error)

      setGradesError(error.message || 'Failed to load data')
      setAllGradesRecipes([])
    } finally {
      setLoadingGrades(false)
    }
  }

  const fetchData = async () => {
    if (!PLANT_ID || !AOP_YEAR) return

    setModifiedCells({})

    try {
      setLoading(true)

      const response = await LineConfigurationApiDataService.getData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (!response || response.code !== 200) {
        console.error('API Error:', response?.message)
        return
      }

      const rawData = response.data || []

      const formattedData = rawData.map((item, index) => {
        const converted = {}

        Object.entries(item).forEach(([key, value]) => {
          if (
            key !== 'UOM' &&
            typeof value === 'string' &&
            value.trim() !== '' &&
            !isNaN(value)
          ) {
            converted[key] = value.includes('.')
              ? parseFloat(value)
              : parseInt(value, 10)
          } else {
            converted[key] = value
          }
        })

        return {
          ...converted,
          id: index,
          TypeDisplayName: item?.TypeDisplayName || 'Recipe',
        }
      })

      setRows(formattedData)
    } catch (error) {
      console.error('Error fetching Line Configuration:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    setModifiedCells({})
    fetchData()
    getAllGrades()
  }, [
    oldYear,
    yearChanged,
    keycloak,
    PLANT_ID,
    AOP_YEAR,
    revision,
    loadBtnClicked,
  ])

  const deleteRowData = async (paramsForDelete) => {
    setLoading(true)
    try {
      const { idFromApi, id } = paramsForDelete
      const deleteIdLocal = id
      if (!idFromApi) {
        setRows((prevRows) =>
          prevRows.filter((row) => row.id !== deleteIdLocal),
        )
      } else {
        await ExclusionDateApiDataService.deleteExclusionDate(
          idFromApi,
          keycloak,
        )
        setRows((prevRows) =>
          prevRows.filter((row) => row.id !== deleteIdLocal),
        )
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        // refresh list
        await fetchData()
      }
    } catch (error) {
      console.error('Error deleting Record', error)
    } finally {
      setLoading(false)
    }
  }

  const downloadExcelForConfiguration = async () => {
    try {
      let response

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Excel download started!',
        severity: 'success',
      })

      response = await ExclusionDateApiDataService.exportExclusionDate(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        EXCEL_EXPORT_TITLE,
      )

      return response
    } catch (error) {
      console.error('Error downloading Excel:', error)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(true)
    }
  }

  const importExcel = async (rawFile) => {
    setLoading(true)

    try {
      let response

      response = await ExclusionDateApiDataService.importExclusionDate(
        rawFile,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchData()
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteNumbers = Array.from(byteCharacters, (char) =>
          char.charCodeAt(0),
        )
        const byteArray = new Uint8Array(byteNumbers)

        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })

        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'Error File - Line Configuration.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        await fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'Upload Failed!', severity: 'error' })
      }

      return response
    } catch (error) {
      console.error('Error uploading excel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = (rawFile) => {
    importExcel(rawFile)
  }

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      downloadExcelBtn: false,
      uploadExcelBtn: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? true,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      customHeight: permissions?.customHeight,
      allAction: true,
      downloadExcelBtn: true,
      showTitleNameBusiness: true,
      titleName: 'Line Configuration',
      uploadExcelBtn: false,
    },
    IS_OLD_YEAR,
  )

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

        setSnackbarOpen(true)
        // setIsEdited(false)
      } else {
        setSnackbarData({
          message: 'Saved Failed!',
          severity: 'error',
        })
      }
      return response
    } catch (error) {
      console.error('Error saving Summary!', error)
    } finally {
      setLoading(false)
    }
  }

  const handleUpdate = async (updatedRows) => {
    setLoading(true)
    try {
      const payloadMap = {}

      // Loop grade rows
      updatedRows.forEach((row) => {
        const gradeId = row.GradeId

        Object.entries(row)
          .filter(([key]) => /^[0-9A-Fa-f-]{36}$/.test(key)) // Line UUID columns
          .forEach(([lineId, value]) => {
            if (!payloadMap[lineId]) {
              payloadMap[lineId] = {
                lineId,
                grades: {},
              }
            }

            payloadMap[lineId].grades[gradeId] = String(value)
          })
      })

      // Convert map to array
      const payload = Object.values(payloadMap)

      if (payload.length > 0) {
        const response = await LineConfigurationApiDataService.postData(
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
          fetchData()
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

  const saveChanges = useCallback(async () => {
    setLoading(true)

    try {
      if (Object.keys(modifiedCells).length === 0) {
        if (summaryEdited) {
          await saveSummary(summary)
          setModifiedCells({})
          setSummaryEdited(false)
        }
        return
      }

      const rawData = Object.values(modifiedCells)

      console.log(rawData)

      const data = rawData.filter((row) => row.inEdit)
      if (data.length === 0) {
        setLoading(false)
        return
      }

      await handleUpdate(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    } finally {
      setLoading(false)
    }
  }, [modifiedCells, summaryEdited, summary])

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        setRows={setRows}
        columns={productionColumns}
        rows={rows}
        paginationOptions={[100, 200, 300]}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        open1={open1}
        setOpen1={setOpen1}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        handleRemarkCellClick={handleRemarkCellClick}
        fetchData={fetchData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        permissions={adjustedPermissions}
        handleExcelUpload={handleExcelUpload}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
        summaryEdited={summaryEdited}
      />
    </div>
  )
}

export default LineConfiguration
