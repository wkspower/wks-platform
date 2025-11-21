import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { validateFields } from 'utils/validationUtils'
import crackercolumns from '../../assets/CrackerMaintenanceColumn.json'
import KendoDataTables from './index'
import { MaintenanceDetailsApiService } from 'services/maintenance-details-api-service'
import { getRoleName } from 'services/role-service'
const MaintenanceTable = () => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const keycloak = useSession()
  const READ_ONLY = getRoleName(keycloak)

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
  const AOP_YEAR = year?.selectedYear
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const SCREEN_NAME = screenTitle?.title
  const lowerVertName = vertName?.toLowerCase()
  const dataConfig = useMemo(
    () => ({
      isCracker: lowerVertName === 'cracker',
      serviceFn:
        lowerVertName === 'cracker'
          ? (keycloak, PLANT_ID, AOP_YEAR) =>
              MaintenanceDetailsApiService.getCrackerMaintenanceData(
                keycloak,
                PLANT_ID,
                AOP_YEAR,
              )
          : (keycloak, PLANT_ID, AOP_YEAR) =>
              MaintenanceDetailsApiService.getMaintenanceData(
                keycloak,
                PLANT_ID,
                AOP_YEAR,
              ),
      editable: lowerVertName === 'cracker',
    }),
    [PLANT_ID, AOP_YEAR, lowerVertName],
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
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
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
      let plantId = PLANT_ID
      let year = AOP_YEAR

      const payloadData = newRows.map((row) => ({
        fourFD: row.fourFD,
        aopYear: year,
        totalSAD: row.totalSAD,
        monthName: row.monthName ?? null,
        plantId: plantId,
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
          plantId,
          year,
          payloadData,
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
    if (!PLANT_ID || !AOP_YEAR) return
    setRows([])
    setLoading(true)
    try {
      const resp = await dataConfig.serviceFn(keycloak, PLANT_ID, AOP_YEAR)
      const raw = dataConfig.isCracker ? resp.data?.data : resp
      const monthFields = [
        'April',
        'May',
        'June',
        'July',
        'Aug',
        'Sep',
        'Oct',
        'Nov',
        'Dec',
        'Jan',
        'Feb',
        'Mar',
      ]

      const formatted = (raw || []).map((item, idx) => {
        const allMonthsTotal = monthFields.reduce((sum, month) => {
          const value = parseFloat(item[month]) || 0
          return sum + value
        }, 0)

        return {
          ...item,
          idFromApi: item.id,
          id: idx,
          isEditable: dataConfig.editable,
          originalRemark: item.remarks,
          allMonthsTotal,
        }
      })

      setRows(formatted)
    } catch (err) {
      console.error('Error fetching data:', err)
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [PLANT_ID, AOP_YEAR, keycloak, dataConfig])

  const handleCalculate = useCallback(async () => {
    const plantId = PLANT_ID
    const year = AOP_YEAR
    try {
      const result =
        await MaintenanceDetailsApiService.handleCalculateMaintenance(
          plantId,
          year,
          keycloak,
        )
      setSnackbarData({
        message:
          result === 0
            ? 'Data refreshed successfully!'
            : 'Data Refresh Failed!',
        severity: result === 0 ? 'success' : 'error',
      })
      setSnackbarOpen(true)
      if (result === 0) fetchData()
    } catch (err) {
      console.error(err)
      setSnackbarData({ message: err.message || 'Error!', severity: 'error' })
      setSnackbarOpen(true)
    }
  }, [keycloak, fetchData])

  useEffect(() => {
    fetchData()
  }, [fetchData, oldYear, yearChanged, PLANT_ID])

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

  // Base function to generate column set
  const generateColumns = (nameWidthT) => [
    {
      field: 'Name',
      title: 'Description',
      align: 'left',
      headerAlign: 'left',
      widthT: nameWidthT,
      editable: false,
    },
    ...getMonthlyColumns(),
    isEditableField,
  ]

  const generateColumnsELASTOMER = (nameWidthT) => [
    {
      field: 'Name',
      title: 'Description',
      align: 'left',
      headerAlign: 'left',
      widthT: nameWidthT,
      editable: false,
    },
    ...getMonthlyColumns(),
    isEditableField,
    {
      field: 'allMonthsTotal',
      title: 'Total',
      type: 'number',
      format: '{0:00}',
      editable: false,
    },
  ]

  // Column sets
  const productionColumnsMEG = generateColumns(390)
  const productionColumnsPE = generateColumns(150)
  const productionColumnsPP = generateColumns(220)
  const productionColumnsNonMEG = generateColumns(200)
  const productionColumnsELASTOMER = generateColumnsELASTOMER(200)

  // Column selection
  let basecols

  switch (lowerVertName) {
    case 'cracker':
      basecols = crackercolumns
      break
    case 'meg':
      basecols = productionColumnsMEG
      break
    case 'pe':
      basecols = productionColumnsPE
      break
    case 'pp':
      basecols = productionColumnsPP
      break
    case 'elastomer':
      basecols = productionColumnsELASTOMER
      break
    default:
      basecols = productionColumnsNonMEG
      break
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
      allAction: false,
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
          saveBtn: dataConfig.isCracker,
          allAction: true,
          downloadExcelBtnFromUI: true,
          ExcelName: `Maintenance Details`,
          showRefresh: false,

          showTitleNameBusiness: true,
          titleName: 'Maintenance Details',
        },
        oldYear?.oldYear,
      ),
    [dataConfig.isCracker, oldYear],
  )

  return (
    <>
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
        />
      </div>
    </>
  )
}
export default MaintenanceTable
