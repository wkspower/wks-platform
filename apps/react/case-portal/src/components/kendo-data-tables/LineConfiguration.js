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
        link.setAttribute('download', 'Error File - Exclusion Date.xlsx')
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
      addButton: permissions?.addButton ?? true,
      deleteButton: permissions?.deleteButton ?? true,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      customHeight: permissions?.customHeight,
      allAction: true,
      downloadExcelBtn: true,
      showTitleNameBusiness: true,
      titleName: 'Line Configuration',
      uploadExcelBtn: true,
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

  const saveAPI = async (newRows) => {
    // --- 1. Basic Structure Validation ---

    if (!newRows || newRows.length === 0) return

    // Convert limit states to Date objects for comparison
    const limitStart = new Date(startDate)
    const limitEnd = new Date(endDate)

    try {
      const payloadData = []

      for (let i = 0; i < newRows.length; i++) {
        const row = newRows[i]

        // --- 0. Validation: Both dates must be present ---
        if (!row.exclusionStartDate || !row.exclusionEndDate) {
          setSnackbarData({
            message: 'Both From date and To date are required.',
            severity: 'error',
          })
          setSnackbarOpen(true)
          return // Stop execution
        }

        const rowStart = new Date(row.exclusionStartDate)
        const rowEnd = new Date(row.exclusionEndDate)

        // --- 2. Validation: Start Date < End Date ---
        if (rowStart > rowEnd) {
          setSnackbarData({
            message: `From date cannot be after To date.`,
            severity: 'error',
          })
          setSnackbarOpen(true)
          return // Stop execution
        }

        // --- 3. Validation: Within Global Range (Inclusive) ---
        const normalizeDate = (date) => {
          const d = new Date(date)
          d.setHours(0, 0, 0, 0)
          return d
        }

        const rs = normalizeDate(rowStart)
        const re = normalizeDate(rowEnd)
        const ls = normalizeDate(limitStart)
        const le = normalizeDate(limitEnd)

        const formatDDMMYYYY = (date) => {
          if (!date) return ''
          const d = new Date(date)
          const day = String(d.getDate()).padStart(2, '0')
          const month = String(d.getMonth() + 1).padStart(2, '0')
          const year = d.getFullYear()
          return `${day}-${month}-${year}`
        }

        if (rs < ls || re > le) {
          setSnackbarData({
            message: `Dates must be between ${formatDDMMYYYY(startDate)} and ${formatDDMMYYYY(endDate)}.`,
            severity: 'error',
          })
          setSnackbarOpen(true)
          return
        }

        const allRows = [...rows, ...newRows]

        const parseDateSafe = (value) => {
          if (!value) return null

          // Case 1: Already a Date object
          if (value instanceof Date) {
            const d = new Date(value)
            d.setHours(0, 0, 0, 0)
            return d
          }

          // Case 2: String in DD-MM-YYYY
          if (typeof value === 'string' && value.includes('-')) {
            const parts = value.split('-')

            // DD-MM-YYYY
            if (parts[0].length === 2) {
              const [dd, mm, yyyy] = parts
              const d = new Date(yyyy, mm - 1, dd)
              d.setHours(0, 0, 0, 0)
              return d
            }

            // YYYY-MM-DD
            if (parts[0].length === 4) {
              const [yyyy, mm, dd] = parts
              const d = new Date(yyyy, mm - 1, dd)
              d.setHours(0, 0, 0, 0)
              return d
            }
          }

          return null
        }

        // --- Validation: No overlapping with existing + new rows ---
        for (let i = 0; i < allRows.length; i++) {
          const rowStart = parseDateSafe(allRows[i].exclusionStartDate)
          const rowEnd = parseDateSafe(allRows[i].exclusionEndDate)

          for (let j = i + 1; j < allRows.length; j++) {
            // Skip same row (important when editing)
            if (allRows[i].id === allRows[j].id) continue

            const otherStart = parseDateSafe(allRows[j].exclusionStartDate)
            const otherEnd = parseDateSafe(allRows[j].exclusionEndDate)

            if (rowStart <= otherEnd && rowEnd >= otherStart) {
              setSnackbarData({
                message: 'Overlapping dates not allowed.',
                severity: 'error',
              })
              setSnackbarOpen(true)
              return
            }
          }
        }

        // --- 5. Validation: reason must be non-empty and different from originalRemark ---
        const reason = (row?.remark ?? '').trim()
        const originalRemark = (row?.originalRemark ?? '').trim()

        if (!reason) {
          setSnackbarData({
            message: `Please add the Reason`,
            severity: 'error',
          })
          setSnackbarOpen(true)
          return
        }

        if (reason === originalRemark) {
          setSnackbarData({
            message: `Please update the Reason`,
            severity: 'error',
          })
          setSnackbarOpen(true)
          return
        }

        // If valid, push to payload
        const toLocalDateOnly = (date) => {
          if (!date) return null

          const d = new Date(date)
          const year = d.getFullYear()
          const month = String(d.getMonth() + 1).padStart(2, '0')
          const day = String(d.getDate()).padStart(2, '0')

          return `${year}-${month}-${day}` // YYYY-MM-DD (LOCAL)
        }

        // If valid, push to payload
        payloadData.push({
          id: row?.idFromApi || null,
          startDate: toLocalDateOnly(row?.exclusionStartDate),
          endDate: toLocalDateOnly(row?.exclusionEndDate),
          remark: row?.remark || row?.remarks,
        })
      }

      // --- 5. Proceed to API Call ---
      const response = await ExclusionDateApiDataService.postExclusionDate(
        payloadData,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (summaryEdited) {
        await saveSummary(summary)
        setSummaryEdited(false)
      }

      setSnackbarOpen(true)
      setSnackbarData({ message: 'Saved Successfully!', severity: 'success' })
      setModifiedCells({})
      await fetchData()
      return response
    } catch (error) {
      console.error('Error in saving data!', error)
      setSnackbarData({ message: 'Failed to save data.', severity: 'error' })
      setSnackbarOpen(true)
    }
  }

  const saveChanges = useCallback(async () => {
    setLoading(true)

    try {
      // CASE 1: only summary edited
      if (Object.keys(modifiedCells).length === 0) {
        if (summaryEdited) {
          await saveSummary(summary)
          setModifiedCells({})
          setSummaryEdited(false)
        }
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      if (data.length === 0) {
        setLoading(false)
        return
      }

      await saveAPI(data)
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
