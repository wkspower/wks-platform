import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'

import { MaintenanceDetailsApiService } from 'services/maintenance-details-api-service'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import crackercolumns from '../../assets/CrackerMaintenanceColumn.json'
import KendoDataTables from './index'
import { getRoleName } from 'services/role-service'
const MaintenanceProcessTable = ({ viewOnly }) => {
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak)

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
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const dataConfig = useMemo(
    () => ({
      serviceFn: () =>
        MaintenanceDetailsApiService.getCrackerMaintenanceData(
          keycloak,
          PLANT_ID,
          AOP_YEAR,
        ),
    }),
    [keycloak, PLANT_ID, AOP_YEAR],
  )

  const headerMap = generateHeaderNames(AOP_YEAR)

  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [deleteId, setDeleteId] = useState(null)
  const [open1, setOpen1] = useState(false)

  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const handleRemarkCellClick = (row) => {
    if (READ_ONLY) return
    // if (!row?.isEditable) return

    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  function isLeapYear(yearStr) {
    // yearStr is like "2025-26"
    if (!yearStr) return false
    const year = parseInt(yearStr.split('-')[0], 10)
    if (!year) return false
    return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0
  }
  const saveChanges = useCallback(async () => {
    try {
      setLoading(true)
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
      // --- MONTHLY SUM VALIDATION (move here) ---
      const febDays = isLeapYear(AOP_YEAR) ? 29 : 28
      const monthDays = {
        January: 31,
        February: febDays,
        March: 31,
        April: 30,
        May: 31,
        June: 30,
        July: 31,
        August: 31,
        September: 30,
        October: 31,
        November: 30,
        December: 31,
      }
      for (const row of data) {
        const month = row.monthName
        if (month && monthDays[month]) {
          const sum =
            Number(row.fourF || 0) +
            Number(row.fiveF || 0) +
            Number(row.fourFD || 0)
          if (sum !== monthDays[month]) {
            setSnackbarOpen(true)
            setSnackbarData({
              message: `Sum of 4F, 5F, and 4F with Demo for ${month} must be ${monthDays[month]} days. Current sum: ${sum}`,
              severity: 'error',
            })
            setLoading(false)
            return
          }
        }
      }
      // --- END VALIDATION ---

      const validationMessage = validateFields(data, ['remarks'])
      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({ message: validationMessage, severity: 'error' })
        setLoading(false)
        return
      }

      await saveCrackerMaintenanceData(data)
    } catch (err) {
      console.error('Save Cracker Data Error:', err)
    } finally {
      setLoading(false)
    }
  }, [modifiedCells])

  const saveCrackerMaintenanceData = async (newRows) => {
    setLoading(true)
    try {
      const payload = newRows.map((row) => ({
        fourFD: row.fourFD,
        aopYear: AOP_YEAR,
        totalSAD: row.totalSAD,
        monthName: row.monthName ?? null,
        plantId: PLANT_ID,
        numberOfDays: row.numberOfDays,
        demoBBU: row.demoBBU,
        coilReplacement: row.coilReplacement,
        ibr: row.coilReplacement,
        demoSAD: row.demoSAD,
        demoSD: row.demoSD,
        fourF: row.fourF,
        mnt: row.mnt,
        total: row.total,
        fourFHours: row.fourFHours,
        bbu: row.bbu,
        bbd: row.bbd,
        sad: row.sad,
        demoHSS: row.demoHSS,
        fiveF: row.fiveF,
        id: row.idFromApi || row.id,
        shutdown: row.shutdown,
        slowdown: row.slowdown,
        remarks: row.remarks ?? row.remark ?? '',
      }))

      const response =
        await MaintenanceDetailsApiService.saveCrackerMaintenance(
          PLANT_ID,
          AOP_YEAR,
          payload,
          keycloak,
        )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Saved successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Error saving data!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error saving data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }
  const fetchData = useCallback(async () => {
    if (!PLANT_ID || !AOP_YEAR) {
      setRows([])
      return
    }
    setLoading(true)
    try {
      const resp = await dataConfig.serviceFn(keycloak)
      const raw = resp.data
      const formatted = (raw || []).map((item, idx, arr) => ({
        ...item,
        idFromApi: item.id,
        id: idx,
        isEditable: viewOnly
          ? false
          : idx === arr.length - 1
            ? false
            : item?.isEditable,
        originalRemark: item.remarks,
      }))

      const finalData = [...formatted]

      setRows(finalData)
    } catch (err) {
      console.error('Error fetching data:', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, keycloak])

  const handleCalculate = useCallback(async () => {
    try {
      const result =
        await MaintenanceDetailsApiService.handleCalculateMaintenanceCracker(
          PLANT_ID,
          AOP_YEAR,
          keycloak,
        )
      setSnackbarData({
        message:
          result?.code == 200
            ? 'Data refreshed successfully!'
            : 'Data Refresh Failed!',
        severity: result?.code == 200 ? 'success' : 'error',
      })
      setSnackbarOpen(true)
      if (result?.code == 200) fetchData()
    } catch (err) {
      console.error(err)
      setSnackbarData({ message: err.message || 'Error!', severity: 'error' })
      setSnackbarOpen(true)
    }
  }, [keycloak, fetchData])

  useEffect(() => {
    fetchData()
  }, [fetchData, oldYear, yearChanged, PLANT_ID])

  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response
      response = await MaintenanceDetailsApiService.CrackerMaintenanceExport(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
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

  const uploadMaintenance = async (rawFile) => {
    setLoading(true)

    try {
      let response

      response = await MaintenanceDetailsApiService.CrackerMaintenanceImport(
        rawFile,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
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
        link.setAttribute('download', 'Error File - Maintenance Details.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error uploading xcel:', error)
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
    uploadMaintenance(rawFile)
  }
  // Helper to generate monthly fields
  const getMonthlyColumns = () => {
    const months = [
      { field: 'April', index: 4 },
      { field: 'May', index: 5 },
      { field: 'June', index: 6 },
      { field: 'July', index: 7 },
      { field: 'Aug', index: 8 },
      { field: 'Sep', index: 9 },
      { field: 'Oct', index: 10 },
      { field: 'Nov', index: 11 },
      { field: 'Dec', index: 12 },
      { field: 'Jan', index: 1 },
      { field: 'Feb', index: 2 },
      { field: 'Mar', index: 3 },
    ]

    return months.map(({ field, index }) => ({
      field,
      title: headerMap[index],
      type: 'number',
      format: '{0:n2}',
      editable: false,
      align: 'right',
      headerAlign: 'left',
    }))
  }

  // Shared editable field
  const isEditableField = {
    field: 'isEditable',
    title: 'isEditable',
    hidden: true,
  }

  let basecols = crackercolumns

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
      allAction: false,
      uploadExcelBtn: false,
      downloadExcelBtn: false,
    }
  }

  const adjustedPermissions = useMemo(
    () =>
      getAdjustedPermissions(
        {
          showAction: false,
          addButton: false,
          deleteButton: false,
          editButton: false,
          showUnit: false,
          saveWithRemark: false,
          saveBtn: viewOnly ? false : true,
          allAction: true,
          downloadExcelBtn: true,
          uploadExcelBtn: viewOnly ? false : true,
          showRefresh: false,
          showCalculate: viewOnly ? false : true,
          showCalculateVisibility: true,
          showNote: true,
        },
        oldYear?.oldYear,
      ),
    [oldYear],
  )

  return (
    <div>
      <Backdrop
        open={loading}
        sx={{ color: '#fff', zIndex: (t) => t.zIndex.drawer + 1 }}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <KendoDataTables
        columns={basecols}
        rows={rows}
        setRows={setRows}
        fetchData={fetchData}
        handleCalculate={handleCalculate}
        deleteId={deleteId}
        setDeleteId={setDeleteId}
        open1={open1}
        setOpen1={setOpen1}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        snackbarData={snackbarData}
        setSnackbarData={setSnackbarData}
        permissions={adjustedPermissions}
        saveChanges={saveChanges}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        handleRemarkCellClick={handleRemarkCellClick}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        note='*Unit of Measurement - Days'
        supressGridHeight={true}
        handleExcelUpload={handleExcelUpload}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
      />
    </div>
  )
}
export default MaintenanceProcessTable
