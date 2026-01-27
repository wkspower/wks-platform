import { useGridApiRef } from '@mui/x-data-grid'
import { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import KendoDataTables from './index'
import { ExclusionDateColumns } from 'components/colums/ShutdownColumn'
import { ExclusionDateApiDataService } from 'services/exclusion-date-api-service'
import { getRoleName } from 'services/role-service'
import { DataService } from 'services/DataService'
import { validateFields } from 'utils/validationUtils'

const ExclusionDate = ({ permissions }) => {
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
  const [deleteId, setDeleteId] = useState(null)
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
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const colDefs = ExclusionDateColumns

  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    setCurrentRemark(row.remark || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const fetchData = async () => {
    if (!PLANT_ID || !AOP_YEAR) return
    setModifiedCells({})
    try {
      setLoading(true)
      const data = await ExclusionDateApiDataService.getExclusionDate(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      const modifiedData = data?.data?.Data?.map((item, index) => ({
        ...item,
        idFromApi: item?.id,
        id: index,
        originalRemark: item.remark,
        reason: item.remark,
        exclusionEndDate: item?.endDate ? new Date(item.endDate) : null,
        exclusionStartDate: item?.startDate ? new Date(item.startDate) : null,
      }))

      setRows(modifiedData)
    } catch (error) {
      console.error('Error fetching data:', error)
    } finally {
      setLoading(false)
    }
  }

  const getConfigurationExecutionDetails = async () => {
    try {
      const response = await DataService.getConfigurationExecutionDetails(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      const details = response?.data || []
      if (details.length === 0) {
        console.warn(
          'getConfigurationExecutionDetails returned an empty array:',
          response,
        )
      }
      // const hasNoModifiedOn = details.length && !details[0]?.ModifiedOn
      const hasNoModifiedOn = true
      if (hasNoModifiedOn) {
        const startDateObj = details.find((item) => item.Name === 'StartDate')
        const endDateObj = details.find((item) => item.Name === 'EndDate')

        setStartDate(startDateObj?.AttributeValue)
        setEndDate(endDateObj?.AttributeValue)
      }
    } catch (error) {
      console.error('Error fetching getConfigurationExecutionDetails:', error)
    } finally {
      // setLoading1(false)
    }
  }

  useEffect(() => {
    fetchData()
    getConfigurationExecutionDetails()
  }, [oldYear, yearChanged, keycloak, PLANT_ID, AOP_YEAR])

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
      titleName: 'Exclusion Date',
      uploadExcelBtn: true,
    },
    IS_OLD_YEAR,
  )

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
        const rowStart = new Date(row.exclusionStartDate)
        const rowEnd = new Date(row.exclusionEndDate)

        // --- 2. Validation: Start Date < End Date ---
        if (rowStart > rowEnd) {
          setSnackbarData({
            message: `Row ${i + 1}: Start date cannot be after end date.`,
            severity: 'error',
          })
          setSnackbarOpen(true)
          return // Stop execution
        }

        // --- 3. Validation: Within Global Range (Inclusive) ---
        if (rowStart < limitStart || rowEnd > limitEnd) {
          setSnackbarData({
            message: `Row ${i + 1}: Dates must be between ${startDate} and ${endDate}.`,
            severity: 'error',
          })
          setSnackbarOpen(true)
          return
        }

        // --- 4. Validation: No Overlapping with other rows ---
        for (let j = i + 1; j < newRows.length; j++) {
          const otherStart = new Date(newRows[j].exclusionStartDate)
          const otherEnd = new Date(newRows[j].exclusionEndDate)

          // Logic: Two ranges overlap if (StartA <= EndB) AND (EndA >= StartB)
          if (rowStart <= otherEnd && rowEnd >= otherStart) {
            setSnackbarData({
              message: `Row ${i + 1} and Row ${j + 1} have overlapping dates.`,
              severity: 'error',
            })
            setSnackbarOpen(true)
            return
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
        payloadData.push({
          id: row?.idFromApi || null,
          endDate: row?.exclusionEndDate,
          startDate: row?.exclusionStartDate,
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
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }

      const rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      if (data.length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
        setLoading(false)
        return
      }

      await saveAPI(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    } finally {
      setLoading(false)
    }
  }, [modifiedCells])

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
        columns={colDefs}
        rows={rows}
        paginationOptions={[100, 200, 300]}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        apiRef={apiRef}
        deleteId={deleteId}
        open1={open1}
        setDeleteId={setDeleteId}
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
        deleteRowData={deleteRowData}
        permissions={adjustedPermissions}
        handleExcelUpload={handleExcelUpload}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
      />
    </div>
  )
}

export default ExclusionDate
